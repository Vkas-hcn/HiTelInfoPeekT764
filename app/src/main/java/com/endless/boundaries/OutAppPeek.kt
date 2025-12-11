package com.endless.boundaries

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.endless.boundaries.databinding.PeekOutAppBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class OutAppPeek : AppCompatActivity() {

    private val binding by lazy {
        PeekOutAppBinding.inflate(layoutInflater)
    }
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    
    private val googleSignInLauncher: ActivityResultLauncher<Intent> = 
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                    Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

        // Login Or Logout
        binding.tvLoginOrOut.setOnClickListener {
            if (userPreferences.isLoggedIn) {
                handleLogout()
            } else {
                handleLogin()
            }
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
        // Check if user is logged in
        val isLoggedIn = userPreferences.isLoggedIn
        
        if (isLoggedIn) {
            // Logged in: show user info
            val userName = userPreferences.userName
            val email = userPreferences.userEmail
            
            binding.tvUserName.text = userName ?: getString(R.string.app_name)
            binding.tvAppEmail.text = email ?: "-"
            binding.tvLoginOrOut.text = getString(R.string.login_out)
        } else {
            // Not logged in: show default info
            binding.tvUserName.text = getString(R.string.app_name)
            binding.tvAppEmail.text = "-"
            binding.tvLoginOrOut.text = "Login"
        }
    }
    
    private fun handleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
        
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    
                    // Save user info
                    userPreferences.saveUserInfo(
                        email = user?.email,
                        name = user?.displayName,
                        uid = user?.uid
                    )
                    
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    
                    // Update UI
                    updateUserInfo()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
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
            
            // Update UI to show logged out state
            updateUserInfo()
        }
    }
    
    companion object {
        private const val TAG = "OutAppPeek"
    }
}
