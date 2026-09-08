package com.example.kkokday.ui.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.auth.validation.MAX_NICKNAME_LENGTH
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySubTextDark
import com.example.kkokday.ui.theme.KkokDaySubTextLight
import com.example.kkokday.ui.theme.KkokDaySuccessGreen
import com.example.kkokday.ui.theme.KkokDayTextBlack
import com.example.kkokday.ui.theme.KkokDayTextWhite

enum class NicknameCheckState {
    NOT_CHECKED,
    CHECKING,
    AVAILABLE,
    TAKEN,
    ERROR,
}

/**
 * 닉네임 입력 + 중복확인 버튼 + 상태/글자수 표시를 묶은 공용 컴포저블.
 * 이메일 회원가입, 카카오 최초 로그인 닉네임 설정 화면이 함께 쓴다.
 */
@Composable
fun NicknameField(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    nicknameError: String?,
    nicknameCheckState: NicknameCheckState,
    onCheckNicknameClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = KkokDayOrange,
    onImeDone: () -> Unit = {},
) {
    val textColor = if (isDark) KkokDayTextWhite else KkokDayTextBlack
    val labelColor = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text("닉네임") },
                isError = nicknameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onImeDone() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = labelColor,
                    cursorColor = accentColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onCheckNicknameClick,
                enabled = nickname.isNotBlank() &&
                    nicknameError == null &&
                    nicknameCheckState != NicknameCheckState.CHECKING,
                modifier = Modifier
                    .height(56.dp)
                    .wrapContentWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
            ) {
                if (nicknameCheckState == NicknameCheckState.CHECKING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = accentColor,
                    )
                } else {
                    Text(text = "중복확인", fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val (statusText, statusColor) = nicknameStatusMessage(nicknameError, nicknameCheckState, isDark)
            Text(
                text = statusText.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(
                text = "${nickname.length}/$MAX_NICKNAME_LENGTH",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun nicknameStatusMessage(
    nicknameError: String?,
    nicknameCheckState: NicknameCheckState,
    isDark: Boolean,
): Pair<String?, Color> {
    nicknameError?.let { return it to MaterialTheme.colorScheme.error }
    val subTextColor = if (isDark) KkokDaySubTextDark else KkokDaySubTextLight
    return when (nicknameCheckState) {
        NicknameCheckState.NOT_CHECKED -> null to subTextColor
        NicknameCheckState.CHECKING -> "확인 중이에요..." to subTextColor
        NicknameCheckState.AVAILABLE -> "사용 가능한 닉네임입니다" to KkokDaySuccessGreen
        NicknameCheckState.TAKEN -> "이미 사용 중인 닉네임입니다" to MaterialTheme.colorScheme.error
        NicknameCheckState.ERROR -> "확인 중 오류가 발생했어요" to MaterialTheme.colorScheme.error
    }
}
