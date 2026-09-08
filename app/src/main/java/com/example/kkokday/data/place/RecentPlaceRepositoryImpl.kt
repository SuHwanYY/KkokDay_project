package com.example.kkokday.data.place

import android.util.Log
import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "RecentPlaceRepository"
private const val USERS_COLLECTION = "users"
private const val RECENT_PLACES_COLLECTION = "recentPlaces"

@Singleton
class RecentPlaceRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : RecentPlaceRepository {

    // saveRecentPlace를 호출한 화면(PlaceSearchViewModel)은 저장 완료를 기다리지 않고
    // 바로 popBackStack되어 viewModelScope가 취소될 수 있다. 그래도 저장이 끝까지
    // 실행되도록 화면 ViewModel이 아닌 이 Singleton 리포지토리 자신의 스코프에서 돈다.
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _saveFailureEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val saveFailureEvents: SharedFlow<Unit> = _saveFailureEvents.asSharedFlow()

    override fun saveRecentPlace(uid: String, place: KakaoPlace) {
        repositoryScope.launch {
            runAuthCatching {
                val data = mapOf(
                    "placeName" to place.placeName,
                    "address" to (place.roadAddressName ?: place.addressName),
                    "latitude" to place.latitude,
                    "longitude" to place.longitude,
                    "kakaoPlaceId" to place.id.takeIf { it.isNotBlank() },
                    "selectedAt" to FieldValue.serverTimestamp(),
                )
                recentPlacesCollection(uid)
                    .document(place.recentPlaceDocId())
                    .set(data, SetOptions.merge())
                    .await()
            }.onFailure { error ->
                Log.w(TAG, "최근 콕 찍은 곳 저장 실패", error)
                _saveFailureEvents.tryEmit(Unit)
            }
        }
    }

    override suspend fun getRecentPlaces(uid: String): Result<List<RecentPlace>> = runAuthCatching {
        recentPlacesCollection(uid)
            .orderBy("selectedAt", Query.Direction.DESCENDING)
            .limit(MAX_RECENT_PLACES.toLong())
            .get()
            .await()
            .documents
            .map { it.toRecentPlace() }
    }

    override suspend fun deleteRecentPlace(uid: String, docId: String): Result<Unit> = runAuthCatching {
        recentPlacesCollection(uid).document(docId).delete().await()
    }

    override suspend fun deleteAllRecentPlaces(uid: String): Result<Unit> = runAuthCatching {
        val snapshot = recentPlacesCollection(uid).get().await()
        if (snapshot.isEmpty) return@runAuthCatching
        val batch = firestore.get().batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    private fun recentPlacesCollection(uid: String) = firestore.get()
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(RECENT_PLACES_COLLECTION)

    private fun DocumentSnapshot.toRecentPlace() = RecentPlace(
        docId = id,
        placeName = getString("placeName").orEmpty(),
        address = getString("address").orEmpty(),
        latitude = getDouble("latitude") ?: 0.0,
        longitude = getDouble("longitude") ?: 0.0,
        kakaoPlaceId = getString("kakaoPlaceId"),
        // 저장 직후 서버 타임스탬프가 아직 반영되기 전이면 null일 수 있다 — 그땐 "방금"으로 본다.
        selectedAtMillis = getTimestamp("selectedAt")?.toDate()?.time ?: System.currentTimeMillis(),
    )

    /** 즐겨찾기/리뷰와 동일한 규칙([placeDocId]) — kakao place id 우선, 없으면 좌표 조합으로 폴백. */
    private fun KakaoPlace.recentPlaceDocId(): String = placeDocId()
}
