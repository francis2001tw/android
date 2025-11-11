package com.example.smartadvisor.service

import com.example.smartadvisor.model.*
import com.example.smartadvisor.provider.Provider
import com.example.smartadvisor.repository.ConversationRepository
import com.example.smartadvisor.storage.SettingsStore
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
        android.util.Log.d("ChatServiceImpl", "🚀 generateResponseStream started for: $conversationId")

        // 1. 取消现有生成任务
        cancelGeneration(conversationId)

        // 2. 获取对话和设置
        val conversation = getConversation(conversationId)
        android.util.Log.d("ChatServiceImpl", "📖 Conversation loaded: ${conversation.messageNodes.size} nodes")

        val assistant = settingsStore.getAssistant(conversation.assistantId)
        android.util.Log.d("ChatServiceImpl", "🤖 Assistant: ${assistant.name}")

        val model = params?.model ?: getDefaultModel(assistant)
        android.util.Log.d("ChatServiceImpl", "🎯 Model: ${model.id}, Provider: ${model.providerType}")

        val provider = providerManager.getProvider(model.providerType)
        android.util.Log.d("ChatServiceImpl", "🔌 Provider: ${provider::class.simpleName}")

        // 3. 准备消息列表
        val messages = prepareMessages(conversation, assistant)
        android.util.Log.d("ChatServiceImpl", "📝 Prepared ${messages.size} messages for API")

        // 4. 构建生成参数
        val generationParams = buildGenerationParams(params, assistant, model)

        // 5. 创建初始 AI 消息，立即包含一个空的 Reasoning part（用于显示 DeepThinking 动画）
        var aiMessage = Message(
            role = MessageRole.ASSISTANT,
            parts = listOf(
                MessagePart.Reasoning(
                    reasoning = "",
                    createdAt = Clock.System.now(),
                    finishedAt = null
                )
            ),
            createdAt = Clock.System.now(),
            modelId = model.id
        )

        // 6. 添加 AI 消息到对话
        val conversationWithAI = conversation.addMessage(aiMessage)
        updateConversation(conversationId, conversationWithAI)
        android.util.Log.d("ChatServiceImpl", "✅ Initial AI message added")

        // 立即发送 ThinkingChunk 事件，触发 DeepThinking 动画显示
        emit(GenerationChunk.ThinkingChunk(""))
        android.util.Log.d("ChatServiceImpl", "📤 Emitted initial ThinkingChunk")

        // 7. 流式生成
        var currentThinking = ""
        var currentResponse = ""

        android.util.Log.d("ChatServiceImpl", "🌊 Starting provider.streamText...")

        provider.streamText(
            messages = messages,
            params = generationParams
        ).collect { chunk ->
            android.util.Log.d("ChatServiceImpl", "📦 Received chunk from provider")
            // 发送不同类型的块
            chunk.choices.firstOrNull()?.delta?.parts?.forEach { part ->
                when (part) {
                    is MessagePart.Reasoning -> {
                        currentThinking += part.reasoning
                        // 只記錄長度，不記錄內容
                        android.util.Log.d("ChatServiceImpl", "📥 Reasoning chunk received: +${part.reasoning.length} chars, total: ${currentThinking.length} chars")
                        emit(GenerationChunk.ThinkingChunk(part.reasoning))

                        if (part.finishedAt != null) {
                            emit(GenerationChunk.ThinkingComplete(currentThinking))
                        }
                    }
                    is MessagePart.Text -> {
                        currentResponse += part.text
                        // 所有文本响应都显示在 DeepThinking 中
                        currentThinking += part.text
                        android.util.Log.d("ChatServiceImpl", "📥 Text chunk received: +${part.text.length} chars, total: ${currentThinking.length} chars")
                        emit(GenerationChunk.ThinkingChunk(part.text))
                    }
                    else -> {}
                }
            }

            // 处理消息块 - 更新 aiMessage（在接收过程中）
            val oldReasoningLength = aiMessage.parts.filterIsInstance<MessagePart.Reasoning>()
                .firstOrNull()?.reasoning?.length ?: 0
            aiMessage = appendChunk(aiMessage, chunk)
            val newReasoningLength = aiMessage.parts.filterIsInstance<MessagePart.Reasoning>()
                .firstOrNull()?.reasoning?.length ?: 0
            android.util.Log.d("ChatServiceImpl", "🔄 Message updated: reasoning ${oldReasoningLength} -> ${newReasoningLength} chars")

            // 更新对话 - 这会触发 UI 刷新
            updateConversationMessage(conversationId, aiMessage)

            // 处理完成
            if (chunk.choices.firstOrNull()?.finishReason != null) {
                // 发送 ThinkingComplete
                if (currentThinking.isNotEmpty()) {
                    emit(GenerationChunk.ThinkingComplete(currentThinking))
                }

                // 完成后：保留 Reasoning + Text（与 rikkahub 一致，MessagePart 中显示全部数据）
                val finalUsage = chunk.usage ?: TokenUsage()
                aiMessage = aiMessage.copy(
                    parts = aiMessage.parts.map { part ->
                        if (part is MessagePart.Reasoning && part.finishedAt == null) {
                            part.copy(finishedAt = Clock.System.now())
                        } else part
                    },
                    usage = finalUsage
                )

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
        val flow = activeConversations[conversationId]
        if (flow != null) {
            // 强制触发 StateFlow 更新 - 创建新的 Conversation 实例确保引用变化
            // 这样即使内容看起来相同，StateFlow 也会发出新值
            flow.value = conversation.copy(updateAt = Clock.System.now())
            android.util.Log.d("ChatServiceImpl", "✅ StateFlow updated, nodes: ${conversation.messageNodes.size}")
        } else {
            android.util.Log.e("ChatServiceImpl", "❌ No active flow for conversation $conversationId")
        }
    }

    private fun updateConversationMessage(conversationId: String, message: Message) {
        val conversation = getConversation(conversationId)
        val reasoningLength = message.parts.filterIsInstance<MessagePart.Reasoning>()
            .firstOrNull()?.reasoning?.length ?: 0
        android.util.Log.d("ChatServiceImpl", "🔄 Update message: reasoning=$reasoningLength chars")

        val updated = conversation.updateLastMessage(message)
        updateConversation(conversationId, updated)
    }

    private fun launchGeneration(conversationId: String) {
        val job = scope.launch {
            generateResponseStream(conversationId).collect { /* consume stream */ }
        }
        generationJobs[conversationId] = job
    }

    private fun getDefaultModel(assistant: Assistant): Model {
        val modelId = assistant.chatModelId ?: error("No model configured for assistant")
        return settingsStore.getModel(modelId) ?: error("Model not found: $modelId")
    }

    private fun prepareMessages(conversation: Conversation, assistant: Assistant): List<Message> {
        val messages = mutableListOf<Message>()

        // 1. Add system prompt
        if (assistant.systemPrompt.isNotBlank()) {
            messages.add(Message(
                role = MessageRole.SYSTEM,
                parts = listOf(MessagePart.Text(assistant.systemPrompt)),
                createdAt = Clock.System.now()
            ))
        }

        // 2. Add preset messages
        messages.addAll(assistant.presetMessages)

        // 3. Add conversation history (limited)
        val historyMessages = conversation.getCurrentMessages()
            .takeLast(assistant.contextMessageSize)
        messages.addAll(historyMessages)

        return messages
    }

    private fun buildGenerationParams(
        params: TextGenerationParams?,
        assistant: Assistant,
        model: Model
    ): TextGenerationParams {
        return TextGenerationParams(
            model = model,
            temperature = params?.temperature ?: assistant.temperature ?: 0.7f,
            topP = params?.topP ?: assistant.topP ?: 0.9f,
            maxTokens = params?.maxTokens ?: assistant.maxTokens ?: 2048,
            thinkingBudget = params?.thinkingBudget ?: assistant.thinkingBudget ?: 1024,
            tools = params?.tools ?: emptyList(),
            customHeaders = params?.customHeaders ?: assistant.customHeaders,
            customBody = params?.customBody ?: assistant.customBodies
        )
    }

    private fun appendChunk(message: Message, chunk: MessageChunk): Message {
        val delta = chunk.choices.firstOrNull()?.delta ?: return message

        // 使用 fold 来累积更新 parts，参考 rikkahub 的实现
        // 每次都创建全新的 List 以确保引用变化，触发 Compose 重组
        val updatedParts = delta.parts.fold(message.parts.toMutableList()) { acc, deltaPart ->
            when (deltaPart) {
                is MessagePart.Reasoning -> {
                    // 查找现有的 Reasoning part
                    val existingIndex = acc.indexOfFirst { it is MessagePart.Reasoning }
                    if (existingIndex >= 0) {
                        val existingReasoningPart = acc[existingIndex] as MessagePart.Reasoning
                        // 创建新的 Reasoning 对象并替换
                        acc[existingIndex] = MessagePart.Reasoning(
                            reasoning = existingReasoningPart.reasoning + deltaPart.reasoning,
                            createdAt = existingReasoningPart.createdAt,
                            finishedAt = deltaPart.finishedAt ?: existingReasoningPart.finishedAt
                        )
                        acc
                    } else {
                        // 添加新的 Reasoning part
                        acc.add(deltaPart)
                        acc
                    }
                }
                is MessagePart.Text -> {
                    // 1. 更新 Reasoning part（用于 DeepThinking 显示）
                    val reasoningIndex = acc.indexOfFirst { it is MessagePart.Reasoning }
                    if (reasoningIndex >= 0) {
                        val reasoningPart = acc[reasoningIndex] as MessagePart.Reasoning
                        acc[reasoningIndex] = MessagePart.Reasoning(
                            reasoning = reasoningPart.reasoning + deltaPart.text,
                            createdAt = reasoningPart.createdAt,
                            finishedAt = reasoningPart.finishedAt
                        )
                    }

                    // 2. 更新或添加 Text part（用于最终消息显示）
                    val textIndex = acc.indexOfFirst { it is MessagePart.Text }
                    if (textIndex >= 0) {
                        val existingTextPart = acc[textIndex] as MessagePart.Text
                        acc[textIndex] = MessagePart.Text(existingTextPart.text + deltaPart.text)
                    } else {
                        acc.add(deltaPart)
                    }
                    acc
                }
                else -> {
                    acc.add(deltaPart)
                    acc
                }
            }
        }.toList() // 转换为不可变 List

        // 创建新的 Message 对象，确保引用变化
        return message.copy(
            parts = updatedParts,
            createdAt = message.createdAt // 保持原始创建时间
        )
    }

    private fun launchBackgroundTasks(conversationId: String) {
        scope.launch {
            try {
                generateTitle(conversationId)
                generateSuggestions(conversationId)
            } catch (e: Exception) {
                logger.error(e) { "Error in background tasks for conversation: $conversationId" }
            }
        }
    }
}
