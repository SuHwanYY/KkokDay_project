package com.example.kkokday.data.user

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val PINNED_COURSE_FIELD = "homePinnedCourseId"
private const val PINNED_VOTE_COURSE_FIELD = "homePinnedVoteCourseId"

@Singleton
class HomePinsRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : HomePinsRepository {

    override suspend fun getHomePins(uid: String): Result<HomePins> = runAuthCatching {
        val snapshot = userDoc(uid).get().await()
        HomePins(
            pinnedCourseId = snapshot.getString(PINNED_COURSE_FIELD),
            pinnedVoteCourseId = snapshot.getString(PINNED_VOTE_COURSE_FIELD),
        )
    }

    override suspend fun setPinnedCourseId(uid: String, courseId: String): Result<Unit> = runAuthCatching {
        // users/{uid} 문서는 이미 존재(가입 시 생성)하지만, merge set을 써서 혹시라도
        // 없는 상태에서도 안전하게 이 필드 하나만 만들거나 덮어쓴다.
        userDoc(uid).set(mapOf(PINNED_COURSE_FIELD to courseId), SetOptions.merge()).await()
    }

    override suspend fun setPinnedVoteCourseId(uid: String, courseId: String): Result<Unit> = runAuthCatching {
        userDoc(uid).set(mapOf(PINNED_VOTE_COURSE_FIELD to courseId), SetOptions.merge()).await()
    }

    private fun userDoc(uid: String) = firestore.get().collection(USERS_COLLECTION).document(uid)
}
