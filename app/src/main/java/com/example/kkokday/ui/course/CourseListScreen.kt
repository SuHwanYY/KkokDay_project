package com.example.kkokday.ui.course

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.kkokday.ui.main.components.BottomNavBar
import com.example.kkokday.ui.main.components.ConfirmActionDialog
import com.example.kkokday.ui.main.model.BottomNavDestination
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainRed
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme
import kotlinx.coroutines.launch

/** 바텀 네비게이션 "코스" 탭 진입 경로. 내가 만든 코스 목록을 최근 수정순으로 보여준다. */
@Composable
fun CourseListRoute(
    onNavigateTab: (BottomNavDestination) -> Boolean,
    onCourseClick: (String) -> Unit,
    viewModel: CourseListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showKkokDaySnackbar(message, KkokDaySnackbarTone.ERROR)
        viewModel.consumeErrorMessage()
    }

    LaunchedEffect(uiState.toastMessage) {
        val message = uiState.toastMessage ?: return@LaunchedEffect
        snackbarHostState.showKkokDaySnackbar(message, KkokDaySnackbarTone.SUCCESS)
        viewModel.consumeToastMessage()
    }

    LaunchedEffect(Unit) {
        // 코스 상세에서 이름 변경/삭제를 하고 돌아왔을 수 있으니 다시 진입할 때마다 새로 받아온다.
        viewModel.refresh()
    }

    CourseListScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCourseClick = onCourseClick,
        onAddCourseClick = viewModel::onAddCourseClick,
        onDismissNewCourseDialog = viewModel::onDismissNewCourseDialog,
        onConfirmNewCourse = viewModel::onConfirmNewCourse,
        onSelectionModeToggle = viewModel::onSelectionModeToggle,
        onCourseSelectToggle = viewModel::onCourseSelectToggle,
        onDeleteSelectedClick = viewModel::onDeleteSelectedClick,
        onDismissDeleteConfirm = viewModel::onDismissDeleteConfirm,
        onConfirmDeleteSelected = viewModel::onConfirmDeleteSelected,
        onNavigateTab = onNavigateTab,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseListScreenContent(
    uiState: CourseListUiState,
    snackbarHostState: SnackbarHostState,
    onCourseClick: (String) -> Unit,
    onAddCourseClick: () -> Unit,
    onDismissNewCourseDialog: () -> Unit,
    onConfirmNewCourse: (title: String, description: String) -> Unit,
    onSelectionModeToggle: () -> Unit,
    onCourseSelectToggle: (String) -> Unit,
    onDeleteSelectedClick: () -> Unit,
    onDismissDeleteConfirm: () -> Unit,
    onConfirmDeleteSelected: () -> Unit,
    onNavigateTab: (BottomNavDestination) -> Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    if (uiState.showNewCourseDialog) {
        NewCourseDialog(
            onConfirm = onConfirmNewCourse,
            onDismiss = onDismissNewCourseDialog,
        )
    }

    if (uiState.showDeleteConfirm) {
        ConfirmActionDialog(
            title = "선택한 코스를 삭제할까요?",
            highlightText = "${uiState.selectedCourseIds.size}개 코스",
            confirmLabel = "삭제",
            onConfirm = onConfirmDeleteSelected,
            onDismiss = onDismissDeleteConfirm,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KkokDayMainBackground,
        snackbarHost = { KkokDaySnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selected = BottomNavDestination.COURSE,
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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "내 코스",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    modifier = Modifier.weight(1f),
                )
                if (uiState.selectionMode) {
                    TextButton(onClick = onSelectionModeToggle) {
                        Text("취소", color = KkokDayMainSubText, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDeleteSelectedClick,
                        enabled = uiState.selectedCourseIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainRed, contentColor = Color.White),
                    ) {
                        Text("삭제(${uiState.selectedCourseIds.size})", fontWeight = FontWeight.Bold)
                    }
                } else {
                    IconButton(onClick = onAddCourseClick) {
                        Icon(Icons.Filled.Add, contentDescription = "새 코스 만들기", tint = KkokDayMainTextDark)
                    }
                    TextButton(onClick = onSelectionModeToggle) {
                        Text("삭제", color = KkokDayMainRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.courses.isEmpty() -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KkokDayMainYellow,
                    )
                    uiState.courses.isEmpty() -> EmptyCourses(modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(contentPadding = ResultsPadding) {
                        items(uiState.courses, key = { it.id }) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onCourseClick(course.id) },
                                selectionMode = uiState.selectionMode,
                                isSelected = course.id in uiState.selectedCourseIds,
                                onSelectToggle = { onCourseSelectToggle(course.id) },
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

private val ResultsPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

@Composable
private fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CourseCardGradient)
            .border(
                width = 1.dp,
                color = if (selectionMode && isSelected) KkokDayMainYellow else KkokDayMainCardBorder,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = if (selectionMode) onSelectToggle else onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (course.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = KkokDayMainSubText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = KkokDayMainSubText,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "장소 ${course.places.size}개",
                        style = MaterialTheme.typography.labelSmall,
                        color = KkokDayMainSubText,
                    )
                }
            }
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = KkokDayMainYellow),
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = KkokDayMainSubText,
                )
            }
        }
    }
}

@Composable
private fun EmptyCourses(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "아직 만든 코스가 없어요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "즐겨찾기나 카테고리 결과에서\n장소를 골라 코스를 만들어보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CourseListScreenPreview() {
    KkokDayTheme {
        CourseListScreenContent(
            uiState = CourseListUiState(
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
            onAddCourseClick = {},
            onDismissNewCourseDialog = {},
            onConfirmNewCourse = { _, _ -> },
            onSelectionModeToggle = {},
            onCourseSelectToggle = {},
            onDeleteSelectedClick = {},
            onDismissDeleteConfirm = {},
            onConfirmDeleteSelected = {},
            onNavigateTab = { true },
        )
    }
}
