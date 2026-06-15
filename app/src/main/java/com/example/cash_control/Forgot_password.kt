package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Forgot_password Activity handles the email verification for password recovery.
 * It checks Firestore to see if the email exists, then navigates to Reset_password.
 */
class Forgot_password : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "CASH_CONTROL_FORGOT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    /**
     * Checks if the email exists in Firestore and navigates to the reset page.
     */
    fun verifyEmail(@Suppress("UNUSED_PARAMETER") view: View) {
        val emailEdit = findViewById<EditText>(R.id.email_reset)
        val emailInput = emailEdit.text.toString().trim().lowercase()

        emailEdit.error = null

        if (emailInput.isEmpty()) {
            emailEdit.error = "Email is required"
            emailEdit.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() || !emailInput.endsWith("@gmail.com")) {
            emailEdit.error = "Please enter a valid @gmail.com address"
            emailEdit.requestFocus()
            return
        }

        lifecycleScope.launch {
            try {
                // Check if the user exists in Firestore "users" collection
                val userQuery = db.collection("users")
                    .whereEqualTo("email", emailInput)
                    .get()
                    .await()

                if (!userQuery.isEmpty) {
                    // User found! Navigate to Reset_password page
                    Toast.makeText(this@Forgot_password, "Email verified!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Forgot_password, Reset_password::class.java)
                    intent.putExtra("email", emailInput)
                    startActivity(intent)
                    finish()
                } else {
                    // User not found in Firestore
                    Toast.makeText(this@Forgot_password, "No account found with this email", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Database check failed: ${e.message}")
                Toast.makeText(this@Forgot_password, "Error checking account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
