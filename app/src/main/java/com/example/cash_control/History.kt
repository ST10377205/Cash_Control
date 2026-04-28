package com.example.cash_control

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class History : AppCompatActivity() {

    private lateinit var inputStartDate: EditText
    private lateinit var inputEndDate: EditText
    private lateinit var container: LinearLayout
    private lateinit var placeholder: LinearLayout
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        
        inputStartDate = findViewById(R.id.inputStartDate)
        inputEndDate = findViewById(R.id.inputEndDate)
        container = findViewById(R.id.transactionContainer)
        placeholder = findViewById(R.id.emptyPlaceholder)

        setupDatePickers()

        val mainLayout = findViewById<View>(R.id.main_history_layout)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        displayTransactions(null, null)
    }

    private fun setupDatePickers() {
        val dateListener = { editText: EditText ->
            DatePickerDialog(this, { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editText.setText(sdf.format(cal.time))
                
                // Auto-filter when both dates are selected
                val start = inputStartDate.text.toString()
                val end = inputEndDate.text.toString()
                if (start.isNotEmpty() && end.isNotEmpty()) {
                    displayTransactions(start, end)
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        inputStartDate.setOnClickListener { dateListener(inputStartDate) }
        inputEndDate.setOnClickListener { dateListener(inputEndDate) }
    }

    private fun displayTransactions(startDate: String?, endDate: String?) {
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

                withContext(Dispatchers.Main) {
                    container.removeAllViews()
                    if (transactions.isEmpty()) {
                        placeholder.visibility = View.VISIBLE
                    } else {
                        placeholder.visibility = View.GONE

                        for (transaction in transactions) {
                            val transactionView = layoutInflater.inflate(R.layout.item_transaction, null)
                            
                            val textTitle = transactionView.findViewById<TextView>(R.id.txtTransTitle)
                            val textDate = transactionView.findViewById<TextView>(R.id.txtTransDate)
                            val textDesc = transactionView.findViewById<TextView>(R.id.txtTransDesc)
                            val imgReceipt = transactionView.findViewById<ImageView>(R.id.imgTransReceipt)

                            textTitle.text = "${transaction.category} - R %.2f".format(transaction.amount)
                            textDate.text = "${transaction.date} (${transaction.startTime} - ${transaction.endTime})"
                            textDesc.text = transaction.description ?: ""

                            if (transaction.imageUri != null) {
                                try {
                                    val imageBytes = Base64.decode(transaction.imageUri, Base64.DEFAULT)
                                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    imgReceipt.setImageBitmap(decodedImage)
                                    imgReceipt.visibility = View.VISIBLE
                                } catch (e: Exception) {
                                    imgReceipt.visibility = View.GONE
                                }
                            } else {
                                imgReceipt.visibility = View.GONE
                            }

                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 0, 0, 24)
                            transactionView.layoutParams = params
                            
                            container.addView(transactionView)
                        }
                    }
                }
            }
        }
    }

    fun filterTransactions(@Suppress("UNUSED_PARAMETER") view: View) {
        val start = inputStartDate.text.toString().trim()
        val end = inputEndDate.text.toString().trim()

        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            return
        }
        displayTransactions(start, end)
    }

    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { finish() }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) {}
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
