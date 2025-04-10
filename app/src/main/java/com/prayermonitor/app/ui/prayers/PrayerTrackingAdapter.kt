package com.prayermonitor.app.ui.prayers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prayermonitor.app.R
import com.prayermonitor.app.data.model.PrayerRecord
import com.prayermonitor.app.data.model.PrayerTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerTrackingAdapter(
    private val prayerTime: PrayerTime,
    private val onSaveListener: (PrayerRecord) -> Unit
) : RecyclerView.Adapter<PrayerTrackingAdapter.PrayerViewHolder>() {

    private val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    private val prayerTimes = listOf(
        prayerTime.fajrTime,
        prayerTime.dhuhrTime,
        prayerTime.asrTime,
        prayerTime.maghribTime,
        prayerTime.ishaTime
    )

    class PrayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textPrayerName: TextView = view.findViewById(R.id.text_prayer_name)
        val textPrayerTime: TextView = view.findViewById(R.id.text_prayer_time)
        val checkboxPerformed: CheckBox = view.findViewById(R.id.checkbox_performed)
        val checkboxOnTime: CheckBox = view.findViewById(R.id.checkbox_on_time)
        val checkboxInMosque: CheckBox = view.findViewById(R.id.checkbox_in_mosque)
        val checkboxInGroup: CheckBox = view.findViewById(R.id.checkbox_in_group)
        val buttonSave: Button = view.findViewById(R.id.button_save)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prayer_tracking, parent, false)
        return PrayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        val prayerName = prayers[position]
        val prayerTime = prayerTimes[position]
        
        holder.textPrayerName.text = prayerName
        holder.textPrayerTime.text = prayerTime
        
        // Reset checkboxes
        holder.checkboxPerformed.isChecked = false
        holder.checkboxOnTime.isChecked = false
        holder.checkboxInMosque.isChecked = false
        holder.checkboxInGroup.isChecked = false
        
        // Enable/disable on time checkbox based on performed checkbox
        holder.checkboxPerformed.setOnCheckedChangeListener { _, isChecked ->
            holder.checkboxOnTime.isEnabled = isChecked
            holder.checkboxInMosque.isEnabled = isChecked
            holder.checkboxInGroup.isEnabled = isChecked
            
            if (!isChecked) {
                holder.checkboxOnTime.isChecked = false
                holder.checkboxInMosque.isChecked = false
                holder.checkboxInGroup.isChecked = false
            }
        }
        
        // Initialize checkboxes as disabled
        holder.checkboxOnTime.isEnabled = false
        holder.checkboxInMosque.isEnabled = false
        holder.checkboxInGroup.isEnabled = false
        
        holder.buttonSave.setOnClickListener {
            if (holder.checkboxPerformed.isChecked) {
                val prayerRecord = PrayerRecord(
                    date = Date(),
                    prayerName = prayerName,
                    performed = holder.checkboxPerformed.isChecked,
                    onTime = holder.checkboxOnTime.isChecked,
                    inMosque = holder.checkboxInMosque.isChecked,
                    inGroup = holder.checkboxInGroup.isChecked,
                    userId = "current_user" // This will be replaced with actual user ID
                )
                onSaveListener(prayerRecord)
                
                // Disable editing after saving
                holder.checkboxPerformed.isEnabled = false
                holder.checkboxOnTime.isEnabled = false
                holder.checkboxInMosque.isEnabled = false
                holder.checkboxInGroup.isEnabled = false
                holder.buttonSave.isEnabled = false
                holder.buttonSave.text = "Saved"
            }
        }
    }

    override fun getItemCount() = prayers.size
}
