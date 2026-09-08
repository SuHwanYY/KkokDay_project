package com.example.kkokday.ui.auth.passwordreset

data class ResetPasswordUiState(
    val isVerifying: Boolean = true,
    val isCodeValid: Boolean = false,
    val email: String? = null,
    val verifyError: String? = null,
    val newPassword: String = "",
    val newPasswordConfirm: String = "",
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    val newPasswordError: String? = null,
    val newPasswordConfirmError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val resetSuccess: Boolean = false,
) {
    val isFormValid: Boolean
        get() = newPassword.isNotBlank() && newPasswordError == null &&
            newPasswordConfirm.isNotBlank() && newPasswordConfirmError == null
}
