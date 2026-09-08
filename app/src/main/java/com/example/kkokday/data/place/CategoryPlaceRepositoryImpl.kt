package com.example.kkokday.data.place

import android.util.Log
import com.example.kkokday.data.auth.runAuthCatching
import com.example.kkokday.data.network.KakaoCategoryPlaceDocumentDto
import com.example.kkokday.data.network.KakaoCategorySearchResponseDto
import com.example.kkokday.data.network.KakaoLocalApiService
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "CategoryPlaceRepository"

/** 카카오 로컬 카테고리 검색 API가 허용하는 최대 페이지 수. */
private const val MAX_PAGE = 45

/** 카카오 로컬 API가 허용하는 반경 상한(미터) — "반경을 고른다"가 아니라 항상 이 값으로 고정 검색한다. */
private const val FIXED_SEARCH_RADIUS_METERS = 20000

class CategoryPlaceRepositoryImpl @Inject constructor(
    private val kakaoLocalApiService: KakaoLocalApiService,
) : CategoryPlaceRepository {

    override fun searchCategory(
        categoryGroupCode: String,
        latitude: Double,
        longitude: Double,
    ): Flow<CategorySearchEvent> {
        Log.d(
            TAG,
            "검색 시작: category=$categoryGroupCode x(경도)=$longitude y(위도)=$latitude " +
                "radius=${FIXED_SEARCH_RADIUS_METERS}m",
        )
        return paginatedSearch("카테고리 검색($categoryGroupCode)") { page ->
            kakaoLocalApiService.searchCategory(
                categoryGroupCode = categoryGroupCode,
                x = longitude.toString(),
                y = latitude.toString(),
                radius = FIXED_SEARCH_RADIUS_METERS,
                page = page,
            )
        }
    }

    override fun searchKeywordInCategory(
        query: String,
        categoryGroupCode: String?,
        latitude: Double,
        longitude: Double,
    ): Flow<CategorySearchEvent> {
        Log.d(
            TAG,
            "상호명 검색 시작: query=$query category=$categoryGroupCode x(경도)=$longitude y(위도)=$latitude",
        )
        return paginatedSearch("상호명 검색(query=$query, category=$categoryGroupCode)") { page ->
            kakaoLocalApiService.searchKeywordNearby(
                query = query,
                categoryGroupCode = categoryGroupCode,
                x = longitude.toString(),
                y = latitude.toString(),
                radius = FIXED_SEARCH_RADIUS_METERS,
                page = page,
            )
        }
    }

    /**
     * [searchCategory]/[searchKeywordInCategory]가 공유하는 페이지 순회 로직. 카카오 로컬
     * API가 페이지당 최대 15개, 최대 45페이지까지만 내려주기 때문에 여러 페이지를 순회하며
     * 결과를 누적해 [CategorySearchEvent.Progress]로 흘려보낸다 — 전체 페이지를 다 받을
     * 때까지 기다리지 않고 호출부가 점진적으로 화면에 반영할 수 있다.
     */
    private fun paginatedSearch(
        logLabel: String,
        fetchPage: suspend (page: Int) -> KakaoCategorySearchResponseDto,
    ): Flow<CategorySearchEvent> = flow {
        val accumulated = mutableListOf<CategoryPlace>()
        for (page in 1..MAX_PAGE) {
            val result = runAuthCatching { fetchPage(page) }

            val response = result.getOrElse { error ->
                Log.w(TAG, "$logLabel page=$page 요청 실패", error)
                emit(CategorySearchEvent.Failed(error))
                return@flow
            }

            // 원본 meta를 그대로 찍는다 — 종료 조건(isEnd)과 total/pageable count를
            // 직접 눈으로 확인할 수 있게. documents.size 같은 자체 휴리스틱은 쓰지 않는다.
            Log.d(
                TAG,
                "$logLabel page=$page documents=${response.documents.size} " +
                    "totalCount=${response.meta.totalCount} pageableCount=${response.meta.pageableCount} " +
                    "isEnd=${response.meta.isEnd}",
            )

            accumulated += response.documents.map { it.toCategoryPlace() }
            emit(
                CategorySearchEvent.Progress(
                    places = accumulated.toList(),
                    totalCount = response.meta.totalCount,
                    pageableCount = response.meta.pageableCount,
                ),
            )

            if (response.meta.isEnd) break
        }
    }

    override suspend fun searchKeywordNearby(
        query: String,
        latitude: Double,
        longitude: Double,
        maxPages: Int,
    ): Result<List<CategoryPlace>> = runAuthCatching {
        val accumulated = mutableListOf<CategoryPlace>()
        for (page in 1..maxPages) {
            val response = kakaoLocalApiService.searchKeywordNearby(
                query = query,
                x = longitude.toString(),
                y = latitude.toString(),
                radius = FIXED_SEARCH_RADIUS_METERS,
                page = page,
            )
            Log.d(
                TAG,
                "키워드 보완검색 query=$query page=$page documents=${response.documents.size} " +
                    "totalCount=${response.meta.totalCount} isEnd=${response.meta.isEnd}",
            )
            accumulated += response.documents.map { it.toCategoryPlace() }
            if (response.meta.isEnd) break
        }
        accumulated
    }

    private fun KakaoCategoryPlaceDocumentDto.toCategoryPlace() = CategoryPlace(
        id = id,
        placeName = placeName,
        categoryName = categoryName,
        address = roadAddressName?.takeIf { it.isNotBlank() } ?: addressName,
        latitude = y.toDouble(),
        longitude = x.toDouble(),
        distanceMeters = distance.toIntOrNull() ?: 0,
        phone = phone,
        placeUrl = placeUrl,
    )
}
