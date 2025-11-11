package com.example.smartadvisor.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.smartadvisor.util.LogManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var logContent by remember { mutableStateOf("Loading...") }
    var autoRefresh by remember { mutableStateOf(true) }
    
    // Auto-refresh log content every second
    LaunchedEffect(autoRefresh) {
        while (autoRefresh) {
            logContent = LogManager.readLog(context)
            delay(1000)
        }
    }
    
    // Initial load
    LaunchedEffect(Unit) {
        logContent = LogManager.readLog(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Auto-refresh toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Auto",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = autoRefresh,
                            onCheckedChange = { autoRefresh = it },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    
                    // Refresh button
                    IconButton(onClick = {
                        logContent = LogManager.readLog(context)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    
                    // Copy ALL button
                    Button(
                        onClick = {
                            try {
                                val fullLog = LogManager.readLog(context)
                                if (fullLog.isEmpty()) {
                                    Toast.makeText(context, "Log is empty", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // 限制最大複製長度為 1MB (1,000,000 chars)
                                val maxLength = 1_000_000
                                val logToCopy = if (fullLog.length > maxLength) {
                                    "Log too large (${fullLog.length} chars), showing last ${maxLength} chars:\n\n" +
                                    fullLog.takeLast(maxLength)
                                } else {
                                    fullLog
                                }

                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Full Debug Log", logToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied ${logToCopy.length} chars", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Copy failed: ${e.message}", Toast.LENGTH_LONG).show()
                                android.util.Log.e("DebugLogScreen", "Copy failed", e)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copy All",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy All", style = MaterialTheme.typography.labelMedium)
                    }
                    
                    // Clear button
                    IconButton(onClick = {
                        LogManager.clearLog(context)
                        logContent = "Log cleared"
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status bar
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lines: ${logContent.lines().size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Size: ${logContent.length / 1024} KB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = if (LogManager.isCapturing()) "● Capturing" else "○ Stopped",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (LogManager.isCapturing()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }
            
            // Log content
            val verticalScrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()
            
            // Auto-scroll to bottom when content changes
            LaunchedEffect(logContent) {
                if (autoRefresh) {
                    verticalScrollState.animateScrollTo(verticalScrollState.maxValue)
                }
            }
            
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.small
            ) {
                SelectionContainer {
                    Text(
                        text = logContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScrollState)
                            .horizontalScroll(horizontalScrollState)
                            .padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

