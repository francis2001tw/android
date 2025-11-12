package com.example.smartadvisor.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartadvisor.model.Message
import com.example.smartadvisor.service.GenerationChunk
import com.example.smartadvisor.ui.components.MessageBubble
import com.example.smartadvisor.ui.viewmodel.ChatViewModel
import com.example.smartadvisor.util.LogManager
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onNavigateToDebugLog: () -> Unit = {}
) {
    // 使用 collectAsState() 收集消息列表
    // 注意：由於 Message 是 data class，collectAsState 會使用 equals() 比較
    // 但我們已經在 ViewModel 中使用 messagesVersion 來強制更新
    val messagesRaw by viewModel.messages.collectAsState(initial = emptyList())
    val messagesVersion by viewModel.messagesVersion.collectAsState(initial = 0)

    // 使用 derivedStateOf 結合 messagesVersion 來強制重組
    // 每次 messagesVersion 變化時，都會創建新的 messages 引用
    val messages by remember {
        derivedStateOf {
            // 添加 messagesVersion 作為依賴，確保版本變化時重新計算
            android.util.Log.d("ChatScreen", "📊 derivedStateOf: version=$messagesVersion, messages=${messagesRaw.size}")
            messagesRaw
        }
    }
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val thinkingOverlay by viewModel.thinkingOverlay.collectAsState()

    // Debug: Log overlay changes
    LaunchedEffect(thinkingOverlay) {
        thinkingOverlay.forEach { (messageId, content) ->
            android.util.Log.d("ChatScreen", "📊 Overlay for $messageId: ${content.length} chars")
        }
    }

    // 添加 log 來追蹤收集到的值
    LaunchedEffect(messages.size, messagesVersion) {
        val lastReasoning = messages.lastOrNull()?.parts?.filterIsInstance<com.example.smartadvisor.model.MessagePart.Reasoning>()
            ?.firstOrNull()?.reasoning?.length ?: 0
        android.util.Log.d("ChatScreen", "📥 Collected: ${messages.size} messages, version=$messagesVersion, last reasoning=$lastReasoning chars")
    }

    // 添加日志来追踪 Compose 重组
    val lastReasoning = messages.lastOrNull()?.parts?.filterIsInstance<com.example.smartadvisor.model.MessagePart.Reasoning>()
        ?.firstOrNull()?.reasoning?.length ?: 0
    android.util.Log.d("ChatScreen", "🎨 Recomposed: ${messages.size} messages, version=$messagesVersion, last reasoning=$lastReasoning chars")

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Show error messages
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // You can show a Snackbar here
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Advisor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Debug Log") },
                            onClick = {
                                showMenu = false
                                onNavigateToDebugLog()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Debug Log") },
                            onClick = {
                                showMenu = false
                                // 分享日志文件
                                val logFile = LogManager.getLogFile(context)
                                if (logFile.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        logFile
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Log"))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Debug Log") },
                            onClick = {
                                showMenu = false
                                LogManager.clearLog(context)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() && !isGenerating) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                        scope.launch {
                            listState.animateScrollToItem(messages.size)
                        }
                    }
                },
                isGenerating = isGenerating,
                onCancel = { viewModel.cancelGeneration() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (messages.isEmpty() && !isGenerating) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "👋 Welcome to Smart Advisor!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Start a conversation with QWEN AI",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = messages,
                        // 只使用 message.id 作為 key，讓 Compose 自動檢測 message 對象的變化
                        // 不要在 key 中包含內容相關的信息，否則會導致 LazyColumn 認為是新項目
                        key = { message -> message.id }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            thinkingOverlay = thinkingOverlay[message.id]
                        )
                    }

                    // Show loading indicator when generating
                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Thinking...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var showContextMenu by remember { mutableStateOf(false) }
    var textFieldValue by remember(text) {
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onTextChange(newValue.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showContextMenu = true }
                        ),
                    placeholder = { Text("Type your message...") },
                    enabled = !isGenerating,
                    maxLines = 4
                )

                // Context Menu (Pop-up Menu)
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    offset = DpOffset(0.dp, (-40).dp)
                ) {
                    // Select All
                    DropdownMenuItem(
                        text = { Text("Select All") },
                        onClick = {
                            textFieldValue = textFieldValue.copy(
                                selection = TextRange(0, textFieldValue.text.length)
                            )
                            showContextMenu = false
                        }
                    )

                    // Cut
                    DropdownMenuItem(
                        text = { Text("Cut") },
                        onClick = {
                            val selectedText = textFieldValue.text.substring(
                                textFieldValue.selection.start,
                                textFieldValue.selection.end
                            )
                            if (selectedText.isNotEmpty()) {
                                clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("text", selectedText)
                                )
                                val newText = textFieldValue.text.removeRange(
                                    textFieldValue.selection.start,
                                    textFieldValue.selection.end
                                )
                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(textFieldValue.selection.start)
                                )
                                onTextChange(newText)
                            }
                            showContextMenu = false
                        },
                        enabled = textFieldValue.selection.length > 0
                    )

                    // Copy
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        onClick = {
                            val selectedText = textFieldValue.text.substring(
                                textFieldValue.selection.start,
                                textFieldValue.selection.end
                            )
                            if (selectedText.isNotEmpty()) {
                                clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("text", selectedText)
                                )
                            }
                            showContextMenu = false
                        },
                        enabled = textFieldValue.selection.length > 0
                    )

                    // Paste
                    DropdownMenuItem(
                        text = { Text("Paste") },
                        onClick = {
                            val clipData = clipboardManager.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val pasteText = clipData.getItemAt(0).text.toString()
                                val newText = textFieldValue.text.substring(0, textFieldValue.selection.start) +
                                        pasteText +
                                        textFieldValue.text.substring(textFieldValue.selection.end)
                                val newCursorPos = textFieldValue.selection.start + pasteText.length
                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newCursorPos)
                                )
                                onTextChange(newText)
                            }
                            showContextMenu = false
                        },
                        enabled = clipboardManager.hasPrimaryClip()
                    )
                }
            }

            if (isGenerating) {
                FilledTonalButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Cancel")
                }
            } else {
                FilledIconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

