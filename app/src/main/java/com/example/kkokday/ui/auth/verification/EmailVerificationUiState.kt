package com.example.kkokday.ui.auth.verification

data class EmailVerificationUiState(
    val email: String? = null,
    val isChecking: Boolean = false,
    val isResending: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
)
