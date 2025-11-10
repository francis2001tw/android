package com.example.smartadvisor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.smartadvisor.model.*
import com.example.smartadvisor.provider.DeepSeekProvider
import com.example.smartadvisor.repository.InMemoryConversationRepository
import com.example.smartadvisor.service.GenerationChunk
import com.example.smartadvisor.service.ProviderManager
import com.example.smartadvisor.service.UniversalChatServiceImpl
import com.example.smartadvisor.storage.InMemorySettingsStore
import com.example.smartadvisor.ui.theme.SmartAdvisorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartAdvisorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestMainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestMainScreen() {
    var logs by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    fun log(message: String) {
        logs += "$message\n"
    }
    
    LaunchedEffect(logs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部标题
        TopAppBar(
            title = { Text("测试 Main.kt 功能") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 状态卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isRunning) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isRunning) "🔄 测试运行中..." else "⏸️ 等待测试",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 日志输出
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "📋 测试日志",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = logs.ifEmpty { "点击下方按钮开始测试..." },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 测试按钮
        Button(
            onClick = {
                if (!isRunning) {
                    isRunning = true
                    logs = ""
                    scope.launch {
                        try {
                            runTest(::log)
                            isRunning = false
                        } catch (e: Exception) {
                            log("❌ 测试失败: ${e.message}")
                            e.printStackTrace()
                            isRunning = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        ) {
            Text(if (isRunning) "测试运行中..." else "开始测试")
        }
    }
}

suspend fun runTest(log: (String) -> Unit) {
    log("=".repeat(60))
    log("QWEN API 连接测试")
    log("=".repeat(60))
    log("")

    try {
        // 1. 配置 DeepSeek 提供商
        log("📦 配置 DeepSeek 提供商...")
        log("   API Key: sk-9e0f6612f850465f9057ef5e0d0ce641")
        log("   Base URL: https://api.deepseek.com")
        log("")

        val deepseekSetting = ProviderSetting(
            type = ProviderType.DEEPSEEK,
            name = "DeepSeek",
            baseUrl = "https://api.deepseek.com",
            apiKey = "sk-9e0f6612f850465f9057ef5e0d0ce641"
        )

        val deepseekProvider = DeepSeekProvider(deepseekSetting)
        log("✅ Provider 创建成功!")
        log("")

        // 2. 创建测试模型
        log("📦 创建测试模型...")
        val testModel = Model(
            modelId = "deepseek-chat",
            displayName = "DeepSeek Chat",
            providerType = ProviderType.DEEPSEEK,
            abilities = listOf(ModelAbility.STREAMING, ModelAbility.REASONING)
        )
        log("✅ 模型创建成功: ${testModel.displayName}")
        log("")

        // 3. 准备测试消息
        log("=".repeat(60))
        log("测试问题: how to improve brain health?")
        log("=".repeat(60))
        log("")

        val testMessages = listOf(
            Message(
                role = MessageRole.USER,
                parts = listOf(MessagePart.Text("how to improve brain health?"))
            )
        )

        // 4. 创建生成参数
        val params = TextGenerationParams(
            model = testModel,
            temperature = 0.7f,
            topP = 0.9f,
            maxTokens = 2000,
            thinkingBudget = null  // 不启用 Deep Thinking，先测试基本功能
        )

        log("📤 发送请求到 QWEN API...")
        log("")

        // 5. 流式生成
        var responseText = ""
        var hasThinking = false
        var thinkingContent = ""
        var chunkCount = 0

        log("🤖 AI 响应:")
        log("")

        deepseekProvider.streamText(testMessages, params).collect { chunk ->
            chunkCount++
            log("📦 收到 Chunk #$chunkCount")

            chunk.choices.firstOrNull()?.delta?.parts?.forEach { part ->
                when (part) {
                    is MessagePart.Reasoning -> {
                        if (!hasThinking) {
                            log("🧠 [Deep Thinking 开始]")
                            hasThinking = true
                        }
                        thinkingContent += part.reasoning
                        log("   思考内容: ${part.reasoning.take(50)}...")
                    }
                    is MessagePart.Text -> {
                        responseText += part.text
                        log("   文本内容: ${part.text}")
                    }
                    else -> {
                        log("   其他类型: ${part::class.simpleName}")
                    }
                }
            }

            // 检查是否完成
            chunk.choices.firstOrNull()?.finishReason?.let { reason ->
                log("")
                log("✅ 生成完成! 原因: $reason")
                log("")
                log("📊 完整响应:")
                log("   $responseText")
                log("")

                chunk.usage?.let { usage ->
                    log("📊 Token 使用:")
                    log("   - Prompt Tokens: ${usage.promptTokens}")
                    log("   - Completion Tokens: ${usage.completionTokens}")
                    log("   - Total Tokens: ${usage.totalTokens}")
                }
            }
        }

        log("")
        log("=".repeat(60))
        log("✅ 测试完成!")
        log("   总共收到 $chunkCount 个数据块")
        log("   响应长度: ${responseText.length} 字符")
        if (hasThinking) {
            log("   🎉 检测到 Deep Thinking!")
        }
        log("=".repeat(60))

    } catch (e: Exception) {
        log("")
        log("❌ 测试失败!")
        log("错误类型: ${e::class.simpleName}")
        log("错误消息: ${e.message}")
        log("")
        log("详细堆栈:")
        e.printStackTrace()
        val stackTrace = e.stackTraceToString()
        stackTrace.lines().take(20).forEach { line ->
            log("   $line")
        }
    }
}

