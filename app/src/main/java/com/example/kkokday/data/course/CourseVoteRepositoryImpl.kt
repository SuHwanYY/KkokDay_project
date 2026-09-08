package com.example.kkokday.data.course

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COURSES_COLLECTION = "courses"
private const val VOTES_COLLECTION = "votes"

/** Firestore 문서 ID 길이를 넉넉히 아끼면서도 사람 이름으로는 충분한 상한. */
private const val MAX_VOTER_NAME_LENGTH = 40

@Singleton
class CourseVoteRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : CourseVoteRepository {

    override fun observeVotes(courseId: String): Flow<CourseVoteEvent> = callbackFlow {
        val registration = votesCollection(courseId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(CourseVoteEvent.Failed(error))
                return@addSnapshotListener
            }
            trySend(CourseVoteEvent.Updated(snapshot?.documents?.map { it.toCourseVote() }.orEmpty()))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getVotes(courseId: String): Result<List<CourseVote>> = runAuthCatching {
        votesCollection(courseId).get().await().documents.map { it.toCourseVote() }
    }

    override suspend fun castVote(courseId: String, voterName: String, candidateIds: List<String>): Result<Unit> =
        runAuthCatching {
            val voterId = sanitizeVoterId(voterName)
            check(isValidVoterId(voterId)) { "올바른 이름을 입력해주세요." }

            val data = mapOf(
                "voterName" to voterId,
                "candidateIds" to candidateIds,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            // 모든 필드를 매번 통째로 다시 쓰므로 merge 없이 그냥 set — 같은 이름으로 다시
            // 투표하면 이전 선택 배열이 자연스럽게 덮어써진다(빈 배열이면 전부 선택 해제).
            votesCollection(courseId).document(voterId).set(data).await()
        }

    private fun votesCollection(courseId: String) =
        firestore.get().collection(COURSES_COLLECTION).document(courseId).collection(VOTES_COLLECTION)

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toCourseVote() = CourseVote(
        voterName = getString("voterName").orEmpty(),
        candidateIds = (get("candidateIds") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time ?: 0L,
    )
}

/**
 * 사람이 입력한 이름을 Firestore 문서 ID로 그대로 쓰기 위한 최소 정제. "/"는 경로
 * 구분자라 통째로 막고, 길이를 넉넉한 상한으로 자른다. course.html(웹 투표 페이지)의
 * `sanitizeVoterId`도 반드시 이 로직과 동일하게 맞춘다 — 다르면 같은 이름인데 앱/웹에서
 * 다른 문서로 갈라져 "같은 이름 덮어쓰기"가 깨진다.
 */
internal fun sanitizeVoterId(rawName: String): String =
    rawName.trim().replace("/", "_").take(MAX_VOTER_NAME_LENGTH)

internal fun isValidVoterId(voterId: String): Boolean =
    voterId.isNotBlank() && voterId != "." && voterId != ".."
