package com.example.kkokday.data.review

import android.net.Uri

interface PlaceReviewRepository {

    /** 최신순(createdAt 내림차순) 리뷰 전체. */
    suspend fun getReviews(placeDocId: String): Result<List<PlaceReview>>

    /** 로그인 유저가 이 장소에 남긴 리뷰(없으면 null). */
    suspend fun getMyReview(placeDocId: String, uid: String): Result<PlaceReview?>

    /** 로그인 유저가 여러 장소에 남긴 리뷰 전체(최근 수정순) — collectionGroup 쿼리로 한 번에 모은다. */
    suspend fun getMyReviews(uid: String): Result<List<PlaceReview>>

    /** 장소 리뷰 화면 상단 요약용 — 집계 문서가 없으면(리뷰 없음) null. */
    suspend fun getRatingSummary(placeDocId: String): Result<PlaceRatingSummary?>

    /**
     * 카테고리 목록 로드 시 화면에 나온 장소들만 배치 조회한다. 반경 내 전체 결과를
     * 미리 다 조회하지 않도록 호출부가 그때그때 필요한 [placeDocIds]만 넘긴다.
     * 내부적으로 Firestore `whereIn` 제약(최대 30개)에 맞춰 청크로 나눠 조회한다.
     */
    suspend fun getRatingSummaries(placeDocIds: List<String>): Result<Map<String, PlaceRatingSummary>>

    /**
     * 리뷰 생성/수정 UPSERT. [removedPhotoUrls]는 수정 중 사용자가 뺀 기존 사진(Storage에서
     * 삭제), [keepPhotoUrls]는 그대로 남긴 기존 사진, [newPhotoUris]는 이번에 새로 추가한
     * 로컬 사진(업로드 후 URL로 교체)이다. 최종 `photoUrls`는 keep + new(업로드 완료) 순서.
     */
    suspend fun saveReview(
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
    ): Result<Unit>

    /** 리뷰 문서와 그 사진 전부를 삭제한다. */
    suspend fun deleteReview(placeDocId: String, uid: String): Result<Unit>
}
