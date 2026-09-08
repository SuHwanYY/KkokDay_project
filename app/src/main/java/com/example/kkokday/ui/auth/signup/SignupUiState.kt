package com.example.kkokday.ui.auth.signup

import com.example.kkokday.ui.auth.components.NicknameCheckState

data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val nickname: String = "",
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmError: String? = null,
    val nicknameError: String? = null,
    val nicknameCheckState: NicknameCheckState = NicknameCheckState.NOT_CHECKED,
    val isTermsAgreed: Boolean = false,
    val isPrivacyAgreed: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val showResetPasswordSuggestion: Boolean = false,
    val signUpSuccess: Boolean = false,
    val existingAccountLoginSuccess: Boolean = false,
    val existingUnverifiedAccountDetected: Boolean = false,
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && emailError == null &&
            password.isNotBlank() && passwordError == null &&
            passwordConfirm.isNotBlank() && passwordConfirmError == null &&
            nickname.isNotBlank() && nicknameError == null &&
            nicknameCheckState == NicknameCheckState.AVAILABLE &&
            isTermsAgreed && isPrivacyAgreed
}
