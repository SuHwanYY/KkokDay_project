package com.example.kkokday.data.auth

interface AuthRepository {

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signUp(email: String, password: String, nickname: String): Result<Unit>

    suspend fun sendEmailVerification(): Result<Unit>

    /** 서버에 저장된 최신 사용자 정보(emailVerified 포함)를 다시 받아온다. */
    suspend fun reloadCurrentUser(): Result<Unit>

    fun isCurrentUserEmailVerified(): Boolean

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /** oobCode가 유효한지 확인하고, 유효하면 연결된 이메일 주소를 반환한다. */
    suspend fun verifyPasswordResetCode(oobCode: String): Result<String>

    suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit>

    /** 닉네임 예약 등 가입 후속 처리가 실패했을 때 계정 생성을 되돌리기 위해 사용한다. */
    suspend fun deleteCurrentUser(): Result<Unit>

    fun currentUserUid(): String?

    fun currentUserEmail(): String?

    fun currentUserNickname(): String?

    /** 이메일 로그인 사용자의 홈 인사말 등이 읽는 Firebase Auth displayName을 갱신한다. */
    suspend fun updateDisplayName(nickname: String): Result<Unit>

    fun signOut()
}
