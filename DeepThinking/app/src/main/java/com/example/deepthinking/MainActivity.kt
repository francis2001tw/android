package com.example.deepthinking

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.deepthinking.ui.chat.ChatScreen
import com.example.deepthinking.ui.debug.DebugScreen
import com.example.deepthinking.ui.filepicker.FilePickerScreen
import com.example.deepthinking.ui.theme.DeepThinkingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DeepThinkingApp

        setContent {
            DeepThinkingTheme {
                var showDebug by remember { mutableStateOf(false) }
                var showFilePicker by remember { mutableStateOf(false) }
                var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }

                when {
                    showFilePicker -> {
                        FilePickerScreen(
                            onNavigateBack = { showFilePicker = false },
                            onFilesSelected = { files ->
                                selectedFiles = files
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    showDebug -> {
                        DebugScreen(
                            viewModel = app.chatViewModel,
                            onNavigateBack = { showDebug = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        ChatScreen(
                            viewModel = app.chatViewModel,
                            onNavigateToDebug = { showDebug = true },
                            onNavigateToFilePicker = { showFilePicker = true },
                            selectedDocuments = selectedFiles,
                            onDocumentsCleared = { selectedFiles = emptyList() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}