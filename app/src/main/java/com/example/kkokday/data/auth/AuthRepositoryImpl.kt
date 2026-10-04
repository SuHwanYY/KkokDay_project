package com.example.kkokday.data.auth

import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.Lazy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: Lazy<FirebaseAuth>
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Unit> = runAuthCatching {
        firebaseAuth.get().signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun signUp(email: String, password: String, nickname: String): Result<Unit> = runAuthCatching {
        val authResult = firebaseAuth.get().createUserWithEmailAndPassword(email, password).await()
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(nickname)
            .build()
        authResult.user?.updateProfile(profileUpdate)?.await()
        Unit
    }

    override suspend fun sendEmailVerification(): Result<Unit> = runAuthCatching {
        firebaseAuth.get().currentUser?.sendEmailVerification()?.await()
        Unit
    }

    override suspend fun reloadCurrentUser(): Result<Unit> = runAuthCatching {
        firebaseAuth.get().currentUser?.reload()?.await()
        Unit
    }

    override fun isCurrentUserEmailVerified(): Boolean =
        firebaseAuth.get().currentUser?.isEmailVerified == true

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runAuthCatching {
        val settings = ActionCodeSettings.newBuilder()
            .setUrl(PasswordResetConfig.PASSWORD_RESET_CONTINUE_URL)
            .setHandleCodeInApp(true)
            .build()
        firebaseAuth.get().sendPasswordResetEmail(email, settings).await()
        Unit
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): Result<String> = runAuthCatching {
        firebaseAuth.get().verifyPasswordResetCode(oobCode).await()
    }

    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> = runAuthCatching {
        firebaseAuth.get().confirmPasswordReset(oobCode, newPassword).await()
        Unit
    }

    override fun currentUserUid(): String? = firebaseAuth.get().currentUser?.uid

    override fun currentUserEmail(): String? = firebaseAuth.get().currentUser?.email

    override fun currentUserNickname(): String? = firebaseAuth.get().currentUser?.displayName

    override suspend fun updateDisplayName(nickname: String): Result<Unit> = runAuthCatching {
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(nickname)
            .build()
        firebaseAuth.get().currentUser?.updateProfile(profileUpdate)?.await()
        Unit
    }

    override fun signOut() {
        firebaseAuth.get().signOut()
    }
}

internal suspend fun <T> runAuthCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Exception) {
    Result.failure(error)
}
