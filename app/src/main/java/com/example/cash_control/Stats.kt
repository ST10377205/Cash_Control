package com.example.cash_control

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Stats Activity provides advanced spending analytics.
 * SATISFIES POE: Graph showing spent per category + visual Min/Max goal lines.
 */
class Stats : AppCompatActivity() {

    private val TAG = "CASH_CONTROL_STATS"
    private lateinit var inputStart: EditText
    private lateinit var inputEnd: EditText
    private lateinit var container: LinearLayout
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)
        Log.i(TAG, "Stats Activity initializing...")
        
        inputStart = findViewById(R.id.inputStatStart)
        inputEnd = findViewById(R.id.inputStatEnd)
        container = findViewById(R.id.categoryStatContainer)
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)

        setupDatePickers()

        val mainLayout = findViewById<View>(R.id.main_stats_layout)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        
        loadStats(null, null)
    }

    private fun setupDatePickers() {
        val dateListener = { editText: EditText ->
            DatePickerDialog(this, { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editText.setText(sdf.format(cal.time))

                val start = inputStart.text.toString()
                val end = inputEnd.text.toString()
                if (start.isNotEmpty() && end.isNotEmpty()) {
                    loadStats(start, end)
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        inputStart.setOnClickListener { dateListener(inputStart) }
        inputEnd.setOnClickListener { dateListener(inputEnd) }
    }

    /**
     * Handles the Filter button click event from XML.
     */
    fun filterStats(view: View) {
        val start = inputStart.text.toString()
        val end = inputEnd.text.toString()
        if (start.isNotEmpty() && end.isNotEmpty()) {
            loadStats(start, end)
        } else {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStats(startDate: String?, endDate: String?) {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null) ?: return

        val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)
        val minGoal = userSharedPref.getFloat("min_goal", 0f)
        val maxGoal = userSharedPref.getFloat("max_goal", 0f)

        val db = DatabaseProvider.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = if (startDate != null && endDate != null) {
                db.transactionDao().getTransactionsInRange(currentUserEmail, startDate, endDate)
            } else {
                db.transactionDao().getTransactionsForUser(currentUserEmail)
            }

            val categoryTotals = mutableMapOf<String, Double>()
            var totalSpend = 0.0
            for (transaction in transactions) {
                if (transaction.type == "expense") {
                    val current = categoryTotals.getOrDefault(transaction.category, 0.0)
                    categoryTotals[transaction.category] = current + transaction.amount
                    totalSpend += transaction.amount
                }
            }

            withContext(Dispatchers.Main) {
                container.removeAllViews()
                if (categoryTotals.isEmpty()) {
                    val emptyText = TextView(this@Stats)
                    emptyText.text = "No records found. Start logging to see stats!"
                    container.addView(emptyText)
                    pieChart.clear()
                    barChart.clear()
                } else {
                    setupPieChart(categoryTotals)
                    setupCategoryBarChart(categoryTotals, minGoal, maxGoal, totalSpend)
                }
            }
        }
    }

    private fun setupPieChart(categoryTotals: Map<String, Double>) {
        val entries = categoryTotals.map { PieEntry(it.value.toFloat(), it.key) }
        
        // "Vibrant Deep" Palette - Classic colors darkened (700 shades) for visibility
        val vibrantDeepColors = arrayListOf(
            Color.parseColor("#1976D2"), // Blue 700
            Color.parseColor("#388E3C"), // Green 700
            Color.parseColor("#D32F2F"), // Red 700
            Color.parseColor("#F57C00"), // Orange 700
            Color.parseColor("#7B1FA2"), // Purple 700
            Color.parseColor("#00796B"), // Teal 700
            Color.parseColor("#C2185B"), // Pink 700
            Color.parseColor("#FFA000"), // Amber 700
            Color.parseColor("#303F9F"), // Indigo 700
            Color.parseColor("#0097A7"), // Cyan 700
            Color.parseColor("#5D4037")  // Brown 700
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = vibrantDeepColors
            valueTextSize = 14f // Clear and large
            valueTextColor = Color.WHITE
            valueTypeface = android.graphics.Typeface.DEFAULT_BOLD
            sliceSpace = 2f 
        }

        pieChart.apply {
            data = PieData(dataSet).apply { 
                setValueFormatter(PercentFormatter(pieChart))
            }
            description = null
            setUsePercentValues(true)
            
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            
            // Label visibility styling
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            setEntryLabelTypeface(android.graphics.Typeface.DEFAULT_BOLD)

            animateXY(1200, 1200)
            invalidate()
        }
    }

    private fun setupCategoryBarChart(categoryTotals: Map<String, Double>, min: Float, max: Float, total: Double) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        // Same "Vibrant Deep" Palette for consistency
        val chartPalette = arrayListOf(
            Color.parseColor("#1976D2"), // Blue 700
            Color.parseColor("#388E3C"), // Green 700
            Color.parseColor("#D32F2F"), // Red 700
            Color.parseColor("#F57C00"), // Orange 700
            Color.parseColor("#7B1FA2"), // Purple 700
            Color.parseColor("#00796B"), // Teal 700
            Color.parseColor("#C2185B"), // Pink 700
            Color.parseColor("#FFA000"), // Amber 700
            Color.parseColor("#303F9F"), // Indigo 700
            Color.parseColor("#0097A7"), // Cyan 700
            Color.parseColor("#5D4037")  // Brown 700
        )

        categoryTotals.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }

        val totalIndex = categoryTotals.size
        entries.add(BarEntry(totalIndex.toFloat(), total.toFloat()))
        labels.add("TOTAL")

        val dataSet = BarDataSet(entries, "Spending (R)").apply {
            val colorList = ArrayList<Int>()
            for (i in 0 until categoryTotals.size) {
                // Cycle through palette for categories
                colorList.add(chartPalette[i % chartPalette.size])
            }
            // Distinct Blue Grey for the summary TOTAL bar
            colorList.add(Color.parseColor("#455A64"))
            colors = colorList
            valueTextSize = 10f
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.5f }
            description = null
            setFitBars(true)

            xAxis.apply {
                valueFormatter = object : com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels) {}
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelRotationAngle = -45f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                removeAllLimitLines()
                if (max > 0) {
                    val maxLine = LimitLine(max, "MAX LIMIT").apply {
                        lineWidth = 2f
                        lineColor = Color.RED
                        enableDashedLine(10f, 10f, 0f)
                        labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                        textColor = Color.RED
                    }
                    addLimitLine(maxLine)
                }

                if (min > 0) {
                    val minLine = LimitLine(min, "MIN GOAL").apply {
                        lineWidth = 2f
                        lineColor = Color.parseColor("#EA580C")
                        enableDashedLine(10f, 10f, 0f)
                        labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                        textColor = Color.parseColor("#EA580C")
                    }
                    addLimitLine(minLine)
                }

                val highestVal = maxOf(total.toFloat(), max)
                axisMaximum = highestVal * 1.2f
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            animateY(1500)
            invalidate()
        }
    }

    fun openHome(view: View) { finish() }
    fun openHistory(view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(view: View) { /* On Page */ }
    fun openProfile(view: View) { startActivity(Intent(this, Profile::class.java)) }
}
