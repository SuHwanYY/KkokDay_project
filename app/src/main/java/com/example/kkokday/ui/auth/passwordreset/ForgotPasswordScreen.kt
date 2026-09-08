package com.example.kkokday.ui.auth.passwordreset

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
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.ui.auth.components.AuthOutlinedTextField
import com.example.kkokday.ui.theme.KkokDayBackgroundDark
import com.example.kkokday.ui.theme.KkokDayCream
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySubTextDark
import com.example.kkokday.ui.theme.KkokDaySubTextLight
import com.example.kkokday.ui.theme.KkokDayTextBlack
import com.example.kkokday.ui.theme.KkokDayTextWhite
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSendResetEmailClick = viewModel::onSendResetEmailClick,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun ForgotPasswordScreenContent(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendResetEmailClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) KkokDayBackgroundDark else KkokDayCream
    val titleColor = if (isDark) KkokDayTextWhite else KkokDayTextBlack
    val subTextColor = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight
    val focusManager = LocalFocusManager.current

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
                    text = "비밀번호 찾기",
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

                if (uiState.isEmailSent) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Icon(
                        imageVector = Icons.Filled.MarkEmailRead,
                        contentDescription = null,
                        tint = KkokDayOrange,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "재설정 메일을 보냈어요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${uiState.email} 메일함에서 링크를 눌러 비밀번호를 재설정해주세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subTextColor,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KkokDayOrange,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(text = "로그인으로 돌아가기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "가입하신 이메일로 비밀번호 재설정 링크를 보내드려요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subTextColor,
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    AuthOutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = "이메일",
                        errorText = uiState.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onSendResetEmailClick()
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onSendResetEmailClick,
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
                            Text(text = "재설정 메일 보내기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun ForgotPasswordScreenFormPreview() {
    KkokDayTheme(darkTheme = false) {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(email = "test@kkokday.com"),
            onEmailChange = {},
            onSendResetEmailClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Sent", showBackground = true)
@Composable
private fun ForgotPasswordScreenSentPreview() {
    KkokDayTheme(darkTheme = false) {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(email = "test@kkokday.com", isEmailSent = true),
            onEmailChange = {},
            onSendResetEmailClick = {},
            onNavigateBack = {},
        )
    }
}
