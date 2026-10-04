package com.example.kkokday.data.nickname

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Lazy
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

private const val NICKNAMES_COLLECTION = "nicknames"
private const val USERS_COLLECTION = "users"

class NicknameRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : NicknameRepository {

    override suspend fun isNicknameAvailable(nickname: String): Result<Boolean> = runAuthCatching {
        val snapshot = firestore.get()
            .collection(NICKNAMES_COLLECTION)
            .document(nickname)
            .get()
            .await()
        !snapshot.exists()
    }

    override suspend fun hasUserProfile(uid: String): Result<Boolean> = runAuthCatching {
        firestore.get()
            .collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
            .exists()
    }

    override suspend fun reserveNickname(
        nickname: String,
        uid: String,
        profileImageUrl: String?,
    ): Result<Unit> = runAuthCatching {
        val db = firestore.get()
        db.runTransaction { transaction ->
            val nicknameRef = db.collection(NICKNAMES_COLLECTION).document(nickname)
            val userRef = db.collection(USERS_COLLECTION).document(uid)

            if (transaction.get(nicknameRef).exists()) {
                throw NicknameTakenException()
            }

            transaction.set(nicknameRef, mapOf("uid" to uid))
            transaction.set(
                userRef,
                mapOf(
                    "nickname" to nickname,
                    "profileImageUrl" to profileImageUrl,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )
        }.await()
        Unit
    }

    override suspend fun changeNickname(uid: String, oldNickname: String, newNickname: String): Result<Unit> =
        runAuthCatching {
            if (oldNickname == newNickname) return@runAuthCatching Unit

            val db = firestore.get()
            db.runTransaction { transaction ->
                val newNicknameRef = db.collection(NICKNAMES_COLLECTION).document(newNickname)
                val oldNicknameRef = db.collection(NICKNAMES_COLLECTION).document(oldNickname)
                val userRef = db.collection(USERS_COLLECTION).document(uid)

                if (transaction.get(newNicknameRef).exists()) {
                    throw NicknameTakenException()
                }

                transaction.set(newNicknameRef, mapOf("uid" to uid))
                transaction.delete(oldNicknameRef)
                transaction.update(userRef, mapOf("nickname" to newNickname))
            }.await()
            Unit
        }
}
