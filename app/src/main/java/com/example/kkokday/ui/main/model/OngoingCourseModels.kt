package com.example.kkokday.ui.main.model

/** 홈 화면 "진행 중인 코스/투표" 섹션의 토글. */
enum class OngoingSectionTab {
    COURSE,
    VOTE,
}

/** 코스 토글에 고정된 카드 하나 분량의 표시 데이터. */
data class OngoingCourseCardData(
    val courseId: String,
    val courseTitle: String,
    val placeCount: Int,
)

/**
 * 투표 토글에 고정된 카드 하나 분량의 표시 데이터. [topCandidateName]이 null이면 아직
 * 아무도 투표하지 않은 상태(집계 값도 전부 0).
 */
data class OngoingVoteCardData(
    val courseId: String,
    val courseTitle: String,
    val topCandidateName: String?,
    val topCandidateVoteCount: Int,
    val participantCount: Int,
)
