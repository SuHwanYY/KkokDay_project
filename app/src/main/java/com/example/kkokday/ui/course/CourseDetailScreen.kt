package com.example.kkokday.ui.course

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.directions.NavigationDestination
import com.example.kkokday.directions.toNavigationDestination
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.common.KkokDaySnackbarTone
import com.example.kkokday.ui.common.showKkokDaySnackbar
import com.example.kkokday.ui.directions.DirectionsBottomSheet
import com.example.kkokday.ui.main.components.ConfirmActionDialog
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainRed
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** 드래그 재정렬 계산에 쓰는 [CoursePlaceRow] 한 칸의 대략적인 높이(카드 높이 + 아래 여백). */
private val COURSE_PLACE_ROW_SLOT_HEIGHT = 88.dp

@Composable
fun CourseDetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.courseDeleted) {
        if (uiState.courseDeleted) onNavigateBack()
    }

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

    CourseDetailScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onPlacesChanged = viewModel::onPlacesChanged,
        onRenameClick = viewModel::onRenameClick,
        onDismissRenameDialog = viewModel::onDismissRenameDialog,
        onConfirmRename = viewModel::onConfirmRename,
        onDeleteCourseClick = viewModel::onDeleteCourseClick,
        onDismissDeleteConfirm = viewModel::onDismissDeleteConfirm,
        onConfirmDeleteCourse = viewModel::onConfirmDeleteCourse,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailScreenContent(
    uiState: CourseDetailUiState,
    snackbarHostState: SnackbarHostState,
    onPlacesChanged: (List<CoursePlace>) -> Unit,
    onRenameClick: () -> Unit,
    onDismissRenameDialog: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onDeleteCourseClick: () -> Unit,
    onDismissDeleteConfirm: () -> Unit,
    onConfirmDeleteCourse: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var directionsDestination by remember { mutableStateOf<NavigationDestination?>(null) }
    val course = uiState.course

    directionsDestination?.let { destination ->
        DirectionsBottomSheet(
            destination = destination,
            onDismiss = { directionsDestination = null },
        )
    }

    if (uiState.showRenameDialog && course != null) {
        RenameCourseDialog(
            initialTitle = course.title,
            onConfirm = onConfirmRename,
            onDismiss = onDismissRenameDialog,
        )
    }

    if (uiState.showDeleteConfirm && course != null) {
        ConfirmActionDialog(
            title = "코스를 삭제할까요?",
            highlightText = course.title,
            confirmLabel = "삭제",
            onConfirm = onConfirmDeleteCourse,
            onDismiss = onDismissDeleteConfirm,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KkokDayMainBackground,
        snackbarHost = { KkokDaySnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when {
                uiState.isLoading && course == null -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = KkokDayMainYellow,
                )
                course == null -> Text(
                    text = uiState.errorMessage ?: "코스를 찾을 수 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KkokDayMainSubText,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp),
                    textAlign = TextAlign.Center,
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 4.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = KkokDayMainTextDark)
                        }
                        Text(
                            text = course.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KkokDayMainTextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onRenameClick) {
                            Icon(Icons.Filled.Edit, contentDescription = "이름 변경", tint = KkokDayMainSubText)
                        }
                        IconButton(onClick = {
                            shareCourse(context, clipboardManager, course.id)
                            coroutineScope.launch {
                                snackbarHostState.showKkokDaySnackbar("링크를 복사했어요", KkokDaySnackbarTone.SUCCESS)
                            }
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "공유", tint = KkokDayMainSubText)
                        }
                    }

                    if (course.description.isNotBlank()) {
                        // 타이틀(titleLarge, bold) 아래 보조 설명을 bodyMedium 서브텍스트로 잇는
                        // 배치 — 인증 플로우 타이틀 화면들(EmailVerificationScreen 등)과 동일한
                        // "큰 제목 + 8dp 간격 + bodyMedium 서브텍스트" 패턴을 그대로 재사용한다.
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = course.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = KkokDayMainSubText,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (course.places.isEmpty()) {
                        EmptyCoursePlaces()
                    } else {
                        Column(modifier = Modifier.padding(ResultsPadding)) {
                            PlacesSection(
                                places = course.places,
                                onPlacesChanged = onPlacesChanged,
                                onDirectionsClick = { place -> directionsDestination = place.toNavigationDestination() },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = onDeleteCourseClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = KkokDayMainRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("코스 전체 삭제", color = KkokDayMainRed, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private val ResultsPadding = PaddingValues(horizontal = 20.dp)

/** 스크롤 가능한 [Column] 안에서 세로 위치를 계산하는 데 필요한 항목 하나(id는 드래그 중 위치가 바뀌어도 유지되는 안정적인 키). */
private data class DraggableEntry(val id: String, val place: CoursePlace)

/**
 * 코스 안 장소 목록 — 별도 재정렬 라이브러리 없이 직접 구현했다.
 *
 * 제스처는 각 항목의 바깥쪽 Box(=[graphicsLayer]로 위치를 옮기는 그 Box) **하나에서만**
 * 받는다. 예전엔 드래그 핸들 안쪽(자식)에 pointerInput을 중첩시켰는데, 그러면 이 Box가
 * translationY로 움직이는 순간 핸들 자신의 로컬 좌표계도 같은 방향·양만큼 같이 밀려서
 * 다음 이벤트의 이동량이 스스로 상쇄돼버렸다 — "롱프레스는 인식되는데 실제로 안 움직이는"
 * 원인이었다. pointerInput을 graphicsLayer보다 모디파이어 체인 앞쪽(바깥쪽)에 둬서, 이
 * Box 자신의 변환에 영향받지 않는 좌표계를 쓰도록 고쳤다.
 *
 * 터치 시작 x좌표로 세 영역을 구분한다([CoursePlaceRow]의 레이아웃 상수를 그대로 가져다
 * 근사한다 — 그 쪽 레이아웃이 바뀌면 여기도 같이 맞춰야 한다):
 * - 왼쪽 드래그 핸들: 롱프레스 없이 바로 드래그 시작(전용 핸들이라 스크롤 등 다른 제스처와
 *   겹칠 걱정이 없다).
 * - 오른쪽 버튼(길찾기/삭제) 두 개: 아무것도 소비하지 않고 그대로 흘려보내 원래 탭 동작이
 *   되게 한다.
 * - 그 사이 본문: 길게 누르면 그 항목이 잡히면서 드래그 시작(핸들과 동일한 [trackDrag] 재사용).
 *
 * [DraggableEntry.id](이번 화면 진입 세션에서만 쓰는 로컬 UUID)로 항목을 추적한다 —
 * CoursePlace 자체엔 고유 ID가 없고, 위치(index)로 추적하면 재정렬 도중 다른 항목으로
 * 제스처가 넘어가버린다.
 */
@Composable
private fun PlacesSection(
    places: List<CoursePlace>,
    onPlacesChanged: (List<CoursePlace>) -> Unit,
    onDirectionsClick: (CoursePlace) -> Unit,
) {
    val entriesState = remember(places) {
        mutableStateOf(places.map { DraggableEntry(id = UUID.randomUUID().toString(), place = it) })
    }
    val draggingIdState = remember { mutableStateOf<String?>(null) }
    val dragOffsetYState = remember { mutableStateOf(0f) }
    var pendingDeleteEntry by remember { mutableStateOf<DraggableEntry?>(null) }
    val hapticFeedback = LocalHapticFeedback.current

    pendingDeleteEntry?.let { entry ->
        ConfirmActionDialog(
            title = "이 장소를 코스에서 삭제할까요?",
            highlightText = entry.place.placeName,
            confirmLabel = "삭제",
            onConfirm = {
                val updated = entriesState.value.filterNot { it.id == entry.id }
                entriesState.value = updated
                onPlacesChanged(updated.map { it.place })
                pendingDeleteEntry = null
            },
            onDismiss = { pendingDeleteEntry = null },
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        entriesState.value.forEachIndexed { index, entry ->
            // entry.id로 키잉해서, 드래그 중 재정렬로 이 항목의 리스트 위치(index)가 바뀌어도
            // Compose가 같은 컴포저블(=진행 중인 pointerInput 코루틴)을 계속 재사용하게 한다.
            // 키가 없으면 위치 기준으로 슬롯이 재사용돼, 재정렬 한 번만 일어나도 진행 중이던
            // 제스처가 엉뚱한 항목으로 바뀌거나 취소돼버린다.
            key(entry.id) {
            val isDragging = entry.id == draggingIdState.value
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isDragging) 1f else 0f)
                    .pointerInput(entry.id) {
                        val handleZonePx = COURSE_PLACE_ROW_LEADING_PADDING.toPx() + DRAG_HANDLE_TOUCH_SIZE.toPx()
                        val buttonsZonePx = COURSE_PLACE_ROW_TRAILING_PADDING.toPx() +
                            COURSE_PLACE_ROW_ACTION_BUTTON_SIZE.toPx() * 2 +
                            COURSE_PLACE_ROW_ACTION_BUTTON_SPACING.toPx()
                        val widthPx = size.width.toFloat()
                        val slotHeightPx = COURSE_PLACE_ROW_SLOT_HEIGHT.toPx()

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val touchX = down.position.x

                            when {
                                touchX >= widthPx - buttonsZonePx -> {
                                    // 버튼 영역 — 소비하지 않는다. PlaceActionButton의 clickable이 그대로 처리한다.
                                }
                                touchX <= handleZonePx -> {
                                    down.consume()
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    trackDrag(down.id, entry.id, entriesState, draggingIdState, dragOffsetYState, slotHeightPx) {
                                        onPlacesChanged(it.map { moved -> moved.place })
                                    }
                                }
                                else -> {
                                    down.consume()
                                    // long-press 대기 동안에도 같은 손가락의 이동을 전부 소비해서, 이 화면을
                                    // 감싸는 부모 verticalScroll이 그 미세한 떨림을 스크롤로 먼저 가로채지
                                    // 못하게 한다. 타임아웃 전에 손을 떼면 정상 취소되고(non-null 반환),
                                    // 타임아웃까지 눌려있으면(성공) withTimeoutOrNull이 null을 반환한다.
                                    val longPressAchieved = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull
                                            if (!change.pressed) return@withTimeoutOrNull
                                            change.consume()
                                        }
                                    } == null
                                    if (longPressAchieved) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        trackDrag(down.id, entry.id, entriesState, draggingIdState, dragOffsetYState, slotHeightPx) {
                                            onPlacesChanged(it.map { moved -> moved.place })
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .graphicsLayer { translationY = if (isDragging) dragOffsetYState.value else 0f },
            ) {
                CoursePlaceRow(
                    place = entry.place,
                    order = index + 1,
                    isDragging = isDragging,
                    onDirectionsClick = { onDirectionsClick(entry.place) },
                    onDeleteClick = { pendingDeleteEntry = entry },
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * 실제 드래그 추적 — 핸들(바로 시작)과 본문(롱프레스 후 시작) 두 트리거가 공유한다.
 * 포인터가 눌려있는 동안 이동량을 누적해 [slotHeightPx] 단위로 몇 칸 옮겼는지 환산하고,
 * 칸이 바뀔 때마다 [entriesState]를 즉시 재정렬한다. 손을 떼면 최종 순서로 [onFinished]를 호출한다.
 *
 * move 추적 자체는 손으로 짠 while(true) 루프 대신 Foundation이 제공하는 [drag]를 쓴다 —
 * `detectDragGestures`/`detectDragGesturesAfterLongPress` 내부에서 쓰는 것과 같은, 포인터
 * 하나를 손을 뗄 때까지 계속 따라가주는 검증된 함수라 직접 구현보다 안전하다.
 */
private suspend fun AwaitPointerEventScope.trackDrag(
    pointerId: PointerId,
    entryId: String,
    entriesState: MutableState<List<DraggableEntry>>,
    draggingIdState: MutableState<String?>,
    dragOffsetYState: MutableState<Float>,
    slotHeightPx: Float,
    onFinished: (List<DraggableEntry>) -> Unit,
) {
    draggingIdState.value = entryId
    dragOffsetYState.value = 0f

    drag(pointerId) { change ->
        // positionChange()는 이미 consume()된 change에 대해 항상 Offset.Zero를 반환한다 —
        // 델타를 먼저 읽고 나서 consume()해야 한다(순서가 바뀌면 오프셋이 계속 0으로 찍힌다.
        // 실기기 로그로 실제 확인한 버그: position/previousPosition은 정상인데 delta만 0이었음).
        val delta = change.positionChange().y
        change.consume()
        dragOffsetYState.value += delta

        val currentEntries = entriesState.value
        val currentIndex = currentEntries.indexOfFirst { it.id == entryId }
        if (currentIndex != -1) {
            val targetIndex = (currentIndex + (dragOffsetYState.value / slotHeightPx).roundToInt())
                .coerceIn(0, currentEntries.lastIndex)
            if (targetIndex != currentIndex) {
                val moved = currentEntries.toMutableList()
                val item = moved.removeAt(currentIndex)
                moved.add(targetIndex, item)
                entriesState.value = moved
                dragOffsetYState.value -= (targetIndex - currentIndex) * slotHeightPx
            }
        }
    }

    draggingIdState.value = null
    dragOffsetYState.value = 0f
    onFinished(entriesState.value)
}

@Composable
private fun EmptyCoursePlaces() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Place, contentDescription = null, tint = KkokDayMainYellow, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "아직 담은 장소가 없어요",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RenameCourseDialog(
    initialTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(initialTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("코스 이름 변경", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KkokDayMainTextDark)
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KkokDayMainYellow,
                    unfocusedBorderColor = KkokDayMainCardBorder,
                    cursorColor = KkokDayMainYellow,
                    focusedTextColor = KkokDayMainTextDark,
                    unfocusedTextColor = KkokDayMainTextDark,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.trim()) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
            ) {
                Text("변경", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = KkokDayMainSubText)
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CourseDetailScreenPreview() {
    KkokDayTheme {
        CourseDetailScreenContent(
            uiState = CourseDetailUiState(
                isLoading = false,
                course = Course(
                    id = "1",
                    ownerId = "u1",
                    title = "홍대 데이트 코스",
                    description = "저녁 먹고 카페 가기",
                    places = listOf(
                        CoursePlace(
                            placeName = "삼대째손두부",
                            address = "서울 강서구 송정로 48",
                            latitude = 37.5,
                            longitude = 126.8,
                            kakaoPlaceId = "1",
                            category = "음식점 > 한식",
                        ),
                        CoursePlace(
                            placeName = "스타벅스 홍대역점",
                            address = "서울 마포구 양화로",
                            latitude = 37.5,
                            longitude = 126.8,
                            kakaoPlaceId = "2",
                            category = "음식점 > 카페",
                        ),
                    ),
                    createdAtMillis = System.currentTimeMillis(),
                    updatedAtMillis = System.currentTimeMillis(),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onPlacesChanged = {},
            onRenameClick = {},
            onDismissRenameDialog = {},
            onConfirmRename = {},
            onDeleteCourseClick = {},
            onDismissDeleteConfirm = {},
            onConfirmDeleteCourse = {},
            onNavigateBack = {},
        )
    }
}
