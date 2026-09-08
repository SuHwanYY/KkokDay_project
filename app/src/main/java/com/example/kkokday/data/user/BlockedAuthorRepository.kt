package com.example.kkokday.data.user

interface BlockedAuthorRepository {

    /** 문서 ID(=차단한 작성자 uid) 집합만 모아서 가져온다 — 리뷰 목록 클라이언트 필터링에 이 값만 있으면 된다. */
    suspend fun getBlockedAuthorIds(uid: String): Result<Set<String>>

    /** favorites/recentPlaces와 동일한 UPSERT 패턴 — 이미 차단한 작성자를 다시 차단해도 안전하다. */
    suspend fun blockAuthor(uid: String, authorUid: String, authorNickname: String): Result<Unit>
}
