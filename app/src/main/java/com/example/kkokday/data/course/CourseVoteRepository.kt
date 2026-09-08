package com.example.kkokday.data.course

import kotlinx.coroutines.flow.Flow

/**
 * [CourseVoteRepository.observeVotes]가 흘려보내는 이벤트 —
 * [com.example.kkokday.data.place.CategorySearchEvent]와 같은 패턴.
 */
sealed interface CourseVoteEvent {
    data class Updated(val votes: List<CourseVote>) : CourseVoteEvent
    data class Failed(val error: Throwable) : CourseVoteEvent
}

interface CourseVoteRepository {

    /**
     * `courses/{courseId}/votes` 실시간 구독. 로그인 여부와 무관하게(투표든 구경만 하든)
     * 누구나 볼 수 있다 — 결과는 항상 실시간 공개라는 정책과 일치.
     */
    fun observeVotes(courseId: String): Flow<CourseVoteEvent>

    /**
     * `courses/{courseId}/votes` 1회성 조회. 홈 화면처럼 그 순간의 득표 현황만 필요하고
     * 화면에 머무는 동안 실시간으로 갱신될 필요는 없는 곳에서 쓴다 — 리스너를 계속
     * 띄워두지 않기 위해.
     */
    suspend fun getVotes(courseId: String): Result<List<CourseVote>>

    /**
     * 같은 이름으로 다시 투표하면 기존 투표(선택했던 후보 배열 전체)를 덮어쓴다(UPSERT).
     * [voterName]은 정제 후 Firestore 문서 ID로 그대로 쓰이므로, 공백/슬래시 등은 내부에서
     * 정리한다. [candidateIds]는 빈 리스트(전부 선택 해제)도 허용한다.
     */
    suspend fun castVote(courseId: String, voterName: String, candidateIds: List<String>): Result<Unit>
}
