package com.taifun.checks.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.taifun.checks.data.nmea.NmeaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

/**
 * Repository for managing Bluetooth GPS connections and NMEA data streaming
 * Supports external GPS devices that transmit NMEA sentences via Bluetooth SPP
 */
class BluetoothGpsRepository(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothGpsRepository"
        // Standard UUID for Serial Port Profile (SPP)
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    // Connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

    // GPS data (accumulated from multiple NMEA sentences)
    private val _nmeaData = MutableStateFlow(NmeaParser.NmeaData())
    val nmeaData: StateFlow<NmeaParser.NmeaData> = _nmeaData.asStateFlow()

    // Last raw NMEA sentences received (for debugging) - keeps last 20
    private val _rawNmeaSentences = MutableStateFlow<List<String>>(emptyList())
    val rawNmeaSentences: StateFlow<List<String>> = _rawNmeaSentences.asStateFlow()

    private var bluetoothSocket: BluetoothSocket? = null
    private var readJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Check if Bluetooth is available and enabled on this device
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    /**
     * Check if necessary Bluetooth permissions are granted
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires BLUETOOTH_CONNECT
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Older versions use BLUETOOTH
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get list of paired Bluetooth devices
     * Returns empty list if permissions not granted
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermissions() || !isBluetoothAvailable()) {
            return emptyList()
        }

        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException getting paired devices", e)
            emptyList()
        }
    }

    /**
     * Connect to a Bluetooth GPS device by MAC address
     * @param deviceAddress MAC address of the Bluetooth device (e.g., "00:11:22:33:44:55")
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(deviceAddress: String) = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermissions()) {
            _connectionStatus.value = "Bluetooth permission not granted"
            return@withContext
        }

        if (!isBluetoothAvailable()) {
            _connectionStatus.value = "Bluetooth not available or disabled"
            return@withContext
        }

        // Disconnect existing connection
        disconnect()

        try {
            _connectionStatus.value = "Connecting to $deviceAddress..."
            Log.d(TAG, "Attempting to connect to $deviceAddress")

            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            if (device == null) {
                _connectionStatus.value = "Device not found"
                return@withContext
            }

            // Create socket
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            val socket = bluetoothSocket ?: return@withContext

            // Cancel discovery to speed up connection
            bluetoothAdapter.cancelDiscovery()

            // Connect (blocking call)
            socket.connect()

            _isConnected.value = true
            _connectionStatus.value = "Connected to ${device.name ?: deviceAddress}"
            Log.d(TAG, "Connected to ${device.name}")

            // Start reading NMEA data
            startReading(socket)

        } catch (e: IOException) {
            Log.e(TAG, "Connection failed", e)
            _connectionStatus.value = "Connection failed: ${e.message}"
            _isConnected.value = false
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during connection", e)
            _connectionStatus.value = "Permission error"
            _isConnected.value = false
        }
    }

    /**
     * Start reading NMEA sentences from the Bluetooth socket
     */
    private fun startReading(socket: BluetoothSocket) {
        readJob?.cancel()
        readJob = coroutineScope.launch {
            var reader: BufferedReader? = null
            try {
                val inputStream = socket.inputStream
                reader = BufferedReader(InputStreamReader(inputStream))

                Log.d(TAG, "Started reading NMEA data")
                var accumulatedData = NmeaParser.NmeaData()

                while (isActive && socket.isConnected) {
                    try {
                        val line = reader.readLine()
                        if (line != null && line.isNotEmpty()) {
                            // Add to raw sentences list (keep last 20)
                            val currentList = _rawNmeaSentences.value.toMutableList()
                            currentList.add(line)
                            if (currentList.size > 20) {
                                currentList.removeAt(0)
                            }
                            _rawNmeaSentences.value = currentList

                            // Parse sentence (NMEA or BlueFlyVario)
                            val parsedData = NmeaParser.parse(line)
                            if (parsedData != null) {
                                // Merge with accumulated data
                                accumulatedData = NmeaParser.merge(accumulatedData, parsedData)

                                // Update flow if we have GPS fix OR variometer data
                                val hasGpsFix = NmeaParser.isValidFix(accumulatedData)
                                val hasVarioData = accumulatedData.pressure != null ||
                                                   accumulatedData.vario != null ||
                                                   accumulatedData.baroAltitude != null

                                if (hasGpsFix || hasVarioData) {
                                    _nmeaData.value = accumulatedData

                                    if (hasGpsFix) {
                                        Log.d(TAG, "GPS fix: lat=${accumulatedData.latitude}, " +
                                                  "lon=${accumulatedData.longitude}, " +
                                                  "alt=${accumulatedData.altitude}m")
                                    }
                                    if (hasVarioData) {
                                        Log.d(TAG, "Variometer data: pressure=${accumulatedData.pressure} hPa, " +
                                                  "vario=${accumulatedData.vario} m/s, " +
                                                  "temp=${accumulatedData.temperature}°C")
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        if (isActive) {
                            Log.e(TAG, "Error reading data", e)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in read loop", e)
            } finally {
                // Cerrar el BufferedReader para evitar memory leak
                try {
                    reader?.close()
                } catch (e: IOException) {
                    Log.w(TAG, "Error closing reader", e)
                }
                if (_isConnected.value) {
                    _connectionStatus.value = "Connection lost"
                    _isConnected.value = false
                }
            }
        }
    }

    /**
     * Disconnect from current Bluetooth GPS device
     */
    fun disconnect() {
        readJob?.cancel()
        readJob = null

        bluetoothSocket?.let {
            try {
                it.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing socket", e)
            }
        }
        bluetoothSocket = null

        _isConnected.value = false
        _connectionStatus.value = null
        _nmeaData.value = NmeaParser.NmeaData()
        _rawNmeaSentences.value = emptyList()

        Log.d(TAG, "Disconnected")
    }

    /**
     * Attempt to auto-reconnect to a device
     * Useful for restoring connection after interruption
     */
    suspend fun autoReconnect(deviceAddress: String, maxAttempts: Int = 3) {
        repeat(maxAttempts) { attempt ->
            if (_isConnected.value) return

            Log.d(TAG, "Auto-reconnect attempt ${attempt + 1}/$maxAttempts")
            _connectionStatus.value = "Reconnecting... (${attempt + 1}/$maxAttempts)"

            connect(deviceAddress)

            if (_isConnected.value) {
                Log.d(TAG, "Auto-reconnect successful")
                return
            }

            if (attempt < maxAttempts - 1) {
                delay(2000L * (attempt + 1)) // Exponential backoff
            }
        }

        _connectionStatus.value = "Auto-reconnect failed"
        Log.w(TAG, "Auto-reconnect failed after $maxAttempts attempts")
    }

    /**
     * Clean up resources
     * Must be called when the repository is no longer needed to prevent memory leaks
     */
    fun cleanup() {
        disconnect()
        // Cancelar el scope para liberar recursos de coroutines
        coroutineScope.cancel()
    }
}
