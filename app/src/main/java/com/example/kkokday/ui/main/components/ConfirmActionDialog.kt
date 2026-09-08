package com.example.kkokday.ui.main.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.kkokday.ui.theme.KkokDayAmberAccent
import com.example.kkokday.ui.theme.KkokDayLocationCardBackground
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark

/**
 * "최근 콕 찍은 곳" 삭제/재선택 확인 다이얼로그가 공유하는 스타일. [title]은 한 줄에
 * 들어오도록 titleMedium으로 고정하고, [highlightText](장소 이름)만 진한 앰버로 강조한다.
 */
@Composable
fun ConfirmActionDialog(
    title: String,
    highlightText: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "취소",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KkokDayLocationCardBackground,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                maxLines = 1,
            )
        },
        text = {
            Text(
                text = highlightText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = KkokDayAmberAccent,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = KkokDayAmberAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel, color = KkokDayMainSubText)
            }
        },
    )
}
