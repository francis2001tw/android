package com.universalchat.service

import com.universalchat.model.*
import com.universalchat.provider.Provider
import com.universalchat.repository.ConversationRepository
import com.universalchat.storage.SettingsStore
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

private val logger = KotlinLogging.logger {}

/**
 * 通用对话服务实现
 */
class UniversalChatServiceImpl(
    private val conversationRepository: ConversationRepository,
    private val providerManager: ProviderManager,
    private val settingsStore: SettingsStore,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : UniversalChatService {
    
    // 活跃对话缓存
    private val activeConversations = mutableMapOf<String, MutableStateFlow<Conversation>>()
    
    // 生成任务管理
    private val generationJobs = mutableMapOf<String, Job>()
    
    override suspend fun createConversation(
        assistantId: String,
        initialMessages: List<Message>
    ): Conversation {
        val conversation = Conversation(
            assistantId = assistantId,
            messageNodes = initialMessages.map { MessageNode.of(it) }
        )
        
        conversationRepository.save(conversation)
        activeConversations[conversation.id] = MutableStateFlow(conversation)
        
        logger.info { "Created conversation: ${conversation.id}" }
        return conversation
    }
    
    override suspend fun loadConversation(conversationId: String): Conversation? {
        return conversationRepository.load(conversationId)?.also { conversation ->
            activeConversations[conversationId] = MutableStateFlow(conversation)
        }
    }
    
    override fun observeConversation(conversationId: String): Flow<Conversation> {
        return activeConversations[conversationId]?.asStateFlow()
            ?: flow { emit(loadConversation(conversationId) ?: error("Conversation not found")) }
    }
    
    override suspend fun saveConversation(conversation: Conversation) {
        conversationRepository.save(conversation)
        activeConversations[conversation.id]?.value = conversation
    }
    
    override suspend fun deleteConversation(conversationId: String) {
        conversationRepository.delete(conversationId)
        activeConversations.remove(conversationId)
        generationJobs[conversationId]?.cancel()
        generationJobs.remove(conversationId)
    }
    
    override fun searchConversations(query: String, assistantId: String?): Flow<List<Conversation>> {
        return conversationRepository.search(query, assistantId)
    }
    
    override suspend fun sendMessage(
        conversationId: String,
        content: List<MessagePart>,
        autoGenerate: Boolean
    ): Result<Message> = runCatching {
        logger.info { "Sending message to conversation: $conversationId" }
        
        // 1. 验证输入
        require(content.isNotEmpty()) { "Message content cannot be empty" }
        
        // 2. 创建用户消息
        val userMessage = Message(
            role = MessageRole.USER,
            parts = content,
            createdAt = Clock.System.now()
        )
        
        // 3. 添加到对话
        val conversation = getConversation(conversationId)
        val updatedConversation = conversation.addMessage(userMessage)
        updateConversation(conversationId, updatedConversation)
        
        // 4. 保存对话
        saveConversation(updatedConversation)
        
        // 5. 自动生成响应
        if (autoGenerate) {
            launchGeneration(conversationId)
        }
        
        userMessage
    }
    
    override fun generateResponseStream(
        conversationId: String,
        params: TextGenerationParams?
    ): Flow<GenerationChunk> = flow {
        logger.info { "Generating response stream for conversation: $conversationId" }
        
        // 1. 取消现有生成任务
        cancelGeneration(conversationId)
        
        // 2. 获取对话和设置
        val conversation = getConversation(conversationId)
        val assistant = settingsStore.getAssistant(conversation.assistantId)
        val model = params?.model ?: getDefaultModel(assistant)
        val provider = providerManager.getProvider(model.providerType)
        
        // 3. 准备消息列表
        val messages = prepareMessages(conversation, assistant)
        
        // 4. 构建生成参数
        val generationParams = buildGenerationParams(params, assistant, model)
        
        // 5. 创建初始 AI 消息
        var aiMessage = Message(
            role = MessageRole.ASSISTANT,
            parts = emptyList(),
            createdAt = Clock.System.now(),
            modelId = model.id
        )
        
        // 6. 添加 AI 消息到对话
        val conversationWithAI = conversation.addMessage(aiMessage)
        updateConversation(conversationId, conversationWithAI)
        
        // 7. 流式生成
        var currentThinking = ""
        var currentResponse = ""
        
        provider.streamText(
            messages = messages,
            params = generationParams
        ).collect { chunk ->
            // 处理消息块
            aiMessage = appendChunk(aiMessage, chunk)
            
            // 发送不同类型的块
            chunk.choices.firstOrNull()?.delta?.parts?.forEach { part ->
                when (part) {
                    is MessagePart.Reasoning -> {
                        currentThinking += part.reasoning
                        emit(GenerationChunk.ThinkingChunk(part.reasoning))
                        
                        if (part.finishedAt != null) {
                            emit(GenerationChunk.ThinkingComplete(currentThinking))
                        }
                    }
                    is MessagePart.Text -> {
                        currentResponse += part.text
                        emit(GenerationChunk.ResponseChunk(part.text))
                    }
                    else -> {}
                }
            }
            
            // 更新对话
            updateConversationMessage(conversationId, aiMessage)
            
            // 处理完成
            if (chunk.choices.firstOrNull()?.finishReason != null) {
                val finalUsage = chunk.usage ?: TokenUsage()
                aiMessage = aiMessage.copy(usage = finalUsage)
                
                emit(GenerationChunk.ResponseComplete(
                    message = aiMessage,
                    usage = finalUsage
                ))
                
                // 保存对话
                val finalConversation = getConversation(conversationId)
                    .updateLastMessage(aiMessage)
                saveConversation(finalConversation)
                
                // 后台任务: 生成标题和建议
                launchBackgroundTasks(conversationId)
            }
        }
    }.catch { error ->
        logger.error(error) { "Error generating response" }
        emit(GenerationChunk.Error(error))
    }
    
    override suspend fun generateResponse(
        conversationId: String,
        params: TextGenerationParams?
    ): Result<Message> = runCatching {
        var lastMessage: Message? = null
        
        generateResponseStream(conversationId, params).collect { chunk ->
            if (chunk is GenerationChunk.ResponseComplete) {
                lastMessage = chunk.message
            }
        }
        
        lastMessage ?: error("No response generated")
    }
    
    override fun cancelGeneration(conversationId: String) {
        generationJobs[conversationId]?.cancel()
        generationJobs.remove(conversationId)
        logger.info { "Cancelled generation for conversation: $conversationId" }
    }
    
    override suspend fun regenerateMessage(
        conversationId: String,
        messageId: String
    ): Result<Message> {
        // TODO: Implement regeneration logic
        return Result.failure(NotImplementedError("Regenerate not implemented"))
    }
    
    override suspend fun editMessage(
        conversationId: String,
        messageId: String,
        newContent: List<MessagePart>
    ): Result<Message> {
        // TODO: Implement edit logic
        return Result.failure(NotImplementedError("Edit not implemented"))
    }
    
    override suspend fun deleteMessage(conversationId: String, messageId: String) {
        // TODO: Implement delete logic
    }
    
    override suspend fun generateTitle(conversationId: String): String {
        val conversation = getConversation(conversationId)
        if (conversation.title.isNotBlank()) return conversation.title
        
        val messages = conversation.getCurrentMessages().take(3)
        val summary = messages.joinToString(" ") { it.toText().take(50) }
        val title = "对话: ${summary.take(30)}..."
        
        val updatedConversation = conversation.copy(title = title)
        saveConversation(updatedConversation)
        
        return title
    }
    
    override suspend fun generateSuggestions(conversationId: String): List<String> {
        // TODO: Implement suggestion generation
        return emptyList()
    }
    
    override fun getTokenUsage(conversationId: String): Flow<TokenUsage> = flow {
        val conversation = getConversation(conversationId)
        val totalUsage = conversation.getCurrentMessages()
            .mapNotNull { it.usage }
            .fold(TokenUsage()) { acc, usage -> acc + usage }
        emit(totalUsage)
    }
    
    // ========== Private Helper Methods ==========
    
    private fun getConversation(conversationId: String): Conversation {
        return activeConversations[conversationId]?.value
            ?: error("Conversation not found: $conversationId")
    }
    
    private fun updateConversation(conversationId: String, conversation: Conversation) {
        activeConversations[conversationId]?.value = conversation
    }
    
    private fun updateConversationMessage(conversationId: String, message: Message) {
        val conversation = getConversation(conversationId)
        val updated = conversation.updateLastMessage(message)
        updateConversation(conversationId, updated)
    }

