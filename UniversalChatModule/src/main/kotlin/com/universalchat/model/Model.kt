package com.universalchat.model

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

/**
 * 模型配置
 */
@Serializable
data class Model(
    val id: String = uuid4().toString(),
    val modelId: String,
    val displayName: String,
    val type: ModelType = ModelType.CHAT,
    val inputModalities: List<Modality> = listOf(Modality.TEXT),
    val outputModalities: List<Modality> = listOf(Modality.TEXT),
    val abilities: List<ModelAbility> = emptyList(),
    val providerType: ProviderType,
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBodies: List<CustomBody> = emptyList()
)

@Serializable
enum class ModelType {
    CHAT,
    IMAGE,
    EMBEDDING
}

@Serializable
enum class Modality {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO
}

@Serializable
enum class ModelAbility {
    REASONING,   // 推理能力
    TOOL,        // 工具调用
    VISION,      // 视觉理解
    STREAMING    // 流式输出
}

/**
 * 推理级别 (Deep Thinking)
 */
@Serializable
enum class ReasoningLevel(
    val budgetTokens: Int,
    val effort: String
) {
    OFF(0, "minimal"),
    AUTO(-1, "auto"),
    LOW(1024, "low"),
    MEDIUM(16_000, "medium"),
    HIGH(32_000, "high");
    
    val isEnabled: Boolean
        get() = this != OFF
}

/**
 * 提供商类型
 */
@Serializable
enum class ProviderType {
    OPENAI,
    CLAUDE,
    GOOGLE,
    QWEN,
    CUSTOM
}

/**
 * 提供商配置
 */
@Serializable
data class ProviderSetting(
    val id: String = uuid4().toString(),
    val type: ProviderType,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val models: List<Model> = emptyList(),
    val customHeaders: Map<String, String> = emptyMap()
)

/**
 * 文本生成参数
 */
@Serializable
data class TextGenerationParams(
    val model: Model,
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxTokens: Int? = null,
    val thinkingBudget: Int? = null,
    val tools: List<Tool> = emptyList(),
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBody: List<CustomBody> = emptyList()
)

/**
 * 自定义请求头
 */
@Serializable
data class CustomHeader(
    val name: String,
    val value: String
)

/**
 * 自定义请求体
 */
@Serializable
data class CustomBody(
    val key: String,
    val value: String
)

/**
 * 工具定义
 */
@Serializable
data class Tool(
    val name: String,
    val description: String,
    val parameters: Map<String, String>
)

/**
 * 助手配置
 */
@Serializable
data class Assistant(
    val id: String = uuid4().toString(),
    val name: String,
    val avatar: String? = null,
    val chatModelId: String? = null,
    val systemPrompt: String = "",
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxTokens: Int? = null,
    val thinkingBudget: Int? = 1024,
    val streamOutput: Boolean = true,
    val contextMessageSize: Int = 64,
    val enableMemory: Boolean = false,
    val presetMessages: List<Message> = emptyList(),
    val quickMessages: List<QuickMessage> = emptyList(),
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBodies: List<CustomBody> = emptyList()
)

/**
 * 快捷消息
 */
@Serializable
data class QuickMessage(
    val title: String,
    val content: String
)

