package com.example.kkokday.data.user

import android.content.Context
import android.net.Uri
import com.example.kkokday.data.auth.runAuthCatching
import com.example.kkokday.data.media.compressImageForUpload
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val PROFILE_IMAGES_STORAGE_ROOT = "profileImages"
private const val PROFILE_IMAGE_FILE_NAME = "avatar.jpg"
private const val DELETE_ACCOUNT_FUNCTION_NAME = "deleteAccount"

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: Lazy<FirebaseFirestore>,
    private val storage: Lazy<FirebaseStorage>,
    private val functions: Lazy<FirebaseFunctions>,
) : UserProfileRepository {

    override suspend fun getUserProfile(uid: String): Result<UserProfile> = runAuthCatching {
        val snapshot = userDoc(uid).get().await()
        UserProfile(
            nickname = snapshot.getString("nickname").orEmpty(),
            profileImageUrl = snapshot.getString("profileImageUrl"),
        )
    }

    override suspend fun updateProfileImage(uid: String, imageUri: Uri): Result<String> = runAuthCatching {
        val compressed = compressImageForUpload(context, imageUri)
        val ref = storage.get().reference
            .child(PROFILE_IMAGES_STORAGE_ROOT)
            .child(uid)
            .child(PROFILE_IMAGE_FILE_NAME)
        ref.putBytes(compressed).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        userDoc(uid).set(mapOf("profileImageUrl" to downloadUrl), SetOptions.merge()).await()
        downloadUrl
    }

    override suspend fun deleteAccount(): Result<Unit> = runAuthCatching {
        functions.get().getHttpsCallable(DELETE_ACCOUNT_FUNCTION_NAME).call().await()
        Unit
    }

    private fun userDoc(uid: String) = firestore.get().collection(USERS_COLLECTION).document(uid)
}
