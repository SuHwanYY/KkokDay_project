package com.example.kkokday.data.location

import com.example.kkokday.data.place.KakaoPlace
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SelectedLocationRepositoryImpl @Inject constructor() : SelectedLocationRepository {

    private val _selectedLocation = MutableStateFlow<KakaoPlace?>(null)
    override val selectedLocation: StateFlow<KakaoPlace?> = _selectedLocation.asStateFlow()

    override fun setSelectedLocation(place: KakaoPlace) {
        _selectedLocation.value = place
    }
}
