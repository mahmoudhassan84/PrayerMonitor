package com.prayermonitor.app.ui.prayers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayermonitor.app.data.model.PrayerRecord
import kotlinx.coroutines.launch
import java.util.Date

class PrayersViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Prayer Tracking"
    }
    val text: LiveData<String> = _text
    
    private val _prayerRecords = MutableLiveData<List<PrayerRecord>>()
    val prayerRecords: LiveData<List<PrayerRecord>> = _prayerRecords
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    // Temporary storage for prayer records until database is implemented
    private val tempPrayerRecords = mutableListOf<PrayerRecord>()
    
    fun onPrayerRecorded(prayerRecord: PrayerRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Add to temporary storage
                tempPrayerRecords.add(prayerRecord)
                
                // Update LiveData
                _prayerRecords.value = tempPrayerRecords.toList()
                
                // This will be replaced with database insertion in the next step
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadTodaysPrayerRecords(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // This will be replaced with database query in the next step
                // For now, just filter the temporary records for today
                val today = Date()
                val todayRecords = tempPrayerRecords.filter { 
                    isSameDay(it.date, today) && it.userId == userId 
                }
                
                _prayerRecords.value = todayRecords
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
