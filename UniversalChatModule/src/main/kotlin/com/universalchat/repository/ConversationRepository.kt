package com.universalchat.repository

import com.universalchat.model.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * 对话仓库接口
 */
interface ConversationRepository {
    suspend fun save(conversation: Conversation)
    suspend fun load(conversationId: String): Conversation?
    suspend fun delete(conversationId: String)
    fun search(query: String, assistantId: String? = null): Flow<List<Conversation>>
    fun getAll(): Flow<List<Conversation>>
}

/**
 * 内存实现 (用于演示)
 */
class InMemoryConversationRepository : ConversationRepository {
    private val conversations = MutableStateFlow<Map<String, Conversation>>(emptyMap())
    
    override suspend fun save(conversation: Conversation) {
        conversations.value = conversations.value + (conversation.id to conversation)
    }
    
    override suspend fun load(conversationId: String): Conversation? {
        return conversations.value[conversationId]
    }
    
    override suspend fun delete(conversationId: String) {
        conversations.value = conversations.value - conversationId
    }
    
    override fun search(query: String, assistantId: String?): Flow<List<Conversation>> {
        return conversations.map { map ->
            map.values.filter { conversation ->
                val matchesQuery = conversation.title.contains(query, ignoreCase = true) ||
                    conversation.getCurrentMessages().any { it.toText().contains(query, ignoreCase = true) }
                val matchesAssistant = assistantId == null || conversation.assistantId == assistantId
                matchesQuery && matchesAssistant
            }
        }
    }
    
    override fun getAll(): Flow<List<Conversation>> {
        return conversations.map { it.values.toList() }
    }
}

