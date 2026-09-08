package com.example.kkokday.ui.home

import com.example.kkokday.data.course.Course
import com.example.kkokday.data.place.KakaoPlace
import com.example.kkokday.data.place.RecentPlace
import com.example.kkokday.ui.main.model.OngoingCourseCardData
import com.example.kkokday.ui.main.model.OngoingSectionTab
import com.example.kkokday.ui.main.model.OngoingVoteCardData

data class HomeUiState(
    val nickname: String? = null,
    val subtitle: String? = null,
    val isKakaoSession: Boolean = false,
    /** 위치 지정 화면에서 확정한 장소. 아직 지정한 적이 없으면 null. */
    val currentLocation: KakaoPlace? = null,
    val recentPlaces: List<RecentPlace> = emptyList(),
    /** X 버튼을 눌러 삭제 확인 다이얼로그가 떠 있는 항목. null이면 다이얼로그를 띄우지 않는다. */
    val pendingDeletePlace: RecentPlace? = null,
    /** 카드를 탭해 재선택 확인 다이얼로그가 떠 있는 항목. null이면 다이얼로그를 띄우지 않는다. */
    val pendingSelectPlace: RecentPlace? = null,
    /** "전체 삭제" 확인 다이얼로그가 떠 있는지. */
    val pendingDeleteAll: Boolean = false,
    /** 저장/삭제 실패 등을 스낵바로 한 번 보여주고 나면 소비(null로)할 메시지. */
    val errorMessage: String? = null,

    // "진행 중인 코스/투표" 섹션 — 코스/투표 토글은 서로 독립적으로 고정 값을 관리한다.
    val ongoingTab: OngoingSectionTab = OngoingSectionTab.COURSE,
    val isOngoingLoading: Boolean = true,
    /** 지금 이 계정이 만든 코스 전체(updatedAt 내림차순) — 카드 계산과 "변경" 피커가 같이 쓴다. */
    val myCourses: List<Course> = emptyList(),
    val pinnedCourseCard: OngoingCourseCardData? = null,
    val pinnedVoteCard: OngoingVoteCardData? = null,
    /** "변경" 버튼을 눌러 코스 선택 바텀시트가 떠 있는지. */
    val showCoursePicker: Boolean = false,
)
