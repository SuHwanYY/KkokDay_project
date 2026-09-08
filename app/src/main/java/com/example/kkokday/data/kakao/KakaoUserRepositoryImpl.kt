package com.example.kkokday.data.kakao

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.Lazy
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val KAKAO_UID_PREFIX = "kakao:"

class KakaoUserRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : KakaoUserRepository {

    override suspend fun findExistingNickname(kakaoId: Long): Result<String?> = runAuthCatching {
        val snapshot = firestore.get()
            .collection(USERS_COLLECTION)
            .document("$KAKAO_UID_PREFIX$kakaoId")
            .get()
            .await()
        snapshot.getString("nickname")
    }

    override suspend fun refreshProfileImage(kakaoId: Long, profileImageUrl: String?): Result<Unit> = runAuthCatching {
        firestore.get()
            .collection(USERS_COLLECTION)
            .document("$KAKAO_UID_PREFIX$kakaoId")
            .set(
                mapOf(
                    "profileImageUrl" to profileImageUrl,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
        Unit
    }
}
