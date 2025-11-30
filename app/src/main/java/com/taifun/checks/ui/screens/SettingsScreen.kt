package com.taifun.checks.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taifun.checks.R
import com.taifun.checks.data.BluetoothGpsRepository
import com.taifun.checks.data.SensorDataRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.ui.components.BluetoothGpsSettings
import com.taifun.checks.ui.rememberHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val repo = remember { SettingsRepository(ctx) }
    val sensorDataRepo = remember { SensorDataRepository(ctx) }
    val bluetoothGpsRepo = remember { BluetoothGpsRepository(ctx) }
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()
    val scrollState = rememberScrollState()

    val darkTheme by repo.darkFlow.collectAsState(initial = false)
    val highContrast by repo.highContrastFlow.collectAsState(initial = false)
    val screenOn by repo.screenOnFlow.collectAsState(initial = false)
    val language by repo.languageFlow.collectAsState(initial = "auto")
    val hapticsEnabled by repo.hapticsFlow.collectAsState(initial = true)
    val icaoMaxDistanceKm by repo.icaoMaxDistanceKmFlow.collectAsState(initial = 2.0f)
    val icaoMaxAltitudeDiffM by repo.icaoMaxAltitudeDiffMFlow.collectAsState(initial = 50.0f)

    // Estado de diagnóstico de aeródromos
    var aerodromeCount by remember { mutableStateOf<Int?>(null) }

    // Cargar diagnóstico al abrir la pantalla
    LaunchedEffect(Unit) {
        try {
            val aerodromeRepo = com.taifun.checks.data.AerodromeRepository(ctx)
            aerodromeCount = aerodromeRepo.getAerodromeCount()
        } catch (e: Exception) {
            aerodromeCount = -1 // Error
        }
    }

    // Cleanup Bluetooth resources when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            // Don't disconnect here - keep connection alive
            // bluetoothGpsRepo.cleanup()
        }
    }

    // Obtener versión de la app dinámicamente
    val versionName = remember {
        try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sección de Tema
            Text(
                text = stringResource(R.string.theme_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dark Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dark_theme),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = {
                                haptic.performHapticFeedback()
                                scope.launch { repo.setDark(it) }
                            }
                        )
                    }

                    // High Contrast
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(R.string.high_contrast),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.high_contrast_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = highContrast,
                            onCheckedChange = {
                                haptic.performHapticFeedback()
                                scope.launch { repo.setHighContrast(it) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sección de Pantalla
            Text(
                text = stringResource(R.string.screen_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Screen Always On
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(R.string.screen_on),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.screen_on_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = screenOn,
                            onCheckedChange = {
                                haptic.performHapticFeedback()
                                scope.launch { repo.setScreenOn(it) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sección de Idioma
            Text(
                text = stringResource(R.string.language_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.language_label),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Auto language option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.language_auto),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        RadioButton(
                            selected = language == "auto",
                            onClick = {
                                haptic.performHapticFeedback()
                                scope.launch {
                                    repo.setLanguage("auto")
                                    (ctx as? ComponentActivity)?.recreate()
                                }
                            }
                        )
                    }

                    // Spanish option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Español", style = MaterialTheme.typography.bodyLarge)
                        RadioButton(
                            selected = language == "es",
                            onClick = {
                                haptic.performHapticFeedback()
                                scope.launch {
                                    repo.setLanguage("es")
                                    (ctx as? ComponentActivity)?.recreate()
                                }
                            }
                        )
                    }

                    // English option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("English", style = MaterialTheme.typography.bodyLarge)
                        RadioButton(
                            selected = language == "en",
                            onClick = {
                                haptic.performHapticFeedback()
                                scope.launch {
                                    repo.setLanguage("en")
                                    (ctx as? ComponentActivity)?.recreate()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sección de Háptica
            Text(
                text = stringResource(R.string.haptics_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.haptics_enabled),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.haptics_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback()
                                scope.launch { repo.setHaptics(it) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sección de Filtros ICAO
            Text(
                text = stringResource(R.string.icao_filters_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Distancia máxima
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.icao_max_distance),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = String.format("%.1f km", icaoMaxDistanceKm),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = stringResource(R.string.icao_max_distance_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = icaoMaxDistanceKm,
                            onValueChange = { value ->
                                scope.launch { repo.setIcaoMaxDistanceKm(value) }
                            },
                            valueRange = 0.5f..10.0f,
                            steps = 18 // 0.5 step increments
                        )
                    }

                    HorizontalDivider()

                    // Diferencia de altitud máxima
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.icao_max_altitude_diff),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = String.format("%.0f m", icaoMaxAltitudeDiffM),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = stringResource(R.string.icao_max_altitude_diff_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = icaoMaxAltitudeDiffM,
                            onValueChange = { value ->
                                scope.launch { repo.setIcaoMaxAltitudeDiffM(value) }
                            },
                            valueRange = 10.0f..500.0f,
                            steps = 48 // 10 step increments
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Diagnóstico ICAO
            Text(
                text = stringResource(R.string.icao_diagnostics_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.icao_db_title),
                        style = MaterialTheme.typography.titleSmall
                    )

                    when (aerodromeCount) {
                        null -> {
                            Text(
                                text = stringResource(R.string.icao_db_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        -1 -> {
                            Text(
                                text = stringResource(R.string.icao_db_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = stringResource(R.string.icao_db_error_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        0 -> {
                            Text(
                                text = stringResource(R.string.icao_db_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = stringResource(R.string.icao_db_empty_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.icao_db_loaded, aerodromeCount!!),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.icao_db_operational),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bluetooth GPS Settings
            BluetoothGpsSettings(
                settingsRepo = repo,
                sensorDataRepo = sensorDataRepo,
                bluetoothGpsRepo = bluetoothGpsRepo
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Información de la app
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${stringResource(R.string.version)} $versionName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.app_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
