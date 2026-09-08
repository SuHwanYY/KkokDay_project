package com.example.kkokday.data.course

interface CourseRepository {

    /** 새 코스를 만들고 새로 생성된 courseId를 반환한다. */
    suspend fun createCourse(
        ownerId: String,
        title: String,
        description: String,
        places: List<CoursePlace>,
    ): Result<String>

    /** 기존 코스 끝에 [places]를 이어붙인다. */
    suspend fun addPlacesToCourse(courseId: String, places: List<CoursePlace>): Result<Unit>

    /** updatedAt 내림차순(최근 수정순) 본인 코스 전체. */
    suspend fun getMyCourses(ownerId: String): Result<List<Course>>

    /** courseId 하나 조회 — 소유자가 아니어도(공유 링크로 접근한 비로그인 사용자 포함) 읽을 수 있다. */
    suspend fun getCourse(courseId: String): Result<Course>

    /** 드래그 재정렬/장소 삭제 후 places 배열 전체를 덮어쓴다. */
    suspend fun updateCoursePlaces(courseId: String, places: List<CoursePlace>): Result<Unit>

    suspend fun renameCourse(courseId: String, title: String): Result<Unit>

    suspend fun deleteCourse(courseId: String): Result<Unit>
}
