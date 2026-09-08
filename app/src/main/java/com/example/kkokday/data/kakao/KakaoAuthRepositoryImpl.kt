package com.example.kkokday.data.kakao

import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
// TEMP-TEST: 신규가입 플로우 테스트용 — 테스트 끝나면 제거
import com.kakao.sdk.auth.model.Prompt
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

private const val TAG = "KakaoAuthRepository"

/**
 * LoginViewModel의 카카오 로그인 UI 타임아웃(25초)보다 넉넉히 길게 잡은, "진행 중" 표시가
 * 유효한 최대 시간. 이 시간이 지나도 실제 SDK 콜백이 오지 않았다면 그 시도는 죽은 것으로 보고
 * 새 시도를 막지 않는다 — [isLoginInFlight] 플래그를 내리는 콜백 경로 하나를 놓쳐도 앱을 다시
 * 켜기 전까지 로그인이 영구히 막히는 일이 없도록 하는 안전장치.
 */
private const val STALE_LOGIN_ATTEMPT_MILLIS = 40_000L

class KakaoAuthRepositoryImpl @Inject constructor() : KakaoAuthRepository {

    override suspend fun login(context: Context): Result<KakaoUserInfo> {
        Log.d(TAG, "[1] login() 시작")
        val tokenResult = requestToken(context)
        val accessToken = tokenResult.getOrElse {
            Log.d(TAG, "[1] login() 종료 — requestToken 실패: $it")
            return Result.failure(it)
        }.accessToken
        Log.d(TAG, "[5] 토큰 확보, 사용자 정보 조회 시작")
        val userInfoResult = fetchUserInfo(accessToken)
        Log.d(TAG, "[6] login() 종료 — 사용자 정보 조회 성공=${userInfoResult.isSuccess}")
        return userInfoResult
    }

    override suspend fun isTokenValid(): Boolean = suspendCancellableCoroutine { continuation ->
        if (!AuthApiClient.instance.hasToken()) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        UserApiClient.instance.accessTokenInfo { _, error ->
            continuation.resume(error == null)
        }
    }

    override suspend fun logout(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.logout { error ->
            if (error != null) {
                continuation.resume(Result.failure(error))
            } else {
                continuation.resume(Result.success(Unit))
            }
        }
    }

    // 카카오 SDK는 진행 중인 로그인 콜백을 하나만 추적한다. 이전 시도의 콜백이 아직 응답하기 전에
    // (예: LoginViewModel의 25초 타임아웃이 먼저 포기하고 사용자가 재시도) loginWithKakaoTalk/
    // loginWithKakaoAccount를 또 호출하면 먼저 뜬 로그인창과 뒤섞여 로그인/동의창이 반복해서 뜨거나
    // 응답이 영영 오지 않는다. null이 아니면 "그 시각에 시작된 시도가 아직 안 끝났다"는 뜻이고,
    // 실제 SDK 콜백이 도착할 때만(우리 쪽 타임아웃과 무관하게) null로 정리된다 — 다만 콜백 경로 중
    // 하나라도 이 정리를 놓칠 가능성에 대비해 STALE_LOGIN_ATTEMPT_MILLIS가 지나면 강제로 풀어준다.
    @Volatile
    private var loginInFlightSince: Long? = null

    /** 카카오톡 로그인이 가능하면 그쪽을 먼저 시도하고, 실패(취소 제외) 시 카카오계정 로그인으로 넘어간다. */
    private suspend fun requestToken(context: Context): Result<OAuthToken> {
        val now = System.currentTimeMillis()
        val since = loginInFlightSince
        if (since != null && now - since < STALE_LOGIN_ATTEMPT_MILLIS) {
            Log.d(TAG, "[2] requestToken 차단 — ${now - since}ms 전 시작된 시도가 아직 안 끝남")
            return Result.failure(KakaoLoginInProgressException())
        }
        loginInFlightSince = now
        Log.d(TAG, "[2] requestToken 시작 (loginInFlightSince=$now)")

        return suspendCancellableCoroutine { continuation ->
            val finishAttempt: (Result<OAuthToken>) -> Unit = { result ->
                loginInFlightSince = null
                Log.d(
                    TAG,
                    "[4] requestToken 종료 — success=${result.isSuccess}" +
                        (result.exceptionOrNull()?.let { " error=$it" } ?: "") +
                        " continuation.isActive=${continuation.isActive}",
                )
                // continuation은 우리 쪽 타임아웃(withTimeoutOrNull)이 먼저 포기하면 이미 취소돼
                // 있을 수 있다. 취소된 continuation을 resume하면 예외가 나므로 반드시 확인한다.
                if (continuation.isActive) {
                    continuation.resume(result)
                } else {
                    Log.d(TAG, "[4] continuation이 이미 취소돼 있어 resume 생략 — 결과가 버려짐")
                }
            }
            val onAccountResult: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                Log.d(TAG, "[3b] loginWithKakaoAccount 콜백 도착 — token=${token != null}, error=$error")
                when {
                    error != null -> finishAttempt(Result.failure(error))
                    token != null -> finishAttempt(Result.success(token))
                    // 카카오 SDK 계약상 나오면 안 되는 조합(둘 다 null)이지만, 혹시라도 이 콜백이
                    // 이렇게 호출되면 아래 처리가 없는 한 loginInFlightSince가 영영 안 풀리고
                    // continuation도 영원히 suspend된 채 남는다 — 반드시 실패로 종료시킨다.
                    else -> finishAttempt(
                        Result.failure(IllegalStateException("카카오 로그인 응답을 받지 못했어요.")),
                    )
                }
            }

            val kakaoTalkAvailable = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
            Log.d(TAG, "[3] isKakaoTalkLoginAvailable=$kakaoTalkAvailable")
            // TEMP-TEST: 신규가입 플로우 테스트용 — 테스트 끝나면 제거 (아래 "false && " 지우고 kakaoTalkAvailable만 남길 것)
            if (false && kakaoTalkAvailable) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    Log.d(TAG, "[3a] loginWithKakaoTalk 콜백 도착 — token=${token != null}, error=$error")
                    when {
                        error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                            finishAttempt(Result.failure(error))
                        error != null -> {
                            Log.d(TAG, "[3a] 카카오톡 로그인 실패(취소 아님) → 카카오계정 로그인으로 폴백")
                            UserApiClient.instance.loginWithKakaoAccount(context, callback = onAccountResult)
                        }
                        token != null -> finishAttempt(Result.success(token))
                        else -> {
                            Log.d(TAG, "[3a] loginWithKakaoTalk 콜백이 token/error 둘 다 null → 카카오계정으로 폴백")
                            UserApiClient.instance.loginWithKakaoAccount(context, callback = onAccountResult)
                        }
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(
                    context,
                    // TEMP-TEST: 신규가입 플로우 테스트용 — 테스트 끝나면 이 줄 제거
                    prompts = listOf(Prompt.LOGIN),
                    callback = onAccountResult,
                )
            }
        }
    }

    private suspend fun fetchUserInfo(accessToken: String): Result<KakaoUserInfo> =
        suspendCancellableCoroutine { continuation ->
            UserApiClient.instance.me { user, error ->
                Log.d(TAG, "[5a] me() 콜백 도착 — user=${user != null}, error=$error")
                val id = user?.id
                when {
                    error != null -> continuation.resume(Result.failure(error))
                    id != null -> continuation.resume(
                        Result.success(
                            KakaoUserInfo(
                                id = id,
                                nickname = user.kakaoAccount?.profile?.nickname,
                                profileImageUrl = user.kakaoAccount?.profile?.thumbnailImageUrl,
                                accessToken = accessToken,
                            ),
                        ),
                    )
                    else -> continuation.resume(Result.failure(IllegalStateException("카카오 사용자 정보를 받아오지 못했어요.")))
                }
            }
        }
}
