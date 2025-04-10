package com.prayermonitor.app.data.repository

import androidx.lifecycle.LiveData
import com.prayermonitor.app.data.database.PrayerRecordDao
import com.prayermonitor.app.data.model.PrayerRecord
import java.util.Calendar
import java.util.Date

class PrayerRecordRepository(private val prayerRecordDao: PrayerRecordDao) {
    
    suspend fun insertPrayerRecord(prayerRecord: PrayerRecord): Long {
        return prayerRecordDao.insert(prayerRecord)
    }
    
    fun getAllPrayerRecordsForUser(userId: String): LiveData<List<PrayerRecord>> {
        return prayerRecordDao.getAllPrayerRecordsForUser(userId)
    }
    
    fun getPrayerRecordsForToday(userId: String): LiveData<List<PrayerRecord>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time
        
        return prayerRecordDao.getPrayerRecordsBetweenDates(userId, startOfDay, endOfDay)
    }
    
    fun getPrayerRecordsForWeek(userId: String): LiveData<List<PrayerRecord>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Go back to the start of the week (assuming Sunday is first day of week)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeek)
        val startOfWeek = calendar.time
        
        // Go to the end of the week
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.time
        
        return prayerRecordDao.getPrayerRecordsBetweenDates(userId, startOfWeek, endOfWeek)
    }
    
    fun getPrayerRecordsForMonth(userId: String): LiveData<List<PrayerRecord>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time
        
        return prayerRecordDao.getPrayerRecordsBetweenDates(userId, startOfMonth, endOfMonth)
    }
    
    fun getPrayerStatistics(userId: String, startDate: Date, endDate: Date): Map<String, LiveData<Int>> {
        return mapOf(
            "performed" to prayerRecordDao.getPerformedPrayerCountBetweenDates(userId, startDate, endDate),
            "onTime" to prayerRecordDao.getOnTimePrayerCountBetweenDates(userId, startDate, endDate),
            "inMosque" to prayerRecordDao.getInMosquePrayerCountBetweenDates(userId, startDate, endDate),
            "inGroup" to prayerRecordDao.getInGroupPrayerCountBetweenDates(userId, startDate, endDate)
        )
    }
}
