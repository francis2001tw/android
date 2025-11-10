package com.example.smartadvisor.storage

import com.example.smartadvisor.model.Assistant
import com.example.smartadvisor.model.Model
import com.example.smartadvisor.model.ModelAbility
import com.example.smartadvisor.model.ProviderType

/**
 * 设置存储接口
 */
interface SettingsStore {
    fun getAssistant(assistantId: String): Assistant
    fun getModel(modelId: String): Model
    fun getDefaultModel(): Model
}

/**
 * 内存实现 (用于演示)
 */
class InMemorySettingsStore : SettingsStore {
    private val assistants = mutableMapOf<String, Assistant>()
    private val models = mutableMapOf<String, Model>()
    
    init {
        // 默认模型
        val defaultModel = Model(
            id = "deepseek-chat",
            modelId = "deepseek-chat",
            displayName = "DeepSeek Chat",
            providerType = ProviderType.DEEPSEEK,
            abilities = listOf(
                ModelAbility.REASONING,
                ModelAbility.VISION,
                ModelAbility.STREAMING
            )
        )
        models[defaultModel.id] = defaultModel

        // 默认助手
        val defaultAssistant = Assistant(
            id = "default-assistant",
            name = "AI 助手",
            chatModelId = defaultModel.id,
            systemPrompt = "你是一个有帮助的 AI 助手。",
            thinkingBudget = 1024,
            streamOutput = true
        )
        assistants[defaultAssistant.id] = defaultAssistant
    }
    
    override fun getAssistant(assistantId: String): Assistant {
        return assistants[assistantId] ?: error("Assistant not found: $assistantId")
    }
    
    override fun getModel(modelId: String): Model {
        return models[modelId] ?: error("Model not found: $modelId")
    }
    
    override fun getDefaultModel(): Model {
        return models.values.first()
    }
    
    fun addAssistant(assistant: Assistant) {
        assistants[assistant.id] = assistant
    }
    
    fun addModel(model: Model) {
        models[model.id] = model
    }
}

