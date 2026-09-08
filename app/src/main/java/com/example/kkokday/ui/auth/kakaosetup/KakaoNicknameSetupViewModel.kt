package com.example.kkokday.ui.auth.kakaosetup

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.nickname.NicknameRepository
import com.example.kkokday.data.nickname.NicknameTakenException
import com.example.kkokday.data.session.KakaoSession
import com.example.kkokday.data.session.KakaoSessionRepository
import com.example.kkokday.navigation.KkokDayRoute
import com.example.kkokday.ui.auth.components.NicknameCheckState
import com.example.kkokday.ui.auth.validation.isValidNickname
import com.example.kkokday.ui.auth.validation.nicknameErrorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "KakaoNicknameSetupVM"

@HiltViewModel
class KakaoNicknameSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val nicknameRepository: NicknameRepository,
    private val kakaoSessionRepository: KakaoSessionRepository,
) : ViewModel() {

    private val kakaoId: Long = checkNotNull(savedStateHandle[KkokDayRoute.KAKAO_NICKNAME_SETUP_ID_ARG])
    private val profileImageUrl: String? = savedStateHandle
        .get<String>(KkokDayRoute.KAKAO_NICKNAME_SETUP_PROFILE_IMAGE_ARG)
        ?.takeIf { it.isNotBlank() }
    private val suggestedNickname: String = savedStateHandle
        .get<String>(KkokDayRoute.KAKAO_NICKNAME_SETUP_NICKNAME_ARG)
        ?.takeIf { it.isNotBlank() }
        .orEmpty()

    private val _uiState = MutableStateFlow(
        KakaoNicknameSetupUiState(
            nickname = suggestedNickname,
            nicknameError = nicknameErrorOrNull(suggestedNickname),
        ),
    )
    val uiState: StateFlow<KakaoNicknameSetupUiState> = _uiState.asStateFlow()

    init {
        Log.d(
            TAG,
            "받은 nav 인자 — kakaoId=$kakaoId, suggestedNickname=[$suggestedNickname], profileImageUrl=[$profileImageUrl]",
        )
    }

    fun onNicknameChange(value: String) {
        if (value.length > 10) return
        _uiState.update {
            it.copy(
                nickname = value,
                nicknameError = nicknameErrorOrNull(value),
                // 닉네임을 바꾸면 이전 중복확인 결과는 더 이상 유효하지 않으므로 초기화한다.
                nicknameCheckState = NicknameCheckState.NOT_CHECKED,
                generalError = null,
            )
        }
    }

    fun onCheckNicknameClick() {
        val nickname = _uiState.value.nickname.trim()
        if (!isValidNickname(nickname)) return
        if (_uiState.value.nicknameCheckState == NicknameCheckState.CHECKING) return

        viewModelScope.launch {
            _uiState.update { it.copy(nicknameCheckState = NicknameCheckState.CHECKING) }
            nicknameRepository.isNicknameAvailable(nickname)
                .onSuccess { available ->
                    _uiState.update {
                        it.copy(
                            nicknameCheckState = if (available) {
                                NicknameCheckState.AVAILABLE
                            } else {
                                NicknameCheckState.TAKEN
                            },
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(nicknameCheckState = NicknameCheckState.ERROR) }
                }
        }
    }

    fun onSubmitClick() {
        val state = _uiState.value
        if (!state.isFormValid || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val uid = authRepository.currentUserUid()
            if (uid == null) {
                _uiState.update {
                    it.copy(isLoading = false, generalError = "로그인 정보를 찾을 수 없어요. 다시 로그인해주세요.")
                }
                return@launch
            }

            val nickname = state.nickname.trim()
            nicknameRepository.reserveNickname(nickname, uid, profileImageUrl)
                .onSuccess {
                    // KakaoSession의 닉네임 캐시도 실제로 확정된 닉네임으로 맞춰둔다 —
                    // HomeViewModel이 홈 화면 인사말에 이 값을 그대로 쓴다.
                    kakaoSessionRepository.saveSession(
                        KakaoSession(kakaoId = kakaoId, nickname = nickname, profileImageUrl = profileImageUrl),
                    )
                    _uiState.update { it.copy(isLoading = false, setupComplete = true) }
                }
                .onFailure { error ->
                    val isTaken = error is NicknameTakenException
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nicknameCheckState = if (isTaken) NicknameCheckState.TAKEN else it.nicknameCheckState,
                            generalError = if (isTaken) {
                                "방금 다른 사용자가 먼저 사용한 닉네임이에요. 다른 닉네임으로 다시 시도해주세요."
                            } else {
                                "닉네임 설정 중 오류가 발생했어요. 잠시 후 다시 시도해주세요."
                            },
                        )
                    }
                }
        }
    }

    fun consumeSetupComplete() {
        _uiState.update { it.copy(setupComplete = false) }
    }
}
