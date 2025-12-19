package com.taifun.checks.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import kotlin.math.*

/**
 * Representa un aeródromo con su identificador, coordenadas, elevación, nombre y tipo.
 * El identificador puede ser un código ICAO oficial (ej: LEMD)
 * o un identificador basado en nombre (ej: ES_FUENTEMILANOS)
 *
 * @property identifier Código ICAO o identificador único del aeródromo
 * @property latitude Latitud en grados decimales
 * @property longitude Longitud en grados decimales
 * @property elevationMeters Elevación del aeródromo en metros (puede ser null)
 * @property name Nombre del aeródromo (ej: "Madrid Barajas International Airport")
 * @property typeEn Tipo en inglés (aerodrome, airport, heliport, seaplane base, balloonport)
 * @property typeEs Tipo en español (aeródromo, aeropuerto, helipuerto, hidrobase, globopuerto)
 * @property source Fuente de datos (generalmente "OurAirports")
 */
data class Aerodrome(
    val identifier: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val name: String = "",
    val typeEn: String = "aerodrome",
    val typeEs: String = "aeródromo",
    val source: String = "Unknown"
)

/**
 * Repositorio para gestionar la base de datos de aeródromos
 * y encontrar aeródromos cercanos basados en GPS
 */
class AerodromeRepository(private val context: Context) {

    private var aerodromes: List<Aerodrome> = emptyList()
    private var isLoaded = false

    /**
     * Carga la base de datos de aeródromos desde assets (formato gzip)
     * Solo se carga una vez en memoria
     */
    private fun loadAerodromes() {
        if (isLoaded) return

        try {
            // Usar GZIPInputStream para leer archivo comprimido desde res/raw/ (reduce tamaño APK en ~70%)
            // Movido de assets/ a res/raw/ para evitar que R8 resource shrinking lo elimine
            val inputStream = GZIPInputStream(context.resources.openRawResource(com.taifun.checks.R.raw.aerodromes_db))
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Nuevo formato CSV: identifier,latitude,longitude,elevation_m,name,type_en,type_es,source
            aerodromes = reader.useLines { lines ->
                lines.drop(1) // Skip header
                    .mapNotNull { line ->
                        val parts = line.split(",")
                        if (parts.size >= 4) {
                            try {
                                Aerodrome(
                                    identifier = parts[0].trim(),
                                    latitude = parts[1].trim().toDouble(),
                                    longitude = parts[2].trim().toDouble(),
                                    elevationMeters = parts[3].trim().toDoubleOrNull(),
                                    name = if (parts.size >= 5) parts[4].trim() else "",
                                    typeEn = if (parts.size >= 6) parts[5].trim() else "aerodrome",
                                    typeEs = if (parts.size >= 7) parts[6].trim() else "aeródromo",
                                    source = if (parts.size >= 8) parts[7].trim() else "OurAirports"
                                )
                            } catch (e: NumberFormatException) {
                                null // Skip invalid entries
                            }
                        } else {
                            null
                        }
                    }
                    .toList()
            }

            isLoaded = true
        } catch (e: Exception) {
            // Error loading database, keep empty list
            android.util.Log.e("AerodromeRepo", "Failed to load aerodrome database", e)
            aerodromes = emptyList()
        }
    }

    /**
     * Encuentra el aeródromo más cercano a la posición dada
     * Solo retorna si está dentro del radio máximo (2 km por defecto) Y
     * la diferencia de altitud es menor al umbral especificado (50m por defecto)
     *
     * @param latitude Latitud actual
     * @param longitude Longitud actual
     * @param altitudeMeters Altitud GPS actual en metros
     * @param maxDistanceKm Distancia máxima en km (default: 2 km)
     * @param maxAltitudeDifferenceM Diferencia máxima de altitud en metros (default: 50m)
     * @return Objeto Aerodrome completo con nombre y tipo, o null si no hay ninguno cercano
     */
    fun findNearestAerodrome(
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double?,
        maxDistanceKm: Double = 2.0,
        maxAltitudeDifferenceM: Double = 50.0
    ): Aerodrome? {
        loadAerodromes()

        if (aerodromes.isEmpty()) return null

        var nearestAerodrome: Aerodrome? = null
        var minDistance = Double.MAX_VALUE

        for (aerodrome in aerodromes) {
            val distance = calculateDistance(
                latitude, longitude,
                aerodrome.latitude, aerodrome.longitude
            )

            if (distance < minDistance) {
                minDistance = distance
                nearestAerodrome = aerodrome
            }
        }

        // Solo retornar si:
        // 1. Está dentro del radio máximo horizontal (2 km)
        // 2. Si tenemos altitud GPS y elevación del aeródromo, la diferencia debe ser < 50m
        if (minDistance <= maxDistanceKm) {
            val aerodrome = nearestAerodrome ?: return null

            // Si tenemos ambas altitudes, verificar diferencia vertical
            if (altitudeMeters != null && aerodrome.elevationMeters != null) {
                val altitudeDifference = kotlin.math.abs(altitudeMeters - aerodrome.elevationMeters)
                return if (altitudeDifference <= maxAltitudeDifferenceM) {
                    aerodrome
                } else {
                    null
                }
            } else {
                // Si no tenemos altitud, usar solo criterio horizontal (comportamiento legacy)
                return aerodrome
            }
        }

        return null
    }

    /**
     * Calcula la distancia entre dos coordenadas GPS usando la fórmula de Haversine
     *
     * @param lat1 Latitud punto 1
     * @param lon1 Longitud punto 1
     * @param lat2 Latitud punto 2
     * @param lon2 Longitud punto 2
     * @return Distancia en kilómetros
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    /**
     * Retorna el número de aeródromos cargados en la base de datos
     */
    fun getAerodromeCount(): Int {
        loadAerodromes()
        return aerodromes.size
    }
}
