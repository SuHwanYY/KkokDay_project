package com.example.kkokday.ui.favorite

import com.example.kkokday.data.place.Favorite
import com.example.kkokday.ui.course.CourseSaveUiState

data class FavoritesUiState(
    /** savedAt 내림차순(저장 최신순). */
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** 즐겨찾기 해제 직후 한 번 보여주고 소비(null로)할 성공 토스트 메시지. */
    val favoriteToastMessage: String? = null,
    /** "코스 만들기" 다중 선택 흐름 — [com.example.kkokday.ui.course.CourseSaveDelegate] 참고. */
    val courseSave: CourseSaveUiState = CourseSaveUiState(),
)
