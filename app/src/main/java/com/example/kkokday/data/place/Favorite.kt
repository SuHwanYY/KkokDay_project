package com.example.kkokday.data.place

/** 즐겨찾기 한 건. Firestore `users/{uid}/favorites/{docId}` 문서를 그대로 옮긴 값. */
data class Favorite(
    val docId: String,
    val placeName: String,
    val categoryName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val placeUrl: String,
    val kakaoPlaceId: String?,
    val savedAtMillis: Long,
)

/** recentPlaces/리뷰와 동일한 규칙([placeDocId]) — kakao place id 우선, 없으면 좌표 조합으로 폴백. */
fun CategoryPlace.favoriteDocId(): String = placeDocId()
