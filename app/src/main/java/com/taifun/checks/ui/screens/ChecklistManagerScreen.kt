package com.taifun.checks.ui.screens

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.taifun.checks.R
import com.taifun.checks.data.ChecklistRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.ui.rememberHapticFeedback
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistManagerScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    onOpenEditor: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val repo = remember(ctx.applicationContext) { ChecklistRepository(ctx.applicationContext) }
    val settingsRepo = remember(ctx.applicationContext) { SettingsRepository(ctx.applicationContext) }
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    var checklistFiles by remember { mutableStateOf<List<String>>(emptyList()) }
    var activeChecklist by remember { mutableStateOf("Taifun17E_ES.yaml") }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newChecklistName by remember { mutableStateOf("") }

    // Observar checklist activo
    LaunchedEffect(Unit) {
        settingsRepo.activeChecklistFlow.collect { active ->
            activeChecklist = active
        }
    }

    // Función para recargar lista
    fun reload() {
        scope.launch {
            isLoading = true
            repo.listChecklistFiles().fold(
                onSuccess = { files ->
                    checklistFiles = files
                    isLoading = false
                },
                onFailure = { error ->
                    Toast.makeText(ctx, error.message, Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            )
        }
    }

    // Cargar archivos al inicio
    LaunchedEffect(Unit) {
        reload()
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                // Extraer el nombre del archivo original del URI
                val originalFilename = ctx.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)
                    } else {
                        null
                    }
                }

                repo.importFromUri(it, originalFilename).fold(
                    onSuccess = { (filename, _) ->
                        Toast.makeText(
                            ctx,
                            ctx.getString(R.string.manager_imported) + ": $filename",
                            Toast.LENGTH_SHORT
                        ).show()
                        reload()
                    },
                    onFailure = { error ->
                        Toast.makeText(ctx, error.message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    // Export launcher
    var fileToExport by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/yaml")
    ) { uri ->
        uri?.let {
            scope.launch {
                fileToExport?.let { filename ->
                    repo.exportToUri(it, filename).fold(
                        onSuccess = {
                            Toast.makeText(
                                ctx,
                                ctx.getString(R.string.status_exported),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(ctx, error.message, Toast.LENGTH_LONG).show()
                        }
                    )
                }
                fileToExport = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.manager_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Botón crear checklist vacío
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        showCreateDialog = true
                    }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.create_empty_checklist)
                        )
                    }
                    // Botón importar
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        importLauncher.launch(arrayOf("application/x-yaml", "text/yaml", "text/plain", "*/*"))
                    }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.manager_import)
                        )
                    }
                }
            )
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
        } else if (checklistFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.manager_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = {
                            haptic.performHapticFeedback()
                            showCreateDialog = true
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.create_new))
                        }
                        Button(onClick = {
                            haptic.performHapticFeedback()
                            importLauncher.launch(arrayOf("application/x-yaml", "text/yaml", "text/plain", "*/*"))
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.manager_import))
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(checklistFiles, key = { it }) { filename ->
                    ChecklistFileCard(
                        filename = filename,
                        isActive = filename == activeChecklist,
                        onSelect = {
                            scope.launch {
                                settingsRepo.setActiveChecklist(filename)
                                Toast.makeText(
                                    ctx,
                                    ctx.getString(R.string.manager_selected),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onExport = {
                            fileToExport = filename
                            exportLauncher.launch(filename)
                        },
                        onShare = {
                            // Compartir usando FileProvider
                            try {
                                val file = repo.getFileReference(filename)
                                if (file.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        ctx,
                                        "${ctx.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/yaml"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, filename)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    ctx.startActivity(Intent.createChooser(shareIntent, null))
                                } else {
                                    Toast.makeText(ctx, "File not found", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        onDelete = {
                            fileToDelete = filename
                            showDeleteDialog = true
                        },
                        onEdit = {
                            scope.launch {
                                // Set this file as active, then navigate to editor
                                settingsRepo.setActiveChecklist(filename)
                                onOpenEditor()
                            }
                        },
                        haptic = haptic
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && fileToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                fileToDelete = null
            },
            title = { Text(stringResource(R.string.manager_confirm_delete_title)) },
            text = { Text(stringResource(R.string.manager_confirm_delete_message) + ": $fileToDelete") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            fileToDelete?.let { filename ->
                                repo.delete(filename).fold(
                                    onSuccess = {
                                        Toast.makeText(
                                            ctx,
                                            ctx.getString(R.string.manager_deleted),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        reload()
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(ctx, error.message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                            showDeleteDialog = false
                            fileToDelete = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.aceptar))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    fileToDelete = null
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
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

                            repo.createEmptyChecklist(filename).fold(
                                onSuccess = { createdFilename ->
                                    Toast.makeText(
                                        ctx,
                                        ctx.getString(R.string.checklist_created) + ": $createdFilename",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    reload()
                                    // Set as active
                                    settingsRepo.setActiveChecklist(createdFilename)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChecklistFileCard(
    filename: String,
    isActive: Boolean,
    onSelect: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    haptic: com.taifun.checks.ui.HapticFeedbackHelper
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isActive) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 1.dp else 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título del archivo con badge si es activo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filename,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.manager_current_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de selección (solo si no es actual)
                if (!isActive) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback()
                            onSelect()
                        },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.manager_select))
                    }
                }

                IconButton(onClick = {
                    haptic.performHapticFeedback()
                    onEdit()
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editor_button))
                }

                IconButton(onClick = {
                    haptic.performHapticFeedback()
                    onExport()
                }) {
                    Icon(Icons.Outlined.Download, contentDescription = stringResource(R.string.manager_export))
                }

                IconButton(onClick = {
                    haptic.performHapticFeedback()
                    onShare()
                }) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.manager_share))
                }

                IconButton(onClick = {
                    haptic.performHapticFeedback()
                    onDelete()
                }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.manager_delete),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
