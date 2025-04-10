package com.prayermonitor.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface AlAdhanApi {
    @GET("timingsByCity")
    fun getPrayerTimesByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 2 // ISNA method by default
    ): Call<PrayerTimeResponse>
    
    @GET("timingsByLatLong")
    fun getPrayerTimesByCoordinates(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2 // ISNA method by default
    ): Call<PrayerTimeResponse>
}

data class PrayerTimeResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: PrayerTimings,
    val date: DateInfo
)

data class PrayerTimings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

data class DateInfo(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

data class HijriDate(
    val date: String,
    val month: Month,
    val year: String
)

data class GregorianDate(
    val date: String,
    val month: Month,
    val year: String
)

data class Month(
    val number: Int,
    val en: String
)
