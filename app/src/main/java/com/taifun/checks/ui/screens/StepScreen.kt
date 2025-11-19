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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepScreen(
    checklistId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    // Fix: usar applicationContext para evitar memory leaks
    val repo = remember(ctx.applicationContext) { ChecklistRepository(ctx.applicationContext) }
    val settingsRepo = remember(ctx.applicationContext) { SettingsRepository(ctx.applicationContext) }
    val sensorRepo = remember(ctx.applicationContext) { SensorDataRepository(ctx.applicationContext) }
    val logRepo = remember(ctx.applicationContext) { LogRepository(ctx.applicationContext) }
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

    // Permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            sensorRepo.startLocationTracking()
        }
    }

    // Observar idioma de configuración
    val currentLanguage by settingsRepo.languageFlow.collectAsState(initial = "auto")

    // Observar checklist activo
    val activeChecklist by settingsRepo.activeChecklistFlow.collectAsState(initial = "Taifun17E_ES.yaml")

    var checklist by remember { mutableStateOf<Checklist?>(null) }
    var total by remember { mutableStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Observar estado del ViewModel (persistido)
    val index by vm.index.collectAsState()
    val page by vm.page.collectAsState()
    val checked by vm.checked.collectAsState()
    val userPreferredFullList by vm.fullList.collectAsState()
    val voiceControlEnabled by vm.voiceControl.collectAsState()

    // Modo: si el usuario tiene preferencia, usar esa, sino usar la del checklist
    val isFullList = userPreferredFullList ?: (checklist?.fullList == true)

    var isListening by remember { mutableStateOf(false) }

    // Permisos de audio
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.setVoiceControl(true)
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
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
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
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            sensorRepo.stopAll()
        }
    }

    // Funciones de navegación (usando ViewModel para persistencia)
    val onPrevious = {
        haptic.performHapticFeedback()
        if (isFullList) {
            if (page > 0) vm.prevPage() else onBack()
        } else {
            if (index > 0) vm.prevStep() else onBack()
        }
    }

    val onNext = {
        haptic.performHapticFeedback()
        if (isFullList) {
            val pasos = checklist?.pasos.orEmpty()
            val totalPages = if (pasos.isEmpty()) 1 else ((pasos.size - 1) / 10 + 1)
            if (page < totalPages - 1) vm.nextPage(totalPages - 1) else onBack()
        } else {
            if (index < total - 1) vm.nextStep(total - 1) else onBack()
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

                    // Control por voz
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback()
                            if (!voiceControlEnabled) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                vm.setVoiceControl(false) // Persistir en ViewModel
                            }
                        }
                    ) {
                        Icon(
                            IconsRepo.Microfono,
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
        if (!isFullList) {
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
                logRepo = logRepo,
                language = currentLanguage,
                modifier = Modifier.padding(pad)
            )
        } else {
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
                showButtons = !voiceControlEnabled,
                altitude = altitude,
                pressure = pressure,
                latitude = latitude,
                longitude = longitude,
                speedKmh = speedKmh,
                sensorRepo = sensorRepo,
                logRepo = logRepo,
                language = currentLanguage,
                haptic = haptic,
                modifier = Modifier.padding(pad)
            )
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
    logRepo: LogRepository,
    language: String,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Si se otorgan permisos, iniciar tracking e intentar guardar log de nuevo
        if (permissions.values.any { it }) {
            sensorRepo.startLocationTracking()
            Toast.makeText(ctx, "Permisos otorgados. Esperando GPS... Pulse de nuevo en unos segundos.", Toast.LENGTH_LONG).show()
        }
    }

    // Función para verificar permisos de ubicación
    val hasLocationPermission = remember {
        {
            androidx.core.content.ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
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
                        Toast.makeText(ctx, "Cannot open link: ${e.message}", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(ctx, "App not installed: ${p.app}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e2: Exception) {
                                Toast.makeText(ctx, "Cannot open app: ${p.app}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Error launching app: ${e.message}", Toast.LENGTH_SHORT).show()
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

                        // Texto
                        Text(
                            text = paso?.texto ?: stringResource(R.string.loading),
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                            lineHeight = 34.sp
                        )
                    }

                    // Lado derecho: datos opcionales y botón log en columna vertical
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Datos opcionales
                        optionalData.forEach { data ->
                            Text(
                                text = data,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
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
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                        return@Button
                                    }

                                    // Verificar que tenemos datos GPS válidos
                                    if (latitude != null && longitude != null && altitude != null) {
                                        coroutineScope.launch {
                                            val lang = if (language == "en") "en" else "es"
                                            val success = logRepo.addLogEntry(
                                                latitude = latitude,
                                                longitude = longitude,
                                                altitudeMeters = altitude,
                                                speedKmh = speedKmh,
                                                logText = paso.log ?: "",
                                                language = lang
                                            )

                                            if (success) {
                                                Toast.makeText(
                                                    ctx,
                                                    if (lang == "en") "Log entry saved" else "Entrada guardada",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    ctx,
                                                    if (lang == "en") "Error saving log" else "Error al guardar",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            ctx,
                                            if (language == "en") "GPS data not available" else "Datos GPS no disponibles",
                                            Toast.LENGTH_SHORT
                                        ).show()
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

                    // Texto responsive
                    Text(
                        text = paso?.texto ?: stringResource(R.string.loading),
                        fontSize = if (isPortrait) 22.sp else 28.sp,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        lineHeight = if (isPortrait) 28.sp else 34.sp
                    )

                    // Mostrar datos opcionales debajo
                    if (optionalData.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            optionalData.forEach { data ->
                                Text(
                                    text = data,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
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
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                    return@Button
                                }

                                // Verificar que tenemos datos GPS válidos
                                if (latitude != null && longitude != null && altitude != null) {
                                    coroutineScope.launch {
                                        val lang = if (language == "en") "en" else "es"
                                        val success = logRepo.addLogEntry(
                                            latitude = latitude,
                                            longitude = longitude,
                                            altitudeMeters = altitude,
                                            speedKmh = speedKmh,
                                            logText = paso.log ?: "",
                                            language = lang
                                        )

                                        if (success) {
                                            Toast.makeText(
                                                ctx,
                                                if (lang == "en") "Log entry saved" else "Entrada guardada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                ctx,
                                                if (lang == "en") "Error saving log" else "Error al guardar",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        ctx,
                                        if (language == "en") "GPS data not available" else "Datos GPS no disponibles",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onPrevious),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.anterior),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onNext),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.siguiente),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
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
    showButtons: Boolean,
    altitude: Double?,
    pressure: Float?,
    latitude: Double?,
    longitude: Double?,
    speedKmh: Float?,
    sensorRepo: SensorDataRepository,
    logRepo: LogRepository,
    language: String,
    haptic: com.taifun.checks.ui.HapticFeedbackHelper,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Si se otorgan permisos, iniciar tracking e informar al usuario
        if (permissions.values.any { it }) {
            sensorRepo.startLocationTracking()
            Toast.makeText(ctx, "Permisos otorgados. Esperando GPS... Pulse de nuevo en unos segundos.", Toast.LENGTH_LONG).show()
        }
    }

    // Función para verificar permisos de ubicación
    val hasLocationPermission = remember {
        {
            androidx.core.content.ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val pasos = checklist?.pasos.orEmpty()

    // Calcular items que caben dinámicamente
    val listState = rememberLazyListState()
    val screenHeight = configuration.screenHeightDp
    val availableHeight = screenHeight - 56 - 32 - 24 - 16 - (if (showButtons) 80 else 0) // topbar, padding, text, spacing, buttons
    val itemHeight = 80 // altura aproximada de cada card en dp
    val itemsPerPage = (availableHeight / itemHeight).coerceAtLeast(5)

    val totalPages = if (pasos.isEmpty()) 1 else ((pasos.size - 1) / itemsPerPage + 1)
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
                onBack()
            }
        }
    }

    Column(
        modifier = modifier
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                android.widget.Toast.makeText(ctx, "Cannot open link: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                                            android.widget.Toast.makeText(ctx, "App not installed: ${p.app}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e2: Exception) {
                                        android.widget.Toast.makeText(ctx, "Cannot open app: ${p.app}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(ctx, "Error launching app: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    } else {
                        CardDefaults.cardColors()
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (p.icono != null) {
                                    val iconTint = if (!checklist?.color.isNullOrBlank()) {
                                        hexToColor(checklist?.color!!)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Icon(
                                        imageVector = IconsRepo.iconFor(p.icono),
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        tint = iconTint
                                    )
                                }

                                Column {
                                    Text(
                                        text = p.texto,
                                        fontSize = 16.sp,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (optionalInfo.isNotEmpty()) {
                                        Text(
                                            text = optionalInfo.joinToString(" • "),
                                            fontSize = 12.sp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                        return@Button
                                    }

                                    // Verificar que tenemos datos GPS válidos
                                    if (latitude != null && longitude != null && altitude != null) {
                                        coroutineScope.launch {
                                            val lang = if (language == "en") "en" else "es"
                                            val success = logRepo.addLogEntry(
                                                latitude = latitude,
                                                longitude = longitude,
                                                altitudeMeters = altitude,
                                                speedKmh = speedKmh,
                                                logText = p.log ?: "",
                                                language = lang
                                            )

                                            if (success) {
                                                Toast.makeText(
                                                    ctx,
                                                    if (lang == "en") "Log entry saved" else "Entrada guardada",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    ctx,
                                                    if (lang == "en") "Error saving log" else "Error al guardar",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            ctx,
                                            if (language == "en") "GPS data not available" else "Datos GPS no disponibles",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
