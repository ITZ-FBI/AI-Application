package com.localaiassistant.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChatMemoryEntity::class, PermissionLogEntity::class, UserPreferenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMemoryDao(): ChatMemoryDao
    abstract fun permissionLogDao(): PermissionLogDao
    abstract fun preferencesDao(): PreferencesDao
}
