package com.prayermonitor.app.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.prayermonitor.app.R
import com.prayermonitor.app.data.api.AlAdhanApi
import com.prayermonitor.app.data.repository.LocationRepository
import com.prayermonitor.app.data.repository.PrayerTimeRepository
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var locationRepository: LocationRepository
    private lateinit var prayerTimeRepository: PrayerTimeRepository
    
    // Views
    private lateinit var textDate: TextView
    private lateinit var textFajrTime: TextView
    private lateinit var textDhuhrTime: TextView
    private lateinit var textAsrTime: TextView
    private lateinit var textMaghribTime: TextView
    private lateinit var textIshaTime: TextView
    
    // Permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            loadPrayerTimes()
        } else {
            Toast.makeText(requireContext(), "Location permission is required to calculate prayer times", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        
        // Initialize views
        textDate = root.findViewById(R.id.text_date)
        textFajrTime = root.findViewById(R.id.text_fajr_time)
        textDhuhrTime = root.findViewById(R.id.text_dhuhr_time)
        textAsrTime = root.findViewById(R.id.text_asr_time)
        textMaghribTime = root.findViewById(R.id.text_maghrib_time)
        textIshaTime = root.findViewById(R.id.text_isha_time)
        
        // Initialize repositories
        locationRepository = LocationRepository(requireContext())
        
        // Initialize Retrofit and API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val alAdhanApi = retrofit.create(AlAdhanApi::class.java)
        prayerTimeRepository = PrayerTimeRepository(alAdhanApi)
        
        // Set current date
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        textDate.text = dateFormat.format(Date())
        
        // Check location permission
        checkLocationPermission()
        
        return root
    }
    
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadPrayerTimes()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(requireContext(), "Location permission is required to calculate prayer times", Toast.LENGTH_LONG).show()
                requestLocationPermission()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }
    
    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    private fun loadPrayerTimes() {
        lifecycleScope.launch {
            try {
                val location = locationRepository.getCurrentLocation()
                val prayerTime = prayerTimeRepository.getPrayerTimesByLocation(
                    location.latitude,
                    location.longitude
                )
                
                // Update UI with prayer times
                textFajrTime.text = prayerTime.fajrTime
                textDhuhrTime.text = prayerTime.dhuhrTime
                textAsrTime.text = prayerTime.asrTime
                textMaghribTime.text = prayerTime.maghribTime
                textIshaTime.text = prayerTime.ishaTime
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading prayer times: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
