package com.example.kkokday.ui.vote

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.data.course.CourseRepository
import com.example.kkokday.data.course.CourseVote
import com.example.kkokday.data.course.CourseVoteEvent
import com.example.kkokday.data.course.CourseVoteRepository
import com.example.kkokday.data.course.sanitizeVoterId
import com.example.kkokday.data.course.voteCandidateId
import com.example.kkokday.navigation.KkokDayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "VoteDetailViewModel"

@HiltViewModel
class VoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
    private val courseVoteRepository: CourseVoteRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle[KkokDayRoute.VOTE_DETAIL_ID_ARG])

    private var candidatePlaces: List<CoursePlace> = emptyList()
    private var votes: List<CourseVote> = emptyList()

    private val _uiState = MutableStateFlow(
        VoteDetailUiState(courseId = courseId, voterName = authRepository.currentUserNickname().orEmpty()),
    )
    val uiState: StateFlow<VoteDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourse()
        observeVotes()
    }

    private fun loadCourse() {
        viewModelScope.launch {
            courseRepository.getCourse(courseId)
                .onSuccess { course ->
                    candidatePlaces = course.places
                    _uiState.update { it.copy(courseTitle = course.title, isLoadingCourse = false) }
                    rebuildCandidates()
                }
                .onFailure { error ->
                    Log.w(TAG, "코스 조회 실패", error)
                    _uiState.update { it.copy(isLoadingCourse = false, errorMessage = "코스를 불러오지 못했어요.") }
                }
        }
    }

    private fun observeVotes() {
        viewModelScope.launch {
            courseVoteRepository.observeVotes(courseId).collect { event ->
                when (event) {
                    is CourseVoteEvent.Updated -> {
                        votes = event.votes
                        rebuildCandidates()
                    }
                    is CourseVoteEvent.Failed -> {
                        Log.w(TAG, "투표 현황 구독 실패", event.error)
                        _uiState.update { it.copy(errorMessage = "투표 현황을 불러오지 못했어요.") }
                    }
                }
            }
        }
    }

    /** 후보 목록/득표 현황을 다시 계산한다. 열려 있는 투표자 목록 바텀시트가 있으면 그 내용도 같이 갱신한다. */
    private fun rebuildCandidates() {
        val candidates = candidatePlaces.map { place ->
            val id = place.voteCandidateId()
            val voters = votes.filter { id in it.candidateIds }
            VoteCandidateUiModel(
                candidateId = id,
                place = place,
                voteCount = voters.size,
                voterNames = voters.map { it.voterName },
            )
        }
        _uiState.update { state ->
            state.copy(
                candidates = candidates,
                totalVoters = votes.size,
                voterListCandidate = state.voterListCandidate?.let { open ->
                    candidates.firstOrNull { it.candidateId == open.candidateId }
                },
            )
        }
    }

    /** 현황 화면에서 후보를 탭하면 그 후보에 투표한 사람 목록 바텀시트를 연다. */
    fun onCandidateRowClick(candidate: VoteCandidateUiModel) {
        if (_uiState.value.isVotingMode) return
        _uiState.update { it.copy(voterListCandidate = candidate) }
    }

    fun onDismissVoterList() {
        _uiState.update { it.copy(voterListCandidate = null) }
    }

    /** "투표하기" — 지금 이름으로 이미 투표한 게 있으면 그 선택을 미리 체크한 채 투표 화면으로 전환한다. */
    fun onStartVotingClick() {
        val existing = votes.firstOrNull { it.voterName == sanitizeVoterId(_uiState.value.voterName) }
        _uiState.update {
            it.copy(
                isVotingMode = true,
                selectedCandidateIds = existing?.candidateIds?.toSet().orEmpty(),
            )
        }
    }

    /** 투표 화면에서 제출하지 않고 현황 화면으로 돌아간다 — 선택 상태는 버려진다. */
    fun onCancelVotingClick() {
        _uiState.update { it.copy(isVotingMode = false, selectedCandidateIds = emptySet()) }
    }

    fun onCandidateToggled(candidateId: String) {
        _uiState.update { state ->
            val current = state.selectedCandidateIds
            state.copy(
                selectedCandidateIds = if (candidateId in current) current - candidateId else current + candidateId,
            )
        }
    }

    fun onVoterNameChange(name: String) {
        _uiState.update { it.copy(voterName = name) }
    }

    fun onSubmitVoteClick() {
        val state = _uiState.value
        if (state.voterName.isBlank() || state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            courseVoteRepository.castVote(courseId, state.voterName, state.selectedCandidateIds.toList())
                .onSuccess {
                    _uiState.update {
                        it.copy(isSubmitting = false, isVotingMode = false, toastMessage = "투표했어요!")
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "투표 실패", error)
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = "투표에 실패했어요. 다시 시도해주세요.")
                    }
                }
        }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
