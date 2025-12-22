package com.taifun.checks.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

/**
 * Data source for GPS/location data
 */
enum class GpsSource {
    INTERNAL,   // Device's internal GPS
    BLUETOOTH   // External Bluetooth GPS device
}

/**
 * Repositorio para gestionar datos de sensores (GPS, barómetro)
 * utilizados en funciones opcionales de pasos
 * Supports both internal GPS and external Bluetooth GPS sources
 */
class SensorDataRepository(private val context: Context) {

    private val locationManager: LocationManager? = try {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    } catch (e: Exception) {
        null
    }
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Current GPS source
    private val _gpsSource = MutableStateFlow(GpsSource.INTERNAL)
    val gpsSource: StateFlow<GpsSource> = _gpsSource.asStateFlow()

    // Flujos de datos
    private val _altitude = MutableStateFlow<Double?>(null)
    val altitude: StateFlow<Double?> = _altitude.asStateFlow()

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    private val _speedKmh = MutableStateFlow<Float?>(null)
    val speedKmh: StateFlow<Float?> = _speedKmh.asStateFlow()

    private val _accuracy = MutableStateFlow<Float?>(null)
    val accuracy: StateFlow<Float?> = _accuracy.asStateFlow()

    /**
     * Precisión vertical del GPS en metros (disponible desde API 26)
     * Indica la precisión de la medición de altitud
     */
    private val _verticalAccuracy = MutableStateFlow<Float?>(null)
    val verticalAccuracy: StateFlow<Float?> = _verticalAccuracy.asStateFlow()

    /**
     * Timestamp del último fix GPS (nanosegundos desde boot del sistema)
     * Se usa para calcular la antigüedad del fix y rechazar datos obsoletos
     */
    private val _lastFixElapsedRealtimeNanos = MutableStateFlow<Long?>(null)
    val lastFixElapsedRealtimeNanos: StateFlow<Long?> = _lastFixElapsedRealtimeNanos.asStateFlow()

    /**
     * Indica si la altitud fue realmente medida por el GPS
     * Android devuelve 0.0 cuando no tiene datos de altitud
     */
    private val _hasValidAltitude = MutableStateFlow(false)
    val hasValidAltitude: StateFlow<Boolean> = _hasValidAltitude.asStateFlow()

    private val _pressure = MutableStateFlow<Float?>(null)
    val pressure: StateFlow<Float?> = _pressure.asStateFlow()

    private var locationListener: LocationListener? = null
    private var pressureListener: SensorEventListener? = null

    /**
     * Set the GPS data source
     */
    fun setGpsSource(source: GpsSource) {
        _gpsSource.value = source

        // If switching to internal GPS, stop internal tracking and restart it
        // If switching to Bluetooth, stop internal tracking
        if (source == GpsSource.INTERNAL && locationListener == null) {
            startLocationTracking()
        } else if (source == GpsSource.BLUETOOTH) {
            stopLocationTracking()
        }
    }

    /**
     * Check if device has GPS hardware
     */
    fun hasGpsHardware(): Boolean {
        return locationManager?.allProviders?.any { provider ->
            provider == LocationManager.GPS_PROVIDER || provider == LocationManager.NETWORK_PROVIDER
        } ?: false
    }

    /**
     * Update GPS data from external source (e.g., Bluetooth GPS)
     * Call this method when receiving data from BluetoothGpsRepository
     */
    fun updateExternalGpsData(
        latitude: Double?,
        longitude: Double?,
        altitude: Double?,
        speedKmh: Float?
    ) {
        if (_gpsSource.value == GpsSource.BLUETOOTH) {
            _latitude.value = latitude
            _longitude.value = longitude
            _altitude.value = altitude
            _speedKmh.value = speedKmh
        }
    }

    /**
     * Update barometer/pressure data from external source (e.g., Bluetooth Vario)
     * Call this method when receiving LK8EX1 data from BluetoothGpsRepository
     * @param pressure Barometric pressure in hPa
     * @param baroAltitude Barometric altitude in meters (QNE)
     *
     * Note: Variometer data is independent from GPS source - you can have:
     * - Internal GPS + Bluetooth Variometer
     * - Bluetooth GPS + Bluetooth Variometer
     * - Internal GPS + Internal Barometer + Bluetooth Variometer (vario overrides)
     */
    fun updateExternalBarometerData(
        pressure: Float?,
        baroAltitude: Double?
    ) {
        // Always update pressure from external variometer (independent from GPS source)
        pressure?.let { _pressure.value = it }

        // If we have barometric altitude but no GPS altitude, use it
        // This is useful when there's no GPS fix
        if (baroAltitude != null && _altitude.value == null) {
            _altitude.value = baroAltitude
        }
    }

    // Lock para sincronizar acceso a locationListener
    private val locationLock = Any()

    /**
     * Inicia el seguimiento de ubicación GPS
     * Usa sincronización para evitar doble registro de listeners
     */
    fun startLocationTracking() {
        synchronized(locationLock) {
            // Check if LocationManager is available
            if (locationManager == null) return

            if (!hasLocationPermission()) return

            // Si ya hay un listener, no crear otro
            if (locationListener != null) return

            // Only start if using internal GPS
            if (_gpsSource.value != GpsSource.INTERNAL) return

            // Check if GPS hardware is available
            if (!hasGpsHardware()) return

            try {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        // Solo actualizar si seguimos usando GPS interno
                        if (_gpsSource.value == GpsSource.INTERNAL) {
                            updateFromLocation(location)
                        }
                    }

                    @Deprecated("Deprecated in API 29")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                locationListener = listener

                // Usar AMBOS proveedores simultáneamente para mayor fiabilidad
                // GPS: más preciso pero más lento
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,  // 1 segundo (más rápido)
                        0f,     // Sin distancia mínima para obtener datos más rápido
                        listener
                    )
                }

                // Network: menos preciso pero más rápido
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000L,  // 1 segundo
                        0f,     // Sin distancia mínima
                        listener
                    )
                }

                // Obtener última ubicación conocida de ambos proveedores
                // Solo usar si es reciente (menos de MAX_FIX_AGE_MS)
                val gpsLocation = try {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } catch (e: Exception) { null }

                val networkLocation = try {
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } catch (e: Exception) { null }

                // Usar la más reciente (si no es demasiado antigua)
                val lastKnown = when {
                    gpsLocation != null && networkLocation != null -> {
                        if (gpsLocation.elapsedRealtimeNanos > networkLocation.elapsedRealtimeNanos) gpsLocation else networkLocation
                    }
                    gpsLocation != null -> gpsLocation
                    networkLocation != null -> networkLocation
                    else -> null
                }

                // Solo usar lastKnown si no es demasiado antiguo
                lastKnown?.let { location ->
                    val ageNanos = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
                    val ageMs = ageNanos / 1_000_000
                    if (ageMs <= MAX_FIX_AGE_MS) {
                        updateFromLocation(location)
                    }
                    // Si es muy antiguo, no actualizar - esperar un fix fresco
                }

            } catch (e: SecurityException) {
                // Permiso denegado - limpiar listener para permitir reintentos
                locationListener = null
            }
        }
    }

    /**
     * Inicia el seguimiento del sensor de presión (barómetro)
     */
    fun startPressureTracking() {
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) ?: return

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    _pressure.value = it.values[0] // hPa
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        pressureListener = listener
        sensorManager.registerListener(
            listener,
            pressureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    /**
     * Detiene el seguimiento de ubicación
     */
    fun stopLocationTracking() {
        synchronized(locationLock) {
            locationListener?.let {
                locationManager?.removeUpdates(it)
            }
            locationListener = null
        }
    }

    /**
     * Detiene el seguimiento de presión
     */
    fun stopPressureTracking() {
        pressureListener?.let { sensorManager.unregisterListener(it) }
        pressureListener = null
    }

    /**
     * Detiene todos los seguimientos
     */
    fun stopAll() {
        stopLocationTracking()
        stopPressureTracking()
    }

    /**
     * Actualiza los StateFlows con datos de una Location
     * Valida antigüedad del fix y si la altitud fue realmente medida
     */
    private fun updateFromLocation(location: Location) {
        _latitude.value = location.latitude
        _longitude.value = location.longitude
        _lastFixElapsedRealtimeNanos.value = location.elapsedRealtimeNanos

        // Speed en Android está en m/s, convertir a km/h
        _speedKmh.value = if (location.hasSpeed()) {
            location.speed * 3.6f // m/s to km/h
        } else {
            null
        }

        // Accuracy in meters
        _accuracy.value = if (location.hasAccuracy()) {
            location.accuracy
        } else {
            null
        }

        // Vertical accuracy in meters (API 26+)
        _verticalAccuracy.value = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
            location.hasVerticalAccuracy()) {
            location.verticalAccuracyMeters
        } else {
            null
        }

        // Solo actualizar altitud si fue realmente medida
        // Android devuelve 0.0 cuando hasAltitude() es false
        if (location.hasAltitude()) {
            _altitude.value = location.altitude
            _hasValidAltitude.value = true
        } else {
            // No actualizar altitud - mantener valor anterior si lo hay
            // pero marcar como no válida si no teníamos una previa
            if (_altitude.value == null) {
                _hasValidAltitude.value = false
            }
        }
    }

    /**
     * Calcula la antigüedad del último fix GPS en milisegundos
     * @return Antigüedad en ms, o null si no hay fix registrado
     */
    fun getFixAgeMs(): Long? {
        val lastFix = _lastFixElapsedRealtimeNanos.value ?: return null
        val currentNanos = SystemClock.elapsedRealtimeNanos()
        return (currentNanos - lastFix) / 1_000_000
    }

    /**
     * Verifica si tiene permisos de ubicación
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        /**
         * Máxima antigüedad permitida para un fix GPS (60 segundos)
         * Fixes más antiguos se consideran obsoletos y no se usan
         */
        const val MAX_FIX_AGE_MS = 60_000L
    }

    /**
     * Verifica si el dispositivo tiene barómetro
     */
    fun hasBarometer(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null
    }

    /**
     * Calcula QNH (presión al nivel del mar) basado en presión actual y altitud
     * Usa la fórmula barométrica ICAO estándar
     *
     * @param pressureHPa Presión actual en hPa
     * @param altitudeMeters Altitud en metros
     * @return QNH en hPa
     */
    fun calculateQNH(pressureHPa: Float, altitudeMeters: Double): Double {
        // Fórmula ICAO: P0 = P * (1 - (L * h) / (T0 + L * h))^(-g * M / (R * L))
        // Simplificada: QNH = P * (1 + h / 44330.77)^5.255
        // Donde h es la altitud en metros

        val exponent = 5.255
        val constant = 44330.77

        return pressureHPa * (1.0 + altitudeMeters / constant).pow(exponent)
    }

    /**
     * Convierte metros a pies
     */
    fun metersToFeet(meters: Double): Double {
        return meters * 3.28084
    }

    /**
     * Convierte hPa a inHg (pulgadas de mercurio)
     */
    fun hPaToInHg(hPa: Double): Double {
        return hPa * 0.02953
    }
}
