package com.example.smartadvisor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartadvisor.model.*
import com.example.smartadvisor.provider.DeepSeekProvider
import com.example.smartadvisor.repository.InMemoryConversationRepository
import com.example.smartadvisor.service.GenerationChunk
import com.example.smartadvisor.service.ProviderManager
import com.example.smartadvisor.service.UniversalChatService
import com.example.smartadvisor.service.UniversalChatServiceImpl
import com.example.smartadvisor.storage.InMemorySettingsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow

class ChatViewModel : ViewModel() {

    private val settingsStore = InMemorySettingsStore()
    private val conversationRepository = InMemoryConversationRepository()

    // Create DeepSeek provider setting
    private val deepseekSetting = ProviderSetting(
        type = ProviderType.DEEPSEEK,
        name = "DeepSeek",
        apiKey = "sk-9e0f6612f850465f9057ef5e0d0ce641",
        baseUrl = "https://api.deepseek.com"
    )

    // Create provider map with DeepSeek provider
    private val providers = mapOf(
        ProviderType.DEEPSEEK to DeepSeekProvider(deepseekSetting)
    )
    private val providerManager = ProviderManager(providers)

    private val chatService: UniversalChatService = UniversalChatServiceImpl(
        conversationRepository = conversationRepository,
        providerManager = providerManager,
        settingsStore = settingsStore,
        scope = viewModelScope
    )
    
    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()
    
    // 使用 SharedFlow 代替 StateFlow，避免 conflation 導致更新丟失
    // replay = 1 確保新訂閱者能收到最新值
    // extraBufferCapacity = 64 確保快速更新不會丟失
    private var _messagesUpdateCounter = 0
    private val _messages = MutableSharedFlow<List<Message>>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: Flow<List<Message>> = _messages.asSharedFlow()

    // 添加一个更新计数器来强制触发 UI 更新
    private val _messagesVersion = MutableSharedFlow<Int>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messagesVersion: Flow<Int> = _messagesVersion.asSharedFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // 實時 DeepThinking 覆蓋層（messageId -> 累積內容）
    private val _thinkingOverlay = MutableStateFlow<Map<String, String>>(emptyMap())
    val thinkingOverlay: StateFlow<Map<String, String>> = _thinkingOverlay.asStateFlow()

    private val _generationChunks = MutableSharedFlow<GenerationChunk>()
    val generationChunks: SharedFlow<GenerationChunk> = _generationChunks.asSharedFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        android.util.Log.d("ChatViewModel", "🚀 ChatViewModel init started")
        // 發送初始空值到 SharedFlow
        viewModelScope.launch {
            android.util.Log.d("ChatViewModel", "📤 Emitting initial empty messages to SharedFlow")
            _messages.emit(emptyList())
            _messagesVersion.emit(0)
            android.util.Log.d("ChatViewModel", "✅ Initial values emitted")
        }
        createNewConversation()
    }
    
    private fun createNewConversation() {
        viewModelScope.launch {
            try {
                val assistant = settingsStore.getAssistant("default-assistant")
                    ?: error("Default assistant not found")
                
                val conversation = chatService.createConversation(
                    assistantId = assistant.id,
                    initialMessages = emptyList()
                )
                
                _currentConversation.value = conversation
                observeConversation(conversation.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create conversation: ${e.message}"
            }
        }
    }
    
    private fun observeConversation(conversationId: String) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            chatService.observeConversation(conversationId)
                .distinctUntilChanged { old, new ->
                    // 永远不跳过更新 - 确保每次都触发
                    false
                }
                .collect { conversation ->
                    _currentConversation.value = conversation
                    val newMessages = conversation.getCurrentMessages()

                    // 只打印最後一條消息的 reasoning 長度
                    val lastMessage = newMessages.lastOrNull()
                    val reasoningLength = lastMessage?.parts?.filterIsInstance<MessagePart.Reasoning>()
                        ?.firstOrNull()?.reasoning?.length ?: 0

                    android.util.Log.d("ChatViewModel", "📨 Conversation updated: ${newMessages.size} messages, last reasoning=$reasoningLength chars")

                    // 使用 SharedFlow.emit() 確保每次更新都被發送
                    val messagesCopy = newMessages.toList()
                    _messages.emit(messagesCopy)

                    // 强制触发版本更新
                    _messagesUpdateCounter++
                    _messagesVersion.emit(_messagesUpdateCounter)
                    android.util.Log.d("ChatViewModel", "✅ Messages SharedFlow emitted, version: $_messagesUpdateCounter")
                }
        }
    }
    
    fun sendMessage(text: String) {
        val conversationId = _currentConversation.value?.id ?: run {
            android.util.Log.e("ChatViewModel", "❌ No conversation ID")
            return
        }

        android.util.Log.d("ChatViewModel", "📤 Sending message: ${text.take(50)}...")

        viewModelScope.launch {
            try {
                val content = listOf(MessagePart.Text(text))

                // Send user message
                chatService.sendMessage(
                    conversationId = conversationId,
                    content = content,
                    autoGenerate = false
                ).onSuccess {
                    android.util.Log.d("ChatViewModel", "✅ Message sent, starting generation")
                    // Start generating response
                    generateResponse(conversationId)
                }.onFailure { error ->
                    android.util.Log.e("ChatViewModel", "❌ Failed to send message: ${error.message}")
                    _errorMessage.value = "Failed to send message: ${error.message}"
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "❌ Error sending message", e)
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }
    
    private fun generateResponse(conversationId: String) {
        viewModelScope.launch {
            try {
                _isGenerating.value = true
                android.util.Log.d("ChatViewModel", "🚀 Starting generation for conversation: $conversationId")

                var targetMsgId: String? = null
                var pendingBuffer: StringBuilder? = null

                chatService.generateResponseStream(conversationId).collect { chunk ->
                    android.util.Log.d("ChatViewModel", "📦 Received chunk: ${chunk::class.simpleName}")
                    _generationChunks.emit(chunk)

                    when (chunk) {
                        is GenerationChunk.StreamTarget -> {
                            targetMsgId = chunk.messageId
                            android.util.Log.d("ChatViewModel", "🎯 StreamTarget received: $targetMsgId")
                            // 預先初始化 overlay，立刻進入 loading 狀態（顯示等待文案）
                            val currentOverlay = _thinkingOverlay.value
                            val newOverlay = currentOverlay.toMutableMap().apply {
                                this[targetMsgId!!] = this[targetMsgId!!] ?: ""
                            }
                            _thinkingOverlay.value = newOverlay

                            // 如果在拿到 messageId 前已經暫存了內容，立即回放
                            pendingBuffer?.let { buf ->
                                val delta = buf.toString()
                                if (delta.isNotEmpty()) {
                                    val overlayWithBuffer = _thinkingOverlay.value.toMutableMap().apply {
                                        val prev = this[targetMsgId!!] ?: ""
                                        this[targetMsgId!!] = prev + delta
                                    }
                                    _thinkingOverlay.value = overlayWithBuffer
                                    android.util.Log.d("ChatViewModel", "🔄 Replayed buffered content: ${delta.length} chars")
                                }
                                pendingBuffer = null
                            }
                        }
                        is GenerationChunk.ThinkingChunk -> {
                            val id = targetMsgId ?: _currentConversation.value
                                ?.getCurrentMessages()
                                ?.lastOrNull()
                                ?.id

                            val content = chunk.content
                            if (id != null) {
                                // 將 chunk 內容累加到 overlay（接多少顯示多少）
                                val currentOverlay = _thinkingOverlay.value
                                val prev = currentOverlay[id] ?: ""
                                val newContent = prev + content
                                val newOverlay = currentOverlay.toMutableMap().apply {
                                    this[id] = newContent
                                }
                                _thinkingOverlay.value = newOverlay
                                android.util.Log.d("ChatViewModel", "🧠 ThinkingChunk: +${content.length} chars, overlay now ${newContent.length} chars for $id")
                            } else {
                                //  尚未獲取到 messageId，先暫存，待 StreamTarget 到達後回放
                                if (pendingBuffer == null) pendingBuffer = StringBuilder()
                                pendingBuffer!!.append(content)
                                android.util.Log.w("ChatViewModel", "⏳ Buffering first chunks (${content.length} chars) until messageId is known")
                            }
                        }
                        is GenerationChunk.ThinkingComplete -> {
                            val id = targetMsgId ?: _currentConversation.value
                                ?.getCurrentMessages()
                                ?.lastOrNull()
                                ?.id
                            if (id != null) {
                                val newOverlay = _thinkingOverlay.value.toMutableMap()
                                newOverlay.remove(id)
                                _thinkingOverlay.value = newOverlay
                                android.util.Log.d("ChatViewModel", "🧹 Cleared overlay for $id (ThinkingComplete)")
                            }
                        }
                        is GenerationChunk.ResponseComplete -> {
                            android.util.Log.d("ChatViewModel", "✅ ResponseComplete")
                            _isGenerating.value = false
                            val id = targetMsgId ?: _currentConversation.value
                                ?.getCurrentMessages()
                                ?.lastOrNull()
                                ?.id
                            if (id != null) {
                                val newOverlay = _thinkingOverlay.value.toMutableMap()
                                newOverlay.remove(id)
                                _thinkingOverlay.value = newOverlay
                                android.util.Log.d("ChatViewModel", "🧹 Cleared overlay for $id (ResponseComplete)")
                            }
                        }
                        is GenerationChunk.Error -> {
                            android.util.Log.e("ChatViewModel", "❌ Error: ${chunk.error.message}")
                            _isGenerating.value = false
                            _errorMessage.value = "Generation error: ${chunk.error.message}"
                            val id = targetMsgId ?: _currentConversation.value
                                ?.getCurrentMessages()
                                ?.lastOrNull()
                                ?.id
                            if (id != null) {
                                val newOverlay = _thinkingOverlay.value.toMutableMap()
                                newOverlay.remove(id)
                                _thinkingOverlay.value = newOverlay
                                android.util.Log.d("ChatViewModel", "🧹 Cleared overlay for $id (Error)")
                            }
                        }
                        else -> {
                            android.util.Log.d("ChatViewModel", "Other chunk: ${chunk::class.simpleName}")
                        }
                    }
                }
            } catch (e: Exception) {
                _isGenerating.value = false
                _errorMessage.value = "Generation failed: ${e.message}"
            }
        }
    }
    
    fun cancelGeneration() {
        val conversationId = _currentConversation.value?.id ?: return
        chatService.cancelGeneration(conversationId)
        _isGenerating.value = false
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}

