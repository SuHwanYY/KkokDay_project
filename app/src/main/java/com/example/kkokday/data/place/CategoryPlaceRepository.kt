package com.example.kkokday.data.place

import kotlinx.coroutines.flow.Flow

/** [CategoryPlaceRepository.searchCategory] 진행 상황. */
sealed interface CategorySearchEvent {
    /**
     * 지금까지 모은 전체 결과(거리 오름차순, 누적). 페이지를 하나씩 받을 때마다 새로 emit된다.
     * [totalCount]/[pageableCount]는 가장 최근 페이지 응답의 값 — 카카오 API가
     * pageableCount를 45로 상한을 두기 때문에, totalCount가 더 크면 일부만 보여주고
     * 있다는 뜻이다(화면의 "일부만 표시돼요" 안내에 사용).
     */
    data class Progress(
        val places: List<CategoryPlace>,
        val totalCount: Int,
        val pageableCount: Int,
    ) : CategorySearchEvent

    /** 페이지 조회 중 실패. 그 시점까지 emit된 [Progress]는 화면에 그대로 남는다. */
    data class Failed(val error: Throwable) : CategorySearchEvent
}

interface CategoryPlaceRepository {

    /**
     * [categoryGroupCode] 기준 주변 장소를 거리 오름차순으로 검색한다. 반경은 카카오 API
     * 상한인 20km 고정 — 사용자가 반경을 고르지 않고, 가까운 순으로 보여줄 수 있는 만큼
     * 보여주는 컨셉이다. 카카오 API가 페이지당 최대 15개, 최대 45페이지까지만 내려주기
     * 때문에 내부적으로 여러 페이지를 순회하며 결과를 누적해 [CategorySearchEvent.Progress]로
     * 흘려보낸다 — 전체 페이지를 다 받을 때까지 기다리지 않고 호출부가 점진적으로 화면에
     * 반영할 수 있다.
     */
    fun searchCategory(
        categoryGroupCode: String,
        latitude: Double,
        longitude: Double,
    ): Flow<CategorySearchEvent>

    /**
     * [query] 상호명으로 좌표 주변을 검색한다("카테고리 내 상호명 검색"). [categoryGroupCode]를
     * 주면 서버 단에서 그 카테고리로 좁혀서 찾고(FD6/CE7/CS2처럼 실제로 안정적으로 붙는
     * 카테고리에서만 의미가 있다), null이면 순수 키워드 검색이다(CT1/AT4처럼 카카오가
     * category_group_code를 잘 안 붙이는 카테고리용 — 호출부가 직접 분류 필터를 적용해야 한다).
     * [searchCategory]와 동일하게 페이지를 순회하며 누적 결과를 [CategorySearchEvent.Progress]로 흘려보낸다.
     */
    fun searchKeywordInCategory(
        query: String,
        categoryGroupCode: String?,
        latitude: Double,
        longitude: Double,
    ): Flow<CategorySearchEvent>

    /**
     * [query] 키워드로 좌표 주변 장소를 검색한다("술집" 보완 검색용 — FD6 카테고리
     * 검색만으로는 안 잡히는 호프/요리주점/포차/이자카야 같은 업종을 보완한다).
     * 카테고리 검색과 달리 결과가 많지 않을 것으로 보고 [maxPages]로 순회 페이지 수를 제한한다.
     */
    suspend fun searchKeywordNearby(
        query: String,
        latitude: Double,
        longitude: Double,
        maxPages: Int = 3,
    ): Result<List<CategoryPlace>>
}
