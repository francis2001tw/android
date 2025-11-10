# 通用多模态对话模块设计文档

基于 rikkahub 项目提取的通用对话模块设计

---

## 1. 功能清单 (Features)

### 1.1 核心对话功能
- **多轮对话管理**: 支持上下文记忆与历史对话追踪
- **流式响应**: 实时流式输出 AI 回复
- **非流式响应**: 一次性获取完整回复
- **对话分支**: 支持消息节点树状结构，允许编辑历史消息并创建新分支

### 1.2 深度思考模式 (Deep Thinking)
- **思考阶段 (Thinking Phase)**: 模型内部推理过程，可选择性展示
- **响应阶段 (Response Phase)**: 最终输出给用户的答案
- **思考预算控制**: 可配置思考 token 上限 (OFF/AUTO/LOW/MEDIUM/HIGH)
- **思考内容可视化**: 支持展开/折叠思考过程
- **自动关闭思考**: 完成后可自动折叠思考内容

### 1.3 多模态输入支持
- **文本输入**: 纯文本消息
- **图片输入**:
  - 从相册选择
  - 拍照上传
  - 支持多张图片
- **文件上传**:
  - 支持文档类型: PDF, DOCX, TXT, Markdown, 代码文件等
  - 文件类型验证
  - 本地文件缓存管理
- **混合输入**: 单条消息可包含文本+图片+文件的组合

### 1.4 模型设置
- **默认模型**: Qwen (可配置)
- **模型切换**: 支持运行时切换不同模型
- **多提供商支持**: OpenAI, Claude, Google, 自定义提供商
- **模型能力检测**: 自动识别模型支持的功能 (推理/工具/多模态)
- **自定义参数**: Temperature, TopP, MaxTokens, 自定义 Headers/Body

### 1.5 对话记录管理
- **持久化存储**: 对话保存到本地数据库
- **结构化 JSON**: 包含完整元数据
- **搜索功能**: 按标题/内容搜索历史对话
- **分页加载**: 大量对话的高效加载
- **导入/导出**: 对话数据的备份与恢复
- **自动标题生成**: 根据对话内容自动生成标题
- **对话建议**: 自动生成后续问题建议

### 1.6 高级功能
- **Token 使用统计**: 记录每条消息的 token 消耗
- **消息转换器**: 输入/输出消息的预处理和后处理
- **工具调用**: 支持 Function Calling
- **记忆系统**: 长期记忆存储与检索
- **消息模板**: 自定义消息格式化
- **正则替换**: 输出内容的自动替换规则

---

## 2. 状态流 (State Flow)

### 2.1 对话生命周期状态

```
[IDLE] 空闲状态
  ↓
[INITIALIZING] 初始化对话
  ↓
[READY] 准备接收输入
  ↓
[USER_INPUT] 用户输入中
  ↓
[VALIDATING] 验证输入 (文件类型、大小等)
  ↓
[THINKING_PHASE] 深度思考阶段 (可选)
  │ ├─ [THINKING_STREAMING] 思考内容流式输出
  │ └─ [THINKING_COMPLETE] 思考完成
  ↓
[RESPONSE_PHASE] 响应阶段
  │ ├─ [STREAMING] 流式输出响应
  │ └─ [GENERATING] 非流式生成
  ↓
[COMPLETE] 生成完成
  ↓
[SAVING] 保存对话
  ↓
[READY] 返回准备状态

[ERROR] 错误状态 (可从任意状态进入)
  ↓
[READY] 恢复到准备状态
```

### 2.2 消息状态

```
[PENDING] 待发送
  ↓
[SENDING] 发送中
  ↓
[SENT] 已发送
  ↓
[PROCESSING] AI 处理中
  ↓
[PARTIAL] 部分响应 (流式)
  ↓
[COMPLETED] 完成
  ↓
[SAVED] 已保存

[FAILED] 失败
[CANCELLED] 已取消
```

---

## 3. 事件模型 (Event Model)

### 3.1 用户事件
```kotlin
sealed class UserEvent {
    // 对话管理
    data class CreateConversation(val assistantId: String) : UserEvent()
    data class LoadConversation(val conversationId: String) : UserEvent()
    data class DeleteConversation(val conversationId: String) : UserEvent()

    // 消息操作
    data class SendMessage(val content: List<MessagePart>) : UserEvent()
    data class EditMessage(val messageId: String, val newContent: List<MessagePart>) : UserEvent()
    data class DeleteMessage(val messageId: String) : UserEvent()
    data class RegenerateMessage(val messageId: String) : UserEvent()
    data class CancelGeneration(val conversationId: String) : UserEvent()

    // 输入操作
    data class AddText(val text: String) : UserEvent()
    data class AddImages(val imageUris: List<String>) : UserEvent()
    data class AddFiles(val fileUris: List<String>) : UserEvent()
    data class RemoveAttachment(val attachmentId: String) : UserEvent()

    // 设置操作
    data class ChangeModel(val modelId: String) : UserEvent()
    data class UpdateThinkingBudget(val budget: Int) : UserEvent()
    data class ToggleStreamMode(val enabled: Boolean) : UserEvent()
}
```

### 3.2 系统事件
```kotlin
sealed class SystemEvent {
    // 生成事件
    data class GenerationStarted(val conversationId: String) : SystemEvent()
    data class ThinkingPhaseStarted(val conversationId: String) : SystemEvent()
    data class ThinkingChunk(val conversationId: String, val content: String) : SystemEvent()
    data class ThinkingPhaseCompleted(val conversationId: String) : SystemEvent()
    data class ResponseChunk(val conversationId: String, val content: String) : SystemEvent()
    data class GenerationCompleted(val conversationId: String, val usage: TokenUsage) : SystemEvent()
    data class GenerationFailed(val conversationId: String, val error: Throwable) : SystemEvent()

    // 对话事件
    data class ConversationLoaded(val conversation: Conversation) : SystemEvent()
    data class ConversationSaved(val conversationId: String) : SystemEvent()
    data class TitleGenerated(val conversationId: String, val title: String) : SystemEvent()
    data class SuggestionsGenerated(val conversationId: String, val suggestions: List<String>) : SystemEvent()

    // 状态事件
    data class StateChanged(val oldState: ConversationState, val newState: ConversationState) : SystemEvent()
    data class TokenUsageUpdated(val usage: TokenUsage) : SystemEvent()
}
```

### 3.3 错误事件
```kotlin
sealed class ErrorEvent {
    data class NetworkError(val message: String) : ErrorEvent()
    data class ValidationError(val field: String, val message: String) : ErrorEvent()
    data class ModelError(val modelId: String, val message: String) : ErrorEvent()
    data class FileError(val fileUri: String, val message: String) : ErrorEvent()
    data class StorageError(val message: String) : ErrorEvent()
}
```

---

## 4. 数据结构定义 (Data Schema)

### 4.1 核心数据模型

```kotlin
/**
 * 对话实体
 */
data class Conversation(
    val id: String,                          // 唯一标识
    val assistantId: String,                 // 助手 ID
    val title: String = "",                  // 对话标题
    val messageNodes: List<MessageNode>,     // 消息节点树
    val truncateIndex: Int = -1,             // 上下文截断索引
    val chatSuggestions: List<String> = emptyList(), // 对话建议
    val isPinned: Boolean = false,           // 是否置顶
    val createAt: Instant,                   // 创建时间
    val updateAt: Instant,                   // 更新时间
    val metadata: ConversationMetadata = ConversationMetadata()
)

/**
 * 对话元数据
 */
data class ConversationMetadata(
    val totalTokens: Int = 0,                // 总 token 消耗
    val messageCount: Int = 0,               // 消息数量
    val modelIds: Set<String> = emptySet(),  // 使用过的模型
    val tags: List<String> = emptyList()     // 标签
)

/**
 * 消息节点 (支持分支)
 */
data class MessageNode(
    val id: String,
    val messages: List<Message>,             // 同一节点的多个版本
    val currentIndex: Int = 0,               // 当前选中的版本
    val parentId: String? = null,            // 父节点 ID
    val childrenIds: List<String> = emptyList() // 子节点 ID 列表
) {
    val currentMessage: Message
        get() = messages[currentIndex]
}

/**
 * 消息实体
 */
data class Message(
    val id: String,
    val role: MessageRole,                   // USER / ASSISTANT / SYSTEM
    val parts: List<MessagePart>,            // 消息部分 (多模态)
    val annotations: List<MessageAnnotation> = emptyList(),
    val createdAt: Instant,
    val modelId: String? = null,             // 生成此消息的模型
    val usage: TokenUsage? = null,           // Token 使用情况
    val translation: String? = null          // 翻译内容
)

/**
 * 消息角色
 */
enum class MessageRole {
    USER,        // 用户
    ASSISTANT,   // AI 助手
    SYSTEM,      // 系统提示
    TOOL         // 工具调用结果
}

/**
 * 消息部分 (多模态)
 */
sealed class MessagePart {
    abstract val priority: Int
    abstract val metadata: Map<String, Any>?

    data class Text(
        val text: String,
        override val metadata: Map<String, Any>? = null
    ) : MessagePart() {
        override val priority = 0
    }

    data class Image(
        val url: String,                     // 本地路径或远程 URL
        override val metadata: Map<String, Any>? = null
    ) : MessagePart() {
        override val priority = 1
    }

    data class Document(
        val url: String,
        val fileName: String,
        val mimeType: String,
        override val metadata: Map<String, Any>? = null
    ) : MessagePart() {
        override val priority = 1
    }

    /**
     * 推理内容 (Deep Thinking)
     */
    data class Reasoning(
        val reasoning: String,               // 思考内容
        val createdAt: Instant,
        val finishedAt: Instant? = null,     // 完成时间 (null 表示进行中)
        override val metadata: Map<String, Any>? = null
    ) : MessagePart() {
        override val priority = -1           // 优先级最低，显示在最前
    }

    data class ToolCall(
        val toolCallId: String,
        val toolName: String,
        val arguments: String,
        override val metadata: Map<String, Any>? = null
    ) : MessagePart() {
        override val priority = 0
    }

    data class ToolResult(
        val toolCallId: String,
        val toolName: String,
        val content: Any,
        val arguments: Any,
        override val metadata: Map<String, Any>? = null
    ) : MessagePart() {
        override val priority = 0
    }
}

/**
 * 消息注解
 */
sealed class MessageAnnotation {
    data class UrlCitation(
        val title: String,
        val url: String
    ) : MessageAnnotation()
}

/**
 * Token 使用统计
 */
data class TokenUsage(
    val promptTokens: Int = 0,               // 输入 tokens
    val completionTokens: Int = 0,           // 输出 tokens
    val cachedTokens: Int = 0,               // 缓存 tokens
    val totalTokens: Int = 0                 // 总计
)

/**
 * 模型配置
 */
data class Model(
    val id: String,
    val modelId: String,                     // 模型标识符 (如 "qwen-max")
    val displayName: String,
    val type: ModelType,
    val inputModalities: List<Modality>,     // 支持的输入模态
    val outputModalities: List<Modality>,    // 支持的输出模态
    val abilities: List<ModelAbility>,       // 模型能力
    val providerType: ProviderType,
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBodies: List<CustomBody> = emptyList()
)

enum class ModelType {
    CHAT,        // 对话模型
    IMAGE,       // 图像生成
    EMBEDDING    // 嵌入模型
}

enum class Modality {
    TEXT,        // 文本
    IMAGE,       // 图像
    AUDIO,       // 音频
    VIDEO        // 视频
}

enum class ModelAbility {
    REASONING,   // 推理能力 (Deep Thinking)
    TOOL,        // 工具调用
    VISION,      // 视觉理解
    STREAMING    // 流式输出
}

/**
 * 推理级别 (Deep Thinking)
 */
enum class ReasoningLevel(
    val budgetTokens: Int,
    val effort: String
) {
    OFF(0, "minimal"),           // 关闭
    AUTO(-1, "auto"),            // 自动
    LOW(1024, "low"),            // 低 (1K tokens)
    MEDIUM(16_000, "medium"),    // 中 (16K tokens)
    HIGH(32_000, "high");        // 高 (32K tokens)

    val isEnabled: Boolean
        get() = this != OFF
}

/**
 * 提供商类型
 */
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
data class ProviderSetting(
    val id: String,
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
data class TextGenerationParams(
    val model: Model,
    val temperature: Float? = null,          // 0.0 - 2.0
    val topP: Float? = null,                 // 0.0 - 1.0
    val maxTokens: Int? = null,              // 最大输出 tokens
    val thinkingBudget: Int? = null,         // 思考 token 预算
    val tools: List<Tool> = emptyList(),     // 可用工具
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBody: List<CustomBody> = emptyList()
)

/**
 * 自定义请求头
 */
data class CustomHeader(
    val name: String,
    val value: String
)

/**
 * 自定义请求体
 */
data class CustomBody(
    val key: String,
    val value: Any
)

/**
 * 工具定义
 */
data class Tool(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

/**
 * 助手配置
 */
data class Assistant(
    val id: String,
    val name: String,
    val avatar: String? = null,
    val chatModelId: String? = null,         // 默认模型 (null 使用全局)
    val systemPrompt: String = "",           // 系统提示词
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxTokens: Int? = null,
    val thinkingBudget: Int? = 1024,         // 默认思考预算
    val streamOutput: Boolean = true,        // 是否流式输出
    val contextMessageSize: Int = 64,        // 上下文消息数量
    val enableMemory: Boolean = false,       // 启用长期记忆
    val presetMessages: List<Message> = emptyList(), // 预设消息
    val quickMessages: List<QuickMessage> = emptyList(), // 快捷消息
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBodies: List<CustomBody> = emptyList()
)

/**
 * 快捷消息
 */
data class QuickMessage(
    val title: String,
    val content: String
)
```

---

## 5. 伪代码 (Pseudocode)

### 5.1 核心服务接口

```kotlin
/**
 * 通用对话服务
 */
interface UniversalChatService {

    // ========== 对话管理 ==========

    /**
     * 创建新对话
     */
    suspend fun createConversation(
        assistantId: String,
        initialMessages: List<Message> = emptyList()
    ): Conversation

    /**
     * 加载对话
     */
    suspend fun loadConversation(conversationId: String): Conversation?

    /**
     * 获取对话流 (实时更新)
     */
    fun observeConversation(conversationId: String): Flow<Conversation>

    /**
     * 保存对话
     */
    suspend fun saveConversation(conversation: Conversation)

    /**
     * 删除对话
     */
    suspend fun deleteConversation(conversationId: String)

    /**
     * 搜索对话
     */
    fun searchConversations(
        query: String,
        assistantId: String? = null
    ): Flow<List<Conversation>>

    // ========== 消息操作 ==========

    /**
     * 发送消息 (核心方法)
     */
    suspend fun sendMessage(
        conversationId: String,
        content: List<MessagePart>,
        autoGenerate: Boolean = true
    ): Result<Message>

    /**
     * 生成 AI 响应 (流式)
     */
    fun generateResponseStream(
        conversationId: String,
        params: TextGenerationParams? = null
    ): Flow<GenerationChunk>

    /**
     * 生成 AI 响应 (非流式)
     */
    suspend fun generateResponse(
        conversationId: String,
        params: TextGenerationParams? = null
    ): Result<Message>

    /**
     * 取消生成
     */
    fun cancelGeneration(conversationId: String)

    /**
     * 重新生成消息
     */
    suspend fun regenerateMessage(
        conversationId: String,
        messageId: String
    ): Result<Message>

    /**
     * 编辑消息 (创建新分支)
     */
    suspend fun editMessage(
        conversationId: String,
        messageId: String,
        newContent: List<MessagePart>
    ): Result<Message>

    /**
     * 删除消息
     */
    suspend fun deleteMessage(
        conversationId: String,
        messageId: String
    )

    // ========== 高级功能 ==========

    /**
     * 自动生成标题
     */
    suspend fun generateTitle(conversationId: String): String

    /**
     * 生成对话建议
     */
    suspend fun generateSuggestions(conversationId: String): List<String>

    /**
     * 翻译消息
     */
    suspend fun translateMessage(
        messageId: String,
        targetLanguage: String
    ): String

    /**
     * 获取 Token 使用统计
     */
    fun getTokenUsage(conversationId: String): Flow<TokenUsage>
}

/**
 * 生成块 (流式输出)
 */
sealed class GenerationChunk {
    data class ThinkingChunk(val content: String) : GenerationChunk()
    data class ThinkingComplete(val totalContent: String) : GenerationChunk()
    data class ResponseChunk(val content: String) : GenerationChunk()
    data class ResponseComplete(
        val message: Message,
        val usage: TokenUsage
    ) : GenerationChunk()
    data class Error(val error: Throwable) : GenerationChunk()
}
```



### 5.2 核心实现逻辑

```kotlin
/**
 * 通用对话服务实现
 */
class UniversalChatServiceImpl(
    private val conversationRepository: ConversationRepository,
    private val providerManager: ProviderManager,
    private val fileManager: FileManager,
    private val settingsStore: SettingsStore
) : UniversalChatService {

    // 活跃对话缓存
    private val activeConversations = mutableMapOf<String, MutableStateFlow<Conversation>>()

    // 生成任务管理
    private val generationJobs = mutableMapOf<String, Job>()

    override suspend fun sendMessage(
        conversationId: String,
        content: List<MessagePart>,
        autoGenerate: Boolean
    ): Result<Message> = runCatching {
        // 1. 验证输入
        validateMessageContent(content)

        // 2. 处理文件上传 (复制到应用目录)
        val processedContent = processMessageContent(content)

        // 3. 创建用户消息
        val userMessage = Message(
            id = generateId(),
            role = MessageRole.USER,
            parts = processedContent,
            createdAt = Clock.System.now()
        )

        // 4. 添加到对话
        val conversation = getConversation(conversationId)
        val updatedConversation = conversation.addMessage(userMessage)
        updateConversation(conversationId, updatedConversation)

        // 5. 保存对话
        saveConversation(updatedConversation)

        // 6. 自动生成响应
        if (autoGenerate) {
            launchGeneration(conversationId)
        }

        userMessage
    }

    override fun generateResponseStream(
        conversationId: String,
        params: TextGenerationParams?
    ): Flow<GenerationChunk> = flow {
        // 1. 取消现有生成任务
        cancelGeneration(conversationId)

        // 2. 获取对话和设置
        val conversation = getConversation(conversationId)
        val assistant = settingsStore.getAssistant(conversation.assistantId)
        val model = params?.model ?: getDefaultModel(assistant)
        val provider = providerManager.getProvider(model.providerType)

        // 3. 准备消息列表
        val messages = prepareMessages(conversation, assistant)

        // 4. 构建生成参数
        val generationParams = buildGenerationParams(params, assistant, model)

        // 5. 创建初始 AI 消息
        var aiMessage = Message(
            id = generateId(),
            role = MessageRole.ASSISTANT,
            parts = emptyList(),
            createdAt = Clock.System.now(),
            modelId = model.id
        )

        // 6. 流式生成
        provider.streamText(
            messages = messages,
            params = generationParams
        ).collect { chunk ->
            // 处理消息块
            aiMessage = aiMessage.appendChunk(chunk)

            // 发送不同类型的块
            when {
                chunk.hasReasoning() -> {
                    emit(GenerationChunk.ThinkingChunk(chunk.getReasoningContent()))

                    if (chunk.isReasoningComplete()) {
                        emit(GenerationChunk.ThinkingComplete(aiMessage.getReasoningContent()))
                    }
                }
                chunk.hasText() -> {
                    emit(GenerationChunk.ResponseChunk(chunk.getTextContent()))
                }
            }

            // 更新对话
            updateConversationWithMessage(conversationId, aiMessage)

            // 处理完成
            if (chunk.isComplete()) {
                emit(GenerationChunk.ResponseComplete(
                    message = aiMessage,
                    usage = chunk.usage ?: TokenUsage()
                ))

                // 保存对话
                saveConversation(getConversation(conversationId))

                // 后台任务: 生成标题和建议
                launchBackgroundTasks(conversationId)
            }
        }
    }.catch { error ->
        emit(GenerationChunk.Error(error))
    }

    /**
     * 准备发送给模型的消息列表
     */
    private fun prepareMessages(
        conversation: Conversation,
        assistant: Assistant
    ): List<Message> {
        val messages = mutableListOf<Message>()

        // 1. 添加系统提示
        if (assistant.systemPrompt.isNotBlank()) {
            messages.add(Message(
                id = generateId(),
                role = MessageRole.SYSTEM,
                parts = listOf(MessagePart.Text(assistant.systemPrompt)),
                createdAt = Clock.System.now()
            ))
        }

        // 2. 添加预设消息
        messages.addAll(assistant.presetMessages)

        // 3. 添加对话历史 (限制数量)
        val historyMessages = conversation.getCurrentMessages()
            .takeLast(assistant.contextMessageSize)
            .filter { it.isValidForUpload() } // 过滤掉 Reasoning 等不需要上传的部分
        messages.addAll(historyMessages)

        return messages
    }

    /**
     * 构建生成参数
     */
    private fun buildGenerationParams(
        params: TextGenerationParams?,
        assistant: Assistant,
        model: Model
    ): TextGenerationParams {
        return TextGenerationParams(
            model = model,
            temperature = params?.temperature ?: assistant.temperature,
            topP = params?.topP ?: assistant.topP,
            maxTokens = params?.maxTokens ?: assistant.maxTokens,
            thinkingBudget = if (model.abilities.contains(ModelAbility.REASONING)) {
                params?.thinkingBudget ?: assistant.thinkingBudget
            } else {
                0 // 模型不支持推理，设为 0
            },
            tools = params?.tools ?: emptyList(),
            customHeaders = assistant.customHeaders + (params?.customHeaders ?: emptyList()),
            customBody = assistant.customBodies + (params?.customBody ?: emptyList())
        )
    }
}
```

### 5.3 提供商抽象层

```kotlin
/**
 * AI 提供商接口
 */
interface Provider {
    /**
     * 流式文本生成
     */
    fun streamText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Flow<MessageChunk>

    /**
     * 非流式文本生成
     */
    suspend fun generateText(
        messages: List<Message>,
        params: TextGenerationParams
    ): MessageChunk
}

/**
 * 消息块 (流式输出单元)
 */
data class MessageChunk(
    val id: String,
    val model: String,
    val choices: List<MessageChoice>,
    val usage: TokenUsage? = null
) {
    fun hasReasoning(): Boolean =
        choices.firstOrNull()?.delta?.parts?.any { it is MessagePart.Reasoning } == true

    fun hasText(): Boolean =
        choices.firstOrNull()?.delta?.parts?.any { it is MessagePart.Text } == true

    fun isReasoningComplete(): Boolean =
        choices.firstOrNull()?.delta?.parts
            ?.filterIsInstance<MessagePart.Reasoning>()
            ?.any { it.finishedAt != null } == true

    fun isComplete(): Boolean =
        choices.firstOrNull()?.finishReason != null

    fun getReasoningContent(): String =
        choices.firstOrNull()?.delta?.parts
            ?.filterIsInstance<MessagePart.Reasoning>()
            ?.joinToString("") { it.reasoning } ?: ""

    fun getTextContent(): String =
        choices.firstOrNull()?.delta?.parts
            ?.filterIsInstance<MessagePart.Text>()
            ?.joinToString("") { it.text } ?: ""
}

data class MessageChoice(
    val index: Int,
    val delta: Message?,          // 流式输出的增量
    val message: Message?,        // 非流式输出的完整消息
    val finishReason: String?     // "stop", "length", "tool_calls", etc.
)

/**
 * Qwen 提供商实现示例
 */
class QwenProvider(
    private val httpClient: HttpClient,
    private val setting: ProviderSetting
) : Provider {

    override fun streamText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Flow<MessageChunk> = flow {
        val request = buildRequest(messages, params, stream = true)

        httpClient.post(setting.baseUrl) {
            headers {
                append("Authorization", "Bearer ${setting.apiKey}")
                append("Content-Type", "application/json")
                params.customHeaders.forEach { header ->
                    append(header.name, header.value)
                }
            }
            setBody(request)
        }.bodyAsChannel().consumeAsFlow().collect { buffer ->
            // 解析 SSE 流
            val line = buffer.readText()
            if (line.startsWith("data: ")) {
                val json = line.removePrefix("data: ").trim()
                if (json != "[DONE]") {
                    val chunk = parseMessageChunk(json)
                    emit(chunk)
                }
            }
        }
    }

    override suspend fun generateText(
        messages: List<Message>,
        params: TextGenerationParams
    ): MessageChunk {
        val request = buildRequest(messages, params, stream = false)

        val response = httpClient.post(setting.baseUrl) {
            headers {
                append("Authorization", "Bearer ${setting.apiKey}")
                append("Content-Type", "application/json")
            }
            setBody(request)
        }

        return parseMessageChunk(response.bodyAsText())
    }

    private fun buildRequest(
        messages: List<Message>,
        params: TextGenerationParams,
        stream: Boolean
    ): Map<String, Any> {
        return buildMap {
            put("model", params.model.modelId)
            put("messages", messages.map { it.toApiFormat() })
            put("stream", stream)

            params.temperature?.let { put("temperature", it) }
            params.topP?.let { put("top_p", it) }
            params.maxTokens?.let { put("max_tokens", it) }

            // Deep Thinking 支持
            if (params.thinkingBudget != null && params.thinkingBudget > 0) {
                put("thinking", mapOf(
                    "budget_tokens" to params.thinkingBudget
                ))
            }

            // 工具调用
            if (params.tools.isNotEmpty()) {
                put("tools", params.tools.map { it.toApiFormat() })
            }

            // 自定义参数
            params.customBody.forEach { custom ->
                put(custom.key, custom.value)
            }
        }
    }
}
```

### 5.4 使用示例

```kotlin
// ========== 初始化 ==========

// 1. 配置提供商
val qwenProvider = ProviderSetting(
    id = "qwen-provider",
    type = ProviderType.QWEN,
    name = "Qwen",
    baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
    apiKey = "your-api-key",
    models = listOf(
        Model(
            id = "qwen-max-id",
            modelId = "qwen-max",
            displayName = "Qwen Max",
            type = ModelType.CHAT,
            inputModalities = listOf(Modality.TEXT, Modality.IMAGE),
            outputModalities = listOf(Modality.TEXT),
            abilities = listOf(
                ModelAbility.REASONING,
                ModelAbility.TOOL,
                ModelAbility.VISION,
                ModelAbility.STREAMING
            ),
            providerType = ProviderType.QWEN
        )
    )
)

// 2. 创建助手
val assistant = Assistant(
    id = "assistant-1",
    name = "AI 助手",
    systemPrompt = "你是一个有帮助的 AI 助手。",
    chatModelId = "qwen-max-id",
    temperature = 0.7f,
    thinkingBudget = 1024,  // 启用低级别深度思考
    streamOutput = true,
    contextMessageSize = 32
)

// 3. 初始化服务
val chatService = UniversalChatServiceImpl(
    conversationRepository = conversationRepository,
    providerManager = providerManager,
    fileManager = fileManager,
    settingsStore = settingsStore
)

// ========== 创建对话 ==========

val conversation = chatService.createConversation(
    assistantId = assistant.id
)

// ========== 发送文本消息 ==========

chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("什么是量子计算？")
    )
)

// ========== 发送多模态消息 ==========

chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("这张图片里有什么？"),
        MessagePart.Image(url = "file:///path/to/image.jpg")
    )
)

// ========== 发送文件 ==========

chatService.sendMessage(
    conversationId = conversation.id,
    content = listOf(
        MessagePart.Text("请分析这份文档"),
        MessagePart.Document(
            url = "file:///path/to/document.pdf",
            fileName = "report.pdf",
            mimeType = "application/pdf"
        )
    )
)

// ========== 流式生成响应 ==========

chatService.generateResponseStream(
    conversationId = conversation.id
).collect { chunk ->
    when (chunk) {
        is GenerationChunk.ThinkingChunk -> {
            // 显示思考过程
            println("思考中: ${chunk.content}")
        }
        is GenerationChunk.ThinkingComplete -> {
            // 思考完成
            println("思考完成: ${chunk.totalContent}")
        }
        is GenerationChunk.ResponseChunk -> {
            // 显示响应内容
            print(chunk.content)
        }
        is GenerationChunk.ResponseComplete -> {
            // 生成完成
            println("\n完成! Token 使用: ${chunk.usage}")
        }
        is GenerationChunk.Error -> {
            // 处理错误
            println("错误: ${chunk.error.message}")
        }
    }
}

// ========== 自定义深度思考级别 ==========

chatService.generateResponseStream(
    conversationId = conversation.id,
    params = TextGenerationParams(
        model = qwenModel,
        thinkingBudget = 32_000,  // 高级别深度思考
        temperature = 0.3f
    )
).collect { chunk ->
    // 处理响应...
}

// ========== 观察对话更新 ==========

chatService.observeConversation(conversation.id)
    .collect { updatedConversation ->
        // UI 自动更新
        updateUI(updatedConversation)
    }

// ========== 搜索对话 ==========

chatService.searchConversations(
    query = "量子计算",
    assistantId = assistant.id
).collect { conversations ->
    displaySearchResults(conversations)
}

// ========== 编辑消息 (创建分支) ==========

chatService.editMessage(
    conversationId = conversation.id,
    messageId = "message-id",
    newContent = listOf(
        MessagePart.Text("修改后的问题")
    )
)

// ========== 重新生成响应 ==========

chatService.regenerateMessage(
    conversationId = conversation.id,
    messageId = "assistant-message-id"
)
```


---

## 6. 架构图

### 6.1 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Chat Screen  │  │ Settings     │  │ History      │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
┌─────────────────────────────┼─────────────────────────────────┐
│                    Service Layer                              │
│                             │                                 │
│              ┌──────────────▼──────────────┐                  │
│              │ UniversalChatService        │                  │
│              │  - sendMessage()            │                  │
│              │  - generateResponseStream() │                  │
│              │  - observeConversation()    │                  │
│              └──────────┬──────────────────┘                  │
│                         │                                     │
│         ┌───────────────┼───────────────┐                     │
│         │               │               │                     │
│    ┌────▼────┐    ┌────▼────┐    ┌────▼────┐                │
│    │Provider │    │  File   │    │Settings │                │
│    │Manager  │    │ Manager │    │  Store  │                │
│    └────┬────┘    └─────────┘    └─────────┘                │
└─────────┼──────────────────────────────────────────────────┘
          │
┌─────────┼──────────────────────────────────────────────────┐
│         │           Provider Layer                          │
│    ┌────▼────┐    ┌──────────┐    ┌──────────┐            │
│    │  Qwen   │    │  OpenAI  │    │  Claude  │            │
│    │Provider │    │ Provider │    │ Provider │            │
│    └────┬────┘    └────┬─────┘    └────┬─────┘            │
└─────────┼──────────────┼───────────────┼──────────────────┘
          │              │               │
          └──────────────┼───────────────┘
                         │
┌─────────────────────────┼─────────────────────────────────┐
│                  Data Layer                                │
│              ┌──────────▼──────────────┐                   │
│              │ ConversationRepository  │                   │
│              │  - save()               │                   │
│              │  - load()               │                   │
│              │  - search()             │                   │
│              └──────────┬──────────────┘                   │
│                         │                                  │
│              ┌──────────▼──────────────┐                   │
│              │   Room Database         │                   │
│              │  - ConversationEntity   │                   │
│              │  - MessageEntity        │                   │
│              └─────────────────────────┘                   │
└────────────────────────────────────────────────────────────┘
```

### 6.2 深度思考流程图

```
用户发送消息
    │
    ▼
验证输入 (文件类型、大小)
    │
    ▼
处理附件 (复制到本地)
    │
    ▼
创建用户消息
    │
    ▼
保存到对话
    │
    ▼
检查模型能力
    │
    ├─ 支持推理 ──────────┐
    │                     │
    │                     ▼
    │              ┌──────────────┐
    │              │ Thinking     │
    │              │ Phase        │
    │              │              │
    │              │ - 内部推理   │
    │              │ - 流式输出   │
    │              │ - Token预算  │
    │              └──────┬───────┘
    │                     │
    │                     ▼
    │              思考完成标记
    │                     │
    └─────────────────────┘
                          │
                          ▼
                   ┌──────────────┐
                   │ Response     │
                   │ Phase        │
                   │              │
                   │ - 最终答案   │
                   │ - 流式输出   │
                   └──────┬───────┘
                          │
                          ▼
                   合并 Token 统计
                          │
                          ▼
                   保存完整消息
                          │
                          ▼
                   后台任务
                   ├─ 生成标题
                   └─ 生成建议
```

### 6.3 消息流转图

```
┌─────────────┐
│ User Input  │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────┐
│ MessagePart[]               │
│ - Text                      │
│ - Image                     │
│ - Document                  │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Message (USER)              │
│ - id                        │
│ - role: USER                │
│ - parts: MessagePart[]      │
│ - createdAt                 │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Conversation                │
│ - messageNodes[]            │
│   └─ MessageNode            │
│      └─ messages[]          │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Provider API                │
│ - Convert to API format     │
│ - Add system prompt         │
│ - Add context messages      │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ AI Model                    │
│ - Process request           │
│ - Generate response         │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ MessageChunk (Stream)       │
│ - delta: Message            │
│ - usage: TokenUsage         │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Message (ASSISTANT)         │
│ - id                        │
│ - role: ASSISTANT           │
│ - parts:                    │
│   ├─ Reasoning (optional)   │
│   └─ Text                   │
│ - usage: TokenUsage         │
│ - modelId                   │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Update Conversation         │
│ - Append to messageNodes    │
│ - Update metadata           │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Persist to Database         │
└─────────────────────────────┘
```

---

## 7. 实现要点

### 7.1 深度思考 (Deep Thinking) 实现

**关键特性**:
1. **两阶段输出**: Thinking Phase → Response Phase
2. **Token 预算控制**: 通过 `thinkingBudget` 参数控制思考深度
3. **可视化**: `MessagePart.Reasoning` 包含 `createdAt` 和 `finishedAt` 时间戳
4. **流式展示**: 思考过程可以实时流式输出
5. **模型能力检测**: 自动检测模型是否支持推理功能

**实现细节**:
```kotlin
// 1. 检查模型能力
if (model.abilities.contains(ModelAbility.REASONING)) {
    params.thinkingBudget = assistant.thinkingBudget
}

// 2. API 请求中包含思考预算
{
    "model": "qwen-max",
    "thinking": {
        "budget_tokens": 1024
    }
}

// 3. 解析响应中的推理部分
MessagePart.Reasoning(
    reasoning = "思考内容...",
    createdAt = Instant.now(),
    finishedAt = null  // 进行中
)

// 4. 完成后标记
MessagePart.Reasoning(
    reasoning = "完整思考内容",
    createdAt = startTime,
    finishedAt = Instant.now()  // 已完成
)
```

### 7.2 多模态输入处理

**支持的输入类型**:
- 文本 (Text)
- 图片 (Image): JPG, PNG, WebP
- 文档 (Document): PDF, DOCX, TXT, Markdown, 代码文件

**处理流程**:
1. **验证**: 检查文件类型和大小
2. **复制**: 将文件复制到应用私有目录
3. **转换**: 转换为 MessagePart 对象
4. **上传**: 根据提供商要求转换格式 (Base64/URL)

### 7.3 对话分支管理

**MessageNode 结构**:
```kotlin
MessageNode(
    id = "node-1",
    messages = [
        Message(...),  // 版本 1
        Message(...),  // 版本 2 (编辑后)
    ],
    currentIndex = 1,  // 当前选中版本 2
    parentId = "node-0",
    childrenIds = ["node-2", "node-3"]  // 两个分支
)
```

**编辑消息流程**:
1. 找到目标 MessageNode
2. 创建新的 Message 版本
3. 添加到 messages 列表
4. 更新 currentIndex
5. 从该节点重新生成后续对话

### 7.4 Token 使用统计

**统计维度**:
- `promptTokens`: 输入 tokens
- `completionTokens`: 输出 tokens (包含思考 tokens)
- `cachedTokens`: 缓存 tokens (提示词缓存)
- `totalTokens`: 总计

**合并逻辑**:
```kotlin
fun TokenUsage?.merge(other: TokenUsage): TokenUsage {
    return TokenUsage(
        promptTokens = other.promptTokens.takeIf { it > 0 } ?: this?.promptTokens ?: 0,
        completionTokens = other.completionTokens.takeIf { it > 0 } ?: this?.completionTokens ?: 0,
        cachedTokens = other.cachedTokens.takeIf { it > 0 } ?: this?.cachedTokens ?: 0,
        totalTokens = promptTokens + completionTokens
    )
}
```

### 7.5 流式响应处理

**SSE (Server-Sent Events) 解析**:
```kotlin
flow {
    response.bodyAsChannel().consumeAsFlow().collect { buffer ->
        val line = buffer.readText()
        if (line.startsWith("data: ")) {
            val json = line.removePrefix("data: ").trim()
            if (json != "[DONE]") {
                val chunk = parseMessageChunk(json)
                emit(chunk)
            }
        }
    }
}
```

**增量合并**:
```kotlin
var aiMessage = Message(...)
chunks.collect { chunk ->
    aiMessage = aiMessage.appendChunk(chunk)
    emit(aiMessage)
}
```

---

## 8. 数据持久化

### 8.1 数据库 Schema

```kotlin
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val assistantId: String,
    val title: String,
    val nodes: String,  // JSON: List<MessageNode>
    val truncateIndex: Int,
    val chatSuggestions: String,  // JSON: List<String>
    val isPinned: Boolean,
    val createAt: Long,
    val updateAt: Long
)

@Entity(tableName = "assistants")
data class AssistantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatar: String?,
    val chatModelId: String?,
    val systemPrompt: String,
    val temperature: Float?,
    val topP: Float?,
    val maxTokens: Int?,
    val thinkingBudget: Int?,
    val streamOutput: Boolean,
    val contextMessageSize: Int
)
```

### 8.2 JSON 序列化

使用 `kotlinx.serialization`:
```kotlin
@Serializable
data class Message(...)

// 序列化
val json = Json.encodeToString(message)

// 反序列化
val message = Json.decodeFromString<Message>(json)
```

---

## 9. 总结

### 9.1 核心优势

1. **通用性**: 支持多种 AI 提供商 (Qwen, OpenAI, Claude, Google)
2. **多模态**: 统一处理文本、图片、文档输入
3. **深度思考**: 内置推理模式，支持可视化思考过程
4. **流式响应**: 实时流式输出，提升用户体验
5. **对话分支**: 支持编辑历史消息并创建新分支
6. **完整记录**: 结构化 JSON 存储，包含完整元数据
7. **可扩展**: 清晰的抽象层，易于添加新提供商

### 9.2 技术栈

- **语言**: Kotlin
- **异步**: Coroutines + Flow
- **序列化**: kotlinx.serialization
- **网络**: Ktor Client
- **数据库**: Room
- **依赖注入**: Koin (可选)

### 9.3 适用场景

- Android 应用
- Kotlin Multiplatform (KMP) 项目
- 桌面应用 (Compose Desktop)
- 服务端应用 (Ktor Server)

### 9.4 后续扩展方向

1. **语音输入/输出**: 添加 Audio modality
2. **视频理解**: 添加 Video modality
3. **实时对话**: WebSocket 支持
4. **协作对话**: 多用户共享对话
5. **插件系统**: 支持自定义工具和扩展
6. **云同步**: 跨设备对话同步
7. **导出功能**: 导出为 Markdown/PDF/HTML

---

## 附录

### A. 参考资源

- **rikkahub 项目**: 原始参考实现
- **OpenAI API**: https://platform.openai.com/docs/api-reference
- **Qwen API**: https://help.aliyun.com/zh/dashscope/
- **Claude API**: https://docs.anthropic.com/claude/reference

### B. 许可证

本设计文档基于 rikkahub 项目提取，遵循原项目许可证。

### C. 版本历史

- **v1.0** (2025-11-09): 初始版本
  - 完整的功能清单
  - 状态流和事件模型
  - 数据结构定义
  - 伪代码实现
  - 深度思考模式支持
  - 多模态输入支持

---

**文档结束**

