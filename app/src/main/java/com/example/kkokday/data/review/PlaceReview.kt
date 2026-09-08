package com.example.kkokday.data.review

import androidx.compose.runtime.Immutable

/** 리뷰 사진 최대 장수 — 리포지토리(업로드 제한)와 작성 화면(선택 제한)이 공유한다. */
const val MAX_REVIEW_PHOTOS = 5

/**
 * 장소 리뷰 한 건. Firestore `places/{placeDocId}/reviews/{uid}` 문서를 그대로 옮긴 값.
 * 문서 ID가 [authorUid]로 고정돼 있어 장소당 유저당 리뷰는 항상 1개뿐이다.
 * [photoUrls]가 List라 Compose 컴파일러가 기본 unstable로 추론하는데, 항상 새로 매핑해서
 * 만들 뿐 제자리에서 변경하지 않으므로 @Immutable로 명시해 리뷰 리스트 행의 불필요한
 * 재구성을 막는다.
 */
@Immutable
data class PlaceReview(
    val placeDocId: String,
    val authorUid: String,
    val authorNickname: String,
    /** 1~5 정수. */
    val rating: Int,
    val comment: String,
    val photoUrls: List<String> = emptyList(),
    /** 작성 시점 장소 정보를 같이 저장해둔 값 — "내가 쓴 리뷰" 목록에서 조회 없이 바로 보여준다.
     * 이 필드가 생기기 전에 작성된 리뷰는 빈 문자열이다. */
    val placeName: String = "",
    val address: String = "",
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

/** `places/{placeDocId}` 집계 문서 — Cloud Functions만 쓰고 클라이언트는 읽기만 한다. */
data class PlaceRatingSummary(
    val avgRating: Double,
    val reviewCount: Int,
)
