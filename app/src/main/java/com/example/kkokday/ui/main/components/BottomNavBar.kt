package com.example.kkokday.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.main.model.BottomNavDestination
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainYellow

/**
 * 하단 네비게이션 바: 홈 / 즐겨찾기 / 코스 / 투표 / 마이.
 *
 * 화면에 딱 붙은 사각형 바가 아니라, 좌우/아래 여백을 두고 캡슐형 카드로 떠 있는
 * 형태다. 제스처 바 영역 회피는 [com.example.kkokday.navigation.KkokDayNavHost]의
 * insetSafeComposable이 화면 진입 시 한 번만 처리하므로, 여기서는 고정 마진만 둔다.
 *
 * "코스"·"투표"는 목표 목업과 일치하는 기성 Material 아이콘이 없어
 * [CourseIcon]/[VoteIcon] 커스텀 벡터 아이콘을 그린다.
 */
@Composable
fun BottomNavBar(
    selected: BottomNavDestination,
    modifier: Modifier = Modifier,
    onSelect: (BottomNavDestination) -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BottomNavDestination.entries.forEach { destination ->
                BottomNavItem(
                    destination = destination,
                    selected = destination == selected,
                    onClick = { onSelect(destination) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: BottomNavDestination,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val tint = if (selected) KkokDayMainYellow else KkokDayMainSubText
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val iconModifier = Modifier.size(24.dp)
        when (destination) {
            BottomNavDestination.HOME ->
                Icon(imageVector = Icons.Filled.Home, contentDescription = destination.label, tint = tint, modifier = iconModifier)
            BottomNavDestination.FAVORITE ->
                Icon(imageVector = Icons.Filled.FavoriteBorder, contentDescription = destination.label, tint = tint, modifier = iconModifier)
            BottomNavDestination.COURSE ->
                CourseIcon(modifier = iconModifier, tint = tint)
            BottomNavDestination.VOTE ->
                VoteIcon(modifier = iconModifier, tint = tint)
            BottomNavDestination.MY ->
                Icon(imageVector = Icons.Outlined.Person, contentDescription = destination.label, tint = tint, modifier = iconModifier)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
