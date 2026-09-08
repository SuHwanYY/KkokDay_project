package com.example.kkokday.ui.auth.kakaosetup

import com.example.kkokday.ui.auth.components.NicknameCheckState

data class KakaoNicknameSetupUiState(
    val nickname: String = "",
    val nicknameError: String? = null,
    val nicknameCheckState: NicknameCheckState = NicknameCheckState.NOT_CHECKED,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val setupComplete: Boolean = false,
) {
    val isFormValid: Boolean
        get() = nickname.isNotBlank() && nicknameError == null && nicknameCheckState == NicknameCheckState.AVAILABLE
}
