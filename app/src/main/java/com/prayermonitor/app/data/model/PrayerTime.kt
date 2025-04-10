package com.prayermonitor.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "prayer_times")
data class PrayerTime(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val fajrTime: String,
    val dhuhrTime: String,
    val asrTime: String,
    val maghribTime: String,
    val ishaTime: String,
    val latitude: Double,
    val longitude: Double,
    val calculationMethod: String
)
