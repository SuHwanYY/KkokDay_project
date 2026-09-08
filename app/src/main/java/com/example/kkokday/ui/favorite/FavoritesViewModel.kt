package com.example.kkokday.ui.favorite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.data.course.CourseRepository
import com.example.kkokday.data.place.Favorite
import com.example.kkokday.data.place.FavoriteRepository
import com.example.kkokday.ui.course.CourseSaveDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "FavoritesViewModel"

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val courseSaveDelegate = CourseSaveDelegate(
        courseRepository = courseRepository,
        authRepository = authRepository,
        scope = viewModelScope,
        getState = { _uiState.value.courseSave },
        updateState = { transform -> _uiState.update { it.copy(courseSave = transform(it.courseSave)) } },
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = authRepository.currentUserUid()
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            favoriteRepository.getFavorites(uid)
                .onSuccess { favorites -> _uiState.update { it.copy(favorites = favorites, isLoading = false) } }
                .onFailure { error ->
                    Log.w(TAG, "즐겨찾기 목록 조회 실패", error)
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "즐겨찾기를 불러오지 못했어요.")
                    }
                }
        }
    }

    /**
     * 이 화면의 항목은 항상 즐겨찾기된 상태로만 보이므로, 토글은 곧 해제다.
     * 목록에서 먼저 낙관적으로 제거한 뒤, 실패하면 되돌리고 스낵바로 알린다.
     */
    fun onFavoriteToggleClick(favorite: Favorite) {
        val uid = authRepository.currentUserUid() ?: return

        _uiState.update { state ->
            state.copy(
                favorites = state.favorites.filterNot { it.docId == favorite.docId },
                favoriteToastMessage = "즐겨찾기에서 삭제했어요",
            )
        }

        viewModelScope.launch {
            favoriteRepository.removeFavorite(uid, favorite.docId)
                .onFailure { error ->
                    Log.w(TAG, "즐겨찾기 해제 실패", error)
                    _uiState.update { state ->
                        state.copy(
                            favorites = (state.favorites + favorite).sortedByDescending { it.savedAtMillis },
                            errorMessage = "즐겨찾기 해제에 실패했어요. 다시 시도해주세요.",
                        )
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

    // "코스 만들기" 다중 선택 흐름 — 실제 로직은 [CourseSaveDelegate]가 갖고 있다.
    fun onCourseModeToggle() = courseSaveDelegate.onModeToggle()
    fun onPlaceSelectToggle(id: String) = courseSaveDelegate.onSelectToggle(id)
    fun onCourseSaveClick() = courseSaveDelegate.onSaveClick()
    fun onDismissExistingCoursePicker() = courseSaveDelegate.onDismissExistingCoursePicker()
    fun consumeCourseToastMessage() = courseSaveDelegate.consumeToastMessage()

    fun onExistingCourseSelected(course: Course) =
        courseSaveDelegate.onExistingCourseSelected(course, selectedCoursePlaces())

    private fun selectedCoursePlaces(): List<CoursePlace> {
        val selectedIds = _uiState.value.courseSave.selectedIds
        return _uiState.value.favorites
            .filter { it.docId in selectedIds }
            .map { it.toCoursePlace() }
    }

    private fun Favorite.toCoursePlace() = CoursePlace(
        placeName = placeName,
        address = address,
        latitude = latitude,
        longitude = longitude,
        kakaoPlaceId = kakaoPlaceId,
        category = categoryName,
    )
}
