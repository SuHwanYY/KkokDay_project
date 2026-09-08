package com.example.kkokday.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.course.CourseRepository
import com.example.kkokday.data.course.CourseVoteRepository
import com.example.kkokday.data.course.voteCandidateId
import com.example.kkokday.data.location.SelectedLocationRepository
import com.example.kkokday.data.place.KakaoPlace
import com.example.kkokday.data.place.RecentPlace
import com.example.kkokday.data.place.RecentPlaceRepository
import com.example.kkokday.data.session.KakaoSessionRepository
import com.example.kkokday.data.user.HomePinsRepository
import com.example.kkokday.ui.main.model.OngoingCourseCardData
import com.example.kkokday.ui.main.model.OngoingSectionTab
import com.example.kkokday.ui.main.model.OngoingVoteCardData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kakaoSessionRepository: KakaoSessionRepository,
    private val selectedLocationRepository: SelectedLocationRepository,
    private val recentPlaceRepository: RecentPlaceRepository,
    private val courseRepository: CourseRepository,
    private val courseVoteRepository: CourseVoteRepository,
    private val homePinsRepository: HomePinsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val kakaoSession = kakaoSessionRepository.kakaoSession.first()
            _uiState.update {
                if (kakaoSession != null) {
                    it.copy(
                        nickname = kakaoSession.nickname,
                        subtitle = "카카오 계정으로 로그인했어요",
                        isKakaoSession = true,
                    )
                } else {
                    it.copy(
                        nickname = authRepository.currentUserNickname(),
                        subtitle = authRepository.currentUserEmail(),
                        isKakaoSession = false,
                    )
                }
            }
        }
        viewModelScope.launch {
            selectedLocationRepository.selectedLocation.collect { place ->
                _uiState.update {
                    it.copy(currentLocation = place)
                }
            }
        }
        refreshRecentPlaces()
        refreshOngoingSection()
        // PlaceSearchViewModel의 fire-and-forget 저장이 실패했을 때, 이미 화면이
        // 전환된 뒤라도 여기서 스낵바로 알려준다. Home이 백스택에 남아있는 한
        // (위치 지정 화면 방문 중에도) viewModelScope가 살아있어 구독이 끊기지 않는다.
        viewModelScope.launch {
            recentPlaceRepository.saveFailureEvents.collect {
                _uiState.update { it.copy(errorMessage = "최근 콕 찍은 곳 저장에 실패했어요.") }
            }
        }
    }

    fun refreshRecentPlaces() {
        viewModelScope.launch {
            val uid = authRepository.currentUserUid() ?: return@launch
            recentPlaceRepository.getRecentPlaces(uid)
                .onSuccess { places -> _uiState.update { it.copy(recentPlaces = places) } }
                .onFailure { error -> Log.w(TAG, "최근 콕 찍은 곳 조회 실패", error) }
        }
    }

    /**
     * "진행 중인 코스/투표" 섹션 전체를 1회성으로 다시 계산한다(실시간 리스너는 쓰지 않는다
     * — 그 정도 즉시성은 상세 화면에 들어갔을 때만 필요하다). 코스 목록과 고정값을 같이
     * 받아 두 토글(코스/투표) 카드를 모두 채운다.
     */
    fun refreshOngoingSection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOngoingLoading = true) }
            val uid = authRepository.currentUserUid()
            if (uid == null) {
                _uiState.update { it.copy(isOngoingLoading = false) }
                return@launch
            }

            val courses = courseRepository.getMyCourses(uid).getOrElse { error ->
                Log.w(TAG, "코스 목록 조회 실패", error)
                _uiState.update { it.copy(isOngoingLoading = false) }
                return@launch
            }
            val pins = homePinsRepository.getHomePins(uid).getOrNull()

            // 고정해둔 코스가 삭제됐거나 아직 한 번도 고정한 적이 없으면 가장 최근 코스로 폴백한다.
            val resolvedCourse = courses.firstOrNull { it.id == pins?.pinnedCourseId } ?: courses.firstOrNull()
            val resolvedVoteCourse = courses.firstOrNull { it.id == pins?.pinnedVoteCourseId } ?: courses.firstOrNull()

            val courseCard = resolvedCourse?.toOngoingCourseCardData()
            val voteCard = resolvedVoteCourse?.let { buildOngoingVoteCardData(it) }

            _uiState.update {
                it.copy(
                    myCourses = courses,
                    pinnedCourseCard = courseCard,
                    pinnedVoteCard = voteCard,
                    isOngoingLoading = false,
                )
            }
        }
    }

    private suspend fun buildOngoingVoteCardData(course: Course): OngoingVoteCardData {
        val votes = courseVoteRepository.getVotes(course.id)
            .onFailure { error -> Log.w(TAG, "투표 현황 조회 실패: courseId=${course.id}", error) }
            .getOrDefault(emptyList())
        val countsByCandidate = votes.flatMap { it.candidateIds }.groupingBy { it }.eachCount()
        val topCandidate = course.places
            .map { place -> place to (countsByCandidate[place.voteCandidateId()] ?: 0) }
            .filter { (_, count) -> count > 0 }
            .maxByOrNull { (_, count) -> count }

        return OngoingVoteCardData(
            courseId = course.id,
            courseTitle = course.title,
            topCandidateName = topCandidate?.first?.placeName,
            topCandidateVoteCount = topCandidate?.second ?: 0,
            participantCount = votes.size,
        )
    }

    private fun Course.toOngoingCourseCardData() =
        OngoingCourseCardData(courseId = id, courseTitle = title, placeCount = places.size)

    fun onOngoingTabSelected(tab: OngoingSectionTab) {
        _uiState.update { it.copy(ongoingTab = tab) }
    }

    fun onChangePinnedCourseClick() {
        _uiState.update { it.copy(showCoursePicker = true) }
    }

    fun onDismissCoursePicker() {
        _uiState.update { it.copy(showCoursePicker = false) }
    }

    /** "변경" 피커에서 코스를 고르면 지금 선택된 토글(코스/투표) 쪽에만 반영하고 서버에 고정값을 저장한다. */
    fun onPinnedCourseSelected(course: Course) {
        val tab = _uiState.value.ongoingTab
        _uiState.update { it.copy(showCoursePicker = false) }

        viewModelScope.launch {
            val uid = authRepository.currentUserUid() ?: return@launch
            when (tab) {
                OngoingSectionTab.COURSE -> {
                    homePinsRepository.setPinnedCourseId(uid, course.id)
                        .onFailure { error -> Log.w(TAG, "코스 고정 저장 실패", error) }
                    _uiState.update { it.copy(pinnedCourseCard = course.toOngoingCourseCardData()) }
                }
                OngoingSectionTab.VOTE -> {
                    homePinsRepository.setPinnedVoteCourseId(uid, course.id)
                        .onFailure { error -> Log.w(TAG, "투표 코스 고정 저장 실패", error) }
                    val card = buildOngoingVoteCardData(course)
                    _uiState.update { it.copy(pinnedVoteCard = card) }
                }
            }
        }
    }

    fun onRecentPlaceDeleteClick(place: RecentPlace) {
        _uiState.update { it.copy(pendingDeletePlace = place) }
    }

    fun dismissDeleteRecentPlace() {
        _uiState.update { it.copy(pendingDeletePlace = null) }
    }

    fun onRecentPlaceClick(place: RecentPlace) {
        _uiState.update { it.copy(pendingSelectPlace = place) }
    }

    fun dismissSelectRecentPlace() {
        _uiState.update { it.copy(pendingSelectPlace = null) }
    }

    /**
     * 카드를 탭해 같은 장소를 다시 콕 찍은 경우 — 위치 지정 화면에서 확정할 때와
     * 동일하게 현재 위치를 바꾸고, recentPlaces도 UPSERT해 selectedAt을 갱신한다.
     * saveRecentPlace는 fire-and-forget이라 목록 순서가 서버 응답을 기다리지 않고
     * 바로 반영되도록 로컬에서 먼저 맨 위로 낙관적으로 재정렬한다.
     */
    fun confirmSelectRecentPlace() {
        val place = _uiState.value.pendingSelectPlace ?: return
        _uiState.update { it.copy(pendingSelectPlace = null) }

        val kakaoPlace = place.toKakaoPlace()
        selectedLocationRepository.setSelectedLocation(kakaoPlace)

        _uiState.update { state ->
            val reordered = listOf(place.copy(selectedAtMillis = System.currentTimeMillis())) +
                state.recentPlaces.filterNot { it.docId == place.docId }
            state.copy(recentPlaces = reordered)
        }

        authRepository.currentUserUid()?.let { uid ->
            recentPlaceRepository.saveRecentPlace(uid, kakaoPlace)
        }
    }

    private fun RecentPlace.toKakaoPlace(): KakaoPlace = KakaoPlace(
        id = kakaoPlaceId ?: docId,
        placeName = placeName,
        addressName = address,
        roadAddressName = null,
        longitude = longitude,
        latitude = latitude,
    )

    fun confirmDeleteRecentPlace() {
        val place = _uiState.value.pendingDeletePlace ?: return
        viewModelScope.launch {
            val uid = authRepository.currentUserUid()
            if (uid == null) {
                _uiState.update { it.copy(pendingDeletePlace = null) }
                return@launch
            }
            recentPlaceRepository.deleteRecentPlace(uid, place.docId)
                .onSuccess { refreshRecentPlaces() }
                .onFailure { error ->
                    Log.w(TAG, "최근 콕 찍은 곳 삭제 실패", error)
                    _uiState.update { it.copy(errorMessage = "삭제에 실패했어요. 다시 시도해주세요.") }
                }
            _uiState.update { it.copy(pendingDeletePlace = null) }
        }
    }

    fun onDeleteAllRecentPlacesClick() {
        if (_uiState.value.recentPlaces.isEmpty()) return
        _uiState.update { it.copy(pendingDeleteAll = true) }
    }

    fun dismissDeleteAllRecentPlaces() {
        _uiState.update { it.copy(pendingDeleteAll = false) }
    }

    fun confirmDeleteAllRecentPlaces() {
        _uiState.update { it.copy(pendingDeleteAll = false) }
        viewModelScope.launch {
            val uid = authRepository.currentUserUid() ?: return@launch
            recentPlaceRepository.deleteAllRecentPlaces(uid)
                .onSuccess { _uiState.update { it.copy(recentPlaces = emptyList()) } }
                .onFailure { error ->
                    Log.w(TAG, "최근 콕 찍은 곳 전체 삭제 실패", error)
                    _uiState.update { it.copy(errorMessage = "전체 삭제에 실패했어요. 다시 시도해주세요.") }
                }
        }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
