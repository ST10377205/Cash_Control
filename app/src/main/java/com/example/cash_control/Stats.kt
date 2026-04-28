package com.example.cash_control

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class Stats : AppCompatActivity() {

    private lateinit var inputStart: EditText
    private lateinit var inputEnd: EditText
    private lateinit var container: LinearLayout
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)
        
        inputStart = findViewById(R.id.inputStatStart)
        inputEnd = findViewById(R.id.inputStatEnd)
        container = findViewById(R.id.categoryStatContainer)

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

                // Auto-filter when both dates are selected
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

    private fun loadStats(startDate: String?, endDate: String?) {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail != null) {
            val db = DatabaseProvider.getDatabase(this)

            CoroutineScope(Dispatchers.IO).launch {
                val transactions = if (startDate != null && endDate != null) {
                    db.transactionDao().getTransactionsInRange(currentUserEmail, startDate, endDate)
                } else {
                    db.transactionDao().getTransactionsForUser(currentUserEmail)
                }

                val categoryTotals = mutableMapOf<String, Double>()
                var maxTotal = 0.0
                for (transaction in transactions) {
                    val currentTotal = categoryTotals.getOrDefault(transaction.category, 0.0)
                    val newTotal = currentTotal + transaction.amount
                    categoryTotals[transaction.category] = newTotal
                    if (newTotal > maxTotal) maxTotal = newTotal
                }

                withContext(Dispatchers.Main) {
                    container.removeAllViews()
                    if (categoryTotals.isEmpty()) {
                        val emptyText = TextView(this@Stats)
                        emptyText.text = "No data for this period"
                        emptyText.setPadding(0, 32, 0, 0)
                        container.addView(emptyText)
                    } else {
                        for ((category, total) in categoryTotals) {
                            // Category Label and Value
                            val labelLayout = LinearLayout(this@Stats)
                            labelLayout.orientation = LinearLayout.HORIZONTAL
                            labelLayout.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            
                            val nameText = TextView(this@Stats)
                            nameText.text = category
                            nameText.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                            nameText.setTextColor(Color.BLACK)
                            
                            val valueText = TextView(this@Stats)
                            valueText.text = "R %.2f".format(total)
                            valueText.setTextColor("#2E7D32".toColorInt())
                            valueText.setTypeface(null, Typeface.BOLD)

                            labelLayout.addView(nameText)
                            labelLayout.addView(valueText)
                            container.addView(labelLayout)

                            // Visual Bar (Graph)
                            val progressBar = ProgressBar(this@Stats, null, android.R.attr.progressBarStyleHorizontal)
                            progressBar.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 
                                40
                            )
                            progressBar.max = 100
                            val progress = if (maxTotal > 0) ((total / maxTotal) * 100).toInt() else 0
                            progressBar.progress = progress
                            progressBar.progressDrawable = AppCompatResources.getDrawable(this@Stats, R.drawable.custom_progress_bar)
                            
                            val params = progressBar.layoutParams as LinearLayout.LayoutParams
                            params.setMargins(0, 8, 0, 32)
                            progressBar.layoutParams = params

                            container.addView(progressBar)
                        }
                    }
                }
            }
        }
    }

    fun filterStats(@Suppress("UNUSED_PARAMETER") view: View) {
        val start = inputStart.text.toString().trim()
        val end = inputEnd.text.toString().trim()

        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            return
        }
        loadStats(start, end)
    }

    // NAVIGATION
    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { finish() }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { /* Already on Stats */ }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
