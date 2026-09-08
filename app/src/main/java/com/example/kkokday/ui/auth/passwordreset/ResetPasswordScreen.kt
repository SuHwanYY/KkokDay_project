package com.example.kkokday.ui.auth.passwordreset

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.ui.auth.components.PasswordOutlinedTextField
import com.example.kkokday.ui.theme.KkokDayBackgroundDark
import com.example.kkokday.ui.theme.KkokDayCream
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySubTextDark
import com.example.kkokday.ui.theme.KkokDaySubTextLight
import com.example.kkokday.ui.theme.KkokDayTextBlack
import com.example.kkokday.ui.theme.KkokDayTextWhite
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun ResetPasswordScreen(
    onResetSuccess: () -> Unit,
    onInvalidLink: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.resetSuccess) {
        if (uiState.resetSuccess) {
            onResetSuccess()
        }
    }

    ResetPasswordScreenContent(
        uiState = uiState,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onNewPasswordConfirmChange = viewModel::onNewPasswordConfirmChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onTogglePasswordConfirmVisibility = viewModel::onTogglePasswordConfirmVisibility,
        onSubmitClick = viewModel::onSubmitClick,
        onInvalidLinkAcknowledged = onInvalidLink,
    )
}

@Composable
private fun ResetPasswordScreenContent(
    uiState: ResetPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onNewPasswordConfirmChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onTogglePasswordConfirmVisibility: () -> Unit,
    onSubmitClick: () -> Unit,
    onInvalidLinkAcknowledged: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) KkokDayBackgroundDark else KkokDayCream
    val titleColor = if (isDark) KkokDayTextWhite else KkokDayTextBlack
    val subTextColor = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        when {
            uiState.isVerifying -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KkokDayOrange)
                }
            }

            !uiState.isCodeValid -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "링크를 열 수 없어요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.verifyError ?: "유효하지 않거나 만료된 링크예요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subTextColor,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onInvalidLinkAcknowledged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KkokDayOrange,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(text = "비밀번호 다시 찾기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 28.dp),
                ) {
                    Spacer(modifier = Modifier.height(56.dp))
                    Text(
                        text = "새 비밀번호 설정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.email != null) {
                            "${uiState.email}의 새 비밀번호를 입력해주세요"
                        } else {
                            "새로 사용할 비밀번호를 입력해주세요"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = subTextColor,
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    PasswordOutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = onNewPasswordChange,
                        label = "새 비밀번호",
                        isVisible = uiState.isPasswordVisible,
                        onToggleVisibility = onTogglePasswordVisibility,
                        errorText = uiState.newPasswordError,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PasswordOutlinedTextField(
                        value = uiState.newPasswordConfirm,
                        onValueChange = onNewPasswordConfirmChange,
                        label = "새 비밀번호 확인",
                        isVisible = uiState.isPasswordConfirmVisible,
                        onToggleVisibility = onTogglePasswordConfirmVisibility,
                        errorText = uiState.newPasswordConfirmError,
                        imeAction = ImeAction.Done,
                        onImeAction = onSubmitClick,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (uiState.submitError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.submitError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onSubmitClick,
                        enabled = uiState.isFormValid && !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KkokDayOrange,
                            contentColor = Color.White,
                        ),
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.White,
                            )
                        } else {
                            Text(text = "비밀번호 재설정", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Preview(name = "Form", showBackground = true)
@Composable
private fun ResetPasswordScreenFormPreview() {
    KkokDayTheme(darkTheme = false) {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(isVerifying = false, isCodeValid = true, email = "test@kkokday.com"),
            onNewPasswordChange = {},
            onNewPasswordConfirmChange = {},
            onTogglePasswordVisibility = {},
            onTogglePasswordConfirmVisibility = {},
            onSubmitClick = {},
            onInvalidLinkAcknowledged = {},
        )
    }
}

@Preview(name = "Invalid", showBackground = true)
@Composable
private fun ResetPasswordScreenInvalidPreview() {
    KkokDayTheme(darkTheme = false) {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(isVerifying = false, isCodeValid = false, verifyError = "링크가 만료되었거나 이미 사용됐어요. 다시 시도해주세요."),
            onNewPasswordChange = {},
            onNewPasswordConfirmChange = {},
            onTogglePasswordVisibility = {},
            onTogglePasswordConfirmVisibility = {},
            onSubmitClick = {},
            onInvalidLinkAcknowledged = {},
        )
    }
}
