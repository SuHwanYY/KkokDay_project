package com.example.kkokday.ui.auth.verification

data class EmailVerificationUiState(
    val email: String? = null,
    val isChecking: Boolean = false,
    val isResending: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
    /** 인증은 확인됐지만 로컬에 캐시된 닉네임이 없거나 이미 선점돼, 닉네임 설정 화면으로 보내야 한다. */
    val needsNicknameSetup: Boolean = false,
    val nicknameSuggestionForSetup: String? = null,
)
