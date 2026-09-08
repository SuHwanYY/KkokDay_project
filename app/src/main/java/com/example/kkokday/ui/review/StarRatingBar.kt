package com.example.kkokday.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.theme.KkokDayMainYellow

private const val STAR_COUNT = 5

/**
 * 별 5개 행. [onRatingChange]가 null이면 읽기 전용(리뷰 목록/헤더 요약), 있으면 별을 탭해
 * 1~5점을 고르는 입력 컴포넌트(작성/수정 폼)로 동작한다.
 */
@Composable
fun StarRatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp,
    onRatingChange: ((Int) -> Unit)? = null,
) {
    Row(modifier = modifier) {
        for (star in 1..STAR_COUNT) {
            val filled = star <= rating
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (onRatingChange != null) "${star}점" else null,
                tint = KkokDayMainYellow,
                modifier = Modifier
                    .size(starSize)
                    .let { base ->
                        if (onRatingChange != null) base.clickable { onRatingChange(star) } else base
                    },
            )
        }
    }
}
