package com.prayermonitor.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayermonitor.app.data.model.PrayerTime
import com.prayermonitor.app.data.repository.LocationRepository
import com.prayermonitor.app.data.repository.PrayerTimeRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Prayer Times"
    }
    val text: LiveData<String> = _text
    
    private val _prayerTime = MutableLiveData<PrayerTime>()
    val prayerTime: LiveData<PrayerTime> = _prayerTime
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun loadPrayerTimes(locationRepository: LocationRepository, prayerTimeRepository: PrayerTimeRepository) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val location = locationRepository.getCurrentLocation()
                val prayerTime = prayerTimeRepository.getPrayerTimesByLocation(
                    location.latitude,
                    location.longitude
                )
                _prayerTime.value = prayerTime
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
