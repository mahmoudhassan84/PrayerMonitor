package com.prayermonitor.app.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayermonitor.app.data.model.PrayerRecord
import com.prayermonitor.app.data.repository.PrayerRecordRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Prayer Statistics"
    }
    val text: LiveData<String> = _text
    
    private val _weeklyStats = MutableLiveData<Map<String, Float>>()
    val weeklyStats: LiveData<Map<String, Float>> = _weeklyStats
    
    private val _monthlyStats = MutableLiveData<Map<String, List<Float>>>()
    val monthlyStats: LiveData<Map<String, List<Float>>> = _monthlyStats
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    fun loadStatistics(
        prayerRecordRepository: PrayerRecordRepository,
        userId: String,
        timeRange: Int,
        prayerType: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get date range based on selected time range
                val (startDate, endDate) = getDateRange(timeRange)
                
                // Get prayer records for the selected date range and prayer type
                val prayerRecords = when (prayerType) {
                    0 -> prayerRecordRepository.getPrayerRecordsBetweenDates(userId, startDate, endDate).value
                    1 -> prayerRecordRepository.getPrayerRecordsByName(userId, "Fajr").value?.filter { it.date in startDate..endDate }
                    2 -> prayerRecordRepository.getPrayerRecordsByName(userId, "Dhuhr").value?.filter { it.date in startDate..endDate }
                    3 -> prayerRecordRepository.getPrayerRecordsByName(userId, "Asr").value?.filter { it.date in startDate..endDate }
                    4 -> prayerRecordRepository.getPrayerRecordsByName(userId, "Maghrib").value?.filter { it.date in startDate..endDate }
                    5 -> prayerRecordRepository.getPrayerRecordsByName(userId, "Isha").value?.filter { it.date in startDate..endDate }
                    else -> emptyList()
                } ?: emptyList()
                
                // Calculate weekly statistics
                calculateWeeklyStats(prayerRecords)
                
                // Calculate monthly statistics
                calculateMonthlyStats(prayerRecords)
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun getDateRange(timeRange: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        when (timeRange) {
            0 -> { // Week
                calendar.add(Calendar.DAY_OF_MONTH, -7)
            }
            1 -> { // Month
                calendar.add(Calendar.MONTH, -1)
            }
            2 -> { // 3 Months
                calendar.add(Calendar.MONTH, -3)
            }
            3 -> { // Year
                calendar.add(Calendar.YEAR, -1)
            }
        }
        
        val startDate = calendar.time
        return Pair(startDate, endDate)
    }
    
    private fun calculateWeeklyStats(records: List<PrayerRecord>) {
        val totalPrayers = records.size.toFloat()
        if (totalPrayers == 0f) {
            _weeklyStats.value = mapOf(
                "performed" to 0f,
                "missed" to 0f
            )
            return
        }
        
        val performed = records.count { it.performed }.toFloat()
        val missed = totalPrayers - performed
        
        _weeklyStats.value = mapOf(
            "performed" to performed,
            "missed" to missed
        )
    }
    
    private fun calculateMonthlyStats(records: List<PrayerRecord>) {
        // Group records by day
        val groupedRecords = records.groupBy { record ->
            val calendar = Calendar.getInstance()
            calendar.time = record.date
            calendar.get(Calendar.DAY_OF_YEAR)
        }
        
        val performedPercentages = mutableListOf<Float>()
        val onTimePercentages = mutableListOf<Float>()
        val inMosquePercentages = mutableListOf<Float>()
        val inGroupPercentages = mutableListOf<Float>()
        
        // Calculate percentages for each day
        groupedRecords.entries.sortedBy { it.key }.forEach { (_, dayRecords) ->
            val totalForDay = dayRecords.size.toFloat()
            if (totalForDay > 0) {
                val performedForDay = dayRecords.count { it.performed }.toFloat()
                val onTimeForDay = dayRecords.count { it.performed && it.onTime }.toFloat()
                val inMosqueForDay = dayRecords.count { it.performed && it.inMosque }.toFloat()
                val inGroupForDay = dayRecords.count { it.performed && it.inGroup }.toFloat()
                
                performedPercentages.add(performedForDay / totalForDay * 100)
                onTimePercentages.add(onTimeForDay / totalForDay * 100)
                inMosquePercentages.add(inMosqueForDay / totalForDay * 100)
                inGroupPercentages.add(inGroupForDay / totalForDay * 100)
            }
        }
        
        _monthlyStats.value = mapOf(
            "performed" to performedPercentages,
            "onTime" to onTimePercentages,
            "inMosque" to inMosquePercentages,
            "inGroup" to inGroupPercentages
        )
    }
}
