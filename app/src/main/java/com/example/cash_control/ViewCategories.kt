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
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewCategories Activity displays current category budgets and allows top-ups.
 * Syncs changes with Firestore for cloud persistence.
 */
class ViewCategories : AppCompatActivity() {
    
    private lateinit var container: LinearLayout
    private lateinit var placeholder: LinearLayout
    private val TAG = "CASH_CONTROL_VIEW_CAT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_categories)

        container = findViewById(R.id.categoriesContainer)
        placeholder = findViewById(R.id.emptyPlaceholder)

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        displayCategories()
    }

    private fun displayCategories() {
        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserEmail = appPrefs.getString("current_user_email", null)

        if (currentUserEmail != null) {
            val userSharedPref = getSharedPreferences("UserData_" + currentUserEmail, MODE_PRIVATE)
            val jsonString = userSharedPref.getString("categories", "[]")
            val jsonArray = JSONArray(jsonString)

            container.removeAllViews()

            if (jsonArray.length() == 0) {
                placeholder.visibility = View.VISIBLE
            } else {
                placeholder.visibility = View.GONE
                for (i in 0 until jsonArray.length()) {
                    val categoryJson = jsonArray.getJSONObject(i)
                    val name = categoryJson.getString("name")
                    val amount = categoryJson.getDouble("amount")

                    val categoryView = layoutInflater.inflate(R.layout.item_category, null)
                    val txtName = categoryView.findViewById<TextView>(R.id.txtCategoryName)
                    val txtAmount = categoryView.findViewById<TextView>(R.id.txtCategoryAmount)
                    val btnTopUp = categoryView.findViewById<Button>(R.id.btnTopUp)

                    txtName.text = name
                    txtAmount.text = "Remaining: R %.2f".format(amount)

                    btnTopUp.setOnClickListener {
                        showTopUpDialog(name, i, currentUserEmail)
                    }

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 24)
                    categoryView.layoutParams = params

                    container.addView(categoryView)
                }
            }
        }
    }

    private fun showTopUpDialog(categoryName: String, index: Int, userEmail: String) {
        val input = EditText(this)
        input.hint = "Enter amount"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        
        val padding = (24 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this)
        container.addView(input)
        input.setPadding(padding, padding / 2, padding, padding / 2)

        AlertDialog.Builder(this)
            .setTitle("Top Up $categoryName")
            .setMessage("How much would you like to add to your $categoryName budget?")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val amountText = input.text.toString()
                val amount = amountText.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    performTopUp(categoryName, index, amount, userEmail)
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performTopUp(categoryName: String, index: Int, amount: Double, userEmail: String) {
        val userSharedPref = getSharedPreferences("UserData_$userEmail", MODE_PRIVATE)
        val jsonString = userSharedPref.getString("categories", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        try {
            val categoryJson = jsonArray.getJSONObject(index)
            val oldAmount = categoryJson.getDouble("amount")
            categoryJson.put("amount", oldAmount + amount)

            val currentIncome = userSharedPref.getFloat("income", 0f).toDouble()
            val updatedIncome = currentIncome + amount

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val stf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = Date()

            val transaction = Transaction(
                userEmail = userEmail,
                category = categoryName,
                amount = amount,
                date = sdf.format(now),
                startTime = stf.format(now),
                endTime = stf.format(now),
                description = "Budget Top-up",
                type = "income"
            )

            lifecycleScope.launch {
                try {
                    // 1. SAVE LOCALLY
                    withContext(Dispatchers.IO) {
                        DatabaseProvider.getDatabase(this@ViewCategories).transactionDao().insertTransaction(transaction)
                    }
                    userSharedPref.edit()
                        .putString("categories", jsonArray.toString())
                        .putFloat("income", updatedIncome.toFloat())
                        .apply()

                    // 2. SYNC TO FIRESTORE
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        val firestore = FirebaseFirestore.getInstance()
                        val userRef = firestore.collection("users").document(firebaseUser.uid)
                        
                        firestore.runTransaction { transactionFirestore ->
                            transactionFirestore.update(userRef, "categories", jsonArray.toString())
                            transactionFirestore.update(userRef, "income", updatedIncome)
                            null
                        }.await()

                        val transactionData = hashMapOf(
                            "userEmail" to transaction.userEmail,
                            "category" to transaction.category,
                            "amount" to transaction.amount,
                            "date" to transaction.date,
                            "startTime" to transaction.startTime,
                            "endTime" to transaction.endTime,
                            "description" to transaction.description,
                            "type" to transaction.type,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        userRef.collection("transactions").add(transactionData).await()
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ViewCategories, "R %.2f added to $categoryName".format(amount), Toast.LENGTH_SHORT).show()
                        displayCategories()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error performing top-up: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ViewCategories, "Saved locally. Cloud sync failed.", Toast.LENGTH_SHORT).show()
                        displayCategories()
                    }
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error updating category", Toast.LENGTH_SHORT).show()
        }
    }

    // NAVIGATION
    fun openHome(view: View) { startActivity(Intent(this, Dash_board::class.java)) }
    fun openHistory(view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(view: View) { startActivity(Intent(this, Profile::class.java)) }
}
