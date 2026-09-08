package com.example.kkokday.data.kakao

import android.content.Context

data class KakaoUserInfo(
    val id: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    /** Cloud Functions에 보내 Firebase 로그인용 커스텀 토큰을 발급받는 데 쓰는 원본 카카오 액세스 토큰. */
    val accessToken: String,
)

/**
 * 이전 카카오 로그인 시도(카카오톡/카카오계정 로그인창)의 콜백이 아직 응답하지 않은 상태에서
 * 새 로그인을 또 시작하려 할 때 던진다. 카카오 SDK는 진행 중인 로그인 콜백을 하나만 추적하므로,
 * 여기서 막지 않고 새 로그인창을 또 띄우면 먼저 뜬 창과 뒤섞여 로그인/동의창이 반복해서 뜨거나
 * 응답이 영영 오지 않게 된다.
 */
class KakaoLoginInProgressException : Exception("이전 카카오 로그인 시도가 아직 진행 중이에요.")

interface KakaoAuthRepository {

    /** 카카오톡 우선, 불가능하면 카카오계정으로 로그인 후 사용자 정보를 받아온다. */
    suspend fun login(context: Context): Result<KakaoUserInfo>

    /** 저장된 토큰이 있고, 만료됐다면 SDK가 자동 갱신을 시도해 유효성을 확인한다. */
    suspend fun isTokenValid(): Boolean

    suspend fun logout(): Result<Unit>
}