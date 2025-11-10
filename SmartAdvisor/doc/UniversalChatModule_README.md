# 通用多模态对话模块 (Universal Chat Module)

> 基于 rikkahub 项目提取的通用对话模块设计

## 📋 概述

这是一个从 rikkahub 项目中提取并重新设计的**通用多模态对话模块**，旨在提供一个可复用、可扩展的 AI 对话系统框架。

### 核心特性

✅ **多轮对话管理** - 支持上下文记忆与历史追踪  
✅ **深度思考模式** - 内置 Thinking Phase 与 Response Phase  
✅ **多模态输入** - 支持文本、图片、文档混合输入  
✅ **流式响应** - 实时流式输出 AI 回复  
✅ **对话分支** - 支持编辑历史消息并创建新分支  
✅ **多提供商** - 支持 Qwen、OpenAI、Claude、Google 等  
✅ **完整记录** - 结构化 JSON 存储，包含 Token 统计  

---

## 📁 文档结构

完整设计文档请查看: **[UniversalChatModule_Design.md](./UniversalChatModule_Design.md)**

文档包含以下内容:

1. **功能清单 (Features)** - 详细的功能列表
2. **状态流 (State Flow)** - 对话生命周期状态图
3. **事件模型 (Event Model)** - 用户事件、系统事件、错误事件
4. **数据结构定义 (Data Schema)** - 完整的数据模型
5. **伪代码 (Pseudocode)** - 核心实现逻辑
6. **架构图** - 系统架构、流程图、消息流转图
7. **实现要点** - 深度思考、多模态、分支管理等
8. **数据持久化** - 数据库 Schema 和序列化
9. **总结** - 核心优势、技术栈、适用场景

---

## 🎯 深度思考模式 (Deep Thinking)

这是本模块的核心特性之一，支持两阶段输出:

### Thinking Phase (思考阶段)
- 模型内部推理过程
- 可选择性展示给用户
- 支持流式输出思考内容
- Token 预算控制 (OFF/AUTO/LOW/MEDIUM/HIGH)

### Response Phase (响应阶段)
- 最终输出给用户的答案
- 基于思考阶段的推理结果
- 流式或非流式输出

### 思考级别

```kotlin
enum class ReasoningLevel(val budgetTokens: Int) {
    OFF(0),           // 关闭
    AUTO(-1),         // 自动
    LOW(1024),        // 低 (1K tokens)
    MEDIUM(16_000),   // 中 (16K tokens)
    HIGH(32_000)      // 高 (32K tokens)
}
```

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────┐
│           UI Layer                      │
│  Chat Screen | Settings | History      │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        Service Layer                    │
│  UniversalChatService                   │
│  - sendMessage()                        │
│  - generateResponseStream()             │
│  - observeConversation()                │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       Provider Layer                    │
│  Qwen | OpenAI | Claude | Google        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        Data Layer                       │
│  ConversationRepository                 │
│  Room Database                          │
└─────────────────────────────────────────┘
```

---

## 💡 快速开始

### 1. 初始化服务

```kotlin
// 配置提供商
val qwenProvider = ProviderSetting(
    id = "qwen-provider",
    type = ProviderType.QWEN,
    name = "Qwen",
    baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
    apiKey = "your-api-key"
)

// 创建助手
val assistant = Assistant(
    id = "assistant-1",
    name = "AI 助手",
    systemPrompt = "你是一个有帮助的 AI 助手。",
    thinkingBudget = 1024,  // 启用低级别深度思考
    streamOutput = true
)

// 初始化服务
val chatService = UniversalChatServiceImpl(
    conversationRepository = conversationRepository,
    providerManager = providerManager,
    fileManager = fileManager,
    settingsStore = settingsStore
)
```

### 2. 创建对话

```kotlin
val conversation = chatService.createConversation(
    assistantId = assistant.id
)
```

### 3. 发送消息

```kotlin
// 文本消息
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("什么是量子计算？")
    )
)

// 多模态消息
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("这张图片里有什么？"),
        MessagePart.Image(url = "file:///path/to/image.jpg")
    )
)
```

### 4. 流式生成响应

```kotlin
chatService.generateResponseStream(
    conversationId = conversation.id
).collect { chunk ->
    when (chunk) {
        is GenerationChunk.ThinkingChunk -> {
            println("思考中: ${chunk.content}")
        }
        is GenerationChunk.ThinkingComplete -> {
            println("思考完成")
        }
        is GenerationChunk.ResponseChunk -> {
            print(chunk.content)
        }
        is GenerationChunk.ResponseComplete -> {
            println("\n完成! Token 使用: ${chunk.usage}")
        }
        is GenerationChunk.Error -> {
            println("错误: ${chunk.error.message}")
        }
    }
}
```

---

## 📊 数据结构

### 核心数据模型

```kotlin
// 对话
data class Conversation(
    val id: String,
    val assistantId: String,
    val title: String,
    val messageNodes: List<MessageNode>,
    val createAt: Instant,
    val updateAt: Instant
)

// 消息
data class Message(
    val id: String,
    val role: MessageRole,
    val parts: List<MessagePart>,
    val createdAt: Instant,
    val modelId: String?,
    val usage: TokenUsage?
)

// 消息部分 (多模态)
sealed class MessagePart {
    data class Text(val text: String)
    data class Image(val url: String)
    data class Document(val url: String, val fileName: String, val mimeType: String)
    data class Reasoning(val reasoning: String, val createdAt: Instant, val finishedAt: Instant?)
    data class ToolCall(val toolCallId: String, val toolName: String, val arguments: String)
    data class ToolResult(val toolCallId: String, val toolName: String, val content: Any)
}

// Token 使用统计
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val cachedTokens: Int,
    val totalTokens: Int
)
```

---

## 🔧 技术栈

- **语言**: Kotlin
- **异步**: Coroutines + Flow
- **序列化**: kotlinx.serialization
- **网络**: Ktor Client
- **数据库**: Room
- **依赖注入**: Koin (可选)

---

## 🎨 适用场景

- ✅ Android 应用
- ✅ Kotlin Multiplatform (KMP) 项目
- ✅ 桌面应用 (Compose Desktop)
- ✅ 服务端应用 (Ktor Server)

---

## 🚀 后续扩展方向

1. **语音输入/输出** - 添加 Audio modality
2. **视频理解** - 添加 Video modality
3. **实时对话** - WebSocket 支持
4. **协作对话** - 多用户共享对话
5. **插件系统** - 支持自定义工具和扩展
6. **云同步** - 跨设备对话同步
7. **导出功能** - 导出为 Markdown/PDF/HTML

---

## 📚 参考资源

- **rikkahub 项目**: 原始参考实现
- **OpenAI API**: https://platform.openai.com/docs/api-reference
- **Qwen API**: https://help.aliyun.com/zh/dashscope/
- **Claude API**: https://docs.anthropic.com/claude/reference

---

## 📄 许可证

本设计文档基于 rikkahub 项目提取，遵循原项目许可证。

---

## 📝 版本历史

- **v1.0** (2025-11-09): 初始版本
  - 完整的功能清单
  - 状态流和事件模型
  - 数据结构定义
  - 伪代码实现
  - 深度思考模式支持
  - 多模态输入支持

---

**文档作者**: AI Assistant  
**创建日期**: 2025-11-09  
**基于项目**: rikkahub

