package com.prayermonitor.app.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prayermonitor.app.data.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): LiveData<User>
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): LiveData<User>
    
    @Query("UPDATE users SET calculationMethod = :calculationMethod WHERE id = :userId")
    suspend fun updateCalculationMethod(userId: String, calculationMethod: String)
    
    @Query("UPDATE users SET locationLatitude = :latitude, locationLongitude = :longitude WHERE id = :userId")
    suspend fun updateLocation(userId: String, latitude: Double, longitude: Double)
}
