package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

/**
 * CreateCategory Activity handles custom category creation and syncs them to Firestore.
 */
class CreateCategory : AppCompatActivity() {

    private val TAG = "CASH_CONTROL_CAT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_category)
        
        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    fun saveCategory(@Suppress("UNUSED_PARAMETER") view: View) {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show()
            return
        }

        val nameInput = findViewById<EditText>(R.id.inputCategory)
        val amountInput = findViewById<EditText>(R.id.input_Amount_Category)

        val name = nameInput.text.toString().trim()
        val amountText = amountInput.text.toString().trim()

        if (name.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val newAmount = amountText.toDoubleOrNull() ?: 0.0

        // Use user-specific SharedPreferences
        val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)
        
        // Check total budget vs allocations
        val totalBudget = userSharedPref.getFloat("income", 0f).toDouble()
        val jsonString = userSharedPref.getString("categories", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        var totalAllocated = 0.0
        for (i in 0 until jsonArray.length()) {
            totalAllocated += jsonArray.getJSONObject(i).getDouble("amount")
        }

        if (totalAllocated + newAmount > totalBudget) {
            Toast.makeText(this, "Warning: Total allocated (R%.2f) exceeds budget (R%.2f)".format(totalAllocated + newAmount, totalBudget), Toast.LENGTH_LONG).show()
            return
        }

        // Check if category already exists
        for (i in 0 until jsonArray.length()) {
            if (jsonArray.getJSONObject(i).getString("name").equals(name, ignoreCase = true)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val newCategory = JSONObject()
        newCategory.put("name", name)
        newCategory.put("amount", newAmount)

        jsonArray.put(newCategory)

        // 1. SAVE LOCALLY
        userSharedPref.edit {
            putString("categories", jsonArray.toString())
        }

        // 2. SAVE TO FIRESTORE
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            lifecycleScope.launch {
                try {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(firebaseUser.uid)
                        .update("categories", jsonArray.toString())
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync categories: ${e.message}")
                }
            }
        }

        Toast.makeText(this, "Category Saved Successfully", Toast.LENGTH_SHORT).show()

        nameInput.text.clear()
        amountInput.text.clear()
    }

    fun viewCategories(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, ViewCategories::class.java))
    }

    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Dash_board::class.java)) }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
