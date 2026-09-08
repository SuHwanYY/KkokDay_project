package com.example.kkokday.data.report

interface ReportRepository {

    /**
     * 리뷰 신고를 최상위 `reports` 컬렉션에 생성만 한다 — firestore.rules가 create만 허용하고
     * read/update/delete는 전부 막아서, 신고 접수 이후 클라이언트는 이 내용을 다시 볼 수 없다
     * (운영자가 Firebase 콘솔/Admin SDK로만 확인). [reviewAuthorUid]는 신고 대상 리뷰 문서
     * ID이기도 하다 — `PlaceReview` 문서 ID가 authorUid로 고정돼 있기 때문.
     */
    suspend fun reportReview(
        placeDocId: String,
        reviewAuthorUid: String,
        reporterUid: String,
        reason: String,
    ): Result<Unit>
}
