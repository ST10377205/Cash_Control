package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
        if (currentUserEmail == null) return

        val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)
        val streak = userSharedPref.getInt("streak_count", 0)
        val bestStreak = userSharedPref.getInt("best_streak", 0)
        val budgetTrophyCount = userSharedPref.getInt("budget_trophy_count", 0)

        val trophyCount = streak / 3
        val totalTrophies = trophyCount + budgetTrophyCount

        // Streak counter
        findViewById<TextView>(R.id.streakCount).text = streak.toString()
        findViewById<TextView>(R.id.currentStreakLabel).text = "$streak days"
        findViewById<TextView>(R.id.bestStreakLabel).text = "$bestStreak days"
        findViewById<TextView>(R.id.trophyCountLabel).text = totalTrophies.toString()

        // Streak message
        val message = when {
            streak == 0 -> "Start tracking daily to build your streak! \uD83D\uDCAA"
            streak < 3 -> "$streak day streak! Keep the momentum going! \uD83D\uDD25"
            streak < 7 -> "$streak day streak! You're on fire! \uD83D\uDD25"
            streak < 30 -> "$streak day streak! Unstoppable! \uD83D\uDCA5"
            streak < 100 -> "$streak day streak! You're a legend! \uD83C\uDFC6"
            else -> "$streak day streak! Absolutely incredible! \uD83D\uDC51"
        }
        findViewById<TextView>(R.id.streakMessage).text = message

        // Badges
        val badgeMilestones = listOf(3, 7, 30, 100)
        val badgeViews = listOf(
            findViewById<ImageView>(R.id.badge3Day),
            findViewById<ImageView>(R.id.badge7Day),
            findViewById<ImageView>(R.id.badge30Day),
            findViewById<ImageView>(R.id.badge100Day)
        )
        val badgeLabels = listOf(
            findViewById<TextView>(R.id.badge3Label),
            findViewById<TextView>(R.id.badge7Label),
            findViewById<TextView>(R.id.badge30Label),
            findViewById<TextView>(R.id.badge100Label)
        )
        val badgeStatuses = listOf(
            findViewById<TextView>(R.id.badge3Status),
            findViewById<TextView>(R.id.badge7Status),
            findViewById<TextView>(R.id.badge30Status),
            findViewById<TextView>(R.id.badge100Status)
        )

        // Find next badge for progress calculation
        var nextMilestone: Int? = null
        for (i in badgeMilestones.indices) {
            val earned = streak >= badgeMilestones[i]
            val badgeView = badgeViews[i]
            val labelView = badgeLabels[i]
            val statusView = badgeStatuses[i]

            if (earned) {
                badgeView.alpha = 1.0f
                statusView.text = "\u2713 Earned"
                statusView.setTextColor(android.graphics.Color.parseColor("#16A34A"))
            } else {
                badgeView.alpha = 0.25f
                val remaining = badgeMilestones[i] - streak
                statusView.text = "$remaining more days"
                statusView.setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
                if (nextMilestone == null) {
                    nextMilestone = badgeMilestones[i]
                }
            }
        }

        // Progress toward next badge
        val progressBar = findViewById<ProgressBar>(R.id.badgeProgress)
        val nextBadgeLabel = findViewById<TextView>(R.id.nextBadgeLabel)

        if (streak >= 100) {
            nextBadgeLabel.text = "All badges earned! You're a saving legend!"
            progressBar.progress = 100
        } else if (nextMilestone != null) {
            val prevMilestone = when {
                streak >= 30 -> 30
                streak >= 7 -> 7
                streak >= 3 -> 3
                else -> 0
            }
            val range = nextMilestone - prevMilestone
            val current = streak - prevMilestone
            val percent = (current.toFloat() / range * 100).toInt().coerceIn(0, 100)
            val badgeName = when (nextMilestone) {
                3 -> "3 Day Saver"
                7 -> "Weekly Warrior"
                30 -> "Saving Legend"
                100 -> "Century Club"
                else -> ""
            }
            val remaining = nextMilestone - streak
            nextBadgeLabel.text = "$remaining more days until \"$badgeName\" badge!"
            progressBar.progress = percent
        } else {
            nextBadgeLabel.text = ""
            progressBar.progress = 0
        }

        // Trophy list (summary only)
        val trophyListContainer = findViewById<LinearLayout>(R.id.trophyListContainer)
        val emptyTrophyText = findViewById<TextView>(R.id.emptyTrophyText)
        trophyListContainer.removeAllViews()

        if (totalTrophies == 0) {
            emptyTrophyText.visibility = View.VISIBLE
        } else {
            emptyTrophyText.visibility = View.GONE

            if (trophyCount > 0) {
                val card = createTrophyCard(
                    emoji = "\uD83C\uDFC6",
                    title = "$trophyCount Milestone Trophies",
                    subtitle = "Earned every 3 days of streak",
                    color = "#FFF7ED",
                    accentColor = "#EA580C"
                )
                trophyListContainer.addView(card)
                animateTrophy(card, 0)
            }

            if (budgetTrophyCount > 0) {
                val card = createTrophyCard(
                    emoji = "\uD83C\uDFC6",
                    title = "$budgetTrophyCount Budget Trophies",
                    subtitle = "Awarded for setting financial goals",
                    color = "#F0FDF4",
                    accentColor = "#16A34A"
                )
                trophyListContainer.addView(card)
                animateTrophy(card, 1)
            }
        }
    }

    private fun createTrophyCard(emoji: String, title: String, subtitle: String, color: String, accentColor: String): android.widget.LinearLayout {
        val dp = resources.displayMetrics.density

        val card = android.widget.LinearLayout(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).also { it.setMargins(0, (4 * dp).toInt(), 0, 0) }
        card.orientation = android.widget.LinearLayout.HORIZONTAL
        card.setPadding((12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt(), (12 * dp).toInt())
        card.elevation = 2f
        val gd = android.graphics.drawable.GradientDrawable()
        gd.cornerRadius = (12 * dp).toFloat()
        gd.setColor(android.graphics.Color.parseColor(color))
        card.background = gd

        val emojiView = TextView(this)
        emojiView.text = emoji
        emojiView.textSize = 28f
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = android.view.Gravity.CENTER_VERTICAL
        emojiView.layoutParams = lp

        val textContainer = android.widget.LinearLayout(this)
        textContainer.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).also { it.setMargins((12 * dp).toInt(), 0, 0, 0) }
        textContainer.orientation = android.widget.LinearLayout.VERTICAL

        val titleView = TextView(this)
        titleView.text = title
        titleView.textSize = 14f
        titleView.setTextColor(resources.getColor(R.color.black))
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)

        val subtitleView = TextView(this)
        subtitleView.text = subtitle
        subtitleView.textSize = 12f
        subtitleView.setTextColor(android.graphics.Color.parseColor(accentColor))
        subtitleView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).also { it.topMargin = (2 * dp).toInt() }

        textContainer.addView(titleView)
        textContainer.addView(subtitleView)

        card.addView(emojiView)
        card.addView(textContainer)

        return card
    }

    private fun animateTrophy(view: View, index: Int) {
        view.alpha = 0f
        view.translationY = 40f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350)
            .setStartDelay((index * 80).toLong())
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()
    }

    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { finish() }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
