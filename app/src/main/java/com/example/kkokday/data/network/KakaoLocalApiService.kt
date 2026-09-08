package com.example.kkokday.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoLocalApiService {

    /** 카카오 로컬 키워드 검색. Authorization 헤더는 OkHttp 인터셉터가 자동으로 붙인다. */
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15,
    ): KakaoKeywordSearchResponseDto

    /** 카카오 로컬 카테고리 검색(주변 카테고리별 장소). sort=distance면 x,y 기준 거리순으로 내려온다. */
    @GET("v2/local/search/category.json")
    suspend fun searchCategory(
        @Query("category_group_code") categoryGroupCode: String,
        @Query("x") x: String,
        @Query("y") y: String,
        @Query("radius") radius: Int,
        @Query("page") page: Int,
        @Query("sort") sort: String = "distance",
        @Query("size") size: Int = 15,
    ): KakaoCategorySearchResponseDto

    /**
     * 카카오 로컬 키워드 검색을 좌표 기준으로 제한해서 호출한다("술집" 보완 검색용, 카테고리 내
     * 상호명 검색용). x/y/radius를 같이 주면 [searchCategory]와 동일한 응답 스키마
     * (documents.distance, meta)를 그대로 내려주기 때문에 [KakaoCategorySearchResponseDto]를
     * 그대로 재사용한다.
     *
     * [categoryGroupCode]는 옵션이다 — null이면 Retrofit이 쿼리 파라미터 자체를 안 붙인다.
     * 실측 확인: FD6/CE7/CS2 소속 장소는 키워드 검색 결과에도 category_group_code가 안정적으로
     * 붙어있어 서버 필터로 좁혀도 되지만, CT1/AT4는 대부분 비어있어(카카오 자체 데이터 한계)
     * 이 값을 넘기면 결과가 거의 다 사라진다 — 호출부(CategoryPlaceRepositoryImpl)가 그
     * 카테고리에서만 null로 호출해서 순수 키워드 검색으로 대체한다.
     */
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeywordNearby(
        @Query("query") query: String,
        @Query("category_group_code") categoryGroupCode: String? = null,
        @Query("x") x: String,
        @Query("y") y: String,
        @Query("radius") radius: Int,
        @Query("page") page: Int,
        @Query("sort") sort: String = "distance",
        @Query("size") size: Int = 15,
    ): KakaoCategorySearchResponseDto
}

@Serializable
data class KakaoKeywordSearchResponseDto(
    val documents: List<KakaoPlaceDocumentDto> = emptyList(),
)

@Serializable
data class KakaoPlaceDocumentDto(
    val id: String,
    @SerialName("place_name") val placeName: String,
    @SerialName("address_name") val addressName: String,
    @SerialName("road_address_name") val roadAddressName: String? = null,
    /** 카카오 API가 경도/위도를 문자열로 내려준다. */
    val x: String,
    val y: String,
)

@Serializable
data class KakaoCategorySearchResponseDto(
    val documents: List<KakaoCategoryPlaceDocumentDto> = emptyList(),
    val meta: KakaoCategorySearchMetaDto = KakaoCategorySearchMetaDto(),
)

@Serializable
data class KakaoCategoryPlaceDocumentDto(
    val id: String,
    @SerialName("place_name") val placeName: String,
    @SerialName("category_name") val categoryName: String,
    @SerialName("address_name") val addressName: String,
    @SerialName("road_address_name") val roadAddressName: String? = null,
    val x: String,
    val y: String,
    /** 미터 단위 거리. 문자열로 내려온다(빈 문자열일 수 있음). */
    val distance: String = "",
    /** 전화번호 없는 장소는 빈 문자열로 내려온다. */
    val phone: String = "",
    @SerialName("place_url") val placeUrl: String = "",
)

@Serializable
data class KakaoCategorySearchMetaDto(
    @SerialName("is_end") val isEnd: Boolean = true,
    @SerialName("pageable_count") val pageableCount: Int = 0,
    @SerialName("total_count") val totalCount: Int = 0,
)
