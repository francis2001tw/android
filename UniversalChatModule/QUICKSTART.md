# 快速入门指南

## 📋 前置要求

1. **安装 JDK 17+**
   - 下载: https://adoptium.net/
   - 验证: `java -version`

2. **安装 Gradle** (可选，可使用 gradlew)
   - 下载: https://gradle.org/install/
   - 验证: `gradle -version`

## 🚀 5 分钟快速开始

### 步骤 1: 进入项目目录

```bash
cd UniversalChatModule
```

### 步骤 2: 配置 API Key

编辑 `src/main/kotlin/com/universalchat/Main.kt`，找到以下代码并替换 API Key:

```kotlin
val qwenSetting = ProviderSetting(
    type = ProviderType.QWEN,
    name = "Qwen",
    baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
    apiKey = "YOUR_API_KEY_HERE"  // ⬅️ 在这里替换为您的 API Key
)
```

**获取 Qwen API Key:**
1. 访问: https://dashscope.aliyun.com/
2. 注册/登录阿里云账号
3. 进入控制台获取 API Key

### 步骤 3: 构建项目

**Windows:**
```bash
gradle build
```

**Linux/Mac:**
```bash
./gradlew build
```

### 步骤 4: 运行示例

**Windows:**
```bash
gradle run
```

**Linux/Mac:**
```bash
./gradlew run
```

或者直接运行批处理文件 (Windows):
```bash
run-example.bat
```

## 📝 预期输出

运行成功后，您将看到:

```
============================================================
通用多模态对话模块 (Universal Chat Module)
============================================================

📦 初始化组件...
✅ 初始化完成!

💬 创建新对话...
✅ 对话创建成功! ID: xxx-xxx-xxx

============================================================
示例 1: 简单文本对话
============================================================
👤 用户: 你好，请介绍一下你自己
🤖 AI: 你好！我是一个 AI 助手...
✅ 完成! Token 使用: 150

...
```

## 🎯 核心代码示例

### 1. 创建对话服务

```kotlin
// 初始化组件
val settingsStore = InMemorySettingsStore()
val conversationRepository = InMemoryConversationRepository()

// 配置提供商
val qwenProvider = QwenProvider(qwenSetting)
val providerManager = ProviderManager(
    providers = mapOf(ProviderType.QWEN to qwenProvider)
)

// 创建服务
val chatService = UniversalChatServiceImpl(
    conversationRepository = conversationRepository,
    providerManager = providerManager,
    settingsStore = settingsStore
)
```

### 2. 发送消息并获取响应

```kotlin
// 创建对话
val conversation = chatService.createConversation(
    assistantId = "default-assistant"
)

// 发送消息
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(MessagePart.Text("你好")),
    autoGenerate = false
)

// 流式生成响应
chatService.generateResponseStream(conversation.id).collect { chunk ->
    when (chunk) {
        is GenerationChunk.ResponseChunk -> {
            print(chunk.content)  // 实时输出
        }
        is GenerationChunk.ResponseComplete -> {
            println("\n完成!")
        }
        is GenerationChunk.Error -> {
            println("错误: ${chunk.error.message}")
        }
        else -> {}
    }
}
```

### 3. 深度思考模式

```kotlin
// 发送需要深度思考的问题
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(MessagePart.Text("解释量子纠缠")),
    autoGenerate = false
)

// 观察思考过程
chatService.generateResponseStream(conversation.id).collect { chunk ->
    when (chunk) {
        is GenerationChunk.ThinkingChunk -> {
            println("思考: ${chunk.content}")
        }
        is GenerationChunk.ThinkingComplete -> {
            println("思考完成!")
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

### 4. 多模态输入

```kotlin
// 发送图片
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("这张图片里有什么?"),
        MessagePart.Image("https://example.com/image.jpg")
    )
)

// 发送文档
chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("总结这个文档"),
        MessagePart.Document(
            url = "file:///path/to/doc.pdf",
            fileName = "doc.pdf",
            mimeType = "application/pdf"
        )
    )
)
```

## 🔧 常见问题

### Q1: 构建失败 - "Could not resolve dependencies"

**解决方案:**
1. 检查网络连接
2. 配置 Gradle 镜像 (中国用户):

编辑 `build.gradle.kts`:
```kotlin
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    mavenCentral()
}
```

### Q2: 运行时错误 - "Provider not found"

**解决方案:**
确保已正确配置提供商:
```kotlin
val providerManager = ProviderManager(
    providers = mapOf(
        ProviderType.QWEN to QwenProvider(qwenSetting)
    )
)
```

### Q3: API 调用失败 - "Unauthorized"

**解决方案:**
1. 检查 API Key 是否正确
2. 检查 API Key 是否有效
3. 检查网络连接

### Q4: 如何使用其他 AI 提供商?

**解决方案:**
实现 `Provider` 接口并注册:

```kotlin
class OpenAIProvider(private val setting: ProviderSetting) : Provider {
    // 实现接口方法
}

val providerManager = ProviderManager(
    providers = mapOf(
        ProviderType.QWEN to QwenProvider(qwenSetting),
        ProviderType.OPENAI to OpenAIProvider(openaiSetting)
    )
)
```

## 📚 下一步

1. **阅读完整文档**: 查看 [README.md](README.md)
2. **查看设计文档**: 查看 [UniversalChatModule_Design.md](../UniversalChatModule_Design.md)
3. **自定义配置**: 修改 `Main.kt` 中的配置
4. **添加新功能**: 扩展 `Provider` 接口支持更多 AI 提供商
5. **集成到项目**: 将模块集成到您的应用中

## 💡 提示

- 首次运行会下载依赖，可能需要几分钟
- 建议使用 IntelliJ IDEA 或 Android Studio 打开项目
- 可以使用 `gradle --daemon` 加速构建
- 查看 `Main.kt` 了解更多使用示例

## 🆘 获取帮助

如果遇到问题:
1. 查看错误日志
2. 检查 API Key 配置
3. 查看 [README.md](README.md) 中的详细文档
4. 提交 Issue

---

**祝您使用愉快！** 🎉

