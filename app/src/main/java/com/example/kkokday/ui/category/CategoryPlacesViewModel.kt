package com.example.kkokday.ui.category

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.data.course.CourseRepository
import com.example.kkokday.data.location.SelectedLocationRepository
import com.example.kkokday.data.place.CategoryPlace
import com.example.kkokday.data.place.CategoryPlaceRepository
import com.example.kkokday.data.place.CategorySearchEvent
import com.example.kkokday.data.place.FavoriteRepository
import com.example.kkokday.data.place.favoriteDocId
import com.example.kkokday.data.place.placeDocId
import com.example.kkokday.data.review.PlaceRatingSummary
import com.example.kkokday.data.review.PlaceReviewRepository
import com.example.kkokday.navigation.KkokDayRoute
import com.example.kkokday.ui.course.CourseSaveDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CategoryPlacesVM"
private const val FOOD_CATEGORY_CODE = "FD6"
private const val PLAY_CATEGORY_CODE = "CT1"
/**
 * 실측 확인: "한강공원" 같은 산책·명소도 키워드 검색 응답의 category_group_code가 대부분
 * 비어있다(카카오 자체 데이터 한계 — CT1과 동일한 문제). 그래서 상호명 검색 시 이 카테고리만
 * category_group_code 없이 순수 키워드 검색으로 돌린다([CategoryPlacesViewModel.searchWholeCategoryByKeyword]).
 */
private const val WALK_SPOT_CATEGORY_CODE = "AT4"
private const val PUB_KEYWORD = "술집"
private const val SEARCH_DEBOUNCE_MILLIS = 300L

/**
 * FD6(음식점) 카테고리 검색만으로는 안 잡히는 "술집" 업종을 보완하기 위한 키워드 목록.
 * 카카오 카테고리 검색은 category_name에 "술집"이 포함된 곳만 잡아내는데, 실제로는
 * 호프/요리주점/포차/이자카야처럼 이름에 "술집"이 안 들어가는 업종이 많다.
 */
private val PUB_SUPPLEMENT_KEYWORDS = listOf("호프", "요리주점", "포차", "이자카야")

/**
 * [PlayCategory.SELF_PHOTO]의 브랜드 화이트리스트에 없는 신생/소규모 셀프사진관을 잡기
 * 위한 보조 키워드. "네컷" 자체는 범용 단어라 카카오 키워드 검색이 관련 없는 결과까지
 * 느슨하게 매칭해서 돌려줄 수 있어서(실측: "포토엘"로 검색해도 "포토그레이"가 나오는 등
 * 퍼지 매칭이 흔함), [isSelfPhotoBooth]로 한 번 더 걸러낸다.
 */
private const val SELF_PHOTO_BROAD_KEYWORD = "네컷"

/**
 * 카카오가 셀프사진관 프랜차이즈에 실제로 붙이는 category_name 세분류(실측 확인:
 * "문화,예술 > 사진 > 사진관,포토스튜디오 > 즉석사진 > {브랜드명}"). 여권사진/증명사진
 * 찍는 일반 사진관은 이 세그먼트 없이 "…> 사진관,포토스튜디오"에서 끊긴다.
 */
private const val SELF_PHOTO_CATEGORY_TAG = "즉석사진"

/**
 * 카카오 키워드 검색이 [matchedKeyword]와 무관한 결과까지 느슨하게 섞어 보낼 수 있어서,
 * category_name에 [SELF_PHOTO_CATEGORY_TAG]가 붙어있거나(셀프사진관으로 확인된 것) 상호명에
 * 검색어가 그대로 포함된 경우(셀픽스처럼 태그가 안 붙은 브랜드 보완)만 셀프사진관으로 인정한다.
 */
private fun CategoryPlace.isSelfPhotoBooth(matchedKeyword: String): Boolean =
    categoryName.contains(SELF_PHOTO_CATEGORY_TAG) || placeName.contains(matchedKeyword)

/** category_group_code → 화면 제목. route에 한글/특수문자를 안 태우려고 코드로만 진입시키고 여기서 구한다. */
private val CATEGORY_LABELS = mapOf(
    "FD6" to "음식점",
    "CE7" to "카페",
    "CS2" to "편의점",
    "CT1" to "놀거리",
    "AT4" to "산책·명소",
)

@HiltViewModel
class CategoryPlacesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryPlaceRepository: CategoryPlaceRepository,
    private val selectedLocationRepository: SelectedLocationRepository,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
    private val placeReviewRepository: PlaceReviewRepository,
) : ViewModel() {

    private val categoryGroupCode: String =
        checkNotNull(savedStateHandle[KkokDayRoute.CATEGORY_PLACES_CODE_ARG])

    private val courseSaveDelegate = CourseSaveDelegate(
        courseRepository = courseRepository,
        authRepository = authRepository,
        scope = viewModelScope,
        getState = { _uiState.value.courseSave },
        updateState = { transform -> _uiState.update { it.copy(courseSave = transform(it.courseSave)) } },
    )

    private var latitude: Double? = null
    private var longitude: Double? = null
    private var searchJob: Job? = null
    private var searchQueryDebounceJob: Job? = null

    /**
     * placeDocId별 평점 요약 캐시 — null 값도 "조회했지만 리뷰 없음"으로 저장해, 이미 확인한
     * 장소를 검색 결과가 갱신될 때마다 다시 배치 조회하지 않는다("반경 내 전체 결과를
     * 미리 다 조회하지 않는다"는 원칙과 같은 이유로, 이미 아는 것도 또 묻지 않는다).
     */
    private val ratingCache = mutableMapOf<String, PlaceRatingSummary?>()

    /**
     * 화면에 새로 로드된 [places]만 배치 조회([PlaceReviewRepository.getRatingSummaries])해
     * avgRating/reviewCount를 채운다. 정렬 모드와 무관하게 항상 호출한다 — 행의 평점 배지
     * 표시에 필요하기 때문. 실제로 그 값을 정렬 기준으로 쓸지는
     * [CategoryPlacesUiState.displayedPlaces]가 [CategoryPlacesUiState.sortMode]를 보고
     * 따로 결정한다(여기서 순서를 바꾸지 않는다 — 거리순을 선택했는데 평점 때문에 몰래
     * 재정렬되는 문제를 막기 위해). 이미 캐시에 있는 장소는 다시 조회하지 않는다.
     */
    private suspend fun enrichWithRatings(places: List<CategoryPlace>): List<CategoryPlace> {
        val unresolvedIds = places.map { it.placeDocId() }.distinct().filterNot { it in ratingCache }
        if (unresolvedIds.isNotEmpty()) {
            placeReviewRepository.getRatingSummaries(unresolvedIds)
                .onSuccess { summaries ->
                    unresolvedIds.forEach { id -> ratingCache[id] = summaries[id] }
                }
                .onFailure { error -> Log.w(TAG, "평점 배치 조회 실패", error) }
        }
        return places.map { place ->
            val summary = ratingCache[place.placeDocId()]
            place.copy(avgRating = summary?.avgRating, reviewCount = summary?.reviewCount)
        }
    }

    fun onSortModeSelected(mode: PlaceSortMode) {
        _uiState.update { it.copy(sortMode = mode) }
    }

    private val _uiState = MutableStateFlow(
        CategoryPlacesUiState(
            categoryLabel = CATEGORY_LABELS[categoryGroupCode].orEmpty(),
            showTabs = categoryGroupCode == FOOD_CATEGORY_CODE,
            showSubCategoryChips = categoryGroupCode == PLAY_CATEGORY_CODE,
            subCategories = if (categoryGroupCode == PLAY_CATEGORY_CODE) {
                PlayCategory.values().toList()
            } else {
                emptyList()
            },
        ),
    )
    val uiState: StateFlow<CategoryPlacesUiState> = _uiState.asStateFlow()

    init {
        val place = selectedLocationRepository.selectedLocation.value
        if (place == null) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "먼저 위치를 지정해주세요.")
            }
        } else {
            latitude = place.latitude
            longitude = place.longitude
            search()
        }
        loadFavoriteIds()
    }

    /**
     * 리스트에 하트(즐겨찾기 여부)를 표시하기 위해, 장소마다 개별 조회하는 대신
     * 진입 시점에 이 유저의 즐겨찾기 문서 ID를 한 번에 받아 [CategoryPlacesUiState.favoriteIds]에 둔다.
     */
    private fun loadFavoriteIds() {
        viewModelScope.launch {
            val uid = authRepository.currentUserUid() ?: return@launch
            favoriteRepository.getFavoriteIds(uid)
                .onSuccess { ids -> _uiState.update { it.copy(favoriteIds = ids) } }
                .onFailure { error -> Log.w(TAG, "즐겨찾기 목록 조회 실패", error) }
        }
    }

    /**
     * 즉시 추가/삭제되는 낙관적 토글 — 서버 응답을 기다리지 않고 먼저 하트를 바꾼 뒤,
     * 실패하면 되돌리고 스낵바로 알린다.
     */
    fun onFavoriteToggleClick(place: CategoryPlace) {
        val uid = authRepository.currentUserUid() ?: return
        val docId = place.favoriteDocId()
        val wasFavorite = docId in _uiState.value.favoriteIds

        _uiState.update { state ->
            state.copy(
                favoriteIds = if (wasFavorite) state.favoriteIds - docId else state.favoriteIds + docId,
                favoriteToastMessage = if (wasFavorite) "즐겨찾기에서 삭제했어요" else "즐겨찾기에 추가했어요",
            )
        }

        viewModelScope.launch {
            val result = if (wasFavorite) {
                favoriteRepository.removeFavorite(uid, docId)
            } else {
                favoriteRepository.addFavorite(uid, place)
            }
            result.onFailure { error ->
                Log.w(TAG, "즐겨찾기 토글 실패", error)
                _uiState.update { state ->
                    state.copy(
                        favoriteIds = if (wasFavorite) state.favoriteIds + docId else state.favoriteIds - docId,
                        errorMessage = "즐겨찾기 처리에 실패했어요. 다시 시도해주세요.",
                    )
                }
            }
        }
    }

    private fun search() {
        val lat = latitude ?: return
        val lon = longitude ?: return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    primaryPlaces = emptyList(),
                    pubPlaces = emptyList(),
                    playPlacesBySubCategory = emptyMap(),
                    errorMessage = null,
                )
            }
            categoryPlaceRepository.searchCategory(
                categoryGroupCode = categoryGroupCode,
                latitude = lat,
                longitude = lon,
            ).collect { event ->
                when (event) {
                    is CategorySearchEvent.Progress -> {
                        // 평점 채우기 후 partition/groupBy로 나눠도 각 버킷 내부의 상대 순서는
                        // 그대로 유지되므로, 전체를 한 번만 채우면 된다(버킷별로 다시 조회할
                        // 필요 없음). 실제 정렬은 UiState.displayedPlaces가 담당한다.
                        val enriched = enrichWithRatings(event.places)
                        _uiState.update { state ->
                            when {
                                state.showTabs -> {
                                    // 술집도 FD6 결과 안에 섞여 있으므로, 같은 크롤 한 번으로
                                    // 밥집/술집 두 탭을 동시에 채운다(API를 두 번 타지 않는다).
                                    val (pub, rice) = enriched.partition {
                                        it.categoryName.contains(PUB_KEYWORD)
                                    }
                                    state.copy(primaryPlaces = rice, pubPlaces = pub)
                                }
                                state.showSubCategoryChips -> {
                                    val bucketed = enriched.groupBy { classifyPlayCategory(it) }
                                    state.copy(
                                        playPlacesBySubCategory = PlayCategory.values().associateWith {
                                            bucketed[it].orEmpty()
                                        },
                                    )
                                }
                                else -> state.copy(primaryPlaces = enriched)
                            }
                        }
                    }
                    is CategorySearchEvent.Failed -> {
                        Log.w(TAG, "카테고리 검색 실패", event.error)
                        _uiState.update {
                            it.copy(errorMessage = "주변 장소를 불러오지 못했어요. 다시 시도해주세요.")
                        }
                    }
                }
            }

            when (categoryGroupCode) {
                FOOD_CATEGORY_CODE -> augmentPubResults(lat, lon)
                PLAY_CATEGORY_CODE -> {
                    augmentPlayResults(lat, lon)
                    // 브랜드 화이트리스트 병합이 끝난 뒤에 "네컷" 보조 검색을 더해야 같은
                    // 버킷(SELF_PHOTO)에 대한 상태 갱신이 꼬이지 않는다.
                    augmentSelfPhotoBroadSearch(lat, lon)
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /** CT1 카테고리 크롤 결과의 category_name으로 세부 업종을 분류한다. 매칭되는 게 없으면 기타. */
    private fun classifyPlayCategory(place: CategoryPlace): PlayCategory =
        PlayCategory.values().firstOrNull { category ->
            category != PlayCategory.ETC &&
                category.classificationKeywords.any { place.categoryName.contains(it) }
        } ?: PlayCategory.ETC

    /**
     * FD6 카테고리 검색의 category_name 필터만으로는 술집 결과가 부실해서, 흔한 술집
     * 업종 키워드로 추가 검색해 병합한다. place id로 중복 제거 후 [enrichWithRatings]로
     * 평점만 채우고, 실제 정렬은 [CategoryPlacesUiState.displayedPlaces]가 담당한다.
     */
    private suspend fun augmentPubResults(lat: Double, lon: Double) = coroutineScope {
        val supplements = PUB_SUPPLEMENT_KEYWORDS.map { keyword ->
            async {
                categoryPlaceRepository.searchKeywordNearby(
                    query = keyword,
                    latitude = lat,
                    longitude = lon,
                ).onFailure { error ->
                    Log.w(TAG, "술집 보완검색 실패: query=$keyword", error)
                }.getOrDefault(emptyList())
            }
        }.awaitAll()

        val supplementCount = supplements.sumOf { it.size }
        val merged = (_uiState.value.pubPlaces + supplements.flatten()).distinctBy { it.id }
        val enriched = enrichWithRatings(merged)
        _uiState.update { it.copy(pubPlaces = enriched) }
        Log.d(
            TAG,
            "술집 보완검색 완료: 키워드검색 원본 합계=$supplementCount 병합 후 최종=" +
                "${_uiState.value.pubPlaces.size}",
        )
    }

    /**
     * CT1 카테고리 크롤만으로는 세부 업종이 잘 안 잡힌다(예: 보드게임카페는 카카오가
     * CE7로 분류해놓기도 함). 업종별 키워드 검색으로 보완해서 각 [PlayCategory] 버킷에
     * 병합한다. place id로 중복 제거 후 [enrichWithRatings]로 평점만 채운다.
     *
     * [PlayCategory.SELF_PHOTO]만 예외적으로 한 겹 더 거른다 — 카카오 키워드 검색은
     * 브랜드명을 검색해도 무관한 결과를 느슨하게 섞어 보낼 수 있어서(실측 확인),
     * [isSelfPhotoBooth]로 실제 셀프사진관이 맞는지 확인한 것만 남긴다.
     */
    private suspend fun augmentPlayResults(lat: Double, lon: Double) = coroutineScope {
        val searchTasks = PlayCategory.values()
            .filter { it != PlayCategory.ETC }
            .flatMap { category -> category.searchKeywords.map { keyword -> category to keyword } }

        val supplements = searchTasks.map { (category, keyword) ->
            async {
                val results = categoryPlaceRepository.searchKeywordNearby(
                    query = keyword,
                    latitude = lat,
                    longitude = lon,
                ).onFailure { error ->
                    Log.w(TAG, "놀거리 보완검색 실패: category=${category.label} query=$keyword", error)
                }.getOrDefault(emptyList())

                val filtered = if (category == PlayCategory.SELF_PHOTO) {
                    results.filter { it.isSelfPhotoBooth(keyword) }
                } else {
                    results
                }
                category to filtered
            }
        }.awaitAll()

        val supplementsByCategory = supplements
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, lists) -> lists.flatten() }

        val mergedByCategory = PlayCategory.values().associateWith { category ->
            val existing = _uiState.value.playPlacesBySubCategory[category].orEmpty()
            val supplement = supplementsByCategory[category].orEmpty()
            (existing + supplement).distinctBy { it.id }
        }
        val enrichedByCategory = mergedByCategory.mapValues { (_, list) -> enrichWithRatings(list) }
        _uiState.update { it.copy(playPlacesBySubCategory = enrichedByCategory) }

        val finalState = _uiState.value.playPlacesBySubCategory
        Log.d(
            TAG,
            "놀거리 보완검색 완료: " +
                PlayCategory.values().joinToString { "${it.label}=${finalState[it].orEmpty().size}" },
        )
    }

    /**
     * [PlayCategory.SELF_PHOTO] 브랜드 화이트리스트에 없는 신생/소규모 셀프사진관을 잡기
     * 위해 "네컷"으로 한 번 더 검색한다. 범용 단어라 무관한 결과가 섞일 수 있어
     * [isSelfPhotoBooth]로 실제 셀프사진관(카카오 category_name의 "즉석사진" 태그 또는
     * 상호명에 "네컷" 포함)만 남긴다.
     */
    private suspend fun augmentSelfPhotoBroadSearch(lat: Double, lon: Double) {
        val results = categoryPlaceRepository.searchKeywordNearby(
            query = SELF_PHOTO_BROAD_KEYWORD,
            latitude = lat,
            longitude = lon,
        ).onFailure { error ->
            Log.w(TAG, "셀프사진관 보조검색 실패: query=$SELF_PHOTO_BROAD_KEYWORD", error)
        }.getOrDefault(emptyList())

        val filtered = results.filter { it.isSelfPhotoBooth(SELF_PHOTO_BROAD_KEYWORD) }

        val existing = _uiState.value.playPlacesBySubCategory[PlayCategory.SELF_PHOTO].orEmpty()
        val merged = (existing + filtered).distinctBy { it.id }
        val enriched = enrichWithRatings(merged)
        _uiState.update { it.copy(playPlacesBySubCategory = it.playPlacesBySubCategory + (PlayCategory.SELF_PHOTO to enriched)) }

        Log.d(
            TAG,
            "셀프사진관 보조검색 완료: query=$SELF_PHOTO_BROAD_KEYWORD 원본=${results.size} " +
                "필터 후=${filtered.size} 병합 후 최종=" +
                "${_uiState.value.playPlacesBySubCategory[PlayCategory.SELF_PHOTO].orEmpty().size}",
        )
    }

    fun onTabSelected(tab: RestaurantTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * CT1(놀거리)은 검색 모드에서 지금 선택된 탭 범위 안에서만 검색하므로([searchPlaySubCategoryByKeyword]),
     * 탭을 바꾸면 검색어가 있을 때만 새로 검색한다. FD6 밥집/술집은 한 크롤 결과를 나눠 쓰는
     * 구조라(검색 모드에서도 동일) 탭을 바꿔도 재검색이 필요 없다.
     */
    fun onSubCategorySelected(category: PlayCategory) {
        _uiState.update { it.copy(selectedSubCategory = category) }
        val query = _uiState.value.searchQuery.trim()
        if (query.isNotBlank()) {
            searchByKeyword(query)
        }
    }

    /** 검색창 입력마다 호출 — [SEARCH_DEBOUNCE_MILLIS] 동안 추가 입력이 없으면 그때 검색/브라우즈를 실행한다. */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryDebounceJob?.cancel()
        searchQueryDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            runSearchOrBrowse()
        }
    }

    /** 검색창의 X 버튼 — 디바운스를 기다리지 않고 바로 검색어를 지우고 기존 카테고리 브라우즈로 돌아간다. */
    fun onSearchQueryClear() {
        searchQueryDebounceJob?.cancel()
        _uiState.update { it.copy(searchQuery = "") }
        runSearchOrBrowse()
    }

    private fun runSearchOrBrowse() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) search() else searchByKeyword(query)
    }

    /**
     * 카테고리 내 상호명 검색. 화면에 이미 로드된 목록을 클라이언트에서 필터링하지 않고
     * 매번 카카오 키워드 검색 API를 새로 호출한다 — 반경 안에 있지만 아직 안 불러온
     * 페이지의 상호를 놓치지 않기 위해서다.
     */
    private fun searchByKeyword(query: String) {
        val lat = latitude ?: return
        val lon = longitude ?: return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (categoryGroupCode) {
                PLAY_CATEGORY_CODE -> searchPlaySubCategoryByKeyword(query, lat, lon)
                else -> searchWholeCategoryByKeyword(query, lat, lon)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * FD6/CE7/CS2/AT4 상호명 검색. FD6/CE7/CS2는 키워드 검색에도 category_group_code가
     * 안정적으로 붙어있어(실측 확인) 서버 단에서 바로 좁힌다. AT4는 대부분 비어있어
     * 이 값을 넘기면 결과가 거의 사라지므로 순수 키워드 검색만 쓴다. FD6는 기존 밥집/술집
     * 분리(categoryName에 "술집" 포함 여부) 후처리를 그대로 유지한다.
     */
    private suspend fun searchWholeCategoryByKeyword(query: String, lat: Double, lon: Double) {
        val scopedCategoryGroupCode = categoryGroupCode.takeIf { it != WALK_SPOT_CATEGORY_CODE }
        categoryPlaceRepository.searchKeywordInCategory(
            query = query,
            categoryGroupCode = scopedCategoryGroupCode,
            latitude = lat,
            longitude = lon,
        ).collect { event ->
            when (event) {
                is CategorySearchEvent.Progress -> {
                    val enriched = enrichWithRatings(event.places)
                    _uiState.update { state ->
                        if (state.showTabs) {
                            val (pub, rice) = enriched.partition { it.categoryName.contains(PUB_KEYWORD) }
                            state.copy(primaryPlaces = rice, pubPlaces = pub)
                        } else {
                            state.copy(primaryPlaces = enriched)
                        }
                    }
                }
                is CategorySearchEvent.Failed -> {
                    Log.w(TAG, "상호명 검색 실패", event.error)
                    _uiState.update { it.copy(errorMessage = "검색 중 오류가 발생했어요. 다시 시도해주세요.") }
                }
            }
        }
    }

    /**
     * CT1(놀거리) 상호명 검색. category_group_code=CT1은 영화관 말고는 거의 안 붙어있어서
     * (실측 확인) 서버 필터로 못 쓰고, 카테고리 전체가 아니라 지금 선택된 [PlayCategory]
     * 범위로만 순수 키워드 검색 후 그 탭에 이미 쓰는 분류 필터를 그대로 적용한다 —
     * 셀프사진관은 [isSelfPhotoBooth], 나머지는 [classifyPlayCategory]가 그 탭으로
     * 분류하는 것만 남긴다.
     */
    private suspend fun searchPlaySubCategoryByKeyword(query: String, lat: Double, lon: Double) {
        val selected = _uiState.value.selectedSubCategory
        categoryPlaceRepository.searchKeywordInCategory(
            query = query,
            categoryGroupCode = null,
            latitude = lat,
            longitude = lon,
        ).collect { event ->
            when (event) {
                is CategorySearchEvent.Progress -> {
                    val filtered = if (selected == PlayCategory.SELF_PHOTO) {
                        event.places.filter { it.isSelfPhotoBooth(query) }
                    } else {
                        event.places.filter { classifyPlayCategory(it) == selected }
                    }
                    val enriched = enrichWithRatings(filtered)
                    _uiState.update { state ->
                        state.copy(playPlacesBySubCategory = mapOf(selected to enriched))
                    }
                }
                is CategorySearchEvent.Failed -> {
                    Log.w(TAG, "상호명 검색 실패", event.error)
                    _uiState.update { it.copy(errorMessage = "검색 중 오류가 발생했어요. 다시 시도해주세요.") }
                }
            }
        }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeFavoriteToastMessage() {
        _uiState.update { it.copy(favoriteToastMessage = null) }
    }

    // "코스 만들기" 다중 선택 흐름 — 실제 로직은 [CourseSaveDelegate]가 갖고 있고, 여기서는
    // 지금 화면에 표시 중인 장소(displayedPlaces) 기준으로 선택된 것만 CoursePlace로 변환해 넘긴다.
    fun onCourseModeToggle() = courseSaveDelegate.onModeToggle()
    fun onPlaceSelectToggle(id: String) = courseSaveDelegate.onSelectToggle(id)
    fun onCourseSaveClick() = courseSaveDelegate.onSaveClick()
    fun onDismissExistingCoursePicker() = courseSaveDelegate.onDismissExistingCoursePicker()
    fun consumeCourseToastMessage() = courseSaveDelegate.consumeToastMessage()
    fun onCreateNewCourseClick() = courseSaveDelegate.onCreateNewCourseClick()
    fun onDismissNewCourseDialog() = courseSaveDelegate.onDismissNewCourseDialog()

    fun onExistingCourseSelected(course: Course) =
        courseSaveDelegate.onExistingCourseSelected(course, selectedCoursePlaces())

    fun onConfirmNewCourse(title: String, description: String) =
        courseSaveDelegate.onConfirmNewCourse(title, description, selectedCoursePlaces())

    private fun selectedCoursePlaces(): List<CoursePlace> {
        val selectedIds = _uiState.value.courseSave.selectedIds
        return _uiState.value.displayedPlaces
            .filter { it.id in selectedIds }
            .map { it.toCoursePlace() }
    }

    private fun CategoryPlace.toCoursePlace() = CoursePlace(
        placeName = placeName,
        address = address,
        latitude = latitude,
        longitude = longitude,
        kakaoPlaceId = id.takeIf { it.isNotBlank() },
        category = categoryName,
    )
}
