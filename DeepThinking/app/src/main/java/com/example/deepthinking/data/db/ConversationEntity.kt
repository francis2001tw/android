package com.example.deepthinking.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.deepthinking.data.model.Conversation
import com.example.deepthinking.data.model.UIMessage
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Room entity for conversation
 */
@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "conversations")
@TypeConverters(Converters::class)
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val messagesJson: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toConversation(): Conversation {
        val messages = Json.decodeFromString<List<UIMessage>>(messagesJson)
        return Conversation(
            id = Uuid.parse(id),
            title = title,
            messages = messages,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
    }

    companion object {
        fun fromConversation(conversation: Conversation): ConversationEntity {
            return ConversationEntity(
                id = conversation.id.toString(),
                title = conversation.title,
                messagesJson = Json.encodeToString(conversation.messages),
                createdAt = conversation.createdAt.toEpochMilliseconds(),
                updatedAt = conversation.updatedAt.toEpochMilliseconds()
            )
        }
    }
}

/**
 * Type converters for Room
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }
}

