package com.example.kkokday.ui.course

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.ui.category.PlaceActionButton
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow

/**
 * [CoursePlaceRow]의 레이아웃 상수 — [CourseDetailScreen]이 드래그 제스처를 시작할 영역
 * (핸들 vs 버튼 vs 나머지 본문)을 좌표만으로 판단할 때 이 값들을 그대로 가져다 쓴다.
 * 실제 레이아웃(패딩/스페이서/버튼 크기)이 바뀌면 여기도 같이 맞춰야 한다.
 */
val COURSE_PLACE_ROW_LEADING_PADDING = 2.dp
val DRAG_HANDLE_TOUCH_SIZE = 44.dp
val COURSE_PLACE_ROW_TRAILING_PADDING = 8.dp
val COURSE_PLACE_ROW_ACTION_BUTTON_SIZE = 34.dp
val COURSE_PLACE_ROW_ACTION_BUTTON_SPACING = 6.dp

/**
 * 코스 상세 화면의 장소 한 줄. [CategoryPlaceRow][com.example.kkokday.ui.category.CategoryPlaceRow]와
 * 같은 카드/[PlaceActionButton] 톤을 재사용하되, 즐겨찾기 토글 대신 순서 번호 배지 +
 * 드래그 핸들을, 더보기 대신 삭제 버튼을 둔다 — CoursePlace엔 phone/place_url/distance가
 * 없어 그 버튼들을 그대로 재사용할 데이터가 없기 때문이다.
 *
 * 이 컴포저블 자체는 순수 표시용이다 — 드래그 제스처는 [CourseDetailScreen.PlacesSection]이
 * 좌표 기반으로 영역(핸들/버튼/본문)을 나눠서 처리한다(제스처 노드를 이 안에 중첩시키면
 * 드래그로 움직이는 부모의 graphicsLayer 변환이 자기 자신의 포인터 좌표에도 반영돼
 * "인식은 되는데 실제로 안 움직이는" 문제가 생긴다). [isDragging]일 때는 배경/테두리로
 * "지금 잡혔다"는 것만 보여준다.
 */
@Composable
fun CoursePlaceRow(
    place: CoursePlace,
    order: Int,
    onDirectionsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDragging: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isDragging) KkokDayMainYellow else KkokDayMainCardBorder),
        shadowElevation = if (isDragging) 6.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                start = COURSE_PLACE_ROW_LEADING_PADDING,
                end = COURSE_PLACE_ROW_TRAILING_PADDING,
                top = 8.dp,
                bottom = 8.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(DRAG_HANDLE_TOUCH_SIZE)
                    .clip(CircleShape)
                    .background(if (isDragging) KkokDayMainYellow.copy(alpha = 0.35f) else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "드래그해서 순서 변경 (꾹 눌러서 이동)",
                    tint = if (isDragging) KkokDayMainTextDark else KkokDayMainSubText,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(KkokDayMainYellow),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$order",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                )
            }
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
            PlaceActionButton(
                icon = Icons.Filled.Directions,
                contentDescription = "길찾기",
                onClick = onDirectionsClick,
            )
            Spacer(modifier = Modifier.width(6.dp))
            PlaceActionButton(
                icon = Icons.Filled.Delete,
                contentDescription = "코스에서 삭제",
                onClick = onDeleteClick,
            )
        }
    }
}
