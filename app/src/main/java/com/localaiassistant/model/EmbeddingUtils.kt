package com.localaiassistant.model

object EmbeddingUtils {
    fun hashedEmbedding(input: String, dimensions: Int = 64): List<Float> {
        val vec = MutableList(dimensions) { 0f }
        input.lowercase().split(" ").filter { it.isNotBlank() }.forEach { token ->
            val idx = token.hashCode().let { kotlin.math.abs(it % dimensions) }
            vec[idx] += 1f
        }
        val norm = kotlin.math.sqrt(vec.sumOf { (it * it).toDouble() }).toFloat().coerceAtLeast(1e-6f)
        return vec.map { it / norm }
    }
}
