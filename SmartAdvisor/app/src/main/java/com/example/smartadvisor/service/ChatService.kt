package com.example.smartadvisor.service

import com.example.smartadvisor.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 通用对话服务接口
 */
interface UniversalChatService {
    
    // ========== 对话管理 ==========
    
    /**
     * 创建新对话
     */
    suspend fun createConversation(
        assistantId: String,
        initialMessages: List<Message> = emptyList()
    ): Conversation
    
    /**
     * 加载对话
     */
    suspend fun loadConversation(conversationId: String): Conversation?
    
    /**
     * 获取对话流 (实时更新)
     */
    fun observeConversation(conversationId: String): Flow<Conversation>
    
    /**
     * 保存对话
     */
    suspend fun saveConversation(conversation: Conversation)
    
    /**
     * 删除对话
     */
    suspend fun deleteConversation(conversationId: String)
    
    /**
     * 搜索对话
     */
    fun searchConversations(
        query: String,
        assistantId: String? = null
    ): Flow<List<Conversation>>
    
    // ========== 消息操作 ==========
    
    /**
     * 发送消息 (核心方法)
     */
    suspend fun sendMessage(
        conversationId: String,
        content: List<MessagePart>,
        autoGenerate: Boolean = true
    ): Result<Message>
    
    /**
     * 生成 AI 响应 (流式)
     */
    fun generateResponseStream(
        conversationId: String,
        params: TextGenerationParams? = null
    ): Flow<GenerationChunk>
    
    /**
     * 生成 AI 响应 (非流式)
     */
    suspend fun generateResponse(
        conversationId: String,
        params: TextGenerationParams? = null
    ): Result<Message>
    
    /**
     * 取消生成
     */
    fun cancelGeneration(conversationId: String)
    
    /**
     * 重新生成消息
     */
    suspend fun regenerateMessage(
        conversationId: String,
        messageId: String
    ): Result<Message>
    
    /**
     * 编辑消息 (创建新分支)
     */
    suspend fun editMessage(
        conversationId: String,
        messageId: String,
        newContent: List<MessagePart>
    ): Result<Message>
    
    /**
     * 删除消息
     */
    suspend fun deleteMessage(
        conversationId: String,
        messageId: String
    )
    
    // ========== 高级功能 ==========
    
    /**
     * 自动生成标题
     */
    suspend fun generateTitle(conversationId: String): String
    
    /**
     * 生成对话建议
     */
    suspend fun generateSuggestions(conversationId: String): List<String>
    
    /**
     * 获取 Token 使用统计
     */
    fun getTokenUsage(conversationId: String): Flow<TokenUsage>
}

/**
 * 生成块 (流式输出)
 */
sealed class GenerationChunk {
    // 在服务端创建 AI 消息后立即发出，供 UI 绑定 overlay 到确切 messageId
    data class StreamTarget(val messageId: String) : GenerationChunk()

    data class ThinkingChunk(val content: String) : GenerationChunk()
    data class ThinkingComplete(val totalContent: String) : GenerationChunk()
    data class ResponseChunk(val content: String) : GenerationChunk()
    data class ResponseComplete(
        val message: Message,
        val usage: TokenUsage
    ) : GenerationChunk()
    data class Error(val error: Throwable) : GenerationChunk()
}

