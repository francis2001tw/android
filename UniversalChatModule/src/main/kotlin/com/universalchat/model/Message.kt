package com.universalchat.model

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * 消息角色
 */
@Serializable
enum class MessageRole {
    USER,        // 用户
    ASSISTANT,   // AI 助手
    SYSTEM,      // 系统提示
    TOOL         // 工具调用结果
}

/**
 * 消息实体
 */
@Serializable
data class Message(
    val id: String = uuid4().toString(),
    val role: MessageRole,
    val parts: List<MessagePart>,
    val annotations: List<MessageAnnotation> = emptyList(),
    val createdAt: Instant = Clock.System.now(),
    val modelId: String? = null,
    val usage: TokenUsage? = null,
    val translation: String? = null
) {
    /**
     * 转换为纯文本
     */
    fun toText(): String = parts.joinToString(separator = "\n") { part ->
        when (part) {
            is MessagePart.Text -> part.text
            is MessagePart.Image -> "[图片: ${part.url}]"
            is MessagePart.Document -> "[文档: ${part.fileName}]"
            is MessagePart.Reasoning -> "[思考: ${part.reasoning}]"
            is MessagePart.ToolCall -> "[工具调用: ${part.toolName}]"
            is MessagePart.ToolResult -> "[工具结果: ${part.toolName}]"
        }
    }
    
    /**
     * 是否包含推理内容
     */
    fun hasReasoning(): Boolean = parts.any { it is MessagePart.Reasoning }
    
    /**
     * 获取推理内容
     */
    fun getReasoningContent(): String = parts
        .filterIsInstance<MessagePart.Reasoning>()
        .joinToString("") { it.reasoning }
    
    /**
     * 是否有效可上传 (过滤掉不需要上传的部分)
     */
    fun isValidForUpload(): Boolean = parts.any { it !is MessagePart.Reasoning }
    
    /**
     * 获取可上传的消息 (移除 Reasoning 部分)
     */
    fun forUpload(): Message = copy(
        parts = parts.filter { it !is MessagePart.Reasoning }
    )
    
    companion object {
        fun user(text: String) = Message(
            role = MessageRole.USER,
            parts = listOf(MessagePart.Text(text))
        )
        
        fun assistant(text: String) = Message(
            role = MessageRole.ASSISTANT,
            parts = listOf(MessagePart.Text(text))
        )
        
        fun system(text: String) = Message(
            role = MessageRole.SYSTEM,
            parts = listOf(MessagePart.Text(text))
        )
    }
}

/**
 * 消息部分 (多模态)
 */
@Serializable
sealed class MessagePart {
    abstract val metadata: JsonObject?
    
    @Serializable
    data class Text(
        val text: String,
        override val metadata: JsonObject? = null
    ) : MessagePart()
    
    @Serializable
    data class Image(
        val url: String,
        override val metadata: JsonObject? = null
    ) : MessagePart()
    
    @Serializable
    data class Document(
        val url: String,
        val fileName: String,
        val mimeType: String,
        override val metadata: JsonObject? = null
    ) : MessagePart()
    
    /**
     * 推理内容 (Deep Thinking)
     */
    @Serializable
    data class Reasoning(
        val reasoning: String,
        val createdAt: Instant,
        val finishedAt: Instant? = null,
        override val metadata: JsonObject? = null
    ) : MessagePart()
    
    @Serializable
    data class ToolCall(
        val toolCallId: String,
        val toolName: String,
        val arguments: String,
        override val metadata: JsonObject? = null
    ) : MessagePart()
    
    @Serializable
    data class ToolResult(
        val toolCallId: String,
        val toolName: String,
        val content: JsonElement,
        override val metadata: JsonObject? = null
    ) : MessagePart()
}

/**
 * 消息注解
 */
@Serializable
sealed class MessageAnnotation {
    @Serializable
    data class UrlCitation(
        val title: String,
        val url: String
    ) : MessageAnnotation()
}

/**
 * Token 使用统计
 */
@Serializable
data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val cachedTokens: Int = 0,
    val totalTokens: Int = 0
) {
    operator fun plus(other: TokenUsage): TokenUsage {
        return TokenUsage(
            promptTokens = promptTokens + other.promptTokens,
            completionTokens = completionTokens + other.completionTokens,
            cachedTokens = cachedTokens + other.cachedTokens,
            totalTokens = totalTokens + other.totalTokens
        )
    }
}

/**
 * Token 使用合并
 */
fun TokenUsage?.merge(other: TokenUsage): TokenUsage {
    val promptTokens = if (other.promptTokens > 0) {
        other.promptTokens
    } else {
        this?.promptTokens ?: 0
    }
    val completionTokens = if (other.completionTokens > 0) {
        other.completionTokens
    } else {
        this?.completionTokens ?: 0
    }
    val cachedTokens = if (other.cachedTokens > 0) {
        other.cachedTokens
    } else {
        this?.cachedTokens ?: 0
    }
    val totalTokens = promptTokens + completionTokens
    
    return TokenUsage(
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        cachedTokens = cachedTokens,
        totalTokens = totalTokens
    )
}

/**
 * 消息块 (流式输出单元)
 */
@Serializable
data class MessageChunk(
    val id: String,
    val model: String,
    val choices: List<MessageChoice>,
    val usage: TokenUsage? = null
)

/**
 * 消息选择
 */
@Serializable
data class MessageChoice(
    val index: Int,
    val delta: Message? = null,
    val message: Message? = null,
    val finishReason: String? = null
)

