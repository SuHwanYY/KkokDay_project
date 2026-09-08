package com.example.kkokday.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.auth.toAuthErrorMessage
import com.example.kkokday.data.nickname.NicknameRepository
import com.example.kkokday.data.nickname.NicknameTakenException
import com.example.kkokday.ui.auth.components.NicknameCheckState
import com.example.kkokday.ui.auth.validation.emailErrorOrNull
import com.example.kkokday.ui.auth.validation.isValidNickname
import com.example.kkokday.ui.auth.validation.nicknameErrorOrNull
import com.example.kkokday.ui.auth.validation.passwordConfirmErrorOrNull
import com.example.kkokday.ui.auth.validation.signupPasswordErrorOrNull
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val nicknameRepository: NicknameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = emailErrorOrNull(value),
                generalError = null,
                showResetPasswordSuggestion = false,
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = signupPasswordErrorOrNull(value),
                passwordConfirmError = passwordConfirmErrorOrNull(value, it.passwordConfirm),
                generalError = null,
                showResetPasswordSuggestion = false,
            )
        }
    }

    fun onPasswordConfirmChange(value: String) {
        _uiState.update {
            it.copy(
                passwordConfirm = value,
                passwordConfirmError = passwordConfirmErrorOrNull(it.password, value),
                generalError = null,
                showResetPasswordSuggestion = false,
            )
        }
    }

    fun onNicknameChange(value: String) {
        if (value.length > 10) return
        _uiState.update {
            it.copy(
                nickname = value,
                nicknameError = nicknameErrorOrNull(value),
                // 닉네임을 바꾸면 이전 중복확인 결과는 더 이상 유효하지 않으므로 초기화한다.
                nicknameCheckState = NicknameCheckState.NOT_CHECKED,
                generalError = null,
            )
        }
    }

    fun onCheckNicknameClick() {
        val nickname = _uiState.value.nickname.trim()
        if (!isValidNickname(nickname)) return
        if (_uiState.value.nicknameCheckState == NicknameCheckState.CHECKING) return

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
                .onFailure {
                    _uiState.update { it.copy(nicknameCheckState = NicknameCheckState.ERROR) }
                }
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onTogglePasswordConfirmVisibility() {
        _uiState.update { it.copy(isPasswordConfirmVisible = !it.isPasswordConfirmVisible) }
    }

    fun onToggleTermsAgreed() {
        _uiState.update { it.copy(isTermsAgreed = !it.isTermsAgreed) }
    }

    fun onTogglePrivacyAgreed() {
        _uiState.update { it.copy(isPrivacyAgreed = !it.isPrivacyAgreed) }
    }

    fun onSignUpClick() {
        val state = _uiState.value
        if (!state.isFormValid || state.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, generalError = null, showResetPasswordSuggestion = false)
            }

            val email = state.email.trim()
            val password = state.password
            val nickname = state.nickname.trim()

            val signUpError = authRepository.signUp(email, password, nickname).exceptionOrNull()
            if (signUpError != null) {
                if (signUpError is FirebaseAuthUserCollisionException) {
                    handleEmailAlreadyInUse(email, password)
                } else {
                    _uiState.update { it.copy(isLoading = false, generalError = signUpError.toAuthErrorMessage()) }
                }
                return@launch
            }

            // Firebase Auth 계정 생성과 Firestore 닉네임/프로필 예약은 서로 다른 시스템이라
            // 하나의 트랜잭션으로 묶을 수 없다. 계정을 먼저 만들고, 닉네임 예약(Firestore
            // 트랜잭션)이 실패하면 방금 만든 계정을 삭제해 되돌린다.
            val uid = authRepository.currentUserUid()
            val reserveResult = if (uid != null) {
                nicknameRepository.reserveNickname(nickname, uid)
            } else {
                Result.failure(IllegalStateException("가입 직후 사용자 정보를 찾을 수 없어요."))
            }

            val reserveError = reserveResult.exceptionOrNull()
            if (reserveError != null) {
                authRepository.deleteCurrentUser()
                val isTaken = reserveError is NicknameTakenException
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nicknameCheckState = if (isTaken) NicknameCheckState.TAKEN else it.nicknameCheckState,
                        generalError = if (isTaken) {
                            "방금 다른 사용자가 먼저 사용한 닉네임이에요. 다른 닉네임으로 다시 시도해주세요."
                        } else {
                            "회원가입 중 오류가 발생했어요. 잠시 후 다시 시도해주세요."
                        },
                    )
                }
                return@launch
            }

            // 인증 메일 발송이 실패하더라도 계정/닉네임은 이미 확정됐으므로 가입 자체는 성공으로
            // 처리한다. 사용자는 다음 화면에서 재전송 버튼으로 다시 시도할 수 있다.
            authRepository.sendEmailVerification()
            _uiState.update { it.copy(isLoading = false, signUpSuccess = true) }
        }
    }

    /**
     * auth/email-already-in-use는 "본인이 인증을 끝내지 않은 예전 계정"과 "이미 정상 가입되어
     * 쓰이고 있는 계정(본인 또는 타인)"을 구분하지 못한다. fetchSignInMethodsForEmail은 이메일
     * 열거(enumeration) 방지 정책으로 더 이상 신뢰할 수 있는 결과를 주지 않으므로, 방금 입력한
     * 이메일/비밀번호로 실제 로그인을 시도해 세 가지 상황을 구분한다.
     */
    private suspend fun handleEmailAlreadyInUse(email: String, password: String) {
        val signInError = authRepository.signIn(email, password).exceptionOrNull()
        if (signInError != null) {
            // 로그인 실패 = 다른 사람이 이미 쓰고 있는 이메일이거나, 본인이 비밀번호를 잊은 경우.
            _uiState.update {
                it.copy(
                    isLoading = false,
                    generalError = "이미 가입된 이메일이에요. 비밀번호를 잊으셨다면 재설정해주세요.",
                    showResetPasswordSuggestion = true,
                )
            }
            return
        }

        // emailVerified는 캐시된 값일 수 있으므로 최신 상태를 다시 받아온 뒤 판단한다.
        authRepository.reloadCurrentUser()
        if (authRepository.isCurrentUserEmailVerified()) {
            // 이미 인증까지 끝난 본인 계정 — 로그인 상태 그대로 홈으로 보내준다.
            _uiState.update { it.copy(isLoading = false, existingAccountLoginSuccess = true) }
        } else {
            // 예전에 가입만 시작하고 인증을 끝내지 않은 본인 계정 — 인증 메일을 다시 보내준다.
            authRepository.sendEmailVerification()
            _uiState.update { it.copy(isLoading = false, existingUnverifiedAccountDetected = true) }
        }
    }

    fun consumeSignUpSuccess() {
        _uiState.update { it.copy(signUpSuccess = false) }
    }

    fun consumeExistingAccountLoginSuccess() {
        _uiState.update { it.copy(existingAccountLoginSuccess = false) }
    }

    fun consumeExistingUnverifiedAccountDetected() {
        _uiState.update { it.copy(existingUnverifiedAccountDetected = false) }
    }
}
