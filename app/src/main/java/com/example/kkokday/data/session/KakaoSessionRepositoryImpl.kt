package com.example.kkokday.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.kakaoSessionDataStore by preferencesDataStore(name = "kakao_session")

private object Keys {
    val KAKAO_ID = longPreferencesKey("kakao_id")
    val NICKNAME = stringPreferencesKey("nickname")
    val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
}

class KakaoSessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : KakaoSessionRepository {

    override val kakaoSession: Flow<KakaoSession?> = context.kakaoSessionDataStore.data.map { prefs ->
        val kakaoId = prefs[Keys.KAKAO_ID] ?: return@map null
        KakaoSession(
            kakaoId = kakaoId,
            nickname = prefs[Keys.NICKNAME],
            profileImageUrl = prefs[Keys.PROFILE_IMAGE_URL],
        )
    }

    override suspend fun saveSession(session: KakaoSession) {
        context.kakaoSessionDataStore.edit { prefs ->
            prefs[Keys.KAKAO_ID] = session.kakaoId
            if (session.nickname != null) {
                prefs[Keys.NICKNAME] = session.nickname
            } else {
                prefs.remove(Keys.NICKNAME)
            }
            if (session.profileImageUrl != null) {
                prefs[Keys.PROFILE_IMAGE_URL] = session.profileImageUrl
            } else {
                prefs.remove(Keys.PROFILE_IMAGE_URL)
            }
        }
    }

    override suspend fun clearSession() {
        context.kakaoSessionDataStore.edit { it.clear() }
    }
}
