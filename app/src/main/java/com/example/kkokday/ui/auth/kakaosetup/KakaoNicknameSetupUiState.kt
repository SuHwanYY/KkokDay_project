package com.example.kkokday.ui.auth.kakaosetup

import com.example.kkokday.ui.auth.components.NicknameCheckState

data class KakaoNicknameSetupUiState(
    val nickname: String = "",
    val nicknameError: String? = null,
    val nicknameCheckState: NicknameCheckState = NicknameCheckState.NOT_CHECKED,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val setupComplete: Boolean = false,
    /** 카카오 최초 로그인 경로면 true, 이메일 최초 가입 경로(kakaoId 없음)면 false — 안내 문구를 다르게 보여줄 때 쓴다. */
    val isKakaoFlow: Boolean = true,
) {
    val isFormValid: Boolean
        get() = nickname.isNotBlank() && nicknameError == null && nicknameCheckState == NicknameCheckState.AVAILABLE
}
