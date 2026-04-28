package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login_page : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        val forgotpassword = findViewById<TextView>(R.id.forgot_password)
        forgotpassword.setOnClickListener {
            startActivity(Intent(this, Forgot_password::class.java))
        }

        val signUpText = findViewById<TextView>(R.id.sign)
        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }

    fun login(@Suppress("UNUSED_PARAMETER") view: View) {
        val emailInput = findViewById<EditText>(R.id.login_email).text.toString().trim()
        val passwordInput = findViewById<EditText>(R.id.login_password).text.toString().trim()

        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            val user = db.userDao().loginUser(emailInput, passwordInput)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    // ✅ SAVE CURRENT USER SESSION
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit {
                        putString("current_user_email", user.email)
                    }

                    // ✅ SAVE USER PROFILE DATA
                    getSharedPreferences("UserData_" + user.email, MODE_PRIVATE).edit {
                        putString("name", user.name)
                        putString("email", user.email)
                        putString("password", user.password)
                        putString("gender", user.gender)
                    }

                    Toast.makeText(this@Login_page, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Login_page, Dash_board::class.java))
                    finish()
                } else {
                    Toast.makeText(this@Login_page, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
