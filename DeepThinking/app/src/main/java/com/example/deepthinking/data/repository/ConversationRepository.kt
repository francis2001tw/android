package com.example.deepthinking.data.repository

import com.example.deepthinking.data.api.DeepSeekApi
import com.example.deepthinking.data.db.ConversationDao
import com.example.deepthinking.data.db.ConversationEntity
import com.example.deepthinking.data.model.Conversation
import com.example.deepthinking.data.model.ConversationLog
import com.example.deepthinking.data.model.MessageChunk
import com.example.deepthinking.data.model.UIMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Repository for conversation operations
 */
@OptIn(ExperimentalUuidApi::class)
class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val deepSeekApi: DeepSeekApi
) {
    private val json = Json { prettyPrint = true }
    private val conversationLogs = mutableListOf<ConversationLog>()

    /**
     * Get all conversations
     */
    fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations()
            .map { entities -> entities.map { it.toConversation() } }
    }

    /**
     * Get conversation by ID
     */
    suspend fun getConversationById(id: Uuid): Conversation? {
        return conversationDao.getConversationById(id.toString())?.toConversation()
    }

    /**
     * Save conversation
     */
    suspend fun saveConversation(conversation: Conversation) {
        conversationDao.insertConversation(ConversationEntity.fromConversation(conversation))
    }

    /**
     * Update conversation
     */
    suspend fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(ConversationEntity.fromConversation(conversation))
    }

    /**
     * Delete conversation
     */
    suspend fun deleteConversation(id: Uuid) {
        conversationDao.deleteConversation(id.toString())
    }

    /**
     * Stream chat completions
     */
    fun streamChatCompletions(
        messages: List<UIMessage>,
        model: String = "deepseek-reasoner"
    ): Flow<MessageChunk> {
        return deepSeekApi.streamChatCompletions(messages, model)
    }

    /**
     * Add conversation log
     */
    fun addConversationLog(log: ConversationLog) {
        conversationLogs.add(log)
    }

    /**
     * Get all conversation logs as JSON
     */
    fun getConversationLogsJson(): String {
        return json.encodeToString(conversationLogs)
    }

    /**
     * Clear conversation logs
     */
    fun clearConversationLogs() {
        conversationLogs.clear()
    }

    /**
     * Determine input type from message
     */
    fun getInputType(message: UIMessage): String {
        return when {
            message.parts.any { it is com.example.deepthinking.data.model.UIMessagePart.Image } -> "image"
            message.parts.any { it is com.example.deepthinking.data.model.UIMessagePart.Document } -> "document"
            else -> "text"
        }
    }
}

