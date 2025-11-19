package com.taifun.checks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taifun.checks.R
import com.taifun.checks.data.ChecklistRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.domain.Catalogo
import com.taifun.checks.domain.Checklist
import com.taifun.checks.domain.Paso
import com.taifun.checks.ui.IconsRepo
import com.taifun.checks.ui.components.ColorPickerDialog
import com.taifun.checks.ui.components.hexToColor
import com.taifun.checks.ui.rememberHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChecklistScreen(
    checklistId: String,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    // Fix: usar applicationContext para evitar memory leaks
    val repo = remember(ctx.applicationContext) { ChecklistRepository(ctx.applicationContext) }
    val settingsRepo = remember(ctx.applicationContext) { SettingsRepository(ctx.applicationContext) }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    // Observar checklist activo
    val activeChecklist by settingsRepo.activeChecklistFlow.collectAsState(initial = "Taifun17E_ES.yaml")

    var catalogo by remember { mutableStateOf<Catalogo?>(null) }
    var checklist by remember { mutableStateOf<Checklist?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados editables
    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var fullList by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf<String?>(null) }
    var pasos by remember { mutableStateOf<List<Paso>>(emptyList()) }

    // Estados para diálogos
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showEditStepDialog by remember { mutableStateOf(false) }
    var showAddStepDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var editingStepIndex by remember { mutableStateOf(-1) }
    var stepToDelete by remember { mutableStateOf(-1) }

    // Cargar checklist desde el archivo activo
    LaunchedEffect(checklistId, activeChecklist) {
        repo.load(activeChecklist).fold(
            onSuccess = { cat ->
                catalogo = cat
                val cl = cat.checklists.find { it.id == checklistId }
                checklist = cl
                if (cl != null) {
                    titulo = cl.titulo
                    categoria = cl.categoria ?: ""
                    fullList = cl.fullList ?: false
                    color = cl.color
                    pasos = cl.pasos
                }
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message ?: ctx.getString(R.string.error_unknown)
                showErrorDialog = true
                isLoading = false
            }
        )
    }

    fun saveChecklist() {
        val cat = catalogo ?: return
        val updatedChecklist = Checklist(
            id = checklistId,
            titulo = titulo,
            categoria = categoria.ifBlank { null },
            fullList = fullList,
            color = color,
            pasos = pasos
        )
        val updatedCatalogs = cat.copy(
            checklists = cat.checklists.map {
                if (it.id == checklistId) updatedChecklist else it
            }
        )
        scope.launch {
            repo.save(updatedCatalogs, activeChecklist).fold(
                onSuccess = {
                    onBack()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: ctx.getString(R.string.error_unknown)
                    showErrorDialog = true
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_checklist_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.accessibility_back)
                        )
                    }
                },
                actions = {
                    // Botón guardar
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        saveChecklist()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        },
        floatingActionButton = {
            // Botón añadir paso
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback()
                    showAddStepDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_step))
            }
        }
    ) { pad ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sección de información del checklist
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.checklist_information),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Título
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    haptic.performHapticFeedback()
                                    showEditTitleDialog = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.title_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = titulo.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Categoría
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    haptic.performHapticFeedback()
                                    showEditCategoryDialog = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.category_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = categoria.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Color
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    haptic.performHapticFeedback()
                                    showColorPickerDialog = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.color_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (color.isNullOrBlank()) {
                                                stringResource(R.string.color_none)
                                            } else {
                                                color ?: ""
                                            },
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!color.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(
                                                        color = hexToColor(color!!),
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Full-list toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.default_mode),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (fullList) {
                                            stringResource(R.string.full_list_mode)
                                        } else {
                                            stringResource(R.string.step_by_step_mode)
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Switch(
                                    checked = fullList,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback()
                                        fullList = it
                                    }
                                )
                            }
                        }
                    }
                }

                // Título de la sección de pasos
                item {
                    Text(
                        text = stringResource(R.string.steps_count, pasos.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Lista de pasos
                itemsIndexed(pasos) { index, paso ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botones de reordenamiento
                            Column {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback()
                                        if (index > 0) {
                                            pasos = pasos.toMutableList().apply {
                                                val temp = this[index]
                                                this[index] = this[index - 1]
                                                this[index - 1] = temp
                                            }
                                        }
                                    },
                                    enabled = index > 0
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback()
                                        if (index < pasos.size - 1) {
                                            pasos = pasos.toMutableList().apply {
                                                val temp = this[index]
                                                this[index] = this[index + 1]
                                                this[index + 1] = temp
                                            }
                                        }
                                    },
                                    enabled = index < pasos.size - 1
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (index < pasos.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                            }

                            // Contenido del paso
                            Column(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. ${paso.texto}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (paso.icono != null) {
                                    Text(
                                        text = stringResource(R.string.icon_colon, paso.icono!!),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Botón editar
                            IconButton(onClick = {
                                haptic.performHapticFeedback()
                                editingStepIndex = index
                                showEditStepDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }

                            // Botón eliminar
                            IconButton(onClick = {
                                haptic.performHapticFeedback()
                                stepToDelete = index
                                showDeleteDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Espacio al final para el FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Diálogo de error
    if (showErrorDialog && errorMessage != null) {
        val message = errorMessage // Capturar valor para evitar problema de smart cast
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                errorMessage = null
            },
            title = { Text("Error") },
            text = { Text(message ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    errorMessage = null
                }) {
                    Text(stringResource(R.string.aceptar))
                }
            }
        )
    }

    // Diálogo editar título
    if (showEditTitleDialog) {
        EditTextDialog(
            title = stringResource(R.string.edit_title),
            initialValue = titulo,
            onDismiss = { showEditTitleDialog = false },
            onSave = { newValue ->
                titulo = newValue
                showEditTitleDialog = false
            }
        )
    }

    // Diálogo editar categoría
    if (showEditCategoryDialog) {
        EditTextDialog(
            title = stringResource(R.string.edit_category),
            initialValue = categoria,
            onDismiss = { showEditCategoryDialog = false },
            onSave = { newValue ->
                categoria = newValue
                showEditCategoryDialog = false
            }
        )
    }

    // Diálogo selector de color
    if (showColorPickerDialog) {
        ColorPickerDialog(
            currentColor = color,
            onColorSelected = { newColor ->
                color = newColor
            },
            onDismiss = { showColorPickerDialog = false }
        )
    }

    // Diálogo editar paso
    if (showEditStepDialog && editingStepIndex >= 0) {
        val paso = pasos.getOrNull(editingStepIndex)
        if (paso != null) {
            EditStepDialog(
                paso = paso,
                onDismiss = {
                    showEditStepDialog = false
                    editingStepIndex = -1
                },
                onSave = { newPaso ->
                    pasos = pasos.toMutableList().apply {
                        this[editingStepIndex] = newPaso
                    }
                    showEditStepDialog = false
                    editingStepIndex = -1
                }
            )
        }
    }

    // Diálogo añadir paso
    if (showAddStepDialog) {
        EditStepDialog(
            paso = null,
            onDismiss = { showAddStepDialog = false },
            onSave = { newPaso ->
                pasos = pasos + newPaso
                showAddStepDialog = false
            }
        )
    }

    // Diálogo confirmar eliminación
    if (showDeleteDialog && stepToDelete >= 0) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                stepToDelete = -1
            },
            title = {
                Text(stringResource(R.string.delete_step_title))
            },
            text = {
                Text(stringResource(R.string.delete_step_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pasos = pasos.filterIndexed { index, _ -> index != stepToDelete }
                        showDeleteDialog = false
                        stepToDelete = -1
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        stepToDelete = -1
                    }
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

@Composable
private fun EditTextDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditStepDialog(
    paso: Paso?,
    onDismiss: () -> Unit,
    onSave: (Paso) -> Unit
) {
    var texto by remember { mutableStateOf(paso?.texto ?: "") }
    var selectedIconId by remember { mutableStateOf(paso?.icono ?: "") }
    var altitud by remember { mutableStateOf(paso?.altitud ?: "") }
    var qnh by remember { mutableStateOf(paso?.qnh ?: "") }
    var link by remember { mutableStateOf(paso?.link ?: "") }
    var app by remember { mutableStateOf(paso?.app ?: "") }
    var localtime by remember { mutableStateOf(paso?.localtime ?: false) }
    var utctime by remember { mutableStateOf(paso?.utctime ?: false) }
    var log by remember { mutableStateOf(paso?.log ?: "") }
    val id = paso?.id ?: "step_${System.currentTimeMillis()}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (paso == null) {
                    stringResource(R.string.add_step)
                } else {
                    stringResource(R.string.edit_step)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text(stringResource(R.string.step_text)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                IconSelector(
                    selectedIconId = selectedIconId,
                    onIconSelected = { selectedIconId = it ?: "" },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.optional_features),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Altitud
                OutlinedTextField(
                    value = altitud,
                    onValueChange = { altitud = it },
                    label = { Text(stringResource(R.string.altitude_label)) },
                    placeholder = { Text(stringResource(R.string.altitude_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // QNH
                OutlinedTextField(
                    value = qnh,
                    onValueChange = { qnh = it },
                    label = { Text(stringResource(R.string.qnh_label)) },
                    placeholder = { Text(stringResource(R.string.qnh_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Link
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text(stringResource(R.string.link_label)) },
                    placeholder = { Text("https://example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // App
                OutlinedTextField(
                    value = app,
                    onValueChange = { app = it },
                    label = { Text(stringResource(R.string.app_label)) },
                    placeholder = { Text("com.google.android.apps.maps") },
                    supportingText = { Text("Package name, not display name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Local Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.local_time_label))
                    Switch(
                        checked = localtime,
                        onCheckedChange = { localtime = it }
                    )
                }

                // UTC Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.utc_time_label))
                    Switch(
                        checked = utctime,
                        onCheckedChange = { utctime = it }
                    )
                }

                // Log
                OutlinedTextField(
                    value = log,
                    onValueChange = { log = it },
                    label = { Text(stringResource(R.string.log_label)) },
                    placeholder = { Text(stringResource(R.string.log_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (texto.isNotBlank()) {
                        onSave(
                            Paso(
                                id = id,
                                texto = texto,
                                icono = selectedIconId.ifBlank { null },
                                altitud = altitud.ifBlank { null },
                                qnh = qnh.ifBlank { null },
                                link = link.ifBlank { null },
                                app = app.ifBlank { null },
                                localtime = if (localtime) true else null,
                                utctime = if (utctime) true else null,
                                log = log.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = texto.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
                }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconSelector(
    selectedIconId: String,
    onIconSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = IconsRepo.getIconOption(selectedIconId)

    // Agrupar iconos por categoría
    val iconsByCategory = IconsRepo.availableIcons.groupBy { it.category }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text(stringResource(R.string.icon_optional))
            },
            leadingIcon = {
                if (selectedOption != null) {
                    Icon(
                        imageVector = selectedOption.icon,
                        contentDescription = selectedOption.name,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            placeholder = {
                Text(stringResource(R.string.select_icon))
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            // Opción para "Sin icono"
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.no_icon),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                onClick = {
                    onIconSelected(null)
                    expanded = false
                },
                leadingIcon = {
                    Box(modifier = Modifier.size(24.dp)) // Espacio vacío
                }
            )

            HorizontalDivider()

            // Iconos agrupados por categoría
            iconsByCategory.forEach { (category, icons) ->
                // Encabezado de categoría
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    },
                    onClick = { },
                    enabled = false
                )

                // Iconos de la categoría
                icons.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onIconSelected(option.id)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.name,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }
        }
    }
}
