package com.example.kkokday.ui.place

import com.example.kkokday.data.place.KakaoPlace

enum class PlaceSearchStatus {
    IDLE,
    LOADING,
    SUCCESS,
    EMPTY,
    ERROR,
}

data class PlaceSearchUiState(
    val query: String = "",
    val status: PlaceSearchStatus = PlaceSearchStatus.IDLE,
    val results: List<KakaoPlace> = emptyList(),
    val errorMessage: String? = null,
    val selectedPlace: KakaoPlace? = null,
    /** "확인" 버튼을 눌러 selectedPlace를 SelectedLocationRepository에 반영했는지 여부. */
    val locationConfirmed: Boolean = false,
)
