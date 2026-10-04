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
    /**
     * 최초 가입이라 users/{uid} 프로필이 아직 없을 때, 닉네임 설정 화면으로 보내기 위한 정보.
     * 카카오 로그인이면 [kakaoIdForNicknameSetup]이 채워지고, 이메일 로그인(로컬에 캐시해둔
     * 닉네임이 없거나 이미 선점된 경우)이면 null로 남는다 — 화면/ViewModel이 이 값으로 두
     * 경로를 구분한다.
     */
    val needsNicknameSetup: Boolean = false,
    val kakaoIdForNicknameSetup: Long? = null,
    val nicknameSuggestionForSetup: String? = null,
    val profileImageUrlForSetup: String? = null,
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && emailError == null
}
