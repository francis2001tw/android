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
        // DeepSeek Reasoner 模型 - 支持 reasoning_content
        val reasonerModel = Model(
            id = "deepseek-reasoner",
            modelId = "deepseek-reasoner",
            displayName = "DeepSeek Reasoner (R1)",
            providerType = ProviderType.DEEPSEEK,
            abilities = listOf(
                ModelAbility.REASONING,
                ModelAbility.STREAMING
            )
        )
        models[reasonerModel.id] = reasonerModel

        // DeepSeek Chat 模型 - 普通对话模型
        val chatModel = Model(
            id = "deepseek-chat",
            modelId = "deepseek-chat",
            displayName = "DeepSeek Chat",
            providerType = ProviderType.DEEPSEEK,
            abilities = listOf(
                ModelAbility.VISION,
                ModelAbility.STREAMING
            )
        )
        models[chatModel.id] = chatModel

        // 默认助手 - 使用 Reasoner 模型以支持 Deep Thinking
        val defaultAssistant = Assistant(
            id = "default-assistant",
            name = "AI 助手",
            chatModelId = reasonerModel.id,  // 使用 reasoner 模型
            systemPrompt = "你是一个有帮助的 AI 助手。",
            thinkingBudget = 8192,  // 增加思考预算
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

