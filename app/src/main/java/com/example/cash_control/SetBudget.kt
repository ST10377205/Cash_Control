package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * SetBudget Activity allows users to define financial goals and syncs them to Firebase.
 */
class SetBudget : AppCompatActivity() {

    private val TAG = "CASH_CONTROL_BUDGET"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_budget)

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        
        loadCurrentGoals()
    }

    private fun loadCurrentGoals() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail != null) {
            val userSharedPref = getSharedPreferences("UserData_" + currentUserEmail, MODE_PRIVATE)
            val minGoal = userSharedPref.getFloat("min_goal", 0f)
            val maxGoal = userSharedPref.getFloat("max_goal", 0f)
            findViewById<TextView>(R.id.txtGoalRange)?.text = "Goal Range: R %.2f - R %.2f".format(minGoal, maxGoal)
        }
    }

    fun saveBudget(@Suppress("UNUSED_PARAMETER") view: View) {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail == null) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show()
            return
        }

        val inputMin = findViewById<EditText>(R.id.inputMinGoal)
        val inputMax = findViewById<EditText>(R.id.inputMaxGoal)
        
        val minText = inputMin.text.toString().trim()
        val maxText = inputMax.text.toString().trim()

        if (minText.isEmpty() || maxText.isEmpty()) {
            Toast.makeText(this, "Enter both min and max goals", Toast.LENGTH_SHORT).show()
            return
        }

        val minGoal = minText.toFloatOrNull() ?: 0f
        val maxGoal = maxText.toFloatOrNull() ?: 0f

        if (minGoal > maxGoal) {
            Toast.makeText(this, "Min goal cannot be greater than Max goal", Toast.LENGTH_SHORT).show()
            return
        }

        val userSharedPref = getSharedPreferences("UserData_" + currentUserEmail, MODE_PRIVATE)
        
        // Update financial data: Add new budget to current income instead of overwriting
        val currentIncome = userSharedPref.getFloat("income", 0f)
        val updatedIncome = currentIncome + maxGoal

        // Increment Budget Planning Achievement
        val currentBudgetTrophies = userSharedPref.getInt("budget_trophy_count", 0)
        val newBudgetTrophies = currentBudgetTrophies + 1

        // 1. SAVE LOCALLY
        userSharedPref.edit()
            .putFloat("min_goal", minGoal)
            .putFloat("max_goal", maxGoal)
            .putFloat("income", updatedIncome)
            .putInt("budget_trophy_count", newBudgetTrophies)
            .apply()

        // 2. SAVE TO FIRESTORE
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            lifecycleScope.launch {
                try {
                    val budgetData = hashMapOf(
                        "min_goal" to minGoal,
                        "max_goal" to maxGoal,
                        "income" to updatedIncome,
                        "budget_trophy_count" to newBudgetTrophies
                    )
                    FirebaseFirestore.getInstance().collection("users")
                        .document(firebaseUser.uid)
                        .update(budgetData as Map<String, Any>)
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync budget to Firestore: ${e.message}")
                }
            }
        }

        findViewById<TextView>(R.id.txtGoalRange)?.text = "Goal Range: R %.2f - R %.2f".format(minGoal, maxGoal)
        
        Toast.makeText(this, "🏆 Achievement Unlocked: Budget Planner Level $newBudgetTrophies!", Toast.LENGTH_LONG).show()
        inputMin.text.clear()
        inputMax.text.clear()
    }

    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { finish() }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
