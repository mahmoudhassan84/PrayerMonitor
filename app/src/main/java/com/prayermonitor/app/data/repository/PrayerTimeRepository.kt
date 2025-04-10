package com.prayermonitor.app.data.repository

import android.location.Location
import com.prayermonitor.app.data.api.AlAdhanApi
import com.prayermonitor.app.data.api.PrayerTimeResponse
import com.prayermonitor.app.data.model.PrayerTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PrayerTimeRepository(private val alAdhanApi: AlAdhanApi) {
    
    suspend fun getPrayerTimesByLocation(latitude: Double, longitude: Double, method: Int = 2): PrayerTime {
        return suspendCoroutine { continuation ->
            alAdhanApi.getPrayerTimesByCoordinates(latitude, longitude, method).enqueue(object : Callback<PrayerTimeResponse> {
                override fun onResponse(call: Call<PrayerTimeResponse>, response: Response<PrayerTimeResponse>) {
                    if (response.isSuccessful) {
                        val prayerTimeResponse = response.body()
                        if (prayerTimeResponse != null) {
                            val timings = prayerTimeResponse.data.timings
                            val date = Date() // Current date
                            
                            val prayerTime = PrayerTime(
                                date = date,
                                fajrTime = timings.Fajr,
                                dhuhrTime = timings.Dhuhr,
                                asrTime = timings.Asr,
                                maghribTime = timings.Maghrib,
                                ishaTime = timings.Isha,
                                latitude = latitude,
                                longitude = longitude,
                                calculationMethod = method.toString()
                            )
                            continuation.resume(prayerTime)
                        } else {
                            continuation.resumeWithException(Exception("Empty response body"))
                        }
                    } else {
                        continuation.resumeWithException(Exception("API call failed with code: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<PrayerTimeResponse>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
    
    suspend fun getPrayerTimesByCity(city: String, country: String, method: Int = 2): PrayerTime {
        return suspendCoroutine { continuation ->
            alAdhanApi.getPrayerTimesByCity(city, country, method).enqueue(object : Callback<PrayerTimeResponse> {
                override fun onResponse(call: Call<PrayerTimeResponse>, response: Response<PrayerTimeResponse>) {
                    if (response.isSuccessful) {
                        val prayerTimeResponse = response.body()
                        if (prayerTimeResponse != null) {
                            val timings = prayerTimeResponse.data.timings
                            val date = Date() // Current date
                            
                            val prayerTime = PrayerTime(
                                date = date,
                                fajrTime = timings.Fajr,
                                dhuhrTime = timings.Dhuhr,
                                asrTime = timings.Asr,
                                maghribTime = timings.Maghrib,
                                ishaTime = timings.Isha,
                                latitude = 0.0, // We don't have coordinates when searching by city
                                longitude = 0.0,
                                calculationMethod = method.toString()
                            )
                            continuation.resume(prayerTime)
                        } else {
                            continuation.resumeWithException(Exception("Empty response body"))
                        }
                    } else {
                        continuation.resumeWithException(Exception("API call failed with code: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<PrayerTimeResponse>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}
