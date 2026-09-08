package com.example.kkokday.data.review

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.kkokday.data.auth.runAuthCatching
import com.example.kkokday.data.media.compressImageForUpload
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

private const val TAG = "PlaceReviewRepository"
private const val PLACES_COLLECTION = "places"
private const val REVIEWS_COLLECTION = "reviews"
private const val REVIEWS_STORAGE_ROOT = "reviews"

/** Firestore `whereIn`이 한 번에 받는 최대 개수. */
private const val WHERE_IN_CHUNK_SIZE = 30

@Singleton
class PlaceReviewRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: Lazy<FirebaseFirestore>,
    private val storage: Lazy<FirebaseStorage>,
) : PlaceReviewRepository {

    override suspend fun getReviews(placeDocId: String): Result<List<PlaceReview>> = runAuthCatching {
        reviewsCollection(placeDocId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toPlaceReview(placeDocId) }
    }

    override suspend fun getMyReview(placeDocId: String, uid: String): Result<PlaceReview?> = runAuthCatching {
        reviewsCollection(placeDocId).document(uid).get().await()
            .takeIf { it.exists() }
            ?.toPlaceReview(placeDocId)
    }

    override suspend fun getMyReviews(uid: String): Result<List<PlaceReview>> = runAuthCatching {
        firestore.get().collectionGroup(REVIEWS_COLLECTION)
            .whereEqualTo("authorUid", uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { doc -> doc.toPlaceReview(doc.reference.parent.parent?.id.orEmpty()) }
    }

    override suspend fun getRatingSummary(placeDocId: String): Result<PlaceRatingSummary?> = runAuthCatching {
        placesCollection().document(placeDocId).get().await().toRatingSummaryOrNull()
    }

    override suspend fun getRatingSummaries(placeDocIds: List<String>): Result<Map<String, PlaceRatingSummary>> =
        runAuthCatching {
            val distinctIds = placeDocIds.distinct()
            if (distinctIds.isEmpty()) return@runAuthCatching emptyMap()

            distinctIds.chunked(WHERE_IN_CHUNK_SIZE).fold(emptyMap<String, PlaceRatingSummary>()) { acc, chunk ->
                val snapshot = placesCollection()
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                acc + snapshot.documents.mapNotNull { doc -> doc.toRatingSummaryOrNull()?.let { doc.id to it } }
            }
        }

    override suspend fun saveReview(
        placeDocId: String,
        uid: String,
        authorNickname: String,
        rating: Int,
        comment: String,
        keepPhotoUrls: List<String>,
        newPhotoUris: List<Uri>,
        removedPhotoUrls: List<String>,
        placeName: String,
        address: String,
    ): Result<Unit> = runAuthCatching {
        removedPhotoUrls.forEach { url -> deletePhotoQuietly(url) }

        val docRef = reviewsCollection(placeDocId).document(uid)

        // 사진 여러 장을 순차로 올리면(장당 압축+업로드 왕복) 저장이 눈에 띄게 느려져서
        // 전부 병렬로 올린다. "새 리뷰인지" 확인도 어차피 독립적인 조회라 같이 병렬로 묶는다.
        val (uploadedUrls, isNew) = coroutineScope {
            val uploadDeferreds = newPhotoUris.map { uri -> async { uploadPhoto(placeDocId, uid, uri) } }
            val isNewDeferred = async { !docRef.get().await().exists() }
            uploadDeferreds.awaitAll() to isNewDeferred.await()
        }

        // createdAt은 최초 생성 때만 채운다 — merge라도 매번 serverTimestamp()를 보내면
        // 수정할 때마다 작성일이 갱신돼버린다.
        val data = buildMap<String, Any> {
            put("rating", rating)
            put("comment", comment)
            put("photoUrls", keepPhotoUrls + uploadedUrls)
            put("authorUid", uid)
            put("authorNickname", authorNickname)
            put("placeName", placeName)
            put("address", address)
            put("updatedAt", FieldValue.serverTimestamp())
            if (isNew) put("createdAt", FieldValue.serverTimestamp())
        }
        docRef.set(data, SetOptions.merge()).await()
        Unit
    }

    override suspend fun deleteReview(placeDocId: String, uid: String): Result<Unit> = runAuthCatching {
        val folderRef = storage.get().reference
            .child(REVIEWS_STORAGE_ROOT)
            .child(placeDocId)
            .child(uid)
        try {
            folderRef.listAll().await().items.forEach { item ->
                try {
                    item.delete().await()
                } catch (error: Exception) {
                    Log.w(TAG, "리뷰 사진 삭제 실패(무시): ${item.path}", error)
                }
            }
        } catch (error: Exception) {
            Log.w(TAG, "리뷰 사진 목록 조회 실패(무시): placeDocId=$placeDocId uid=$uid", error)
        }
        reviewsCollection(placeDocId).document(uid).delete().await()
    }

    /** 업로드 전 [compressImageForUpload]로 리사이즈/재압축한다 — 원본 그대로 올리면 저장이 오래 걸린다. */
    private suspend fun uploadPhoto(placeDocId: String, uid: String, uri: Uri): String {
        val compressed = compressImageForUpload(context, uri)
        val ref = storage.get().reference
            .child(REVIEWS_STORAGE_ROOT)
            .child(placeDocId)
            .child(uid)
            .child("${UUID.randomUUID()}.jpg")
        ref.putBytes(compressed).await()
        return ref.downloadUrl.await().toString()
    }

    private suspend fun deletePhotoQuietly(url: String) {
        try {
            storage.get().getReferenceFromUrl(url).delete().await()
        } catch (error: Exception) {
            Log.w(TAG, "리뷰 사진 삭제 실패(무시): $url", error)
        }
    }

    private fun placesCollection() = firestore.get().collection(PLACES_COLLECTION)

    private fun reviewsCollection(placeDocId: String) =
        placesCollection().document(placeDocId).collection(REVIEWS_COLLECTION)

    private fun DocumentSnapshot.toRatingSummaryOrNull(): PlaceRatingSummary? {
        val avgRating = getDouble("avgRating") ?: return null
        val reviewCount = getLong("reviewCount")?.toInt() ?: return null
        return PlaceRatingSummary(avgRating = avgRating, reviewCount = reviewCount)
    }

    private fun DocumentSnapshot.toPlaceReview(placeDocId: String) = PlaceReview(
        placeDocId = placeDocId,
        authorUid = getString("authorUid").orEmpty(),
        authorNickname = getString("authorNickname").orEmpty(),
        rating = (getLong("rating") ?: 0L).toInt(),
        comment = getString("comment").orEmpty(),
        photoUrls = (get("photoUrls") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        placeName = getString("placeName").orEmpty(),
        address = getString("address").orEmpty(),
        createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time ?: 0L,
    )
}
