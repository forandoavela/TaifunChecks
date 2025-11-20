package com.taifun.checks.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taifun.checks.R
import com.taifun.checks.data.LogEntry
import com.taifun.checks.data.LogRepository
import com.taifun.checks.ui.rememberHapticFeedback
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val haptic = rememberHapticFeedback()
    val logRepo = remember(ctx.applicationContext) { LogRepository(ctx.applicationContext) }
    val scope = rememberCoroutineScope()

    var entries by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<Int?>(null) }
    var entryToEdit by remember { mutableStateOf<Pair<Int, LogEntry>?>(null) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Launcher para importar CSV
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    // Launcher para exportar CSV (guardar como)
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = logRepo.exportToUri(it)
                if (success) {
                    Toast.makeText(ctx, ctx.getString(R.string.export_log_success), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.export_log_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función para recargar las entradas
    val reloadEntries: () -> Unit = {
        scope.launch {
            isLoading = true
            entries = logRepo.readAllEntries().reversed()
            isLoading = false
        }
    }

    // Cargar entradas al inicio
    LaunchedEffect(Unit) {
        reloadEntries()
    }

    // Dialog de confirmación para eliminar entrada
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text(stringResource(R.string.delete_entry_title)) },
            text = { Text(stringResource(R.string.delete_entry_message)) },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performStrongFeedback()
                    scope.launch {
                        val realIndex = entries.size - 1 - entryToDelete!!
                        val success = logRepo.deleteEntry(realIndex)
                        if (success) {
                            Toast.makeText(ctx, ctx.getString(R.string.entry_deleted), Toast.LENGTH_SHORT).show()
                            reloadEntries()
                        } else {
                            Toast.makeText(ctx, ctx.getString(R.string.entry_delete_error), Toast.LENGTH_SHORT).show()
                        }
                        entryToDelete = null
                    }
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performLightFeedback()
                    entryToDelete = null
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    // Dialog de confirmación para limpiar todo
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text(stringResource(R.string.delete_all_title)) },
            text = { Text(stringResource(R.string.delete_all_message)) },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performStrongFeedback()
                    scope.launch {
                        val success = logRepo.clearLog()
                        if (success) {
                            Toast.makeText(ctx, ctx.getString(R.string.log_cleared), Toast.LENGTH_SHORT).show()
                            reloadEntries()
                        } else {
                            Toast.makeText(ctx, ctx.getString(R.string.log_clear_error), Toast.LENGTH_SHORT).show()
                        }
                        showClearAllDialog = false
                    }
                }) {
                    Text(stringResource(R.string.delete_all_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performLightFeedback()
                    showClearAllDialog = false
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    // Dialog de confirmación para importar
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text(stringResource(R.string.import_log_confirm_title)) },
            text = { Text(stringResource(R.string.import_log_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performStrongFeedback()
                    scope.launch {
                        pendingImportUri?.let { uri ->
                            val success = logRepo.importFromCSV(uri)
                            if (success) {
                                Toast.makeText(ctx, ctx.getString(R.string.import_log_success), Toast.LENGTH_SHORT).show()
                                reloadEntries()
                            } else {
                                Toast.makeText(ctx, ctx.getString(R.string.import_log_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                        showImportConfirm = false
                        pendingImportUri = null
                    }
                }) {
                    Text(stringResource(R.string.aceptar))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performLightFeedback()
                    showImportConfirm = false
                    pendingImportUri = null
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    // Dialog de edición
    entryToEdit?.let { (visualIndex, entry) ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { entryToEdit = null },
            onSave = { newEntry ->
                scope.launch {
                    val realIndex = entries.size - 1 - visualIndex
                    val success = logRepo.editEntry(realIndex, newEntry)
                    if (success) {
                        Toast.makeText(ctx, ctx.getString(R.string.edit_entry_success), Toast.LENGTH_SHORT).show()
                        reloadEntries()
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.edit_entry_error), Toast.LENGTH_SHORT).show()
                    }
                    entryToEdit = null
                }
            },
            haptic = haptic
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.log_viewer_title)) },
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
                    // Botón exportar/guardar
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        exportLauncher.launch("flight_log.csv")
                    }) {
                        Icon(Icons.Outlined.Download, contentDescription = stringResource(R.string.export_log))
                    }

                    // Botón importar
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        importLauncher.launch("text/*")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.import_log))
                    }

                    // Botón compartir
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback()
                            if (entries.isEmpty()) {
                                Toast.makeText(ctx, ctx.getString(R.string.no_log_to_share), Toast.LENGTH_SHORT).show()
                            } else {
                                val result = logRepo.createShareIntent()
                                if (result.intent != null) {
                                    ctx.startActivity(android.content.Intent.createChooser(result.intent, ctx.getString(R.string.share_log)))
                                } else {
                                    Toast.makeText(ctx, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = entries.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_log))
                    }

                    // Botón borrar todo
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        showClearAllDialog = true
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.delete_all_title), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
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
            // Contador de entradas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.total_entries, entries.size),
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Contenido
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_log_entries),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.no_log_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(entries) { index, entry ->
                        LogEntryCard(
                            entry = entry,
                            onDelete = {
                                haptic.performHapticFeedback()
                                entryToDelete = index
                            },
                            onEdit = {
                                haptic.performHapticFeedback()
                                entryToEdit = index to entry
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(
    entry: LogEntry,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Título: Texto del log
                Text(
                    text = entry.logText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    // Botón editar
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_entry),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón eliminar
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_entry),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Hora UTC
            Text(
                text = "🕐 ${formatUtcTime(entry.utcTime)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )

            // Coordenadas
            Text(
                text = "📍 ${String.format(Locale.US, "%.6f", entry.latitude)}, ${String.format(Locale.US, "%.6f", entry.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Altitud
            Text(
                text = "✈️  ${String.format(Locale.US, "%.0f m", entry.altitudeMeters)}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // ICAO (si existe)
            if (!entry.icaoCode.isNullOrBlank()) {
                Text(
                    text = "🛬 ICAO: ${entry.icaoCode}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEntryDialog(
    entry: LogEntry,
    onDismiss: () -> Unit,
    onSave: (LogEntry) -> Unit,
    haptic: com.taifun.checks.ui.HapticFeedbackHelper
) {
    var text by remember { mutableStateOf(entry.logText) }
    var latitude by remember { mutableStateOf(entry.latitude.toString()) }
    var longitude by remember { mutableStateOf(entry.longitude.toString()) }
    var altitude by remember { mutableStateOf(entry.altitudeMeters.toString()) }
    var icao by remember { mutableStateOf(entry.icaoCode ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_entry_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.entry_text_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(R.string.entry_latitude_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(R.string.entry_longitude_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = altitude,
                    onValueChange = { altitude = it },
                    label = { Text(stringResource(R.string.entry_altitude_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = icao,
                    onValueChange = { icao = it },
                    label = { Text(stringResource(R.string.entry_icao_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                haptic.performStrongFeedback()
                try {
                    val newEntry = LogEntry(
                        utcTime = entry.utcTime,
                        latitude = latitude.toDouble(),
                        longitude = longitude.toDouble(),
                        altitudeMeters = altitude.toDouble(),
                        icaoCode = icao.ifBlank { null },
                        logText = text
                    )
                    onSave(newEntry)
                } catch (e: NumberFormatException) {
                    // Ignorar si hay error de formato
                }
            }) {
                Text(stringResource(R.string.edit_entry_save))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.performLightFeedback()
                onDismiss()
            }) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

/**
 * Formatea la hora UTC para mostrarla de forma legible
 */
private fun formatUtcTime(utcTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcTime)
        date?.let { outputFormat.format(it) + " UTC" } ?: utcTime
    } catch (e: Exception) {
        utcTime
    }
}
