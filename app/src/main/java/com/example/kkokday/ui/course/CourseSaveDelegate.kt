package com.example.kkokday.ui.course

import android.util.Log
import com.example.kkokday.data.auth.AuthRepository
import com.example.kkokday.data.course.Course
import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.data.course.CourseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "CourseSaveDelegate"

/**
 * "코스 만들기" 다중 선택 → 보유 코스에 담기 흐름을 즐겨찾기 탭과 카테고리 검색 결과
 * 화면(둘 다 별도의 [androidx.lifecycle.ViewModel])이 공유하기 위한 헬퍼. 코스 생성/이름
 * 변경/삭제는 코스 탭에서만 하므로 이 흐름은 기존 코스에 담는 것만 다룬다.
 * Hilt ViewModel 두 개가 이 로직을 그대로 상속/공유할 방법이 마땅치 않아, 각 ViewModel이
 * 생성자에서 이미 주입받은 리포지토리로 이 클래스를 직접 만들어 위임하는 방식을 썼다.
 *
 * [updateState]/[getState]로 호출부의 상태 조각(CourseSaveUiState)만 갱신한다 — 이 클래스는
 * 자기 상태를 따로 들고 있지 않는다.
 */
class CourseSaveDelegate(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope,
    private val getState: () -> CourseSaveUiState,
    private val updateState: (transform: (CourseSaveUiState) -> CourseSaveUiState) -> Unit,
) {

    fun onModeToggle() {
        updateState { state -> if (state.selectionMode) CourseSaveUiState() else state.copy(selectionMode = true) }
    }

    fun onSelectToggle(id: String) {
        updateState { state ->
            state.copy(selectedIds = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id)
        }
    }

    /** "담기" 클릭 — 방식을 고르는 단계 없이 바로 보유 코스 목록을 불러와 보여준다. */
    fun onSaveClick() {
        if (getState().selectedIds.isEmpty()) return
        updateState { it.copy(showExistingCoursePicker = true, isLoadingMyCourses = true) }
        scope.launch {
            val uid = authRepository.currentUserUid()
            if (uid == null) {
                updateState { it.copy(isLoadingMyCourses = false) }
                return@launch
            }
            courseRepository.getMyCourses(uid)
                .onSuccess { courses -> updateState { it.copy(myCourses = courses, isLoadingMyCourses = false) } }
                .onFailure { error ->
                    Log.w(TAG, "코스 목록 조회 실패", error)
                    updateState { it.copy(isLoadingMyCourses = false, toastMessage = "코스 목록을 불러오지 못했어요.") }
                }
        }
    }

    fun onDismissExistingCoursePicker() {
        updateState { it.copy(showExistingCoursePicker = false) }
    }

    fun onExistingCourseSelected(course: Course, places: List<CoursePlace>) {
        scope.launch {
            courseRepository.addPlacesToCourse(course.id, places)
                .onSuccess { updateState { CourseSaveUiState(toastMessage = "\"${course.title}\"에 추가했어요") } }
                .onFailure { error ->
                    Log.w(TAG, "코스에 장소 추가 실패", error)
                    updateState { it.copy(toastMessage = "코스에 추가하지 못했어요. 다시 시도해주세요.") }
                }
        }
    }

    fun consumeToastMessage() {
        updateState { it.copy(toastMessage = null) }
    }
}
