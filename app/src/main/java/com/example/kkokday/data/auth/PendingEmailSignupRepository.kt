package com.example.kkokday.data.auth

import kotlinx.coroutines.flow.Flow

/**
 * 이메일 회원가입 폼에서 입력받았지만 아직 Firestore에 예약하지 않은 닉네임/프로필 사진.
 * 이메일 인증(이 uid를 다시 만날 때까지) 전까지는 [uid]와 함께 기기에만 보관해두고,
 * 인증이 끝난 시점에 [com.example.kkokday.data.nickname.NicknameRepository.reserveNickname]로
 * 실제 예약한다 — [KakaoSession][com.example.kkokday.data.session.KakaoSession]이 카카오
 * 로그인 정보를 로컬에 캐싱하는 것과 같은 패턴.
 */
data class PendingEmailSignup(
    val uid: String,
    val nickname: String,
    val profileImageUrl: String?,
)

interface PendingEmailSignupRepository {

    /** 저장된 임시 가입 정보. 저장한 적 없거나 이미 소비돼 지워졌으면 null을 방출한다. */
    val pendingSignup: Flow<PendingEmailSignup?>

    suspend fun save(pending: PendingEmailSignup)

    suspend fun clear()
}
