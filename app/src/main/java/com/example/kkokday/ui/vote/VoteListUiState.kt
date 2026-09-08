package com.example.kkokday.ui.vote

import com.example.kkokday.data.course.Course

data class VoteListUiState(
    /** updatedAt 내림차순(최근 수정순) 본인 코스 전체 — [com.example.kkokday.ui.course.CourseListUiState]와 동일 출처. */
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
