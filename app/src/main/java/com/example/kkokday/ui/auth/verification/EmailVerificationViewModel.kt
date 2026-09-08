package com.example.kkokday.ui.auth.verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
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
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            isVerified = verified,
                            infoMessage = when {
                                verified -> null
                                notice != null -> notice
                                else -> "아직 인증이 확인되지 않았어요. 메일함을 확인해주세요."
                            },
                        )
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
}
