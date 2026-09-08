package com.example.kkokday.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.theme.KkokDayMainRed
import com.example.kkokday.ui.theme.KkokDayMainTeal
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTextBlack

/** 즐겨찾기 추가/삭제 같은 짧은 결과 알림에 쓰는 톤. 기본 Material 스낵바 대신 브랜드 톤의 카드로 그린다. */
enum class KkokDaySnackbarTone {
    SUCCESS,
    ERROR,
    /** 오류는 아니지만 알려줄 게 있는 경우(예: "아직 준비 중인 기능이에요"). */
    INFO,
}

private class KkokDaySnackbarVisuals(
    override val message: String,
    val tone: KkokDaySnackbarTone,
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

/** [KkokDaySnackbarHost]와 짝을 이루는 표시 함수 — 새로 호출하면 떠 있던 스낵바를 밀어내고 바로 보여준다. */
suspend fun SnackbarHostState.showKkokDaySnackbar(
    message: String,
    tone: KkokDaySnackbarTone,
): SnackbarResult = showSnackbar(KkokDaySnackbarVisuals(message, tone))

/**
 * 브랜드 톤(짙은 카드 + 원형 아이콘 배지)으로 그리는 스낵바 호스트. [showKkokDaySnackbar]로 띄운
 * 메시지는 이 톤으로, 일반 [SnackbarHostState.showSnackbar]로 띄운 메시지는 ERROR 톤으로 그린다
 * (이 앱에서 커스텀 톤 없이 그냥 문자열만 보내는 경우는 전부 에러 메시지이기 때문).
 */
@Composable
fun KkokDaySnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        val tone = (data.visuals as? KkokDaySnackbarVisuals)?.tone ?: KkokDaySnackbarTone.ERROR
        KkokDaySnackbarCard(message = data.visuals.message, tone = tone)
    }
}

@Composable
private fun KkokDaySnackbarCard(message: String, tone: KkokDaySnackbarTone) {
    val (icon, badgeColor) = when (tone) {
        KkokDaySnackbarTone.SUCCESS -> Icons.Filled.Favorite to KkokDayMainYellow
        KkokDaySnackbarTone.ERROR -> Icons.Filled.ErrorOutline to KkokDayMainRed
        KkokDaySnackbarTone.INFO -> Icons.Filled.Info to KkokDayMainTeal
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = KkokDayTextBlack,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KkokDayTextBlack,
                    modifier = Modifier.size(15.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
        }
    }
}
