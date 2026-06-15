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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Reset_password Activity allows users to change their password after verification.
 */
class Reset_password : AppCompatActivity() {

    private var userEmail: String? = null
    private val TAG = "CASH_CONTROL_RESET"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        userEmail = intent.getStringExtra("email")

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    fun savePassword(@Suppress("UNUSED_PARAMETER") view: View) {
        val newPasswordEdit = findViewById<EditText>(R.id.new_password)
        val confirmPasswordEdit = findViewById<EditText>(R.id.confirm_new_password)
        
        val newPassword = newPasswordEdit.text.toString().trim()
        val confirmPassword = confirmPasswordEdit.text.toString().trim()

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            newPasswordEdit.error = "Password must be at least 6 characters"
            return
        }

        if (newPassword != confirmPassword) {
            confirmPasswordEdit.error = "Passwords do not match"
            return
        }

        val email = userEmail ?: return

        lifecycleScope.launch {
            try {
                // 1. Update Firestore database
                val firestore = FirebaseFirestore.getInstance()
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!userQuery.isEmpty) {
                    val docId = userQuery.documents[0].id
                    firestore.collection("users").document(docId)
                        .update("password", newPassword) // Optional: storing for reference
                        .await()
                }

                // 2. Update local Room Database
                val db = DatabaseProvider.getDatabase(this@Reset_password)
                withContext(Dispatchers.IO) {
                    db.userDao().updatePassword(email, newPassword)
                }

                // 3. Update local session
                getSharedPreferences("UserData_$email", MODE_PRIVATE).edit {
                    putString("password", newPassword)
                }

                Toast.makeText(this@Reset_password, "Password Reset Successful (Database Updated)", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@Reset_password, Login_page::class.java))
                finish()

            } catch (e: Exception) {
                Log.e(TAG, "Reset failed: ${e.message}")
                Toast.makeText(this@Reset_password, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
