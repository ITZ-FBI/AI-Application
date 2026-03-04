package com.callflow.dialer.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "theme")

class ThemeManager(private val context: Context) {
    private val key = stringPreferencesKey("theme_mode")

    fun themeFlow(): Flow<String> = context.dataStore.data.map { it[key] ?: SYSTEM }

    fun applyTheme(mode: String = runBlocking { context.dataStore.data.map { it[key] ?: SYSTEM }.first() }) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    suspend fun setTheme(mode: String) {
        context.dataStore.edit { it[key] = mode }
        applyTheme(mode)
    }

    companion object {
        const val LIGHT = "light"
        const val DARK = "dark"
        const val SYSTEM = "system"
    }
}
