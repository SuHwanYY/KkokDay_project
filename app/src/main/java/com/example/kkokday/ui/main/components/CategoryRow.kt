package com.example.kkokday.ui.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.main.model.HomeCategory
import com.example.kkokday.ui.theme.KkokDayMainTextDark

private val CategorySideMargin = 10.dp
private val CategoryItemGap = 6.dp
private val CategoryTitleHorizontalPadding = 20.dp
private val CategoryBoxCornerRadius = 16.dp
private const val CategoryIconWidthRatio = 0.6f

/**
 * "카테고리" 섹션 타이틀 + 카테고리 5개를 스크롤 없이 한 줄에 고정 배치한다.
 *
 * 박스 폭은 고정값이 아니라 실제 사용 가능한 폭을 기준으로 계산한다:
 * `(가용 폭 - 아이템 사이 간격 합) / 아이템 개수`. 좌우 여백 [CategorySideMargin],
 * 아이템 간격 [CategoryItemGap]을 뺀 나머지를 5등분해, "스크롤 없이 한 줄에 다
 * 들어가는 한도 내에서 최대한 크게" 박스를 보여준다 (화면 폭에 따라 자동으로
 * 맞춰지므로 기기별로 잘리거나 스크롤이 생기지 않는다).
 *
 * 각 카테고리는 [HomeCategory.backgroundColor] 파스텔톤 둥근 사각형 박스로
 * 감싸고, 그 안에 아이콘(텍스트 없는 원형 이미지) + 카테고리명 라벨을 세로로
 * 배치한다. 아이콘 drawable 자체는 여백 없이 캔버스를 꽉 채운 상태라 별도의
 * scale 확대나 clip 없이 [ContentScale.Fit]으로 그대로 그린다.
 *
 * 클릭 시 [onCategoryClick]으로 카테고리를 그대로 전달하므로, 추후 카테고리별
 * 세부 동작(예: 음식점 하위 선택 UI)을 붙일 때 이 핸들러 구조를 그대로 활용할 수 있다.
 */
@Composable
fun CategoryRow(
    categories: List<HomeCategory>,
    modifier: Modifier = Modifier,
    onCategoryClick: (HomeCategory) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "카테고리",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            modifier = Modifier.padding(horizontal = CategoryTitleHorizontalPadding),
        )
        Spacer(modifier = Modifier.height(12.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CategorySideMargin),
        ) {
            val gapCount = (categories.size - 1).coerceAtLeast(0)
            val boxWidth: Dp = (maxWidth - CategoryItemGap * gapCount) / categories.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CategoryItemGap),
            ) {
                categories.forEach { category ->
                    CategoryBox(
                        category = category,
                        width = boxWidth,
                        onClick = { onCategoryClick(category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBox(
    category: HomeCategory,
    width: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(width),
        shape = RoundedCornerShape(CategoryBoxCornerRadius),
        color = category.backgroundColor,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val iconSize = width * CategoryIconWidthRatio
            if (category.iconRes != null) {
                Image(
                    painter = painterResource(id = category.iconRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(iconSize),
                )
            } else if (category.iconVector != null) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(category.iconAccentColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = category.iconVector,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize * 0.55f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
