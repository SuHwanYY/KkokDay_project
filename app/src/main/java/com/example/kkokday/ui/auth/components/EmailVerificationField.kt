package com.example.kkokday.ui.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayOrange
import com.example.kkokday.ui.theme.KkokDaySuccessGreen

/**
 * 이메일 입력 + [인증하기]/재전송 버튼, 그 아래 "메일함을 확인해주세요" 안내 + [인증 확인]
 * 버튼을 묶은 공용 컴포저블. [NicknameField]가 닉네임 입력 + 중복확인 버튼을 묶는 것과 같은
 * 패턴이다 — 회원가입 화면이 쓴다.
 */
@Composable
fun EmailVerificationField(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isVerifyingEmail: Boolean,
    emailVerificationSent: Boolean,
    isEmailVerified: Boolean,
    isCheckingEmailVerification: Boolean,
    emailVerificationInfoMessage: String?,
    resendCooldownSeconds: Int,
    canRequestEmailVerification: Boolean,
    subTextColor: Color,
    onVerifyEmailClick: () -> Unit,
    onCheckEmailVerificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    onImeNext: () -> Unit = {},
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            AuthOutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "이메일",
                errorText = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { onImeNext() }),
                trailingIcon = if (isEmailVerified) {
                    {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "이메일 인증 완료",
                            tint = KkokDaySuccessGreen,
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onVerifyEmailClick,
                enabled = canRequestEmailVerification,
                modifier = Modifier
                    .height(56.dp)
                    .wrapContentWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KkokDayOrange),
            ) {
                if (isVerifyingEmail) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayOrange)
                } else {
                    Text(
                        text = when {
                            // 재전송 쿨다운이 분 단위(현재 3분)라 초 단위 그대로 보여주면 잘
                            // 안 읽혀서 분:초로 포맷한다.
                            resendCooldownSeconds > 0 -> "재전송(${formatCooldown(resendCooldownSeconds)})"
                            emailVerificationSent -> "재전송"
                            else -> "인증하기"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (emailVerificationSent && !isEmailVerified) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "메일함에서 링크를 클릭한 뒤 인증 확인을 눌러주세요",
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "메일이 안 보이면 스팸(정크) 메일함도 확인해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = KkokDayMainSubText,
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onCheckEmailVerificationClick,
                enabled = !isCheckingEmailVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KkokDayOrange),
            ) {
                if (isCheckingEmailVerification) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayOrange)
                } else {
                    Text(text = "인증 확인", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isEmailVerified) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "이메일 인증이 완료됐어요",
                    style = MaterialTheme.typography.labelSmall,
                    color = KkokDaySuccessGreen,
                )
            }
        } else if (emailVerificationInfoMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = emailVerificationInfoMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun formatCooldown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
