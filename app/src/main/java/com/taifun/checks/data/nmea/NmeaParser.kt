package com.taifun.checks.data.nmea

import android.util.Log
import kotlin.math.abs

/**
 * Parser for NMEA 0183 GPS messages and variometer data
 * Supports common sentence types: GPGGA, GPRMC, GPGLL, GPGSA, GPGSV, LK8EX1
 * Also supports GNSS variants (GN*, GL*, GA*)
 */
object NmeaParser {

    private const val TAG = "NmeaParser"

    /**
     * Parsed NMEA data container
     */
    data class NmeaData(
        // GPS data
        val latitude: Double? = null,
        val longitude: Double? = null,
        val altitude: Double? = null,  // meters above sea level (GPS)
        val speedKmh: Float? = null,
        val fixQuality: Int? = null,    // 0=invalid, 1=GPS, 2=DGPS
        val satellitesUsed: Int? = null,
        val hdop: Float? = null,        // Horizontal Dilution of Precision
        val timestamp: String? = null,   // HHMMSS.SSS format

        // Variometer/Barometer data (LK8EX1)
        val pressure: Float? = null,     // hPa (barometric pressure)
        val baroAltitude: Double? = null,  // meters (barometric altitude, QNE)
        val vario: Float? = null,        // m/s (climb/sink rate)
        val temperature: Float? = null,  // °C
        val battery: Float? = null       // voltage or percentage
    )

    /**
     * Parse a single NMEA sentence
     * @param sentence Raw NMEA sentence (e.g., "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47")
     * @return NmeaData with available fields populated, or null if invalid
     */
    fun parse(sentence: String): NmeaData? {
        if (!isValidChecksum(sentence)) {
            Log.w(TAG, "Invalid checksum: $sentence")
            return null
        }

        // Remove checksum and split by comma
        val parts = sentence.substringBefore('*').split(',')
        if (parts.isEmpty()) return null

        val messageType = parts[0].trimStart('$')

        return when {
            messageType.endsWith("GGA") -> parseGGA(parts)
            messageType.endsWith("RMC") -> parseRMC(parts)
            messageType.endsWith("GLL") -> parseGLL(parts)
            messageType == "LK8EX1" -> {
                Log.d(TAG, "Parsing LK8EX1: $sentence")
                parseLK8EX1(parts)
            }
            else -> {
                Log.v(TAG, "Ignoring message type: $messageType")
                null // Ignore other message types for now
            }
        }
    }

    /**
     * Parse GPGGA (Global Positioning System Fix Data)
     * Format: $GPGGA,time,lat,N/S,lon,E/W,quality,sats,hdop,alt,M,geoid,M,age,stnID*checksum
     * Example: $GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47
     */
    private fun parseGGA(parts: List<String>): NmeaData? {
        if (parts.size < 15) return null

        val time = parts.getOrNull(1)?.takeIf { it.isNotEmpty() }
        val lat = parseLatitude(parts.getOrNull(2), parts.getOrNull(3))
        val lon = parseLongitude(parts.getOrNull(4), parts.getOrNull(5))
        val quality = parts.getOrNull(6)?.toIntOrNull()
        val sats = parts.getOrNull(7)?.toIntOrNull()
        val hdop = parts.getOrNull(8)?.toFloatOrNull()
        val alt = parts.getOrNull(9)?.toDoubleOrNull()

        return NmeaData(
            latitude = lat,
            longitude = lon,
            altitude = alt,
            fixQuality = quality,
            satellitesUsed = sats,
            hdop = hdop,
            timestamp = time
        )
    }

    /**
     * Parse GPRMC (Recommended Minimum Specific GPS/Transit Data)
     * Format: $GPRMC,time,status,lat,N/S,lon,E/W,speed,course,date,mag,E/W,mode*checksum
     * Example: $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A
     */
    private fun parseRMC(parts: List<String>): NmeaData? {
        if (parts.size < 12) return null

        val time = parts.getOrNull(1)?.takeIf { it.isNotEmpty() }
        val status = parts.getOrNull(2) // A=active, V=void
        if (status != "A") return null // Only process active fixes

        val lat = parseLatitude(parts.getOrNull(3), parts.getOrNull(4))
        val lon = parseLongitude(parts.getOrNull(5), parts.getOrNull(6))
        val speedKnots = parts.getOrNull(7)?.toFloatOrNull()
        val speedKmh = speedKnots?.let { it * 1.852f } // knots to km/h

        return NmeaData(
            latitude = lat,
            longitude = lon,
            speedKmh = speedKmh,
            timestamp = time
        )
    }

    /**
     * Parse GPGLL (Geographic Position - Latitude/Longitude)
     * Format: $GPGLL,lat,N/S,lon,E/W,time,status,mode*checksum
     * Example: $GPGLL,4807.038,N,01131.000,E,123519,A,A*5C
     */
    private fun parseGLL(parts: List<String>): NmeaData? {
        if (parts.size < 7) return null

        val status = parts.getOrNull(6) // A=active, V=void
        if (status != "A") return null

        val lat = parseLatitude(parts.getOrNull(1), parts.getOrNull(2))
        val lon = parseLongitude(parts.getOrNull(3), parts.getOrNull(4))
        val time = parts.getOrNull(5)?.takeIf { it.isNotEmpty() }

        return NmeaData(
            latitude = lat,
            longitude = lon,
            timestamp = time
        )
    }

    /**
     * Parse LK8EX1 (Variometer/Barometer data)
     * Format: $LK8EX1,pressure,altitude,vario,temperature,battery,*checksum
     * Example: $LK8EX1,101325,430,0.4,28.5,4.2,*XX
     *
     * Field 0: Raw Pressure in hPa*100 (e.g., 1013.25 → 101325), or 999999 if not available
     * Field 1: Altitude in meters (QNE, relative to 1013.25 hPa), ignored if pressure available
     * Field 2: Vario (climb/sink rate in m/s)
     * Field 3: Temperature (°C)
     * Field 4: Battery (voltage)
     *
     * Used by Bluetooth variometers: BlueFlyVario, XCTracer, Blues II, BlueBip, etc.
     */
    private fun parseLK8EX1(parts: List<String>): NmeaData? {
        if (parts.size < 6) {
            Log.w(TAG, "LK8EX1 too few fields: ${parts.size}, parts=$parts")
            return null
        }

        Log.d(TAG, "LK8EX1 parts: $parts")

        // Parse pressure (hPa*100)
        val pressureRaw = parts.getOrNull(1)?.toIntOrNull()
        val pressure = if (pressureRaw != null && pressureRaw != 999999) {
            pressureRaw / 100.0f  // Convert back to hPa
        } else {
            null
        }

        // Parse barometric altitude (only use if pressure not available)
        val baroAlt = if (pressure == null) {
            parts.getOrNull(2)?.toDoubleOrNull()?.takeIf { it != 99999.0 }
        } else {
            parts.getOrNull(2)?.toDoubleOrNull()  // Parse but may be ignored
        }

        // Parse vario (m/s)
        val vario = parts.getOrNull(3)?.toFloatOrNull()

        // Parse temperature (°C)
        val temperature = parts.getOrNull(4)?.toFloatOrNull()

        // Parse battery (voltage)
        val battery = parts.getOrNull(5)?.toFloatOrNull()

        val result = NmeaData(
            pressure = pressure,
            baroAltitude = baroAlt,
            vario = vario,
            temperature = temperature,
            battery = battery
        )

        Log.d(TAG, "LK8EX1 parsed: pressure=$pressure hPa, baroAlt=$baroAlt m, vario=$vario m/s, temp=$temperature°C, battery=$battery")

        return result
    }

    /**
     * Parse latitude from NMEA format (DDMM.MMMM,N/S) to decimal degrees
     * Example: "4807.038,N" -> 48.1173
     */
    private fun parseLatitude(value: String?, hemisphere: String?): Double? {
        if (value.isNullOrEmpty() || hemisphere.isNullOrEmpty()) return null

        return try {
            val degrees = value.substring(0, 2).toDouble()
            val minutes = value.substring(2).toDouble()
            val decimal = degrees + (minutes / 60.0)

            if (hemisphere == "S") -decimal else decimal
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse longitude from NMEA format (DDDMM.MMMM,E/W) to decimal degrees
     * Example: "01131.000,E" -> 11.5167
     */
    private fun parseLongitude(value: String?, hemisphere: String?): Double? {
        if (value.isNullOrEmpty() || hemisphere.isNullOrEmpty()) return null

        return try {
            val degrees = value.substring(0, 3).toDouble()
            val minutes = value.substring(3).toDouble()
            val decimal = degrees + (minutes / 60.0)

            if (hemisphere == "W") -decimal else decimal
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validate NMEA checksum
     * NMEA sentences end with *XX where XX is the XOR of all bytes between $ and *
     * Example: $GPGGA,123519...*47
     */
    private fun isValidChecksum(sentence: String): Boolean {
        if (!sentence.startsWith('$') || !sentence.contains('*')) {
            return false
        }

        try {
            // Extract checksum
            val checksumHex = sentence.substringAfter('*').take(2)
            val expectedChecksum = checksumHex.toInt(16)

            // Calculate XOR of all bytes between $ and *
            val data = sentence.substring(1, sentence.indexOf('*'))
            var calculatedChecksum = 0
            data.forEach { calculatedChecksum = calculatedChecksum xor it.code }

            return calculatedChecksum == expectedChecksum
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Merge multiple NmeaData objects, preferring non-null values
     * Useful for combining data from different sentence types (e.g., GGA + RMC + LK8EX1)
     */
    fun merge(existing: NmeaData, new: NmeaData): NmeaData {
        return NmeaData(
            latitude = new.latitude ?: existing.latitude,
            longitude = new.longitude ?: existing.longitude,
            altitude = new.altitude ?: existing.altitude,
            speedKmh = new.speedKmh ?: existing.speedKmh,
            fixQuality = new.fixQuality ?: existing.fixQuality,
            satellitesUsed = new.satellitesUsed ?: existing.satellitesUsed,
            hdop = new.hdop ?: existing.hdop,
            timestamp = new.timestamp ?: existing.timestamp,
            pressure = new.pressure ?: existing.pressure,
            baroAltitude = new.baroAltitude ?: existing.baroAltitude,
            vario = new.vario ?: existing.vario,
            temperature = new.temperature ?: existing.temperature,
            battery = new.battery ?: existing.battery
        )
    }

    /**
     * Check if GPS fix is valid (has position and acceptable quality)
     */
    fun isValidFix(data: NmeaData): Boolean {
        return data.latitude != null &&
               data.longitude != null &&
               (data.fixQuality == null || data.fixQuality > 0) &&
               abs(data.latitude) <= 90.0 &&
               abs(data.longitude) <= 180.0
    }
}
