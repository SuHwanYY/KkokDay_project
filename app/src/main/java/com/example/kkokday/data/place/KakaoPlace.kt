package com.example.kkokday.data.place

/**
 * 카카오 로컬 검색 결과 하나. [longitude]/[latitude]는 카테고리별 주변 검색(다음 화면)에
 * 그대로 넘길 좌표다 — 카카오 로컬 API 관례대로 x=경도, y=위도.
 */
data class KakaoPlace(
    val id: String,
    val placeName: String,
    val addressName: String,
    val roadAddressName: String?,
    val longitude: Double,
    val latitude: Double,
)
