package com.example.deepthinking.ui.filepicker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * File picker screen with custom UI and back button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerScreen(
    onNavigateBack: () -> Unit,
    onFilesSelected: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // System file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedFiles = uris
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Files") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (selectedFiles.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onFilesSelected(selectedFiles)
                                onNavigateBack()
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm")
                        }
                    }
                    IconButton(onClick = { selectedFiles = emptyList() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions
            Text(
                text = "Tap the button below to browse and select files",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Browse button
            Button(
                onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Files")
            }

            // Selected files list
            if (selectedFiles.isNotEmpty()) {
                Divider()
                
                Text(
                    text = "Selected Files (${selectedFiles.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedFiles) { uri ->
                        FileItem(
                            uri = uri,
                            context = context,
                            onRemove = {
                                selectedFiles = selectedFiles.filter { it != uri }
                            }
                        )
                    }
                }

                // Confirm button
                Button(
                    onClick = {
                        onFilesSelected(selectedFiles)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Selection (${selectedFiles.size})")
                }
            }
        }
    }
}

@Composable
private fun FileItem(
    uri: Uri,
    context: Context,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fileName = remember(uri) {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: uri.lastPathSegment ?: "Unknown file"
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

