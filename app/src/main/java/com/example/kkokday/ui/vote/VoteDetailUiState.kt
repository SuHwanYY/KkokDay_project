package com.example.kkokday.ui.vote

import androidx.compose.runtime.Immutable
import com.example.kkokday.data.course.CoursePlace

data class VoteDetailUiState(
    val courseId: String = "",
    val courseTitle: String = "",
    /** 최신 실시간 스냅샷 기준 후보(장소) + 득표 요약. 결과 화면/투표 화면이 공유한다. */
    val candidates: List<VoteCandidateUiModel> = emptyList(),
    /** 득표 수가 아니라 투표에 참여한 사람 수(중복 선택을 허용하므로 둘이 다르다). */
    val totalVoters: Int = 0,
    /** 로그인 유저는 닉네임으로 미리 채워지고, 그 값 그대로 또는 수정해서 투표한다. */
    val voterName: String = "",
    val isLoadingCourse: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val toastMessage: String? = null,
    /**
     * false면 "현황 화면"(득표 수만 보여주고 후보를 탭하면 투표자 목록), true면 "투표
     * 화면"(체크박스 다중 선택). 진입 시 항상 false로 시작한다.
     */
    val isVotingMode: Boolean = false,
    /** 투표 화면에서만 쓰는 체크 상태 — "투표하기" 진입 시 기존 투표로 미리 채워진다. */
    val selectedCandidateIds: Set<String> = emptySet(),
    /** 현황 화면에서 후보를 탭했을 때 보여줄 바텀시트 대상. null이면 안 뜬다. */
    val voterListCandidate: VoteCandidateUiModel? = null,
)

/**
 * 후보(코스에 담긴 장소) 하나 + 실시간 득표 현황. [voterNames]가 List라 Compose
 * 컴파일러가 기본 unstable로 추론하는데, 항상 새로 매핑해서 만들 뿐 제자리에서 변경하지
 * 않으므로 @Immutable로 명시해 후보 리스트 행의 불필요한 재구성을 막는다.
 */
@Immutable
data class VoteCandidateUiModel(
    val candidateId: String,
    val place: CoursePlace,
    val voteCount: Int,
    val voterNames: List<String> = emptyList(),
)
