package com.localaiassistant.domain

import com.localaiassistant.data.ChatMemoryDao
import com.localaiassistant.data.ChatMemoryEntity
import com.localaiassistant.security.CryptoManager

class MemoryRepository(
    private val chatMemoryDao: ChatMemoryDao,
    private val cryptoManager: CryptoManager
) {
    suspend fun store(role: String, text: String, metadata: String, vector: List<Float>) {
        chatMemoryDao.insert(
            ChatMemoryEntity(
                role = role,
                encryptedText = cryptoManager.encrypt(text),
                metadata = metadata,
                timestamp = System.currentTimeMillis(),
                vector = vector.joinToString(",")
            )
        )
    }

    suspend fun recent(limit: Int = 20): List<Pair<String, String>> {
        return chatMemoryDao.recent(limit).map {
            it.role to cryptoManager.decrypt(it.encryptedText)
        }
    }
}
