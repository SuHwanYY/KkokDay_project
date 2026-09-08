package com.example.kkokday.data.user

/**
 * 홈 화면 "진행 중인 코스/투표" 섹션에서 사용자가 고정해둔 코스. `users/{uid}` 문서의
 * 필드 2개(homePinnedCourseId/homePinnedVoteCourseId)를 그대로 옮긴 값 — 코스 탭/투표 탭
 * 고정이 서로 독립적이라 필드도 둘로 분리돼 있다. 아직 고정한 적이 없으면 null(그러면
 * 호출부가 "가장 최근에 만든 코스"로 폴백한다).
 */
data class HomePins(
    val pinnedCourseId: String?,
    val pinnedVoteCourseId: String?,
)
