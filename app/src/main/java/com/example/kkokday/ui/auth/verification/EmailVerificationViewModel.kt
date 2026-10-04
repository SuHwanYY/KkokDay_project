package com.example.kkokday.ui.auth.verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.auth.FirstSignupCompletionRepository
import com.example.kkokday.data.auth.FirstSignupCompletionResult
import com.example.kkokday.data.auth.toAuthErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firstSignupCompletionRepository: FirstSignupCompletionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 회원가입 화면에서 "예전에 가입만 시작한 계정" 판정 시 전달하는 일회성 안내 문구.
    private var pendingInitialNotice: String? = savedStateHandle["notice"]

    private val _uiState = MutableStateFlow(
        EmailVerificationUiState(
            email = authRepository.currentUserEmail(),
            infoMessage = pendingInitialNotice,
        ),
    )
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    init {
        checkVerification()
    }

    /** 서버에서 사용자 정보를 새로 받아와 emailVerified 여부를 다시 확인한다. */
    fun checkVerification() {
        // 화면 진입 직후 자동으로 한 번 실행되는 첫 체크에서는, 방금 세팅한 초기 안내 문구를
        // 곧바로 일반 안내 문구로 덮어쓰지 않는다.
        val notice = pendingInitialNotice
        pendingInitialNotice = null

        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, errorMessage = null, infoMessage = notice) }
            authRepository.reloadCurrentUser()
                .onSuccess {
                    val verified = authRepository.isCurrentUserEmailVerified()
                    if (!verified) {
                        _uiState.update {
                            it.copy(
                                isChecking = false,
                                isVerified = false,
                                infoMessage = notice ?: "아직 인증이 확인되지 않았어요. 메일함을 확인해주세요.",
                            )
                        }
                        return@launch
                    }

                    val uid = authRepository.currentUserUid()
                    if (uid == null) {
                        _uiState.update {
                            it.copy(isChecking = false, errorMessage = "로그인 정보를 찾을 수 없어요. 다시 로그인해주세요.")
                        }
                        return@launch
                    }

                    // 인증은 확인됐지만 아직 users/{uid} 프로필이 없을 수 있다(로컬에 캐시해둔
                    // 닉네임으로 예약을 아직 안 끝낸 경우) — 여기서 최초 가입을 마무리한다.
                    when (val completion = firstSignupCompletionRepository.completeIfNeeded(uid)) {
                        is FirstSignupCompletionResult.NeedsNicknameSetup -> _uiState.update {
                            it.copy(
                                isChecking = false,
                                infoMessage = null,
                                needsNicknameSetup = true,
                                nicknameSuggestionForSetup = completion.nicknameSuggestion,
                            )
                        }
                        is FirstSignupCompletionResult.Failed -> _uiState.update {
                            it.copy(isChecking = false, errorMessage = completion.message)
                        }
                        FirstSignupCompletionResult.AlreadyCompleted,
                        is FirstSignupCompletionResult.Completed,
                        -> _uiState.update { it.copy(isChecking = false, isVerified = true, infoMessage = null) }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isChecking = false, errorMessage = error.toAuthErrorMessage()) }
                }
        }
    }

    fun onResendClick() {
        if (_uiState.value.isResending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, errorMessage = null, infoMessage = null) }
            authRepository.sendEmailVerification()
                .onSuccess {
                    _uiState.update { it.copy(isResending = false, infoMessage = "인증 메일을 다시 보냈어요.") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isResending = false, errorMessage = error.toAuthErrorMessage()) }
                }
        }
    }

    fun onSignOutClick() {
        authRepository.signOut()
    }

    fun consumeNeedsNicknameSetup() {
        _uiState.update { it.copy(needsNicknameSetup = false, nicknameSuggestionForSetup = null) }
    }
}
