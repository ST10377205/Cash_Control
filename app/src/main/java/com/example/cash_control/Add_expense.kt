package com.example.cash_control

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
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

    private lateinit var pickReceiptImage: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    private var categories = JSONArray()
    private var capturedImageBase64: String? = null
    private var photoUri: Uri? = null
    private var selectedType = "expense"
    private val calendar = Calendar.getInstance()
    private val TAG = "CASH_CONTROL_ADD"

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

        findViewById<View>(R.id.btnExpense).setOnClickListener { selectExpense() }
        findViewById<View>(R.id.btnIncome).setOnClickListener { selectIncome() }

        pickReceiptImage = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                processAndSetImage(uri)
            }
        }

        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success && photoUri != null) {
                processAndSetImage(photoUri!!)
            }
        }

        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        }

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

    private fun processAndSetImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val base64 = getResizedBitmapString(uri)
            withContext(Dispatchers.Main) {
                if (base64 != null) {
                    capturedImageBase64 = base64
                    imageView.setImageURI(uri)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    Toast.makeText(this@Add_expense, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getResizedBitmapString(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var width = options.outWidth
            var height = options.outHeight
            var scale = 1
            while (width / 2 >= 800 && height / 2 >= 800) {
                width /= 2
                height /= 2
                scale *= 2
            }

            val options2 = BitmapFactory.Options().apply { inSampleSize = scale }
            val inputStream2 = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, options2)
            inputStream2?.close()

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupPickers() {
        inputDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    inputDate.setText(sdf.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val timeListener = { editText: EditText ->
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
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
        val currentUserEmail = appPrefs.getString("current_user_email", null) ?: return

        val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)
        val jsonString = userSharedPref.getString("categories", "[]")

        try {
            categories = JSONArray(jsonString)
        } catch (e: Exception) {
            categories = JSONArray()
        }

        val names = ArrayList<String>()
        for (i in 0 until categories.length()) {
            names.add(categories.getJSONObject(i).getString("name"))
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        dropdown.setAdapter(adapter)
    }

    private fun selectExpense() {
        selectedType = "expense"
        findViewById<View>(R.id.btnExpense).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#2E7D32"))
            (this as? TextView)?.setTextColor(android.graphics.Color.WHITE)
        }
        findViewById<View>(R.id.btnIncome).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#F3F4F6"))
            (this as? TextView)?.setTextColor(android.graphics.Color.parseColor("#6B7280"))
        }
        findViewById<View>(R.id.categorySection).visibility = View.VISIBLE
    }

    private fun selectIncome() {
        selectedType = "income"
        findViewById<View>(R.id.btnIncome).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#2E7D32"))
            (this as? TextView)?.setTextColor(android.graphics.Color.WHITE)
        }
        findViewById<View>(R.id.btnExpense).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#F3F4F6"))
            (this as? TextView)?.setTextColor(android.graphics.Color.parseColor("#6B7280"))
        }
        findViewById<View>(R.id.categorySection).visibility = View.GONE
    }

    fun openCamera(view: View) {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Attach Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            launchCamera()
                        } else {
                            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                    1 -> pickReceiptImage.launch("image/*")
                }
            }
            .show()
    }

    private fun launchCamera() {
        try {
            val imagesDir = File(cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val photoFile = File.createTempFile("receipt_${System.currentTimeMillis()}", ".jpg", imagesDir)
            photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            takePictureLauncher.launch(photoUri!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show()
        }
    }

    fun addExpense(view: View) {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null) ?: return

        val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)

        val selectedCategory = if (selectedType == "expense") dropdown.text.toString().trim() else ""
        val amountText = inputAmount.text.toString().trim()
        val date = inputDate.text.toString().trim()
        val startTime = inputStartTime.text.toString().trim()
        val endTime = inputEndTime.text.toString().trim()
        val description = inputDescription.text.toString().trim()

        if (amountText.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedType == "expense" && selectedCategory.isEmpty()) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        var categoryIndex = -1
        if (selectedType == "expense") {
            for (i in 0 until categories.length()) {
                if (categories.getJSONObject(i).getString("name") == selectedCategory) {
                    if (categories.getJSONObject(i).getDouble("amount") < amount) {
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
        }

        val currentExpense = userSharedPref.getFloat("totalExpense", 0f)
        val currentIncome = userSharedPref.getFloat("income", 0f)
        
        val updatedExpense = if (selectedType == "expense") currentExpense + amount.toFloat() else currentExpense
        val updatedIncome = if (selectedType == "income") currentIncome + amount.toFloat() else currentIncome

        if (selectedType == "expense") {
            val catObj = categories.getJSONObject(categoryIndex)
            catObj.put("amount", catObj.getDouble("amount") - amount)
        }

        val transaction = Transaction(
            userEmail = currentUserEmail,
            category = selectedCategory.ifEmpty { "Income" },
            amount = amount,
            date = date,
            startTime = startTime,
            endTime = endTime,
            description = description,
            imageUri = capturedImageBase64,
            type = selectedType
        )

        lifecycleScope.launch {
            try {
                // 1. SAVE LOCALLY (Room)
                withContext(Dispatchers.IO) {
                    DatabaseProvider.getDatabase(this@Add_expense).transactionDao().insertTransaction(transaction)
                }

                // 2. SAVE TO FIRESTORE
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val firestore = FirebaseFirestore.getInstance()
                    val transactionData = hashMapOf(
                        "userEmail" to transaction.userEmail,
                        "category" to transaction.category,
                        "amount" to transaction.amount,
                        "date" to transaction.date,
                        "startTime" to transaction.startTime,
                        "endTime" to transaction.endTime,
                        "description" to transaction.description,
                        "type" to transaction.type,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    // We don't save the full base64 image string to Firestore to avoid size limits
                    // In a production app, you'd upload the image to Firebase Storage and save the URL.
                    
                    firestore.collection("users").document(firebaseUser.uid)
                        .collection("transactions").add(transactionData).await()
                }

                withContext(Dispatchers.Main) {
                    val editor = userSharedPref.edit()
                    editor.putFloat("totalExpense", updatedExpense)
                    editor.putFloat("income", updatedIncome)

                    if (selectedType == "expense") {
                        editor.putString("categories", categories.toString())
                    }
                    editor.apply()
                    Toast.makeText(this@Add_expense, "Transaction saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving transaction: ${e.message}")
                // Even if Firestore fails, local Room saving succeeded or we can handle it
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Add_expense, "Saved locally. Sync may be delayed.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    fun openHome(view: View) { finish() }
    fun openHistory(view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(view: View) { startActivity(Intent(this, Profile::class.java)) }
}
