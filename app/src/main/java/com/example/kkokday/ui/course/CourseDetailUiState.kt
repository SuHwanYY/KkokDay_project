package com.example.kkokday.ui.course

import com.example.kkokday.data.course.Course

data class CourseDetailUiState(
    val course: Course? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** 저장/삭제 실패 등을 한 번 보여주고 소비(null로)할 메시지. */
    val toastMessage: String? = null,
    val showRenameDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    /** 삭제가 완료돼 화면에서 나가야 하는지. */
    val courseDeleted: Boolean = false,
)
