package com.example.kkokday.data.kakao

interface KakaoFirebaseBridgeRepository {

    /**
     * 카카오 액세스 토큰을 Cloud Functions(verifyKakaoAndMintFirebaseToken)로 보내 검증받고,
     * 발급된 Firebase 커스텀 토큰으로 Firebase Auth에 로그인한다.
     *
     * 카카오 SDK 로그인만으로는 Firebase가 "이 사람이 로그인했다"는 걸 전혀 모르는 상태라
     * request.auth가 필요한 모든 곳(Firestore 보안 규칙, 서버 API 인증)이 동작하지 않는다.
     * 이 함수가 성공해야 비로소 진짜 Firebase Auth 세션이 생긴다.
     */
    suspend fun signInWithKakaoAccessToken(kakaoAccessToken: String): Result<Unit>
}
