package com.example.kkokday.data.user

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val BLOCKED_AUTHORS_COLLECTION = "blockedAuthors"

@Singleton
class BlockedAuthorRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : BlockedAuthorRepository {

    override suspend fun getBlockedAuthorIds(uid: String): Result<Set<String>> = runAuthCatching {
        blockedAuthorsCollection(uid).get().await().documents.map { it.id }.toSet()
    }

    override suspend fun blockAuthor(uid: String, authorUid: String, authorNickname: String): Result<Unit> = runAuthCatching {
        val data = mapOf(
            "authorNickname" to authorNickname,
            "blockedAt" to FieldValue.serverTimestamp(),
        )
        blockedAuthorsCollection(uid).document(authorUid).set(data, SetOptions.merge()).await()
        Unit
    }

    private fun blockedAuthorsCollection(uid: String) = firestore.get()
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(BLOCKED_AUTHORS_COLLECTION)
}
