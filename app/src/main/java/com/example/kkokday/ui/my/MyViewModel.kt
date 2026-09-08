package com.example.kkokday.ui.my

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.kakao.KakaoAuthRepository
import com.example.kkokday.data.nickname.NicknameRepository
import com.example.kkokday.data.nickname.NicknameTakenException
import com.example.kkokday.data.review.PlaceReviewRepository
import com.example.kkokday.data.session.KakaoSessionRepository
import com.example.kkokday.data.user.UserProfileRepository
import com.example.kkokday.ui.auth.components.NicknameCheckState
import com.example.kkokday.ui.auth.validation.isValidNickname
import com.example.kkokday.ui.auth.validation.nicknameErrorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "MyViewModel"

@HiltViewModel
class MyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kakaoAuthRepository: KakaoAuthRepository,
    private val kakaoSessionRepository: KakaoSessionRepository,
    private val nicknameRepository: NicknameRepository,
    private val userProfileRepository: UserProfileRepository,
    private val placeReviewRepository: PlaceReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
        refreshMyReviews()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true) }
            val uid = authRepository.currentUserUid()
            if (uid == null) {
                _uiState.update { it.copy(isLoadingProfile = false) }
                return@launch
            }
            userProfileRepository.getUserProfile(uid)
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            nickname = profile.nickname,
                            profileImageUrl = profile.profileImageUrl,
                            isLoadingProfile = false,
                        )
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "프로필 조회 실패", error)
                    _uiState.update { it.copy(isLoadingProfile = false, errorMessage = "프로필을 불러오지 못했어요.") }
                }
        }
    }

    // ── 내가 쓴 리뷰 ─────────────────────────────────────────────────────

    fun refreshMyReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReviews = true) }
            val uid = authRepository.currentUserUid()
            if (uid == null) {
                _uiState.update { it.copy(isLoadingReviews = false) }
                return@launch
            }
            placeReviewRepository.getMyReviews(uid)
                .onSuccess { reviews -> _uiState.update { it.copy(myReviews = reviews, isLoadingReviews = false) } }
                .onFailure { error ->
                    Log.w(TAG, "내가 쓴 리뷰 조회 실패", error)
                    _uiState.update { it.copy(isLoadingReviews = false, errorMessage = "리뷰 목록을 불러오지 못했어요.") }
                }
        }
    }

    // ── 닉네임 변경 ──────────────────────────────────────────────────────

    fun onNicknameMenuClick() {
        _uiState.update {
            it.copy(
                showNicknameDialog = true,
                nicknameInput = it.nickname,
                nicknameInputError = null,
                nicknameCheckState = NicknameCheckState.NOT_CHECKED,
                nicknameDialogError = null,
            )
        }
    }

    fun onDismissNicknameDialog() {
        _uiState.update { it.copy(showNicknameDialog = false, nicknameDialogError = null) }
    }

    fun onNicknameInputChange(value: String) {
        if (value.length > 10) return
        _uiState.update {
            it.copy(
                nicknameInput = value,
                nicknameInputError = nicknameErrorOrNull(value),
                // 닉네임을 바꾸면 이전 중복확인 결과는 더 이상 유효하지 않으므로 초기화한다.
                nicknameCheckState = NicknameCheckState.NOT_CHECKED,
                nicknameDialogError = null,
            )
        }
    }

    fun onCheckNicknameClick() {
        val state = _uiState.value
        val nickname = state.nicknameInput.trim()
        if (!isValidNickname(nickname)) return
        if (state.nicknameCheckState == NicknameCheckState.CHECKING) return

        // 지금 쓰고 있는 닉네임 그대로면 중복확인을 할 필요가 없다 — 이미 내 것이다.
        if (nickname == state.nickname) {
            _uiState.update { it.copy(nicknameCheckState = NicknameCheckState.AVAILABLE) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(nicknameCheckState = NicknameCheckState.CHECKING) }
            nicknameRepository.isNicknameAvailable(nickname)
                .onSuccess { available ->
                    _uiState.update {
                        it.copy(
                            nicknameCheckState = if (available) {
                                NicknameCheckState.AVAILABLE
                            } else {
                                NicknameCheckState.TAKEN
                            },
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(nicknameCheckState = NicknameCheckState.ERROR) } }
        }
    }

    fun onConfirmNicknameChange() {
        val state = _uiState.value
        if (!state.isNicknameChangeConfirmEnabled) return
        val newNickname = state.nicknameInput.trim()
        val oldNickname = state.nickname
        val uid = authRepository.currentUserUid() ?: return

        _uiState.update { it.copy(isSavingNickname = true) }
        viewModelScope.launch {
            nicknameRepository.changeNickname(uid, oldNickname, newNickname)
                .onSuccess {
                    // Firebase Auth displayName(이메일 로그인 홈 인사말)과 KakaoSession 캐시(카카오
                    // 로그인 홈 인사말)도 같이 맞춰야, 홈 화면에 예전 닉네임이 남아있지 않는다.
                    authRepository.updateDisplayName(newNickname)
                    kakaoSessionRepository.kakaoSession.first()?.let { session ->
                        kakaoSessionRepository.saveSession(session.copy(nickname = newNickname))
                    }
                    _uiState.update {
                        it.copy(
                            nickname = newNickname,
                            showNicknameDialog = false,
                            isSavingNickname = false,
                            toastMessage = "닉네임을 변경했어요",
                        )
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "닉네임 변경 실패", error)
                    val isTaken = error is NicknameTakenException
                    _uiState.update {
                        it.copy(
                            isSavingNickname = false,
                            nicknameCheckState = if (isTaken) NicknameCheckState.TAKEN else it.nicknameCheckState,
                            // 이 다이얼로그는 모달로 떠 있어 Scaffold의 스낵바를 가리므로,
                            // errorMessage(스낵바)가 아니라 다이얼로그 안에서 바로 보여준다.
                            nicknameDialogError = if (isTaken) {
                                "방금 다른 사용자가 먼저 사용한 닉네임이에요."
                            } else {
                                "닉네임 변경에 실패했어요. 다시 시도해주세요."
                            },
                        )
                    }
                }
        }
    }

    // ── 프로필 사진 변경 ──────────────────────────────────────────────────

    fun onProfileImagePicked(uri: Uri) {
        val uid = authRepository.currentUserUid() ?: return
        _uiState.update { it.copy(isUploadingProfileImage = true) }
        viewModelScope.launch {
            userProfileRepository.updateProfileImage(uid, uri)
                .onSuccess { url ->
                    _uiState.update {
                        it.copy(
                            profileImageUrl = url,
                            isUploadingProfileImage = false,
                            toastMessage = "프로필 사진을 변경했어요",
                        )
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "프로필 사진 변경 실패", error)
                    _uiState.update {
                        it.copy(isUploadingProfileImage = false, errorMessage = "프로필 사진 변경에 실패했어요. 다시 시도해주세요.")
                    }
                }
        }
    }

    // ── 로그아웃 ────────────────────────────────────────────────────────

    fun onLogoutMenuClick() {
        _uiState.update { it.copy(showSignOutConfirm = true) }
    }

    fun onDismissSignOutConfirm() {
        _uiState.update { it.copy(showSignOutConfirm = false) }
    }

    fun onConfirmSignOut() {
        _uiState.update { it.copy(showSignOutConfirm = false) }
        viewModelScope.launch {
            // 카카오 로그인도 Cloud Functions를 거쳐 실제 Firebase Auth 세션을 만들기 때문에,
            // 카카오/이메일 세션 어느 쪽이든 Firebase 로그아웃까지 항상 같이 처리해야 한다.
            if (kakaoSessionRepository.kakaoSession.first() != null) {
                kakaoAuthRepository.logout()
                kakaoSessionRepository.clearSession()
            }
            authRepository.signOut()
            _uiState.update { it.copy(signedOut = true) }
        }
    }

    // ── 회원탈퇴 ────────────────────────────────────────────────────────

    fun onDeleteAccountMenuClick() {
        _uiState.update { it.copy(showDeleteAccountConfirm = true) }
    }

    fun onDismissDeleteAccountConfirm() {
        _uiState.update { it.copy(showDeleteAccountConfirm = false) }
    }

    fun onConfirmDeleteAccount() {
        _uiState.update { it.copy(showDeleteAccountConfirm = false, isDeletingAccount = true) }
        viewModelScope.launch {
            userProfileRepository.deleteAccount()
                .onSuccess {
                    // 서버(Cloud Functions)에서 이미 Firebase Auth 계정 자체를 지웠지만, 로컬
                    // FirebaseAuth 클라이언트는 이걸 즉시 알지 못해 currentUser가 한동안 남아있을
                    // 수 있다 — signOut()으로 로컬 세션도 명시적으로 정리한다.
                    if (kakaoSessionRepository.kakaoSession.first() != null) {
                        kakaoAuthRepository.logout()
                        kakaoSessionRepository.clearSession()
                    }
                    authRepository.signOut()
                    _uiState.update { it.copy(isDeletingAccount = false, signedOut = true) }
                }
                .onFailure { error ->
                    Log.w(TAG, "회원탈퇴 실패", error)
                    _uiState.update {
                        it.copy(isDeletingAccount = false, errorMessage = "계정 삭제에 실패했어요. 다시 시도해주세요.")
                    }
                }
        }
    }

    // ── 그 외 메뉴 ──────────────────────────────────────────────────────

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun consumeSignedOut() {
        _uiState.update { it.copy(signedOut = false) }
    }
}
