package com.example.kkokday.data.place

/** 리뷰가 전혀 없는 장소가 수렴하는 기준 평점. */
private const val PRIOR_MEAN_RATING = 3.5

/**
 * "리뷰 몇 개까지는 아직 못 믿는다"는 확신 강도 — 이 값이 클수록 리뷰가 적은 곳의 점수가
 * [PRIOR_MEAN_RATING] 쪽으로 더 세게 당겨진다. IMDB 가중평균 공식에서 흔히 쓰는 완만한
 * 값(5)을 그대로 채택했다.
 */
private const val PRIOR_CONFIDENCE_REVIEWS = 5.0

/**
 * 베이지안 가중평균 인기 점수(IMDB 공식과 동일한 형태): `(v/(v+m))*R + (m/(v+m))*C`.
 * 리뷰가 없는 장소(v=0)는 항상 [PRIOR_MEAN_RATING]으로 수렴해 서로 동점 처리되고,
 * [sortedByPopularity]가 그 상태에서 거리로 한 번 더 정렬하므로 리뷰 데이터가 없는 지금은
 * 사실상 거리순과 같게 동작한다 — 리뷰가 쌓일수록 실제 평점이 점점 더 크게 반영된다.
 */
fun popularityScore(avgRating: Double, reviewCount: Int): Double {
    val v = reviewCount.toDouble()
    val m = PRIOR_CONFIDENCE_REVIEWS
    return (v / (v + m)) * avgRating + (m / (v + m)) * PRIOR_MEAN_RATING
}

/** 인기순 정렬 — 점수가 같으면(리뷰가 없는 장소들끼리) 가까운 곳을 우선한다. */
fun List<CategoryPlace>.sortedByPopularity(): List<CategoryPlace> = sortedWith(
    compareByDescending<CategoryPlace> { popularityScore(it.avgRating ?: 0.0, it.reviewCount ?: 0) }
        .thenBy { it.distanceMeters },
)
