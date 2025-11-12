package com.example.deepthinking.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.deepthinking.data.model.UIMessagePart
import kotlinx.coroutines.launch
import com.example.deepthinking.ui.components.MessageBubble

/**
 * Chat screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToDebug: () -> Unit,
    onNavigateToFilePicker: () -> Unit = {},
    selectedDocuments: List<Uri> = emptyList(),
    onDocumentsCleared: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Track the last message content to trigger scroll on content changes
    val lastMessageContent by remember {
        derivedStateOf {
            uiState.messages.lastOrNull()?.parts?.joinToString { part ->
                when (part) {
                    is UIMessagePart.Text -> part.text
                    is UIMessagePart.Reasoning -> part.reasoning
                    else -> ""
                }
            } ?: ""
        }
    }

    // Check if there's active reasoning (not finished)
    val hasActiveReasoning = uiState.messages.lastOrNull()?.parts?.any {
        it is UIMessagePart.Reasoning && it.finishedAt == null
    } ?: false

    // Hide keyboard when reasoning is active
    LaunchedEffect(hasActiveReasoning) {
        if (hasActiveReasoning) {
            focusManager.clearFocus()
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        selectedImages = uris
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Handle camera result
        // For now, we'll skip this implementation
    }

    // Auto-scroll to bottom when last message content changes (triggers on every character during streaming)
    // This ensures the entire response is visible as it streams in, including the last line
    LaunchedEffect(lastMessageContent) {
        if (uiState.messages.isNotEmpty()) {
            // Scroll to the last item with a large offset to ensure we see the bottom
            // Using Int.MAX_VALUE as offset forces scroll to the absolute bottom of the item
            listState.scrollToItem(uiState.messages.size - 1, scrollOffset = Int.MAX_VALUE)
        }
    }

    // Additional scroll when new messages are added
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            // Scroll to bottom of the last item
            listState.scrollToItem(uiState.messages.size - 1, scrollOffset = Int.MAX_VALUE)
        }
    }

    // Show error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DeepThinking") },
                actions = {
                    IconButton(onClick = { viewModel.newConversation() }) {
                        Icon(Icons.Default.Add, contentDescription = "New Conversation")
                    }
                    TextButton(onClick = onNavigateToDebug) {
                        Text("Debug")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(
                        message = message,
                        isLoading = uiState.isLoading && message == uiState.messages.lastOrNull()
                    )
                }

                // Loading indicator
                if (uiState.isLoading && uiState.messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Add a spacer at the end to ensure we can scroll past the last message
                // This ensures the last line of long messages is always visible
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Input area - hide when reasoning is active
            if (!hasActiveReasoning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Attachment buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text("📷 Image")
                        }

                        TextButton(onClick = { /* Camera */ }) {
                            Text("📸 Camera")
                        }

                        TextButton(
                            onClick = onNavigateToFilePicker
                        ) {
                            Text("📄 File${if (selectedDocuments.isNotEmpty()) " (${selectedDocuments.size})" else ""}")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { viewModel.updateInputText(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                val images = selectedImages.map { it.toString() }
                                val documents = selectedDocuments.map { it.toString() to it.lastPathSegment.orEmpty() }
                                viewModel.sendMessage(
                                    text = uiState.inputText,
                                    images = images,
                                    documents = documents
                                )
                                selectedImages = emptyList()
                                onDocumentsCleared()
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

