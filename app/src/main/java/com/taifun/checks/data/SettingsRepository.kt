package com.taifun.checks.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "settings_prefs"
private const val PREFS_NAME = "settings_sync"  // SharedPreferences para acceso síncrono
private val Context.settingsDataStore by preferencesDataStore(name = DS_NAME)

/**
 * Ajustes simples: tema oscuro, alto contraste, pantalla siempre encendida, idioma, hápticos.
 * También gestiona el checklist activo y el estado de primer lanzamiento.
 */
class SettingsRepository(private val ctx: Context) {

    private val KEY_DARK = booleanPreferencesKey("dark_theme")
    private val KEY_CONTRAST = booleanPreferencesKey("high_contrast")
    private val KEY_SCREEN_ON = booleanPreferencesKey("screen_on")
    private val KEY_LANGUAGE = stringPreferencesKey("language")
    private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
    private val KEY_ACTIVE_CHECKLIST = stringPreferencesKey("active_checklist_file")
    private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")

    // SharedPreferences para acceso síncrono al idioma
    private val syncPrefs: SharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val darkFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_DARK] ?: true }
    val highContrastFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_CONTRAST] ?: false }
    val screenOnFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_SCREEN_ON] ?: false }
    val languageFlow: Flow<String> = ctx.settingsDataStore.data.map { it[KEY_LANGUAGE] ?: "auto" }
    val hapticsFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_HAPTICS] ?: true }
    val activeChecklistFlow: Flow<String> = ctx.settingsDataStore.data.map { it[KEY_ACTIVE_CHECKLIST] ?: "Taifun17E_ES.yaml" }
    val firstLaunchFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_FIRST_LAUNCH] ?: true }

    /**
     * Obtiene el idioma de forma síncrona (para usar en attachBaseContext)
     */
    fun getLanguageSync(): String {
        return syncPrefs.getString("language", "auto") ?: "auto"
    }

    /**
     * Obtiene el estado de primer lanzamiento de forma síncrona
     */
    fun getFirstLaunchSync(): Boolean {
        return syncPrefs.getBoolean("first_launch", true)
    }

    suspend fun setDark(enabled: Boolean) {
        ctx.settingsDataStore.edit { it[KEY_DARK] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        ctx.settingsDataStore.edit { it[KEY_CONTRAST] = enabled }
    }

    suspend fun setScreenOn(enabled: Boolean) {
        ctx.settingsDataStore.edit { it[KEY_SCREEN_ON] = enabled }
    }

    suspend fun setLanguage(language: String) {
        // Escribir en DataStore para flows
        ctx.settingsDataStore.edit { it[KEY_LANGUAGE] = language }
        // También escribir en SharedPreferences para acceso síncrono
        syncPrefs.edit().putString("language", language).apply()
    }

    suspend fun setHaptics(enabled: Boolean) {
        ctx.settingsDataStore.edit { it[KEY_HAPTICS] = enabled }
    }

    suspend fun setActiveChecklist(filename: String) {
        ctx.settingsDataStore.edit { it[KEY_ACTIVE_CHECKLIST] = filename }
    }

    suspend fun setFirstLaunchComplete() {
        // Escribir en DataStore para flows
        ctx.settingsDataStore.edit { it[KEY_FIRST_LAUNCH] = false }
        // También escribir en SharedPreferences para acceso síncrono
        syncPrefs.edit().putBoolean("first_launch", false).apply()
    }
}
