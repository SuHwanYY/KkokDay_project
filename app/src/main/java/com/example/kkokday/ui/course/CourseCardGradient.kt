package com.example.kkokday.ui.course

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.kkokday.ui.theme.KkokDayMainYellow

/**
 * 코스 카드 배경 — 앱 기존 톤(옐로우)에서 크게 벗어나지 않는 대각선 그라데이션(왼쪽 위가
 * 진하고 오른쪽 아래로 갈수록 흰색에 가까워진다). [CourseListScreen]의 코스 카드와
 * [VoteListScreen]의 투표용 코스 카드가 톤을 맞추기 위해 이 배경을 공유한다.
 */
val CourseCardGradient = Brush.linearGradient(
    colors = listOf(KkokDayMainYellow.copy(alpha = 0.32f), KkokDayMainYellow.copy(alpha = 0.06f), Color.White),
)
