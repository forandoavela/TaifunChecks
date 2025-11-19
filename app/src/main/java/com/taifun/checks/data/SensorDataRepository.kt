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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

/**
 * Repositorio para gestionar datos de sensores (GPS, barómetro)
 * utilizados en funciones opcionales de pasos
 */
class SensorDataRepository(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Flujos de datos
    private val _altitude = MutableStateFlow<Double?>(null)
    val altitude: StateFlow<Double?> = _altitude.asStateFlow()

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    private val _speedKmh = MutableStateFlow<Float?>(null)
    val speedKmh: StateFlow<Float?> = _speedKmh.asStateFlow()

    private val _pressure = MutableStateFlow<Float?>(null)
    val pressure: StateFlow<Float?> = _pressure.asStateFlow()

    private var locationListener: LocationListener? = null
    private var pressureListener: SensorEventListener? = null

    /**
     * Inicia el seguimiento de ubicación GPS
     */
    fun startLocationTracking() {
        if (!hasLocationPermission()) return

        // Si ya hay un listener, no crear otro
        if (locationListener != null) return

        try {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    _altitude.value = location.altitude
                    _latitude.value = location.latitude
                    _longitude.value = location.longitude
                    // Speed en Android está en m/s, convertir a km/h
                    _speedKmh.value = if (location.hasSpeed()) {
                        location.speed * 3.6f // m/s to km/h
                    } else {
                        null
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
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            // Usar la más reciente
            val lastKnown = when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }

            lastKnown?.let {
                _altitude.value = it.altitude
                _latitude.value = it.latitude
                _longitude.value = it.longitude
                _speedKmh.value = if (it.hasSpeed()) it.speed * 3.6f else null
            }

        } catch (e: SecurityException) {
            // Permiso denegado
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
        locationListener?.let { locationManager.removeUpdates(it) }
        locationListener = null
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
