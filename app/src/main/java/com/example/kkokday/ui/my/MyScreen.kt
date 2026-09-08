package com.example.kkokday.ui.my

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.kkokday.BuildConfig
import com.example.kkokday.data.review.PlaceReview
import com.example.kkokday.directions.ContactActionLauncher
import com.example.kkokday.ui.auth.components.NicknameField
import com.example.kkokday.ui.auth.components.PRIVACY_POLICY_URL
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.common.KkokDaySnackbarTone
import com.example.kkokday.ui.common.showKkokDaySnackbar
import com.example.kkokday.ui.course.CourseCardGradient
import com.example.kkokday.ui.main.components.BottomNavBar
import com.example.kkokday.ui.main.components.ConfirmActionDialog
import com.example.kkokday.ui.main.model.BottomNavDestination
import com.example.kkokday.ui.review.StarRatingBar
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
import kotlinx.coroutines.launch

/** 바텀 네비게이션 "마이" 탭 진입 경로. 프로필 카드 + 계정 관리 메뉴 리스트를 보여준다. */
@Composable
fun MyRoute(
    onNavigateTab: (BottomNavDestination) -> Boolean,
    onSignedOut: () -> Unit,
    onNavigateToPlaceReview: (PlaceReview) -> Unit,
    viewModel: MyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.signedOut) {
        if (uiState.signedOut) {
            viewModel.consumeSignedOut()
            onSignedOut()
        }
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

    MyScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onProfileImagePicked = viewModel::onProfileImagePicked,
        onNicknameMenuClick = viewModel::onNicknameMenuClick,
        onNicknameInputChange = viewModel::onNicknameInputChange,
        onCheckNicknameClick = viewModel::onCheckNicknameClick,
        onConfirmNicknameChange = viewModel::onConfirmNicknameChange,
        onDismissNicknameDialog = viewModel::onDismissNicknameDialog,
        onLogoutMenuClick = viewModel::onLogoutMenuClick,
        onConfirmSignOut = viewModel::onConfirmSignOut,
        onDismissSignOutConfirm = viewModel::onDismissSignOutConfirm,
        onDeleteAccountMenuClick = viewModel::onDeleteAccountMenuClick,
        onConfirmDeleteAccount = viewModel::onConfirmDeleteAccount,
        onDismissDeleteAccountConfirm = viewModel::onDismissDeleteAccountConfirm,
        onNavigateTab = onNavigateTab,
        onReviewClick = onNavigateToPlaceReview,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyScreenContent(
    uiState: MyUiState,
    snackbarHostState: SnackbarHostState,
    onProfileImagePicked: (Uri) -> Unit,
    onNicknameMenuClick: () -> Unit,
    onNicknameInputChange: (String) -> Unit,
    onCheckNicknameClick: () -> Unit,
    onConfirmNicknameChange: () -> Unit,
    onDismissNicknameDialog: () -> Unit,
    onLogoutMenuClick: () -> Unit,
    onConfirmSignOut: () -> Unit,
    onDismissSignOutConfirm: () -> Unit,
    onDeleteAccountMenuClick: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
    onDismissDeleteAccountConfirm: () -> Unit,
    onNavigateTab: (BottomNavDestination) -> Boolean,
    onReviewClick: (PlaceReview) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(onProfileImagePicked) }

    if (uiState.showNicknameDialog) {
        NicknameChangeDialog(
            uiState = uiState,
            onNicknameInputChange = onNicknameInputChange,
            onCheckNicknameClick = onCheckNicknameClick,
            onConfirm = onConfirmNicknameChange,
            onDismiss = onDismissNicknameDialog,
        )
    }

    if (uiState.showSignOutConfirm) {
        ConfirmActionDialog(
            title = "로그아웃할까요?",
            highlightText = uiState.nickname,
            confirmLabel = "로그아웃",
            onConfirm = onConfirmSignOut,
            onDismiss = onDismissSignOutConfirm,
        )
    }

    if (uiState.showDeleteAccountConfirm) {
        ConfirmActionDialog(
            title = "정말 계정을 삭제할까요?",
            highlightText = "계정과 모든 데이터가 영구 삭제되며 되돌릴 수 없어요",
            confirmLabel = "삭제",
            onConfirm = onConfirmDeleteAccount,
            onDismiss = onDismissDeleteAccountConfirm,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KkokDayMainBackground,
        snackbarHost = { KkokDaySnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selected = BottomNavDestination.MY,
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "마이",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            ProfileCard(
                nickname = uiState.nickname,
                profileImageUrl = uiState.profileImageUrl,
                isLoading = uiState.isLoadingProfile,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White,
                border = BorderStroke(1.dp, KkokDayMainCardBorder),
            ) {
                Column {
                    MyMenuRow(
                        icon = Icons.Filled.Edit,
                        label = "닉네임 변경",
                        onClick = onNicknameMenuClick,
                    )
                    HorizontalDivider(color = KkokDayMainCardBorder)
                    MyMenuRow(
                        icon = Icons.Filled.AddAPhoto,
                        label = "프로필 사진 변경",
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        trailing = {
                            if (uiState.isUploadingProfileImage) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayMainYellow)
                            }
                        },
                    )
                    HorizontalDivider(color = KkokDayMainCardBorder)
                    MyMenuRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        label = "로그아웃",
                        onClick = onLogoutMenuClick,
                    )
                    HorizontalDivider(color = KkokDayMainCardBorder)
                    MyMenuRow(
                        icon = Icons.Filled.DeleteForever,
                        label = "회원탈퇴",
                        onClick = onDeleteAccountMenuClick,
                        tint = KkokDayMainRed,
                        isLoading = uiState.isDeletingAccount,
                    )
                    HorizontalDivider(color = KkokDayMainCardBorder)
                    MyMenuRow(
                        icon = Icons.Filled.PrivacyTip,
                        label = "개인정보처리방침",
                        onClick = { ContactActionLauncher.openUrl(context, PRIVACY_POLICY_URL) },
                    )
                    HorizontalDivider(color = KkokDayMainCardBorder)
                    MyMenuRow(
                        icon = Icons.Filled.Info,
                        label = "앱 버전 정보",
                        trailingText = BuildConfig.VERSION_NAME,
                        onClick = null,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "내가 쓴 리뷰",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            MyReviewsSection(
                reviews = uiState.myReviews,
                isLoading = uiState.isLoadingReviews,
                onReviewClick = onReviewClick,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileCard(
    nickname: String,
    profileImageUrl: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CourseCardGradient)
            .border(width = 1.dp, color = KkokDayMainCardBorder, shape = RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatar(profileImageUrl = profileImageUrl, size = 64.dp)
        Spacer(modifier = Modifier.width(14.dp))
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = KkokDayMainYellow)
        } else {
            Text(
                text = nickname,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KkokDayMainTextDark,
            )
        }
    }
}

/**
 * 리뷰가 있으면 [MyReviewItem] 세로 리스트, 없으면 [RecentPlacesSection]의 빈 상태와
 * 같은 톤(56dp 아이콘 + bodyMedium 서브 문구, 120dp 높이)으로 대체한다.
 */
@Composable
private fun MyReviewsSection(
    reviews: List<PlaceReview>,
    isLoading: Boolean,
    onReviewClick: (PlaceReview) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> Box(
            modifier = modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = KkokDayMainYellow)
        }
        reviews.isEmpty() -> EmptyMyReviews(modifier = modifier)
        else -> Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            reviews.forEach { review ->
                MyReviewItem(review = review, onClick = { onReviewClick(review) })
            }
        }
    }
}

@Composable
private fun EmptyMyReviews(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.RateReview,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "아직 작성한 리뷰가 없어요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
        )
    }
}

@Composable
private fun MyReviewItem(review: PlaceReview, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, KkokDayMainCardBorder),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.placeName.ifBlank { "장소 정보 없음" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatMyReviewDate(review.updatedAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = KkokDayMainSubText,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            StarRatingBar(rating = review.rating, starSize = 14.dp)
            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KkokDayMainTextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun formatMyReviewDate(millis: Long): String =
    SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(millis))

/**
 * [profileImageUrl]이 없으면 기본 아바타(옐로우 원 + 흰 사람 아이콘)를 보여준다 —
 * 회원가입 시점에 프로필 사진을 안 넣는 이메일 가입 사용자가 기본으로 보는 모습과 같다.
 */
@Composable
private fun ProfileAvatar(profileImageUrl: String?, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(KkokDayMainYellow),
        contentAlignment = Alignment.Center,
    ) {
        if (profileImageUrl != null) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f),
            )
        }
    }
}

@Composable
private fun MyMenuRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)?,
    tint: Color = KkokDayMainTextDark,
    trailingText: String? = null,
    isLoading: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = tint,
            modifier = Modifier.weight(1f),
        )
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayMainRed)
            trailing != null -> trailing()
            trailingText != null -> Text(
                text = trailingText,
                style = MaterialTheme.typography.bodySmall,
                color = KkokDayMainSubText,
            )
        }
    }
}

@Composable
private fun NicknameChangeDialog(
    uiState: MyUiState,
    onNicknameInputChange: (String) -> Unit,
    onCheckNicknameClick: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("닉네임 변경", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KkokDayMainTextDark)
        },
        text = {
            Column {
                NicknameField(
                    nickname = uiState.nicknameInput,
                    onNicknameChange = onNicknameInputChange,
                    nicknameError = uiState.nicknameInputError,
                    nicknameCheckState = uiState.nicknameCheckState,
                    onCheckNicknameClick = onCheckNicknameClick,
                    isDark = false,
                    accentColor = KkokDayMainYellow,
                )
                // 이 다이얼로그가 모달로 떠 있는 동안은 Scaffold의 에러 스낵바가 가려지므로,
                // 실패 메시지를 다이얼로그 안에서 바로 보여준다.
                if (uiState.nicknameDialogError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.nicknameDialogError,
                        style = MaterialTheme.typography.labelSmall,
                        color = KkokDayMainRed,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = uiState.isNicknameChangeConfirmEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
            ) {
                if (uiState.isSavingNickname) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = KkokDayMainTextDark)
                } else {
                    Text("변경", fontWeight = FontWeight.Bold)
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MyScreenPreview() {
    KkokDayTheme {
        MyScreenContent(
            uiState = MyUiState(
                nickname = "콕데이",
                isLoadingProfile = false,
                isLoadingReviews = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onProfileImagePicked = {},
            onNicknameMenuClick = {},
            onNicknameInputChange = {},
            onCheckNicknameClick = {},
            onConfirmNicknameChange = {},
            onDismissNicknameDialog = {},
            onLogoutMenuClick = {},
            onConfirmSignOut = {},
            onDismissSignOutConfirm = {},
            onDeleteAccountMenuClick = {},
            onConfirmDeleteAccount = {},
            onDismissDeleteAccountConfirm = {},
            onNavigateTab = { true },
            onReviewClick = {},
        )
    }
}
