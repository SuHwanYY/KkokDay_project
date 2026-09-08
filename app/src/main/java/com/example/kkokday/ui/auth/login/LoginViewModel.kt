package com.example.kkokday.ui.auth.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.auth.toAuthErrorMessage
import com.example.kkokday.data.kakao.KakaoAuthRepository
import com.example.kkokday.data.kakao.KakaoFirebaseBridgeRepository
import com.example.kkokday.data.kakao.KakaoUserRepository
import com.example.kkokday.data.kakao.isKakaoLoginCancelled
import com.example.kkokday.data.kakao.toKakaoErrorMessage
import com.example.kkokday.data.session.KakaoSession
import com.example.kkokday.data.session.KakaoSessionRepository
import com.example.kkokday.ui.auth.validation.emailErrorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val KAKAO_LOGIN_TIMEOUT_MILLIS = 25_000L
private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kakaoAuthRepository: KakaoAuthRepository,
    private val kakaoFirebaseBridgeRepository: KakaoFirebaseBridgeRepository,
    private val kakaoUserRepository: KakaoUserRepository,
    private val kakaoSessionRepository: KakaoSessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = emailErrorOrNull(value), generalError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, generalError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (!state.isFormValid || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            authRepository.signIn(state.email.trim(), state.password)
                .onSuccess {
                    // emailVerified는 캐시된 값일 수 있으므로 최신 상태를 다시 받아온 뒤 판단한다.
                    authRepository.reloadCurrentUser()
                    if (authRepository.isCurrentUserEmailVerified()) {
                        _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, needsEmailVerification = true) }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, generalError = error.toAuthErrorMessage())
                    }
                }
        }
    }

    fun onKakaoLoginClick(context: Context) {
        if (_uiState.value.isLoading) {
            Log.d(TAG, "[0] onKakaoLoginClick 무시됨 — 이미 로그인 진행 중(isLoading=true)")
            return
        }

        Log.d(TAG, "[0] onKakaoLoginClick 클릭됨")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            // 카카오계정 로그인은 Chrome 커스텀 탭을 띄우는데, 기기/네트워크 상태에 따라 응답이
            // 오지 않을 수 있다. 그런 경우에도 로그인 버튼이 영원히 로딩 상태로 멈추지 않도록 제한 시간을 둔다.
            val result = withTimeoutOrNull(KAKAO_LOGIN_TIMEOUT_MILLIS) { kakaoAuthRepository.login(context) }
            if (result == null) {
                Log.d(TAG, "[7] ${KAKAO_LOGIN_TIMEOUT_MILLIS}ms 타임아웃 — 응답 없음으로 처리")
                _uiState.update {
                    it.copy(isLoading = false, generalError = "카카오 로그인 응답이 없어요. 잠시 후 다시 시도해주세요.")
                }
                return@launch
            }
            Log.d(TAG, "[7] login() 결과 도착 — success=${result.isSuccess}")

            result
                .onSuccess { userInfo ->
                    Log.d(TAG, "[8] 카카오 로그인 성공 — Firebase 연결 시작 (kakaoId=${userInfo.id})")
                    // Cloud Functions로 카카오 토큰을 검증받아 Firebase 커스텀 토큰을 발급받고,
                    // 그 토큰으로 Firebase Auth에 로그인한다. 이게 성공해야 request.auth가 필요한
                    // 이후 동작(Firestore 규칙)이 전부 동작한다 — 카카오 로그인 자체가 성공했어도
                    // 이 단계가 실패하면 로그인을 완료시키지 않는다.
                    val firebaseSignInError = kakaoFirebaseBridgeRepository
                        .signInWithKakaoAccessToken(userInfo.accessToken)
                        .exceptionOrNull()
                    if (firebaseSignInError != null) {
                        Log.w(TAG, "카카오 로그인 → Firebase 연결 실패", firebaseSignInError)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                generalError = "로그인 처리 중 문제가 발생했어요. 잠시 후 다시 시도해주세요.",
                            )
                        }
                        return@launch
                    }

                    val existingNickname = kakaoUserRepository.findExistingNickname(userInfo.id)
                        .getOrElse { error ->
                            Log.w(TAG, "카카오 프로필 확인 실패", error)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalError = "로그인 처리 중 문제가 발생했어요. 잠시 후 다시 시도해주세요.",
                                )
                            }
                            return@launch
                        }

                    Log.d(TAG, "[9] Firebase 연결 성공, 기존 닉네임=${existingNickname != null}")
                    if (existingNickname == null) {
                        // 최초 로그인 — users/{uid} 프로필이 아직 없다. 닉네임 확인/수정 화면으로
                        // 보내고, 세션 저장과 로그인 완료 처리는 그 화면에서 닉네임을 확정한 뒤에 한다.
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                needsKakaoNicknameSetup = true,
                                kakaoIdForNicknameSetup = userInfo.id,
                                kakaoNicknameSuggestion = userInfo.nickname,
                                kakaoProfileImageUrlForSetup = userInfo.profileImageUrl,
                            )
                        }
                        return@launch
                    }

                    // 재로그인 — 닉네임은 그대로 두고 프로필 사진만 최신화한다. 실패해도 로그인
                    // 자체를 막을 정도는 아니라고 판단해 로그만 남기고 계속 진행한다.
                    val profileSyncWarning = kakaoUserRepository
                        .refreshProfileImage(userInfo.id, userInfo.profileImageUrl)
                        .exceptionOrNull()
                        ?.also { Log.w(TAG, "카카오 프로필 사진 갱신 실패", it) }
                        ?.let { "프로필 정보 동기화에 실패했어요. 문제가 계속되면 다시 로그인해주세요." }

                    kakaoSessionRepository.saveSession(
                        KakaoSession(
                            kakaoId = userInfo.id,
                            nickname = existingNickname,
                            profileImageUrl = userInfo.profileImageUrl,
                        ),
                    )
                    Log.d(TAG, "[10] 재로그인 완료 — loginSuccess=true")
                    _uiState.update {
                        it.copy(isLoading = false, loginSuccess = true, profileSyncWarning = profileSyncWarning)
                    }
                }
                .onFailure { error ->
                    Log.d(TAG, "[F] 카카오 로그인 실패 — cancelled=${error.isKakaoLoginCancelled()} error=$error")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = if (error.isKakaoLoginCancelled()) null else error.toKakaoErrorMessage(),
                        )
                    }
                }
        }
    }

    fun consumeLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    fun consumeNeedsKakaoNicknameSetup() {
        _uiState.update {
            it.copy(
                needsKakaoNicknameSetup = false,
                kakaoIdForNicknameSetup = null,
                kakaoNicknameSuggestion = null,
                kakaoProfileImageUrlForSetup = null,
            )
        }
    }

    fun consumeNeedsEmailVerification() {
        _uiState.update { it.copy(needsEmailVerification = false) }
    }

    fun consumeProfileSyncWarning() {
        _uiState.update { it.copy(profileSyncWarning = null) }
    }
}
