package com.taifun.checks.ui.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taifun.checks.data.ChecklistRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.domain.Catalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ChecklistRepository(app)
    private val settingsRepo = SettingsRepository(app)

    private val _catalogo = MutableStateFlow(Catalogo())
    val catalogo: StateFlow<Catalogo> = _catalogo

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Observar cambios en el checklist activo
        viewModelScope.launch {
            settingsRepo.activeChecklistFlow.collect { activeFile ->
                loadChecklist(activeFile)
            }
        }
    }

    private fun loadChecklist(filename: String) {
        viewModelScope.launch {
            repo.load(filename).fold(
                onSuccess = { catalogo ->
                    _catalogo.value = catalogo
                    _error.value = null
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error cargando catálogo $filename", exception)
                    _error.value = exception.message
                }
            )
        }
    }

    fun reload() {
        viewModelScope.launch {
            settingsRepo.activeChecklistFlow.collect { activeFile ->
                loadChecklist(activeFile)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
