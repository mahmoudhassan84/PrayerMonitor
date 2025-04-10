package com.prayermonitor.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val calculationMethod: String = "ISNA",
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null
)
