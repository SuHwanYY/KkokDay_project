package com.example.kkokday.ui.vote

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

private const val TAG = "VoteListViewModel"

@HiltViewModel
class VoteListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoteListUiState())
    val uiState: StateFlow<VoteListUiState> = _uiState.asStateFlow()

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
}
