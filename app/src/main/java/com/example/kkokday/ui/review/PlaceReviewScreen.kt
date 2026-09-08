package com.example.kkokday.ui.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.kkokday.data.review.MAX_REVIEW_PHOTOS
import com.example.kkokday.data.review.PlaceReview
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.common.KkokDaySnackbarTone
import com.example.kkokday.ui.common.showKkokDaySnackbar
import com.example.kkokday.ui.main.components.ConfirmActionDialog
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainRed
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlaceReviewRoute(
    onNavigateBack: () -> Unit,
    viewModel: PlaceReviewViewModel = hiltViewModel(),
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

    PlaceReviewScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onWriteReviewClick = viewModel::onWriteReviewClick,
        onDeleteReviewClick = viewModel::onDeleteReviewClick,
        onDismissDeleteConfirm = viewModel::onDismissDeleteConfirm,
        onConfirmDeleteReview = viewModel::onConfirmDeleteReview,
        onDismissEditSheet = viewModel::onDismissEditSheet,
        onRatingSelected = viewModel::onRatingSelected,
        onCommentChange = viewModel::onCommentChange,
        onPhotosPicked = viewModel::onPhotosPicked,
        onRemoveExistingPhoto = viewModel::onRemoveExistingPhoto,
        onRemoveNewPhoto = viewModel::onRemoveNewPhoto,
        onSaveReviewClick = viewModel::onSaveReviewClick,
        onReportReviewClick = viewModel::onReportReviewClick,
        onReportReasonChange = viewModel::onReportReasonChange,
        onDismissReportDialog = viewModel::onDismissReportDialog,
        onSubmitReport = viewModel::onSubmitReport,
        onBlockAuthorClick = viewModel::onBlockAuthorClick,
        onDismissBlockConfirm = viewModel::onDismissBlockConfirm,
        onConfirmBlockAuthor = viewModel::onConfirmBlockAuthor,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceReviewScreenContent(
    uiState: PlaceReviewUiState,
    snackbarHostState: SnackbarHostState,
    onWriteReviewClick: () -> Unit,
    onDeleteReviewClick: () -> Unit,
    onDismissDeleteConfirm: () -> Unit,
    onConfirmDeleteReview: () -> Unit,
    onDismissEditSheet: () -> Unit,
    onRatingSelected: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onPhotosPicked: (List<Uri>) -> Unit,
    onRemoveExistingPhoto: (String) -> Unit,
    onRemoveNewPhoto: (Uri) -> Unit,
    onSaveReviewClick: () -> Unit,
    onReportReviewClick: (PlaceReview) -> Unit,
    onReportReasonChange: (String) -> Unit,
    onDismissReportDialog: () -> Unit,
    onSubmitReport: () -> Unit,
    onBlockAuthorClick: (PlaceReview) -> Unit,
    onDismissBlockConfirm: () -> Unit,
    onConfirmBlockAuthor: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var photoViewerState by remember { mutableStateOf<PhotoViewerState?>(null) }
    photoViewerState?.let { state ->
        PhotoViewerDialog(
            photoUrls = state.photoUrls,
            initialIndex = state.initialIndex,
            onDismiss = { photoViewerState = null },
        )
    }

    // "더보기"로 연 신고/차단 선택 시트 — 어떤 리뷰를 대상으로 열렸는지만 화면에서 잠깐
    // 기억하면 되는 값이라(제출 자체는 ViewModel이 처리) ViewModel 상태로 올리지 않았다.
    var reviewActionsTarget by remember { mutableStateOf<PlaceReview?>(null) }
    reviewActionsTarget?.let { target ->
        ReviewActionsBottomSheet(
            onReportClick = {
                reviewActionsTarget = null
                onReportReviewClick(target)
            },
            onBlockAuthorClick = {
                reviewActionsTarget = null
                onBlockAuthorClick(target)
            },
            onDismiss = { reviewActionsTarget = null },
        )
    }

    uiState.reportTargetReview?.let {
        ReportReviewDialog(
            reason = uiState.reportReason,
            isSubmitting = uiState.isSubmittingReport,
            onReasonChange = onReportReasonChange,
            onSubmit = onSubmitReport,
            onDismiss = onDismissReportDialog,
        )
    }

    uiState.blockTargetReview?.let { target ->
        ConfirmActionDialog(
            title = "이 작성자를 차단할까요?",
            highlightText = "${target.authorNickname}님의 리뷰가 이 장소에서 더 이상 보이지 않아요",
            confirmLabel = "차단",
            onConfirm = onConfirmBlockAuthor,
            onDismiss = onDismissBlockConfirm,
        )
    }

    if (uiState.showEditSheet) {
        ReviewEditBottomSheet(
            isNewReview = uiState.myReview == null,
            editState = uiState.editState,
            onRatingSelected = onRatingSelected,
            onCommentChange = onCommentChange,
            onPhotosPicked = onPhotosPicked,
            onRemoveExistingPhoto = onRemoveExistingPhoto,
            onRemoveNewPhoto = onRemoveNewPhoto,
            onSaveClick = onSaveReviewClick,
            onDismiss = onDismissEditSheet,
        )
    }

    if (uiState.showDeleteConfirm) {
        ConfirmActionDialog(
            title = "내 리뷰를 삭제할까요?",
            highlightText = uiState.placeName,
            confirmLabel = "삭제",
            onConfirm = onConfirmDeleteReview,
            onDismiss = onDismissDeleteConfirm,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KkokDayMainBackground,
        snackbarHost = { KkokDaySnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = KkokDayMainTextDark)
                }
                Text(
                    text = uiState.placeName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            if (uiState.address.isNotBlank()) {
                Text(
                    text = uiState.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 리뷰가 없으면 이 요약 행 자체를 생략한다 — "아직 리뷰가 없어요"는 아래
            // EmptyReviews 하나로만 전달하고, 여기서 같은 말을 밋밋한 텍스트로 반복하지 않는다.
            if (uiState.reviewCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = KkokDayMainYellow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(uiState.avgRating),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KkokDayMainTextDark,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "리뷰 ${uiState.reviewCount}개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KkokDayMainSubText,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = onWriteReviewClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
                ) {
                    Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.myReview != null) "내 리뷰 수정" else "리뷰 작성", fontWeight = FontWeight.Bold)
                }
                if (uiState.myReview != null) {
                    TextButton(
                        onClick = onDeleteReviewClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = KkokDayMainRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("내 리뷰 삭제", color = KkokDayMainRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.reviews.isEmpty() -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KkokDayMainYellow,
                    )
                    uiState.reviews.isEmpty() -> EmptyReviews(modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(contentPadding = ReviewListPadding) {
                        items(uiState.reviews, key = { it.authorUid }) { review ->
                            ReviewListItem(
                                review = review,
                                showActions = review.authorUid != uiState.currentUserUid,
                                onPhotoClick = { photoUrls, index ->
                                    photoViewerState = PhotoViewerState(photoUrls, index)
                                },
                                onMoreClick = { reviewActionsTarget = review },
                            )
                        }
                    }
                }
            }
        }
    }
}

private val ReviewListPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

/**
 * [com.example.kkokday.ui.favorite.EmptyFavorites]/[EmptyResults]와 같은 톤(72dp 아이콘 +
 * titleLarge 굵은 메인 문구 + bodyMedium 서브 문구, 20dp/8dp 간격)으로 맞췄다 — 이 앱의
 * 빈 상태 화면이 공통으로 쓰는 패턴을 그대로 재사용.
 */
@Composable
private fun BoxScope.EmptyReviews(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = KkokDayMainYellow, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "아직 리뷰가 없어요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "첫 리뷰를 남겨보세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReviewListItem(
    review: PlaceReview,
    showActions: Boolean,
    onPhotoClick: (List<String>, Int) -> Unit,
    onMoreClick: (PlaceReview) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = review.authorNickname,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatReviewDate(review.updatedAtMillis),
                style = MaterialTheme.typography.labelSmall,
                color = KkokDayMainSubText,
            )
            // 내 리뷰엔 신고/차단이 의미가 없으므로 다른 사람 리뷰에만 "더보기"를 보여준다.
            if (showActions) {
                IconButton(onClick = { onMoreClick(review) }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "더보기",
                        tint = KkokDayMainSubText,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        StarRatingBar(rating = review.rating, starSize = 14.dp)
        if (review.comment.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = KkokDayMainTextDark,
            )
        }
        if (review.photoUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(review.photoUrls, key = { _, url -> url }) { index, url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPhotoClick(review.photoUrls, index) },
                    )
                }
            }
        }
    }
}

private fun formatReviewDate(millis: Long): String =
    SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(millis))

/**
 * 리뷰 행 "더보기"를 누르면 뜨는 신고/차단 선택 시트. [com.example.kkokday.ui.category.PlaceContactBottomSheet]/
 * [com.example.kkokday.ui.directions.DirectionsBottomSheet]와 같은 흰 배경 바텀시트
 * 톤이지만, 아이콘 원형 옵션이 아니라 세로 리스트 행(마이 탭 메뉴와 같은 언어)을 쓴다 —
 * 두 옵션 다 문구가 길고 "이 목록에서 고르는" 성격이라 세로 나열이 더 자연스럽다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewActionsBottomSheet(
    onReportClick: () -> Unit,
    onBlockAuthorClick: () -> Unit,
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
            ReviewActionRow(icon = Icons.Filled.Flag, label = "신고하기", onClick = onReportClick)
            ReviewActionRow(
                icon = Icons.Filled.PersonOff,
                label = "이 작성자 차단",
                tint = KkokDayMainRed,
                onClick = onBlockAuthorClick,
            )
        }
    }
}

@Composable
private fun ReviewActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = KkokDayMainTextDark,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = tint)
    }
}

/** 마이 탭 닉네임 변경 다이얼로그(MyScreen.kt)와 같은 흰 배경 AlertDialog 톤. */
@Composable
private fun ReportReviewDialog(
    reason: String,
    isSubmitting: Boolean,
    onReasonChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("리뷰 신고", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KkokDayMainTextDark)
        },
        text = {
            Column {
                Text(
                    text = "신고 사유를 간단히 알려주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 광고성 리뷰예요") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KkokDayMainYellow,
                        unfocusedBorderColor = KkokDayMainCardBorder,
                        cursorColor = KkokDayMainYellow,
                        focusedTextColor = KkokDayMainTextDark,
                        unfocusedTextColor = KkokDayMainTextDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedPlaceholderColor = KkokDayMainSubText,
                        unfocusedPlaceholderColor = KkokDayMainSubText,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = reason.isNotBlank() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayMainTextDark)
                } else {
                    Text("신고", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = KkokDayMainSubText)
            }
        },
    )
}

/** [PhotoViewerDialog]가 뭘 보여줄지 — 탭한 리뷰의 사진 전체와, 탭한 사진의 시작 인덱스. */
private data class PhotoViewerState(val photoUrls: List<String>, val initialIndex: Int)

private const val PHOTO_VIEWER_MIN_SCALE = 1f
private const val PHOTO_VIEWER_MAX_SCALE = 5f

/**
 * 사진 풀스크린 뷰어. [Dialog]를 `usePlatformDefaultWidth = false`로 열어 화면 전체를
 * 채우고, 그 안에 [HorizontalPager]로 좌우 스와이프 넘기기를 구현한다. 뒤로가기는
 * [DialogProperties.dismissOnBackPress]가 기본으로 처리해준다.
 *
 * 페이지마다 확대 상태를 두되, `remember(page)`로 키를 걸어 페이지가 바뀌면 확대 상태가
 * 초기화되게 한다 — 안 그러면 Pager가 슬롯을 재사용할 때 이전 페이지에서 확대했던 상태가
 * 다음 페이지로 그대로 넘어온다.
 */
@Composable
private fun PhotoViewerDialog(
    photoUrls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val pagerState = rememberPagerState(initialPage = initialIndex) { photoUrls.size }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                var scale by remember(page) { mutableStateOf(1f) }
                var offset by remember(page) { mutableStateOf(Offset.Zero) }
                AsyncImage(
                    model = photoUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                        )
                        .pointerInput(page) {
                            // detectTransformGestures를 그대로 쓰면 손가락 1개 드래그도
                            // "pan"으로 보고 터치 슬롭을 넘는 순간 이벤트를 consume()해버려서,
                            // 같은 이벤트를 봐야 하는 HorizontalPager의 내부 스크롤 감지기에는
                            // 아무것도 안 남아 스와이프가 먹통이 된다(실기기 확인). 확대 안 된
                            // 상태(scale==1f)에서 손가락 1개면 아예 소비하지 않고 그대로
                            // Pager로 흘려보내고, 확대된 상태이거나 손가락 2개(핀치) 이상일
                            // 때만 줌/팬으로 소비한다.
                            detectZoomAndPanIfNeeded(
                                isZoomedIn = { scale > PHOTO_VIEWER_MIN_SCALE },
                            ) { pan, zoomChange ->
                                val newScale = (scale * zoomChange)
                                    .coerceIn(PHOTO_VIEWER_MIN_SCALE, PHOTO_VIEWER_MAX_SCALE)
                                scale = newScale
                                offset = if (newScale <= PHOTO_VIEWER_MIN_SCALE) Offset.Zero else offset + pan
                            }
                        },
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(4.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "닫기", tint = Color.White)
            }
            if (photoUrls.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${photoUrls.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp),
                )
            }
        }
    }
}

/**
 * [androidx.compose.foundation.gestures.detectTransformGestures]와 달리, 손가락 1개
 * 드래그는 [isZoomedIn]이 false일 때 전혀 소비하지 않는다 — 그래야 부모 [HorizontalPager]가
 * 같은 드래그를 페이지 스와이프로 인식할 수 있다. 확대된 상태(isZoomedIn=true)이거나
 * 손가락이 2개 이상(핀치)일 때만 줌/팬으로 소비해서 [onZoomPan]에 알린다.
 */
private suspend fun PointerInputScope.detectZoomAndPanIfNeeded(
    isZoomedIn: () -> Boolean,
    onZoomPan: (pan: Offset, zoomChange: Float) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val shouldHandle = isZoomedIn() || event.changes.size > 1
            if (shouldHandle) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                if (zoomChange != 1f || panChange != Offset.Zero) {
                    onZoomPan(panChange, zoomChange)
                }
                event.changes.forEach { change ->
                    if (change.positionChanged()) change.consume()
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewEditBottomSheet(
    isNewReview: Boolean,
    editState: ReviewEditUiState,
    onRatingSelected: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onPhotosPicked: (List<Uri>) -> Unit,
    onRemoveExistingPhoto: (String) -> Unit,
    onRemoveNewPhoto: (Uri) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val remainingSlots = (MAX_REVIEW_PHOTOS - editState.photoCount).coerceAtLeast(0)
    // PickMultipleVisualMedia는 maxItems >= 2를 요구한다 — 남은 슬롯이 1개뿐이어도 2를
    // 넘겨 요청하고, 실제 반영은 onPhotosPicked에서 remainingSlots만큼만 잘라 처리한다.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(remainingSlots.coerceAtLeast(2)),
        onResult = onPhotosPicked,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = if (isNewReview) "리뷰 작성" else "리뷰 수정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
            )
            Spacer(modifier = Modifier.height(16.dp))
            StarRatingBar(rating = editState.rating, starSize = 32.dp, onRatingChange = onRatingSelected)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = editState.comment,
                onValueChange = onCommentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                placeholder = { Text("이 장소는 어땠나요?") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KkokDayMainYellow,
                    unfocusedBorderColor = KkokDayMainCardBorder,
                    cursorColor = KkokDayMainYellow,
                    focusedTextColor = KkokDayMainTextDark,
                    unfocusedTextColor = KkokDayMainTextDark,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedPlaceholderColor = KkokDayMainSubText,
                    unfocusedPlaceholderColor = KkokDayMainSubText,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "사진 (${editState.photoCount}/$MAX_REVIEW_PHOTOS)",
                style = MaterialTheme.typography.labelMedium,
                color = KkokDayMainSubText,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(editState.existingPhotoUrls, key = { it }) { url ->
                    PhotoThumbnail(model = url, onRemove = { onRemoveExistingPhoto(url) })
                }
                items(editState.newPhotoUris, key = { it.toString() }) { uri ->
                    PhotoThumbnail(model = uri, onRemove = { onRemoveNewPhoto(uri) })
                }
                if (remainingSlots > 0) {
                    item {
                        AddPhotoButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSaveClick,
                enabled = editState.rating > 0 && !editState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
            ) {
                if (editState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayMainTextDark)
                } else {
                    Text("저장", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PhotoThumbnail(model: Any, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(72.dp)) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(KkokDayMainTextDark.copy(alpha = 0.7f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, contentDescription = "사진 삭제", tint = Color.White, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun AddPhotoButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, KkokDayMainCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.AddAPhoto, contentDescription = "사진 추가", tint = KkokDayMainSubText, modifier = Modifier.size(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PlaceReviewScreenPreview() {
    KkokDayTheme {
        PlaceReviewScreenContent(
            uiState = PlaceReviewUiState(
                placeName = "삼대째손두부",
                address = "서울 강서구 송정로 48",
                avgRating = 4.5,
                reviewCount = 12,
                reviews = listOf(
                    PlaceReview(
                        placeDocId = "1",
                        authorUid = "u1",
                        authorNickname = "콕데이",
                        rating = 5,
                        comment = "정말 맛있어요! 재방문 의사 있습니다.",
                        createdAtMillis = System.currentTimeMillis(),
                        updatedAtMillis = System.currentTimeMillis(),
                    ),
                ),
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onWriteReviewClick = {},
            onDeleteReviewClick = {},
            onDismissDeleteConfirm = {},
            onConfirmDeleteReview = {},
            onDismissEditSheet = {},
            onRatingSelected = {},
            onCommentChange = {},
            onPhotosPicked = {},
            onRemoveExistingPhoto = {},
            onRemoveNewPhoto = {},
            onSaveReviewClick = {},
            onReportReviewClick = {},
            onReportReasonChange = {},
            onDismissReportDialog = {},
            onSubmitReport = {},
            onBlockAuthorClick = {},
            onDismissBlockConfirm = {},
            onConfirmBlockAuthor = {},
            onNavigateBack = {},
        )
    }
}
