package com.example.kkokday.ui.vote

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.common.KkokDaySnackbarTone
import com.example.kkokday.ui.common.showKkokDaySnackbar
import com.example.kkokday.ui.course.shareCourse
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme
import kotlinx.coroutines.launch

/** 바텀 네비게이션 "투표" 탭 → 코스 선택 후 도착하는 상세 화면. */
@Composable
fun VoteDetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: VoteDetailViewModel = hiltViewModel(),
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

    VoteDetailScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCandidateRowClick = viewModel::onCandidateRowClick,
        onDismissVoterList = viewModel::onDismissVoterList,
        onStartVotingClick = viewModel::onStartVotingClick,
        onCancelVotingClick = viewModel::onCancelVotingClick,
        onCandidateToggled = viewModel::onCandidateToggled,
        onVoterNameChange = viewModel::onVoterNameChange,
        onSubmitVoteClick = viewModel::onSubmitVoteClick,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoteDetailScreenContent(
    uiState: VoteDetailUiState,
    snackbarHostState: SnackbarHostState,
    onCandidateRowClick: (VoteCandidateUiModel) -> Unit,
    onDismissVoterList: () -> Unit,
    onStartVotingClick: () -> Unit,
    onCancelVotingClick: () -> Unit,
    onCandidateToggled: (String) -> Unit,
    onVoterNameChange: (String) -> Unit,
    onSubmitVoteClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    uiState.voterListCandidate?.let { candidate ->
        VoterListBottomSheet(candidate = candidate, onDismiss = onDismissVoterList)
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

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
                IconButton(onClick = if (uiState.isVotingMode) onCancelVotingClick else onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = KkokDayMainTextDark)
                }
                Text(
                    text = uiState.courseTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!uiState.isVotingMode) {
                    IconButton(onClick = {
                        shareCourse(context, clipboardManager, uiState.courseId)
                        coroutineScope.launch {
                            snackbarHostState.showKkokDaySnackbar("링크를 복사했어요", KkokDaySnackbarTone.SUCCESS)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "공유", tint = KkokDayMainSubText)
                    }
                }
            }
            Text(
                text = if (uiState.isVotingMode) {
                    "투표할 장소를 모두 선택해주세요"
                } else if (uiState.totalVoters > 0) {
                    "${uiState.totalVoters}명 참여"
                } else {
                    "아직 투표한 사람이 없어요"
                },
                style = MaterialTheme.typography.bodySmall,
                color = KkokDayMainSubText,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    uiState.isLoadingCourse -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KkokDayMainYellow,
                    )
                    uiState.candidates.isEmpty() -> EmptyCandidates(modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(contentPadding = ListPadding) {
                        items(uiState.candidates, key = { it.candidateId }) { candidate ->
                            VoteCandidateRow(
                                candidate = candidate,
                                isVotingMode = uiState.isVotingMode,
                                isChecked = candidate.candidateId in uiState.selectedCandidateIds,
                                onClick = {
                                    if (uiState.isVotingMode) {
                                        onCandidateToggled(candidate.candidateId)
                                    } else {
                                        onCandidateRowClick(candidate)
                                    }
                                },
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            if (uiState.candidates.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    if (uiState.isVotingMode) {
                        OutlinedTextField(
                            value = uiState.voterName,
                            onValueChange = onVoterNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("이름") },
                            placeholder = { Text("투표할 이름을 입력해주세요") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KkokDayMainYellow,
                                unfocusedBorderColor = KkokDayMainCardBorder,
                                cursorColor = KkokDayMainYellow,
                                focusedTextColor = KkokDayMainTextDark,
                                unfocusedTextColor = KkokDayMainTextDark,
                                focusedContainerColor = KkokDayMainBackground,
                                unfocusedContainerColor = KkokDayMainBackground,
                                focusedPlaceholderColor = KkokDayMainSubText,
                                unfocusedPlaceholderColor = KkokDayMainSubText,
                            ),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = onCancelVotingClick,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("취소", color = KkokDayMainSubText, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onSubmitVoteClick,
                                enabled = uiState.voterName.isNotBlank() && !uiState.isSubmitting,
                                modifier = Modifier
                                    .weight(2f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
                            ) {
                                if (uiState.isSubmitting) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayMainTextDark)
                                } else {
                                    Text("제출", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = onStartVotingClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
                        ) {
                            Icon(Icons.Filled.HowToVote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("투표하기", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private val ListPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)

@Composable
private fun VoteCandidateRow(
    candidate: VoteCandidateUiModel,
    isVotingMode: Boolean,
    isChecked: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isVotingMode) {
                    Modifier.toggleable(value = isChecked, onValueChange = { onClick() })
                } else {
                    Modifier.clickable(onClick = onClick)
                },
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isVotingMode && isChecked) KkokDayMainYellow else KkokDayMainCardBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isVotingMode) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = KkokDayMainYellow),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate.place.placeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = candidate.place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = KkokDayMainSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = KkokDayMainYellow.copy(alpha = if (candidate.voteCount > 0) 1f else 0.25f),
            ) {
                Text(
                    text = "${candidate.voteCount}표",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

/**
 * 후보를 탭하면 뜨는 투표자 이름 목록. [PlaceContactBottomSheet] 등과 같은 흰 배경 +
 * 타이틀 톤의 [ModalBottomSheet].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoterListBottomSheet(
    candidate: VoteCandidateUiModel,
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
                text = candidate.place.placeName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "투표한 사람 ${candidate.voteCount}명",
                style = MaterialTheme.typography.bodySmall,
                color = KkokDayMainSubText,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (candidate.voterNames.isEmpty()) {
                EmptyVoters()
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(candidate.voterNames, key = { it }) { name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = KkokDayMainSubText,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = KkokDayMainTextDark,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * [com.example.kkokday.ui.review.PlaceReviewScreen]의 "아직 리뷰가 없어요" 빈 상태와
 * 같은 톤(72dp 아이콘 + titleLarge 굵은 메인 문구 + bodyMedium 서브 문구, 20dp/8dp 간격)으로
 * 맞췄다 — 이 앱의 빈 상태 화면이 공통으로 쓰는 패턴을 그대로 재사용.
 */
@Composable
private fun EmptyVoters() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "아직 투표한 사람이 없어요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "가장 먼저 투표해보세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyCandidates(modifier: Modifier = Modifier) {
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
            text = "투표할 장소가 없어요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "코스에 장소를 먼저 담아주세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun VoteDetailScreenResultsPreview() {
    KkokDayTheme {
        VoteDetailScreenContent(
            uiState = VoteDetailUiState(
                courseTitle = "홍대 데이트 코스",
                totalVoters = 3,
                voterName = "콕데이",
                isLoadingCourse = false,
                candidates = previewCandidates,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onCandidateRowClick = {},
            onDismissVoterList = {},
            onStartVotingClick = {},
            onCancelVotingClick = {},
            onCandidateToggled = {},
            onVoterNameChange = {},
            onSubmitVoteClick = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun VoteDetailScreenVotingPreview() {
    KkokDayTheme {
        VoteDetailScreenContent(
            uiState = VoteDetailUiState(
                courseTitle = "홍대 데이트 코스",
                totalVoters = 3,
                voterName = "콕데이",
                isLoadingCourse = false,
                isVotingMode = true,
                selectedCandidateIds = setOf("1"),
                candidates = previewCandidates,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onCandidateRowClick = {},
            onDismissVoterList = {},
            onStartVotingClick = {},
            onCancelVotingClick = {},
            onCandidateToggled = {},
            onVoterNameChange = {},
            onSubmitVoteClick = {},
            onNavigateBack = {},
        )
    }
}

private val previewCandidates = listOf(
    VoteCandidateUiModel(
        candidateId = "1",
        place = CoursePlace(
            placeName = "삼대째손두부",
            address = "서울 강서구 송정로 48",
            latitude = 37.5,
            longitude = 126.8,
            kakaoPlaceId = "1",
            category = "음식점 > 한식",
        ),
        voteCount = 2,
        voterNames = listOf("콕데이", "수진"),
    ),
    VoteCandidateUiModel(
        candidateId = "2",
        place = CoursePlace(
            placeName = "스타벅스 홍대역점",
            address = "서울 마포구 양화로",
            latitude = 37.5,
            longitude = 126.8,
            kakaoPlaceId = "2",
            category = "음식점 > 카페",
        ),
        voteCount = 1,
        voterNames = listOf("콕데이"),
    ),
)
