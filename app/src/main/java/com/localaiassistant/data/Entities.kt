package com.localaiassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_memory")
data class ChatMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val encryptedText: String,
    val metadata: String,
    val timestamp: Long,
    val vector: String
)

@Entity(tableName = "permission_log")
data class PermissionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resource: String,
    val rationale: String,
    val decision: String,
    val timestamp: Long
)

@Entity(tableName = "preferences")
data class UserPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)
