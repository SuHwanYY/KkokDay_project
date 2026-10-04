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
    /** [인증하기]/재전송 클릭을 처리하는 중. */
    val isVerifyingEmail: Boolean = false,
    /** 이 이메일로 인증 메일을 (최소 한 번) 보냈다 — "메일함에서 링크를..." 안내와 [인증 확인] 버튼을 보여줄지 결정한다. */
    val emailVerificationSent: Boolean = false,
    /** 인증 확인 완료 — [회원가입] 버튼 활성 조건 중 하나. */
    val isEmailVerified: Boolean = false,
    /** [인증 확인] 클릭(또는 앱 포그라운드 복귀 시 자동 확인)을 처리하는 중. */
    val isCheckingEmailVerification: Boolean = false,
    val emailVerificationInfoMessage: String? = null,
    /** 0이면 [인증하기]/재전송을 다시 누를 수 있다. */
    val resendCooldownSeconds: Int = 0,
) {
    val canRequestEmailVerification: Boolean
        get() = email.isNotBlank() && emailError == null &&
            password.isNotBlank() && passwordError == null &&
            passwordConfirm.isNotBlank() && passwordConfirmError == null &&
            !isVerifyingEmail && resendCooldownSeconds == 0

    val isFormValid: Boolean
        get() = email.isNotBlank() && emailError == null &&
            password.isNotBlank() && passwordError == null &&
            passwordConfirm.isNotBlank() && passwordConfirmError == null &&
            nickname.isNotBlank() && nicknameError == null &&
            nicknameCheckState == NicknameCheckState.AVAILABLE &&
            isEmailVerified &&
            isTermsAgreed && isPrivacyAgreed
}
