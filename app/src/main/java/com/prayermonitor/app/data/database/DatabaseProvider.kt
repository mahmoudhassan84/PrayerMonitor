package com.prayermonitor.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prayermonitor.app.data.database.PrayerDatabase

object DatabaseProvider {
    
    @Volatile
    private var INSTANCE: PrayerDatabase? = null
    
    fun getInstance(context: Context): PrayerDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                PrayerDatabase::class.java,
                "prayer_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            INSTANCE = instance
            instance
        }
    }
}
