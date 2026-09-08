package com.example.kkokday.ui.category

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.place.CategoryPlace
import com.example.kkokday.data.place.favoriteDocId
import com.example.kkokday.directions.NavigationDestination
import com.example.kkokday.directions.toNavigationDestination
import com.example.kkokday.ui.common.KkokDaySnackbarHost
import com.example.kkokday.ui.common.KkokDaySnackbarTone
import com.example.kkokday.ui.common.showKkokDaySnackbar
import com.example.kkokday.ui.course.ExistingCoursePickerBottomSheet
import com.example.kkokday.ui.directions.DirectionsBottomSheet
import com.example.kkokday.ui.theme.KkokDayMainBackground
import com.example.kkokday.ui.theme.KkokDayMainCardBorder
import com.example.kkokday.ui.theme.KkokDayMainSubText
import com.example.kkokday.ui.theme.KkokDayMainTextDark
import com.example.kkokday.ui.theme.KkokDayMainYellow
import com.example.kkokday.ui.theme.KkokDayTheme

@Composable
fun CategoryPlacesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCourses: () -> Unit,
    onNavigateToPlaceReview: (CategoryPlace) -> Unit,
    viewModel: CategoryPlacesViewModel = hiltViewModel(),
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

    CategoryPlacesScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onTabSelected = viewModel::onTabSelected,
        onSubCategorySelected = viewModel::onSubCategorySelected,
        onFavoriteToggle = viewModel::onFavoriteToggleClick,
        onSortModeSelected = viewModel::onSortModeSelected,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchQueryClear = viewModel::onSearchQueryClear,
        onCourseModeToggle = viewModel::onCourseModeToggle,
        onPlaceSelectToggle = viewModel::onPlaceSelectToggle,
        onCourseSaveClick = viewModel::onCourseSaveClick,
        onDismissExistingCoursePicker = viewModel::onDismissExistingCoursePicker,
        onExistingCourseSelected = viewModel::onExistingCourseSelected,
        onNavigateBack = onNavigateBack,
        onNavigateToCourses = onNavigateToCourses,
        onNavigateToPlaceReview = onNavigateToPlaceReview,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPlacesScreenContent(
    uiState: CategoryPlacesUiState,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (RestaurantTab) -> Unit,
    onSubCategorySelected: (PlayCategory) -> Unit,
    onFavoriteToggle: (CategoryPlace) -> Unit,
    onSortModeSelected: (PlaceSortMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchQueryClear: () -> Unit,
    onCourseModeToggle: () -> Unit,
    onPlaceSelectToggle: (String) -> Unit,
    onCourseSaveClick: () -> Unit,
    onDismissExistingCoursePicker: () -> Unit,
    onExistingCourseSelected: (Course) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCourses: () -> Unit,
    onNavigateToPlaceReview: (CategoryPlace) -> Unit,
) {
    var directionsDestination by remember { mutableStateOf<NavigationDestination?>(null) }
    var contactSheetPlace by remember { mutableStateOf<CategoryPlace?>(null) }
    val courseSave = uiState.courseSave
    val listState = rememberLazyListState()

    // 정렬 기준이 바뀌면(거리순 ↔ 인기순) 이전 스크롤 위치 그대로 두지 않고 맨 위부터
    // 다시 보여준다 — 새 순서를 처음부터 확인할 수 있게.
    LaunchedEffect(uiState.sortMode) {
        listState.animateScrollToItem(0)
    }

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
            onNavigateToCourseTab = {
                onDismissExistingCoursePicker()
                onNavigateToCourses()
            },
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = KkokDayMainTextDark,
                    )
                }
                Text(
                    text = uiState.categoryLabel,
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

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                placeholder = { Text("상호명으로 검색") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = KkokDayMainSubText)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = onSearchQueryClear) {
                            Icon(Icons.Filled.Close, contentDescription = "검색어 지우기", tint = KkokDayMainSubText)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
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

            if (uiState.showSubCategoryChips) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.subCategories, key = { it.name }) { category ->
                        val selected = category == uiState.selectedSubCategory
                        FilterChip(
                            selected = selected,
                            onClick = { onSubCategorySelected(category) },
                            label = {
                                Text(
                                    text = category.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KkokDayMainYellow,
                                selectedLabelColor = Color.White,
                                labelColor = KkokDayMainTextDark,
                            ),
                        )
                    }
                }
            }

            if (uiState.showTabs) {
                SecondaryTabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = KkokDayMainBackground,
                    contentColor = KkokDayMainYellow,
                ) {
                    Tab(
                        selected = uiState.selectedTab == RestaurantTab.RICE,
                        onClick = { onTabSelected(RestaurantTab.RICE) },
                        text = {
                            Text(
                                "밥집",
                                fontWeight = if (uiState.selectedTab == RestaurantTab.RICE) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                            )
                        },
                    )
                    Tab(
                        selected = uiState.selectedTab == RestaurantTab.PUB,
                        onClick = { onTabSelected(RestaurantTab.PUB) },
                        text = {
                            Text(
                                "술집",
                                fontWeight = if (uiState.selectedTab == RestaurantTab.PUB) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                            )
                        },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                SortModeChip(
                    label = "거리순",
                    selected = uiState.sortMode == PlaceSortMode.DISTANCE,
                    onClick = { onSortModeSelected(PlaceSortMode.DISTANCE) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                SortModeChip(
                    label = "인기순",
                    selected = uiState.sortMode == PlaceSortMode.POPULARITY,
                    onClick = { onSortModeSelected(PlaceSortMode.POPULARITY) },
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // displayedPlaces는 매번 새로 정렬하는 커스텀 getter라, uiState를 그냥
                // 읽으면 검색어 등 정렬과 무관한 필드가 바뀔 때도 매번 재정렬된다.
                // 실제 정렬 결과에 영향을 주는 필드만 key로 넘겨 불필요한 재계산을 막는다.
                val places = remember(
                    uiState.showTabs,
                    uiState.selectedTab,
                    uiState.showSubCategoryChips,
                    uiState.selectedSubCategory,
                    uiState.primaryPlaces,
                    uiState.pubPlaces,
                    uiState.playPlacesBySubCategory,
                    uiState.sortMode,
                ) { uiState.displayedPlaces }
                when {
                    uiState.isLoading && places.isEmpty() -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = KkokDayMainYellow,
                    )
                    !uiState.isLoading && places.isEmpty() -> EmptyResults(
                        isErrorState = uiState.errorMessage != null,
                        isSearchMode = uiState.searchQuery.isNotBlank(),
                    )
                    else -> LazyColumn(state = listState, contentPadding = ResultsPadding) {
                        items(places, key = { it.id }) { place ->
                            CategoryPlaceRow(
                                place = place,
                                isFavorite = place.favoriteDocId() in uiState.favoriteIds,
                                onFavoriteToggle = { onFavoriteToggle(place) },
                                onDirectionsClick = {
                                    directionsDestination = place.toNavigationDestination()
                                },
                                onMoreClick = { contactSheetPlace = place },
                                selectionMode = courseSave.selectionMode,
                                isSelected = place.id in courseSave.selectedIds,
                                onSelectToggle = { onPlaceSelectToggle(place.id) },
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (uiState.isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = KkokDayMainYellow,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val ResultsPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

@Composable
private fun SortModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun BoxScope.EmptyResults(isErrorState: Boolean, isSearchMode: Boolean = false) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            tint = KkokDayMainYellow,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = when {
                isErrorState -> "장소를 불러오지 못했어요"
                isSearchMode -> "검색 결과가 없어요"
                else -> "근처에 결과가 없어요"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = KkokDayMainTextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearchMode) "다른 검색어로 다시 시도해보세요" else "근처 20km 안에 해당하는 곳을 찾지 못했어요",
            style = MaterialTheme.typography.bodyMedium,
            color = KkokDayMainSubText,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CategoryPlacesScreenPreview() {
    KkokDayTheme {
        CategoryPlacesScreenContent(
            uiState = CategoryPlacesUiState(
                categoryLabel = "음식점",
                showTabs = true,
                isLoading = false,
                primaryPlaces = listOf(
                    CategoryPlace(
                        id = "1",
                        placeName = "삼대째손두부",
                        categoryName = "음식점 > 한식",
                        address = "서울 강서구 송정로 48",
                        latitude = 37.5,
                        longitude = 126.8,
                        distanceMeters = 250,
                        phone = "02-1234-5678",
                        placeUrl = "https://place.map.kakao.com/1",
                    ),
                    CategoryPlace(
                        id = "2",
                        placeName = "송정역 김밥천국",
                        categoryName = "음식점 > 분식",
                        address = "서울 강서구 공항대로",
                        latitude = 37.5,
                        longitude = 126.8,
                        distanceMeters = 1450,
                        placeUrl = "https://place.map.kakao.com/2",
                    ),
                ),
                favoriteIds = setOf("1"),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onTabSelected = {},
            onSubCategorySelected = {},
            onFavoriteToggle = {},
            onSortModeSelected = {},
            onSearchQueryChange = {},
            onSearchQueryClear = {},
            onCourseModeToggle = {},
            onPlaceSelectToggle = {},
            onCourseSaveClick = {},
            onDismissExistingCoursePicker = {},
            onExistingCourseSelected = {},
            onNavigateBack = {},
            onNavigateToCourses = {},
            onNavigateToPlaceReview = {},
        )
    }
}
