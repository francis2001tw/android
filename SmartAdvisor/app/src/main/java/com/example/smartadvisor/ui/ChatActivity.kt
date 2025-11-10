package com.example.smartadvisor.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.smartadvisor.ui.screen.ChatScreen
import com.example.smartadvisor.ui.theme.SmartAdvisorTheme
import com.example.smartadvisor.util.LogManager

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 启动自动日志捕获
        LogManager.startCapture(this)
        Log.d("ChatActivity", "Log capture started. Log file: ${LogManager.getLogFilePath(this)}")

        setContent {
            SmartAdvisorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止日志捕获
        LogManager.stopCapture()
        Log.d("ChatActivity", "Log capture stopped")
    }
}

