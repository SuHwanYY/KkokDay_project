package com.example.kkokday.data.place

/** 카카오 로컬 카테고리 검색 결과 한 건. [distanceMeters]는 검색 기준 좌표로부터의 거리. */
data class CategoryPlace(
    val id: String,
    val placeName: String,
    val categoryName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int,
    /** 빈 문자열이면 전화번호 없음 — 전화 걸기 버튼을 숨기는 기준. */
    val phone: String = "",
    val placeUrl: String = "",
    /**
     * `places/{placeDocId}` 집계 문서(리뷰 서브컬렉션에 쓰기가 있을 때마다 Cloud Functions가
     * 재계산)에서 배치 조회해 채워 넣는 리뷰 평점 요약. 리뷰가 없는 장소는 집계 문서 자체가
     * 없어 null로 남는다 — [avgRating]/[reviewCount] 배지·[popularityScore] 계산 모두 이
     * null 여부로 "리뷰 없음"을 판단한다.
     */
    val avgRating: Double? = null,
    val reviewCount: Int? = null,
)
