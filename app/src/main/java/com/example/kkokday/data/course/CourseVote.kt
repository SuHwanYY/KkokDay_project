package com.example.kkokday.data.course

import com.example.kkokday.data.place.placeDocId

/**
 * 코스 투표 한 표. Firestore `courses/{courseId}/votes/{voterId}` 문서를 그대로 옮긴 값.
 * 문서 ID가 [voterName](정제된 값)으로 고정돼 있어 같은 이름으로 다시 투표하면 자동으로
 * 덮어써진다 — 리뷰가 uid로 "장소당 유저당 1개"를 강제하는 것과 같은 원리를, 로그인 없는
 * 투표자에겐 uid가 없으니 입력받은 이름으로 대신한 것.
 */
data class CourseVote(
    val voterName: String,
    /** 중복 투표(다중 선택)를 허용한다 — 후보 여러 개를 동시에 고를 수 있다. */
    val candidateIds: List<String>,
    val updatedAtMillis: Long,
)

/**
 * 코스 후보(장소) 식별자. [com.example.kkokday.data.place.placeDocId]와 동일한 규칙
 * (kakao place id 우선, 없으면 좌표 조합)을 재사용해서, 코스 안 장소 순서가 바뀌거나
 * 다른 장소가 추가/삭제돼도 투표가 엉뚱한 장소를 가리키지 않게 한다(배열 인덱스를
 * 쓰면 이 경우 깨진다).
 */
fun CoursePlace.voteCandidateId(): String = placeDocId(kakaoPlaceId.orEmpty(), latitude, longitude)
