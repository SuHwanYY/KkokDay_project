package com.example.kkokday.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pendingEmailSignupDataStore by preferencesDataStore(name = "pending_email_signup")

private object Keys {
    val UID = stringPreferencesKey("uid")
    val NICKNAME = stringPreferencesKey("nickname")
    val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
}

class PendingEmailSignupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PendingEmailSignupRepository {

    override val pendingSignup: Flow<PendingEmailSignup?> = context.pendingEmailSignupDataStore.data.map { prefs ->
        val uid = prefs[Keys.UID] ?: return@map null
        val nickname = prefs[Keys.NICKNAME] ?: return@map null
        PendingEmailSignup(
            uid = uid,
            nickname = nickname,
            profileImageUrl = prefs[Keys.PROFILE_IMAGE_URL],
        )
    }

    override suspend fun save(pending: PendingEmailSignup) {
        context.pendingEmailSignupDataStore.edit { prefs ->
            prefs[Keys.UID] = pending.uid
            prefs[Keys.NICKNAME] = pending.nickname
            if (pending.profileImageUrl != null) {
                prefs[Keys.PROFILE_IMAGE_URL] = pending.profileImageUrl
            } else {
                prefs.remove(Keys.PROFILE_IMAGE_URL)
            }
        }
    }

    override suspend fun clear() {
        context.pendingEmailSignupDataStore.edit { it.clear() }
    }
}
