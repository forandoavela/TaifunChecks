package com.taifun.checks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taifun.checks.R
import com.taifun.checks.ui.components.hexToColor
import com.taifun.checks.ui.rememberHapticFeedback
import com.taifun.checks.ui.vm.HomeViewModel
import com.taifun.checks.domain.Checklist
import com.taifun.checks.data.ChecklistRepository
import com.taifun.checks.data.LogRepository
import com.taifun.checks.data.ProgressRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenChecklist: (String) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    onEditChecklist: (String) -> Unit,
    onOpenManager: () -> Unit = {},
    onOpenLogViewer: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val haptic = rememberHapticFeedback()
    // Fix: usar applicationContext para evitar memory leaks en rotaciones
    val repo = remember(ctx.applicationContext) { ChecklistRepository(ctx.applicationContext) }
    val progressRepo = remember(ctx.applicationContext) { ProgressRepository(ctx.applicationContext) }
    val logRepo = remember(ctx.applicationContext) { LogRepository(ctx.applicationContext) }
    val scope = rememberCoroutineScope()
    val vm: HomeViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
        ctx.applicationContext as android.app.Application
    ))

    val catalogo by vm.catalogo.collectAsState()
    val error by vm.error.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showResetAllDialog by remember { mutableStateOf(false) }

    // Estados para editar categoría
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf("") }
    var newCategoryName by remember { mutableStateOf("") }

    // Filtrar y agrupar por categoría
    val filteredLists = catalogo.checklists.filter {
        it.titulo.contains(searchQuery, ignoreCase = true) ||
                it.categoria?.contains(searchQuery, ignoreCase = true) == true
    }

    val groupedByCategory = filteredLists.groupBy { it.categoria ?: "Sin categoría" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Botón de Log Viewer
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onOpenLogViewer()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Ver log de vuelo")
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        showResetAllDialog = true
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_all))
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onOpenHelp()
                    }) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.help_button))
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onOpenManager()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = stringResource(R.string.manager_title))
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onOpenSettings()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_button))
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            // Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Default.Search,
                        contentDescription = stringResource(R.string.accessibility_search))
                },
                singleLine = true
            )

            // Lista vacía
            if (filteredLists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty())
                                stringResource(R.string.empty_list_title)
                            else
                                "No se encontraron resultados",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.empty_list_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Lista agrupada por categorías
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedByCategory.forEach { (categoria, checklists) ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = categoria,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback()
                                        categoryToEdit = categoria
                                        newCategoryName = categoria
                                        showEditCategoryDialog = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar categoría",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        items(checklists, key = { it.id }) { checklist ->
                            ChecklistButton(
                                checklist = checklist,
                                onClick = { onOpenChecklist(checklist.id) },
                                onEdit = { onEditChecklist(checklist.id) },
                                haptic = haptic
                            )
                        }
                    }
                }
            }
        }
    }

    // Recargar en composición inicial y cuando regresamos de otras pantallas
    // Esto resuelve el problema de que los cambios del editor no se reflejaban
    DisposableEffect(Unit) {
        vm.reload()
        onDispose { }
    }

    // Mostrar diálogo de error si existe
    LaunchedEffect(error) {
        if (error != null) {
            showErrorDialog = true
        }
    }

    // Diálogo para editar categoría
    if (showEditCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showEditCategoryDialog = false },
            title = {
                Text(stringResource(R.string.edit_category))
            },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = {
                        Text(stringResource(R.string.category_name))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback()
                        scope.launch {
                            // Actualizar todos los checklists de esta categoría
                            val updatedChecklists = catalogo.checklists.map { checklist ->
                                if (checklist.categoria == categoryToEdit) {
                                    checklist.copy(categoria = newCategoryName.ifBlank { null })
                                } else {
                                    checklist
                                }
                            }
                            val updatedCatalogo = catalogo.copy(checklists = updatedChecklists)
                            repo.save(updatedCatalogo).onSuccess {
                                vm.reload()
                                showEditCategoryDialog = false
                            }.onFailure {
                                // El error se mostrará vía el error flow del ViewModel
                                vm.reload() // Intentar recargar de todas formas
                            }
                        }
                    },
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback()
                    showEditCategoryDialog = false
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    // Diálogo de error
    if (showErrorDialog && error != null) {
        val errorMessage = error // Capturar valor para evitar problema de smart cast
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                vm.clearError()
            },
            title = {
                Text(stringResource(R.string.error))
            },
            text = {
                Text(errorMessage ?: "")
            },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback()
                    showErrorDialog = false
                    vm.clearError()
                }) {
                    Text(stringResource(R.string.aceptar))
                }
            }
        )
    }

    // Diálogo de reset all
    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text(stringResource(R.string.confirm_reset_all_title)) },
            text = { Text(stringResource(R.string.confirm_reset_all_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback()
                        scope.launch {
                            progressRepo.resetAll()
                            Toast.makeText(
                                ctx,
                                ctx.getString(R.string.reset_all_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            showResetAllDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback()
                    showResetAllDialog = false
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

@Composable
private fun ChecklistButton(
    checklist: Checklist,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    haptic: com.taifun.checks.ui.HapticFeedbackHelper
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color (si existe)
            if (!checklist.color.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(hexToColor(checklist.color!!))
                )
            }

            // Botón principal (abrir checklist)
            Button(
                onClick = {
                    haptic.performHapticFeedback()
                    onClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(16.dp),
                colors = if (!checklist.color.isNullOrBlank()) {
                    ButtonDefaults.buttonColors(
                        containerColor = hexToColor(checklist.color!!)
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = checklist.titulo,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${checklist.pasos.size} pasos" +
                                if (checklist.fullList == true) " • Lista completa" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Botón editar
            IconButton(
                onClick = {
                    haptic.performHapticFeedback()
                    onEdit()
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
