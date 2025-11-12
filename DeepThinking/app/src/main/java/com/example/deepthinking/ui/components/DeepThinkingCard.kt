package com.example.deepthinking.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deepthinking.data.model.UIMessagePart
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * Deep thinking card component
 * Displays reasoning content with expandable states
 */
@Composable
fun DeepThinkingCard(
    reasoning: UIMessagePart.Reasoning,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var expandState by remember { mutableStateOf(ReasoningCardState.Collapsed) }
    val scrollState = rememberScrollState()

    // Auto-expand to Preview when loading
    LaunchedEffect(reasoning.reasoning, isLoading) {
        if (isLoading) {
            if (expandState == ReasoningCardState.Collapsed) {
                expandState = ReasoningCardState.Preview
            }
            // Auto-scroll to bottom
            scrollState.animateScrollTo(scrollState.maxValue)
        } else {
            // Auto-collapse when finished (optional)
            if (expandState == ReasoningCardState.Preview) {
                expandState = ReasoningCardState.Collapsed
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                expandState = when (expandState) {
                    ReasoningCardState.Collapsed -> ReasoningCardState.Expanded
                    ReasoningCardState.Preview -> ReasoningCardState.Expanded
                    ReasoningCardState.Expanded -> ReasoningCardState.Collapsed
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💭 Deep Thinking",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.weight(1f))

                // Character count
                Text(
                    text = "${reasoning.reasoning.length} chars",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Duration
                val duration = if (reasoning.finishedAt != null) {
                    (reasoning.finishedAt.toEpochMilliseconds() - reasoning.createdAt.toEpochMilliseconds()).milliseconds
                } else {
                    (Clock.System.now().toEpochMilliseconds() - reasoning.createdAt.toEpochMilliseconds()).milliseconds
                }
                Text(
                    text = "${duration.inWholeSeconds}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Expand icon
                Icon(
                    imageVector = if (expandState == ReasoningCardState.Expanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = "Expand",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Content
            AnimatedVisibility(
                visible = expandState != ReasoningCardState.Collapsed,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                when (expandState) {
                                    ReasoningCardState.Preview -> 100.dp
                                    ReasoningCardState.Expanded -> 300.dp
                                    else -> 0.dp
                                }
                            )
                            .verticalScroll(scrollState)
                    ) {
                        // Shimmer effect when loading
                        if (isLoading) {
                            ShimmerEffect(
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Text(
                            text = reasoning.reasoning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reasoning card states
 */
enum class ReasoningCardState {
    Collapsed,
    Preview,
    Expanded
}

/**
 * Shimmer loading effect
 */
@Composable
fun ShimmerEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0f),
        Color.White.copy(alpha = alpha),
        Color.White.copy(alpha = 0f)
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .alpha(0.3f)
    )
}

