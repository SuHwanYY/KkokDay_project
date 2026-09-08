package com.example.kkokday.data.place

interface FavoriteRepository {

    /**
     * 문서 ID만 모아서 가져온다. 카테고리 검색 결과 리스트에 즐겨찾기 여부를 표시할 때
     * 장소마다 개별 조회하지 않고, 진입 시점에 이걸로 한 번에 대조하기 위함.
     */
    suspend fun getFavoriteIds(uid: String): Result<Set<String>>

    /** savedAt 내림차순(저장 최신순) 전체 목록. */
    suspend fun getFavorites(uid: String): Result<List<Favorite>>

    suspend fun addFavorite(uid: String, place: CategoryPlace): Result<Unit>

    suspend fun removeFavorite(uid: String, docId: String): Result<Unit>
}
