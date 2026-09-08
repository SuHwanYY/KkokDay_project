package com.example.kkokday.ui.course

import com.example.kkokday.data.course.Course

/**
 * "코스 만들기" 다중 선택 흐름의 상태 — 즐겨찾기 탭과 카테고리 검색 결과 화면이 공통으로
 * 갖는다. 각 화면의 UiState가 이 값을 필드 하나로 담아 쓴다([CourseSaveDelegate] 참고).
 */
data class CourseSaveUiState(
    /** true면 리스트가 체크박스 다중 선택 모드다. */
    val selectionMode: Boolean = false,
    /** CategoryPlace.id 또는 Favorite.docId — 화면마다 쓰는 키를 그대로 담는다. */
    val selectedIds: Set<String> = emptySet(),
    /** 보유 코스 목록 바텀시트 — "담기"를 누르면 방식을 고르는 단계 없이 바로 뜬다. */
    val showExistingCoursePicker: Boolean = false,
    val myCourses: List<Course> = emptyList(),
    val isLoadingMyCourses: Boolean = false,
    /** 저장/실패 결과를 한 번 보여주고 소비(null로)할 토스트 메시지. */
    val toastMessage: String? = null,
)
