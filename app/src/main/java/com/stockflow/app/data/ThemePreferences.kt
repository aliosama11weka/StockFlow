package com.stockflow.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The three theme choices offered on the Settings screen. */
enum class AppTheme(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Stores the selected theme with DataStore (a small key-value store).
 * Product data lives in Room; this file only holds the theme setting.
 */
class ThemePreferences(private val context: Context) {

    private val themeKey = stringPreferencesKey("app_theme")

    val theme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val savedName = preferences[themeKey]
        AppTheme.entries.firstOrNull { it.name == savedName } ?: AppTheme.SYSTEM
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }
}
