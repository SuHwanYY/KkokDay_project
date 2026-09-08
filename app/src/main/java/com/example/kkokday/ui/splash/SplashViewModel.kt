package com.example.kkokday.ui.splash

import androidx.lifecycle.ViewModel
import com.example.kkokday.data.kakao.KakaoAuthRepository
import com.example.kkokday.data.session.KakaoSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

enum class SplashDestination { LOGIN, HOME }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val kakaoAuthRepository: KakaoAuthRepository,
    private val kakaoSessionRepository: KakaoSessionRepository,
) : ViewModel() {

    /** 저장된 카카오 세션이 있고 토큰이 아직 유효하면(SDK가 자동 갱신) 홈으로 바로 보낸다. */
    suspend fun resolveStartDestination(): SplashDestination {
        val session = kakaoSessionRepository.kakaoSession.first() ?: return SplashDestination.LOGIN
        return if (kakaoAuthRepository.isTokenValid()) {
            SplashDestination.HOME
        } else {
            kakaoSessionRepository.clearSession()
            SplashDestination.LOGIN
        }
    }
}
