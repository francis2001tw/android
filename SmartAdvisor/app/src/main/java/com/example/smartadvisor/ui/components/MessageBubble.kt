package com.example.smartadvisor.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartadvisor.model.Message
import com.example.smartadvisor.model.MessagePart
import com.example.smartadvisor.model.MessageRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start

    // Extract reasoning parts - DON'T use remember, we want it to update every time
    val reasoningParts = message.parts.filterIsInstance<MessagePart.Reasoning>()

    // Debug: Log when MessageBubble recomposes (只記錄 AI 消息)
    if (!isUser && reasoningParts.isNotEmpty()) {
        val reasoningLength = reasoningParts.firstOrNull()?.reasoning?.length ?: 0
        android.util.Log.d("MessageBubble", "💬 Rendering AI message, reasoning=$reasoningLength chars")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,  // Always start for full width
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First, display Reasoning parts (full width, outside bubble)
        reasoningParts.forEachIndexed { index, reasoning ->
            // Use key to force recomposition when reasoning content changes
            // Include reasoning length in key to ensure updates trigger recomposition
            key(message.id, index, reasoning.reasoning.length, reasoning.reasoning.hashCode(), reasoning.finishedAt) {
                DeepThinkingCard(
                    reasoning = reasoning,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Then, display Text parts in message bubble
        val textParts = message.parts.filterIsInstance<MessagePart.Text>()
        if (textParts.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(alignment)  // Align bubble based on user/assistant
                    .fillMaxWidth(if (isUser) 0.85f else 0.95f)  // 更宽的显示区域
                    .animateContentSize(),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant  // 更柔和的背景色
                },
                tonalElevation = 1.dp
            ) {
                SelectionContainer {  // 允许选择和复制文本
                    Column(
                        modifier = Modifier.padding(16.dp),  // 更大的内边距
                        verticalArrangement = Arrangement.spacedBy(12.dp)  // 更大的间距
                    ) {
                        textParts.forEach { part ->
                            Text(
                                text = part.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5  // 增加行高
                                ),
                                color = if (isUser) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface  // 更清晰的文本颜色
                                }
                            )
                        }

                        // Token usage info
                        message.usage?.let { usage ->
                            if (usage.totalTokens > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📊",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = "Tokens: ${usage.totalTokens} (${usage.promptTokens}+${usage.completionTokens})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Shimmer effect modifier
fun Modifier.shimmer(isLoading: Boolean): Modifier {
    if (!isLoading) return this

    return this.drawWithCache {
        val shimmerWidth = size.width * 2
        val animationSpec = infiniteRepeatable<Float>(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )

        onDrawWithContent {
            drawContent()
            val progress = (System.currentTimeMillis() % 1000) / 1000f
            val offset = -shimmerWidth + (shimmerWidth * 2 * progress)

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    start = Offset(offset, 0f),
                    end = Offset(offset + shimmerWidth, 0f)
                ),
                size = size
            )
        }
    }
}

// Reasoning card states
enum class ReasoningCardState(val expanded: Boolean) {
    Collapsed(false),
    Preview(true),
    Expanded(true),
}

@Composable
fun DeepThinkingCard(
    reasoning: MessagePart.Reasoning,
    modifier: Modifier = Modifier,
    fadeHeight: Float = 64f
) {
    var expandState by remember { mutableStateOf(ReasoningCardState.Preview) }
    val scrollState = rememberScrollState()
    val loading = reasoning.finishedAt == null

    // Debug: Log reasoning content length EVERY time this composable is called
    android.util.Log.d("DeepThinkingCard", "🧠 DeepThinkingCard recompose: ${reasoning.reasoning.length} chars, loading=$loading")

    // Auto-expand when loading, auto-scroll to bottom
    // 使用 LaunchedEffect 觀察 reasoning.reasoning 的變化（與 rikkahub 一致）
    LaunchedEffect(reasoning.reasoning, loading) {
        if (loading) {
            if (!expandState.expanded) {
                expandState = ReasoningCardState.Preview
            }
            // 滾動到底部，確保新內容可見 - 使用 delay 确保布局完成
            kotlinx.coroutines.delay(50)
            scrollState.animateScrollTo(scrollState.maxValue)
        } else {
            // Auto-collapse when finished (you can change this behavior)
            if (expandState == ReasoningCardState.Preview) {
                expandState = ReasoningCardState.Collapsed
            }
        }
    }

    // Calculate and update thinking duration
    var duration by remember(reasoning.finishedAt, reasoning.createdAt) {
        mutableStateOf(
            value = reasoning.finishedAt?.let { endTime ->
                endTime - reasoning.createdAt
            } ?: (Clock.System.now() - reasoning.createdAt)
        )
    }

    // Update duration in real-time when loading
    LaunchedEffect(loading) {
        if (loading) {
            while (isActive) {
                duration = (reasoning.finishedAt ?: Clock.System.now()) - reasoning.createdAt
                delay(50)
            }
        }
    }

    fun toggle() {
        expandState = if (loading) {
            if (expandState == ReasoningCardState.Expanded) ReasoningCardState.Preview else ReasoningCardState.Expanded
        } else {
            if (expandState == ReasoningCardState.Expanded) ReasoningCardState.Collapsed else ReasoningCardState.Expanded
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .fillMaxWidth()
                    .clickable { toggle() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thinking icon with animation
                Text(
                    text = if (loading) "🧠" else "✅",
                    style = MaterialTheme.typography.titleMedium
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deep Thinking",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.shimmer(loading)
                        )

                        if (duration > 0.seconds) {
                            Text(
                                text = "${duration.toString(DurationUnit.SECONDS, 1)}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.shimmer(loading)
                            )
                        }
                    }

                    // Progress indicator: character count or waiting message
                    if (loading) {
                        if (reasoning.reasoning.isEmpty()) {
                            Text(
                                text = "Waiting for response...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.shimmer(true)
                            )
                        } else {
                            Text(
                                text = "${reasoning.reasoning.length} characters generated...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    } else if (reasoning.reasoning.isNotEmpty()) {
                        Text(
                            text = "Completed • ${reasoning.reasoning.length} characters",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = when (expandState) {
                        ReasoningCardState.Collapsed -> Icons.Default.KeyboardArrowDown
                        ReasoningCardState.Expanded -> Icons.Default.KeyboardArrowUp
                        ReasoningCardState.Preview -> Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // Loading progress bar
            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Thinking content (expandable)
            // 在第一個回傳數據之前顯示 "Waiting for response..."，收到數據後顯示內容
            if (expandState.expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // 使用 key 確保內容變化時重組
                key(reasoning.reasoning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let {
                                if (expandState == ReasoningCardState.Preview) {
                                    it
                                        .graphicsLayer { alpha = 0.99f }
                                        .drawWithCache {
                                            val brush = Brush.verticalGradient(
                                                startY = 0f,
                                                endY = size.height,
                                                colorStops = arrayOf(
                                                    0.0f to Color.Transparent,
                                                    (fadeHeight / size.height) to Color.Black,
                                                    (1 - fadeHeight / size.height) to Color.Black,
                                                    1.0f to Color.Transparent
                                                )
                                            )
                                            onDrawWithContent {
                                                drawContent()
                                                drawRect(
                                                    brush = brush,
                                                    size = Size(size.width, size.height),
                                                    blendMode = BlendMode.DstIn
                                                )
                                            }
                                        }
                                        .heightIn(max = 150.dp)
                                        .verticalScroll(scrollState)
                                } else {
                                    it.verticalScroll(scrollState)
                                        .heightIn(max = 400.dp)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        if (reasoning.reasoning.isEmpty() && loading) {
                            // 在第一個回傳數據之前顯示 "Waiting for response..."
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Waiting for response...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // 顯示實時累加的推理內容
                            SelectionContainer {
                                Text(
                                    text = reasoning.reasoning,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

