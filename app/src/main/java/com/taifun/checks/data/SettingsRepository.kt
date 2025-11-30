package com.taifun.checks.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
    private val KEY_ICAO_MAX_DISTANCE_KM = floatPreferencesKey("icao_max_distance_km")
    private val KEY_ICAO_MAX_ALTITUDE_DIFF_M = floatPreferencesKey("icao_max_altitude_diff_m")

    // Bluetooth GPS settings
    private val KEY_GPS_SOURCE = stringPreferencesKey("gps_source")
    private val KEY_BT_GPS_DEVICE_NAME = stringPreferencesKey("bt_gps_device_name")
    private val KEY_BT_GPS_DEVICE_ADDRESS = stringPreferencesKey("bt_gps_device_address")
    private val KEY_BT_GPS_AUTO_CONNECT = booleanPreferencesKey("bt_gps_auto_connect")

    // SharedPreferences para acceso síncrono al idioma
    private val syncPrefs: SharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val darkFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_DARK] ?: true }
    val highContrastFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_CONTRAST] ?: false }
    val screenOnFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_SCREEN_ON] ?: false }
    val languageFlow: Flow<String> = ctx.settingsDataStore.data.map { it[KEY_LANGUAGE] ?: "auto" }
    val hapticsFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_HAPTICS] ?: true }
    val activeChecklistFlow: Flow<String> = ctx.settingsDataStore.data.map { it[KEY_ACTIVE_CHECKLIST] ?: "Taifun17E_ES.yaml" }
    val firstLaunchFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_FIRST_LAUNCH] ?: true }
    val icaoMaxDistanceKmFlow: Flow<Float> = ctx.settingsDataStore.data.map { it[KEY_ICAO_MAX_DISTANCE_KM] ?: 2.0f }
    val icaoMaxAltitudeDiffMFlow: Flow<Float> = ctx.settingsDataStore.data.map { it[KEY_ICAO_MAX_ALTITUDE_DIFF_M] ?: 50.0f }

    // Bluetooth GPS flows
    val gpsSourceFlow: Flow<String> = ctx.settingsDataStore.data.map { it[KEY_GPS_SOURCE] ?: "INTERNAL" }
    val btGpsDeviceNameFlow: Flow<String?> = ctx.settingsDataStore.data.map { it[KEY_BT_GPS_DEVICE_NAME] }
    val btGpsDeviceAddressFlow: Flow<String?> = ctx.settingsDataStore.data.map { it[KEY_BT_GPS_DEVICE_ADDRESS] }
    val btGpsAutoConnectFlow: Flow<Boolean> = ctx.settingsDataStore.data.map { it[KEY_BT_GPS_AUTO_CONNECT] ?: false }

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

    suspend fun setIcaoMaxDistanceKm(distanceKm: Float) {
        ctx.settingsDataStore.edit { it[KEY_ICAO_MAX_DISTANCE_KM] = distanceKm }
    }

    suspend fun setIcaoMaxAltitudeDiffM(altitudeDiffM: Float) {
        ctx.settingsDataStore.edit { it[KEY_ICAO_MAX_ALTITUDE_DIFF_M] = altitudeDiffM }
    }

    // Bluetooth GPS setters
    suspend fun setGpsSource(source: String) {
        ctx.settingsDataStore.edit { it[KEY_GPS_SOURCE] = source }
    }

    suspend fun setBtGpsDevice(name: String?, address: String?) {
        ctx.settingsDataStore.edit { prefs ->
            if (name != null) prefs[KEY_BT_GPS_DEVICE_NAME] = name else prefs.remove(KEY_BT_GPS_DEVICE_NAME)
            if (address != null) prefs[KEY_BT_GPS_DEVICE_ADDRESS] = address else prefs.remove(KEY_BT_GPS_DEVICE_ADDRESS)
        }
    }

    suspend fun setBtGpsAutoConnect(enabled: Boolean) {
        ctx.settingsDataStore.edit { it[KEY_BT_GPS_AUTO_CONNECT] = enabled }
    }
}
