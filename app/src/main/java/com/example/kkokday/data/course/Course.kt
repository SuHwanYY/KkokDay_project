package com.example.kkokday.data.course

import androidx.compose.runtime.Immutable

/** 코스에 담긴 장소 한 건. `courses/{courseId}` 문서의 `places` 배열 항목을 그대로 옮긴 값. */
data class CoursePlace(
    val placeName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val kakaoPlaceId: String?,
    val category: String,
)

/**
 * 나만의 코스 한 건. Firestore `courses/{id}` 문서를 그대로 옮긴 값.
 * [places]가 List라 Compose 컴파일러가 기본적으로 unstable로 추론하는데, 이 값은 항상
 * 새로 매핑해서 만들 뿐 제자리에서 변경하지 않으므로 @Immutable로 명시해 리스트 행(예:
 * CourseListScreen)의 불필요한 재구성을 막는다.
 */
@Immutable
data class Course(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    /** 코스 안에서의 순서를 그대로 보존한다(드래그 정렬 시 이 리스트 전체를 다시 쓴다). */
    val places: List<CoursePlace>,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
