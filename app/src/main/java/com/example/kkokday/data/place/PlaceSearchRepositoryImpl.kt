package com.example.kkokday.data.place

import com.example.kkokday.data.auth.runAuthCatching
import com.example.kkokday.data.network.KakaoLocalApiService
import com.example.kkokday.data.network.KakaoPlaceDocumentDto
import javax.inject.Inject

class PlaceSearchRepositoryImpl @Inject constructor(
    private val kakaoLocalApiService: KakaoLocalApiService,
) : PlaceSearchRepository {

    override suspend fun searchPlaces(query: String): Result<List<KakaoPlace>> = runAuthCatching {
        kakaoLocalApiService.searchKeyword(query = query).documents.map { it.toKakaoPlace() }
    }

    private fun KakaoPlaceDocumentDto.toKakaoPlace() = KakaoPlace(
        id = id,
        placeName = placeName,
        addressName = addressName,
        // 카카오 API는 도로명 주소가 없으면 null이 아니라 빈 문자열("")을 내려준다.
        roadAddressName = roadAddressName?.takeIf { it.isNotBlank() },
        longitude = x.toDouble(),
        latitude = y.toDouble(),
    )
}
