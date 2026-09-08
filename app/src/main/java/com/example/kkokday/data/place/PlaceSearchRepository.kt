package com.example.kkokday.data.place

interface PlaceSearchRepository {

    /** 카카오 로컬 키워드 검색. 결과가 없으면 빈 리스트로 성공한다(그 자체는 오류가 아님). */
    suspend fun searchPlaces(query: String): Result<List<KakaoPlace>>
}
