package com.example.kkokday.ui.auth.passwordreset

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val isEmailSent: Boolean = false,
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && emailError == null
}
