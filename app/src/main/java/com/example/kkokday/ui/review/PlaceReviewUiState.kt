package com.example.kkokday.ui.review

import android.net.Uri
import com.example.kkokday.data.review.MAX_REVIEW_PHOTOS
import com.example.kkokday.data.review.PlaceReview

/** 신고 사유 최대 길이 — 다이얼로그 입력 제한과 firestore.rules 검증이 같은 값을 쓴다. */
const val MAX_REPORT_REASON_LENGTH = 500

data class PlaceReviewUiState(
    val placeName: String = "",
    val address: String = "",
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    /** 최신순(createdAt 내림차순). 차단한 작성자의 리뷰는 조회 직후 클라이언트에서 걸러낸다. */
    val reviews: List<PlaceReview> = emptyList(),
    /** 로그인 유저가 이 장소에 남긴 리뷰. 없으면 "리뷰 작성", 있으면 "내 리뷰 수정" 버튼을 보여준다. */
    val myReview: PlaceReview? = null,
    /** 로그인 유저 uid(없으면 비로그인). 리뷰 행에서 "내 리뷰"엔 신고/차단 메뉴를 숨기는 기준. */
    val currentUserUid: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val toastMessage: String? = null,
    val showEditSheet: Boolean = false,
    val editState: ReviewEditUiState = ReviewEditUiState(),
    val showDeleteConfirm: Boolean = false,

    // 리뷰 신고 — 최상위 reports 컬렉션에 생성만 하는 단방향 신고.
    val reportTargetReview: PlaceReview? = null,
    val reportReason: String = "",
    val isSubmittingReport: Boolean = false,

    // 작성자 차단 — users/{uid}/blockedAuthors에 UPSERT, 확인 다이얼로그 대상.
    val blockTargetReview: PlaceReview? = null,
)

/** 작성/수정 폼 하나가 갖는 상태. */
data class ReviewEditUiState(
    /** 0이면 아직 별점을 고르지 않은 상태 — 저장 버튼 활성화 기준. */
    val rating: Int = 0,
    val comment: String = "",
    /** 수정 중 그대로 남긴 기존 사진 URL. */
    val existingPhotoUrls: List<String> = emptyList(),
    /** 이번에 갤러리에서 새로 고른 로컬 사진(저장 시 업로드). */
    val newPhotoUris: List<Uri> = emptyList(),
    /** 수정 중 사용자가 뺀 기존 사진 URL — 저장 시 Storage에서 삭제한다. */
    val removedPhotoUrls: List<String> = emptyList(),
    val isSaving: Boolean = false,
) {
    val photoCount: Int get() = existingPhotoUrls.size + newPhotoUris.size
    val canAddMorePhotos: Boolean get() = photoCount < MAX_REVIEW_PHOTOS
}
