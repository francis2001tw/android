package com.example.smartadvisor

import com.example.smartadvisor.model.*
import com.example.smartadvisor.provider.DeepSeekProvider
import com.example.smartadvisor.repository.InMemoryConversationRepository
import com.example.smartadvisor.service.GenerationChunk
import com.example.smartadvisor.service.ProviderManager
import com.example.smartadvisor.service.UniversalChatServiceImpl
import com.example.smartadvisor.storage.InMemorySettingsStore
import kotlinx.coroutines.runBlocking

/**
 * 通用对话模块 - 主程序示例
 */
fun main() = runBlocking {
    println("=".repeat(60))
    println("通用多模态对话模块 (Universal Chat Module)")
    println("=".repeat(60))
    println()
    
    // 1. 初始化组件
    println("📦 初始化组件...")
    val settingsStore = InMemorySettingsStore()
    val conversationRepository = InMemoryConversationRepository()
    
    // 2. 配置 DeepSeek 提供商
    val deepseekSetting = ProviderSetting(
        type = ProviderType.DEEPSEEK,
        name = "DeepSeek",
        baseUrl = "https://api.deepseek.com",
        apiKey = "sk-9e0f6612f850465f9057ef5e0d0ce641"
    )

    val deepseekProvider = DeepSeekProvider(deepseekSetting)
    val providerManager = ProviderManager(
        providers = mapOf(ProviderType.DEEPSEEK to deepseekProvider)
    )
    
    // 3. 创建对话服务
    val chatService = UniversalChatServiceImpl(
        conversationRepository = conversationRepository,
        providerManager = providerManager,
        settingsStore = settingsStore
    )
    
    println("✅ 初始化完成!")
    println()
    
    // 4. 创建对话
    println("💬 创建新对话...")
    val conversation = chatService.createConversation(assistantId = "default-assistant")
    println("✅ 对话创建成功! ID: ${conversation.id}")
    println()
    
    // 5. 示例 1: 简单文本对话
    println("=" * 60)
    println("示例 1: 简单文本对话")
    println("=" * 60)
    runSimpleTextChat(chatService, conversation.id)
    println()
    
    // 6. 示例 2: 深度思考模式
    println("=" * 60)
    println("示例 2: 深度思考模式")
    println("=" * 60)
    runDeepThinkingChat(chatService, conversation.id)
    println()
    
    // 7. 示例 3: 多模态输入
    println("=" * 60)
    println("示例 3: 多模态输入 (文本 + 图片)")
    println("=" * 60)
    runMultimodalChat(chatService, conversation.id)
    println()
    
    // 8. 显示对话统计
    println("=" * 60)
    println("对话统计")
    println("=" * 60)
    showConversationStats(chatService, conversation.id)
    
    println()
    println("=" * 60)
    println("演示完成!")
    println("=" * 60)
}

/**
 * 示例 1: 简单文本对话
 */
suspend fun runSimpleTextChat(chatService: UniversalChatServiceImpl, conversationId: String) {
    println("👤 用户: 你好，请介绍一下你自己")
    
    // 发送消息
    chatService.sendMessage(
        conversationId = conversationId,
        content = listOf(MessagePart.Text("你好，请介绍一下你自己")),
        autoGenerate = false
    )
    
    // 生成响应 (流式)
    print("🤖 AI: ")
    chatService.generateResponseStream(conversationId).collect { chunk ->
        when (chunk) {
            is GenerationChunk.ResponseChunk -> {
                print(chunk.content)
            }
            is GenerationChunk.ResponseComplete -> {
                println()
                println("✅ 完成! Token 使用: ${chunk.usage.totalTokens}")
            }
            is GenerationChunk.Error -> {
                println()
                println("❌ 错误: ${chunk.error.message}")
            }
            else -> {}
        }
    }
}

/**
 * 示例 2: 深度思考模式
 */
suspend fun runDeepThinkingChat(chatService: UniversalChatServiceImpl, conversationId: String) {
    println("👤 用户: 解释一下量子纠缠的原理")
    
    // 发送消息
    chatService.sendMessage(
        conversationId = conversationId,
        content = listOf(MessagePart.Text("解释一下量子纠缠的原理")),
        autoGenerate = false
    )
    
    // 生成响应 (带深度思考)
    var thinkingContent = ""
    var responseContent = ""
    
    chatService.generateResponseStream(conversationId).collect { chunk ->
        when (chunk) {
            is GenerationChunk.StreamTarget -> {
                // CLI demo doesn't need to do anything; UI uses this to bind overlay
            }
            is GenerationChunk.ThinkingChunk -> {
                if (thinkingContent.isEmpty()) {
                    println("🧠 思考中...")
                }
                thinkingContent += chunk.content
            }
            is GenerationChunk.ThinkingComplete -> {
                println("✅ 思考完成 (${thinkingContent.length} 字符)")
                println()
                print("🤖 AI: ")
            }
            is GenerationChunk.ResponseChunk -> {
                print(chunk.content)
                responseContent += chunk.content
            }
            is GenerationChunk.ResponseComplete -> {
                println()
                println("✅ 完成! Token 使用: ${chunk.usage.totalTokens}")
            }
            is GenerationChunk.Error -> {
                println()
                println("❌ 错误: ${chunk.error.message}")
            }
        }
    }
}

/**
 * 示例 3: 多模态输入
 */
suspend fun runMultimodalChat(chatService: UniversalChatServiceImpl, conversationId: String) {
    println("👤 用户: [文本 + 图片]")
    println("   文本: 这张图片里有什么?")
    println("   图片: https://example.com/image.jpg")
    
    // 发送多模态消息
    chatService.sendMessage(
        conversationId = conversationId,
        content = listOf(
            MessagePart.Text("这张图片里有什么?"),
            MessagePart.Image("https://example.com/image.jpg")
        ),
        autoGenerate = false
    )
    
    // 生成响应
    print("🤖 AI: ")
    chatService.generateResponseStream(conversationId).collect { chunk ->
        when (chunk) {
            is GenerationChunk.ResponseChunk -> {
                print(chunk.content)
            }
            is GenerationChunk.ResponseComplete -> {
                println()
                println("✅ 完成! Token 使用: ${chunk.usage.totalTokens}")
            }
            is GenerationChunk.Error -> {
                println()
                println("❌ 错误: ${chunk.error.message}")
            }
            else -> {}
        }
    }
}

/**
 * 显示对话统计
 */
suspend fun showConversationStats(chatService: UniversalChatServiceImpl, conversationId: String) {
    val conversation = chatService.loadConversation(conversationId)
    
    if (conversation != null) {
        val messages = conversation.getCurrentMessages()
        val totalMessages = messages.size
        val userMessages = messages.count { it.role == MessageRole.USER }
        val aiMessages = messages.count { it.role == MessageRole.ASSISTANT }
        
        println("📊 对话 ID: ${conversation.id}")
        println("📊 标题: ${conversation.title.ifBlank { "(未命名)" }}")
        println("📊 消息总数: $totalMessages")
        println("📊 用户消息: $userMessages")
        println("📊 AI 消息: $aiMessages")
        println("📊 创建时间: ${conversation.createAt}")
        println("📊 更新时间: ${conversation.updateAt}")
        
        // Token 统计
        chatService.getTokenUsage(conversationId).collect { usage ->
            println("📊 Token 使用:")
            println("   - Prompt Tokens: ${usage.promptTokens}")
            println("   - Completion Tokens: ${usage.completionTokens}")
            println("   - Total Tokens: ${usage.totalTokens}")
        }
    }
}

// 扩展函数: 字符串重复
private operator fun String.times(n: Int): String = repeat(n)

