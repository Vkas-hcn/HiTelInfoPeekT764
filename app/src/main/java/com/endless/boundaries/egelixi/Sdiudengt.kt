package com.endless.boundaries.egelixi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.endless.boundaries.GuidePeek

class Sdiudengt: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, GuidePeek::class.java))
        finish()
    }
}