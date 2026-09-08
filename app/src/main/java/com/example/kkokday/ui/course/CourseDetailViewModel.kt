package com.example.kkokday.ui.course

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.data.course.CourseRepository
import com.example.kkokday.navigation.KkokDayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CourseDetailViewModel"

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle[KkokDayRoute.COURSE_DETAIL_ID_ARG])

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            courseRepository.getCourse(courseId)
                .onSuccess { course -> _uiState.update { it.copy(course = course, isLoading = false) } }
                .onFailure { error ->
                    Log.w(TAG, "코스 조회 실패", error)
                    _uiState.update { it.copy(isLoading = false, errorMessage = "코스를 불러오지 못했어요.") }
                }
        }
    }

    /**
     * 드래그 재정렬과 개별 장소 삭제 둘 다 이걸로 처리한다 — 화면에서 계산한 새 순서(또는
     * 항목이 빠진 리스트)를 그대로 받아 낙관적으로 반영하고 서버에 저장, 실패하면 되돌린다.
     */
    fun onPlacesChanged(newPlaces: List<CoursePlace>) {
        val current = _uiState.value.course ?: return
        val previous = current.places
        _uiState.update { it.copy(course = current.copy(places = newPlaces)) }
        viewModelScope.launch {
            courseRepository.updateCoursePlaces(courseId, newPlaces)
                .onFailure { error ->
                    Log.w(TAG, "코스 장소 변경 실패", error)
                    _uiState.update { state ->
                        state.copy(
                            course = state.course?.copy(places = previous),
                            toastMessage = "저장에 실패했어요. 다시 시도해주세요.",
                        )
                    }
                }
        }
    }

    fun onRenameClick() {
        _uiState.update { it.copy(showRenameDialog = true) }
    }

    fun onDismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = false) }
    }

    fun onConfirmRename(newTitle: String) {
        if (newTitle.isBlank()) return
        val current = _uiState.value.course ?: return
        _uiState.update { it.copy(course = current.copy(title = newTitle), showRenameDialog = false) }
        viewModelScope.launch {
            courseRepository.renameCourse(courseId, newTitle)
                .onFailure { error ->
                    Log.w(TAG, "코스 이름 변경 실패", error)
                    _uiState.update { state ->
                        state.copy(
                            course = state.course?.copy(title = current.title),
                            toastMessage = "이름 변경에 실패했어요. 다시 시도해주세요.",
                        )
                    }
                }
        }
    }

    fun onDeleteCourseClick() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun onDismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun onConfirmDeleteCourse() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
        viewModelScope.launch {
            courseRepository.deleteCourse(courseId)
                .onSuccess { _uiState.update { it.copy(courseDeleted = true) } }
                .onFailure { error ->
                    Log.w(TAG, "코스 삭제 실패", error)
                    _uiState.update { it.copy(toastMessage = "코스 삭제에 실패했어요. 다시 시도해주세요.") }
                }
        }
    }

    fun consumeToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
