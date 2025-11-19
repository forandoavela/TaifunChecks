package com.taifun.checks.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.*

/**
 * Representa un aeródromo con su identificador y coordenadas
 * El identificador puede ser un código ICAO oficial (ej: LEMD)
 * o un identificador basado en nombre (ej: ES_FUENTEMILANOS)
 */
data class Aerodrome(
    val identifier: String,
    val latitude: Double,
    val longitude: Double,
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
     * Carga la base de datos de aeródromos desde assets
     * Solo se carga una vez en memoria
     */
    private fun loadAerodromes() {
        if (isLoaded) return

        try {
            val inputStream = context.assets.open("aerodromes.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            aerodromes = reader.useLines { lines ->
                lines.drop(1) // Skip header
                    .mapNotNull { line ->
                        val parts = line.split(",")
                        if (parts.size >= 3) {
                            try {
                                Aerodrome(
                                    identifier = parts[0].trim(),
                                    latitude = parts[1].trim().toDouble(),
                                    longitude = parts[2].trim().toDouble(),
                                    source = if (parts.size >= 4) parts[3].trim() else "Unknown"
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
            aerodromes = emptyList()
        }
    }

    /**
     * Encuentra el aeródromo más cercano a la posición dada
     * Solo retorna si está dentro del radio máximo (2 km por defecto)
     *
     * @param latitude Latitud actual
     * @param longitude Longitud actual
     * @param maxDistanceKm Distancia máxima en km (default: 2 km)
     * @return Identificador del aeródromo más cercano (código ICAO o nombre) o null si no hay ninguno cercano
     */
    fun findNearestAerodrome(
        latitude: Double,
        longitude: Double,
        maxDistanceKm: Double = 2.0
    ): String? {
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

        // Solo retornar si está dentro del radio máximo
        return if (minDistance <= maxDistanceKm) {
            nearestAerodrome?.identifier
        } else {
            null
        }
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
