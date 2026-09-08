package com.example.kkokday.ui.auth.passwordreset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.auth.toAuthErrorMessage
import com.example.kkokday.ui.auth.validation.passwordConfirmErrorOrNull
import com.example.kkokday.ui.auth.validation.signupPasswordErrorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val oobCode: String? = savedStateHandle["oobCode"]

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    init {
        verifyCode()
    }

    private fun verifyCode() {
        val code = oobCode
        if (code.isNullOrBlank()) {
            _uiState.update {
                it.copy(isVerifying = false, isCodeValid = false, verifyError = "유효하지 않은 링크예요.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, verifyError = null) }
            authRepository.verifyPasswordResetCode(code)
                .onSuccess { email ->
                    _uiState.update { it.copy(isVerifying = false, isCodeValid = true, email = email) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isVerifying = false, isCodeValid = false, verifyError = error.toAuthErrorMessage())
                    }
                }
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                newPassword = value,
                newPasswordError = signupPasswordErrorOrNull(value),
                newPasswordConfirmError = passwordConfirmErrorOrNull(value, it.newPasswordConfirm),
                submitError = null,
            )
        }
    }

    fun onNewPasswordConfirmChange(value: String) {
        _uiState.update {
            it.copy(
                newPasswordConfirm = value,
                newPasswordConfirmError = passwordConfirmErrorOrNull(it.newPassword, value),
                submitError = null,
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onTogglePasswordConfirmVisibility() {
        _uiState.update { it.copy(isPasswordConfirmVisible = !it.isPasswordConfirmVisible) }
    }

    fun onSubmitClick() {
        val state = _uiState.value
        val code = oobCode
        if (!state.isFormValid || state.isSubmitting || code == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            authRepository.confirmPasswordReset(code, state.newPassword)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, resetSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = error.toAuthErrorMessage()) }
                }
        }
    }
}
