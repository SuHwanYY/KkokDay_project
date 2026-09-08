package com.example.kkokday.data.place

/**
 * 사용자당 보관하는 "최근 콕 찍은 곳" 최대 개수. 서버(Cloud Functions
 * `cleanupRecentPlaces`)가 이 개수를 넘는 오래된 항목을 정리하는 것과 별개로,
 * 클라이언트 조회/표시(예: 카운터 UI)도 이 상수 하나만 참조한다.
 */
const val MAX_RECENT_PLACES = 30

/** "최근 콕 찍은 곳" 한 건. Firestore `users/{uid}/recentPlaces/{docId}` 문서를 그대로 옮긴 값. */
data class RecentPlace(
    val docId: String,
    val placeName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val kakaoPlaceId: String?,
    val selectedAtMillis: Long,
)
