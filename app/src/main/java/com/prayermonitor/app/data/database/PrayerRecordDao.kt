package com.prayermonitor.app.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prayermonitor.app.data.model.PrayerRecord
import java.util.Date

@Dao
interface PrayerRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prayerRecord: PrayerRecord): Long
    
    @Query("SELECT * FROM prayer_records WHERE userId = :userId ORDER BY date DESC")
    fun getAllPrayerRecordsForUser(userId: String): LiveData<List<PrayerRecord>>
    
    @Query("SELECT * FROM prayer_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getPrayerRecordsBetweenDates(userId: String, startDate: Date, endDate: Date): LiveData<List<PrayerRecord>>
    
    @Query("SELECT * FROM prayer_records WHERE userId = :userId AND prayerName = :prayerName ORDER BY date DESC")
    fun getPrayerRecordsByName(userId: String, prayerName: String): LiveData<List<PrayerRecord>>
    
    @Query("SELECT COUNT(*) FROM prayer_records WHERE userId = :userId AND performed = 1 AND date BETWEEN :startDate AND :endDate")
    fun getPerformedPrayerCountBetweenDates(userId: String, startDate: Date, endDate: Date): LiveData<Int>
    
    @Query("SELECT COUNT(*) FROM prayer_records WHERE userId = :userId AND performed = 1 AND onTime = 1 AND date BETWEEN :startDate AND :endDate")
    fun getOnTimePrayerCountBetweenDates(userId: String, startDate: Date, endDate: Date): LiveData<Int>
    
    @Query("SELECT COUNT(*) FROM prayer_records WHERE userId = :userId AND performed = 1 AND inMosque = 1 AND date BETWEEN :startDate AND :endDate")
    fun getInMosquePrayerCountBetweenDates(userId: String, startDate: Date, endDate: Date): LiveData<Int>
    
    @Query("SELECT COUNT(*) FROM prayer_records WHERE userId = :userId AND performed = 1 AND inGroup = 1 AND date BETWEEN :startDate AND :endDate")
    fun getInGroupPrayerCountBetweenDates(userId: String, startDate: Date, endDate: Date): LiveData<Int>
}
