package com.taifun.checks.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taifun.checks.R
import com.taifun.checks.data.ChecklistRepository
import com.taifun.checks.data.LogRepository
import com.taifun.checks.data.ProgressRepository
import com.taifun.checks.data.SensorDataRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.domain.Checklist
import com.taifun.checks.ui.IconsRepo
import com.taifun.checks.ui.components.hexToColor
import com.taifun.checks.ui.rememberHapticFeedback
import com.taifun.checks.ui.vm.StepViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.taifun.checks.ui.components.GpsWaitingDialog
import com.taifun.checks.ui.components.isGpsAccurateForLogging
import com.taifun.checks.ui.components.rememberLocationPermissionHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepScreen(
    checklistId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    sensorDataRepo: SensorDataRepository
) {
    val ctx = LocalContext.current
    // Fix: usar applicationContext para evitar memory leaks
    val repo = remember(ctx.applicationContext) { ChecklistRepository(ctx.applicationContext) }
    val settingsRepo = remember(ctx.applicationContext) { SettingsRepository(ctx.applicationContext) }
    val sensorRepo = sensorDataRepo  // Usar la instancia persistente de MainActivity
    val logRepo = remember(ctx.applicationContext) { LogRepository(ctx.applicationContext, settingsRepo) }
    val haptic = rememberHapticFeedback()

    // ViewModel con persistencia de progreso
    val progressRepo = remember(ctx.applicationContext) { ProgressRepository(ctx.applicationContext) }
    val vm = remember(checklistId) { StepViewModel(progressRepo, checklistId) }

    // Observar datos de sensores
    val altitude by sensorRepo.altitude.collectAsState()
    val pressure by sensorRepo.pressure.collectAsState()
    val latitude by sensorRepo.latitude.collectAsState()
    val longitude by sensorRepo.longitude.collectAsState()
    val speedKmh by sensorRepo.speedKmh.collectAsState()

    // Permisos de ubicación usando componente reutilizable
    val locationPermission = rememberLocationPermissionHandler(
        onPermissionGranted = { sensorRepo.startLocationTracking() }
    )

    // Observar idioma de configuración
    val currentLanguage by settingsRepo.languageFlow.collectAsState(initial = "auto")

    // Observar checklist activo
    val activeChecklist by settingsRepo.activeChecklistFlow.collectAsState(initial = "Taifun17E_ES.yaml")

    var checklist by remember { mutableStateOf<Checklist?>(null) }
    var total by remember { mutableStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // Observar estado del ViewModel (persistido)
    val index by vm.index.collectAsState()
    val page by vm.page.collectAsState()
    val checked by vm.checked.collectAsState()
    val userPreferredFullList by vm.fullList.collectAsState()

    // Voice control from SettingsRepository (persistent across all checklists)
    val voiceControlEnabled by settingsRepo.voiceControlFlow.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    // Modo: si el usuario tiene preferencia, usar esa, sino usar la del checklist
    val isFullList = userPreferredFullList ?: (checklist?.fullList == true)

    var isListening by remember { mutableStateOf(false) }

    // Permisos de audio
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            coroutineScope.launch { settingsRepo.setVoiceControl(true) }
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.permission_needed), Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar checklist desde el archivo activo
    LaunchedEffect(checklistId, activeChecklist) {
        repo.load(activeChecklist).fold(
            onSuccess = { cat ->
                val cl = cat.checklists.firstOrNull { it.id == checklistId }
                checklist = cl
                total = cl?.pasos?.size ?: 0
                // Validar que el index no esté fuera de rango
                if (index >= total && total > 0) {
                    vm.setIndex(0)
                }

                // Verificar si algún paso necesita datos de sensores
                val needsLocation = cl?.pasos?.any { it.altitud != null || it.qnh != null || it.log != null } == true
                val needsBarometer = cl?.pasos?.any { it.qnh != null } == true

                if (needsLocation) {
                    locationPermission.requestPermissions()
                }
                if (needsBarometer) {
                    sensorRepo.startPressureTracking()
                }
            },
            onFailure = { error ->
                Toast.makeText(ctx, error.message ?: ctx.getString(R.string.error_unknown), Toast.LENGTH_LONG).show()
            }
        )
    }

    // Dialog de confirmación de reset
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.confirm_reset_title)) },
            text = { Text(stringResource(R.string.confirm_reset_message)) },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performStrongFeedback()
                    vm.reset() // Resetear en ViewModel persistido
                    showResetDialog = false
                }) {
                    Text(stringResource(R.string.aceptar))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performLightFeedback()
                    showResetDialog = false
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }


    // Speech recognizer
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(ctx)) {
            SpeechRecognizer.createSpeechRecognizer(ctx)
        } else null
    }

    // Limpiar recursos al salir
    // Usamos las dependencias reales para que si cambian, los recursos antiguos se limpien correctamente
    DisposableEffect(speechRecognizer, sensorRepo) {
        onDispose {
            speechRecognizer?.destroy()
            sensorRepo.stopAll()
        }
    }

    // Funciones de navegación (usando ViewModel para persistencia)
    // Also handles voice commands when completion screen is shown
    val onPrevious = {
        haptic.performHapticFeedback()
        if (showCompletionDialog) {
            // Voice command "anterior/previous" on completion screen -> go back to previous step
            showCompletionDialog = false
        } else if (isFullList) {
            if (page > 0) vm.prevPage() else onBack()
        } else {
            if (index > 0) vm.prevStep() else onBack()
        }
    }

    val onNext = {
        haptic.performHapticFeedback()
        if (showCompletionDialog) {
            // Voice command "siguiente/next" on completion screen -> exit (reset and go home)
            vm.reset()
            showCompletionDialog = false
            onBack()
        } else if (isFullList) {
            val pasos = checklist?.pasos.orEmpty()
            val totalPages = if (pasos.isEmpty()) 1 else ((pasos.size - 1) / 10 + 1)
            if (page < totalPages - 1) vm.nextPage(totalPages - 1) else showCompletionDialog = true
        } else {
            if (index < total - 1) vm.nextStep(total - 1) else showCompletionDialog = true
        }
    }

    // Voice recognition listener
    LaunchedEffect(voiceControlEnabled) {
        if (voiceControlEnabled && speechRecognizer != null) {
            val recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onError(error: Int) {
                    isListening = false
                    // Reiniciar escucha
                    if (voiceControlEnabled) {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            if (voiceControlEnabled) {
                                startListening(speechRecognizer, ctx, currentLanguage)
                            }
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { command ->
                        val lowerCommand = command.lowercase()
                        when {
                            // Comandos en español
                            lowerCommand.contains("anterior") -> onPrevious()
                            lowerCommand.contains("siguiente") -> onNext()
                            // Comandos en inglés
                            lowerCommand.contains("previous") -> onPrevious()
                            lowerCommand.contains("next") -> onNext()
                        }
                    }
                    // Continuar escuchando
                    if (voiceControlEnabled) {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(300)
                            if (voiceControlEnabled) {
                                startListening(speechRecognizer, ctx, currentLanguage)
                            }
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }

            speechRecognizer.setRecognitionListener(recognitionListener)
            startListening(speechRecognizer, ctx, currentLanguage)
        } else {
            speechRecognizer?.stopListening()
            isListening = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(checklist?.titulo ?: "–") },
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
                    // Botón editar
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        onEdit(checklistId)
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }

                    // Botón de cambio de modo (siempre visible)
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        vm.setFullListMode(!isFullList) // Persistir preferencia en ViewModel
                    }) {
                        Icon(
                            if (isFullList) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.List,
                            contentDescription = if (isFullList)
                                stringResource(R.string.switch_to_stepbystep)
                            else
                                stringResource(R.string.switch_to_fulllist)
                        )
                    }

                    // Control por voz (persistente en toda la app)
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback()
                            if (!voiceControlEnabled) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                coroutineScope.launch { settingsRepo.setVoiceControl(false) }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = stringResource(R.string.voice_control),
                            tint = if (isListening) MaterialTheme.colorScheme.error
                                   else if (voiceControlEnabled) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Reset
                    IconButton(onClick = {
                        haptic.performHapticFeedback()
                        showResetDialog = true
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset))
                    }
                }
            )
        }
    ) { pad ->
        when {
            showCompletionDialog -> {
                // Full screen completion view - same look and feel as step-by-step mode
                CompletionScreen(
                    checklistTitle = checklist?.titulo ?: "",
                    onBack = {
                        haptic.performLightFeedback()
                        showCompletionDialog = false
                        // Return to previous step/page
                    },
                    onExit = {
                        haptic.performStrongFeedback()
                        vm.reset() // Reset checklist progress
                        showCompletionDialog = false
                        onBack() // Go to home
                    },
                    showButtons = !voiceControlEnabled,
                    modifier = Modifier.padding(pad)
                )
            }
            !isFullList -> {
                StepByStepMode(
                    checklist = checklist,
                    index = index,
                    total = total,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    showButtons = !voiceControlEnabled,
                    altitude = altitude,
                    pressure = pressure,
                    latitude = latitude,
                    longitude = longitude,
                    speedKmh = speedKmh,
                    sensorRepo = sensorRepo,
                    settingsRepo = settingsRepo,
                    logRepo = logRepo,
                    language = currentLanguage,
                    modifier = Modifier.padding(pad)
                )
            }
            else -> {
                FullListMode(
                    checklist = checklist,
                    checked = checked,
                    onCheckedChange = { newChecked ->
                        vm.setChecked(newChecked) // Persistir en ViewModel
                    },
                    page = page,
                    onPageChange = { newPage ->
                        vm.setPage(newPage) // Persistir en ViewModel
                    },
                    onBack = onBack,
                    onComplete = { showCompletionDialog = true },
                    showButtons = !voiceControlEnabled,
                    altitude = altitude,
                    pressure = pressure,
                    latitude = latitude,
                    longitude = longitude,
                    speedKmh = speedKmh,
                    sensorRepo = sensorRepo,
                    settingsRepo = settingsRepo,
                    logRepo = logRepo,
                    language = currentLanguage,
                    haptic = haptic,
                    modifier = Modifier.padding(pad)
                )
            }
        }
    }
}

private fun startListening(
    speechRecognizer: SpeechRecognizer,
    ctx: android.content.Context,
    language: String
) {
    // Determinar código de idioma para reconocimiento de voz
    val languageCode = when (language) {
        "es", "spanish" -> "es-ES"
        "en", "english" -> "en-US"
        else -> {
            // Auto: usar idioma del sistema
            val systemLocale = ctx.resources.configuration.locales[0]
            when (systemLocale.language) {
                "es" -> "es-ES"
                "en" -> "en-US"
                else -> "en-US" // Default to English
            }
        }
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
    speechRecognizer.startListening(intent)
}

@Composable
private fun StepByStepMode(
    checklist: Checklist?,
    index: Int,
    total: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    showButtons: Boolean,
    altitude: Double?,
    pressure: Float?,
    latitude: Double?,
    longitude: Double?,
    speedKmh: Float?,
    sensorRepo: SensorDataRepository,
    settingsRepo: SettingsRepository,
    logRepo: LogRepository,
    language: String,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // GPS accuracy for logging
    val gpsAccuracy by sensorRepo.accuracy.collectAsState()
    val gpsVerticalAccuracy by sensorRepo.verticalAccuracy.collectAsState()
    val hasValidAltitude by sensorRepo.hasValidAltitude.collectAsState()
    val icaoMaxAltitudeDiffM by settingsRepo.icaoMaxAltitudeDiffMFlow.collectAsState(initial = 50.0f)
    // Calculate fix age dynamically for UI updates
    var fixAgeMs by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            fixAgeMs = sensorRepo.getFixAgeMs()
            kotlinx.coroutines.delay(1000) // Update every second
        }
    }
    var showGpsWaitingDialog by remember { mutableStateOf(false) }
    var pendingLogText by remember { mutableStateOf<String?>(null) }

    // Permisos de ubicación usando componente reutilizable
    val locationPermission = rememberLocationPermissionHandler(
        onPermissionGranted = {
            sensorRepo.startLocationTracking()
            Toast.makeText(ctx, ctx.getString(R.string.permission_granted_gps), Toast.LENGTH_LONG).show()
        }
    )

    // Función para verificar permisos de ubicación
    val hasLocationPermission = remember(ctx) {
        { com.taifun.checks.ui.components.hasLocationPermission(ctx) }
    }

    // Function to execute log save
    val executeLogSave: (String) -> Unit = { logText ->
        coroutineScope.launch {
            val lang = if (language == "en") "en" else "es"
            val success = logRepo.addLogEntry(
                latitude = latitude,
                longitude = longitude,
                altitudeMeters = altitude,
                speedKmh = speedKmh,
                logText = logText,
                language = lang
            )

            if (success) {
                Toast.makeText(ctx, ctx.getString(R.string.log_entry_saved), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.log_entry_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // GPS Waiting Dialog
    if (showGpsWaitingDialog && pendingLogText != null) {
        GpsWaitingDialog(
            accuracy = gpsAccuracy,
            verticalAccuracy = gpsVerticalAccuracy,
            altitude = altitude,
            hasValidAltitude = hasValidAltitude,
            fixAgeMs = fixAgeMs,
            requiredVerticalAccuracy = icaoMaxAltitudeDiffM,
            onDismiss = {
                showGpsWaitingDialog = false
                pendingLogText = null
            },
            onSaveAnyway = {
                pendingLogText?.let { executeLogSave(it) }
                showGpsWaitingDialog = false
                pendingLogText = null
            },
            onGpsReady = {
                pendingLogText?.let { executeLogSave(it) }
                showGpsWaitingDialog = false
                pendingLogText = null
            }
        )
    }

    val paso = checklist?.pasos?.getOrNull(index)

    // Función para abrir link o app al hacer clic fuera de botones
    val handleMainClick: () -> Unit = {
        paso?.let { p ->
            when {
                !p.link.isNullOrBlank() -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(p.link))
                        ctx.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_cannot_open_link, e.message ?: ""), Toast.LENGTH_SHORT).show()
                    }
                }
                !p.app.isNullOrBlank() -> {
                    try {
                        val pm = ctx.packageManager
                        val intent = pm.getLaunchIntentForPackage(p.app)
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            ctx.startActivity(intent)
                        } else {
                            // Intentar método alternativo
                            try {
                                val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                                    setPackage(p.app)
                                    addCategory(Intent.CATEGORY_LAUNCHER)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                val resolveInfo = pm.queryIntentActivities(launchIntent, 0)
                                if (resolveInfo.isNotEmpty()) {
                                    launchIntent.setClassName(p.app, resolveInfo[0].activityInfo.name)
                                    ctx.startActivity(launchIntent)
                                } else {
                                    Toast.makeText(ctx, ctx.getString(R.string.error_app_not_installed, p.app ?: ""), Toast.LENGTH_SHORT).show()
                                }
                            } catch (e2: Exception) {
                                Toast.makeText(ctx, ctx.getString(R.string.error_cannot_open_app, p.app ?: ""), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_launching_app, e.message ?: ""), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        Unit
    }

    // Usar BoxWithConstraints para detectar orientación de forma confiable
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // Calcular orientación basado en las dimensiones reales del contenedor
        val isPortrait = maxHeight > maxWidth
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = stringResource(R.string.step_of, if (total == 0) 0 else index + 1, total),
            style = MaterialTheme.typography.titleMedium
        )

        // Contenido del paso (clickeable si tiene link o app)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(
                    if (paso?.link != null || paso?.app != null) {
                        Modifier.clickable(onClick = handleMainClick)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Calcular datos opcionales primero para decidir el layout
            val optionalData = paso?.let { p ->
                val data = mutableListOf<String>()

                // Altitud
                if (!p.altitud.isNullOrBlank() && altitude != null) {
                    val value = when (p.altitud.lowercase()) {
                        "ft" -> sensorRepo.metersToFeet(altitude)
                        else -> altitude // "m" o cualquier otro valor
                    }
                    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                        maximumFractionDigits = 0
                        minimumFractionDigits = 0
                        isGroupingUsed = true
                    }
                    val unit = if (p.altitud.lowercase() == "ft") "ft" else "m"
                    data.add("${formatter.format(value)} $unit")
                }

                // QNH
                if (!p.qnh.isNullOrBlank() && altitude != null && pressure != null) {
                    val qnh = sensorRepo.calculateQNH(pressure, altitude)
                    val formatted = when (p.qnh.lowercase()) {
                        "inhg" -> {
                            val inHg = sensorRepo.hPaToInHg(qnh)
                            String.format(Locale.US, "%.2f inHg", inHg)
                        }
                        else -> { // "hpa" o cualquier otro valor
                            val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                                maximumFractionDigits = 0
                                minimumFractionDigits = 0
                                isGroupingUsed = true
                            }
                            "${formatter.format(qnh.toInt())} hPa"
                        }
                    }
                    data.add("QNH: $formatted")
                }

                // Hora local
                if (p.localtime == true) {
                    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    data.add(formatter.format(Date()))
                }

                // Hora UTC
                if (p.utctime == true) {
                    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    data.add("${formatter.format(Date())} UTC")
                }

                data
            } ?: emptyList()

            // Layout: En apaisado con datos opcionales o botón log, poner a la derecha
            if (!isPortrait && (optionalData.isNotEmpty() || !paso?.log.isNullOrBlank())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lado izquierdo: icono y texto
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icono
                        if (paso?.icono != null) {
                            val iconTint = if (!checklist?.color.isNullOrBlank()) {
                                hexToColor(checklist?.color!!)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                            Icon(
                                imageVector = IconsRepo.iconFor(paso.icono),
                                contentDescription = stringResource(R.string.accessibility_step_icon),
                                modifier = Modifier.size(64.dp),
                                tint = iconTint
                            )
                        }

                        // Texto con mejor jerarquía
                        Text(
                            text = paso?.texto ?: stringResource(R.string.loading),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge,
                            lineHeight = 38.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Lado derecho: datos opcionales y botón log en columna vertical
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Datos opcionales
                            optionalData.forEach { data ->
                                Text(
                                    text = data,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Botón de Log (en modo apaisado)
                            if (!paso?.log.isNullOrBlank()) {
                                if (optionalData.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Button(
                                    onClick = {
                                        // Verificar permisos primero
                                        if (!hasLocationPermission()) {
                                            locationPermission.requestPermissions()
                                            return@Button
                                        }

                                        // Check if GPS is accurate enough
                                        val logText = paso.log ?: ""
                                        if (isGpsAccurateForLogging(gpsAccuracy, gpsVerticalAccuracy, altitude, hasValidAltitude, fixAgeMs, icaoMaxAltitudeDiffM)) {
                                            // GPS is good, save directly
                                            executeLogSave(logText)
                                        } else {
                                            // GPS not accurate, show waiting dialog
                                            pendingLogText = logText
                                            showGpsWaitingDialog = true
                                        }
                                    },
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Text(
                                        text = "${if (language == "en") "Log" else "Registrar"}: ${paso.log}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Layout normal: todo en columna (vertical o sin datos opcionales)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono
                    if (paso?.icono != null) {
                        val iconTint = if (!checklist?.color.isNullOrBlank()) {
                            hexToColor(checklist?.color!!)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                        Icon(
                            imageVector = IconsRepo.iconFor(paso.icono),
                            contentDescription = stringResource(R.string.accessibility_step_icon),
                            modifier = Modifier.size(if (isPortrait) 48.dp else 64.dp),
                            tint = iconTint
                        )
                    }

                    // Texto responsive con mejor jerarquía
                    Text(
                        text = paso?.texto ?: stringResource(R.string.loading),
                        fontSize = if (isPortrait) 26.sp else 32.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge,
                        lineHeight = if (isPortrait) 32.sp else 38.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Mostrar datos opcionales debajo con mejor estilo
                    if (optionalData.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                optionalData.forEach { data ->
                                    Text(
                                        text = data,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Botón de Log
                    if (!paso?.log.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                // Verificar permisos primero
                                if (!hasLocationPermission()) {
                                    locationPermission.requestPermissions()
                                    return@Button
                                }

                                // Check if GPS is accurate enough
                                val logText = paso.log ?: ""
                                if (isGpsAccurateForLogging(gpsAccuracy, gpsVerticalAccuracy, altitude, hasValidAltitude, fixAgeMs, icaoMaxAltitudeDiffM)) {
                                    // GPS is good, save directly
                                    executeLogSave(logText)
                                } else {
                                    // GPS not accurate, show waiting dialog
                                    pendingLogText = logText
                                    showGpsWaitingDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            Text(text = "${if (language == "en") "Log" else "Registrar"}: ${paso.log}")
                        }
                    }
                }
            }
        }

        // Botones (solo si no está en modo voz)
        if (showButtons) {
            // Ajustar altura de botones según orientación
            val buttonHeight = if (isPortrait) {
                // Vertical: 20% con mínimo 100dp
                (screenHeight * 0.20f).coerceAtLeast(100.dp)
            } else {
                // Horizontal: 40dp fijo (pequeño para dar espacio al contenido)
                40.dp
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPrevious,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.anterior),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.siguiente),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun FullListMode(
    checklist: Checklist?,
    checked: Set<Int>,
    onCheckedChange: (Set<Int>) -> Unit,
    page: Int,
    onPageChange: (Int) -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    showButtons: Boolean,
    altitude: Double?,
    pressure: Float?,
    latitude: Double?,
    longitude: Double?,
    speedKmh: Float?,
    sensorRepo: SensorDataRepository,
    settingsRepo: SettingsRepository,
    logRepo: LogRepository,
    language: String,
    haptic: com.taifun.checks.ui.HapticFeedbackHelper,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Permisos de ubicación usando componente reutilizable
    val locationPermission = rememberLocationPermissionHandler(
        onPermissionGranted = {
            sensorRepo.startLocationTracking()
            Toast.makeText(ctx, ctx.getString(R.string.permission_granted_gps), Toast.LENGTH_LONG).show()
        }
    )

    // Función para verificar permisos de ubicación
    val hasLocationPermission = remember(ctx) {
        { com.taifun.checks.ui.components.hasLocationPermission(ctx) }
    }

    // GPS accuracy for logging
    val gpsAccuracy by sensorRepo.accuracy.collectAsState()
    val gpsVerticalAccuracy by sensorRepo.verticalAccuracy.collectAsState()
    val hasValidAltitude by sensorRepo.hasValidAltitude.collectAsState()
    val icaoMaxAltitudeDiffM by settingsRepo.icaoMaxAltitudeDiffMFlow.collectAsState(initial = 50.0f)
    // Calculate fix age dynamically for UI updates
    var fixAgeMs by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            fixAgeMs = sensorRepo.getFixAgeMs()
            kotlinx.coroutines.delay(1000) // Update every second
        }
    }
    var showGpsWaitingDialog by remember { mutableStateOf(false) }
    var pendingLogText by remember { mutableStateOf<String?>(null) }

    // Function to execute log save
    val executeLogSave: (String) -> Unit = { logText ->
        coroutineScope.launch {
            val lang = if (language == "en") "en" else "es"
            val success = logRepo.addLogEntry(
                latitude = latitude,
                longitude = longitude,
                altitudeMeters = altitude,
                speedKmh = speedKmh,
                logText = logText,
                language = lang
            )

            if (success) {
                Toast.makeText(ctx, ctx.getString(R.string.log_entry_saved), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.log_entry_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // GPS Waiting Dialog
    if (showGpsWaitingDialog && pendingLogText != null) {
        GpsWaitingDialog(
            accuracy = gpsAccuracy,
            verticalAccuracy = gpsVerticalAccuracy,
            altitude = altitude,
            hasValidAltitude = hasValidAltitude,
            fixAgeMs = fixAgeMs,
            requiredVerticalAccuracy = icaoMaxAltitudeDiffM,
            onDismiss = {
                showGpsWaitingDialog = false
                pendingLogText = null
            },
            onSaveAnyway = {
                pendingLogText?.let { executeLogSave(it) }
                showGpsWaitingDialog = false
                pendingLogText = null
            },
            onGpsReady = {
                pendingLogText?.let { executeLogSave(it) }
                showGpsWaitingDialog = false
                pendingLogText = null
            }
        )
    }

    val pasos = checklist?.pasos.orEmpty()

    // Usar BoxWithConstraints para obtener dimensiones reales de la pantalla
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // Calcular espacio disponible real basado en las dimensiones del contenedor
        val containerHeight = maxHeight
        val containerWidth = maxWidth
        val isPortrait = containerHeight > containerWidth

        // Calcular altura disponible para la lista considerando todos los elementos UI
        val titleHeight = 24.dp // Altura del Text "Página X de Y"
        val topPadding = 16.dp
        val bottomPadding = 16.dp
        val spacingBetweenElements = 12.dp
        val buttonRowHeight = if (showButtons) 60.dp else 0.dp

        val availableHeightForList = containerHeight -
            titleHeight -
            topPadding -
            bottomPadding -
            spacingBetweenElements -
            buttonRowHeight -
            spacingBetweenElements // spacing después del título

        // Estimar altura de cada card considerando contenido variable
        // Card base: padding (32dp) + texto (24dp) + checkbox row
        // Datos opcionales: +40dp si existen
        // Botón log: +56dp si existe
        // Spacing entre cards: 12dp
        val baseItemHeight = 72.dp // Card compacta sin extras
        val estimatedItemHeight = 88.dp // Estimación conservadora con algunos extras
        val itemSpacing = 12.dp

        // Calcular cuántos items caben (usando estimación conservadora)
        val itemsPerPage = ((availableHeightForList) / (estimatedItemHeight + itemSpacing))
            .toInt()
            .coerceAtLeast(3) // Mínimo 3 items por página

        val totalPages = if (pasos.isEmpty()) 1 else ((pasos.size - 1) / itemsPerPage + 1)

        // Guardar el índice absoluto del primer paso visible (persiste en rotaciones)
        var firstVisibleStepIndex by rememberSaveable { mutableStateOf(0) }

        // Bandera para distinguir cambios de página del usuario vs. ajustes por rotación
        var isRotationAdjustment by remember { mutableStateOf(false) }

        // Guardar itemsPerPage anterior para detectar cambios reales (no inicialización)
        var previousItemsPerPage by remember { mutableStateOf<Int?>(null) }

        // Guardar el último itemsPerPage usado para actualizar firstVisibleStepIndex
        var lastItemsPerPageUsed by remember { mutableStateOf(itemsPerPage) }

        // Actualizar el índice guardado cuando el usuario cambia de página manualmente
        LaunchedEffect(page) {
            // Solo actualizar si:
            // 1. No es un ajuste por rotación
            // 2. itemsPerPage no cambió desde la última actualización (evita sobrescribir durante rotación)
            if (!isRotationAdjustment && itemsPerPage == lastItemsPerPageUsed) {
                // Cambio del usuario: guardar el nuevo índice del primer paso visible
                firstVisibleStepIndex = page * itemsPerPage
                lastItemsPerPageUsed = itemsPerPage
            }
            // Resetear la bandera después del ajuste
            isRotationAdjustment = false
        }

        // Cuando cambia itemsPerPage (rotación), recalcular la página que contiene el paso guardado
        LaunchedEffect(itemsPerPage) {
            // Solo ajustar si itemsPerPage realmente cambió (no en carga inicial)
            if (previousItemsPerPage != null && previousItemsPerPage != itemsPerPage && pasos.isNotEmpty() && firstVisibleStepIndex >= 0) {
                // Calcular qué página contiene el paso guardado usando división entera
                // Esto da la página correcta: paso 20 ÷ 6 = 3 → página 3 (pasos 18-23)
                val targetPage = (firstVisibleStepIndex / itemsPerPage).coerceIn(0, totalPages - 1)

                if (targetPage != page) {
                    // Marcar como ajuste por rotación para evitar actualizar firstVisibleStepIndex
                    isRotationAdjustment = true
                    onPageChange(targetPage)
                }
                // Actualizar lastItemsPerPageUsed después del ajuste
                lastItemsPerPageUsed = itemsPerPage
            }
            // Actualizar el valor anterior
            previousItemsPerPage = itemsPerPage
        }

        val start = page * itemsPerPage
        val endExclusive = (start + itemsPerPage).coerceAtMost(pasos.size)
        val current = if (start < endExclusive) pasos.subList(start, endExclusive) else emptyList()

        // Auto-avanzar cuando todos están checkeados
        LaunchedEffect(checked) {
            val allCurrentChecked = current.indices.all { idx -> (start + idx) in checked }
            if (allCurrentChecked && current.isNotEmpty()) {
                delay(300)
                if (page < totalPages - 1) {
                    onPageChange(page + 1)
                } else {
                    onComplete()
                }
            }
        }

        val listState = rememberLazyListState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.page_of, page + 1, totalPages),
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly, // Distribuir uniformemente el espacio
                userScrollEnabled = false // Deshabilitar scroll ya que todos los items deben caber
            ) {
            itemsIndexed(current) { i, p ->
                val absIndex = start + i
                val isChecked = absIndex in checked

                // Calcular datos opcionales para este paso
                val optionalInfo = buildList {
                    // Altitud
                    if (!p.altitud.isNullOrBlank() && altitude != null) {
                        val value = when (p.altitud.lowercase()) {
                            "ft" -> sensorRepo.metersToFeet(altitude)
                            else -> altitude
                        }
                        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault()).apply {
                            maximumFractionDigits = 0
                            minimumFractionDigits = 0
                            isGroupingUsed = true
                        }
                        val unit = if (p.altitud.lowercase() == "ft") "ft" else "m"
                        add("${formatter.format(value)} $unit")
                    }

                    // QNH
                    if (!p.qnh.isNullOrBlank() && altitude != null && pressure != null) {
                        val qnh = sensorRepo.calculateQNH(pressure, altitude)
                        val formatted = when (p.qnh.lowercase()) {
                            "inhg" -> {
                                val inHg = sensorRepo.hPaToInHg(qnh)
                                String.format(java.util.Locale.US, "%.2f inHg", inHg)
                            }
                            else -> {
                                val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault()).apply {
                                    maximumFractionDigits = 0
                                    minimumFractionDigits = 0
                                    isGroupingUsed = true
                                }
                                "${formatter.format(qnh.toInt())} hPa"
                            }
                        }
                        add("QNH: $formatted")
                    }

                    // Hora local
                    if (p.localtime == true) {
                        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        add(formatter.format(java.util.Date()))
                    }

                    // Hora UTC
                    if (p.utctime == true) {
                        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }
                        add("${formatter.format(java.util.Date())} UTC")
                    }

                    // Link indicator
                    if (!p.link.isNullOrBlank()) {
                        add("🔗 ${p.link}")
                    }

                    // App indicator
                    if (!p.app.isNullOrBlank()) {
                        add("📱 ${p.app}")
                    }
                }

                // Handler para click en link/app
                val handleClick: () -> Unit = {
                    when {
                        !p.link.isNullOrBlank() -> {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(p.link))
                                ctx.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(ctx, ctx.getString(R.string.error_cannot_open_link, e.message ?: ""), Toast.LENGTH_SHORT).show()
                            }
                        }
                        !p.app.isNullOrBlank() -> {
                            try {
                                val pm = ctx.packageManager
                                val intent = pm.getLaunchIntentForPackage(p.app)
                                if (intent != null) {
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    ctx.startActivity(intent)
                                } else {
                                    // Intentar método alternativo
                                    try {
                                        val launchIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                                            setPackage(p.app)
                                            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        }
                                        val resolveInfo = pm.queryIntentActivities(launchIntent, 0)
                                        if (resolveInfo.isNotEmpty()) {
                                            launchIntent.setClassName(p.app, resolveInfo[0].activityInfo.name)
                                            ctx.startActivity(launchIntent)
                                        } else {
                                            Toast.makeText(ctx, ctx.getString(R.string.error_app_not_installed, p.app ?: ""), Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e2: Exception) {
                                        Toast.makeText(ctx, ctx.getString(R.string.error_cannot_open_app, p.app ?: ""), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(ctx, ctx.getString(R.string.error_launching_app, e.message ?: ""), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (p.link != null || p.app != null) {
                                Modifier.clickable {
                                    haptic.performHapticFeedback()
                                    handleClick()
                                }
                            } else {
                                Modifier
                            }
                        ),
                    colors = if (isChecked) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    } else {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isChecked) 1.dp else 2.dp,
                        pressedElevation = 4.dp
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (p.icono != null) {
                                    val iconTint = if (!checklist?.color.isNullOrBlank()) {
                                        hexToColor(checklist?.color!!).copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    }
                                    Icon(
                                        imageVector = IconsRepo.iconFor(p.icono),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = iconTint
                                    )
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = p.texto,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (optionalInfo.isNotEmpty()) {
                                        Text(
                                            text = optionalInfo.joinToString(" • "),
                                            fontSize = 13.sp,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { on ->
                                    haptic.performHapticFeedback()
                                    val newChecked = if (on) checked + absIndex else checked - absIndex
                                    onCheckedChange(newChecked)
                                }
                            )
                        }

                        // Botón de Log
                        if (!p.log.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback()

                                    // Verificar permisos primero
                                    if (!hasLocationPermission()) {
                                        locationPermission.requestPermissions()
                                        return@Button
                                    }

                                    // Check if GPS is accurate enough
                                    val logText = p.log ?: ""
                                    if (isGpsAccurateForLogging(gpsAccuracy, gpsVerticalAccuracy, altitude, hasValidAltitude, fixAgeMs, icaoMaxAltitudeDiffM)) {
                                        // GPS is good, save directly
                                        executeLogSave(logText)
                                    } else {
                                        // GPS not accurate, show waiting dialog
                                        pendingLogText = logText
                                        showGpsWaitingDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "${if (language == "en") "Log" else "Registrar"}: ${p.log}")
                            }
                        }
                    }
                }
            }
        }

        // Botones pequeños para tierra
        if (showButtons) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback()
                        if (page > 0) onPageChange(page - 1) else onBack()
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.anterior))
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback()
                        if (page < totalPages - 1) {
                            onPageChange(page + 1)
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text(stringResource(R.string.siguiente))
                }
            }
        }
        }
    }
}

/**
 * Full-screen completion view with same look and feel as step-by-step mode.
 * Designed for glove use with large buttons and voice control support.
 *
 * Voice commands: "siguiente/next" to exit, "anterior/previous" to go back
 */
@Composable
private fun CompletionScreen(
    checklistTitle: String,
    onBack: () -> Unit,
    onExit: () -> Unit,
    showButtons: Boolean,
    modifier: Modifier = Modifier
) {
    // Usar BoxWithConstraints para detectar orientación de forma confiable
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        // Calcular orientación basado en las dimensiones reales del contenedor
        val isPortrait = maxHeight > maxWidth
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título indicando finalización
            Text(
                text = stringResource(R.string.checklist_completed_title),
                style = MaterialTheme.typography.titleMedium
            )

            // Contenido central con mensaje de completado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Icono de check grande
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.checklist_completed_title),
                        modifier = Modifier.size(if (isPortrait) 96.dp else 80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    // Mensaje de completado
                    Text(
                        text = stringResource(R.string.checklist_completed_message),
                        fontSize = if (isPortrait) 28.sp else 32.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge,
                        lineHeight = if (isPortrait) 34.sp else 38.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Nombre del checklist
                    if (checklistTitle.isNotEmpty()) {
                        Text(
                            text = checklistTitle,
                            fontSize = if (isPortrait) 18.sp else 20.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Botones grandes (solo si no está en modo voz)
            if (showButtons) {
                // Ajustar altura de botones según orientación (igual que StepByStepMode)
                val buttonHeight = if (isPortrait) {
                    // Vertical: 20% con mínimo 100dp
                    (screenHeight * 0.20f).coerceAtLeast(100.dp)
                } else {
                    // Horizontal: 40dp fijo (pequeño para dar espacio al contenido)
                    40.dp
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón "Volver" (izquierda) - vuelve al paso anterior
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.checklist_back),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Botón "Salir" (derecha) - resetea y va al inicio
                    Button(
                        onClick = onExit,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.checklist_exit),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
