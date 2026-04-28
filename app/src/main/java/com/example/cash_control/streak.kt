package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Streak : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_streak)
        
        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        
        loadStreakData()
    }

    private fun loadStreakData() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail != null) {
            val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)
            val streak = userSharedPref.getInt("streak_count", 0)
            val budgetTrophyCount = userSharedPref.getInt("budget_trophy_count", 0)
            
            val streakCountText = findViewById<TextView>(R.id.streakCount)
            streakCountText.text = "$streak Days"
            
            val streakMessage = findViewById<TextView>(R.id.streakMessage)
            
            val badge3 = findViewById<ImageView>(R.id.badge3Day)
            val badge7 = findViewById<ImageView>(R.id.badge7Day)
            val trophy30 = findViewById<ImageView>(R.id.trophy30Day)
            val trophyBig = findViewById<ImageView>(R.id.trophyBig)
            
            val trophyListContainer = findViewById<LinearLayout>(R.id.trophyListContainer)
            trophyListContainer.removeAllViews()

            // 1. ADD STREAK TROPHIES
            val trophyCount = streak / 3
            for (i in 1..trophyCount) {
                val trophyItem = TextView(this)
                trophyItem.text = "🏆 Milestone Trophy $i - Earned at day ${i * 3}"
                trophyItem.textSize = 16f
                trophyItem.setPadding(0, 10, 0, 10)
                trophyItem.setTextColor(resources.getColor(R.color.black))
                trophyListContainer.addView(trophyItem)
            }

            // 2. ADD BUDGETING TROPHIES (New achievement category)
            for (i in 1..budgetTrophyCount) {
                val budgetTrophy = TextView(this)
                budgetTrophy.text = "🏆 Budget Planner Trophy $i - Financial Goal Set!"
                budgetTrophy.textSize = 16f
                budgetTrophy.setPadding(0, 10, 0, 10)
                budgetTrophy.setTextColor(resources.getColor(R.color.black))
                trophyListContainer.addView(budgetTrophy)
            }

            if (streak >= 3) {
                badge3.alpha = 1.0f
                streakMessage.text = "Awesome! You've earned ${trophyCount + budgetTrophyCount} total trophies!"
            }
            
            if (streak >= 7) {
                badge7.alpha = 1.0f
            }
            
            if (streak >= 30) {
                trophy30.alpha = 1.0f
                trophyBig.visibility = View.VISIBLE
            } else {
                trophyBig.visibility = View.GONE
            }
            
            if (streak == 0 && budgetTrophyCount == 0) {
                streakMessage.text = "Start saving daily to earn trophies!"
            } else if (streak == 0 && budgetTrophyCount > 0) {
                streakMessage.text = "Great start! You've earned $budgetTrophyCount Budgeting trophies!"
            }
        }
    }

    // NAVIGATION
    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { finish() }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
