package com.example.kkokday.ui.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.course.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CourseListViewModel"

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseListUiState())
    val uiState: StateFlow<CourseListUiState> = _uiState.asStateFlow()

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
            courseRepository.getMyCourses(uid)
                .onSuccess { courses -> _uiState.update { it.copy(courses = courses, isLoading = false) } }
                .onFailure { error ->
                    Log.w(TAG, "코스 목록 조회 실패", error)
                    _uiState.update { it.copy(isLoading = false, errorMessage = "코스 목록을 불러오지 못했어요.") }
                }
        }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun onAddCourseClick() {
        _uiState.update { it.copy(showNewCourseDialog = true) }
    }

    fun onDismissNewCourseDialog() {
        _uiState.update { it.copy(showNewCourseDialog = false) }
    }

    /** 장소 없이 이름(+설명)만으로 빈 코스를 바로 만든다 — 장소는 코스 상세/즐겨찾기·검색 화면에서 나중에 담는다. */
    fun onConfirmNewCourse(title: String, description: String) {
        if (title.isBlank()) return
        val uid = authRepository.currentUserUid() ?: return
        viewModelScope.launch {
            courseRepository.createCourse(uid, title, description, emptyList())
                .onSuccess {
                    _uiState.update { it.copy(showNewCourseDialog = false) }
                    refresh()
                }
                .onFailure { error ->
                    Log.w(TAG, "코스 생성 실패", error)
                    _uiState.update { it.copy(toastMessage = "코스 생성에 실패했어요. 다시 시도해주세요.") }
                }
        }
    }

    fun onSelectionModeToggle() {
        _uiState.update { state ->
            if (state.selectionMode) {
                state.copy(selectionMode = false, selectedCourseIds = emptySet())
            } else {
                state.copy(selectionMode = true)
            }
        }
    }

    fun onCourseSelectToggle(courseId: String) {
        _uiState.update { state ->
            state.copy(
                selectedCourseIds = if (courseId in state.selectedCourseIds) {
                    state.selectedCourseIds - courseId
                } else {
                    state.selectedCourseIds + courseId
                },
            )
        }
    }

    fun onDeleteSelectedClick() {
        if (_uiState.value.selectedCourseIds.isEmpty()) return
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun onDismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun onConfirmDeleteSelected() {
        val ids = _uiState.value.selectedCourseIds
        _uiState.update { it.copy(showDeleteConfirm = false) }
        viewModelScope.launch {
            val failureCount = ids.count { id ->
                courseRepository.deleteCourse(id)
                    .onFailure { error -> Log.w(TAG, "코스 삭제 실패: id=$id", error) }
                    .isFailure
            }
            _uiState.update {
                it.copy(
                    selectionMode = false,
                    selectedCourseIds = emptySet(),
                    toastMessage = if (failureCount > 0) {
                        "일부 코스를 삭제하지 못했어요."
                    } else {
                        "선택한 코스를 삭제했어요."
                    },
                )
            }
            refresh()
        }
    }
}
