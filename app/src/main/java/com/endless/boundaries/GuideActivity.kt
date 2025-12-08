package com.endless.boundaries

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.endless.boundaries.databinding.ActivityGuideBinding
import com.endless.boundaries.databinding.ActivityMainBinding
import kotlinx.coroutines.delay

class GuideActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityGuideBinding.inflate(layoutInflater)
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
        }
        lifecycleScope.launchWhenResumed {
            delay(1500)
            startActivity(Intent(this@GuideActivity, MainActivity::class.java))
        }
    }
}