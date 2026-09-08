package com.example.kkokday.data.session

import kotlinx.coroutines.flow.Flow

data class KakaoSession(
    val kakaoId: Long,
    val nickname: String?,
    val profileImageUrl: String?,
)

interface KakaoSessionRepository {

    /** 저장된 카카오 세션. 로그인된 적이 없으면 null을 방출한다. */
    val kakaoSession: Flow<KakaoSession?>

    suspend fun saveSession(session: KakaoSession)

    suspend fun clearSession()
}
