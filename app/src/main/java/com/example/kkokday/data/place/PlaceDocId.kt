package com.example.kkokday.data.place

/**
 * 카카오 place id가 있으면 그걸, 없으면 좌표 조합을 문서 ID로 쓴다. 즐겨찾기/최근콕찍은곳/
 * 리뷰가 모두 이 규칙을 공유해야 같은 장소가 항상 같은 Firestore 문서(`places/{placeDocId}`
 * 포함)를 가리킨다.
 */
fun placeDocId(kakaoPlaceId: String, latitude: Double, longitude: Double): String =
    kakaoPlaceId.takeIf { it.isNotBlank() } ?: "${latitude}_${longitude}"

fun CategoryPlace.placeDocId(): String = placeDocId(id, latitude, longitude)

fun KakaoPlace.placeDocId(): String = placeDocId(id, latitude, longitude)
