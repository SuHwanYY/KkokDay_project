package com.example.kkokday.ui.vote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.data.course.Course
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.common.KkokDaySnackbarTone
import com.example.kkokday.ui.common.showKkokDaySnackbar
import com.example.kkokday.ui.course.CourseCardGradient
import com.example.kkokday.ui.main.components.BottomNavBar
import com.example.kkokday.ui.main.components.CourseIcon
import com.example.kkokday.ui.main.components.VoteIcon
import com.example.kkokday.ui.main.model.BottomNavDestination
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme
import kotlinx.coroutines.launch

/**
 * 바텀 네비게이션 "투표" 탭 진입 경로. 내가 만든 코스 중 하나를 골라 투표 현황
 * 화면([VoteDetailRoute])으로 들어간다 — 후보를 따로 고르는 단계 없이 코스에 담긴
 * 장소 전체가 자동으로 후보가 되므로, 이 화면은 "코스 고르기"만 담당한다.
 */
@Composable
fun VoteListRoute(
    onNavigateTab: (BottomNavDestination) -> Boolean,
    onCourseClick: (String) -> Unit,
    viewModel: VoteListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showKkokDaySnackbar(message, KkokDaySnackbarTone.ERROR)
        viewModel.consumeErrorMessage()
    }

    LaunchedEffect(Unit) {
        // 코스 상세에서 장소를 추가/삭제하고 돌아왔을 수 있으니 다시 진입할 때마다 새로 받아온다.
        viewModel.refresh()
    }

    VoteListScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCourseClick = onCourseClick,
        onNavigateTab = onNavigateTab,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoteListScreenContent(
    uiState: VoteListUiState,
    snackbarHostState: SnackbarHostState,
    onCourseClick: (String) -> Unit,
    onNavigateTab: (BottomNavDestination) -> Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    var showHelpSheet by remember { mutableStateOf(false) }

    if (showHelpSheet) {
        VoteHelpBottomSheet(onDismiss = { showHelpSheet = false })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KkokDayMainBackground,
        snackbarHost = { KkokDaySnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selected = BottomNavDestination.VOTE,
                onSelect = { destination ->
                    if (!onNavigateTab(destination)) {
                        coroutineScope.launch {
                            snackbarHostState.showKkokDaySnackbar("아직 준비 중인 기능이에요", KkokDaySnackbarTone.INFO)
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "투표",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showHelpSheet = true }) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "투표 기능 사용법", tint = KkokDayMainSubText)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.courses.isEmpty() -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KkokDayMainYellow,
                    )
                    uiState.courses.isEmpty() -> EmptyVoteCourses(modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(contentPadding = ResultsPadding) {
                        items(uiState.courses, key = { it.id }) { course ->
                            VoteCourseCard(course = course, onClick = { onCourseClick(course.id) })
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

private val ResultsPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

/** [com.example.kkokday.ui.course.CourseListScreen]의 코스 카드와 같은 옐로우 그라데이션 배경으로 톤을 맞췄다. */
@Composable
private fun VoteCourseCard(course: Course, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CourseCardGradient)
            .border(width = 1.dp, color = KkokDayMainCardBorder, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.HowToVote,
                contentDescription = null,
                tint = KkokDayMainYellow,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "장소 ${course.places.size}개",
                    style = MaterialTheme.typography.labelSmall,
                    color = KkokDayMainSubText,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = KkokDayMainSubText,
            )
        }
    }
}

@Composable
private fun EmptyVoteCourses(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.HowToVote,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "투표할 코스가 없어요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "코스 탭에서 코스를 먼저 만들어보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 투표 기능이 처음이라 낯선 사람을 위한 사용법 안내. [com.example.kkokday.ui.category.PlaceContactBottomSheet] 등
 * 다른 안내성 바텀시트와 같은 흰 배경 톤을 쓰고, 단계별 아이콘 배지는
 * [com.example.kkokday.ui.main.components.OngoingCourseSection]의 `IconBadge`(색상 원 +
 * 흰 아이콘)와 같은 시각 언어를 재사용한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoteHelpBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
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
                text = "투표 기능은 이렇게 사용해요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
            )
            Spacer(modifier = Modifier.height(20.dp))

            VoteHelpStepRow(
                icon = { tint -> CourseIcon(modifier = Modifier.size(20.dp), tint = tint) },
                text = "코스 탭에서 가고 싶은 장소들을 담아 코스를 만들어요",
            )
            Spacer(modifier = Modifier.height(16.dp))
            VoteHelpStepRow(
                icon = { tint -> Icon(Icons.Filled.Share, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) },
                text = "투표 탭에서 코스를 선택하고 공유 버튼으로 링크를 보내요",
            )
            Spacer(modifier = Modifier.height(16.dp))
            VoteHelpStepRow(
                icon = { tint -> Icon(Icons.Filled.Link, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) },
                text = "링크를 받은 친구는 앱 설치 없이 웹에서 바로 투표할 수 있어요",
                subText = "이름만 입력하면 돼요, 여러 곳에 투표해도 괜찮아요",
            )
            Spacer(modifier = Modifier.height(16.dp))
            VoteHelpStepRow(
                icon = { tint -> VoteIcon(modifier = Modifier.size(20.dp), tint = tint) },
                text = "투표 탭에서 실시간으로 득표 현황과 누가 투표했는지 확인할 수 있어요",
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
            ) {
                Text("확인", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VoteHelpStepRow(
    icon: @Composable (tint: Color) -> Unit,
    text: String,
    subText: String? = null,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(KkokDayMainYellow),
            contentAlignment = Alignment.Center,
        ) {
            icon(Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = KkokDayMainTextDark,
            )
            if (subText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun VoteListScreenPreview() {
    KkokDayTheme {
        VoteListScreenContent(
            uiState = VoteListUiState(
                isLoading = false,
                courses = listOf(
                    Course(
                        id = "1",
                        ownerId = "u1",
                        title = "홍대 데이트 코스",
                        description = "저녁 먹고 카페 가기",
                        places = emptyList(),
                        createdAtMillis = System.currentTimeMillis(),
                        updatedAtMillis = System.currentTimeMillis(),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onCourseClick = {},
            onNavigateTab = { true },
        )
    }
}
