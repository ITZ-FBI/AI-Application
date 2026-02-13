package com.localaiassistant.domain

import com.localaiassistant.model.EmbeddingUtils

class LocalAssistantEngine(private val memoryRepository: MemoryRepository) {

    suspend fun respond(userMessage: String, persona: String): String {
        val vector = EmbeddingUtils.hashedEmbedding(userMessage)
        memoryRepository.store("user", userMessage, "{\"source\":\"chat\"}", vector)

        val context = memoryRepository.recent(8)
            .filter { it.first == "user" }
            .take(3)
            .joinToString(" | ") { it.second }

        val reply = buildString {
            append("[$persona mode] ")
            append("I understood your request. ")
            if (context.isNotBlank()) append("I remember related points: $context. ")
            append("Reasoning: I prioritized privacy, explicit permissions, and no automatic actions.")
        }

        memoryRepository.store("assistant", reply, "{\"reasoning\":true}", EmbeddingUtils.hashedEmbedding(reply))
        return reply
    }
}
