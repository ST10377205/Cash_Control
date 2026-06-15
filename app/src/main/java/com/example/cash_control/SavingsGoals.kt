package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * SavingsGoals Activity manages financial targets and syncs them with Firestore.
 */
class SavingsGoals : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var goalNameInput: EditText
    private lateinit var targetAmountInput: EditText
    private var currentUserEmail: String? = null
    private val TAG = "CASH_CONTROL_GOALS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_savings_goals)

        container = findViewById(R.id.goalsContainer)
        goalNameInput = findViewById(R.id.inputGoalName)
        targetAmountInput = findViewById(R.id.inputTargetAmount)

        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        currentUserEmail = appPrefs.getString("current_user_email", null)

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        loadGoals()
    }

    fun createGoal(view: View) {
        val email = currentUserEmail ?: return
        val name = goalNameInput.text.toString().trim()
        val amountText = targetAmountInput.text.toString().trim()

        if (name.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val target = amountText.toDoubleOrNull() ?: 0.0
        if (target <= 0) {
            Toast.makeText(this, "Enter a valid target amount", Toast.LENGTH_SHORT).show()
            return
        }

        val newGoal = SavingsGoal(userEmail = email, goalName = name, targetAmount = target)

        lifecycleScope.launch {
            try {
                // 1. SAVE LOCALLY
                withContext(Dispatchers.IO) {
                    DatabaseProvider.getDatabase(this@SavingsGoals).savingsGoalDao().insertGoal(newGoal)
                }

                // 2. SAVE TO FIRESTORE
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val goalData = hashMapOf(
                        "goalName" to newGoal.goalName,
                        "targetAmount" to newGoal.targetAmount,
                        "currentAmount" to newGoal.currentAmount,
                        "userEmail" to newGoal.userEmail
                    )
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .collection("savings_goals").add(goalData).await()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SavingsGoals, "Goal Created!", Toast.LENGTH_SHORT).show()
                    goalNameInput.text.clear()
                    targetAmountInput.text.clear()
                    loadGoals()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating goal: ${e.message}")
            }
        }
    }

    private fun loadGoals() {
        val email = currentUserEmail ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val goals = DatabaseProvider.getDatabase(this@SavingsGoals).savingsGoalDao().getGoalsForUser(email)
            withContext(Dispatchers.Main) {
                displayGoals(goals)
            }
        }
    }

    private fun displayGoals(goals: List<SavingsGoal>) {
        container.removeAllViews()
        for (goal in goals) {
            val goalView = layoutInflater.inflate(R.layout.item_savings_goal, null)
            val txtName = goalView.findViewById<TextView>(R.id.txtGoalName)
            val txtProgress = goalView.findViewById<TextView>(R.id.txtGoalProgress)
            val progressBar = goalView.findViewById<ProgressBar>(R.id.goalProgressBar)
            val btnAdd = goalView.findViewById<Button>(R.id.btnContribute)
            val btnDelete = goalView.findViewById<ImageButton>(R.id.btnDeleteGoal)

            txtName.text = goal.goalName
            val percent = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount * 100).toInt() else 0
            txtProgress.text = "R %.2f / R %.2f (%d%%)".format(goal.currentAmount, goal.targetAmount, percent)
            progressBar.progress = percent.coerceIn(0, 100)

            btnAdd.setOnClickListener { showContributeDialog(goal) }
            btnDelete.setOnClickListener { deleteGoal(goal) }

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 32)
            goalView.layoutParams = params

            container.addView(goalView)
        }
    }

    private fun showContributeDialog(goal: SavingsGoal) {
        val input = EditText(this)
        input.hint = "Amount to contribute (R)"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(48, 24, 48, 24)
        val frame = FrameLayout(this)
        frame.addView(input)
        input.layoutParams = lp

        AlertDialog.Builder(this)
            .setTitle("Add to ${goal.goalName}")
            .setView(frame)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) updateGoalAmount(goal, amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateGoalAmount(goal: SavingsGoal, amount: Double) {
        lifecycleScope.launch {
            try {
                val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
                
                // 1. UPDATE LOCALLY
                withContext(Dispatchers.IO) {
                    DatabaseProvider.getDatabase(this@SavingsGoals).savingsGoalDao().updateGoal(updatedGoal)
                }

                // 2. UPDATE FIRESTORE
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val db = FirebaseFirestore.getInstance()
                    val goalsRef = db.collection("users").document(user.uid).collection("savings_goals")
                    
                    // Find the document with the same name (simple matching for this implementation)
                    val query = goalsRef.whereEqualTo("goalName", goal.goalName).get().await()
                    for (doc in query.documents) {
                        doc.reference.update("currentAmount", updatedGoal.currentAmount).await()
                    }
                }

                withContext(Dispatchers.Main) {
                    loadGoals()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating goal: ${e.message}")
            }
        }
    }

    private fun deleteGoal(goal: SavingsGoal) {
        lifecycleScope.launch {
            try {
                // 1. DELETE LOCALLY
                withContext(Dispatchers.IO) {
                    DatabaseProvider.getDatabase(this@SavingsGoals).savingsGoalDao().deleteGoal(goal)
                }

                // 2. DELETE FROM FIRESTORE
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val db = FirebaseFirestore.getInstance()
                    val goalsRef = db.collection("users").document(user.uid).collection("savings_goals")
                    val query = goalsRef.whereEqualTo("goalName", goal.goalName).get().await()
                    for (doc in query.documents) {
                        doc.reference.delete().await()
                    }
                }

                withContext(Dispatchers.Main) {
                    loadGoals()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting goal: ${e.message}")
            }
        }
    }

    fun openHome(view: View) { finish() }
    fun openHistory(view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(view: View) { startActivity(Intent(this, Profile::class.java)) }
}
