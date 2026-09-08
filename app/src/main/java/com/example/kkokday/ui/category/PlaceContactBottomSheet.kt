package com.example.kkokday.ui.category

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RateReview
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kkokday.data.place.CategoryPlace
import com.example.kkokday.directions.ContactActionLauncher
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow

/**
 * "더보기" 버튼을 누르면 뜨는 연락 수단 바텀시트.
 * [com.example.kkokday.ui.directions.DirectionsBottomSheet]와 같은 톤(흰 배경 + 타이틀 +
 * 원형 아이콘 옵션 Row)으로 맞췄다. 전화 걸기는 phone이 있을 때만 노출한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceContactBottomSheet(
    place: CategoryPlace,
    onDismiss: () -> Unit,
    onReviewClick: () -> Unit,
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
                text = "연락 수단",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = place.placeName,
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
                if (place.phone.isNotBlank()) {
                    ContactOption(
                        icon = Icons.Filled.Call,
                        label = "전화 걸기",
                        onClick = {
                            ContactActionLauncher.dial(context, place.phone)
                            onDismiss()
                        },
                    )
                }
                ContactOption(
                    icon = Icons.Filled.Info,
                    label = "상세보기",
                    onClick = {
                        ContactActionLauncher.openUrl(context, place.placeUrl)
                        onDismiss()
                    },
                )
                ContactOption(
                    icon = Icons.Filled.RateReview,
                    label = "리뷰 보기·작성",
                    onClick = {
                        onReviewClick()
                        onDismiss()
                    },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ContactOption(icon: ImageVector, label: String, onClick: () -> Unit) {
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
                .background(KkokDayMainYellow),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KkokDayMainTextDark,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = KkokDayMainTextDark,
        )
    }
}
