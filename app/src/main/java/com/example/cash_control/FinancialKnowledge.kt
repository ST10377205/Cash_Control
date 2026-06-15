package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FinancialKnowledge : AppCompatActivity() {

    private lateinit var tipText: TextView
    private val tips = listOf(
        "The 50/30/20 rule: 50% Needs, 30% Wants, 20% Savings.",
        "Pay yourself first: Move money to savings as soon as you get paid.",
        "Avoid lifestyle creep: Don't spend more just because you earn more.",
        "Emergency Fund: Aim for 3-6 months of expenses in a separate account.",
        "Track every cent: Small daily spends add up to large monthly costs.",
        "Think in hours: Before buying, calculate how many work hours it costs.",
        "The 24-hour rule: Wait a day before any non-essential purchase.",
        "High-interest debt is a financial emergency. Pay it off first."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_financial_knowledge)

        tipText = findViewById(R.id.txtDailyTip)
        refreshTip(null)

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    fun refreshTip(view: View?) {
        tipText.text = tips.random()
    }

    fun acceptChallenge(view: View) {
        Toast.makeText(this, "Challenge Accepted! Check back in 7 days. \uD83D\uDE80", Toast.LENGTH_LONG).show()
    }

    fun openHome(view: View) { finish() }
    fun openHistory(view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(view: View) { startActivity(Intent(this, Profile::class.java)) }
}
