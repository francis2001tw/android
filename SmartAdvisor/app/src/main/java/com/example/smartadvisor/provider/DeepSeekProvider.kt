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
            var totalReasoningChars = 0
            var totalContentChars = 0

            Log.d("DeepSeekProvider", "🌊 Starting to read streaming response...")

            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break

                if (line.isEmpty()) continue

                // 只记录非空行的前100个字符
                if (line.length > 100) {
                    Log.d("DeepSeekProvider", "📥 Received line (${line.length} chars): ${line.take(100)}...")
                } else {
                    Log.d("DeepSeekProvider", "📥 Received line: $line")
                }

                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()

                    if (data == "[DONE]") {
                        Log.d("DeepSeekProvider", "✅ Stream completed - Total chunks: $chunkIndex, Reasoning: $totalReasoningChars chars, Content: $totalContentChars chars")
                        break
                    }

                    try {
                        val chunk = parseChunk(data, chunkIndex)
                        chunkIndex++

                        // 统计内容
                        chunk.choices.firstOrNull()?.delta?.parts?.forEach { part ->
                            when (part) {
                                is MessagePart.Reasoning -> {
                                    totalReasoningChars += part.reasoning.length
                                    Log.d("DeepSeekProvider", "  🧠 Reasoning chunk: +${part.reasoning.length} chars (total: $totalReasoningChars)")
                                }
                                is MessagePart.Text -> {
                                    totalContentChars += part.text.length
                                    Log.d("DeepSeekProvider", "  💬 Text chunk: +${part.text.length} chars (total: $totalContentChars)")
                                }
                                else -> Log.d("DeepSeekProvider", "  ❓ Other: ${part::class.simpleName}")
                            }
                        }

                        // 发射 chunk
                        emit(chunk)
                        Log.d("DeepSeekProvider", "  ✅ Chunk #$chunkIndex emitted")
                    } catch (e: Exception) {
                        Log.e("DeepSeekProvider", "❌ Error parsing chunk #$chunkIndex: ${e.message}", e)
                        Log.e("DeepSeekProvider", "   Raw data: $data")
                    }
                }
            }

            Log.d("DeepSeekProvider", "🏁 Stream reading finished - Total chunks: $chunkIndex")

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
        val modelId = params.model.modelId
        Log.d("DeepSeekProvider", "🎯 Building request for model: $modelId")

        return buildJsonObject {
            put("model", modelId)
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

            // Log the request details
            Log.d("DeepSeekProvider", "   Model: $modelId")
            Log.d("DeepSeekProvider", "   Stream: $stream")
            Log.d("DeepSeekProvider", "   Messages: ${messages.size}")
            Log.d("DeepSeekProvider", "   Max tokens: ${params.maxTokens}")
        }
    }
    
    private fun parseChunk(jsonStr: String, index: Int): MessageChunk {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        val choices = json["choices"]?.jsonArray ?: JsonArray(emptyList())

        val messageChoices = choices.map { choice ->
            val choiceObj = choice.jsonObject
            val delta = choiceObj["delta"]?.jsonObject

            val parts = mutableListOf<MessagePart>()

            // DeepSeek 的 reasoning_content 字段（优先处理，因为它先于 content 返回）
            // 根据 DeepSeek API 文档：chunk.choices[0].delta.reasoning_content
            val reasoningContent = delta?.get("reasoning_content")?.jsonPrimitive?.contentOrNull
            if (reasoningContent != null && reasoningContent.isNotBlank()) {
                parts.add(MessagePart.Reasoning(
                    reasoning = reasoningContent,
                    createdAt = Clock.System.now(),
                    finishedAt = null
                ))
                Log.d("DeepSeekProvider", "  ✅ Parsed reasoning_content: ${reasoningContent.take(50)}...")
            }

            // 解析文本内容（最终答案）
            val textContent = delta?.get("content")?.jsonPrimitive?.contentOrNull
            if (textContent != null && textContent.isNotBlank()) {
                parts.add(MessagePart.Text(textContent))
                Log.d("DeepSeekProvider", "  ✅ Parsed content: ${textContent.take(50)}...")
            }

            // 检查是否完成
            val finishReason = choiceObj["finish_reason"]?.jsonPrimitive?.contentOrNull
            if (finishReason != null) {
                Log.d("DeepSeekProvider", "  🏁 Finish reason: $finishReason")
                // 如果有 reasoning part，标记为完成
                val updatedParts = parts.map { part ->
                    if (part is MessagePart.Reasoning) {
                        part.copy(finishedAt = Clock.System.now())
                    } else part
                }
                parts.clear()
                parts.addAll(updatedParts)
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
                finishReason = finishReason
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

