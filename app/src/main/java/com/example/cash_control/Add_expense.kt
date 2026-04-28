package com.example.cash_control

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Add_expense : AppCompatActivity() {

    private lateinit var dropdown: AutoCompleteTextView
    private lateinit var imageView: ImageView
    private lateinit var inputDate: EditText
    private lateinit var inputStartTime: EditText
    private lateinit var inputEndTime: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputAmount: EditText
    
    private var categories = JSONArray()
    private val CAMERA_REQUEST = 100
    private var capturedImageBase64: String? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expense)

        dropdown = findViewById(R.id.categoryDropdown)
        imageView = findViewById(R.id.imgReceipt)
        inputDate = findViewById(R.id.inputDate)
        inputStartTime = findViewById(R.id.inputStartTime)
        inputEndTime = findViewById(R.id.inputEndTime)
        inputDescription = findViewById(R.id.inputDescription)
        inputAmount = findViewById(R.id.expense_input_Amount_Category)

        // Set default date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        inputDate.setText(sdf.format(Date()))

        setupPickers()

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    private fun setupPickers() {
        inputDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                inputDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val timeListener = { editText: EditText ->
            TimePickerDialog(this, { _, hour, minute ->
                editText.setText(String.format("%02d:%02d", hour, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        inputStartTime.setOnClickListener { timeListener(inputStartTime) }
        inputEndTime.setOnClickListener { timeListener(inputEndTime) }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail != null) {
            val userSharedPref = getSharedPreferences("UserData_" + currentUserEmail, MODE_PRIVATE)
            val jsonString = userSharedPref.getString("categories", "[]")
            categories = JSONArray(jsonString)

            val names = ArrayList<String>()
            for (i in 0 until categories.length()) {
                val obj = categories.getJSONObject(i)
                names.add(obj.getString("name"))
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
            dropdown.setAdapter(adapter)
        }
    }

    fun openCamera(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(photo)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            
            val outputStream = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            capturedImageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }
    }

    fun addExpense(@Suppress("UNUSED_PARAMETER") view: View) {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null) ?: return

        val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)
        val selectedCategory = dropdown.text.toString()
        val amountText = inputAmount.text.toString().trim()
        val date = inputDate.text.toString().trim()
        val startTime = inputStartTime.text.toString().trim()
        val endTime = inputEndTime.text.toString().trim()
        val description = inputDescription.text.toString().trim()

        if (selectedCategory.isEmpty() || amountText.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = amountText.toDoubleOrNull() ?: 0.0
        
        var categoryIndex = -1
        for (i in 0 until categories.length()) {
            val obj = categories.getJSONObject(i)
            if (obj.getString("name") == selectedCategory) {
                val currentBalance = obj.getDouble("amount")
                if (currentBalance < expense) {
                    Toast.makeText(this, "Insufficient funds in $selectedCategory", Toast.LENGTH_LONG).show()
                    return
                }
                categoryIndex = i
                break
            }
        }

        if (categoryIndex == -1) {
             Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
             return
        }

        val currentExpense = userSharedPref.getFloat("totalExpense", 0f)
        val updatedExpense = currentExpense + expense

        // Update local category list
        val catObj = categories.getJSONObject(categoryIndex)
        catObj.put("amount", catObj.getDouble("amount") - expense)

        // Increment streak only if this is the first expense of the day
        val lastExpenseDate = userSharedPref.getString("last_expense_date", "")
        var currentStreak = userSharedPref.getInt("streak_count", 0)
        
        if (lastExpenseDate != date) {
            currentStreak++
            userSharedPref.edit().putString("last_expense_date", date).apply()
        }

        val transaction = Transaction(
            userEmail = currentUserEmail,
            category = selectedCategory,
            amount = expense,
            date = date,
            startTime = startTime,
            endTime = endTime,
            description = description,
            imageUri = capturedImageBase64
        )

        CoroutineScope(Dispatchers.IO).launch {
            DatabaseProvider.getDatabase(this@Add_expense).transactionDao().insertTransaction(transaction)
            withContext(Dispatchers.Main) {
                userSharedPref.edit()
                    .putFloat("totalExpense", updatedExpense.toFloat())
                    .putString("categories", categories.toString())
                    .putInt("streak_count", currentStreak)
                    .apply()

                Toast.makeText(this@Add_expense, "Expense Added! Streak: $currentStreak", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun openHome(view: View) { finish() }
    fun openHistory(view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(view: View) { startActivity(Intent(this, Profile::class.java)) }
}
