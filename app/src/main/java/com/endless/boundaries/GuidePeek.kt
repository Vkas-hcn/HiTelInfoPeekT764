package com.endless.boundaries

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.endless.boundaries.databinding.PeekGuideBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GuidePeek : AppCompatActivity() {
    
    private val binding by lazy {
        PeekGuideBinding.inflate(layoutInflater)
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
                    showLoginButtons()
                }
            } else {
                showLoginButtons()
            }
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.guide)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        onBackPressedDispatcher.addCallback(this) {
            // Disable back button
        }
        
        // Initialize
        userPreferences = UserPreferences.getInstance(this)
        auth = FirebaseAuth.getInstance()
        
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Check if already logged in
        if (userPreferences.isLoggedIn) {
            navigateToMain()
        } else {
            setupLoginButtons()
        }
    }
    
    private fun setupLoginButtons() {
        binding.btnLogin.setOnClickListener {
            signInWithGoogle()
        }
        
        binding.tvPass.setOnClickListener {
            // Skip login
            navigateToMain()
        }
    }
    
    private fun signInWithGoogle() {
        showLoading()
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
                    navigateToMain()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                    showLoginButtons()
                }
            }
    }
    
    private fun showLoading() {
        binding.btnLogin.visibility = View.GONE
        binding.tvPass.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }
    
    private fun showLoginButtons() {
        binding.btnLogin.visibility = View.VISIBLE
        binding.tvPass.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }
    
    private fun navigateToMain() {
        showLoading()
        lifecycleScope.launch {
            delay(1500)
            startActivity(Intent(this@GuidePeek, InfoPeek::class.java))
            finish()
        }
    }
    
    companion object {
        private const val TAG = "GuideActivity"
    }
}