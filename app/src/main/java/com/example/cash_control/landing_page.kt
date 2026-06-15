package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class landing_page : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Firebase Session Check: If user is already logged in, skip landing page
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Dash_board::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_landing_page)

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    /**
     * Navigates to the login_page when clicked.
     */
    fun Get_started(@Suppress("UNUSED_PARAMETER") view: View) {
        val navigate = Intent(this@landing_page, Login_page::class.java)
        startActivity(navigate)
    }
}
