package com.example.cash_control

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray

class ViewCategories : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_categories)

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

            val container = findViewById<LinearLayout>(R.id.categoriesContainer)
            val placeholder = findViewById<LinearLayout>(R.id.emptyPlaceholder)

            if (jsonArray.length() == 0) {
                placeholder.visibility = View.VISIBLE
            } else {
                placeholder.visibility = View.GONE
                for (i in 0 until jsonArray.length()) {
                    val category = jsonArray.getJSONObject(i)
                    val name = category.getString("name")
                    val amount = category.getDouble("amount")

                    val categoryView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
                    val text1 = categoryView.findViewById<TextView>(android.R.id.text1)
                    val text2 = categoryView.findViewById<TextView>(android.R.id.text2)

                    text1.text = name
                    text1.setTextColor(resources.getColor(R.color.black))
                    text1.textSize = 20f
                    text1.setPadding(0, 10, 0, 0)

                    text2.text = "Remaining: R %.2f".format(amount)
                    text2.setTextColor(resources.getColor(R.color.purple_500))
                    text2.setPadding(0, 0, 0, 10)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 24)
                    categoryView.layoutParams = params
                    categoryView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

                    container.addView(categoryView)
                }
            }
        }
    }

    // NAVIGATION
    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Dash_board::class.java)) }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Profile::class.java)) }
}
