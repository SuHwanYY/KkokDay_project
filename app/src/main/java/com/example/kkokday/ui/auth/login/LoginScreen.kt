package com.example.kkokday.ui.auth.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.R
import com.example.kkokday.ui.auth.components.AuthOutlinedTextField
import com.example.kkokday.ui.auth.components.KakaoLoginButton
import com.example.kkokday.ui.auth.components.PasswordOutlinedTextField
import com.example.kkokday.ui.theme.KkokDayBackgroundDark
import com.example.kkokday.ui.theme.KkokDayCream
import com.example.kkokday.ui.theme.KkokDayDividerDark
import com.example.kkokday.ui.theme.KkokDayDividerLight
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySubTextDark
import com.example.kkokday.ui.theme.KkokDaySubTextLight
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNeedsEmailVerification: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNeedsNicknameSetup: (kakaoId: Long?, nicknameSuggestion: String?, profileImageUrl: String?) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            viewModel.consumeLoginSuccess()
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.needsEmailVerification) {
        if (uiState.needsEmailVerification) {
            viewModel.consumeNeedsEmailVerification()
            onNeedsEmailVerification()
        }
    }

    LaunchedEffect(uiState.needsNicknameSetup) {
        if (uiState.needsNicknameSetup) {
            val kakaoId = uiState.kakaoIdForNicknameSetup
            val nicknameSuggestion = uiState.nicknameSuggestionForSetup
            val profileImageUrl = uiState.profileImageUrlForSetup
            viewModel.consumeNeedsNicknameSetup()
            onNeedsNicknameSetup(kakaoId, nicknameSuggestion, profileImageUrl)
        }
    }

    LaunchedEffect(uiState.profileSyncWarning) {
        val warning = uiState.profileSyncWarning
        if (warning != null) {
            Toast.makeText(context, warning, Toast.LENGTH_LONG).show()
            viewModel.consumeProfileSyncWarning()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLoginClick = viewModel::onLoginClick,
        onKakaoLoginClick = { viewModel.onKakaoLoginClick(context) },
        onNavigateToSignup = onNavigateToSignup,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
    )
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) KkokDayBackgroundDark else KkokDayCream
    val subTextColor = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight
    val dividerColor = if (isDark) KkokDayDividerDark else KkokDayDividerLight
    val focusManager = LocalFocusManager.current

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "콕데이 로고",
                modifier = Modifier.size(96.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "이메일로 로그인하고 콕데이를 시작해보세요",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = subTextColor,
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthOutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "이메일",
                errorText = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordOutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "비밀번호",
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
                imeAction = ImeAction.Done,
                onImeAction = {
                    focusManager.clearFocus()
                    onLoginClick()
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "비밀번호를 잊으셨나요?",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor,
                    )
                }
            }

            if (uiState.generalError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.generalError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
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
                    Text(text = "로그인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "아직 계정이 없으신가요?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subTextColor,
                )
                TextButton(onClick = onNavigateToSignup) {
                    Text(
                        text = "회원가입",
                        fontWeight = FontWeight.Bold,
                        color = KkokDayOrange,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
                Text(
                    text = "또는",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = subTextColor,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = dividerColor)
            }

            Spacer(modifier = Modifier.height(20.dp))

            KakaoLoginButton(enabled = !uiState.isLoading, onClick = onKakaoLoginClick)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun LoginScreenLightPreview() {
    KkokDayTheme(darkTheme = false) {
        LoginScreenContent(
            uiState = LoginUiState(email = "test@kkokday.com", password = "password123"),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onKakaoLoginClick = {},
            onNavigateToSignup = {},
            onNavigateToForgotPassword = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun LoginScreenDarkPreview() {
    KkokDayTheme(darkTheme = true) {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "wrong-email",
                password = "123",
                emailError = "올바른 이메일 형식이 아니에요",
                generalError = "이메일 또는 비밀번호가 올바르지 않아요.",
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onKakaoLoginClick = {},
            onNavigateToSignup = {},
            onNavigateToForgotPassword = {},
        )
    }
}
