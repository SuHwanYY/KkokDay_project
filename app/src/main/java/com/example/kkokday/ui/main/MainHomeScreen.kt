package com.example.kkokday.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.data.place.KakaoPlace
import com.example.kkokday.data.place.RecentPlace
import com.example.kkokday.directions.NavigationDestination
import com.example.kkokday.directions.toNavigationDestination
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.course.ExistingCoursePickerBottomSheet
import com.example.kkokday.ui.directions.DirectionsBottomSheet
import com.example.kkokday.ui.home.HomeViewModel
import com.example.kkokday.ui.main.components.BottomNavBar
import com.example.kkokday.ui.main.components.CategoryRow
import com.example.kkokday.ui.main.components.ConfirmActionDialog
import com.example.kkokday.ui.main.components.HomeSearchBar
import com.example.kkokday.ui.main.components.LocationRadiusRow
import com.example.kkokday.ui.main.components.MainTopAppBar
import com.example.kkokday.ui.main.components.OngoingCourseSection
import com.example.kkokday.ui.main.components.RecentPlacesSection
import com.example.kkokday.ui.main.model.BottomNavDestination
import com.example.kkokday.ui.main.model.HomeCategory
import com.example.kkokday.ui.main.model.OngoingCourseCardData
import com.example.kkokday.ui.main.model.OngoingSectionTab
import com.example.kkokday.ui.main.model.OngoingVoteCardData
import com.example.kkokday.ui.main.model.dummyHomeCategories
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayTheme
import kotlinx.coroutines.launch

/**
 * 로그인 성공 후 도착하는 실제 홈 화면 경로. [HomeViewModel]을 붙이는 얇은 래퍼다 — 화면
 * 레이아웃 자체는 [MainHomeScreen]에 있다. 로그아웃/회원탈퇴는 이제 "마이" 탭
 * ([com.example.kkokday.ui.my.MyRoute])이 전담한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeRoute(
    onSearchPlaceClick: () -> Unit,
    onCategoryClick: (HomeCategory) -> Unit,
    onFavoriteTabClick: () -> Unit,
    onCourseTabClick: () -> Unit,
    onVoteTabClick: () -> Unit,
    onMyTabClick: () -> Unit,
    onNavigateToCourseDetail: (String) -> Unit,
    onNavigateToVoteDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 위치 지정 화면/코스 상세 화면 등에서 돌아올 때마다(컴포저블이 다시 컴포지션에
    // 들어올 때마다) 최근 콕 찍은 곳과 진행 중인 코스/투표를 새로 받아온다 —
    // HomeViewModel 인스턴스는 그 사이에도 살아있지만, init에서 한 번만 불러온 값은
    // 다른 화면에서 바뀐 내용(코스 장소 추가, 새 투표 등)을 반영하지 못한다.
    LaunchedEffect(Unit) {
        viewModel.refreshRecentPlaces()
        viewModel.refreshOngoingSection()
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeErrorMessage()
    }

    uiState.pendingDeletePlace?.let { place ->
        ConfirmActionDialog(
            title = "최근 콕 찍은 곳에서 삭제할까요?",
            highlightText = place.placeName,
            confirmLabel = "삭제",
            onConfirm = viewModel::confirmDeleteRecentPlace,
            onDismiss = viewModel::dismissDeleteRecentPlace,
        )
    }

    uiState.pendingSelectPlace?.let { place ->
        ConfirmActionDialog(
            title = "이 위치로 콕 찍을까요?",
            highlightText = place.placeName,
            confirmLabel = "확인",
            onConfirm = viewModel::confirmSelectRecentPlace,
            onDismiss = viewModel::dismissSelectRecentPlace,
        )
    }

    if (uiState.pendingDeleteAll) {
        ConfirmActionDialog(
            title = "최근 콕 찍은 곳을 모두 삭제할까요?",
            highlightText = "${uiState.recentPlaces.size}개 장소",
            confirmLabel = "전체 삭제",
            onConfirm = viewModel::confirmDeleteAllRecentPlaces,
            onDismiss = viewModel::dismissDeleteAllRecentPlaces,
        )
    }

    if (uiState.showCoursePicker) {
        ExistingCoursePickerBottomSheet(
            courses = uiState.myCourses,
            isLoading = false,
            title = if (uiState.ongoingTab == OngoingSectionTab.COURSE) "표시할 코스 선택" else "투표할 코스 선택",
            onCourseSelected = viewModel::onPinnedCourseSelected,
            onDismiss = viewModel::onDismissCoursePicker,
        )
    }

    MainHomeScreen(
        modifier = modifier,
        currentLocation = uiState.currentLocation,
        recentPlaces = uiState.recentPlaces,
        onRecentPlaceClick = viewModel::onRecentPlaceClick,
        onDeleteRecentPlaceClick = viewModel::onRecentPlaceDeleteClick,
        onDeleteAllRecentPlacesClick = viewModel::onDeleteAllRecentPlacesClick,
        ongoingTab = uiState.ongoingTab,
        onOngoingTabSelected = viewModel::onOngoingTabSelected,
        isOngoingLoading = uiState.isOngoingLoading,
        pinnedCourseCard = uiState.pinnedCourseCard,
        pinnedVoteCard = uiState.pinnedVoteCard,
        onOngoingCourseCardClick = onNavigateToCourseDetail,
        onOngoingVoteCardClick = onNavigateToVoteDetail,
        onChangePinnedCourseClick = viewModel::onChangePinnedCourseClick,
        snackbarHostState = snackbarHostState,
        onMyTabClick = onMyTabClick,
        onSearchPlaceClick = onSearchPlaceClick,
        onCategoryClick = onCategoryClick,
        onFavoriteTabClick = onFavoriteTabClick,
        onCourseTabClick = onCourseTabClick,
        onVoteTabClick = onVoteTabClick,
    )
}

/**
 * 콕데이 메인(홈) 화면 레이아웃.
 *
 * [dummyHomeCategories]는 아직 실제 데이터 소스로 교체될 자리표시자다. 나머지(최근 콕
 * 찍은 곳, 진행 중인 코스/투표, 현재 위치)는 이미 실제 데이터로 연결되어 있다.
 * ViewModel/Hilt에 의존하지 않아 `@Preview`에서 바로 그려진다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    modifier: Modifier = Modifier,
    currentLocation: KakaoPlace? = null,
    recentPlaces: List<RecentPlace> = emptyList(),
    onRecentPlaceClick: (RecentPlace) -> Unit = {},
    onDeleteRecentPlaceClick: (RecentPlace) -> Unit = {},
    onDeleteAllRecentPlacesClick: () -> Unit = {},
    ongoingTab: OngoingSectionTab = OngoingSectionTab.COURSE,
    onOngoingTabSelected: (OngoingSectionTab) -> Unit = {},
    isOngoingLoading: Boolean = false,
    pinnedCourseCard: OngoingCourseCardData? = null,
    pinnedVoteCard: OngoingVoteCardData? = null,
    onOngoingCourseCardClick: (String) -> Unit = {},
    onOngoingVoteCardClick: (String) -> Unit = {},
    onChangePinnedCourseClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onMyTabClick: () -> Unit = {},
    onSearchPlaceClick: () -> Unit = {},
    onCategoryClick: (HomeCategory) -> Unit = {},
    onFavoriteTabClick: () -> Unit = {},
    onCourseTabClick: () -> Unit = {},
    onVoteTabClick: () -> Unit = {},
) {
    var selectedNav by remember { mutableStateOf(BottomNavDestination.HOME) }
    var directionsDestination by remember { mutableStateOf<NavigationDestination?>(null) }
    val coroutineScope = rememberCoroutineScope()

    directionsDestination?.let { destination ->
        DirectionsBottomSheet(
            destination = destination,
            onDismiss = { directionsDestination = null },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KkokDayMainBackground,
        // 상태바/제스처 바 인셋은 KkokDayNavHost의 insetSafeComposable이 화면 진입 시
        // 한 번만 처리한다 — 여기서 또 넣으면 이중으로 패딩된다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { MainTopAppBar() },
        snackbarHost = { KkokDaySnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selected = selectedNav,
                onSelect = { destination ->
                    // FAVORITE/MY는 이 화면을 벗어나므로(즐겨찾기 화면으로 이동 / 로그아웃) selectedNav를
                    // 바꾸지 않는다 — 바꿔두면 다시 홈으로 돌아왔을 때 엉뚱한 탭이 하이라이트된 채로 남는다.
                    when (destination) {
                        BottomNavDestination.FAVORITE -> onFavoriteTabClick()
                        BottomNavDestination.COURSE -> onCourseTabClick()
                        BottomNavDestination.MY -> onMyTabClick()
                        BottomNavDestination.VOTE -> onVoteTabClick()
                        else -> selectedNav = destination
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
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                HomeSearchBar(onClick = onSearchPlaceClick)
                Spacer(modifier = Modifier.height(16.dp))
                LocationRadiusRow(
                    currentLocation = currentLocation?.placeName ?: "위치를 지정해주세요",
                    onChangeLocationClick = onSearchPlaceClick,
                    onDirectionsClick = {
                        val place = currentLocation
                        if (place == null) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("먼저 위치를 지정해주세요.")
                            }
                        } else {
                            directionsDestination = place.toNavigationDestination()
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            CategoryRow(categories = dummyHomeCategories, onCategoryClick = onCategoryClick)
            Spacer(modifier = Modifier.height(28.dp))
            RecentPlacesSection(
                places = recentPlaces,
                onPlaceClick = onRecentPlaceClick,
                onDeleteClick = onDeleteRecentPlaceClick,
                onDeleteAllClick = onDeleteAllRecentPlacesClick,
            )
            Spacer(modifier = Modifier.height(28.dp))
            OngoingCourseSection(
                selectedTab = ongoingTab,
                onTabSelected = onOngoingTabSelected,
                isLoading = isOngoingLoading,
                courseCard = pinnedCourseCard,
                voteCard = pinnedVoteCard,
                onCourseCardClick = onOngoingCourseCardClick,
                onVoteCardClick = onOngoingVoteCardClick,
                onChangeClick = onChangePinnedCourseClick,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private val previewRecentPlaces = listOf(
    RecentPlace(
        docId = "1",
        placeName = "해운대 씨클라우드호텔",
        address = "부산 해운대구 해운대해변로 292",
        latitude = 35.1587,
        longitude = 129.1603,
        kakaoPlaceId = "1",
        selectedAtMillis = System.currentTimeMillis(),
    ),
    RecentPlace(
        docId = "2",
        placeName = "황남빵 해운대점",
        address = "부산 해운대구 구남로 21",
        latitude = 35.1596,
        longitude = 129.1610,
        kakaoPlaceId = "2",
        selectedAtMillis = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
    ),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MainHomeScreenPreview() {
    KkokDayTheme {
        MainHomeScreen(
            recentPlaces = previewRecentPlaces,
            pinnedCourseCard = OngoingCourseCardData(courseId = "1", courseTitle = "홍대 데이트 코스", placeCount = 4),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MainHomeScreenNoOngoingCoursePreview() {
    KkokDayTheme {
        MainHomeScreen(pinnedCourseCard = null, pinnedVoteCard = null)
    }
}
