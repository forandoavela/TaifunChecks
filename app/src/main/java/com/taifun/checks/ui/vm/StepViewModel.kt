package com.taifun.checks.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taifun.checks.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el progreso de una checklist.
 * Persiste el estado completo usando ProgressRepository y DataStore.
 * Incluye manejo de errores con rollback para garantizar consistencia.
 */
class StepViewModel(
    private val progress: ProgressRepository,
    private val checklistId: String
) : ViewModel() {

    companion object {
        private const val TAG = "StepViewModel"
    }

    // Step-by-step mode
    private val _index = MutableStateFlow(0)
    val index: StateFlow<Int> = _index.asStateFlow()

    // Full-list mode
    private val _page = MutableStateFlow(0)
    val page: StateFlow<Int> = _page.asStateFlow()

    private val _checked = MutableStateFlow<Set<Int>>(emptySet())
    val checked: StateFlow<Set<Int>> = _checked.asStateFlow()

    // Preferencia de modo (null = usar configuración del checklist)
    private val _fullList = MutableStateFlow<Boolean?>(null)
    val fullList: StateFlow<Boolean?> = _fullList.asStateFlow()

    // Control por voz
    private val _voiceControl = MutableStateFlow(false)
    val voiceControl: StateFlow<Boolean> = _voiceControl.asStateFlow()

    init {
        // Cargar estado persistido con manejo de errores
        viewModelScope.launch {
            try {
                progress.indexFlow(checklistId).collect { _index.value = it }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando índice persistido", e)
            }
        }
        viewModelScope.launch {
            try {
                progress.pageFlow(checklistId).collect { _page.value = it }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando página persistida", e)
            }
        }
        viewModelScope.launch {
            try {
                progress.checkedFlow(checklistId).collect { _checked.value = it }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando items marcados", e)
            }
        }
        viewModelScope.launch {
            try {
                progress.fullListFlow(checklistId).collect { _fullList.value = it }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando preferencia de modo", e)
            }
        }
        viewModelScope.launch {
            try {
                progress.voiceControlFlow(checklistId).collect { _voiceControl.value = it }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando control de voz", e)
            }
        }
    }

    // Step-by-step methods con manejo de errores
    fun nextStep(maxIndex: Int) {
        val oldIdx = _index.value
        val newIdx = (oldIdx + 1).coerceAtMost(maxIndex)
        _index.value = newIdx
        viewModelScope.launch {
            try {
                progress.setIndex(checklistId, newIdx)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo índice, revirtiendo", e)
                _index.value = oldIdx // Rollback
            }
        }
    }

    fun prevStep() {
        val oldIdx = _index.value
        val newIdx = (oldIdx - 1).coerceAtLeast(0)
        _index.value = newIdx
        viewModelScope.launch {
            try {
                progress.setIndex(checklistId, newIdx)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo índice, revirtiendo", e)
                _index.value = oldIdx // Rollback
            }
        }
    }

    fun setIndex(value: Int) {
        val oldIdx = _index.value
        val safe = value.coerceAtLeast(0)
        _index.value = safe
        viewModelScope.launch {
            try {
                progress.setIndex(checklistId, safe)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo índice, revirtiendo", e)
                _index.value = oldIdx // Rollback
            }
        }
    }

    // Full-list methods con manejo de errores
    fun nextPage(maxPage: Int) {
        val oldPage = _page.value
        val newPage = (oldPage + 1).coerceAtMost(maxPage)
        _page.value = newPage
        viewModelScope.launch {
            try {
                progress.setPage(checklistId, newPage)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo página, revirtiendo", e)
                _page.value = oldPage // Rollback
            }
        }
    }

    fun prevPage() {
        val oldPage = _page.value
        val newPage = (oldPage - 1).coerceAtLeast(0)
        _page.value = newPage
        viewModelScope.launch {
            try {
                progress.setPage(checklistId, newPage)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo página, revirtiendo", e)
                _page.value = oldPage // Rollback
            }
        }
    }

    fun setPage(value: Int) {
        val oldPage = _page.value
        val safe = value.coerceAtLeast(0)
        _page.value = safe
        viewModelScope.launch {
            try {
                progress.setPage(checklistId, safe)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo página, revirtiendo", e)
                _page.value = oldPage // Rollback
            }
        }
    }

    fun toggleChecked(itemIndex: Int) {
        val oldChecked = _checked.value
        val newChecked = if (itemIndex in oldChecked) {
            oldChecked - itemIndex
        } else {
            oldChecked + itemIndex
        }
        _checked.value = newChecked
        viewModelScope.launch {
            try {
                progress.setChecked(checklistId, newChecked)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo items marcados, revirtiendo", e)
                _checked.value = oldChecked // Rollback
            }
        }
    }

    fun setChecked(checked: Set<Int>) {
        val oldChecked = _checked.value
        _checked.value = checked
        viewModelScope.launch {
            try {
                progress.setChecked(checklistId, checked)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo items marcados, revirtiendo", e)
                _checked.value = oldChecked // Rollback
            }
        }
    }

    // Mode preference con manejo de errores
    fun setFullListMode(enabled: Boolean?) {
        val oldValue = _fullList.value
        _fullList.value = enabled
        viewModelScope.launch {
            try {
                progress.setFullList(checklistId, enabled)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo modo, revirtiendo", e)
                _fullList.value = oldValue // Rollback
            }
        }
    }

    // Voice control con manejo de errores
    fun setVoiceControl(enabled: Boolean) {
        val oldValue = _voiceControl.value
        _voiceControl.value = enabled
        viewModelScope.launch {
            try {
                progress.setVoiceControl(checklistId, enabled)
            } catch (e: Exception) {
                Log.e(TAG, "Error persistiendo control de voz, revirtiendo", e)
                _voiceControl.value = oldValue // Rollback
            }
        }
    }

    // Reset all progress con manejo de errores
    fun reset() {
        val oldIndex = _index.value
        val oldPage = _page.value
        val oldChecked = _checked.value

        _index.value = 0
        _page.value = 0
        _checked.value = emptySet()
        // No reseteamos fullList ni voiceControl ya que son preferencias

        viewModelScope.launch {
            try {
                progress.reset(checklistId)
            } catch (e: Exception) {
                Log.e(TAG, "Error reseteando progreso, revirtiendo", e)
                // Rollback
                _index.value = oldIndex
                _page.value = oldPage
                _checked.value = oldChecked
            }
        }
    }
}
