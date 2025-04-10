package com.prayermonitor.app.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prayermonitor.app.data.model.PrayerTime
import java.util.Date

@Dao
interface PrayerTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prayerTime: PrayerTime): Long
    
    @Query("SELECT * FROM prayer_times WHERE date = :date")
    fun getPrayerTimeForDate(date: Date): LiveData<PrayerTime>
    
    @Query("SELECT * FROM prayer_times ORDER BY date DESC LIMIT 1")
    fun getLatestPrayerTime(): LiveData<PrayerTime>
    
    @Query("SELECT * FROM prayer_times WHERE date BETWEEN :startDate AND :endDate")
    fun getPrayerTimesBetweenDates(startDate: Date, endDate: Date): LiveData<List<PrayerTime>>
}
