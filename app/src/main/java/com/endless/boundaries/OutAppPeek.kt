package com.endless.boundaries

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.endless.boundaries.databinding.PeekOutAppBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class OutAppPeek : AppCompatActivity() {

    private val binding by lazy {
        PeekOutAppBinding.inflate(layoutInflater)
    }
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize
        userPreferences = UserPreferences.getInstance(this)
        auth = FirebaseAuth.getInstance()
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupImmersiveStatusBar()
        updateUserInfo()
        setupClickListeners()
    }

    private fun setupImmersiveStatusBar() {
        // Set status bar color to match background
        window.statusBarColor = Color.parseColor("#F9F9F9")
        
        // Set status bar icons to dark mode (for light background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Add padding to top bar for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Share
        binding.llShare.setOnClickListener {
            shareApp()
        }

        // Privacy Policy
        binding.llPrivacyPolicy.setOnClickListener {
            openPrivacyPolicy()
        }

        // Login Out
        binding.tvLoginOut.setOnClickListener {
            handleLogout()
        }
    }

    private fun shareApp() {
        try {
            val appPackageName = packageName
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                val shareMessage = "Check out this app: https://play.google.com/store/apps/details?id=$appPackageName"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPrivacyPolicy() {
        try {
            val privacyPolicyUrl = getString(R.string.privacy_policy_url) //TODO replace with your privacy policy URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to open Privacy Policy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserInfo() {
        // Update email display
        val email = userPreferences.userEmail
        if (!email.isNullOrEmpty()) {
            binding.tvAppEmail.text = email
        } else {
            binding.tvAppEmail.text = getString(R.string.app_email)
        }
    }

    private fun handleLogout() {
        // Show logout confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Confirm") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        // Sign out from Firebase
        auth.signOut()
        
        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Clear user preferences
            userPreferences.clearUserInfo()
            
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            
            // Navigate back to Guide Activity
            val intent = Intent(this, GuidePeek::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
