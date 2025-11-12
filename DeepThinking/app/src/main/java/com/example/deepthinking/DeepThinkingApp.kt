package com.example.deepthinking

import android.app.Application
import com.example.deepthinking.data.api.DeepSeekApi
import com.example.deepthinking.data.db.AppDatabase
import com.example.deepthinking.data.repository.ConversationRepository
import com.example.deepthinking.ui.chat.ChatViewModel

/**
 * Application class for dependency injection
 */
class DeepThinkingApp : Application() {
    
    lateinit var database: AppDatabase
        private set
    
    lateinit var deepSeekApi: DeepSeekApi
        private set
    
    lateinit var conversationRepository: ConversationRepository
        private set
    
    lateinit var chatViewModel: ChatViewModel
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database
        database = AppDatabase.getDatabase(this)
        
        // Initialize API
        deepSeekApi = DeepSeekApi(
            apiKey = "sk-9e0f6612f850465f9057ef5e0d0ce641",
            baseUrl = "https://api.deepseek.com"
        )
        
        // Initialize repository
        conversationRepository = ConversationRepository(
            conversationDao = database.conversationDao(),
            deepSeekApi = deepSeekApi
        )
        
        // Initialize ViewModel
        chatViewModel = ChatViewModel(conversationRepository)
    }
}

