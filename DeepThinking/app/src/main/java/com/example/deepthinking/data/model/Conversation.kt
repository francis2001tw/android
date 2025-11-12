package com.example.deepthinking.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Conversation with messages
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Conversation(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = Uuid.random(),
    val title: String = "New Conversation",
    val messages: List<UIMessage> = emptyList(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    fun updateMessages(newMessages: List<UIMessage>): Conversation {
        return copy(
            messages = newMessages,
            updatedAt = Clock.System.now()
        )
    }

    fun addMessage(message: UIMessage): Conversation {
        return copy(
            messages = messages + message,
            updatedAt = Clock.System.now()
        )
    }

    fun updateLastMessage(message: UIMessage): Conversation {
        if (messages.isEmpty()) return this
        return copy(
            messages = messages.dropLast(1) + message,
            updatedAt = Clock.System.now()
        )
    }
}

/**
 * Conversation log entry for JSON export
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ConversationLog(
    @Serializable(with = UuidSerializer::class)
    val conversationId: Uuid,
    val timestamp: Instant,
    val inputType: String, // "text", "image", "document"
    val outputModel: String,
    val tokenUsage: TokenUsage,
    val message: UIMessage
)

