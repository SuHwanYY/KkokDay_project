package com.example.kkokday.data.place

import kotlinx.coroutines.flow.SharedFlow

interface RecentPlaceRepository {

    /**
     * 백그라운드 저장 실패 이벤트. [saveRecentPlace]는 화면 전환을 막지 않도록
     * fire-and-forget으로 동작하므로, 실패를 화면에 알리려면 이 이벤트를 구독해야 한다.
     */
    val saveFailureEvents: SharedFlow<Unit>

    /**
     * 위치 지정을 확정할 때마다 호출한다. Firestore 저장이 끝날 때까지 호출부를
     * 기다리게 하지 않기 위해 fire-and-forget으로 동작한다 — 결과는 [saveFailureEvents]로만 알 수 있다.
     */
    fun saveRecentPlace(uid: String, place: KakaoPlace)

    /** selectedAt 내림차순 최대 30개. */
    suspend fun getRecentPlaces(uid: String): Result<List<RecentPlace>>

    suspend fun deleteRecentPlace(uid: String, docId: String): Result<Unit>

    /** 최근 콕 찍은 곳을 전부 삭제한다. */
    suspend fun deleteAllRecentPlaces(uid: String): Result<Unit>
}
