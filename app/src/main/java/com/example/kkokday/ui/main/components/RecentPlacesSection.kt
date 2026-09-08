package com.example.kkokday.ui.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kkokday.data.place.MAX_RECENT_PLACES
import com.example.kkokday.data.place.RecentPlace
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainRed
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTeal
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val EmptyStateHeight = 120.dp

/**
 * "최근 콕 찍은 곳" 섹션. 저장된 장소가 있으면 가로 스크롤 카드 리스트로, 없으면
 * 카드와 비슷한 높이의 빈 상태 안내로 대체된다.
 */
@Composable
fun RecentPlacesSection(
    places: List<RecentPlace>,
    modifier: Modifier = Modifier,
    onPlaceClick: (RecentPlace) -> Unit = {},
    onDeleteClick: (RecentPlace) -> Unit = {},
    onDeleteAllClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "최근 콕 찍은 곳",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "(${places.size} / $MAX_RECENT_PLACES)",
                style = MaterialTheme.typography.labelMedium,
                color = KkokDayMainSubText,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (places.isNotEmpty()) {
                Text(
                    text = "전체 삭제",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainRed,
                    modifier = Modifier.clickable(onClick = onDeleteAllClick),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (places.isEmpty()) {
            EmptyRecentPlaces(modifier = Modifier.padding(horizontal = 20.dp))
        } else {
            val listState = rememberLazyListState()
            // 장소가 추가/재선택돼 맨 앞 항목이 바뀔 때마다(=목록이 갱신될 때마다) 새로
            // 갱신된 항목이 보이도록 스크롤을 맨 앞으로 되돌린다. 같은 항목이 계속
            // 맨 앞이면 키가 안 바뀌므로 불필요한 스크롤은 일어나지 않는다.
            LaunchedEffect(places.firstOrNull()?.docId) {
                listState.animateScrollToItem(0)
            }
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(places, key = { it.docId }) { place ->
                    RecentPlaceCard(
                        place = place,
                        onClick = { onPlaceClick(place) },
                        onDeleteClick = { onDeleteClick(place) },
                    )
                }
            }
        }
    }
}

/**
 * 최근 콕 찍은 곳이 하나도 없을 때 카드 자리를 대체하는 안내.
 *
 * 지정된 아이콘 리소스가 없어 위치 핀(주황/노랑) + X 뱃지 조합을 기존 아이콘
 * 팔레트로 구성했다 — 전용 아이콘 파일이 생기면 이 Icon/Box 조합을 Image로
 * 교체하면 된다.
 */
@Composable
private fun EmptyRecentPlaces(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(EmptyStateHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = KkokDayMainYellow,
                modifier = Modifier.size(56.dp),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp),
                shape = CircleShape,
                color = KkokDayMainRed,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "아직 콕 찍은 곳이 없어요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
        )
    }
}

@Composable
private fun RecentPlaceCard(
    place: RecentPlace,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    Box(modifier = modifier.width(180.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, KkokDayMainCardBorder),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = KkokDayMainRed,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = place.placeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 16.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatRecentPlaceTime(place.selectedAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = KkokDayMainTeal,
                )
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(26.dp),
            shape = CircleShape,
            color = Color.White,
        ) {
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(26.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "최근 콕 찍은 곳에서 삭제",
                    tint = KkokDayMainSubText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun formatRecentPlaceTime(selectedAtMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = selectedAtMillis }
    val timeText = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(selectedAtMillis))
    val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    return when {
        isSameYear && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> "오늘 $timeText"
        isSameYear && now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1 -> "어제 $timeText"
        else -> {
            val diffDays = (now.timeInMillis - selectedAtMillis) / (1000 * 60 * 60 * 24)
            "${diffDays}일 전"
        }
    }
}
