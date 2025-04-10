package com.prayermonitor.app.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.prayermonitor.app.PrayerMonitorApplication
import com.prayermonitor.app.R
import com.prayermonitor.app.data.database.DatabaseProvider
import com.prayermonitor.app.data.repository.AuthRepository
import com.prayermonitor.app.data.repository.PrayerRecordRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var prayerRecordRepository: PrayerRecordRepository
    private lateinit var authRepository: AuthRepository
    
    private lateinit var textDashboard: TextView
    private lateinit var pieChartWeekly: PieChart
    private lateinit var barChartMonthly: BarChart
    private lateinit var spinnerTimeRange: Spinner
    private lateinit var spinnerPrayerType: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        
        // Initialize views
        textDashboard = root.findViewById(R.id.text_dashboard)
        pieChartWeekly = root.findViewById(R.id.pie_chart_weekly)
        barChartMonthly = root.findViewById(R.id.bar_chart_monthly)
        spinnerTimeRange = root.findViewById(R.id.spinner_time_range)
        spinnerPrayerType = root.findViewById(R.id.spinner_prayer_type)
        
        // Initialize repositories
        val database = DatabaseProvider.getInstance(requireContext())
        prayerRecordRepository = PrayerRecordRepository(database.prayerRecordDao())
        authRepository = AuthRepository(database.userDao())
        
        // Set up spinners
        setupSpinners()
        
        // Set up charts
        setupPieChart()
        setupBarChart()
        
        // Load data
        loadDashboardData()
        
        // Observe ViewModel data
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textDashboard.text = it
        }
        
        return root
    }
    
    private fun setupSpinners() {
        // Time range spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.time_ranges,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTimeRange.adapter = adapter
        }
        
        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                loadDashboardData()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
        
        // Prayer type spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.prayer_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPrayerType.adapter = adapter
        }
        
        spinnerPrayerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                loadDashboardData()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    
    private fun setupPieChart() {
        pieChartWeekly.description.isEnabled = false
        pieChartWeekly.setUsePercentValues(true)
        pieChartWeekly.setEntryLabelTextSize(12f)
        pieChartWeekly.setEntryLabelColor(Color.BLACK)
        pieChartWeekly.legend.isEnabled = true
        pieChartWeekly.legend.textSize = 12f
        pieChartWeekly.setHoleColor(Color.WHITE)
        pieChartWeekly.setTransparentCircleAlpha(0)
        pieChartWeekly.holeRadius = 58f
        pieChartWeekly.setDrawCenterText(true)
        pieChartWeekly.centerText = "Weekly\nPerformance"
        pieChartWeekly.setCenterTextSize(16f)
    }
    
    private fun setupBarChart() {
        barChartMonthly.description.isEnabled = false
        barChartMonthly.setPinchZoom(false)
        barChartMonthly.setDrawBarShadow(false)
        barChartMonthly.setDrawGridBackground(false)
        
        val xAxis = barChartMonthly.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        
        barChartMonthly.axisLeft.setDrawGridLines(false)
        barChartMonthly.axisRight.isEnabled = false
        barChartMonthly.legend.isEnabled = true
    }
    
    private fun loadDashboardData() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to view statistics", Toast.LENGTH_LONG).show()
            return
        }
        
        val userId = currentUser.uid
        val timeRange = spinnerTimeRange.selectedItemPosition
        val prayerType = spinnerPrayerType.selectedItemPosition
        
        lifecycleScope.launch {
            try {
                // Get date range based on selected time range
                val (startDate, endDate) = getDateRange(timeRange)
                
                // Get prayer records for the selected date range
                val prayerRecords = if (prayerType == 0) {
                    // All prayers
                    prayerRecordRepository.getPrayerRecordsBetweenDates(userId, startDate, endDate)
                } else {
                    // Specific prayer
                    val prayerName = resources.getStringArray(R.array.prayer_types)[prayerType]
                    prayerRecordRepository.getPrayerRecordsByName(userId, prayerName)
                }
                
                // Observe the prayer records and update charts
                prayerRecords.observe(viewLifecycleOwner) { records ->
                    updatePieChart(records)
                    updateBarChart(records)
                }
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading statistics: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun getDateRange(timeRange: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        when (timeRange) {
            0 -> { // Week
                calendar.add(Calendar.DAY_OF_MONTH, -7)
            }
            1 -> { // Month
                calendar.add(Calendar.MONTH, -1)
            }
            2 -> { // 3 Months
                calendar.add(Calendar.MONTH, -3)
            }
            3 -> { // Year
                calendar.add(Calendar.YEAR, -1)
            }
        }
        
        val startDate = calendar.time
        return Pair(startDate, endDate)
    }
    
    private fun updatePieChart(records: List<PrayerRecord>) {
        val totalPrayers = records.size
        val performed = records.count { it.performed }
        val notPerformed = totalPrayers - performed
        
        val entries = ArrayList<PieEntry>()
        if (performed > 0) entries.add(PieEntry(performed.toFloat(), "Performed"))
        if (notPerformed > 0) entries.add(PieEntry(notPerformed.toFloat(), "Missed"))
        
        val dataSet = PieDataSet(entries, "Prayer Performance")
        dataSet.colors = listOf(Color.rgb(76, 175, 80), Color.rgb(244, 67, 54))
        
        val data = PieData(dataSet)
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.WHITE)
        
        pieChartWeekly.data = data
        pieChartWeekly.invalidate()
    }
    
    private fun updateBarChart(records: List<PrayerRecord>) {
        val performedEntries = ArrayList<BarEntry>()
        val onTimeEntries = ArrayList<BarEntry>()
        val inMosqueEntries = ArrayList<BarEntry>()
        val inGroupEntries = ArrayList<BarEntry>()
        
        // Group records by day/week/month depending on time range
        val groupedRecords = records.groupBy { record ->
            val calendar = Calendar.getInstance()
            calendar.time = record.date
            calendar.get(Calendar.DAY_OF_YEAR) // Group by day for simplicity
        }
        
        // Create bar entries for each group
        groupedRecords.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
            val dayRecords = entry.value
            val totalForDay = dayRecords.size.toFloat()
            val performedForDay = dayRecords.count { it.performed }.toFloat()
            val onTimeForDay = dayRecords.count { it.performed && it.onTime }.toFloat()
            val inMosqueForDay = dayRecords.count { it.performed && it.inMosque }.toFloat()
            val inGroupForDay = dayRecords.count { it.performed && it.inGroup }.toFloat()
            
            performedEntries.add(BarEntry(index.toFloat(), performedForDay / totalForDay * 100))
            onTimeEntries.add(BarEntry(index.toFloat(), onTimeForDay / totalForDay * 100))
            inMosqueEntries.add(BarEntry(index.toFloat(), inMosqueForDay / totalForDay * 100))
            inGroupEntries.add(BarEntry(index.toFloat(), inGroupForDay / totalForDay * 100))
        }
        
        // Create datasets
        val performedDataSet = BarDataSet(performedEntries, "Performed")
        performedDataSet.color = Color.rgb(76, 175, 80)
        
        val onTimeDataSet = BarDataSet(onTimeEntries, "On Time")
        onTimeDataSet.color = Color.rgb(33, 150, 243)
        
        val inMosqueDataSet = BarDataSet(inMosqueEntries, "In Mosque")
        inMosqueDataSet.color = Color.rgb(255, 193, 7)
        
        val inGroupDataSet = BarDataSet(inGroupEntries, "In Group")
        inGroupDataSet.color = Color.rgb(156, 39, 176)
        
        // Create bar data
        val barData = BarData(performedDataSet, onTimeDataSet, inMosqueDataSet, inGroupDataSet)
        barData.barWidth = 0.2f
        
        // Set x-axis labels
        val labels = groupedRecords.entries.sortedBy { it.key }.mapIndexed { index, entry ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_YEAR, entry.key)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            "$day/$month"
        }
        barChartMonthly.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        // Set data to chart
        barChartMonthly.data = barData
        barChartMonthly.groupBars(0f, 0.1f, 0.05f)
        barChartMonthly.invalidate()
    }
}
