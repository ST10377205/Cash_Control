package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SignUp Activity handles the registration of new users.
 * It validates user input, saves user data to the local Room database,
 * and initializes user-specific preferences.
 */
class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge layout for a modern immersive UI
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Find the "Log In" text view and set a click listener to navigate back to the Login page
        val loginPage = findViewById<TextView>(R.id.log_in_TextView)
        loginPage.setOnClickListener {
            startActivity(Intent(this@SignUp, Login_page::class.java))
        }
    }

    /**
     * Called when the "Sign Up" button is clicked.
     * Performs validation and saves the user data.
     */
    fun signUpBtn(@Suppress("UNUSED_PARAMETER") view: View) {
        // 1. Get user input from the form fields
        val name = findViewById<EditText>(R.id.name_input).text.toString().trim()
        val email = findViewById<EditText>(R.id.name_email).text.toString().trim()
        val password = findViewById<EditText>(R.id.creat_password_input).text.toString().trim()
        val confirmPassword = findViewById<EditText>(R.id.confirm_password_input).text.toString().trim()
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)

        // 2. Validate inputs: ensure all fields are filled
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Validate passwords: ensure they match
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Validate gender selection
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. Get the selected gender string
        val gender = findViewById<RadioButton>(selectedId).text.toString().lowercase()

        // 6. Prepare the local Room Database instance and the User entity
        val db = DatabaseProvider.getDatabase(this)
        val newUser = User(name = name, email = email, password = password, gender = gender)

        // 7. Perform Database operation on a background thread (IO) to avoid blocking the UI
        CoroutineScope(Dispatchers.IO).launch {
            // Save user to Room Database
            db.userDao().insertUser(newUser)

            // 8. Switch back to the main thread to update UI and navigate
            withContext(Dispatchers.Main) {
                // Save the current user's email globally to maintain session state
                getSharedPreferences("AppPrefs", MODE_PRIVATE).edit {
                    putString("current_user_email", email)
                }

                // Initialize the unique user storage for their personal data (budget, expenses, etc.)
                getSharedPreferences("UserData_" + email, MODE_PRIVATE).edit {
                    putString("name", name)
                    putString("email", email)
                    putString("password", password)
                    putString("gender", gender)
                }

                Toast.makeText(this@SignUp, "Signup successful", Toast.LENGTH_SHORT).show()
                
                // Navigate to the Login page and close this registration screen
                startActivity(Intent(this@SignUp, Login_page::class.java))
                finish()
            }
        }
    }
}
