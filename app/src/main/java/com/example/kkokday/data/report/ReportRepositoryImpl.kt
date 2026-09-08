package com.example.kkokday.data.report

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

private const val REPORTS_COLLECTION = "reports"
private const val PLACES_COLLECTION = "places"
private const val REVIEWS_COLLECTION = "reviews"

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : ReportRepository {

    override suspend fun reportReview(
        placeDocId: String,
        reviewAuthorUid: String,
        reporterUid: String,
        reason: String,
    ): Result<Unit> = runAuthCatching {
        val data = mapOf(
            "reportedReviewPath" to "$PLACES_COLLECTION/$placeDocId/$REVIEWS_COLLECTION/$reviewAuthorUid",
            "reviewAuthorUid" to reviewAuthorUid,
            "reporterUid" to reporterUid,
            "reason" to reason,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        firestore.get().collection(REPORTS_COLLECTION).add(data).await()
        Unit
    }
}
