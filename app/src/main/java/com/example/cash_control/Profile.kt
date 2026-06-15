package com.example.cash_control

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

/**
 * Profile Activity manages user details and session termination.
 */
class Profile : AppCompatActivity() {

    private var currentUserEmail: String? = null
    private lateinit var pickPhotoLauncher: ActivityResultLauncher<String>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        currentUserEmail = appPrefs.getString("current_user_email", null)

        pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        val email = currentUserEmail ?: return@registerForActivityResult
                        getSharedPreferences("UserData_$email", MODE_PRIVATE)
                            .edit()
                            .putString("profile_picture", base64)
                            .apply()

                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        findViewById<ImageView>(R.id.profileImage).setImageBitmap(bitmap)
                        findViewById<ImageView>(R.id.profileImage).visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.avatarInitialBg).visibility = View.GONE
                        findViewById<TextView>(R.id.profilePhotoHint).visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val email = currentUserEmail ?: return
        val userSharedPref = getSharedPreferences("UserData_$email", MODE_PRIVATE)
        
        // Attempt to load from Local Prefs first
        var name = userSharedPref.getString("name", "User") ?: "User"
        val userEmail = userSharedPref.getString("email", email) ?: email
        var gender = userSharedPref.getString("gender", "")?.lowercase() ?: ""
        var memberSince = userSharedPref.getString("member_since", "Joined Recently")

        // Update UI with local data
        updateUI(name, userEmail, gender, memberSince)

        // Try to fetch fresher data from Firestore if authenticated
        val user = auth.currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name = document.getString("name") ?: name
                        gender = document.getString("gender") ?: gender
                        memberSince = document.getString("member_since") ?: memberSince
                        updateUI(name, userEmail, gender, memberSince)
                    }
                }
        }

        val initial = name.firstOrNull()?.uppercase() ?: "U"
        findViewById<TextView>(R.id.profileInitial).text = initial

        val photoBase64 = userSharedPref.getString("profile_picture", null)
        if (photoBase64 != null) {
            try {
                val bytes = Base64.decode(photoBase64, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    findViewById<ImageView>(R.id.profileImage).setImageBitmap(bitmap)
                    findViewById<ImageView>(R.id.profileImage).visibility = View.VISIBLE
                    findViewById<LinearLayout>(R.id.avatarInitialBg).visibility = View.GONE
                    findViewById<TextView>(R.id.profilePhotoHint).visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(name: String, email: String, gender: String, memberSince: String?) {
        val title = when (gender) {
            "male" -> "Mr."
            "female" -> "Mrs."
            else -> ""
        }
        findViewById<TextView>(R.id.profileName).text = "$title $name".trim()
        findViewById<TextView>(R.id.profileEmail).text = email

        val displayGender = when (gender) {
            "male" -> "\u2642 Male"
            "female" -> "\u2640 Female"
            else -> ""
        }
        findViewById<TextView>(R.id.profileGender).text = displayGender
        findViewById<TextView>(R.id.profileMemberSince).text = memberSince ?: "Joined Recently"
    }

    fun changeProfilePhoto(@Suppress("UNUSED_PARAMETER") view: View) {
        pickPhotoLauncher.launch("image/*")
    }

    fun logout(@Suppress("UNUSED_PARAMETER") view: View) {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                // Firebase Sign Out
                auth.signOut()

                // Clear Local Session
                val appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                appPrefs.edit().remove("current_user_email").apply()

                val intent = Intent(this, landing_page::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun openHome(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Dash_board::class.java)) }
    fun openHistory(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, History::class.java)) }
    fun openStats(@Suppress("UNUSED_PARAMETER") view: View) { startActivity(Intent(this, Stats::class.java)) }
    fun openProfile(@Suppress("UNUSED_PARAMETER") view: View) {}
}
