package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Reset_password : AppCompatActivity() {

    private var userEmail: String? = null

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
        val newPassword = findViewById<EditText>(R.id.new_password).text.toString().trim()
        val confirmPassword = findViewById<EditText>(R.id.confirm_new_password).text.toString().trim()

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val email = userEmail
        if (email == null) {
            Toast.makeText(this, "Error: Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseProvider.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            // 🔄 Update password in RoomDB
            db.userDao().updatePassword(email, newPassword)

            withContext(Dispatchers.Main) {
                // ✅ Update User-Specific Storage Session
                getSharedPreferences("UserData_" + email, MODE_PRIVATE).edit {
                    putString("password", newPassword)
                }

                Toast.makeText(this@Reset_password, "Password Reset Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Reset_password, Login_page::class.java))
                finish()
            }
        }
    }
}
