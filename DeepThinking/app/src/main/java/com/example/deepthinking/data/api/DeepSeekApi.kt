package com.example.deepthinking.data.api

import com.example.deepthinking.data.model.MessageChunk
import com.example.deepthinking.data.model.MessageChoice
import com.example.deepthinking.data.model.MessageDelta
import com.example.deepthinking.data.model.MessageRole
import com.example.deepthinking.data.model.TokenUsage
import com.example.deepthinking.data.model.UIMessage
import com.example.deepthinking.data.model.UIMessagePart
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

/**
 * DeepSeek API client with streaming support
 */
class DeepSeekApi(
    private val apiKey: String,
    private val baseUrl: String = "https://api.deepseek.com"
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        classDiscriminator = "#class"  // Use different discriminator to avoid conflict with 'type' property
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Stream chat completions with reasoning support
     */
    fun streamChatCompletions(
        messages: List<UIMessage>,
        model: String = "deepseek-reasoner",
        temperature: Double = 1.0,
        maxTokens: Int = 8000
    ): Flow<MessageChunk> = callbackFlow {
        val requestBody = ChatCompletionRequest(
            model = model,
            messages = messages.map { it.toApiMessage() },
            temperature = temperature,
            maxTokens = maxTokens,
            stream = true
        )

        val requestJson = json.encodeToString(ChatCompletionRequest.serializer(), requestBody)

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    close()
                    return
                }

                try {
                    val response = json.decodeFromString<ChatCompletionChunk>(data)
                    val chunk = response.toMessageChunk()
                    trySend(chunk)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?
            ) {
                close(t)
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val eventSource = EventSources.createFactory(client).newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }

    /**
     * Convert UIMessage to API message format
     */
    private fun UIMessage.toApiMessage(): ApiMessage {
        val content = mutableListOf<ApiContent>()

        parts.forEach { part ->
            when (part) {
                is UIMessagePart.Text -> {
                    content.add(ApiContent.TextContent(text = part.text))
                }
                is UIMessagePart.Image -> {
                    content.add(ApiContent.ImageContent(
                        imageUrl = ApiImageUrl(url = part.url)
                    ))
                }
                is UIMessagePart.Document -> {
                    // For documents, we'll include them as text for now
                    content.add(ApiContent.TextContent(text = "[Document: ${part.fileName}]"))
                }
                is UIMessagePart.Reasoning -> {
                    // Reasoning is not sent to API, it's received from API
                }
            }
        }

        return ApiMessage(
            role = role.name.lowercase(),
            content = content
        )
    }
}

/**
 * API request models
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ApiMessage>,
    val temperature: Double = 1.0,
    @SerialName("max_tokens")
    val maxTokens: Int = 8000,
    val stream: Boolean = true
)

@Serializable
data class ApiMessage(
    val role: String,
    val content: List<ApiContent>
)

@Serializable
sealed class ApiContent {
    @Serializable
    @SerialName("text")
    data class TextContent(
        val type: String = "text",
        val text: String
    ) : ApiContent()

    @Serializable
    @SerialName("image_url")
    data class ImageContent(
        val type: String = "image_url",
        @SerialName("image_url")
        val imageUrl: ApiImageUrl
    ) : ApiContent()
}

@Serializable
data class ApiImageUrl(
    val url: String
)

/**
 * API response models
 */
@Serializable
data class ChatCompletionChunk(
    val id: String,
    val model: String,
    val choices: List<ApiChoice>,
    val usage: ApiUsage? = null
) {
    fun toMessageChunk(): MessageChunk {
        return MessageChunk(
            id = id,
            model = model,
            choices = choices.map { it.toMessageChoice() },
            usage = usage?.toTokenUsage()
        )
    }
}

@Serializable
data class ApiChoice(
    val index: Int,
    val delta: ApiDelta,
    @SerialName("finish_reason")
    val finishReason: String? = null
) {
    fun toMessageChoice(): MessageChoice {
        return MessageChoice(
            index = index,
            delta = delta.toMessageDelta(),
            finishReason = finishReason
        )
    }
}

@Serializable
data class ApiDelta(
    val role: String? = null,
    val content: String? = null,
    @SerialName("reasoning_content")
    val reasoningContent: String? = null
) {
    fun toMessageDelta(): MessageDelta {
        return MessageDelta(
            role = role?.let { MessageRole.valueOf(it.uppercase()) },
            content = content,
            reasoningContent = reasoningContent
        )
    }
}

@Serializable
data class ApiUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
    @SerialName("prompt_cache_hit_tokens")
    val promptCacheHitTokens: Int? = null,
    @SerialName("prompt_cache_miss_tokens")
    val promptCacheMissTokens: Int? = null,
    @SerialName("completion_tokens_details")
    val completionTokensDetails: CompletionTokensDetails? = null
) {
    fun toTokenUsage(): TokenUsage {
        return TokenUsage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
            reasoningTokens = completionTokensDetails?.reasoningTokens ?: 0
        )
    }
}

@Serializable
data class CompletionTokensDetails(
    @SerialName("reasoning_tokens")
    val reasoningTokens: Int = 0
)

