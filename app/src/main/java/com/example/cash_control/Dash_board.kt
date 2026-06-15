package com.example.cash_control

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dash_board is the central command center for the user.
 * Now updated to support Lifetime Balance (Carries over) while maintaining Monthly Goals.
 */
class Dash_board : AppCompatActivity() {

    private val TAG = "CASH_CONTROL_DASH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dash_board)
        Log.i(TAG, "Dashboard initializing...")

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
        loadRecentTransactions()
    }

    private fun loadData() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null) ?: return

        val userSharedPref = getSharedPreferences("UserData_$currentUserEmail", MODE_PRIVATE)
        val name = userSharedPref.getString("name", "User") ?: "User"
        
        findViewById<TextView>(R.id.welcomeText)?.text = "Welcome, $name"
        findViewById<TextView>(R.id.userEmail)?.text = currentUserEmail
        findViewById<TextView>(R.id.dashProfileInitial)?.text = name.firstOrNull()?.uppercase() ?: "U"

        val photoBase64 = userSharedPref.getString("profile_picture", null)
        if (photoBase64 != null) {
            try {
                val bytes = Base64.decode(photoBase64, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                findViewById<ImageView>(R.id.dashProfileImage)?.setImageBitmap(bitmap)
                findViewById<ImageView>(R.id.dashProfileImage)?.visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.dashAvatarInitialBg)?.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile image", e)
            }
        }

        val db = DatabaseProvider.getDatabase(this)
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        lifecycleScope.launch(Dispatchers.IO) {
            // LIFETIME CALCULATIONS (For Balance)
            val totalExpenses = db.transactionDao().getTotalSum(currentUserEmail, "expense")
            val totalIncome = userSharedPref.getFloat("income", 0f).toDouble()
            val lifetimeBalance = totalIncome - totalExpenses

            // MONTHLY CALCULATIONS (For Goal Status)
            val monthlyExpenses = db.transactionDao().getMonthlySum(currentUserEmail, "expense", currentMonth)
            
            val minGoal = userSharedPref.getFloat("min_goal", 0f).toDouble()
            val maxGoal = userSharedPref.getFloat("max_goal", 0f).toDouble()

            withContext(Dispatchers.Main) {
                // Update Balance UI (Lifetime)
                findViewById<TextView>(R.id.txtBalance)?.text = "R " + "%.2f".format(lifetimeBalance)
                findViewById<TextView>(R.id.txtIncome)?.text = "R " + "%.2f".format(totalIncome)
                findViewById<TextView>(R.id.txtExpenses)?.text = "R " + "%.2f".format(monthlyExpenses)

                // GOAL STATUS (Still based on monthly spending)
                val statusMsg = findViewById<TextView>(R.id.txtGoalStatusMsg)
                findViewById<TextView>(R.id.txtMinLabel)?.text = "Min Goal: R " + "%.0f".format(minGoal)
                findViewById<TextView>(R.id.txtMaxLabel)?.text = "Max Goal: R " + "%.0f".format(maxGoal)
                
                if (maxGoal <= 0.0) {
                    statusMsg?.text = "Set monthly goals in 'Set Budget'"
                    statusMsg?.setTextColor(android.graphics.Color.GRAY)
                } else {
                    when {
                        monthlyExpenses < minGoal -> {
                            statusMsg?.text = "🔵 Below Min Goal (Safe)"
                            statusMsg?.setTextColor(android.graphics.Color.parseColor("#2563EB"))
                        }
                        monthlyExpenses in minGoal..maxGoal -> {
                            statusMsg?.text = "🟢 On Track (Optimal Range)"
                            statusMsg?.setTextColor(android.graphics.Color.parseColor("#22C55E"))
                        }
                        else -> {
                            statusMsg?.text = "🔴 Over Max Goal (Overspending!)"
                            statusMsg?.setTextColor(android.graphics.Color.parseColor("#DC2626"))
                        }
                    }
                }

                // MILESTONES (Based on monthly goal progress)
                val utilizationPercent = if (maxGoal > 0.0) ((monthlyExpenses / maxGoal) * 100).toInt() else 0
                findViewById<ProgressBar>(R.id.goalProgressBar)?.progress = utilizationPercent.coerceIn(0, 100)

                val milestones = listOf(25, 50, 75, 100)
                val dotIds = listOf(R.id.milestoneDot1, R.id.milestoneDot2, R.id.milestoneDot3, R.id.milestoneDot4)
                val labelIds = listOf(R.id.milestoneLabel1, R.id.milestoneLabel2, R.id.milestoneLabel3, R.id.milestoneLabel4)

                for (i in milestones.indices) {
                    val achieved = utilizationPercent >= milestones[i]
                    findViewById<ImageView>(dotIds[i])?.setImageResource(
                        if (achieved) R.drawable.milestone_dot_achieved else R.drawable.milestone_dot_pending
                    )
                    findViewById<TextView>(labelIds[i])?.setTextColor(
                        if (achieved) android.graphics.Color.parseColor("#2E7D32") else android.graphics.Color.parseColor("#9CA3AF")
                    )
                }

                val next = milestones.firstOrNull { utilizationPercent < it }
                val nextMsg = if (maxGoal > 0.0 && next != null) {
                    val left = (maxGoal * next / 100) - monthlyExpenses
                    "Next reward milestone: $next% (R " + "%.2f".format(left) + " left)"
                } else if (maxGoal > 0.0) "All budget milestones reached! \uD83C\uDFC6" else ""
                
                findViewById<TextView>(R.id.txtMilestoneStatus)?.text = nextMsg
                Log.d(TAG, "Dashboard Loaded - Lifetime Balance: $lifetimeBalance, Monthly Usage: $utilizationPercent%")
            }
        }
    }

    private fun loadRecentTransactions() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null) ?: return
        val db = DatabaseProvider.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = db.transactionDao().getRecentTransactions(currentUserEmail)
            withContext(Dispatchers.Main) {
                if (isFinishing) return@withContext
                val emptyState = findViewById<LinearLayout>(R.id.emptyState)
                val transactionsList = findViewById<LinearLayout>(R.id.transactionsList)

                if (transactions.isEmpty()) {
                    emptyState?.visibility = View.VISIBLE
                    transactionsList?.visibility = View.GONE
                } else {
                    emptyState?.visibility = View.GONE
                    transactionsList?.visibility = View.VISIBLE

                    val rowIds = listOf(R.id.transactionRow1, R.id.transactionRow2, R.id.transactionRow3)
                    val iconBgIds = listOf(R.id.imgTransIconBg1, R.id.imgTransIconBg2, R.id.imgTransIconBg3)
                    val iconIds = listOf(R.id.imgTransIcon1, R.id.imgTransIcon2, R.id.imgTransIcon3)
                    val descIds = listOf(R.id.txtTransDesc1, R.id.txtTransDesc2, R.id.txtTransDesc3)
                    val dateIds = listOf(R.id.txtTransDate1, R.id.txtTransDate2, R.id.txtTransDate3)
                    val amountIds = listOf(R.id.txtTransAmount1, R.id.txtTransAmount2, R.id.txtTransAmount3)

                    for (i in 0 until 3) {
                        val row = findViewById<View>(rowIds[i])
                        if (i < transactions.size) {
                            val t = transactions[i]
                            findViewById<TextView>(descIds[i])?.text = t.category
                            findViewById<TextView>(dateIds[i])?.text = t.date
                            
                            val iconBg = findViewById<View>(iconBgIds[i])
                            val icon = findViewById<ImageView>(iconIds[i])
                            val amountView = findViewById<TextView>(amountIds[i])
                            
                            if (t.type == "income") {
                                amountView?.text = "+ R " + "%.2f".format(t.amount)
                                amountView?.setTextColor(android.graphics.Color.parseColor("#22C55E"))
                                iconBg?.setBackgroundResource(R.drawable.bg_icon_budget)
                                icon?.setImageResource(R.drawable.wallet_24)
                                icon?.setColorFilter(android.graphics.Color.parseColor("#16A34A"))
                            } else {
                                amountView?.text = "- R " + "%.2f".format(t.amount)
                                amountView?.setTextColor(android.graphics.Color.parseColor("#DC2626"))
                                iconBg?.setBackgroundResource(R.drawable.bg_icon_expense)
                                icon?.setImageResource(R.drawable.exchange_24)
                                icon?.setColorFilter(android.graphics.Color.parseColor("#DC2626"))
                            }
                            row?.visibility = View.VISIBLE
                        } else row?.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun openSetBudget(view: View) { startActivity(Intent(this, SetBudget::class.java)) }
    fun openCreateCategory(view: View) { startActivity(Intent(this, CreateCategory::class.java)) }
    fun openAddExpense(view: View) { startActivity(Intent(this, Add_expense::class.java)) }
    fun openStreak(view: View) { startActivity(Intent(this, Streak::class.java)) }
    fun openSavingsGoals(view: View) { startActivity(Intent(this, SavingsGoals::class.java)) }
    fun openKnowledgeHub(view: View) { startActivity(Intent(this, FinancialKnowledge::class.java)) }
    fun openHome(view: View) { /* On Home */ }
    fun openHistory(view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(view: View) { startActivity(Intent(this, Profile::class.java)) }
}
