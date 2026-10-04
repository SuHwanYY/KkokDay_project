package com.example.kkokday.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.auth.FirstSignupCompletionRepository
import com.example.kkokday.data.auth.FirstSignupCompletionResult
import com.example.kkokday.data.auth.PendingEmailSignup
import com.example.kkokday.data.auth.PendingEmailSignupRepository
import com.example.kkokday.data.auth.toAuthErrorMessage
import com.example.kkokday.data.nickname.NicknameRepository
import com.example.kkokday.ui.auth.components.NicknameCheckState
import com.example.kkokday.ui.auth.validation.emailErrorOrNull
import com.example.kkokday.ui.auth.validation.isValidNickname
import com.example.kkokday.ui.auth.validation.nicknameErrorOrNull
import com.example.kkokday.ui.auth.validation.passwordConfirmErrorOrNull
import com.example.kkokday.ui.auth.validation.signupPasswordErrorOrNull
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val RESEND_COOLDOWN_SECONDS = 180

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val nicknameRepository: NicknameRepository,
    private val pendingEmailSignupRepository: PendingEmailSignupRepository,
    private val firstSignupCompletionRepository: FirstSignupCompletionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun onEmailChange(value: String) {
        cooldownJob?.cancel()
        _uiState.update {
            it.copy(
                email = value,
                emailError = emailErrorOrNull(value),
                generalError = null,
                showResetPasswordSuggestion = false,
                // 이메일을 바꾸면 그 전 인증은 더 이상 유효하지 않다 — 새 이메일로 다시 인증받아야 한다.
                isEmailVerified = false,
                emailVerificationSent = false,
                emailVerificationInfoMessage = null,
                resendCooldownSeconds = 0,
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

    /**
     * [인증하기]/재전송 버튼. 아직 이 이메일로 계정을 만든 적 없으면 계정을 새로 만들고 인증
     * 메일을 보낸다. 이미 이 화면에서 만들어 로그인돼 있는 계정이면(재전송 클릭) 새 계정을
     * 또 만들지 않고 인증 메일만 다시 보낸다.
     */
    fun onVerifyEmailClick() {
        val state = _uiState.value
        if (!state.canRequestEmailVerification) return

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingEmail = true, generalError = null) }

            val email = state.email.trim()
            val password = state.password
            val nickname = state.nickname.trim()

            val alreadySignedInAsThisEmail = authRepository.currentUserUid() != null &&
                authRepository.currentUserEmail() == email
            if (alreadySignedInAsThisEmail) {
                resendVerificationEmail()
                return@launch
            }

            val signUpError = authRepository.signUp(email, password, nickname).exceptionOrNull()
            if (signUpError == null) {
                authRepository.sendEmailVerification()
                _uiState.update {
                    it.copy(
                        isVerifyingEmail = false,
                        emailVerificationSent = true,
                        emailVerificationInfoMessage = "인증 메일을 보냈어요. 메일함에서 링크를 클릭한 뒤 인증 확인을 눌러주세요.",
                    )
                }
                startResendCooldown()
                return@launch
            }

            if (signUpError is FirebaseAuthUserCollisionException) {
                handleEmailAlreadyInUse(email, password)
            } else {
                _uiState.update { it.copy(isVerifyingEmail = false, generalError = signUpError.toAuthErrorMessage()) }
            }
        }
    }

    private suspend fun resendVerificationEmail() {
        authRepository.sendEmailVerification()
            .onSuccess {
                _uiState.update {
                    it.copy(
                        isVerifyingEmail = false,
                        emailVerificationSent = true,
                        emailVerificationInfoMessage = "인증 메일을 다시 보냈어요.",
                    )
                }
                startResendCooldown()
            }
            .onFailure { error ->
                _uiState.update { it.copy(isVerifyingEmail = false, generalError = error.toAuthErrorMessage()) }
            }
    }

    /**
     * auth/email-already-in-use는 "본인이 인증을 끝내지 않은 예전 계정"과 "이미 정상 가입되어
     * 쓰이고 있는 계정(본인 또는 타인)"을 구분하지 못한다. fetchSignInMethodsForEmail은 이메일
     * 열거(enumeration) 방지 정책으로 더 이상 신뢰할 수 있는 결과를 주지 않으므로, 방금 입력한
     * 이메일/비밀번호로 실제 로그인을 시도해 상황을 구분한다.
     */
    private suspend fun handleEmailAlreadyInUse(email: String, password: String) {
        val signInError = authRepository.signIn(email, password).exceptionOrNull()
        if (signInError != null) {
            // 로그인 실패 = 다른 사람이 이미 쓰고 있는 이메일이거나, 본인이 비밀번호를 잊은 경우.
            _uiState.update {
                it.copy(
                    isVerifyingEmail = false,
                    generalError = "이미 가입된 이메일이에요. 비밀번호를 잊으셨다면 재설정해주세요.",
                    showResetPasswordSuggestion = true,
                )
            }
            return
        }

        // emailVerified는 캐시된 값일 수 있으므로 최신 상태를 다시 받아온 뒤 판단한다.
        authRepository.reloadCurrentUser()
        if (authRepository.isCurrentUserEmailVerified()) {
            // 예전에 이 이메일로 가입해 인증까지 끝냈지만(다른 기기 등) 가입을 마무리하지 못한
            // 본인 계정 — 다시 인증 메일을 보낼 필요 없이 바로 인증 완료 상태로 표시한다.
            _uiState.update {
                it.copy(
                    isVerifyingEmail = false,
                    emailVerificationSent = true,
                    isEmailVerified = true,
                    emailVerificationInfoMessage = null,
                )
            }
        } else {
            // 예전에 가입만 시작하고 인증을 끝내지 않은 본인 계정 — 인증 메일을 다시 보내준다.
            resendVerificationEmail()
        }
    }

    /** [인증 확인] 버튼과, 앱이 포그라운드로 돌아왔을 때의 자동 확인이 공유하는 실제 확인 로직. */
    fun onCheckEmailVerificationClick() {
        if (_uiState.value.isCheckingEmailVerification) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingEmailVerification = true, generalError = null) }
            authRepository.reloadCurrentUser()
            val verified = authRepository.isCurrentUserEmailVerified()
            _uiState.update {
                it.copy(
                    isCheckingEmailVerification = false,
                    isEmailVerified = verified,
                    emailVerificationInfoMessage = if (verified) {
                        null
                    } else {
                        "아직 인증이 확인되지 않았어요. 메일함을 확인해주세요."
                    },
                )
            }
        }
    }

    /** 인증 메일을 보낸 적 있고 아직 확인 전일 때만 확인한다 — 화면 진입 직후나 무관한 재개 시 불필요한 호출을 막는다. */
    fun checkEmailVerificationIfPending() {
        val state = _uiState.value
        if (state.emailVerificationSent && !state.isEmailVerified) {
            onCheckEmailVerificationClick()
        }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (remaining in RESEND_COOLDOWN_SECONDS downTo 0) {
                _uiState.update { it.copy(resendCooldownSeconds = remaining) }
                if (remaining > 0) delay(1_000)
            }
        }
    }

    /** [회원가입] 버튼 — 닉네임 중복확인 + 이메일 인증이 모두 끝났을 때만 눌린다. 여기서 실제로 닉네임을 예약하고 가입을 마무리한다. */
    fun onSignUpClick() {
        val state = _uiState.value
        if (!state.isFormValid || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val uid = authRepository.currentUserUid()
            if (uid == null) {
                _uiState.update {
                    it.copy(isLoading = false, generalError = "로그인 정보를 찾을 수 없어요. 처음부터 다시 시도해주세요.")
                }
                return@launch
            }

            val nickname = state.nickname.trim()
            pendingEmailSignupRepository.save(
                PendingEmailSignup(uid = uid, nickname = nickname, profileImageUrl = null),
            )

            // FirstSignupCompletionRepository는 로그인/인증 확인 시점의 자동 감지와 같은 로직을
            // 쓰지만, 여기서는 방금 저장한 최신 닉네임으로 즉시(자동 감지를 기다리지 않고) 실행한다.
            when (val completion = firstSignupCompletionRepository.completeIfNeeded(uid)) {
                is FirstSignupCompletionResult.NeedsNicknameSetup -> _uiState.update {
                    // 중복확인과 실제 예약 사이에 다른 사람이 먼저 이 닉네임을 가져간 경우 —
                    // 화면을 벗어나지 않고 바로 다시 고르게 한다.
                    it.copy(
                        isLoading = false,
                        nicknameCheckState = NicknameCheckState.TAKEN,
                        generalError = "방금 다른 사용자가 먼저 사용한 닉네임이에요. 다른 닉네임으로 다시 확인해주세요.",
                    )
                }
                is FirstSignupCompletionResult.Failed -> _uiState.update {
                    it.copy(isLoading = false, generalError = completion.message)
                }
                FirstSignupCompletionResult.AlreadyCompleted,
                is FirstSignupCompletionResult.Completed,
                -> _uiState.update { it.copy(isLoading = false, signUpSuccess = true) }
            }
        }
    }

    fun consumeSignUpSuccess() {
        _uiState.update { it.copy(signUpSuccess = false) }
    }
}
