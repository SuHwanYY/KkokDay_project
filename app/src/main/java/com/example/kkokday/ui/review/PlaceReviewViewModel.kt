package com.example.kkokday.ui.review

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.report.ReportRepository
import com.example.kkokday.data.review.MAX_REVIEW_PHOTOS
import com.example.kkokday.data.review.PlaceReview
import com.example.kkokday.data.review.PlaceReviewRepository
import com.example.kkokday.data.user.BlockedAuthorRepository
import com.example.kkokday.navigation.KkokDayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PlaceReviewViewModel"

@HiltViewModel
class PlaceReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val placeReviewRepository: PlaceReviewRepository,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val blockedAuthorRepository: BlockedAuthorRepository,
) : ViewModel() {

    private val placeDocId: String = checkNotNull(savedStateHandle[KkokDayRoute.PLACE_REVIEW_DOC_ID_ARG])

    private val _uiState = MutableStateFlow(
        PlaceReviewUiState(
            placeName = savedStateHandle.get<String>(KkokDayRoute.PLACE_REVIEW_NAME_ARG).orEmpty(),
            address = savedStateHandle.get<String>(KkokDayRoute.PLACE_REVIEW_ADDRESS_ARG).orEmpty(),
        ),
    )
    val uiState: StateFlow<PlaceReviewUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val uid = authRepository.currentUserUid()

            val reviewsResult = placeReviewRepository.getReviews(placeDocId)
            val summaryResult = placeReviewRepository.getRatingSummary(placeDocId)

            if (reviewsResult.isFailure || summaryResult.isFailure) {
                Log.w(TAG, "리뷰 조회 실패", reviewsResult.exceptionOrNull() ?: summaryResult.exceptionOrNull())
                _uiState.update { it.copy(isLoading = false, errorMessage = "리뷰를 불러오지 못했어요.") }
                return@launch
            }

            val myReview = uid?.let { placeReviewRepository.getMyReview(placeDocId, it).getOrNull() }
            val summary = summaryResult.getOrNull()
            // 차단은 개인 화면 설정이라 서버 집계(avgRating/reviewCount)는 그대로 두고,
            // 목록에서만 걸러낸다 — 다른 사람 눈엔 여전히 보이는 게 맞는 동작이다.
            val blockedAuthorIds = uid?.let { blockedAuthorRepository.getBlockedAuthorIds(it).getOrDefault(emptySet()) }.orEmpty()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    reviews = reviewsResult.getOrDefault(emptyList()).filterNot { review -> review.authorUid in blockedAuthorIds },
                    avgRating = summary?.avgRating ?: 0.0,
                    reviewCount = summary?.reviewCount ?: 0,
                    myReview = myReview,
                    currentUserUid = uid,
                )
            }
        }
    }

    /** "리뷰 작성"/"내 리뷰 수정" 버튼 — 기존 리뷰가 있으면 그 값으로 폼을 미리 채운다. */
    fun onWriteReviewClick() {
        if (authRepository.currentUserUid() == null) {
            _uiState.update { it.copy(errorMessage = "로그인이 필요해요.") }
            return
        }
        val existing = _uiState.value.myReview
        _uiState.update {
            it.copy(
                showEditSheet = true,
                editState = ReviewEditUiState(
                    rating = existing?.rating ?: 0,
                    comment = existing?.comment.orEmpty(),
                    existingPhotoUrls = existing?.photoUrls.orEmpty(),
                ),
            )
        }
    }

    fun onDismissEditSheet() {
        _uiState.update { it.copy(showEditSheet = false) }
    }

    fun onRatingSelected(rating: Int) {
        _uiState.update { it.copy(editState = it.editState.copy(rating = rating)) }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(editState = it.editState.copy(comment = comment)) }
    }

    /** 남은 슬롯만큼만 잘라서 추가한다 — 기존 유지 사진 + 새 사진 합이 [MAX_REVIEW_PHOTOS]를 넘지 않게. */
    fun onPhotosPicked(uris: List<Uri>) {
        _uiState.update { state ->
            val editState = state.editState
            val remainingSlots = (MAX_REVIEW_PHOTOS - editState.photoCount).coerceAtLeast(0)
            state.copy(editState = editState.copy(newPhotoUris = editState.newPhotoUris + uris.take(remainingSlots)))
        }
    }

    fun onRemoveExistingPhoto(url: String) {
        _uiState.update { state ->
            val editState = state.editState
            state.copy(
                editState = editState.copy(
                    existingPhotoUrls = editState.existingPhotoUrls - url,
                    removedPhotoUrls = editState.removedPhotoUrls + url,
                ),
            )
        }
    }

    fun onRemoveNewPhoto(uri: Uri) {
        _uiState.update { state ->
            state.copy(editState = state.editState.copy(newPhotoUris = state.editState.newPhotoUris - uri))
        }
    }

    fun onSaveReviewClick() {
        val uid = authRepository.currentUserUid() ?: return
        val editState = _uiState.value.editState
        if (editState.rating <= 0) return

        val nickname = authRepository.currentUserNickname().orEmpty()
        _uiState.update { it.copy(editState = it.editState.copy(isSaving = true)) }

        viewModelScope.launch {
            placeReviewRepository.saveReview(
                placeDocId = placeDocId,
                uid = uid,
                authorNickname = nickname,
                rating = editState.rating,
                comment = editState.comment.trim(),
                keepPhotoUrls = editState.existingPhotoUrls,
                newPhotoUris = editState.newPhotoUris,
                removedPhotoUrls = editState.removedPhotoUrls,
                placeName = _uiState.value.placeName,
                address = _uiState.value.address,
            ).onSuccess {
                _uiState.update { it.copy(showEditSheet = false, toastMessage = "리뷰를 저장했어요") }
                refresh()
            }.onFailure { error ->
                Log.w(TAG, "리뷰 저장 실패", error)
                _uiState.update {
                    it.copy(
                        editState = it.editState.copy(isSaving = false),
                        errorMessage = "리뷰 저장에 실패했어요. 다시 시도해주세요.",
                    )
                }
            }
        }
    }

    fun onDeleteReviewClick() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun onDismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun onConfirmDeleteReview() {
        val uid = authRepository.currentUserUid() ?: return
        _uiState.update { it.copy(showDeleteConfirm = false) }
        viewModelScope.launch {
            placeReviewRepository.deleteReview(placeDocId, uid)
                .onSuccess {
                    _uiState.update { it.copy(toastMessage = "리뷰를 삭제했어요") }
                    refresh()
                }
                .onFailure { error ->
                    Log.w(TAG, "리뷰 삭제 실패", error)
                    _uiState.update { it.copy(errorMessage = "리뷰 삭제에 실패했어요. 다시 시도해주세요.") }
                }
        }
    }

    // ── 리뷰 신고 ────────────────────────────────────────────────────────

    fun onReportReviewClick(review: PlaceReview) {
        if (authRepository.currentUserUid() == null) {
            _uiState.update { it.copy(errorMessage = "로그인이 필요해요.") }
            return
        }
        _uiState.update { it.copy(reportTargetReview = review, reportReason = "") }
    }

    fun onReportReasonChange(reason: String) {
        if (reason.length > MAX_REPORT_REASON_LENGTH) return
        _uiState.update { it.copy(reportReason = reason) }
    }

    fun onDismissReportDialog() {
        _uiState.update { it.copy(reportTargetReview = null, reportReason = "") }
    }

    fun onSubmitReport() {
        val target = _uiState.value.reportTargetReview ?: return
        val reason = _uiState.value.reportReason.trim()
        if (reason.isBlank()) return
        val uid = authRepository.currentUserUid() ?: return

        _uiState.update { it.copy(isSubmittingReport = true) }
        viewModelScope.launch {
            reportRepository.reportReview(
                placeDocId = placeDocId,
                reviewAuthorUid = target.authorUid,
                reporterUid = uid,
                reason = reason,
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmittingReport = false,
                        reportTargetReview = null,
                        reportReason = "",
                        toastMessage = "신고가 접수됐어요",
                    )
                }
            }.onFailure { error ->
                Log.w(TAG, "리뷰 신고 실패", error)
                _uiState.update {
                    it.copy(isSubmittingReport = false, errorMessage = "신고 접수에 실패했어요. 다시 시도해주세요.")
                }
            }
        }
    }

    // ── 작성자 차단 ──────────────────────────────────────────────────────

    fun onBlockAuthorClick(review: PlaceReview) {
        if (authRepository.currentUserUid() == null) {
            _uiState.update { it.copy(errorMessage = "로그인이 필요해요.") }
            return
        }
        _uiState.update { it.copy(blockTargetReview = review) }
    }

    fun onDismissBlockConfirm() {
        _uiState.update { it.copy(blockTargetReview = null) }
    }

    fun onConfirmBlockAuthor() {
        val target = _uiState.value.blockTargetReview ?: return
        val uid = authRepository.currentUserUid() ?: return

        _uiState.update { it.copy(blockTargetReview = null) }
        viewModelScope.launch {
            blockedAuthorRepository.blockAuthor(uid, target.authorUid, target.authorNickname)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            reviews = state.reviews.filterNot { review -> review.authorUid == target.authorUid },
                            toastMessage = "이 작성자의 리뷰를 더 이상 보지 않아요",
                        )
                    }
                }
                .onFailure { error ->
                    Log.w(TAG, "작성자 차단 실패", error)
                    _uiState.update { it.copy(errorMessage = "차단에 실패했어요. 다시 시도해주세요.") }
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
