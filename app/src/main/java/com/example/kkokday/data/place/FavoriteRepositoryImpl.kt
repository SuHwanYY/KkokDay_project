package com.example.kkokday.data.place

import com.example.kkokday.data.auth.runAuthCatching
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val FAVORITES_COLLECTION = "favorites"

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val firestore: Lazy<FirebaseFirestore>,
) : FavoriteRepository {

    override suspend fun getFavoriteIds(uid: String): Result<Set<String>> = runAuthCatching {
        favoritesCollection(uid).get().await().documents.map { it.id }.toSet()
    }

    override suspend fun getFavorites(uid: String): Result<List<Favorite>> = runAuthCatching {
        favoritesCollection(uid)
            .orderBy("savedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toFavorite() }
    }

    override suspend fun addFavorite(uid: String, place: CategoryPlace): Result<Unit> = runAuthCatching {
        val data = mapOf(
            "placeName" to place.placeName,
            "categoryName" to place.categoryName,
            "address" to place.address,
            "latitude" to place.latitude,
            "longitude" to place.longitude,
            "phone" to place.phone,
            "placeUrl" to place.placeUrl,
            "kakaoPlaceId" to place.id.takeIf { it.isNotBlank() },
            "savedAt" to FieldValue.serverTimestamp(),
        )
        favoritesCollection(uid).document(place.favoriteDocId()).set(data, SetOptions.merge()).await()
        Unit
    }

    override suspend fun removeFavorite(uid: String, docId: String): Result<Unit> = runAuthCatching {
        favoritesCollection(uid).document(docId).delete().await()
    }

    private fun favoritesCollection(uid: String) = firestore.get()
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(FAVORITES_COLLECTION)

    private fun DocumentSnapshot.toFavorite() = Favorite(
        docId = id,
        placeName = getString("placeName").orEmpty(),
        categoryName = getString("categoryName").orEmpty(),
        address = getString("address").orEmpty(),
        latitude = getDouble("latitude") ?: 0.0,
        longitude = getDouble("longitude") ?: 0.0,
        phone = getString("phone").orEmpty(),
        placeUrl = getString("placeUrl").orEmpty(),
        kakaoPlaceId = getString("kakaoPlaceId"),
        savedAtMillis = getTimestamp("savedAt")?.toDate()?.time ?: System.currentTimeMillis(),
    )
}
