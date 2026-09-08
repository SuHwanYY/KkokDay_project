package com.example.kkokday.ui.auth.verification

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.ui.theme.KkokDayBackgroundDark
import com.example.kkokday.ui.theme.KkokDayCream
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySubTextDark
import com.example.kkokday.ui.theme.KkokDaySubTextLight
import com.example.kkokday.ui.theme.KkokDaySuccessGreen
import com.example.kkokday.ui.theme.KkokDayTextBlack
import com.example.kkokday.ui.theme.KkokDayTextWhite
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            onVerified()
        }
    }

    // 메일 앱에서 링크를 누르고 다시 콕데이로 돌아왔을 때 자동으로 인증 여부를 재확인한다.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkVerification()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    EmailVerificationScreenContent(
        uiState = uiState,
        onRefreshClick = viewModel::checkVerification,
        onResendClick = viewModel::onResendClick,
        onSignOutClick = {
            viewModel.onSignOutClick()
            onSignedOut()
        },
    )
}

@Composable
private fun EmailVerificationScreenContent(
    uiState: EmailVerificationUiState,
    onRefreshClick: () -> Unit,
    onResendClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) KkokDayBackgroundDark else KkokDayCream
    val titleColor = if (isDark) KkokDayTextWhite else KkokDayTextBlack
    val subTextColor = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MarkEmailUnread,
                contentDescription = null,
                tint = KkokDayOrange,
                modifier = Modifier.size(64.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "이메일 인증이 필요해요",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildString {
                    if (uiState.email != null) append("${uiState.email}으로 ")
                    append("인증 메일을 보냈어요. 메일함에서 링크를 눌러 가입을 완료해주세요.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = subTextColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            if (uiState.infoMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.infoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDaySuccessGreen,
                )
            }
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRefreshClick,
                enabled = !uiState.isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KkokDayOrange,
                    contentColor = Color.White,
                ),
            ) {
                if (uiState.isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White,
                    )
                } else {
                    Text(text = "인증 확인하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onResendClick,
                enabled = !uiState.isResending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KkokDayOrange),
            ) {
                if (uiState.isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = KkokDayOrange,
                    )
                } else {
                    Text(text = "인증 메일 재전송", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onSignOutClick) {
                Text(
                    text = "로그아웃하고 다시 로그인",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor,
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun EmailVerificationScreenPreview() {
    KkokDayTheme(darkTheme = false) {
        EmailVerificationScreenContent(
            uiState = EmailVerificationUiState(email = "traveler@kkokday.com"),
            onRefreshClick = {},
            onResendClick = {},
            onSignOutClick = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun EmailVerificationScreenDarkPreview() {
    KkokDayTheme(darkTheme = true) {
        EmailVerificationScreenContent(
            uiState = EmailVerificationUiState(
                email = "traveler@kkokday.com",
                infoMessage = "인증 메일을 다시 보냈어요.",
            ),
            onRefreshClick = {},
            onResendClick = {},
            onSignOutClick = {},
        )
    }
}
