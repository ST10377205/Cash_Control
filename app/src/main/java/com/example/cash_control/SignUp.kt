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

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        val loginPage = findViewById<TextView>(R.id.log_in_TextView)
        loginPage.setOnClickListener {
            startActivity(Intent(this@SignUp, Login_page::class.java))
        }
    }

    fun signUpBtn(@Suppress("UNUSED_PARAMETER") view: View) {
        val name = findViewById<EditText>(R.id.name_input).text.toString().trim()
        val email = findViewById<EditText>(R.id.name_email).text.toString().trim()
        val password = findViewById<EditText>(R.id.creat_password_input).text.toString().trim()
        val confirmPassword = findViewById<EditText>(R.id.confirm_password_input).text.toString().trim()
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = findViewById<RadioButton>(selectedId).text.toString().lowercase()

        // Room Database Save
        val db = DatabaseProvider.getDatabase(this)
        val newUser = User(name = name, email = email, password = password, gender = gender)

        CoroutineScope(Dispatchers.IO).launch {
            // 1. Save to RoomDB
            db.userDao().insertUser(newUser)

            withContext(Dispatchers.Main) {
                // 2. Save to SharedPreferences for session
                getSharedPreferences("UserData", MODE_PRIVATE).edit {
                    putString("name", name)
                    putString("email", email)
                    putString("password", password)
                    putString("gender", gender)
                }

                Toast.makeText(this@SignUp, "Signup successful (RoomDB Saved)", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignUp, Login_page::class.java))
                finish()
            }
        }
    }
}
