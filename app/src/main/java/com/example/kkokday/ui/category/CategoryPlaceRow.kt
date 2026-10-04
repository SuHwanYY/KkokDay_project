package com.example.kkokday.ui.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kkokday.data.place.CategoryPlace
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainRed
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTeal
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.util.formatDistance
import com.example.kkokday.util.formatWalkingTime

/**
 * 장소 한 줄(카드) — 카테고리별 주변 검색 결과 화면과 즐겨찾기 탭 화면
 * ([com.example.kkokday.ui.favorite.FavoritesScreen])이 공통으로 재사용한다.
 *
 * [showDistance]는 즐겨찾기 탭처럼 검색 기준 좌표가 없어 거리 정보가 의미 없는
 * 화면에서 false로 꺼서 쓴다.
 *
 * [selectionMode]가 true면("코스 만들기" 진입 중) 오른쪽 버튼 3개 대신 체크박스 하나만
 * 보여주고, 행 전체를 눌러도 토글되게 한다 — 행마다 아이콘을 늘리지 않기 위한 선택.
 */
@Composable
fun CategoryPlaceRow(
    place: CategoryPlace,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onDirectionsClick: () -> Unit,
    onMoreClick: () -> Unit,
    showDistance: Boolean = true,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (selectionMode) it.clickable(onClick = onSelectToggle) else it },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (selectionMode && isSelected) KkokDayMainYellow else KkokDayMainCardBorder),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = KkokDayMainRed,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.placeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            if (showDistance && !selectionMode) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDistance(place.distanceMeters),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = KkokDayMainTeal,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = KkokDayMainSubText,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatWalkingTime(place.distanceMeters),
                            style = MaterialTheme.typography.labelSmall,
                            color = KkokDayMainSubText,
                        )
                    }
                    val avgRating = place.avgRating
                    val reviewCount = place.reviewCount
                    if (avgRating != null && reviewCount != null && reviewCount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = KkokDayMainYellow,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatRatingBadge(avgRating, reviewCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = KkokDayMainSubText,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = KkokDayMainYellow),
                )
            } else {
                PlaceActionButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                    onClick = onFavoriteToggle,
                )
                Spacer(modifier = Modifier.width(6.dp))
                PlaceActionButton(
                    icon = Icons.Filled.Directions,
                    contentDescription = "길찾기",
                    onClick = onDirectionsClick,
                )
                Spacer(modifier = Modifier.width(6.dp))
                PlaceActionButton(
                    icon = Icons.Filled.MoreHoriz,
                    contentDescription = "더보기",
                    onClick = onMoreClick,
                )
            }
        }
    }
}

@Composable
fun PlaceActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(KkokDayMainYellow)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = KkokDayMainTextDark,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** "4.5 · 12" 형태 — 앞의 별 아이콘과 합쳐 "★4.5 · 12"로 읽힌다. */
fun formatRatingBadge(avgRating: Double, reviewCount: Int): String =
    "%.1f · %d".format(avgRating, reviewCount)
