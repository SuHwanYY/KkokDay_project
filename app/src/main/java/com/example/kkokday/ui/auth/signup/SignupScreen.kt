package com.example.kkokday.ui.auth.signup

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.directions.ContactActionLauncher
import com.example.kkokday.ui.auth.components.EmailVerificationField
import com.example.kkokday.ui.auth.components.NicknameCheckState
import com.example.kkokday.ui.auth.components.NicknameField
import com.example.kkokday.ui.auth.components.PRIVACY_POLICY_URL
import com.example.kkokday.ui.auth.components.PasswordOutlinedTextField
import com.example.kkokday.ui.auth.components.TERMS_OF_SERVICE_URL
import com.example.kkokday.ui.auth.components.TermsCheckboxRow
import com.example.kkokday.ui.theme.KkokDayBackgroundDark
import com.example.kkokday.ui.theme.KkokDayCream
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySubTextDark
import com.example.kkokday.ui.theme.KkokDaySubTextLight
import com.example.kkokday.ui.theme.KkokDayTextBlack
import com.example.kkokday.ui.theme.KkokDayTextWhite
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun SignupScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.signUpSuccess) {
        if (uiState.signUpSuccess) {
            viewModel.consumeSignUpSuccess()
            onSignUpSuccess()
        }
    }

    // 메일 앱에서 링크를 누르고 다시 콕데이로 돌아왔을 때 자동으로 인증 여부를 재확인한다.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkEmailVerificationIfPending()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SignupScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
        onNicknameChange = viewModel::onNicknameChange,
        onCheckNicknameClick = viewModel::onCheckNicknameClick,
        onVerifyEmailClick = viewModel::onVerifyEmailClick,
        onCheckEmailVerificationClick = viewModel::onCheckEmailVerificationClick,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onTogglePasswordConfirmVisibility = viewModel::onTogglePasswordConfirmVisibility,
        onToggleTermsAgreed = viewModel::onToggleTermsAgreed,
        onTogglePrivacyAgreed = viewModel::onTogglePrivacyAgreed,
        onSignUpClick = viewModel::onSignUpClick,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun SignupScreenContent(
    uiState: SignupUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onCheckNicknameClick: () -> Unit,
    onVerifyEmailClick: () -> Unit,
    onCheckEmailVerificationClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onTogglePasswordConfirmVisibility: () -> Unit,
    onToggleTermsAgreed: () -> Unit,
    onTogglePrivacyAgreed: () -> Unit,
    onSignUpClick: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) KkokDayBackgroundDark else KkokDayCream
    val titleColor = if (isDark) KkokDayTextWhite else KkokDayTextBlack
    val subTextColor = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = titleColor,
                    )
                }
                Text(
                    text = "회원가입",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "콕데이에서 쓸 계정 정보를 입력해주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subTextColor,
                )

                Spacer(modifier = Modifier.height(28.dp))

                EmailVerificationField(
                    email = uiState.email,
                    onEmailChange = onEmailChange,
                    emailError = uiState.emailError,
                    isVerifyingEmail = uiState.isVerifyingEmail,
                    emailVerificationSent = uiState.emailVerificationSent,
                    isEmailVerified = uiState.isEmailVerified,
                    isCheckingEmailVerification = uiState.isCheckingEmailVerification,
                    emailVerificationInfoMessage = uiState.emailVerificationInfoMessage,
                    resendCooldownSeconds = uiState.resendCooldownSeconds,
                    canRequestEmailVerification = uiState.canRequestEmailVerification,
                    subTextColor = subTextColor,
                    onVerifyEmailClick = onVerifyEmailClick,
                    onCheckEmailVerificationClick = onCheckEmailVerificationClick,
                    onImeNext = { focusManager.moveFocus(FocusDirection.Down) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                PasswordOutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = "비밀번호",
                    isVisible = uiState.isPasswordVisible,
                    onToggleVisibility = onTogglePasswordVisibility,
                    errorText = uiState.passwordError,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                PasswordOutlinedTextField(
                    value = uiState.passwordConfirm,
                    onValueChange = onPasswordConfirmChange,
                    label = "비밀번호 확인",
                    isVisible = uiState.isPasswordConfirmVisible,
                    onToggleVisibility = onTogglePasswordConfirmVisibility,
                    errorText = uiState.passwordConfirmError,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                NicknameField(
                    nickname = uiState.nickname,
                    onNicknameChange = onNicknameChange,
                    nicknameError = uiState.nicknameError,
                    nicknameCheckState = uiState.nicknameCheckState,
                    onCheckNicknameClick = onCheckNicknameClick,
                    isDark = isDark,
                    onImeDone = { focusManager.clearFocus() },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                TermsCheckboxRow(
                    checked = uiState.isTermsAgreed,
                    onCheckedChange = { onToggleTermsAgreed() },
                    label = "이용약관에 동의합니다 (필수)",
                    onViewClick = { ContactActionLauncher.openUrl(context, TERMS_OF_SERVICE_URL) },
                )
                Spacer(modifier = Modifier.height(4.dp))
                TermsCheckboxRow(
                    checked = uiState.isPrivacyAgreed,
                    onCheckedChange = { onTogglePrivacyAgreed() },
                    label = "개인정보처리방침에 동의합니다 (필수)",
                    onViewClick = { ContactActionLauncher.openUrl(context, PRIVACY_POLICY_URL) },
                )

                if (uiState.generalError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.generalError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (uiState.showResetPasswordSuggestion) {
                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "비밀번호 재설정하기",
                            fontWeight = FontWeight.Bold,
                            color = KkokDayOrange,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSignUpClick,
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KkokDayOrange,
                        contentColor = Color.White,
                    ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = Color.White,
                        )
                    } else {
                        Text(text = "회원가입", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 1000)
@Composable
private fun SignupScreenLightPreview() {
    KkokDayTheme(darkTheme = false) {
        SignupScreenContent(
            uiState = SignupUiState(
                email = "test@kkokday.com",
                password = "password123",
                passwordConfirm = "password123",
                nickname = "여행러",
                nicknameCheckState = NicknameCheckState.AVAILABLE,
                emailVerificationSent = true,
                isEmailVerified = true,
                isTermsAgreed = true,
                isPrivacyAgreed = true,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onNicknameChange = {},
            onCheckNicknameClick = {},
            onVerifyEmailClick = {},
            onCheckEmailVerificationClick = {},
            onTogglePasswordVisibility = {},
            onTogglePasswordConfirmVisibility = {},
            onToggleTermsAgreed = {},
            onTogglePrivacyAgreed = {},
            onSignUpClick = {},
            onNavigateToForgotPassword = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Dark - waiting for verification", showBackground = true, heightDp = 1000)
@Composable
private fun SignupScreenDarkPreview() {
    KkokDayTheme(darkTheme = true) {
        SignupScreenContent(
            uiState = SignupUiState(
                email = "test@kkokday.com",
                password = "123",
                passwordConfirm = "1234",
                nickname = "a",
                passwordError = "비밀번호는 8자 이상이어야 해요",
                passwordConfirmError = "비밀번호가 일치하지 않아요",
                nicknameError = "닉네임은 2~10자로 입력해주세요",
                emailVerificationSent = true,
                resendCooldownSeconds = 125,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordConfirmChange = {},
            onNicknameChange = {},
            onCheckNicknameClick = {},
            onVerifyEmailClick = {},
            onCheckEmailVerificationClick = {},
            onTogglePasswordVisibility = {},
            onTogglePasswordConfirmVisibility = {},
            onToggleTermsAgreed = {},
            onTogglePrivacyAgreed = {},
            onSignUpClick = {},
            onNavigateToForgotPassword = {},
            onNavigateBack = {},
        )
    }
}
