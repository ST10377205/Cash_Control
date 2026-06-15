package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * SignUp Activity handles the registration of new users with Firebase Auth and Firestore.
 */
class SignUp : AppCompatActivity() {

    private val TAG = "CASH_CONTROL_AUTH"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Log.i(TAG, "SignUp Activity initialized with Firebase")

        val loginPage = findViewById<TextView>(R.id.log_in_TextView)
        loginPage.setOnClickListener {
            Log.d(TAG, "Navigating to Login Page from SignUp")
            startActivity(Intent(this@SignUp, Login_page::class.java))
        }
    }

    /**
     * Triggered by sign up button. Performs multi-step validation and Firebase registration.
     */
    fun signUpBtn(@Suppress("UNUSED_PARAMETER") view: View) {
        val nameEdit = findViewById<EditText>(R.id.name_input)
        val emailEdit = findViewById<EditText>(R.id.name_email)
        val passwordEdit = findViewById<EditText>(R.id.creat_password_input)
        val confirmPasswordEdit = findViewById<EditText>(R.id.confirm_password_input)
        
        val name = nameEdit.text.toString().trim()
        val email = emailEdit.text.toString().trim()
        val password = passwordEdit.text.toString().trim()
        val confirmPassword = confirmPasswordEdit.text.toString().trim()
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)

        // Reset errors
        nameEdit.error = null
        emailEdit.error = null
        passwordEdit.error = null
        confirmPasswordEdit.error = null

        // 1. Validate Name
        if (name.isEmpty()) {
            nameEdit.error = "Name is required"
            nameEdit.requestFocus()
            return
        }
        if (!name.matches(Regex("^[a-zA-Z\\s]+$"))) {
            nameEdit.error = "Full Name must contain only letters"
            nameEdit.requestFocus()
            return
        }

        // 2. Validate Email
        if (email.isEmpty()) {
            emailEdit.error = "Email is required"
            emailEdit.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.lowercase().endsWith("@gmail.com")) {
            emailEdit.error = "Please enter a valid @gmail.com address"
            emailEdit.requestFocus()
            return
        }
        
        // 3. Validate Password
        if (password.isEmpty()) {
            passwordEdit.error = "Password is required"
            passwordEdit.requestFocus()
            return
        }
        if (password.length < 6) {
            passwordEdit.error = "Password must be at least 6 characters"
            passwordEdit.requestFocus()
            return
        }

        // 4. Check Password Confirmation
        if (password != confirmPassword) {
            confirmPasswordEdit.error = "Passwords do not match"
            confirmPasswordEdit.requestFocus()
            return
        }

        // 5. Validate gender selection
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = findViewById<RadioButton>(selectedId).text.toString().lowercase()
        val normalizedEmail = email.lowercase()

        // Firebase Sign Up
        lifecycleScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
                val user = result.user
                
                if (user != null) {
                    // Save additional data to Firestore
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to normalizedEmail,
                        "password" to password, // Added for the manual reset/login fallback
                        "gender" to gender,
                        "uid" to user.uid,
                        "member_since" to SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
                    )

                    db.collection("users").document(user.uid).set(userData).await()
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SignUp, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignUp, Login_page::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Firebase SignUp Error: ${e.message}")
                    Toast.makeText(this@SignUp, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
