package com.example.kkokday.util

import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 두 좌표 간 거리·도보 소요시간 계산 — 카테고리 장소 목록
 * ([com.example.kkokday.ui.category.CategoryPlaceRow])과 코스 공유 이미지
 * ([com.example.kkokday.ui.course.CourseShareImage])가 공통으로 쓴다.
 */
object WalkingDistance {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /** 성인 평균 도보 속도(시속 약 4km) 기준 분당 이동 거리(m). 값 조정이 필요하면 이 상수만 바꾸면 된다. */
    const val METERS_PER_MINUTE = 67

    /**
     * 두 좌표 간 직선거리(하버사인 공식, m). 카카오 API가 주는 실제 도보 경로 거리가 아닌
     * "직선거리 기준 추정치"라, 실제 도보 이동 거리보다 짧게 나올 수 있다.
     */
    fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (EARTH_RADIUS_METERS * c).roundToInt()
    }

    fun estimateMinutes(distanceMeters: Int): Int =
        ceil(distanceMeters.toDouble() / METERS_PER_MINUTE).toInt().coerceAtLeast(1)
}

fun formatDistance(distanceMeters: Int): String = if (distanceMeters >= 1000) {
    "%.1fkm".format(distanceMeters / 1000.0)
} else {
    "${distanceMeters}m"
}

fun formatWalkingTime(distanceMeters: Int): String = "도보 ${WalkingDistance.estimateMinutes(distanceMeters)}분"
