package com.prayermonitor.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "prayer_records")
data class PrayerRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val prayerName: String,  // Fajr, Dhuhr, Asr, Maghrib, Isha
    val performed: Boolean,
    val onTime: Boolean,
    val inMosque: Boolean,
    val inGroup: Boolean,
    val userId: String
)
