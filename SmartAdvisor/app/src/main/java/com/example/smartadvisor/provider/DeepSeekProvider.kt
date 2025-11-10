package com.example.smartadvisor.provider

import android.util.Log
import com.example.smartadvisor.model.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*

/**
 * DeepSeek 提供商实现
 * 使用 OpenAI 兼容的 API
 */
class DeepSeekProvider(
    private val setting: ProviderSetting
) : Provider {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        // 配置超时时间
        engine {
            requestTimeout = 60_000  // 60 秒请求超时
            endpoint {
                connectTimeout = 30_000  // 30 秒连接超时
                socketTimeout = 60_000   // 60 秒 socket 超时
            }
        }
    }
    
    override fun streamText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Flow<MessageChunk> = flow {
        try {
            val requestBody = buildRequestBody(messages, params, stream = true)

            // DeepSeek API 端点: https://api.deepseek.com/chat/completions
            val apiUrl = if (setting.baseUrl.contains("/chat/completions")) {
                setting.baseUrl
            } else {
                "${setting.baseUrl.trimEnd('/')}/chat/completions"
            }

            Log.d("DeepSeekProvider", "Sending request to: $apiUrl")
            Log.d("DeepSeekProvider", "Request body: $requestBody")

            val response: HttpResponse = client.post(apiUrl) {
                header("Authorization", "Bearer ${setting.apiKey}")
                header("Content-Type", "application/json")
                setBody(requestBody)
            }

            Log.d("DeepSeekProvider", "Response status: ${response.status}")

            // 检查响应状态
            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Log.e("DeepSeekProvider", "Error response: $errorBody")
                throw Exception("API Error: ${response.status} - $errorBody")
            }

            // 处理流式响应
            val channel: ByteReadChannel = response.bodyAsChannel()
            var chunkIndex = 0
            val buffer = StringBuilder()

            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break

                if (line.isEmpty()) continue

                Log.d("DeepSeekProvider", "Received line: $line")

                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()

                    if (data == "[DONE]") {
                        Log.d("DeepSeekProvider", "Stream completed")
                        break
                    }

                    try {
                        val chunk = parseChunk(data, chunkIndex++)
                        Log.d("DeepSeekProvider", "Emitting chunk #$chunkIndex with ${chunk.choices.size} choices")
                        chunk.choices.firstOrNull()?.delta?.parts?.forEach { part ->
                            when (part) {
                                is MessagePart.Reasoning -> Log.d("DeepSeekProvider", "  - Reasoning: ${part.reasoning.take(50)}")
                                is MessagePart.Text -> Log.d("DeepSeekProvider", "  - Text: ${part.text.take(50)}")
                                else -> Log.d("DeepSeekProvider", "  - Other: ${part::class.simpleName}")
                            }
                        }
                        emit(chunk)
                    } catch (e: Exception) {
                        Log.e("DeepSeekProvider", "Error parsing chunk: ${e.message}", e)
                    }
                }
            }

            Log.d("DeepSeekProvider", "Total chunks received: $chunkIndex")

        } catch (e: Exception) {
            Log.e("DeepSeekProvider", "Stream error: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun generateText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Message {
        val requestBody = buildRequestBody(messages, params, stream = false)

        val apiUrl = if (setting.baseUrl.contains("/chat/completions")) {
            setting.baseUrl
        } else {
            "${setting.baseUrl.trimEnd('/')}/chat/completions"
        }

        val response: HttpResponse = client.post(apiUrl) {
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

                        // DeepSeek 使用简单的字符串格式
                        val textContent = message.parts
                            .filterIsInstance<MessagePart.Text>()
                            .joinToString(" ") { it.text }

                        if (textContent.isNotEmpty()) {
                            put("content", textContent)
                        }
                    })
                }
            })
            put("stream", stream)
            params.temperature?.let { put("temperature", it) }
            params.topP?.let { put("top_p", it) }
            params.maxTokens?.let { put("max_tokens", it) }
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
            
            // DeepSeek 的 reasoning_content 字段（如果支持）
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

