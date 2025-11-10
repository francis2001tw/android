package com.example.smartadvisor.provider

import com.example.smartadvisor.model.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * Qwen 提供商实现
 */
class QwenProvider(
    private val setting: ProviderSetting
) : Provider {
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    override fun streamText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Flow<MessageChunk> = flow {
        val requestBody = buildRequestBody(messages, params, stream = true)
        
        val response: HttpResponse = client.post(setting.baseUrl) {
            header("Authorization", "Bearer ${setting.apiKey}")
            header("Content-Type", "application/json")
            setBody(requestBody)
        }
        
        // 模拟流式响应
        val lines = response.bodyAsText().lines()
        var chunkIndex = 0
        
        for (line in lines) {
            if (line.startsWith("data: ") && line != "data: [DONE]") {
                val jsonStr = line.removePrefix("data: ")
                try {
                    val chunk = parseChunk(jsonStr, chunkIndex++)
                    emit(chunk)
                } catch (e: Exception) {
                    // 忽略解析错误
                }
            }
        }
    }
    
    override suspend fun generateText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Message {
        val requestBody = buildRequestBody(messages, params, stream = false)
        
        val response: HttpResponse = client.post(setting.baseUrl) {
            header("Authorization", "Bearer ${setting.apiKey}")
            header("Content-Type", "application/json")
            setBody(requestBody)
        }
        
        val responseText = response.bodyAsText()
        val json = Json.parseToJsonElement(responseText).jsonObject
        
        return parseMessage(json)
    }
    
    private fun buildRequestBody(
        messages: List<Message>,
        params: TextGenerationParams,
        stream: Boolean
    ): JsonObject {
        return buildJsonObject {
            put("model", params.model.modelId)
            put("messages", buildJsonArray {
                messages.forEach { message ->
                    add(buildJsonObject {
                        put("role", message.role.name.lowercase())
                        put("content", buildJsonArray {
                            message.parts.forEach { part ->
                                when (part) {
                                    is MessagePart.Text -> add(buildJsonObject {
                                        put("type", "text")
                                        put("text", part.text)
                                    })
                                    is MessagePart.Image -> add(buildJsonObject {
                                        put("type", "image_url")
                                        put("image_url", buildJsonObject {
                                            put("url", part.url)
                                        })
                                    })
                                    else -> {}
                                }
                            }
                        })
                    })
                }
            })
            put("stream", stream)
            params.temperature?.let { put("temperature", it) }
            params.topP?.let { put("top_p", it) }
            params.maxTokens?.let { put("max_tokens", it) }
            
            // Deep Thinking 支持
            if (params.thinkingBudget != null && params.thinkingBudget > 0) {
                put("enable_search", true)
                put("thinking_budget", params.thinkingBudget)
            }
        }
    }
    
    private fun parseChunk(jsonStr: String, index: Int): MessageChunk {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        val choices = json["choices"]?.jsonArray ?: JsonArray(emptyList())
        
        val messageChoices = choices.map { choice ->
            val choiceObj = choice.jsonObject
            val delta = choiceObj["delta"]?.jsonObject
            
            val parts = mutableListOf<MessagePart>()
            
            // 解析文本内容
            delta?.get("content")?.jsonPrimitive?.contentOrNull?.let { content ->
                if (content.isNotBlank()) {
                    parts.add(MessagePart.Text(content))
                }
            }
            
            // 解析思考内容
            delta?.get("reasoning_content")?.jsonPrimitive?.contentOrNull?.let { reasoning ->
                if (reasoning.isNotBlank()) {
                    parts.add(MessagePart.Reasoning(
                        reasoning = reasoning,
                        createdAt = Clock.System.now(),
                        finishedAt = null
                    ))
                }
            }
            
            val message = if (parts.isNotEmpty()) {
                Message(
                    role = MessageRole.ASSISTANT,
                    parts = parts
                )
            } else null
            
            MessageChoice(
                index = choiceObj["index"]?.jsonPrimitive?.int ?: 0,
                delta = message,
                finishReason = choiceObj["finish_reason"]?.jsonPrimitive?.contentOrNull
            )
        }
        
        val usage = json["usage"]?.jsonObject?.let { usageObj ->
            TokenUsage(
                promptTokens = usageObj["prompt_tokens"]?.jsonPrimitive?.int ?: 0,
                completionTokens = usageObj["completion_tokens"]?.jsonPrimitive?.int ?: 0,
                totalTokens = usageObj["total_tokens"]?.jsonPrimitive?.int ?: 0
            )
        }
        
        return MessageChunk(
            id = json["id"]?.jsonPrimitive?.content ?: "chunk-$index",
            model = json["model"]?.jsonPrimitive?.content ?: "",
            choices = messageChoices,
            usage = usage
        )
    }
    
    private fun parseMessage(json: JsonObject): Message {
        val choices = json["choices"]?.jsonArray ?: JsonArray(emptyList())
        val firstChoice = choices.firstOrNull()?.jsonObject
        val message = firstChoice?.get("message")?.jsonObject
        
        val content = message?.get("content")?.jsonPrimitive?.content ?: ""
        
        return Message(
            role = MessageRole.ASSISTANT,
            parts = listOf(MessagePart.Text(content))
        )
    }
}

