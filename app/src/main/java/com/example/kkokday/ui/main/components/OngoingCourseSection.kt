package com.example.kkokday.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kkokday.ui.course.CourseCardGradient
import com.example.kkokday.ui.main.model.OngoingCourseCardData
import com.example.kkokday.ui.main.model.OngoingSectionTab
import com.example.kkokday.ui.main.model.OngoingVoteCardData
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTeal
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme

private val EmptyStateHeight = 120.dp

/**
 * "진행 중인 코스/투표" 섹션. "최근 콕 찍은 곳"과 하단 네비게이션 바 사이에 위치한다.
 *
 * 코스/투표 두 토글은 각각 "고정해둔 코스 1개"만 카드로 보여준다(목록이 아니다) — 아직
 * 고정한 적 없으면 ViewModel이 가장 최근 코스로 자동 채우고, "변경"으로 사용자가 직접
 * 고르면 그 뒤로는 계속 그 값이 유지된다.
 */
@Composable
fun OngoingCourseSection(
    selectedTab: OngoingSectionTab,
    onTabSelected: (OngoingSectionTab) -> Unit,
    isLoading: Boolean,
    courseCard: OngoingCourseCardData?,
    voteCard: OngoingVoteCardData?,
    modifier: Modifier = Modifier,
    onCourseCardClick: (String) -> Unit = {},
    onVoteCardClick: (String) -> Unit = {},
    onChangeClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "진행 중인 코스/투표",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                modifier = Modifier.weight(1f),
            )
            OngoingTabChip(
                label = "코스",
                selected = selectedTab == OngoingSectionTab.COURSE,
                onClick = { onTabSelected(OngoingSectionTab.COURSE) },
            )
            Spacer(modifier = Modifier.width(6.dp))
            OngoingTabChip(
                label = "투표",
                selected = selectedTab == OngoingSectionTab.VOTE,
                onClick = { onTabSelected(OngoingSectionTab.VOTE) },
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EmptyStateHeight),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = KkokDayMainYellow)
            }
            selectedTab == OngoingSectionTab.COURSE && courseCard != null -> PinnedCourseCard(
                data = courseCard,
                onClick = { onCourseCardClick(courseCard.courseId) },
                onChangeClick = onChangeClick,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            selectedTab == OngoingSectionTab.VOTE && voteCard != null -> PinnedVoteCard(
                data = voteCard,
                onClick = { onVoteCardClick(voteCard.courseId) },
                onChangeClick = onChangeClick,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            else -> EmptyOngoingSection(modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}

/** [com.example.kkokday.ui.category.CategoryPlacesScreen]의 거리순/인기순 칩과 같은 톤(선택 시 옐로우 배경). */
@Composable
private fun OngoingTabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = KkokDayMainYellow,
            selectedLabelColor = Color.White,
            labelColor = KkokDayMainTextDark,
        ),
    )
}

@Composable
private fun PinnedCourseCard(
    data: OngoingCourseCardData,
    onClick: () -> Unit,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PinnedCardSurface(modifier = modifier, onClick = onClick) {
        IconBadge(icon = { tint -> CourseIcon(modifier = Modifier.size(20.dp), tint = tint) }, backgroundColor = KkokDayMainYellow)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.courseTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "장소 ${data.placeCount}개",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = KkokDayMainTeal,
            )
        }
        PinnedCardTrailingActions(onChangeClick = onChangeClick)
    }
}

@Composable
private fun PinnedVoteCard(
    data: OngoingVoteCardData,
    onClick: () -> Unit,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PinnedCardSurface(modifier = modifier, onClick = onClick) {
        IconBadge(icon = { tint -> VoteIcon(modifier = Modifier.size(20.dp), tint = tint) }, backgroundColor = KkokDayMainYellow)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.courseTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (data.topCandidateName != null) {
                    "1위 ${data.topCandidateName} · ${data.topCandidateVoteCount}표"
                } else {
                    "아직 투표가 없어요"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = KkokDayMainTeal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "참여 ${data.participantCount}명",
                style = MaterialTheme.typography.labelSmall,
                color = KkokDayMainSubText,
            )
        }
        PinnedCardTrailingActions(onChangeClick = onChangeClick)
    }
}

@Composable
private fun PinnedCardTrailingActions(onChangeClick: () -> Unit) {
    TextButton(onClick = onChangeClick) {
        Text("변경", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = KkokDayMainSubText)
    }
    Icon(
        imageVector = Icons.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = KkokDayMainSubText,
    )
}

/**
 * 두 카드가 공유하는 바깥 껍데기 — [com.example.kkokday.ui.course.CourseListScreen]/
 * [com.example.kkokday.ui.vote.VoteListScreen]의 코스 카드와 같은 옐로우 그라데이션
 * 배경을 재사용해 톤을 맞추고, 왼쪽 [IconBadge]로 "최근 콕 찍은 곳" 카드([RecentPlaceCard],
 * 흰 배경)와 구분되는 표식을 준다.
 */
@Composable
private fun PinnedCardSurface(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CourseCardGradient)
            .border(width = 1.dp, color = KkokDayMainCardBorder, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** [com.example.kkokday.ui.main.components.CategoryRow]의 색상 원 + 흰 아이콘 배지와 동일한 시각 언어를 재사용한다. */
@Composable
private fun IconBadge(
    icon: @Composable (tint: Color) -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        icon(Color.White)
    }
}

/**
 * 만든 코스가 하나도 없을 때 카드 자리를 대체하는 안내. [RecentPlacesSection]의
 * [EmptyRecentPlaces]와 같은 톤(56dp 아이콘 + bodyMedium 문구, 120dp 높이)으로 맞췄다 —
 * 코스/투표 어느 토글이든 이 화면에서 볼 결과는 같다(코스가 없으면 투표할 것도 없다).
 */
@Composable
private fun EmptyOngoingSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(EmptyStateHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CourseIcon(modifier = Modifier.size(56.dp), tint = KkokDayMainYellow)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "아직 만든 코스가 없어요",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "코스 탭에서 코스를 먼저 만들어보세요",
            style = MaterialTheme.typography.bodySmall,
            color = KkokDayMainSubText,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OngoingCourseSectionCoursePreview() {
    KkokDayTheme {
        OngoingCourseSection(
            selectedTab = OngoingSectionTab.COURSE,
            onTabSelected = {},
            isLoading = false,
            courseCard = OngoingCourseCardData(courseId = "1", courseTitle = "홍대 데이트 코스", placeCount = 4),
            voteCard = null,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OngoingCourseSectionVotePreview() {
    KkokDayTheme {
        OngoingCourseSection(
            selectedTab = OngoingSectionTab.VOTE,
            onTabSelected = {},
            isLoading = false,
            courseCard = null,
            voteCard = OngoingVoteCardData(
                courseId = "1",
                courseTitle = "홍대 데이트 코스",
                topCandidateName = "삼대째손두부",
                topCandidateVoteCount = 3,
                participantCount = 5,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun OngoingCourseSectionEmptyPreview() {
    KkokDayTheme {
        OngoingCourseSection(
            selectedTab = OngoingSectionTab.COURSE,
            onTabSelected = {},
            isLoading = false,
            courseCard = null,
            voteCard = null,
        )
    }
}
