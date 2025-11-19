package com.taifun.checks.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taifun.checks.R
import com.taifun.checks.data.ChecklistRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.ui.rememberHapticFeedback
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstLaunchScreen(
    onComplete: () -> Unit
) {
    val ctx = LocalContext.current
    val settingsRepo = remember(ctx.applicationContext) { SettingsRepository(ctx.applicationContext) }
    val checklistRepo = remember(ctx.applicationContext) { ChecklistRepository(ctx.applicationContext) }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    // Estado
    var selectedLanguage by remember { mutableStateOf(detectSystemLanguage()) }
    var availableChecklists by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedChecklist by remember { mutableStateOf("Taifun17E_ES.yaml") }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newChecklistName by remember { mutableStateOf("") }

    // Función para recargar checklists
    fun reloadChecklists() {
        scope.launch {
            isLoading = true
            checklistRepo.listChecklistFiles().fold(
                onSuccess = { files ->
                    availableChecklists = files
                    if (files.isNotEmpty()) {
                        // Seleccionar checklist por defecto según idioma del sistema
                        val systemLang = Locale.getDefault().language
                        selectedChecklist = when {
                            systemLang == "en" && files.contains("Taifun17E_EN.yaml") -> "Taifun17E_EN.yaml"
                            systemLang == "es" && files.contains("Taifun17E_ES.yaml") -> "Taifun17E_ES.yaml"
                            files.contains("Taifun17E_ES.yaml") -> "Taifun17E_ES.yaml" // Default to Spanish
                            else -> files.first()
                        }
                    }
                    isLoading = false
                },
                onFailure = { error ->
                    Toast.makeText(ctx, error.message, Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            )
        }
    }

    // Cargar checklists disponibles
    LaunchedEffect(Unit) {
        reloadChecklists()
    }

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback()
                            scope.launch {
                                // Guardar configuración
                                settingsRepo.setLanguage(selectedLanguage)
                                settingsRepo.setActiveChecklist(selectedChecklist)
                                settingsRepo.setFirstLaunchComplete()

                                // Navegar a HOME (si hay cambio de idioma, se aplicará en próximo reinicio)
                                onComplete()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.first_launch_continue))
                    }
                }
            }
        }
    ) { pad ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Título
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.first_launch_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.first_launch_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de idioma
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.first_launch_language_label),
                            style = MaterialTheme.typography.titleMedium
                        )

                        LanguageOption(
                            label = stringResource(R.string.language_auto),
                            value = "auto",
                            selected = selectedLanguage == "auto",
                            onClick = {
                                haptic.performHapticFeedback()
                                selectedLanguage = "auto"
                            }
                        )

                        LanguageOption(
                            label = "Español",
                            value = "es",
                            selected = selectedLanguage == "es",
                            onClick = {
                                haptic.performHapticFeedback()
                                selectedLanguage = "es"
                            }
                        )

                        LanguageOption(
                            label = "English",
                            value = "en",
                            selected = selectedLanguage == "en",
                            onClick = {
                                haptic.performHapticFeedback()
                                selectedLanguage = "en"
                            }
                        )
                    }
                }

                // Selector de checklist
                if (availableChecklists.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.first_launch_checklist_label),
                                style = MaterialTheme.typography.titleMedium
                            )

                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedChecklist,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    availableChecklists.forEach { filename ->
                                        DropdownMenuItem(
                                            text = { Text(filename) },
                                            onClick = {
                                                haptic.performHapticFeedback()
                                                selectedChecklist = filename
                                                expanded = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback()
                                    showCreateDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.create_empty_checklist))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // Create empty checklist dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newChecklistName = ""
            },
            title = { Text(stringResource(R.string.create_empty_checklist)) },
            text = {
                OutlinedTextField(
                    value = newChecklistName,
                    onValueChange = { newChecklistName = it },
                    label = { Text(stringResource(R.string.filename_label)) },
                    placeholder = { Text("my_checklist.yaml") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val filename = if (newChecklistName.isBlank()) {
                                null // Auto-generate filename
                            } else {
                                // Ensure .yaml extension
                                if (newChecklistName.endsWith(".yaml")) {
                                    newChecklistName
                                } else {
                                    "$newChecklistName.yaml"
                                }
                            }

                            checklistRepo.createEmptyChecklist(filename).fold(
                                onSuccess = { createdFilename ->
                                    Toast.makeText(
                                        ctx,
                                        ctx.getString(R.string.checklist_created) + ": $createdFilename",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    reloadChecklists()
                                    selectedChecklist = createdFilename
                                },
                                onFailure = { error ->
                                    Toast.makeText(ctx, error.message, Toast.LENGTH_LONG).show()
                                }
                            )
                            showCreateDialog = false
                            newChecklistName = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.create))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    newChecklistName = ""
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.outlinedCardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Detecta el idioma del sistema
 */
private fun detectSystemLanguage(): String {
    return when (Locale.getDefault().language) {
        "es" -> "es"
        "en" -> "en"
        else -> "auto"
    }
}
