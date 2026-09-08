package com.example.kkokday.data.kakao

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import dagger.Lazy
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

private const val VERIFY_KAKAO_FUNCTION_NAME = "verifyKakaoAndMintFirebaseToken"

class KakaoFirebaseBridgeRepositoryImpl @Inject constructor(
    private val functions: Lazy<FirebaseFunctions>,
    private val firebaseAuth: Lazy<FirebaseAuth>,
) : KakaoFirebaseBridgeRepository {

    override suspend fun signInWithKakaoAccessToken(kakaoAccessToken: String): Result<Unit> = runAuthCatching {
        val callResult = functions.get()
            .getHttpsCallable(VERIFY_KAKAO_FUNCTION_NAME)
            .call(mapOf("kakaoAccessToken" to kakaoAccessToken))
            .await()

        @Suppress("UNCHECKED_CAST")
        val data = callResult.data as? Map<String, Any?>
        val customToken = data?.get("customToken") as? String
            ?: throw IllegalStateException("Cloud Functions 응답에서 customToken을 찾을 수 없어요.")

        firebaseAuth.get().signInWithCustomToken(customToken).await()
        Unit
    }
}
