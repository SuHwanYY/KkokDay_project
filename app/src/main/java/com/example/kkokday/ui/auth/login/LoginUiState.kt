package com.example.kkokday.ui.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val loginSuccess: Boolean = false,
    val needsEmailVerification: Boolean = false,
    val profileSyncWarning: String? = null,
    /** 카카오 최초 로그인이라 users/{uid} 프로필이 아직 없을 때, 닉네임 설정 화면으로 보내기 위한 정보. */
    val needsKakaoNicknameSetup: Boolean = false,
    val kakaoIdForNicknameSetup: Long? = null,
    val kakaoNicknameSuggestion: String? = null,
    val kakaoProfileImageUrlForSetup: String? = null,
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && emailError == null
}
