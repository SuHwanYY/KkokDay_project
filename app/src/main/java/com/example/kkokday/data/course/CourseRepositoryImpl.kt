package com.example.kkokday.data.course

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

private const val COURSES_COLLECTION = "courses"

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : CourseRepository {

    override suspend fun createCourse(
        ownerId: String,
        title: String,
        description: String,
        places: List<CoursePlace>,
    ): Result<String> = runAuthCatching {
        val docRef = coursesCollection().document()
        val data = mapOf(
            "ownerId" to ownerId,
            "title" to title,
            "description" to description,
            "places" to places.map { it.toFirestoreMap() },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(data).await()
        docRef.id
    }

    override suspend fun addPlacesToCourse(courseId: String, places: List<CoursePlace>): Result<Unit> = runAuthCatching {
        val docRef = coursesCollection().document(courseId)
        val existing = docRef.get().await().toCoursePlaces()
        docRef.update(
            mapOf(
                "places" to (existing + places).map { it.toFirestoreMap() },
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    override suspend fun getMyCourses(ownerId: String): Result<List<Course>> = runAuthCatching {
        coursesCollection()
            .whereEqualTo("ownerId", ownerId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toCourse() }
    }

    override suspend fun getCourse(courseId: String): Result<Course> = runAuthCatching {
        val snapshot = coursesCollection().document(courseId).get().await()
        check(snapshot.exists()) { "존재하지 않는 코스입니다." }
        snapshot.toCourse()
    }

    override suspend fun updateCoursePlaces(courseId: String, places: List<CoursePlace>): Result<Unit> = runAuthCatching {
        coursesCollection().document(courseId).update(
            mapOf(
                "places" to places.map { it.toFirestoreMap() },
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    override suspend fun renameCourse(courseId: String, title: String): Result<Unit> = runAuthCatching {
        coursesCollection().document(courseId).update(
            mapOf(
                "title" to title,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    override suspend fun deleteCourse(courseId: String): Result<Unit> = runAuthCatching {
        coursesCollection().document(courseId).delete().await()
    }

    private fun coursesCollection() = firestore.get().collection(COURSES_COLLECTION)

    private fun CoursePlace.toFirestoreMap() = mapOf(
        "placeName" to placeName,
        "address" to address,
        "latitude" to latitude,
        "longitude" to longitude,
        "kakaoPlaceId" to kakaoPlaceId,
        "category" to category,
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toCoursePlaces(): List<CoursePlace> {
        val rawPlaces = get("places") as? List<Map<String, Any?>> ?: emptyList()
        return rawPlaces.map { raw ->
            CoursePlace(
                placeName = raw["placeName"] as? String ?: "",
                address = raw["address"] as? String ?: "",
                latitude = (raw["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (raw["longitude"] as? Number)?.toDouble() ?: 0.0,
                kakaoPlaceId = raw["kakaoPlaceId"] as? String,
                category = raw["category"] as? String ?: "",
            )
        }
    }

    private fun DocumentSnapshot.toCourse() = Course(
        id = id,
        ownerId = getString("ownerId").orEmpty(),
        title = getString("title").orEmpty(),
        description = getString("description").orEmpty(),
        places = toCoursePlaces(),
        createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
    )
}
