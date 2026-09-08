package com.example.kkokday.data.location

import com.example.kkokday.data.place.KakaoPlace
import kotlinx.coroutines.flow.StateFlow

interface SelectedLocationRepository {

    /** 사용자가 위치 지정 화면에서 확정한 장소. 아직 아무것도 지정하지 않았으면 null. */
    val selectedLocation: StateFlow<KakaoPlace?>

    fun setSelectedLocation(place: KakaoPlace)
}
