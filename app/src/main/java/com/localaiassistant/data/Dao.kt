package com.localaiassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatMemoryDao {
    @Insert
    suspend fun insert(memory: ChatMemoryEntity)

    @Query("SELECT * FROM chat_memory ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int = 40): List<ChatMemoryEntity>
}

@Dao
interface PermissionLogDao {
    @Insert
    suspend fun insert(log: PermissionLogEntity)

    @Query("SELECT * FROM permission_log ORDER BY timestamp DESC")
    suspend fun all(): List<PermissionLogEntity>
}

@Dao
interface PreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pref: UserPreferenceEntity)

    @Query("SELECT * FROM preferences WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): UserPreferenceEntity?
}
