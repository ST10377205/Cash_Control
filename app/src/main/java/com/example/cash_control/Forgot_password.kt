package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Forgot_password : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    fun verifyEmail(@Suppress("UNUSED_PARAMETER") view: View) {
        val emailInput = findViewById<EditText>(R.id.email_reset).text.toString().trim()

        if (emailInput.isEmpty()) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            // 🔍 Check Room Database for the email
            val user = db.userDao().findUserByEmail(emailInput)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    // ✅ Found: Navigate to Reset
                    val intent = Intent(this@Forgot_password, Reset_password::class.java)
                    intent.putExtra("email", emailInput)
                    startActivity(intent)
                    finish()
                } else {
                    // ❌ Not Found
                    Toast.makeText(this@Forgot_password, "Email not found in our records", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
