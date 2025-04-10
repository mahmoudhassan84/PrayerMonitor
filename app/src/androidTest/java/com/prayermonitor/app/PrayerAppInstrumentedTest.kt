package com.prayermonitor.app

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prayermonitor.app.data.api.AlAdhanApi
import com.prayermonitor.app.data.database.PrayerDatabase
import com.prayermonitor.app.data.model.PrayerRecord
import com.prayermonitor.app.data.model.User
import com.prayermonitor.app.data.repository.AuthRepository
import com.prayermonitor.app.data.repository.PrayerRecordRepository
import com.prayermonitor.app.data.repository.PrayerTimeRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

@RunWith(AndroidJUnit4::class)
class PrayerAppInstrumentedTest {
    
    private lateinit var prayerTimeRepository: PrayerTimeRepository
    private lateinit var prayerRecordRepository: PrayerRecordRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var db: PrayerDatabase
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = PrayerDatabase.getInstance(context)
        
        // Initialize repositories
        val app = context.applicationContext as PrayerMonitorApplication
        prayerTimeRepository = PrayerTimeRepository(app.alAdhanApi)
        prayerRecordRepository = PrayerRecordRepository(db.prayerRecordDao())
        authRepository = AuthRepository(db.userDao())
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
    
    @Test
    fun testPrayerTimeCalculation() {
        try {
            // Test prayer time calculation for a specific location
            val prayerTime = prayerTimeRepository.getPrayerTimesByCoordinates(40.7128, -74.0060)
            
            // Verify that prayer times are not empty
            assert(prayerTime.fajrTime.isNotEmpty())
            assert(prayerTime.dhuhrTime.isNotEmpty())
            assert(prayerTime.asrTime.isNotEmpty())
            assert(prayerTime.maghribTime.isNotEmpty())
            assert(prayerTime.ishaTime.isNotEmpty())
            
            Log.d("TEST", "Prayer times for New York: $prayerTime")
        } catch (e: Exception) {
            Log.e("TEST", "Error testing prayer time calculation: ${e.message}")
            throw e
        }
    }
    
    @Test
    fun testPrayerRecordDatabase() {
        try {
            // Create a test user
            val testUser = User(
                id = "test_user_id",
                email = "test@example.com",
                name = "Test User"
            )
            
            // Insert user into database
            db.userDao().insert(testUser)
            
            // Create a test prayer record
            val testRecord = PrayerRecord(
                date = Date(),
                prayerName = "Fajr",
                performed = true,
                onTime = true,
                inMosque = false,
                inGroup = false,
                userId = testUser.id
            )
            
            // Insert prayer record into database
            val recordId = db.prayerRecordDao().insert(testRecord)
            
            // Verify that record was inserted
            assert(recordId > 0)
            
            // Get prayer records for user
            val records = db.prayerRecordDao().getAllPrayerRecordsForUser(testUser.id).value
            
            // Verify that records contain the inserted record
            assert(records?.isNotEmpty() == true)
            
            Log.d("TEST", "Prayer records for test user: $records")
        } catch (e: Exception) {
            Log.e("TEST", "Error testing prayer record database: ${e.message}")
            throw e
        }
    }
    
    @Test
    fun testUserAuthentication() {
        try {
            // Test user registration
            val email = "test_${System.currentTimeMillis()}@example.com"
            val password = "password123"
            val name = "Test User"
            
            // Register user
            val user = authRepository.register(email, password, name)
            
            // Verify that user was registered
            assert(user.email == email)
            
            // Test user login
            val loggedInUser = authRepository.login(email, password)
            
            // Verify that user was logged in
            assert(loggedInUser.email == email)
            
            // Test user logout
            authRepository.logout()
            
            // Verify that user was logged out
            assert(authRepository.getCurrentUser() == null)
            
            Log.d("TEST", "User authentication test passed")
        } catch (e: Exception) {
            Log.e("TEST", "Error testing user authentication: ${e.message}")
            throw e
        }
    }
}
