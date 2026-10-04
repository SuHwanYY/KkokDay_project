package com.example.kkokday.ui.favorite

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.place.CategoryPlace
import com.example.kkokday.data.place.Favorite
import com.example.kkokday.directions.NavigationDestination
import com.example.kkokday.directions.toNavigationDestination
import com.example.kkokday.ui.category.CategoryPlaceRow
import com.example.kkokday.ui.category.PlaceContactBottomSheet
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.common.KkokDaySnackbarTone
import com.example.kkokday.ui.common.showKkokDaySnackbar
import com.example.kkokday.ui.course.ExistingCoursePickerBottomSheet
import com.example.kkokday.ui.course.NewCourseDialog
import com.example.kkokday.ui.directions.DirectionsBottomSheet
import com.example.kkokday.ui.main.components.BottomNavBar
import com.example.kkokday.ui.main.model.BottomNavDestination
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme
import kotlinx.coroutines.launch

/** 바텀 네비게이션 "즐겨찾기" 탭 진입 경로. [users/{uid}/favorites]를 저장 최신순으로 보여준다. */
@Composable
fun FavoritesRoute(
    onNavigateTab: (BottomNavDestination) -> Boolean,
    onNavigateToPlaceReview: (CategoryPlace) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showKkokDaySnackbar(message, KkokDaySnackbarTone.ERROR)
        viewModel.consumeErrorMessage()
    }

    LaunchedEffect(uiState.favoriteToastMessage) {
        val message = uiState.favoriteToastMessage ?: return@LaunchedEffect
        snackbarHostState.showKkokDaySnackbar(message, KkokDaySnackbarTone.SUCCESS)
        viewModel.consumeFavoriteToastMessage()
    }

    LaunchedEffect(uiState.courseSave.toastMessage) {
        val message = uiState.courseSave.toastMessage ?: return@LaunchedEffect
        snackbarHostState.showKkokDaySnackbar(message, KkokDaySnackbarTone.SUCCESS)
        viewModel.consumeCourseToastMessage()
    }

    FavoritesScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onFavoriteToggle = viewModel::onFavoriteToggleClick,
        onCourseModeToggle = viewModel::onCourseModeToggle,
        onPlaceSelectToggle = viewModel::onPlaceSelectToggle,
        onCourseSaveClick = viewModel::onCourseSaveClick,
        onDismissExistingCoursePicker = viewModel::onDismissExistingCoursePicker,
        onExistingCourseSelected = viewModel::onExistingCourseSelected,
        onCreateNewCourseClick = viewModel::onCreateNewCourseClick,
        onDismissNewCourseDialog = viewModel::onDismissNewCourseDialog,
        onConfirmNewCourse = viewModel::onConfirmNewCourse,
        onNavigateTab = onNavigateTab,
        onNavigateToPlaceReview = onNavigateToPlaceReview,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    uiState: FavoritesUiState,
    snackbarHostState: SnackbarHostState,
    onFavoriteToggle: (Favorite) -> Unit,
    onCourseModeToggle: () -> Unit,
    onPlaceSelectToggle: (String) -> Unit,
    onCourseSaveClick: () -> Unit,
    onDismissExistingCoursePicker: () -> Unit,
    onExistingCourseSelected: (Course) -> Unit,
    onCreateNewCourseClick: () -> Unit,
    onDismissNewCourseDialog: () -> Unit,
    onConfirmNewCourse: (title: String, description: String) -> Unit,
    onNavigateTab: (BottomNavDestination) -> Boolean,
    onNavigateToPlaceReview: (CategoryPlace) -> Unit,
) {
    var directionsDestination by remember { mutableStateOf<NavigationDestination?>(null) }
    var contactSheetPlace by remember { mutableStateOf<CategoryPlace?>(null) }
    val courseSave = uiState.courseSave
    val coroutineScope = rememberCoroutineScope()

    directionsDestination?.let { destination ->
        DirectionsBottomSheet(
            destination = destination,
            onDismiss = { directionsDestination = null },
        )
    }

    contactSheetPlace?.let { place ->
        PlaceContactBottomSheet(
            place = place,
            onDismiss = { contactSheetPlace = null },
            onReviewClick = { onNavigateToPlaceReview(place) },
        )
    }

    if (courseSave.showExistingCoursePicker) {
        ExistingCoursePickerBottomSheet(
            courses = courseSave.myCourses,
            isLoading = courseSave.isLoadingMyCourses,
            onCourseSelected = onExistingCourseSelected,
            onDismiss = onDismissExistingCoursePicker,
            onCreateNewCourseClick = onCreateNewCourseClick,
            onNavigateToCourseTab = {
                onDismissExistingCoursePicker()
                onNavigateTab(BottomNavDestination.COURSE)
            },
        )
    }

    if (courseSave.showNewCourseDialog) {
        NewCourseDialog(
            onConfirm = onConfirmNewCourse,
            onDismiss = onDismissNewCourseDialog,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KkokDayMainBackground,
        snackbarHost = { KkokDaySnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selected = BottomNavDestination.FAVORITE,
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "즐겨찾기",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KkokDayMainTextDark,
                    modifier = Modifier.weight(1f),
                )
                if (courseSave.selectionMode) {
                    TextButton(onClick = onCourseModeToggle) {
                        Text("취소", color = KkokDayMainSubText, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onCourseSaveClick,
                        enabled = courseSave.selectedIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = KkokDayMainYellow, contentColor = KkokDayMainTextDark),
                    ) {
                        Text("담기(${courseSave.selectedIds.size})", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = onCourseModeToggle) {
                        Text("코스 만들기", color = KkokDayMainTextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.favorites.isEmpty() -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KkokDayMainYellow,
                    )
                    uiState.favorites.isEmpty() -> EmptyFavorites(modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(contentPadding = ResultsPadding) {
                        items(uiState.favorites, key = { it.docId }) { favorite ->
                            val place = favorite.toCategoryPlace()
                            CategoryPlaceRow(
                                place = place,
                                isFavorite = true,
                                onFavoriteToggle = { onFavoriteToggle(favorite) },
                                onDirectionsClick = { directionsDestination = place.toNavigationDestination() },
                                onMoreClick = { contactSheetPlace = place },
                                showDistance = false,
                                selectionMode = courseSave.selectionMode,
                                isSelected = favorite.docId in courseSave.selectedIds,
                                onSelectToggle = { onPlaceSelectToggle(favorite.docId) },
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
private fun EmptyFavorites(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.FavoriteBorder,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "즐겨찾기한 장소가 없어요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "장소 목록에서 하트를 눌러 즐겨찾기에 담아보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

/** [CategoryPlaceRow]로 그리기 위한 변환 — 즐겨찾기엔 검색 기준 좌표가 없어 distanceMeters는 쓰지 않는다(showDistance = false). */
private fun Favorite.toCategoryPlace() = CategoryPlace(
    id = kakaoPlaceId ?: docId,
    placeName = placeName,
    categoryName = categoryName,
    address = address,
    latitude = latitude,
    longitude = longitude,
    distanceMeters = 0,
    phone = phone,
    placeUrl = placeUrl,
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FavoritesScreenPreview() {
    KkokDayTheme {
        FavoritesScreenContent(
            uiState = FavoritesUiState(
                isLoading = false,
                favorites = listOf(
                    Favorite(
                        docId = "1",
                        placeName = "삼대째손두부",
                        categoryName = "음식점 > 한식",
                        address = "서울 강서구 송정로 48",
                        latitude = 37.5,
                        longitude = 126.8,
                        phone = "02-1234-5678",
                        placeUrl = "https://place.map.kakao.com/1",
                        kakaoPlaceId = "1",
                        savedAtMillis = System.currentTimeMillis(),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onFavoriteToggle = {},
            onCourseModeToggle = {},
            onPlaceSelectToggle = {},
            onCourseSaveClick = {},
            onDismissExistingCoursePicker = {},
            onExistingCourseSelected = {},
            onCreateNewCourseClick = {},
            onDismissNewCourseDialog = {},
            onConfirmNewCourse = { _, _ -> },
            onNavigateTab = { true },
            onNavigateToPlaceReview = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FavoritesScreenEmptyPreview() {
    KkokDayTheme {
        FavoritesScreenContent(
            uiState = FavoritesUiState(isLoading = false, favorites = emptyList()),
            snackbarHostState = remember { SnackbarHostState() },
            onFavoriteToggle = {},
            onCourseModeToggle = {},
            onPlaceSelectToggle = {},
            onCourseSaveClick = {},
            onDismissExistingCoursePicker = {},
            onExistingCourseSelected = {},
            onCreateNewCourseClick = {},
            onDismissNewCourseDialog = {},
            onConfirmNewCourse = { _, _ -> },
            onNavigateTab = { true },
            onNavigateToPlaceReview = {},
        )
    }
}
