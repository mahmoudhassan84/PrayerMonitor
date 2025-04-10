package com.prayermonitor.app

import android.app.Application
import androidx.room.Room
import com.google.firebase.FirebaseApp
import com.prayermonitor.app.data.api.AlAdhanApi
import com.prayermonitor.app.data.database.PrayerDatabase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PrayerMonitorApplication : Application() {

    // Singleton instance of the database
    companion object {
        private var INSTANCE: PrayerDatabase? = null
        
        fun getInstance(application: Application): PrayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application.applicationContext,
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

    // Retrofit instance for API calls
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // AlAdhan API service
    val alAdhanApi: AlAdhanApi by lazy {
        retrofit.create(AlAdhanApi::class.java)
    }
    
    // Database instance
    val database: PrayerDatabase by lazy {
        getInstance(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
