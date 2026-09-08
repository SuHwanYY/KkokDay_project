package com.example.kkokday.data.user

interface HomePinsRepository {

    suspend fun getHomePins(uid: String): Result<HomePins>

    /** 코스 탭 고정 — 투표 탭 고정과 독립적으로 저장된다. */
    suspend fun setPinnedCourseId(uid: String, courseId: String): Result<Unit>

    /** 투표 탭 고정 — 코스 탭 고정과 독립적으로 저장된다. */
    suspend fun setPinnedVoteCourseId(uid: String, courseId: String): Result<Unit>
}
