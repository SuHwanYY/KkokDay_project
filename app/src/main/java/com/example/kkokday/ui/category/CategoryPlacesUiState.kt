package com.example.kkokday.ui.category

import com.example.kkokday.data.place.CategoryPlace
import com.example.kkokday.data.place.sortedByPopularity
import com.example.kkokday.ui.course.CourseSaveUiState

/** 음식점(FD6) 화면 전용 탭. 다른 카테고리는 탭 없이 [CategoryPlacesUiState.primaryPlaces]만 쓴다. */
enum class RestaurantTab {
    RICE,
    PUB,
}

/**
 * 목록 정렬 기준. avgRating/reviewCount 배치 조회는 정렬 모드와 무관하게 항상 일어난다
 * (행의 평점 배지 표시에 필요하므로) — 이 값은 오직 [CategoryPlacesUiState.displayedPlaces]가
 * 최종적으로 어떤 기준으로 정렬해서 보여줄지만 결정한다.
 */
enum class PlaceSortMode {
    DISTANCE,
    POPULARITY,
}

data class CategoryPlacesUiState(
    val categoryLabel: String = "",
    /** FD6(음식점)일 때만 true — 밥집/술집 탭을 보여준다. */
    val showTabs: Boolean = false,
    val selectedTab: RestaurantTab = RestaurantTab.RICE,
    /** CT1(놀거리)일 때만 true — 세부 업종 칩을 보여준다. */
    val showSubCategoryChips: Boolean = false,
    val subCategories: List<PlayCategory> = emptyList(),
    val selectedSubCategory: PlayCategory = PlayCategory.MOVIE,
    /** 탭/칩이 없는 카테고리의 전체 목록, 또는 FD6의 "밥집" 목록. */
    val primaryPlaces: List<CategoryPlace> = emptyList(),
    /** FD6의 "술집" 목록. 탭이 없는 카테고리에서는 항상 비어 있다. */
    val pubPlaces: List<CategoryPlace> = emptyList(),
    /** CT1의 세부 업종별 목록. CT1이 아닌 카테고리에서는 항상 비어 있다. */
    val playPlacesBySubCategory: Map<PlayCategory, List<CategoryPlace>> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** 현재 로그인 유저의 즐겨찾기 문서 ID 집합. [com.example.kkokday.data.place.favoriteDocId]로 대조한다. */
    val favoriteIds: Set<String> = emptySet(),
    /** 즐겨찾기 추가/삭제 직후 한 번 보여주고 소비(null로)할 성공 토스트 메시지. */
    val favoriteToastMessage: String? = null,
    /** 카테고리 내 상호명 검색어. 비어있으면 기존 카테고리 브라우즈 결과를 보여준다. */
    val searchQuery: String = "",
    /** "코스 만들기" 다중 선택 흐름 — [com.example.kkokday.ui.course.CourseSaveDelegate] 참고. */
    val courseSave: CourseSaveUiState = CourseSaveUiState(),
    /** 기본은 거리순 — 기존 카테고리 브라우즈 동작(카카오 API가 이미 거리순으로 내려줌)을 그대로 유지한다. */
    val sortMode: PlaceSortMode = PlaceSortMode.DISTANCE,
) {
    val displayedPlaces: List<CategoryPlace>
        get() {
            val base = when {
                showTabs && selectedTab == RestaurantTab.PUB -> pubPlaces
                showTabs -> primaryPlaces
                showSubCategoryChips -> playPlacesBySubCategory[selectedSubCategory].orEmpty()
                else -> primaryPlaces
            }
            return when (sortMode) {
                PlaceSortMode.DISTANCE -> base.sortedBy { it.distanceMeters }
                PlaceSortMode.POPULARITY -> base.sortedByPopularity()
            }
        }
}
