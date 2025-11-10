package com.example.smartadvisor.service

import com.example.smartadvisor.model.ProviderType
import com.example.smartadvisor.provider.Provider

/**
 * Provider Manager - 管理所有 AI 提供商
 */
class ProviderManager(
    private val providers: Map<ProviderType, Provider>
) {
    /**
     * 获取指定类型的提供商
     */
    fun getProvider(type: ProviderType): Provider {
        return providers[type] ?: throw IllegalArgumentException("Provider not found for type: $type")
    }
    
    /**
     * 检查提供商是否可用
     */
    fun hasProvider(type: ProviderType): Boolean {
        return providers.containsKey(type)
    }
    
    /**
     * 获取所有可用的提供商类型
     */
    fun getAvailableProviders(): Set<ProviderType> {
        return providers.keys
    }
}

