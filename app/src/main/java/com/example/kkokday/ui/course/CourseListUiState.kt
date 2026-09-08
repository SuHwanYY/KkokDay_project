package com.example.kkokday.ui.course

import com.example.kkokday.data.course.Course

data class CourseListUiState(
    /** updatedAt 내림차순(최근 수정순). */
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** 코스 생성/삭제 결과를 한 번 보여주고 소비(null로)할 토스트 메시지. */
    val toastMessage: String? = null,
    /** "+" 버튼으로 여는, 이름(필수)+설명(선택) 입력 다이얼로그 — 장소 없이 빈 코스를 만든다. */
    val showNewCourseDialog: Boolean = false,
    /** true면 카드가 체크박스 다중 선택 모드다("삭제" 버튼으로 진입). */
    val selectionMode: Boolean = false,
    val selectedCourseIds: Set<String> = emptySet(),
    val showDeleteConfirm: Boolean = false,
)
