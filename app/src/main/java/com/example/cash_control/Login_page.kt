package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Login_page handles user authentication with Firebase Auth and syncs data from Firestore.
 * Includes a fallback to manual password check if Firebase Auth fails (useful for bypassed resets).
 */
class Login_page : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "CASH_CONTROL_LOGIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        auth = FirebaseAuth.getInstance()

        val forgotpassword = findViewById<TextView>(R.id.forgot_password)
        forgotpassword.setOnClickListener {
            startActivity(Intent(this, Forgot_password::class.java))
        }

        val signUpText = findViewById<TextView>(R.id.sign)
        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }

    /**
     * Triggered by the Login button. Performs validation and Firebase verification.
     */
    fun login(@Suppress("UNUSED_PARAMETER") view: View) {
        val emailEdit = findViewById<EditText>(R.id.login_email)
        val passwordEdit = findViewById<EditText>(R.id.login_password)
        
        val emailInput = emailEdit.text.toString().trim().lowercase()
        val passwordInput = passwordEdit.text.toString().trim()

        emailEdit.error = null
        passwordEdit.error = null

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

        if (passwordInput.isEmpty()) {
            passwordEdit.error = "Password is required"
            passwordEdit.requestFocus()
            return
        }

        lifecycleScope.launch {
            try {
                // 1. Try standard Firebase Auth Login
                val result = auth.signInWithEmailAndPassword(emailInput, passwordInput).await()
                val user = result.user
                if (user != null) {
                    syncAllData(user.uid, emailInput)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Login_page, "Welcome back!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Login_page, Dash_board::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                // 2. FALLBACK: Check Firestore if Auth fails (for manual password resets)
                Log.d(TAG, "Auth failed, checking manual Firestore record...")
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val userQuery = firestore.collection("users")
                        .whereEqualTo("email", emailInput)
                        .whereEqualTo("password", passwordInput)
                        .get()
                        .await()

                    if (!userQuery.isEmpty) {
                        val uid = userQuery.documents[0].getString("uid") ?: ""
                        syncAllData(uid, emailInput)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@Login_page, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login_page, Dash_board::class.java))
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@Login_page, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (dbError: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Fallback check failed: ${dbError.message}")
                        Toast.makeText(this@Login_page, "Login error: ${dbError.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun syncAllData(uid: String, email: String) {
        val firestore = FirebaseFirestore.getInstance()
        try {
            val profileDoc = firestore.collection("users").document(uid).get().await()
            if (profileDoc.exists()) {
                val name = profileDoc.getString("name") ?: "User"
                val gender = profileDoc.getString("gender") ?: ""
                val memberSince = profileDoc.getString("member_since") ?: ""
                val minGoal = profileDoc.getDouble("min_goal")?.toFloat() ?: 0f
                val maxGoal = profileDoc.getDouble("max_goal")?.toFloat() ?: 0f
                val income = profileDoc.getDouble("income")?.toFloat() ?: 0f
                val categories = profileDoc.getString("categories") ?: "[]"

                getSharedPreferences("AppPrefs", MODE_PRIVATE).edit {
                    putString("current_user_email", email)
                }

                getSharedPreferences("UserData_$email", MODE_PRIVATE).edit {
                    putString("name", name)
                    putString("email", email)
                    putString("gender", gender)
                    putString("member_since", memberSince)
                    putFloat("min_goal", minGoal)
                    putFloat("max_goal", maxGoal)
                    putFloat("income", income)
                    putString("categories", categories)
                }
            }

            // Sync Transactions to Room
            val transactionsQuery = firestore.collection("users").document(uid)
                .collection("transactions").get().await()
            
            val db = DatabaseProvider.getDatabase(this)
            withContext(Dispatchers.IO) {
                for (doc in transactionsQuery.documents) {
                    val t = Transaction(
                        userEmail = doc.getString("userEmail") ?: email,
                        category = doc.getString("category") ?: "Other",
                        amount = doc.getDouble("amount") ?: 0.0,
                        date = doc.getString("date") ?: "",
                        startTime = doc.getString("startTime"),
                        endTime = doc.getString("endTime"),
                        description = doc.getString("description"),
                        type = doc.getString("type") ?: "expense"
                    )
                    db.transactionDao().insertTransaction(t)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during full sync: ${e.message}")
        }
    }
}
