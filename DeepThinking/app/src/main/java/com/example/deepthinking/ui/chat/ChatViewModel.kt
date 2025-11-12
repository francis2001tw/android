package com.example.deepthinking.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepthinking.data.model.Conversation
import com.example.deepthinking.data.model.ConversationLog
import com.example.deepthinking.data.model.MessageRole
import com.example.deepthinking.data.model.UIMessage
import com.example.deepthinking.data.model.UIMessagePart
import com.example.deepthinking.data.model.handleMessageChunk
import com.example.deepthinking.data.repository.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi

/**
 * ViewModel for chat screen
 */
@OptIn(ExperimentalUuidApi::class)
class ChatViewModel(
    private val repository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversation: Conversation = Conversation()

    init {
        loadConversation()
    }

    /**
     * Load current conversation
     */
    private fun loadConversation() {
        viewModelScope.launch {
            val conversation = repository.getConversationById(currentConversation.id)
            if (conversation != null) {
                currentConversation = conversation
                _uiState.update { it.copy(messages = conversation.messages) }
            }
        }
    }

    /**
     * Send message
     */
    fun sendMessage(text: String, images: List<String> = emptyList(), documents: List<Pair<String, String>> = emptyList()) {
        if (text.isBlank() && images.isEmpty() && documents.isEmpty()) return

        viewModelScope.launch {
            // Create user message
            val parts = mutableListOf<UIMessagePart>()
            if (text.isNotBlank()) {
                parts.add(UIMessagePart.Text(text))
            }
            images.forEach { imageUrl ->
                parts.add(UIMessagePart.Image(imageUrl))
            }
            documents.forEach { (url, fileName) ->
                parts.add(UIMessagePart.Document(url, fileName))
            }

            val userMessage = UIMessage(
                role = MessageRole.USER,
                parts = parts
            )

            // Add user message to conversation
            currentConversation = currentConversation.addMessage(userMessage)
            _uiState.update {
                it.copy(
                    messages = currentConversation.messages,
                    isLoading = true,
                    inputText = ""
                )
            }

            // Save conversation
            repository.saveConversation(currentConversation)

            // Log user message
            repository.addConversationLog(
                ConversationLog(
                    conversationId = currentConversation.id,
                    timestamp = Clock.System.now(),
                    inputType = repository.getInputType(userMessage),
                    outputModel = "deepseek-reasoner",
                    tokenUsage = userMessage.usage ?: com.example.deepthinking.data.model.TokenUsage(),
                    message = userMessage
                )
            )

            // Stream response
            streamResponse()
        }
    }

    /**
     * Stream response from API
     */
    private suspend fun streamResponse() {
        try {
            val messages = currentConversation.messages
            val responseMessages = mutableListOf<UIMessage>()

            repository.streamChatCompletions(messages)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
                .collect { chunk ->
                    val updatedMessages = responseMessages.handleMessageChunk(chunk)
                    responseMessages.clear()
                    responseMessages.addAll(updatedMessages)

                    // Update UI
                    val allMessages = messages + responseMessages
                    _uiState.update {
                        it.copy(
                            messages = allMessages,
                            isLoading = true
                        )
                    }

                    // Update conversation
                    currentConversation = currentConversation.copy(messages = allMessages)
                }

            // Finished streaming
            _uiState.update { it.copy(isLoading = false) }

            // Remove reasoning part from the last message after completion
            val lastMessage = currentConversation.messages.lastOrNull()
            if (lastMessage != null && lastMessage.role == MessageRole.ASSISTANT) {
                val partsWithoutReasoning = lastMessage.parts.filterNot { it is UIMessagePart.Reasoning }
                if (partsWithoutReasoning.size != lastMessage.parts.size) {
                    val updatedMessage = lastMessage.copy(parts = partsWithoutReasoning)
                    val updatedMessages = currentConversation.messages.dropLast(1) + updatedMessage
                    currentConversation = currentConversation.copy(messages = updatedMessages)
                    _uiState.update { it.copy(messages = updatedMessages) }
                }
            }

            // Save conversation
            repository.saveConversation(currentConversation)

            // Log assistant message
            val finalMessage = currentConversation.messages.lastOrNull()
            if (finalMessage != null && finalMessage.role == MessageRole.ASSISTANT) {
                repository.addConversationLog(
                    ConversationLog(
                        conversationId = currentConversation.id,
                        timestamp = Clock.System.now(),
                        inputType = "response",
                        outputModel = finalMessage.modelId ?: "deepseek-reasoner",
                        tokenUsage = finalMessage.usage ?: com.example.deepthinking.data.model.TokenUsage(),
                        message = finalMessage
                    )
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Update input text
     */
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Create new conversation
     */
    fun newConversation() {
        viewModelScope.launch {
            currentConversation = Conversation()
            _uiState.update {
                it.copy(
                    messages = emptyList(),
                    inputText = "",
                    isLoading = false,
                    error = null
                )
            }
            repository.saveConversation(currentConversation)
        }
    }

    /**
     * Get conversation logs as JSON
     */
    fun getConversationLogsJson(): String {
        return repository.getConversationLogsJson()
    }
}

/**
 * UI state for chat screen
 */
data class ChatUiState(
    val messages: List<UIMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

