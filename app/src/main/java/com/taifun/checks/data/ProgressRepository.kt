package com.taifun.checks.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "progress")

/**
 * Repositorio para persistir el progreso de checklists
 */
class ProgressRepository(private val context: Context) {

    private fun indexKey(checklistId: String) = intPreferencesKey("idx_$checklistId")
    private fun pageKey(checklistId: String) = intPreferencesKey("page_$checklistId")
    private fun checkedKey(checklistId: String) = stringPreferencesKey("checked_$checklistId")
    private fun fullListKey(checklistId: String) = booleanPreferencesKey("fullList_$checklistId")
    private fun voiceControlKey(checklistId: String) = booleanPreferencesKey("voice_$checklistId")

    /** Flujo con el índice actual del paso (0 por defecto) - para modo step-by-step. */
    fun indexFlow(checklistId: String): Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[indexKey(checklistId)] ?: 0 }

    /** Guarda índice del paso actual. */
    suspend fun setIndex(checklistId: String, index: Int) {
        context.dataStore.edit { it[indexKey(checklistId)] = index }
    }

    /** Flujo con la página actual (0 por defecto) - para modo full-list. */
    fun pageFlow(checklistId: String): Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[pageKey(checklistId)] ?: 0 }

    /** Guarda página actual. */
    suspend fun setPage(checklistId: String, page: Int) {
        context.dataStore.edit { it[pageKey(checklistId)] = page }
    }

    /** Flujo con los items checkeados - para modo full-list. */
    fun checkedFlow(checklistId: String): Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val str = prefs[checkedKey(checklistId)] ?: ""
            if (str.isBlank()) emptySet()
            else str.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }

    /** Guarda items checkeados. */
    suspend fun setChecked(checklistId: String, checked: Set<Int>) {
        context.dataStore.edit {
            if (checked.isEmpty()) {
                it.remove(checkedKey(checklistId))
            } else {
                it[checkedKey(checklistId)] = checked.sorted().joinToString(",")
            }
        }
    }

    /** Flujo con la preferencia de modo full-list (null por defecto = usar configuración del checklist). */
    fun fullListFlow(checklistId: String): Flow<Boolean?> =
        context.dataStore.data.map { prefs ->
            if (prefs.contains(fullListKey(checklistId))) {
                prefs[fullListKey(checklistId)]
            } else null
        }

    /** Guarda preferencia de modo full-list. */
    suspend fun setFullList(checklistId: String, fullList: Boolean?) {
        context.dataStore.edit {
            if (fullList == null) {
                it.remove(fullListKey(checklistId))
            } else {
                it[fullListKey(checklistId)] = fullList
            }
        }
    }

    /** Flujo con el estado del control por voz (false por defecto). */
    fun voiceControlFlow(checklistId: String): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[voiceControlKey(checklistId)] ?: false }

    /** Guarda estado del control por voz. */
    suspend fun setVoiceControl(checklistId: String, enabled: Boolean) {
        context.dataStore.edit { it[voiceControlKey(checklistId)] = enabled }
    }

    /** Borra todo el progreso de una checklist. */
    suspend fun reset(checklistId: String) {
        context.dataStore.edit {
            it.remove(indexKey(checklistId))
            it.remove(pageKey(checklistId))
            it.remove(checkedKey(checklistId))
            it.remove(fullListKey(checklistId))
            it.remove(voiceControlKey(checklistId))
        }
    }

    /** Borra todo el progreso de todos los checklists. */
    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
