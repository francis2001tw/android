package com.example.smartadvisor.provider

import com.example.smartadvisor.model.*
import kotlinx.coroutines.flow.Flow

/**
 * AI 提供商接口
 */
interface Provider {
    /**
     * 流式生成文本
     */
    fun streamText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Flow<MessageChunk>
    
    /**
     * 非流式生成文本
     */
    suspend fun generateText(
        messages: List<Message>,
        params: TextGenerationParams
    ): Message
}

