package com.example.smartadvisor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartadvisor.ui.ChatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch ChatActivity for normal chat usage
        // Change to TestMainActivity to test Main.kt functionality
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
        finish()
    }
}

