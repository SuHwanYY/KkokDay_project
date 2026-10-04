package com.example.kkokday.data.auth

import com.example.kkokday.data.nickname.NicknameRepository
import com.example.kkokday.data.nickname.NicknameTakenException
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class FirstSignupCompletionRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val nicknameRepository: NicknameRepository,
    private val pendingEmailSignupRepository: PendingEmailSignupRepository,
) : FirstSignupCompletionRepository {

    override suspend fun completeIfNeeded(uid: String): FirstSignupCompletionResult {
        val hasProfile = nicknameRepository.hasUserProfile(uid).getOrElse { error ->
            return FirstSignupCompletionResult.Failed(error.toAuthErrorMessage())
        }
        if (hasProfile) return FirstSignupCompletionResult.AlreadyCompleted

        val pending = pendingEmailSignupRepository.pendingSignup.first()
        if (pending == null || pending.uid != uid) {
            // 로컬 캐시가 없다 — 다른 기기에서 인증 링크를 눌렀거나 앱 데이터가 지워진 경우.
            return FirstSignupCompletionResult.NeedsNicknameSetup(nicknameSuggestion = null, profileImageUrl = null)
        }

        val reserveError = nicknameRepository
            .reserveNickname(pending.nickname, uid, pending.profileImageUrl)
            .exceptionOrNull()

        if (reserveError == null) {
            pendingEmailSignupRepository.clear()
            // AuthRepositoryImpl.signUp이 가입 시점에 이미 같은 닉네임으로 displayName을
            // 설정해두지만, 혹시 그 사이 다른 값으로 바뀌었을 가능성에 대비해 다시 맞춰둔다.
            authRepository.updateDisplayName(pending.nickname)
            return FirstSignupCompletionResult.Completed(pending.nickname)
        }

        if (reserveError is NicknameTakenException) {
            // 이 닉네임은 더 이상 유효한 후보가 아니다 — 지우지 않으면 사용자가 새 닉네임을
            // 골라 설정 화면에서 확정한 뒤에도 다음 로그인 때 이 죽은 캐시를 계속 다시 시도하게 된다.
            pendingEmailSignupRepository.clear()
            return FirstSignupCompletionResult.NeedsNicknameSetup(
                nicknameSuggestion = pending.nickname,
                profileImageUrl = pending.profileImageUrl,
            )
        }

        return FirstSignupCompletionResult.Failed(reserveError.toAuthErrorMessage())
    }
}
