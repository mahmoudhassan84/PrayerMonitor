package com.prayermonitor.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.prayermonitor.app.data.model.PrayerRecord
import com.prayermonitor.app.data.model.PrayerTime
import com.prayermonitor.app.data.model.User

@Database(
    entities = [PrayerTime::class, PrayerRecord::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PrayerDatabase : RoomDatabase() {
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun prayerRecordDao(): PrayerRecordDao
    abstract fun userDao(): UserDao
}
