package com.example.kkokday.ui.my

import com.example.kkokday.data.review.PlaceReview
import com.example.kkokday.ui.auth.components.NicknameCheckState

data class MyUiState(
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val isLoadingProfile: Boolean = true,
    val isUploadingProfileImage: Boolean = false,

    /** "내가 쓴 리뷰" 섹션 — 여러 장소의 리뷰를 collectionGroup 쿼리로 한 번에 모은 목록(최근 수정순). */
    val myReviews: List<PlaceReview> = emptyList(),
    val isLoadingReviews: Boolean = true,

    // 닉네임 변경 다이얼로그 — SignupScreen/KakaoNicknameSetupScreen과 같은
    // 중복확인 흐름([NicknameCheckState])을 그대로 쓴다.
    val showNicknameDialog: Boolean = false,
    val nicknameInput: String = "",
    val nicknameInputError: String? = null,
    val nicknameCheckState: NicknameCheckState = NicknameCheckState.NOT_CHECKED,
    val isSavingNickname: Boolean = false,
    /**
     * 닉네임 변경 실패 메시지를 다이얼로그 안에 바로 보여준다 — 이 다이얼로그는 모달로 떠서
     * [errorMessage]용 스낵바(Scaffold 쪽)를 가려버리기 때문에, 스낵바만 믿으면 사용자
     * 눈에는 "아무 반응도 없는" 것처럼 보인다.
     */
    val nicknameDialogError: String? = null,

    val showSignOutConfirm: Boolean = false,

    val showDeleteAccountConfirm: Boolean = false,
    val isDeletingAccount: Boolean = false,

    /** true가 되면 화면이 로그인 화면으로 이동하고 소비(false로)한다 — 로그아웃/회원탈퇴 공통. */
    val signedOut: Boolean = false,

    val errorMessage: String? = null,
    val toastMessage: String? = null,
) {
    val isNicknameChangeConfirmEnabled: Boolean
        get() = nicknameInput.trim().isNotBlank() &&
            nicknameInputError == null &&
            (nicknameCheckState == NicknameCheckState.AVAILABLE) &&
            !isSavingNickname
}
