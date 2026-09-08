package com.example.kkokday.directions

import com.example.kkokday.data.course.CoursePlace
import com.example.kkokday.data.place.CategoryPlace
import com.example.kkokday.data.place.KakaoPlace

/** 길찾기 딥링크를 만드는 데 필요한 최소 정보(장소 이름 + 좌표). */
data class NavigationDestination(
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
)

fun CategoryPlace.toNavigationDestination() = NavigationDestination(
    placeName = placeName,
    latitude = latitude,
    longitude = longitude,
)

fun KakaoPlace.toNavigationDestination() = NavigationDestination(
    placeName = placeName,
    latitude = latitude,
    longitude = longitude,
)

fun CoursePlace.toNavigationDestination() = NavigationDestination(
    placeName = placeName,
    latitude = latitude,
    longitude = longitude,
)
