package com.localaiassistant

import com.localaiassistant.model.EmbeddingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmbeddingUtilsTest {
    @Test
    fun embedding_has_requested_dimensions() {
        val vec = EmbeddingUtils.hashedEmbedding("hello private assistant", 32)
        assertEquals(32, vec.size)
    }

    @Test
    fun embedding_is_normalized() {
        val vec = EmbeddingUtils.hashedEmbedding("a b c")
        val norm = kotlin.math.sqrt(vec.sumOf { (it * it).toDouble() })
        assertTrue(norm > 0.99 && norm < 1.01)
    }
}
