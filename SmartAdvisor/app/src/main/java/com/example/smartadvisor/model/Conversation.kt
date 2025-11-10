package com.example.smartadvisor.model

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 对话实体
 */
@Serializable
data class Conversation(
    val id: String = uuid4().toString(),
    val assistantId: String,
    val title: String = "",
    val messageNodes: List<MessageNode> = emptyList(),
    val truncateIndex: Int = -1,
    val chatSuggestions: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val createAt: Instant = Clock.System.now(),
    val updateAt: Instant = Clock.System.now(),
    val metadata: ConversationMetadata = ConversationMetadata()
) {
    /**
     * 获取当前对话路径的所有消息
     */
    fun getCurrentMessages(): List<Message> {
        val messages = mutableListOf<Message>()
        var currentNode: MessageNode? = messageNodes.firstOrNull()
        
        while (currentNode != null) {
            messages.add(currentNode.currentMessage)
            
            val nextNodeId = currentNode.childrenIds.firstOrNull()
            currentNode = if (nextNodeId != null) {
                messageNodes.find { it.id == nextNodeId }
            } else {
                null
            }
        }
        
        return messages
    }
    
    /**
     * 添加消息到对话
     */
    fun addMessage(message: Message): Conversation {
        val newNode = MessageNode(
            id = uuid4().toString(),
            messages = listOf(message),
            currentIndex = 0,
            parentId = messageNodes.lastOrNull()?.id
        )
        
        val updatedNodes = if (messageNodes.isEmpty()) {
            listOf(newNode)
        } else {
            val lastNode = messageNodes.last()
            val updatedLastNode = lastNode.copy(
                childrenIds = lastNode.childrenIds + newNode.id
            )
            messageNodes.dropLast(1) + updatedLastNode + newNode
        }
        
        return copy(
            messageNodes = updatedNodes,
            updateAt = Clock.System.now()
        )
    }
    
    /**
     * 更新最后一条消息
     */
    fun updateLastMessage(message: Message): Conversation {
        if (messageNodes.isEmpty()) return this
        
        val lastNode = messageNodes.last()
        val updatedNode = lastNode.copy(
            messages = listOf(message)
        )
        
        return copy(
            messageNodes = messageNodes.dropLast(1) + updatedNode,
            updateAt = Clock.System.now()
        )
    }
}

/**
 * 对话元数据
 */
@Serializable
data class ConversationMetadata(
    val totalTokens: Int = 0,
    val messageCount: Int = 0,
    val modelIds: Set<String> = emptySet(),
    val tags: List<String> = emptyList()
)

/**
 * 消息节点 (支持分支)
 */
@Serializable
data class MessageNode(
    val id: String = uuid4().toString(),
    val messages: List<Message>,
    val currentIndex: Int = 0,
    val parentId: String? = null,
    val childrenIds: List<String> = emptyList()
) {
    val currentMessage: Message
        get() = messages[currentIndex]
    
    companion object {
        fun of(message: Message) = MessageNode(
            messages = listOf(message)
        )
    }
}

