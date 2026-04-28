package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Dash_board : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dash_board)

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail != null) {
            val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)

            val name = userSharedPref.getString("name", "User")
            val income = userSharedPref.getFloat("income", 0f)
            val expenses = userSharedPref.getFloat("totalExpense", 0f)
            val balance = income - expenses

            // Using fully qualified R to avoid any ambiguity
            findViewById<TextView>(com.example.cash_control.R.id.welcomeText)?.text = "Welcome, $name"
            findViewById<TextView>(com.example.cash_control.R.id.txtBalance)?.text = "R %.2f".format(balance)
            findViewById<TextView>(com.example.cash_control.R.id.txtIncome)?.text = "R %.2f".format(income)
            findViewById<TextView>(com.example.cash_control.R.id.txtExpenses)?.text = "R %.2f".format(expenses)
        }
    }

    // NAVIGATION
    fun openSetBudget(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, SetBudget::class.java))
    }

    fun openCreateCategory(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, CreateCategory::class.java))
    }

    fun openAddExpense(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, Add_expense::class.java))
    }

    fun openStreak(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, Streak::class.java))
    }

    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) {}
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
