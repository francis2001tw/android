package com.example.deepthinking.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * UUID Serializer for kotlinx.serialization
 */
@OptIn(ExperimentalUuidApi::class)
object UuidSerializer : KSerializer<Uuid> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uuid", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Uuid) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Uuid = Uuid.parse(decoder.decodeString())
}

/**
 * Message role in conversation
 */
@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}

/**
 * UI Message with multiple parts
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class UIMessage(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = Uuid.random(),
    val role: MessageRole,
    val parts: List<UIMessagePart>,
    val createdAt: Instant = Clock.System.now(),
    val modelId: String? = null,
    val usage: TokenUsage? = null
) {
    fun toText(): String {
        return parts.filterIsInstance<UIMessagePart.Text>()
            .joinToString("\n") { it.text }
    }

    fun hasReasoning(): Boolean {
        return parts.any { it is UIMessagePart.Reasoning }
    }

    fun getReasoning(): UIMessagePart.Reasoning? {
        return parts.filterIsInstance<UIMessagePart.Reasoning>().firstOrNull()
    }

    companion object {
        fun user(text: String) = UIMessage(
            role = MessageRole.USER,
            parts = listOf(UIMessagePart.Text(text))
        )

        fun assistant(text: String) = UIMessage(
            role = MessageRole.ASSISTANT,
            parts = listOf(UIMessagePart.Text(text))
        )
    }
}

/**
 * Message parts (text, image, reasoning, etc.)
 */
@Serializable
sealed class UIMessagePart {
    @Serializable
    data class Text(val text: String) : UIMessagePart()

    @Serializable
    data class Image(val url: String) : UIMessagePart()

    @Serializable
    data class Document(
        val url: String,
        val fileName: String,
        val mime: String = "text/*"
    ) : UIMessagePart()

    @Serializable
    data class Reasoning(
        val reasoning: String,
        val createdAt: Instant = Clock.System.now(),
        val finishedAt: Instant? = null
    ) : UIMessagePart()
}

/**
 * Token usage information
 */
@Serializable
data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
    val reasoningTokens: Int = 0
)

/**
 * Message chunk for streaming
 */
@Serializable
data class MessageChunk(
    val id: String,
    val model: String,
    val choices: List<MessageChoice>,
    val usage: TokenUsage? = null
)

/**
 * Message choice in chunk
 */
@Serializable
data class MessageChoice(
    val index: Int,
    val delta: MessageDelta?,
    val finishReason: String?
)

/**
 * Message delta for streaming updates
 */
@Serializable
data class MessageDelta(
    val role: MessageRole? = null,
    val content: String? = null,
    val reasoningContent: String? = null
)

/**
 * Extension function to append chunk to message
 */
@OptIn(ExperimentalUuidApi::class)
fun UIMessage.appendChunk(chunk: MessageChunk): UIMessage {
    val choice = chunk.choices.firstOrNull() ?: return this
    val delta = choice.delta ?: return this

    val newParts = parts.toMutableList()

    // Handle reasoning content
    if (delta.reasoningContent != null) {
        val existingReasoning = newParts.filterIsInstance<UIMessagePart.Reasoning>().firstOrNull()
        if (existingReasoning != null) {
            val index = newParts.indexOf(existingReasoning)
            newParts[index] = UIMessagePart.Reasoning(
                reasoning = existingReasoning.reasoning + delta.reasoningContent,
                createdAt = existingReasoning.createdAt,
                finishedAt = null
            )
        } else {
            newParts.add(0, UIMessagePart.Reasoning(reasoning = delta.reasoningContent))
        }
    }

    // Handle text content
    if (delta.content != null) {
        // Finish reasoning if exists
        val reasoningIndex = newParts.indexOfFirst { it is UIMessagePart.Reasoning }
        if (reasoningIndex >= 0) {
            val reasoning = newParts[reasoningIndex] as UIMessagePart.Reasoning
            if (reasoning.finishedAt == null) {
                newParts[reasoningIndex] = reasoning.copy(finishedAt = Clock.System.now())
            }
        }

        val existingText = newParts.filterIsInstance<UIMessagePart.Text>().firstOrNull()
        if (existingText != null) {
            val index = newParts.indexOf(existingText)
            newParts[index] = UIMessagePart.Text(existingText.text + delta.content)
        } else {
            newParts.add(UIMessagePart.Text(delta.content))
        }
    }

    // Check if finished
    if (choice.finishReason != null) {
        val reasoningIndex = newParts.indexOfFirst { it is UIMessagePart.Reasoning }
        if (reasoningIndex >= 0) {
            val reasoning = newParts[reasoningIndex] as UIMessagePart.Reasoning
            if (reasoning.finishedAt == null) {
                newParts[reasoningIndex] = reasoning.copy(finishedAt = Clock.System.now())
            }
        }
    }

    return copy(
        parts = newParts,
        usage = chunk.usage ?: usage
    )
}

/**
 * Extension function to handle message chunks in list
 */
@OptIn(ExperimentalUuidApi::class)
fun List<UIMessage>.handleMessageChunk(chunk: MessageChunk): List<UIMessage> {
    if (isEmpty()) {
        val choice = chunk.choices.firstOrNull() ?: return this
        val delta = choice.delta ?: return this
        val role = delta.role ?: MessageRole.ASSISTANT
        return listOf(UIMessage(role = role, parts = emptyList()).appendChunk(chunk))
    }

    val last = last()
    val updated = last.appendChunk(chunk)
    return dropLast(1) + updated
}

