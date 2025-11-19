package com.taifun.checks.ui.vm

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
 */
class StepViewModel(
    private val progress: ProgressRepository,
    private val checklistId: String
) : ViewModel() {

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
        // Cargar estado persistido
        viewModelScope.launch {
            progress.indexFlow(checklistId).collect { _index.value = it }
        }
        viewModelScope.launch {
            progress.pageFlow(checklistId).collect { _page.value = it }
        }
        viewModelScope.launch {
            progress.checkedFlow(checklistId).collect { _checked.value = it }
        }
        viewModelScope.launch {
            progress.fullListFlow(checklistId).collect { _fullList.value = it }
        }
        viewModelScope.launch {
            progress.voiceControlFlow(checklistId).collect { _voiceControl.value = it }
        }
    }

    // Step-by-step methods
    fun nextStep(maxIndex: Int) {
        val newIdx = (_index.value + 1).coerceAtMost(maxIndex)
        _index.value = newIdx
        viewModelScope.launch { progress.setIndex(checklistId, newIdx) }
    }

    fun prevStep() {
        val newIdx = (_index.value - 1).coerceAtLeast(0)
        _index.value = newIdx
        viewModelScope.launch { progress.setIndex(checklistId, newIdx) }
    }

    fun setIndex(value: Int) {
        val safe = value.coerceAtLeast(0)
        _index.value = safe
        viewModelScope.launch { progress.setIndex(checklistId, safe) }
    }

    // Full-list methods
    fun nextPage(maxPage: Int) {
        val newPage = (_page.value + 1).coerceAtMost(maxPage)
        _page.value = newPage
        viewModelScope.launch { progress.setPage(checklistId, newPage) }
    }

    fun prevPage() {
        val newPage = (_page.value - 1).coerceAtLeast(0)
        _page.value = newPage
        viewModelScope.launch { progress.setPage(checklistId, newPage) }
    }

    fun setPage(value: Int) {
        val safe = value.coerceAtLeast(0)
        _page.value = safe
        viewModelScope.launch { progress.setPage(checklistId, safe) }
    }

    fun toggleChecked(itemIndex: Int) {
        val newChecked = if (itemIndex in _checked.value) {
            _checked.value - itemIndex
        } else {
            _checked.value + itemIndex
        }
        _checked.value = newChecked
        viewModelScope.launch { progress.setChecked(checklistId, newChecked) }
    }

    fun setChecked(checked: Set<Int>) {
        _checked.value = checked
        viewModelScope.launch { progress.setChecked(checklistId, checked) }
    }

    // Mode preference
    fun setFullListMode(enabled: Boolean?) {
        _fullList.value = enabled
        viewModelScope.launch { progress.setFullList(checklistId, enabled) }
    }

    // Voice control
    fun setVoiceControl(enabled: Boolean) {
        _voiceControl.value = enabled
        viewModelScope.launch { progress.setVoiceControl(checklistId, enabled) }
    }

    // Reset all progress
    fun reset() {
        _index.value = 0
        _page.value = 0
        _checked.value = emptySet()
        // No reseteamos fullList ni voiceControl ya que son preferencias
        viewModelScope.launch { progress.reset(checklistId) }
    }
}
