package com.example.kkokday.ui.place

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.location.SelectedLocationRepository
import com.example.kkokday.data.place.KakaoPlace
import com.example.kkokday.data.place.PlaceSearchRepository
import com.example.kkokday.data.place.RecentPlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PlaceSearchViewModel"

@HiltViewModel
class PlaceSearchViewModel @Inject constructor(
    private val placeSearchRepository: PlaceSearchRepository,
    private val selectedLocationRepository: SelectedLocationRepository,
    private val recentPlaceRepository: RecentPlaceRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceSearchUiState())
    val uiState: StateFlow<PlaceSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun onSearchClick() {
        val query = _uiState.value.query.trim()
        if (query.isBlank() || _uiState.value.status == PlaceSearchStatus.LOADING) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(status = PlaceSearchStatus.LOADING, errorMessage = null, selectedPlace = null)
            }
            placeSearchRepository.searchPlaces(query)
                .onSuccess { places ->
                    _uiState.update {
                        it.copy(
                            status = if (places.isEmpty()) PlaceSearchStatus.EMPTY else PlaceSearchStatus.SUCCESS,
                            results = places,
                        )
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "장소 검색 실패", error)
                    _uiState.update {
                        it.copy(
                            status = PlaceSearchStatus.ERROR,
                            results = emptyList(),
                            errorMessage = "검색 중 오류가 발생했어요. 네트워크 상태를 확인하고 다시 시도해주세요.",
                        )
                    }
                }
        }
    }

    fun onPlaceSelected(place: KakaoPlace) {
        Log.d(TAG, "선택한 장소: ${place.placeName} (x=${place.longitude}, y=${place.latitude})")
        _uiState.update { it.copy(selectedPlace = place) }
    }

    fun onConfirmClick() {
        val place = _uiState.value.selectedPlace ?: return
        selectedLocationRepository.setSelectedLocation(place)
        // 최근 콕 찍은 곳 저장은 fire-and-forget이다 — 저장 완료를 기다리지 않고 바로
        // 다음 화면으로 넘어간다. 실패하면 RecentPlaceRepository.saveFailureEvents로
        // 홈 화면까지 전달돼 그쪽에서 스낵바로 알린다.
        authRepository.currentUserUid()?.let { uid ->
            recentPlaceRepository.saveRecentPlace(uid, place)
        }
        _uiState.update { it.copy(locationConfirmed = true) }
    }

    fun consumeLocationConfirmed() {
        _uiState.update { it.copy(locationConfirmed = false) }
    }
}
