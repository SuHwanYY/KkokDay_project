package com.example.kkokday.ui.directions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kkokday.directions.MapNavigationApp
import com.example.kkokday.directions.MapNavigationLauncher
import com.example.kkokday.directions.NavigationDestination
import com.example.kkokday.ui.theme.KakaoYellow
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayTextBlack
import com.example.kkokday.ui.theme.NaverMapGreen
import com.example.kkokday.ui.theme.TmapBlue

/**
 * "길찾기" 버튼을 누르면 뜨는, 지도 앱 3개(카카오맵/네이버지도/티맵) 중 하나를 고르는
 * 바텀시트. 아이콘 선택 즉시 [MapNavigationLauncher]로 실행하고 시트를 닫는다 —
 * 마지막 선택 앱을 기억하는 기능은 없어 매번 세 개가 동일하게 노출된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectionsBottomSheet(
    destination: NavigationDestination,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "길찾기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = destination.placeName,
                style = MaterialTheme.typography.bodySmall,
                color = KkokDayMainSubText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MapNavigationApp.entries.forEach { app ->
                    MapAppOption(
                        app = app,
                        onClick = {
                            MapNavigationLauncher.launch(context, app, destination)
                            onDismiss()
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MapAppOption(app: MapNavigationApp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(app.brandColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Directions,
                contentDescription = null,
                tint = app.iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = KkokDayMainTextDark,
        )
    }
}

private val MapNavigationApp.brandColor: Color
    get() = when (this) {
        MapNavigationApp.KAKAO_MAP -> KakaoYellow
        MapNavigationApp.NAVER_MAP -> NaverMapGreen
        MapNavigationApp.TMAP -> TmapBlue
    }

private val MapNavigationApp.iconTint: Color
    get() = when (this) {
        MapNavigationApp.KAKAO_MAP -> KkokDayTextBlack
        MapNavigationApp.NAVER_MAP -> Color.White
        MapNavigationApp.TMAP -> Color.White
    }
