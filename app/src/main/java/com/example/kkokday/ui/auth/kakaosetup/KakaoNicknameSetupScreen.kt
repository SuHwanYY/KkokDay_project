package com.example.kkokday.ui.auth.kakaosetup

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.ui.auth.components.NicknameCheckState
import com.example.kkokday.ui.auth.components.NicknameField
import com.example.kkokday.ui.theme.KkokDayBackgroundDark
import com.example.kkokday.ui.theme.KkokDayCream
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySubTextDark
import com.example.kkokday.ui.theme.KkokDaySubTextLight
import com.example.kkokday.ui.theme.KkokDayTextBlack
import com.example.kkokday.ui.theme.KkokDayTextWhite
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun KakaoNicknameSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: KakaoNicknameSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.setupComplete) {
        if (uiState.setupComplete) {
            viewModel.consumeSetupComplete()
            onSetupComplete()
        }
    }

    KakaoNicknameSetupScreenContent(
        uiState = uiState,
        onNicknameChange = viewModel::onNicknameChange,
        onCheckNicknameClick = viewModel::onCheckNicknameClick,
        onSubmitClick = viewModel::onSubmitClick,
    )
}

@Composable
private fun KakaoNicknameSetupScreenContent(
    uiState: KakaoNicknameSetupUiState,
    onNicknameChange: (String) -> Unit,
    onCheckNicknameClick: () -> Unit,
    onSubmitClick: () -> Unit,
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
                .imePadding()
                .padding(horizontal = 28.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "닉네임을 확인해주세요",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (uiState.isKakaoFlow) {
                    "카카오 프로필의 닉네임을 그대로 쓰거나,\n원하는 닉네임으로 바꿔서 콕데이를 시작해보세요"
                } else {
                    "가입하신 닉네임을 아직 확정하지 못했어요.\n닉네임을 다시 정해 콕데이를 시작해보세요"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = subTextColor,
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                onClick = onSubmitClick,
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
                    Text(text = "콕데이 시작하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun KakaoNicknameSetupScreenPreview() {
    KkokDayTheme(darkTheme = false) {
        KakaoNicknameSetupScreenContent(
            uiState = KakaoNicknameSetupUiState(
                nickname = "여행러",
                nicknameCheckState = NicknameCheckState.AVAILABLE,
            ),
            onNicknameChange = {},
            onCheckNicknameClick = {},
            onSubmitClick = {},
        )
    }
}

@Preview(name = "Dark - Taken", showBackground = true)
@Composable
private fun KakaoNicknameSetupScreenTakenPreview() {
    KkokDayTheme(darkTheme = true) {
        KakaoNicknameSetupScreenContent(
            uiState = KakaoNicknameSetupUiState(
                nickname = "여행러",
                nicknameCheckState = NicknameCheckState.TAKEN,
                generalError = "방금 다른 사용자가 먼저 사용한 닉네임이에요. 다른 닉네임으로 다시 시도해주세요.",
            ),
            onNicknameChange = {},
            onCheckNicknameClick = {},
            onSubmitClick = {},
        )
    }
}
