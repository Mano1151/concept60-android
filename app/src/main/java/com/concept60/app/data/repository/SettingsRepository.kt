package com.concept60.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.concept60.app.data.model.AccessibilitySettings
import com.concept60.app.data.model.AppTheme
import com.concept60.app.data.model.FontSize
import com.concept60.app.data.model.ReadingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val KEY_FONT_SIZE = stringPreferencesKey("font_size")
    private val KEY_READING_MODE = stringPreferencesKey("reading_mode")
    private val KEY_PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
    private val KEY_LISTEN_MODE = booleanPreferencesKey("listen_mode")
    private val KEY_THEME = stringPreferencesKey("theme")

    val settings: Flow<AccessibilitySettings> = dataStore.data.map { prefs ->
        AccessibilitySettings(
            fontSize = prefs[KEY_FONT_SIZE]?.let { FontSize.valueOf(it) } ?: FontSize.MEDIUM,
            readingMode = prefs[KEY_READING_MODE]?.let { ReadingMode.valueOf(it) } ?: ReadingMode.NORMAL,
            playbackSpeed = prefs[KEY_PLAYBACK_SPEED] ?: 1.0f,
            listenMode = prefs[KEY_LISTEN_MODE] ?: false,
            theme = prefs[KEY_THEME]?.let { AppTheme.valueOf(it) } ?: AppTheme.DARK,
        )
    }

    suspend fun updateSettings(settings: AccessibilitySettings) {
        dataStore.edit { prefs ->
            prefs[KEY_FONT_SIZE] = settings.fontSize.name
            prefs[KEY_READING_MODE] = settings.readingMode.name
            prefs[KEY_PLAYBACK_SPEED] = settings.playbackSpeed
            prefs[KEY_LISTEN_MODE] = settings.listenMode
            prefs[KEY_THEME] = settings.theme.name
        }
    }
}
