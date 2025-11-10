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

        // 立即发送 ThinkingChunk 事件，触发 DeepThinking 动画显示
        emit(GenerationChunk.ThinkingChunk(""))

        // 7. 流式生成
        var currentThinking = ""
        var currentResponse = ""

        provider.streamText(
            messages = messages,
            params = generationParams
        ).collect { chunk ->
            // 发送不同类型的块
            chunk.choices.firstOrNull()?.delta?.parts?.forEach { part ->
                when (part) {
                    is MessagePart.Reasoning -> {
                        currentThinking += part.reasoning
                        logger.debug { "Reasoning chunk: ${part.reasoning}" }
                        android.util.Log.d("ChatServiceImpl", "Reasoning chunk: ${part.reasoning}")
                        emit(GenerationChunk.ThinkingChunk(part.reasoning))

                        if (part.finishedAt != null) {
                            emit(GenerationChunk.ThinkingComplete(currentThinking))
                        }
                    }
                    is MessagePart.Text -> {
                        currentResponse += part.text
                        // 所有文本响应都显示在 DeepThinking 中
                        currentThinking += part.text
                        logger.debug { "Text chunk: ${part.text}, total thinking: ${currentThinking.length} chars" }
                        android.util.Log.d("ChatServiceImpl", "Text chunk: ${part.text}, total thinking: ${currentThinking.length} chars")
                        emit(GenerationChunk.ThinkingChunk(part.text))
                    }
                    else -> {}
                }
            }

            // 处理消息块 - 更新 aiMessage（在接收过程中）
            aiMessage = appendChunk(aiMessage, chunk)

            // 打印当前 Reasoning part 的内容
            val reasoningPart = aiMessage.parts.filterIsInstance<MessagePart.Reasoning>().firstOrNull()
            logger.debug { "Current Reasoning part length: ${reasoningPart?.reasoning?.length ?: 0}" }
            android.util.Log.d("ChatServiceImpl", "Current Reasoning part length: ${reasoningPart?.reasoning?.length ?: 0}")

            // 更新对话 - 这会触发 UI 刷新
            updateConversationMessage(conversationId, aiMessage)
            android.util.Log.d("ChatServiceImpl", "Updated conversation message, triggering UI refresh")

            // 处理完成
            if (chunk.choices.firstOrNull()?.finishReason != null) {
                // 发送 ThinkingComplete
                if (currentThinking.isNotEmpty()) {
                    emit(GenerationChunk.ThinkingComplete(currentThinking))
                }

                // 完成后：删除 Reasoning part，只保留 Text part
                val finalParts = mutableListOf<MessagePart>()

                // 只保留 Text parts
                aiMessage.parts.filterIsInstance<MessagePart.Text>().forEach { textPart ->
                    finalParts.add(textPart)
                }

                // 如果没有 Text part，创建一个包含所有内容的 Text part
                if (finalParts.isEmpty() && currentResponse.isNotEmpty()) {
                    finalParts.add(MessagePart.Text(currentResponse))
                }

                val finalUsage = chunk.usage ?: TokenUsage()
                aiMessage = aiMessage.copy(
                    parts = finalParts,  // 只保留 Text parts
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
            // 强制触发 StateFlow 更新 - 即使对象看起来相同
            flow.value = conversation
            android.util.Log.d("ChatServiceImpl", "Updated StateFlow for conversation $conversationId, nodes: ${conversation.messageNodes.size}")
        } else {
            android.util.Log.e("ChatServiceImpl", "No active flow for conversation $conversationId")
        }
    }
    
    private fun updateConversationMessage(conversationId: String, message: Message) {
        val conversation = getConversation(conversationId)
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
        val updatedParts = delta.parts.fold(message.parts) { acc, deltaPart ->
            when (deltaPart) {
                is MessagePart.Reasoning -> {
                    // 查找现有的 Reasoning part
                    val existingReasoningPart = acc.find { it is MessagePart.Reasoning } as? MessagePart.Reasoning
                    if (existingReasoningPart != null) {
                        // 使用 map 创建新列表，每次都创建新的 Reasoning 对象
                        acc.map { part ->
                            if (part is MessagePart.Reasoning) {
                                MessagePart.Reasoning(
                                    reasoning = existingReasoningPart.reasoning + deltaPart.reasoning,
                                    createdAt = existingReasoningPart.createdAt,
                                    finishedAt = deltaPart.finishedAt ?: existingReasoningPart.finishedAt
                                )
                            } else part
                        }
                    } else {
                        // 添加新的 Reasoning part
                        acc + deltaPart
                    }
                }
                is MessagePart.Text -> {
                    // 1. 更新 Reasoning part（用于 DeepThinking 显示）
                    val hasReasoning = acc.any { it is MessagePart.Reasoning }
                    val afterReasoningUpdate = if (hasReasoning) {
                        acc.map { part ->
                            if (part is MessagePart.Reasoning) {
                                MessagePart.Reasoning(
                                    reasoning = part.reasoning + deltaPart.text,
                                    createdAt = part.createdAt,
                                    finishedAt = part.finishedAt
                                )
                            } else part
                        }
                    } else acc

                    // 2. 更新或添加 Text part（用于最终消息显示）
                    val existingTextPart = afterReasoningUpdate.find { it is MessagePart.Text } as? MessagePart.Text
                    if (existingTextPart != null) {
                        afterReasoningUpdate.map { part ->
                            if (part is MessagePart.Text) {
                                MessagePart.Text(existingTextPart.text + deltaPart.text)
                            } else part
                        }
                    } else {
                        afterReasoningUpdate + deltaPart
                    }
                }
                else -> acc + deltaPart
            }
        }

        return message.copy(parts = updatedParts)
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
