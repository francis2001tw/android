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
    
    // 使用 MutableStateFlow，但添加一个计数器来强制更新
    private var _messagesUpdateCounter = 0
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // 添加一个更新计数器来强制触发 UI 更新
    private val _messagesVersion = MutableStateFlow(0)
    val messagesVersion: StateFlow<Int> = _messagesVersion.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    
    private val _generationChunks = MutableSharedFlow<GenerationChunk>()
    val generationChunks: SharedFlow<GenerationChunk> = _generationChunks.asSharedFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
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
        viewModelScope.launch {
            chatService.observeConversation(conversationId).collect { conversation ->
                android.util.Log.d("ChatViewModel", "Conversation updated: ${conversation.messageNodes.size} nodes")
                _currentConversation.value = conversation
                val newMessages = conversation.getCurrentMessages()
                android.util.Log.d("ChatViewModel", "Messages updated: ${newMessages.size} messages")

                // 打印每条消息的详细信息
                newMessages.forEachIndexed { index, message ->
                    val reasoningLength = message.parts.filterIsInstance<MessagePart.Reasoning>().firstOrNull()?.reasoning?.length ?: 0
                    android.util.Log.d("ChatViewModel", "  Message[$index] id=${message.id.take(8)}, parts=${message.parts.size}, reasoning=$reasoningLength")
                }

                // 强制创建新列表以触发 StateFlow 更新
                // 使用 toList() 确保是新的列表实例
                val messagesCopy = newMessages.toList()
                android.util.Log.d("ChatViewModel", "Setting messages, old size: ${_messages.value.size}, new size: ${messagesCopy.size}")
                _messages.value = messagesCopy

                // 强制触发版本更新
                _messagesUpdateCounter++
                _messagesVersion.value = _messagesUpdateCounter
                android.util.Log.d("ChatViewModel", "Messages StateFlow updated, version: $_messagesUpdateCounter")
            }
        }
    }
    
    fun sendMessage(text: String) {
        val conversationId = _currentConversation.value?.id ?: return
        
        viewModelScope.launch {
            try {
                val content = listOf(MessagePart.Text(text))
                
                // Send user message
                chatService.sendMessage(
                    conversationId = conversationId,
                    content = content,
                    autoGenerate = false
                ).onSuccess {
                    // Start generating response
                    generateResponse(conversationId)
                }.onFailure { error ->
                    _errorMessage.value = "Failed to send message: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }
    
    private fun generateResponse(conversationId: String) {
        viewModelScope.launch {
            try {
                _isGenerating.value = true
                android.util.Log.d("ChatViewModel", "Starting generation for conversation: $conversationId")

                chatService.generateResponseStream(conversationId).collect { chunk ->
                    android.util.Log.d("ChatViewModel", "Received chunk: ${chunk::class.simpleName}")
                    _generationChunks.emit(chunk)

                    when (chunk) {
                        is GenerationChunk.ThinkingChunk -> {
                            android.util.Log.d("ChatViewModel", "ThinkingChunk: ${chunk.content.take(50)}")
                        }
                        is GenerationChunk.ResponseComplete -> {
                            android.util.Log.d("ChatViewModel", "ResponseComplete")
                            _isGenerating.value = false
                        }
                        is GenerationChunk.Error -> {
                            android.util.Log.e("ChatViewModel", "Error: ${chunk.error.message}")
                            _isGenerating.value = false
                            _errorMessage.value = "Generation error: ${chunk.error.message}"
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

