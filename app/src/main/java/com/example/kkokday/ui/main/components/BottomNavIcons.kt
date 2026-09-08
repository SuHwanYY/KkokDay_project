package com.example.kkokday.ui.main.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

private const val StrokeWidthRatio = 0.08f

/**
 * "코스" 탭 아이콘. 목표 목업에 맞는 기성 Material 아이콘이 없어 직접 그린 커스텀
 * 벡터(Canvas) 아이콘이다 — 경로 위 정류장 4개(원)를 선으로 이은 모양으로,
 * 다른 하단 탭 아이콘과 동일한 얇은 선(outline) 스타일과 둥근 끝(round cap)을 쓴다.
 */
@Composable
fun CourseIcon(modifier: Modifier = Modifier, tint: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * StrokeWidthRatio, cap = StrokeCap.Round)
        val dotRadius = size.minDimension * 0.07f

        val topLeft = Offset(size.width * 0.30f, size.height * 0.26f)
        val bottomLeft = Offset(size.width * 0.30f, size.height * 0.72f)
        val topRight = Offset(size.width * 0.70f, size.height * 0.40f)
        val bottomRight = Offset(size.width * 0.70f, size.height * 0.84f)

        drawLine(color = tint, start = topLeft, end = bottomLeft, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color = tint, start = topRight, end = bottomRight, strokeWidth = stroke.width, cap = StrokeCap.Round)

        listOf(topLeft, bottomLeft, topRight, bottomRight).forEach { center ->
            drawCircle(color = tint, radius = dotRadius, center = center, style = stroke)
        }
    }
}

/**
 * "투표" 탭 아이콘. 마찬가지로 기성 아이콘이 없어 직접 그린 커스텀 벡터 아이콘 —
 * 마감 시계를 형상화해 원(시계) + 작은 돌기(알람 손잡이) + 시침으로 구성한다.
 */
@Composable
fun VoteIcon(modifier: Modifier = Modifier, tint: Color = Color.Black) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * StrokeWidthRatio
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        val center = Offset(size.width * 0.52f, size.height * 0.58f)
        val radius = size.minDimension * 0.36f
        drawCircle(color = tint, radius = radius, center = center, style = stroke)

        // 알람 손잡이(돌기)
        val notchRadius = size.minDimension * 0.11f
        val notchCenter = Offset(size.width * 0.27f, size.height * 0.22f)
        drawCircle(color = tint, radius = notchRadius, center = notchCenter, style = stroke)

        // 시침
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.45f, center.y - radius * 0.45f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}
