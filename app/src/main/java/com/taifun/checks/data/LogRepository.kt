package com.taifun.checks.data

import android.content.Context
import android.os.Build
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.BufferedReader
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Representa una entrada en el log de vuelo
 */
data class LogEntry(
    val utcTime: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val icaoCode: String?,  // null si no hay aeródromo cercano
    val logText: String
)

/**
 * Repositorio para gestionar el log de vuelo en formato CSV
 * Maneja escritura y lectura de entradas de log
 */
class LogRepository(private val context: Context) {

    private val aerodromeRepository = AerodromeRepository(context)

    // Formato de fecha ISO 8601 para UTC
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Obtiene el archivo CSV de log en Android/media
     * El archivo se guarda aquí para que sea accesible por el usuario
     * No requiere permisos especiales y es visible vía USB
     * Los archivos persisten después de desinstalar la app
     */
    private fun getLogFile(): File {
        // Usar Android/media (accesible por el usuario, disponible desde Android 10/API 29)
        // Los archivos persisten después de desinstalar la app
        val mediaDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.externalMediaDirs.firstOrNull()?.let { mediaDir ->
                File(mediaDir, "logs").apply { mkdirs() }
            }
        } else {
            // Fallback para Android 9 y anteriores: usar external files
            context.getExternalFilesDirs(null).firstOrNull()?.let { externalDir ->
                File(externalDir, "logs").apply { mkdirs() }
            }
        }

        // Fallback a directorio interno si external media no está disponible
        val appDir = if (mediaDir != null && mediaDir.exists()) {
            mediaDir
        } else {
            val fallbackBase = context.getExternalFilesDir(null) ?: context.filesDir
            File(fallbackBase, "logs").apply { mkdirs() }
        }

        return File(appDir, "flight_log.csv")
    }

    /**
     * Verifica si el archivo de log existe
     */
    fun logFileExists(): Boolean {
        return getLogFile().exists()
    }

    /**
     * Crea el archivo CSV con headers si no existe
     * @param language "es" o "en" para determinar idioma de headers
     */
    private suspend fun ensureLogFileExists(language: String = "es") = withContext(Dispatchers.IO) {
        val file = getLogFile()

        if (!file.exists()) {
            file.parentFile?.mkdirs()

            val headers = if (language == "en") {
                "UTC Time;Latitude;Longitude;Altitude (m);ICAO;Text"
            } else {
                "Hora UTC;Latitud;Longitud;Altitud (m);OACI;Texto"
            }

            FileWriter(file, false).use { writer ->
                writer.write(headers)
                writer.write("\n")
            }
        }
    }

    /**
     * Añade una entrada al log de vuelo
     *
     * @param latitude Latitud GPS
     * @param longitude Longitud GPS
     * @param altitudeMeters Altitud GPS en metros
     * @param speedKmh Velocidad GPS en km/h
     * @param logText Texto del log configurado en el paso
     * @param language Idioma para headers ("es" o "en")
     * @return true si se guardó correctamente
     */
    suspend fun addLogEntry(
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double,
        speedKmh: Float?,
        logText: String,
        language: String = "es"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Asegurar que existe el archivo con headers correctos
            ensureLogFileExists(language)

            // Obtener timestamp UTC
            val utcTime = dateFormat.format(Date())

            // Detectar aeródromo solo si velocidad < 40 km/h
            val icaoCode = if (speedKmh != null && speedKmh < 40f) {
                aerodromeRepository.findNearestAerodrome(latitude, longitude, maxDistanceKm = 2.0)
            } else {
                null
            }

            // Crear entrada
            val entry = LogEntry(
                utcTime = utcTime,
                latitude = latitude,
                longitude = longitude,
                altitudeMeters = altitudeMeters,
                icaoCode = icaoCode,
                logText = logText
            )

            // Escribir al archivo CSV
            FileWriter(getLogFile(), true).use { writer ->
                val line = buildString {
                    append(entry.utcTime)
                    append(";")
                    append(String.format(Locale.US, "%.6f", entry.latitude))
                    append(";")
                    append(String.format(Locale.US, "%.6f", entry.longitude))
                    append(";")
                    append(String.format(Locale.US, "%.1f", entry.altitudeMeters))
                    append(";")
                    append(entry.icaoCode ?: "")
                    append(";")
                    append(entry.logText.replace(";", ",")) // Escapar punto y coma en texto
                }
                writer.write(line)
                writer.write("\n")
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Lee todas las entradas del log
     * @return Lista de entradas ordenadas cronológicamente
     */
    suspend fun readAllEntries(): List<LogEntry> = withContext(Dispatchers.IO) {
        val file = getLogFile()
        if (!file.exists()) return@withContext emptyList()

        try {
            val entries = mutableListOf<LogEntry>()

            BufferedReader(FileReader(file)).use { reader ->
                reader.readLine() // Skip header

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let {
                        val parts = it.split(";")
                        if (parts.size >= 6) {
                            try {
                                entries.add(
                                    LogEntry(
                                        utcTime = parts[0],
                                        latitude = parts[1].toDouble(),
                                        longitude = parts[2].toDouble(),
                                        altitudeMeters = parts[3].toDouble(),
                                        icaoCode = parts[4].ifBlank { null },
                                        logText = parts[5]
                                    )
                                )
                            } catch (e: NumberFormatException) {
                                // Skip invalid entries
                            }
                        }
                    }
                }
            }

            entries
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene la ruta del archivo de log para compartir/exportar
     */
    fun getLogFilePath(): String {
        return getLogFile().absolutePath
    }

    /**
     * Elimina todas las entradas del log
     */
    suspend fun clearLog(): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getLogFile()
            file.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Elimina una entrada específica del log por su índice (0-based)
     * @param index Índice de la entrada a eliminar
     * @return true si se eliminó correctamente
     */
    suspend fun deleteEntry(index: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val entries = readAllEntries()
            if (index < 0 || index >= entries.size) return@withContext false

            // Crear nueva lista sin la entrada eliminada
            val newEntries = entries.filterIndexed { i, _ -> i != index }

            // Reescribir el archivo
            val file = getLogFile()
            file.delete()

            if (newEntries.isNotEmpty()) {
                // Recrear con headers
                ensureLogFileExists()

                // Escribir todas las entradas excepto la eliminada
                FileWriter(file, true).use { writer ->
                    newEntries.forEach { entry ->
                        val line = buildString {
                            append(entry.utcTime)
                            append(";")
                            append(String.format(Locale.US, "%.6f", entry.latitude))
                            append(";")
                            append(String.format(Locale.US, "%.6f", entry.longitude))
                            append(";")
                            append(String.format(Locale.US, "%.1f", entry.altitudeMeters))
                            append(";")
                            append(entry.icaoCode ?: "")
                            append(";")
                            append(entry.logText.replace(";", ","))
                        }
                        writer.write(line)
                        writer.write("\n")
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene el número de entradas en el log
     */
    suspend fun getEntryCount(): Int = withContext(Dispatchers.IO) {
        val file = getLogFile()
        if (!file.exists()) return@withContext 0

        try {
            BufferedReader(FileReader(file)).use { reader ->
                reader.readLine() // Skip header
                var count = 0
                while (reader.readLine() != null) {
                    count++
                }
                count
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Exporta el CSV a una ubicación elegida por el usuario
     * @param uri Uri de destino elegida por el usuario
     * @return true si se exportó correctamente
     */
    suspend fun exportToUri(uri: android.net.Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getLogFile()
            if (!file.exists()) return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Resultado de crear el intent de compartir con información de error
     */
    data class ShareIntentResult(
        val intent: android.content.Intent?,
        val errorMessage: String?
    )

    /**
     * Crea un Intent para compartir el archivo de log
     * Usa FileProvider para acceso seguro al archivo
     */
    fun createShareIntent(): ShareIntentResult {
        val file = getLogFile()
        if (!file.exists()) {
            val msg = "Archivo no existe en: ${file.absolutePath}"
            android.util.Log.e("LogRepository", "createShareIntent: $msg")
            return ShareIntentResult(null, msg)
        }

        try {
            // Verificar que el archivo tenga contenido
            if (file.length() == 0L) {
                val msg = "Archivo está vacío"
                android.util.Log.e("LogRepository", "createShareIntent: $msg")
                return ShareIntentResult(null, msg)
            }

            // Copiar a cache para compartir
            val cacheFile = File(context.cacheDir, "flight_log.csv")
            file.copyTo(cacheFile, overwrite = true)

            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                val msg = "Fallo al copiar a cache"
                android.util.Log.e("LogRepository", "createShareIntent: $msg")
                return ShareIntentResult(null, msg)
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Flight Log")
                putExtra(android.content.Intent.EXTRA_TEXT, "Flight log CSV file")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            return ShareIntentResult(intent, null)
        } catch (e: Exception) {
            val msg = "Excepción: ${e.message}"
            android.util.Log.e("LogRepository", "createShareIntent: $msg", e)
            e.printStackTrace()
            return ShareIntentResult(null, msg)
        }
    }

    /**
     * Importa un archivo CSV y sobrescribe el log actual
     * @param uri Uri del archivo CSV a importar
     * @return true si se importó correctamente
     */
    suspend fun importFromCSV(uri: android.net.Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
            val tempFile = File(context.cacheDir, "temp_import.csv")

            // Copiar contenido a archivo temporal
            inputStream.use { input ->
                java.io.FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Validar formato del CSV
            val lines = tempFile.readLines()
            if (lines.isEmpty()) return@withContext false

            // Eliminar log actual
            val currentFile = getLogFile()
            currentFile.delete()

            // Copiar archivo importado al log
            tempFile.copyTo(currentFile, overwrite = true)
            tempFile.delete()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Edita una entrada específica del log
     * @param index Índice de la entrada a editar (0-based)
     * @param newEntry Nueva entrada con los datos actualizados
     * @return true si se editó correctamente
     */
    suspend fun editEntry(index: Int, newEntry: LogEntry): Boolean = withContext(Dispatchers.IO) {
        try {
            val entries = readAllEntries().toMutableList()
            if (index < 0 || index >= entries.size) return@withContext false

            // Actualizar la entrada
            entries[index] = newEntry

            // Reescribir el archivo
            val file = getLogFile()
            file.delete()

            // Recrear con headers
            ensureLogFileExists()

            // Escribir todas las entradas
            FileWriter(file, true).use { writer ->
                entries.forEach { entry ->
                    val line = buildString {
                        append(entry.utcTime)
                        append(";")
                        append(String.format(Locale.US, "%.6f", entry.latitude))
                        append(";")
                        append(String.format(Locale.US, "%.6f", entry.longitude))
                        append(";")
                        append(String.format(Locale.US, "%.1f", entry.altitudeMeters))
                        append(";")
                        append(entry.icaoCode ?: "")
                        append(";")
                        append(entry.logText.replace(";", ","))
                    }
                    writer.write(line)
                    writer.write("\n")
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
