package com.example.kkokday.data.kakao

interface KakaoUserRepository {

    /**
     * users/{"kakao:"+kakaoId} 문서가 이미 있으면 저장된 닉네임을, 없으면(최초 로그인) null을
     * 반환한다. null이면 호출부가 닉네임 설정 화면으로 보내 [NicknameRepository][com.example.kkokday.data.nickname.NicknameRepository]로
     * 직접 예약하게 한다 — 이 저장소는 더 이상 닉네임을 자동으로 정하지 않는다.
     */
    suspend fun findExistingNickname(kakaoId: Long): Result<String?>

    /** 이미 프로필이 있는 사용자(재로그인)의 프로필 사진만 최신화한다. 닉네임은 건드리지 않는다. */
    suspend fun refreshProfileImage(kakaoId: Long, profileImageUrl: String?): Result<Unit>
}
