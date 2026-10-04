package com.example.kkokday.data.auth

/** [FirstSignupCompletionRepository.completeIfNeeded]의 결과. */
sealed interface FirstSignupCompletionResult {

    /** users/{uid} 프로필이 이미 있다 — 가입은 예전에 끝났으므로 더 할 일이 없다. */
    data object AlreadyCompleted : FirstSignupCompletionResult

    /** 로컬에 캐시해둔 닉네임으로 자동 예약까지 방금 끝났다. */
    data class Completed(val nickname: String) : FirstSignupCompletionResult

    /**
     * 예약할 닉네임을 자동으로 정할 수 없다 — 로컬에 캐시된 닉네임이 없거나(다른 기기에서
     * 인증했거나 앱 데이터가 지워진 경우), 있었지만 그 사이 다른 사람이 먼저 선점했다.
     * 호출부는 [nicknameSuggestion]을 미리 채운 닉네임 설정 화면으로 보내야 한다.
     */
    data class NeedsNicknameSetup(
        val nicknameSuggestion: String?,
        val profileImageUrl: String?,
    ) : FirstSignupCompletionResult

    /** 네트워크 오류 등으로 확인/예약 자체에 실패했다 — 사용자에게 에러를 보여주고 나중에 다시 시도하게 한다. */
    data class Failed(val message: String) : FirstSignupCompletionResult
}

/**
 * 이메일 인증(또는 카카오 로그인)으로 Firebase Auth 인증은 끝났지만 아직 users/{uid} 프로필이
 * 없는 "최초 가입 미완료" 상태를 마무리 처리한다. 이메일 회원가입 폼 제출 시에는 계정 생성 +
 * 인증 메일 발송까지만 하고 [PendingEmailSignupRepository]에 닉네임을 로컬로만 캐시해두는데,
 * 그 이유는 인증 메일을 끝까지 누르지 않고 이탈한 사용자의 닉네임이 nicknames/{nickname}에
 * 영구 점유되는 걸 막기 위해서다 — 실제 예약은 인증이 확인된 이 시점에야 이뤄진다.
 *
 * 이메일 인증 확인(EmailVerificationViewModel)과 이메일 로그인(LoginViewModel) 양쪽이 같은
 * 진입점을 공유한다.
 */
interface FirstSignupCompletionRepository {
    suspend fun completeIfNeeded(uid: String): FirstSignupCompletionResult
}
