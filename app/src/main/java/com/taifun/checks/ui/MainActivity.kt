package com.taifun.checks.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.taifun.checks.data.BluetoothGpsRepository
import com.taifun.checks.data.SensorDataRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.ui.navigation.AppNavHost
import com.taifun.checks.ui.navigation.Routes
import com.taifun.checks.ui.theme.TaifunTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var sensorDataRepo: SensorDataRepository
    private lateinit var bluetoothGpsRepo: BluetoothGpsRepository
    private lateinit var bluetoothVarioRepo: BluetoothGpsRepository  // Independent variometer device

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar edge-to-edge para compatibilidad con Android 15+
        // Scaffold de Material3 maneja automáticamente los system bars insets
        enableEdgeToEdge()

        settingsRepo = SettingsRepository(this)
        sensorDataRepo = SensorDataRepository(this)
        bluetoothGpsRepo = BluetoothGpsRepository(this)
        bluetoothVarioRepo = BluetoothGpsRepository(this)  // Second instance for variometer

        // Leer el estado de primer lanzamiento de forma síncrona
        val isFirstLaunch = settingsRepo.getFirstLaunchSync()
        val startDestination = if (isFirstLaunch) Routes.FIRST_LAUNCH else Routes.HOME

        // Configurar la UI
        setupContent(startDestination)

        // Observar configuración de pantalla encendida
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsRepo.screenOnFlow.collect { keepOn ->
                    if (keepOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }

        // Auto-conectar GPS y Variometer Bluetooth si están configurados
        setupBluetoothGpsAutoConnect()
        setupBluetoothVarioAutoConnect()

        // Reconexión periódica para GPS y Variometer Bluetooth
        setupPeriodicBluetoothGpsReconnect()
        setupPeriodicBluetoothVarioReconnect()
    }

    private fun setupBluetoothGpsAutoConnect() {
        // Observar configuración de GPS source, auto-connect y device address
        lifecycleScope.launch {
            combine(
                settingsRepo.gpsSourceFlow,
                settingsRepo.btGpsAutoConnectFlow,
                settingsRepo.btGpsDeviceAddressFlow
            ) { gpsSource, autoConnect, deviceAddress ->
                Triple(gpsSource, autoConnect, deviceAddress)
            }
                .distinctUntilChanged()
                .collect { (gpsSource, autoConnect, deviceAddress) ->
                    // Actualizar fuente de GPS en SensorDataRepository
                    if (gpsSource == "BLUETOOTH") {
                        sensorDataRepo.setGpsSource(com.taifun.checks.data.GpsSource.BLUETOOTH)

                        // Auto-conectar si está habilitado y hay un dispositivo configurado
                        if (autoConnect && !deviceAddress.isNullOrEmpty()) {
                            try {
                                bluetoothGpsRepo.connect(deviceAddress)
                            } catch (e: Exception) {
                                // Silenciar errores de auto-conexión
                            }
                        }
                    } else {
                        sensorDataRepo.setGpsSource(com.taifun.checks.data.GpsSource.INTERNAL)
                        // Desconectar Bluetooth GPS si no es la fuente activa
                        try {
                            bluetoothGpsRepo.disconnect()
                        } catch (e: Exception) {
                            // Silenciar errores
                        }
                    }
                }
        }

        // Observar datos NMEA del Bluetooth GPS y actualizar SensorDataRepository
        lifecycleScope.launch {
            bluetoothGpsRepo.nmeaData.collect { nmeaData ->
                // Actualizar GPS data
                sensorDataRepo.updateExternalGpsData(
                    latitude = nmeaData.latitude,
                    longitude = nmeaData.longitude,
                    altitude = nmeaData.altitude,
                    speedKmh = nmeaData.speedKmh
                )

                // Actualizar datos de barómetro/variometer si existen
                if (nmeaData.pressure != null || nmeaData.baroAltitude != null) {
                    sensorDataRepo.updateExternalBarometerData(
                        pressure = nmeaData.pressure,
                        baroAltitude = nmeaData.baroAltitude
                    )
                }
            }
        }
    }

    private fun setupBluetoothVarioAutoConnect() {
        // Observar configuración de Variometer: auto-connect y device address
        lifecycleScope.launch {
            combine(
                settingsRepo.btVarioAutoConnectFlow,
                settingsRepo.btVarioDeviceAddressFlow
            ) { autoConnect, deviceAddress ->
                Pair(autoConnect, deviceAddress)
            }
                .distinctUntilChanged()
                .collect { (autoConnect, deviceAddress) ->
                    // Auto-conectar variometer si está habilitado y hay un dispositivo configurado
                    if (autoConnect && !deviceAddress.isNullOrEmpty()) {
                        try {
                            bluetoothVarioRepo.connect(deviceAddress)
                        } catch (e: Exception) {
                            // Silenciar errores de auto-conexión
                        }
                    } else {
                        // Desconectar si auto-connect está deshabilitado
                        try {
                            bluetoothVarioRepo.disconnect()
                        } catch (e: Exception) {
                            // Silenciar errores
                        }
                    }
                }
        }

        // Observar datos NMEA del Variometer Bluetooth y actualizar SensorDataRepository
        // Solo actualiza datos de barómetro/variometer (presión, altitud barométrica, vario)
        lifecycleScope.launch {
            bluetoothVarioRepo.nmeaData.collect { nmeaData ->
                // Actualizar solo datos de barómetro/variometer (ignorar GPS data)
                if (nmeaData.pressure != null || nmeaData.baroAltitude != null) {
                    sensorDataRepo.updateExternalBarometerData(
                        pressure = nmeaData.pressure,
                        baroAltitude = nmeaData.baroAltitude
                    )
                }
            }
        }
    }

    /**
     * Periodic reconnection for Bluetooth GPS
     * Checks every X seconds (configurable) if GPS should be connected but isn't, and retries connection
     */
    private fun setupPeriodicBluetoothGpsReconnect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    // Read current settings
                    val gpsSource = settingsRepo.gpsSourceFlow.first()
                    val autoConnect = settingsRepo.btGpsAutoConnectFlow.first()
                    val deviceAddress = settingsRepo.btGpsDeviceAddressFlow.first()
                    val intervalSec = settingsRepo.btGpsReconnectIntervalSecFlow.first()
                    val isConnected = bluetoothGpsRepo.isConnected.value

                    // Only reconnect if:
                    // 1. GPS source is Bluetooth
                    // 2. Auto-connect is enabled
                    // 3. Device is configured
                    // 4. Currently disconnected
                    if (gpsSource == "BLUETOOTH" && autoConnect && !deviceAddress.isNullOrEmpty() && !isConnected) {
                        try {
                            bluetoothGpsRepo.connect(deviceAddress)
                        } catch (e: Exception) {
                            // Silenciar errores de reconexión
                        }
                    }

                    // Wait for the configured interval before next check
                    delay(intervalSec.toLong() * 1000L)
                }
            }
        }
    }

    /**
     * Periodic reconnection for Bluetooth Variometer
     * Checks every X seconds (configurable) if Vario should be connected but isn't, and retries connection
     */
    private fun setupPeriodicBluetoothVarioReconnect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    // Read current settings
                    val autoConnect = settingsRepo.btVarioAutoConnectFlow.first()
                    val deviceAddress = settingsRepo.btVarioDeviceAddressFlow.first()
                    val intervalSec = settingsRepo.btVarioReconnectIntervalSecFlow.first()
                    val isConnected = bluetoothVarioRepo.isConnected.value

                    // Only reconnect if:
                    // 1. Auto-connect is enabled
                    // 2. Device is configured
                    // 3. Currently disconnected
                    if (autoConnect && !deviceAddress.isNullOrEmpty() && !isConnected) {
                        try {
                            bluetoothVarioRepo.connect(deviceAddress)
                        } catch (e: Exception) {
                            // Silenciar errores de reconexión
                        }
                    }

                    // Wait for the configured interval before next check
                    delay(intervalSec.toLong() * 1000L)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desconectar Bluetooth GPS y Variometer al destruir la actividad
        lifecycleScope.launch {
            try {
                bluetoothGpsRepo.disconnect()
                bluetoothVarioRepo.disconnect()
            } catch (e: Exception) {
                // Silenciar errores de desconexión
            }
        }
    }

    private fun setupContent(startDestination: String) {
        setContent {
            val darkTheme by settingsRepo.darkFlow.collectAsState(initial = false)
            val highContrast by settingsRepo.highContrastFlow.collectAsState(initial = false)

            TaifunTheme(
                darkTheme = darkTheme,
                highContrast = highContrast
            ) {
                val nav = rememberNavController()
                AppNavHost(
                    nav = nav,
                    startDestination = startDestination,
                    sensorDataRepo = sensorDataRepo,
                    bluetoothGpsRepo = bluetoothGpsRepo,
                    bluetoothVarioRepo = bluetoothVarioRepo
                )
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Aplicar idioma antes de que se cree la Activity
        val context = applyLanguageContext(newBase)
        super.attachBaseContext(context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-aplicar el idioma cuando cambie la configuración
        applyLanguageContext(this)
        // La configuración se aplica automáticamente, no es necesario updateConfiguration (deprecado)
    }

    private fun applyLanguageContext(context: Context): Context {
        val settingsRepo = SettingsRepository(context)

        // Leer idioma de forma síncrona usando SharedPreferences
        val languageCode = settingsRepo.getLanguageSync()

        val locale = when (languageCode) {
            "es" -> Locale("es", "ES")
            "en" -> Locale("en", "US")
            else -> {
                // Auto: usar idioma del sistema, pero preferir español si no es inglés
                val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
                if (systemLocale.language == "en") {
                    Locale("en", "US")
                } else {
                    Locale("es", "ES")
                }
            }
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
