package com.prayermonitor.app.ui.prayers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prayermonitor.app.PrayerMonitorApplication
import com.prayermonitor.app.R
import com.prayermonitor.app.data.model.PrayerRecord
import com.prayermonitor.app.data.repository.LocationRepository
import com.prayermonitor.app.data.repository.PrayerTimeRepository
import kotlinx.coroutines.launch

class PrayersFragment : Fragment() {

    private lateinit var prayersViewModel: PrayersViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var textPrayers: TextView
    private lateinit var locationRepository: LocationRepository
    private lateinit var prayerTimeRepository: PrayerTimeRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prayersViewModel = ViewModelProvider(this).get(PrayersViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_prayers, container, false)
        
        // Initialize views
        textPrayers = root.findViewById(R.id.text_prayers)
        recyclerView = root.findViewById(R.id.recycler_prayers)
        
        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Initialize repositories
        locationRepository = LocationRepository(requireContext())
        val app = requireActivity().application as PrayerMonitorApplication
        prayerTimeRepository = PrayerTimeRepository(app.alAdhanApi)
        
        // Load prayer times and set up adapter
        loadPrayerTimes()
        
        // Observe ViewModel data
        prayersViewModel.text.observe(viewLifecycleOwner) {
            textPrayers.text = it
        }
        
        return root
    }
    
    private fun loadPrayerTimes() {
        lifecycleScope.launch {
            try {
                val location = locationRepository.getCurrentLocation()
                val prayerTime = prayerTimeRepository.getPrayerTimesByLocation(
                    location.latitude,
                    location.longitude
                )
                
                // Create adapter with prayer times
                val adapter = PrayerTrackingAdapter(prayerTime) { prayerRecord ->
                    savePrayerRecord(prayerRecord)
                }
                recyclerView.adapter = adapter
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading prayer times: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun savePrayerRecord(prayerRecord: PrayerRecord) {
        // This will be implemented in the database step
        Toast.makeText(requireContext(), "Prayer ${prayerRecord.prayerName} recorded", Toast.LENGTH_SHORT).show()
        
        // For now, just notify the ViewModel
        prayersViewModel.onPrayerRecorded(prayerRecord)
    }
}
