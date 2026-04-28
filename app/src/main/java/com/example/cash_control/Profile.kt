package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        
        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail != null) {
            val userSharedPref = getSharedPreferences("UserData_" + currentUserEmail, MODE_PRIVATE)
            val name = userSharedPref.getString("name", "User Name")
            val email = userSharedPref.getString("email", currentUserEmail)
            val gender = userSharedPref.getString("gender", "")?.lowercase()

            val title = when (gender) {
                "male" -> "Mr."
                "female" -> "Mrs."
                else -> ""
            }

            findViewById<TextView>(R.id.profileName).text = "$title $name".trim()
            findViewById<TextView>(R.id.profileEmail).text = email
        }
    }

    fun logout(@Suppress("UNUSED_PARAMETER") view: View) {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        appPrefs.edit().remove("current_user_email").apply()

        val intent = Intent(this, landing_page::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // NAVIGATION
    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Dash_board::class.java)) }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) {}
}
