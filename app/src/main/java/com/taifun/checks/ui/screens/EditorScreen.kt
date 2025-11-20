package com.taifun.checks.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taifun.checks.R
import com.taifun.checks.data.ChecklistRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.data.yaml.ParseResult
import com.taifun.checks.data.yaml.YamlIO
import com.taifun.checks.data.yaml.YamlParseException
import com.taifun.checks.ui.rememberHapticFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    // Fix: usar applicationContext para evitar memory leaks
    val repo = remember(ctx.applicationContext) { ChecklistRepository(ctx.applicationContext) }
    val settingsRepo = remember(ctx.applicationContext) { SettingsRepository(ctx.applicationContext) }
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    var text by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var validationJob by remember { mutableStateOf<Job?>(null) }
    var importWarnings by remember { mutableStateOf<List<String>?>(null) }
    var showWarningsDialog by remember { mutableStateOf(false) }

    // Observar el checklist activo
    val activeChecklist by settingsRepo.activeChecklistFlow.collectAsState(initial = "Taifun17E_ES.yaml")

    // Cargar texto inicial
    LaunchedEffect(activeChecklist) {
        repo.load(activeChecklist).fold(
            onSuccess = { cat ->
                text = YamlIO.stringify(cat)
                status = ctx.getString(R.string.status_loaded) + " ($activeChecklist)"
            },
            onFailure = { error ->
                status = ctx.getString(R.string.status_error, error.message ?: ctx.getString(R.string.error_unknown))
            }
        )
    }

    // Validación en tiempo real (debounced)
    LaunchedEffect(text) {
        validationJob?.cancel()
        validationJob = scope.launch {
            delay(500) // debounce de 500ms
            validationError = try {
                YamlIO.parseCatalog(text)
                null // sin errores
            } catch (e: YamlParseException) {
                e.message
            } catch (e: Exception) {
                ctx.getString(R.string.error_unknown)
            }
        }
    }

    // Importar YAML
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    // Parsear con warnings
                    val parseResult = ctx.contentResolver.openInputStream(uri)?.use { input ->
                        YamlIO.parseCatalogWithWarnings(input)
                    }

                    if (parseResult != null) {
                        val cat = parseResult.catalogo
                        // Guardar el catálogo en el archivo activo
                        repo.save(cat, activeChecklist).fold(
                            onSuccess = {
                                text = YamlIO.stringify(cat)
                                status = ctx.getString(R.string.status_imported) + " ($activeChecklist)"
                                validationError = null

                                // Mostrar warnings si los hay
                                if (parseResult.hasWarnings()) {
                                    importWarnings = parseResult.warnings
                                    showWarningsDialog = true
                                }
                            },
                            onFailure = { error ->
                                status = ctx.getString(R.string.status_error, error.message ?: ctx.getString(R.string.error_unknown))
                            }
                        )
                    } else {
                        status = ctx.getString(R.string.status_error, ctx.getString(R.string.error_unknown))
                    }
                } catch (e: YamlParseException) {
                    status = ctx.getString(R.string.status_error, e.message ?: ctx.getString(R.string.error_unknown))
                } catch (e: Exception) {
                    status = ctx.getString(R.string.status_error, e.message ?: ctx.getString(R.string.error_unknown))
                }
            }
        }
    }

    // Exportar YAML
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-yaml")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                repo.exportToUri(uri, activeChecklist).fold(
                    onSuccess = {
                        status = ctx.getString(R.string.status_exported)
                    },
                    onFailure = { error ->
                        status = ctx.getString(R.string.status_error, error.message ?: ctx.getString(R.string.error_unknown))
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editor_title)) },
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
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = if (validationError != null) {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                } else {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (validationError != null) {
                        Text(
                            text = "${stringResource(R.string.validation_error)}: $validationError",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.validation_ok),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Editor de texto
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.yaml_label)) },
                isError = validationError != null
            )

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Guardar
                Button(
                    onClick = {
                        haptic.performStrongFeedback()
                        scope.launch {
                            val parseResult = runCatching {
                                YamlIO.parseCatalog(text)
                            }

                            if (parseResult.isSuccess) {
                                val cat = parseResult.getOrThrow()
                                repo.save(cat, activeChecklist).fold(
                                    onSuccess = {
                                        status = ctx.getString(R.string.status_saved) + " ($activeChecklist)"
                                        validationError = null
                                    },
                                    onFailure = { error ->
                                        val errorMsg = error.message ?: ctx.getString(R.string.error_unknown)
                                        status = ctx.getString(R.string.status_error, errorMsg)
                                    }
                                )
                            } else {
                                val errorMsg = parseResult.exceptionOrNull()?.message ?: ctx.getString(R.string.error_unknown)
                                status = ctx.getString(R.string.status_error, errorMsg)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = validationError == null,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Text(stringResource(R.string.save))
                }

                // Importar
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback()
                        importLauncher.launch(arrayOf("application/x-yaml", "text/yaml", "text/plain", "*/*"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.importar))
                }

                // Exportar
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback()
                        exportLauncher.launch(activeChecklist)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.exportar))
                }
            }
        }
    }

    // Diálogo de warnings de importación
    if (showWarningsDialog && importWarnings != null) {
        val warnings = importWarnings // Capturar valor para evitar problema de smart cast
        AlertDialog(
            onDismissRequest = {
                showWarningsDialog = false
                importWarnings = null
            },
            title = {
                Text(stringResource(R.string.import_warnings))
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        stringResource(R.string.import_warnings_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    warnings?.forEach { warning ->
                        Text(
                            "• $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback()
                    showWarningsDialog = false
                    importWarnings = null
                }) {
                    Text(stringResource(R.string.aceptar))
                }
            }
        )
    }
}
