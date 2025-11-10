# 通用多模态对话模块 (Universal Chat Module)

一个基于 Kotlin 的通用多模态对话模块，支持深度思考、流式响应、多模态输入等功能。

## 🚀 快速开始

### 1. 环境要求

- JDK 17 或更高版本
- Gradle 8.0 或更高版本

### 2. 构建项目

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### 3. 运行示例

```bash
# Windows
gradlew.bat run

# Linux/Mac
./gradlew run
```

## 📁 项目结构

```
UniversalChatModule/
├── src/main/kotlin/com/universalchat/
│   ├── model/                    # 数据模型
│   │   ├── Message.kt           # 消息相关模型
│   │   ├── Conversation.kt      # 对话模型
│   │   └── Model.kt             # AI 模型配置
│   ├── provider/                 # AI 提供商
│   │   ├── Provider.kt          # 提供商接口
│   │   └── QwenProvider.kt      # Qwen 实现
│   ├── service/                  # 核心服务
│   │   ├── ChatService.kt       # 服务接口
│   │   └── ChatServiceImpl.kt   # 服务实现
│   ├── repository/               # 数据仓库
│   │   └── ConversationRepository.kt
│   ├── storage/                  # 存储层
│   │   └── SettingsStore.kt
│   └── Main.kt                   # 主程序示例
├── build.gradle.kts              # Gradle 配置
└── README.md                     # 本文件
```

## 🎯 核心功能

### 1. 多轮对话管理

```kotlin
// 创建对话
val conversation = chatService.createConversation(assistantId = "default-assistant")

// 发送消息
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(MessagePart.Text("你好"))
)
```

### 2. 深度思考模式 (Deep Thinking)

```kotlin
// 启用深度思考
chatService.generateResponseStream(conversationId).collect { chunk ->
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
            println("\n完成! Token: ${chunk.usage.totalTokens}")
        }
    }
}
```

### 3. 流式响应

```kotlin
// 流式生成响应
chatService.generateResponseStream(conversationId).collect { chunk ->
    when (chunk) {
        is GenerationChunk.ResponseChunk -> {
            print(chunk.content)  // 实时输出
        }
        is GenerationChunk.ResponseComplete -> {
            println("\n完成!")
        }
    }
}
```

### 4. 多模态输入

```kotlin
// 文本 + 图片
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("这张图片里有什么?"),
        MessagePart.Image("https://example.com/image.jpg")
    )
)

// 文本 + 文档
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("总结这个文档"),
        MessagePart.Document(
            url = "file:///path/to/document.pdf",
            fileName = "document.pdf",
            mimeType = "application/pdf"
        )
    )
)
```

### 5. 对话分支

```kotlin
// 对话支持树状结构，可以编辑历史消息并创建新分支
val conversation = chatService.loadConversation(conversationId)
val messages = conversation?.getCurrentMessages()  // 获取当前分支的消息
```

## 🔧 配置

### 配置 Qwen 提供商

在 `Main.kt` 中修改 API Key:

```kotlin
val qwenSetting = ProviderSetting(
    type = ProviderType.QWEN,
    name = "Qwen",
    baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
    apiKey = "YOUR_API_KEY_HERE"  // 替换为您的 API Key
)
```

### 配置助手

```kotlin
val assistant = Assistant(
    id = "my-assistant",
    name = "我的助手",
    systemPrompt = "你是一个有帮助的 AI 助手。",
    thinkingBudget = 1024,      // 思考 Token 预算
    streamOutput = true,         // 启用流式输出
    contextMessageSize = 64,     // 上下文消息数量
    temperature = 0.7f,          // 温度参数
    maxTokens = 2000            // 最大 Token 数
)

settingsStore.addAssistant(assistant)
```

### 配置模型

```kotlin
val model = Model(
    id = "qwen-plus",
    modelId = "qwen-plus",
    displayName = "Qwen Plus",
    providerType = ProviderType.QWEN,
    abilities = listOf(
        ModelAbility.REASONING,   // 支持推理
        ModelAbility.VISION,      // 支持视觉
        ModelAbility.STREAMING    // 支持流式
    )
)

settingsStore.addModel(model)
```

## 📊 数据模型

### Message (消息)

```kotlin
@Serializable
data class Message(
    val id: String,
    val role: MessageRole,           // USER, ASSISTANT, SYSTEM, TOOL
    val parts: List<MessagePart>,    // 多模态内容
    val createdAt: Instant,
    val modelId: String?,
    val usage: TokenUsage?
)
```

### MessagePart (消息部分)

```kotlin
sealed class MessagePart {
    data class Text(val text: String)
    data class Image(val url: String)
    data class Document(val url: String, val fileName: String, val mimeType: String)
    data class Reasoning(val reasoning: String, val createdAt: Instant, val finishedAt: Instant?)
    data class ToolCall(val toolCallId: String, val toolName: String, val arguments: String)
    data class ToolResult(val toolCallId: String, val toolName: String, val content: JsonElement)
}
```

### Conversation (对话)

```kotlin
@Serializable
data class Conversation(
    val id: String,
    val assistantId: String,
    val title: String,
    val messageNodes: List<MessageNode>,  // 支持分支
    val createAt: Instant,
    val updateAt: Instant
)
```

## 🧪 运行测试

```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

## 📝 示例输出

运行 `Main.kt` 后，您将看到类似以下的输出:

```
============================================================
通用多模态对话模块 (Universal Chat Module)
============================================================

📦 初始化组件...
✅ 初始化完成!

💬 创建新对话...
✅ 对话创建成功! ID: 550e8400-e29b-41d4-a716-446655440000

============================================================
示例 1: 简单文本对话
============================================================
👤 用户: 你好，请介绍一下你自己
🤖 AI: 你好！我是一个 AI 助手...
✅ 完成! Token 使用: 150

============================================================
示例 2: 深度思考模式
============================================================
👤 用户: 解释一下量子纠缠的原理
🧠 思考中...
✅ 思考完成 (256 字符)

🤖 AI: 量子纠缠是量子力学中的一个重要现象...
✅ 完成! Token 使用: 450

============================================================
对话统计
============================================================
📊 对话 ID: 550e8400-e29b-41d4-a716-446655440000
📊 消息总数: 6
📊 用户消息: 3
📊 AI 消息: 3
📊 Token 使用:
   - Prompt Tokens: 200
   - Completion Tokens: 400
   - Total Tokens: 600
```

## 🔌 扩展提供商

要添加新的 AI 提供商 (如 OpenAI, Claude):

1. 实现 `Provider` 接口:

```kotlin
class OpenAIProvider(private val setting: ProviderSetting) : Provider {
    override fun streamText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Flow<MessageChunk> {
        // 实现 OpenAI 流式调用
    }
    
    override suspend fun generateText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Message {
        // 实现 OpenAI 非流式调用
    }
}
```

2. 注册到 `ProviderManager`:

```kotlin
val providerManager = ProviderManager(
    providers = mapOf(
        ProviderType.QWEN to QwenProvider(qwenSetting),
        ProviderType.OPENAI to OpenAIProvider(openaiSetting)
    )
)
```

## 📚 依赖项

- **Kotlin**: 1.9.22
- **Kotlinx Coroutines**: 1.7.3
- **Kotlinx Serialization**: 1.6.2
- **Kotlinx DateTime**: 0.5.0
- **Ktor Client**: 2.3.7
- **UUID**: 0.8.2
- **Kotlin Logging**: 5.1.0

## 📄 许可证

本项目基于 rikkahub 项目提取，遵循原项目许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request!

## 📧 联系方式

如有问题，请通过 Issue 联系。

