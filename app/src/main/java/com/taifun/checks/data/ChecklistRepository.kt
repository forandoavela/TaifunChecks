package com.taifun.checks.data

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.taifun.checks.data.yaml.YamlIO
import com.taifun.checks.data.yaml.YamlParseException
import com.taifun.checks.domain.Catalogo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ChecklistRepository(private val context: Context) {

    private val defaultFileName = "Taifun17E_ES.yaml"

    /**
     * Obtiene el directorio para almacenar archivos YAML
     * Usa Android/media para que sea accesible por el usuario
     * Los archivos persisten después de desinstalar la app
     * Fallback a filesDir si no está disponible
     */
    private fun getStorageDir(): File {
        // Usar Android/media (accesible por el usuario, disponible desde Android 10/API 29)
        // Los archivos persisten después de desinstalar la app
        val mediaDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.externalMediaDirs.firstOrNull()?.let { mediaDir ->
                File(mediaDir, "checklists").apply { mkdirs() }
            }
        } else {
            // Fallback para Android 9 y anteriores: usar external files
            context.getExternalFilesDirs(null).firstOrNull()?.let { externalDir ->
                File(externalDir, "checklists").apply { mkdirs() }
            }
        }

        // Fallback a directorio interno si external media no está disponible
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir
        } else {
            File(context.filesDir, "checklists").apply { mkdirs() }
        }
    }

    private fun storeFile(filename: String = defaultFileName): File = File(getStorageDir(), filename)

    private val prefs = context.getSharedPreferences("checklist_prefs", Context.MODE_PRIVATE)
    private val KEY_LAST_VERSION_CODE = "last_version_code"

    init {
        // Migración: Borrar archivo antiguo si existe
        migrateOldFiles()
        // Inicializar archivos por defecto desde assets
        initializeDefaultChecklists()
    }

    /**
     * Migra archivos antiguos borrando el checklists.yaml obsoleto
     * y moviendo archivos del directorio antiguo al nuevo
     */
    private fun migrateOldFiles() {
        try {
            // Borrar archivo antiguo checklists.yaml si existe
            val oldFile = File(context.filesDir, "checklists.yaml")
            if (oldFile.exists()) {
                val deleted = oldFile.delete()
                if (deleted) {
                    Log.i(TAG, "Archivo antiguo checklists.yaml eliminado exitosamente")
                } else {
                    Log.w(TAG, "No se pudo eliminar el archivo antiguo checklists.yaml")
                }
            }

            // Migrar archivos YAML del directorio antiguo al nuevo
            val oldDir = context.filesDir
            val newDir = getStorageDir()

            if (oldDir.absolutePath != newDir.absolutePath) {
                oldDir.listFiles()?.filter { it.extension == "yaml" }?.forEach { file ->
                    try {
                        val targetFile = File(newDir, file.name)
                        if (!targetFile.exists()) {
                            file.copyTo(targetFile, overwrite = false)
                            file.delete()
                            Log.i(TAG, "Archivo ${file.name} migrado a ${newDir.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error migrando archivo ${file.name}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante migración de archivos", e)
        }
    }

    /**
     * Inicializa los archivos YAML por defecto desde assets si no existen
     * o si la versión de la app ha cambiado (actualización automática)
     * Detecta automáticamente todos los archivos .yaml en assets
     */
    private fun initializeDefaultChecklists() {
        try {
            // Verificar si la versión de la app cambió
            val currentVersion = try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }
            } catch (e: PackageManager.NameNotFoundException) {
                -1
            }
            val lastVersion = prefs.getInt(KEY_LAST_VERSION_CODE, -1)
            val shouldUpdate = lastVersion != currentVersion && currentVersion != -1

            if (shouldUpdate) {
                Log.i(TAG, "Actualización detectada: versión anterior $lastVersion -> $currentVersion")
            }

            // Obtener todos los archivos .yaml de assets automáticamente
            val defaultFiles = context.assets.list("")?.filter { it.endsWith(".yaml") } ?: emptyList()

            Log.i(TAG, "Detectados ${defaultFiles.size} archivos YAML en assets: $defaultFiles")

            for (assetFile in defaultFiles) {
                try {
                    val targetFile = File(getStorageDir(), assetFile)

                    // Copiar si no existe O si hay actualización de versión
                    if (!targetFile.exists() || shouldUpdate) {
                        context.assets.open(assetFile).use { input ->
                            val catalog = YamlIO.parseCatalog(input)
                            val yaml = YamlIO.stringify(catalog)
                            targetFile.writeText(yaml, Charsets.UTF_8)

                            if (shouldUpdate) {
                                Log.i(TAG, "Archivo $assetFile actualizado desde assets (nueva versión)")
                            } else {
                                Log.i(TAG, "Archivo por defecto $assetFile copiado desde assets")
                            }
                        }
                    } else {
                        Log.d(TAG, "Archivo $assetFile ya existe y no hay actualización, omitiendo copia")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error copiando archivo por defecto $assetFile desde assets", e)
                }
            }

            // Guardar la versión actual
            if (shouldUpdate) {
                prefs.edit().putInt(KEY_LAST_VERSION_CODE, currentVersion).apply()
                Log.i(TAG, "Versión actualizada en preferencias: $currentVersion")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listando archivos de assets", e)
        }
    }

    /**
     * Obtiene la referencia al archivo para un filename dado
     * Útil para compartir o acceder directamente al archivo
     *
     * @param filename Nombre del archivo YAML
     * @return Referencia al archivo
     */
    fun getFileReference(filename: String): File = storeFile(filename)

    /**
     * Lista todos los archivos YAML disponibles en almacenamiento
     *
     * @return Lista de nombres de archivos YAML
     */
    suspend fun listChecklistFiles(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val files = getStorageDir().listFiles()?.filter { it.extension == "yaml" }
                ?.map { it.name }
                ?.sorted()
                ?: emptyList()
            Result.success(files)
        } catch (e: Exception) {
            Log.e(TAG, "Error listando archivos YAML", e)
            Result.failure(RepositoryException.IOError("Error listando archivos: ${e.message}", e))
        }
    }

    /**
     * Carga catálogo. Si no existe en almacenamiento interno,
     * intenta cargar desde assets. Si no existe, catálogo vacío.
     *
     * @param filename Nombre del archivo YAML a cargar (por defecto: Taifun17E_ES.yaml)
     * @return Result con el catálogo o error
     */
    suspend fun load(filename: String = defaultFileName): Result<Catalogo> = withContext(Dispatchers.IO) {
        try {
            val f = storeFile(filename)
            val catalogo = when {
                f.exists() -> {
                    f.inputStream().use { YamlIO.parseCatalog(it) }
                }
                else -> {
                    // Intentar cargar desde assets
                    val seed = runCatching {
                        context.assets.open(filename)
                    }.getOrNull()

                    if (seed != null) {
                        val cat = seed.use { YamlIO.parseCatalog(it) }
                        // Guardar en storage interno para futuros accesos
                        save(cat, filename).getOrElse {
                            Log.w(TAG, "No se pudo guardar archivo $filename desde assets")
                        }
                        cat
                    } else {
                        Log.w(TAG, "Archivo $filename no encontrado ni en storage ni en assets")
                        Catalogo(emptyList())
                    }
                }
            }
            Result.success(catalogo)
        } catch (e: IOException) {
            Log.e(TAG, "Error de I/O cargando catálogo", e)
            Result.failure(RepositoryException.IOError("Error leyendo archivo: ${e.message}", e))
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos cargando catálogo", e)
            Result.failure(RepositoryException.PermissionError("Sin permisos para leer archivo", e))
        } catch (e: YamlParseException) {
            Log.e(TAG, "Error parseando YAML", e)
            Result.failure(RepositoryException.ParseError(e.message ?: "Error parseando YAML", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado cargando catálogo", e)
            Result.failure(RepositoryException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    /**
     * Guarda el catálogo en almacenamiento interno.
     *
     * @param catalogo Catálogo a guardar
     * @param filename Nombre del archivo YAML (por defecto: Taifun17E_ES.yaml)
     * @return Result indicando éxito o error
     */
    suspend fun save(catalogo: Catalogo, filename: String = defaultFileName): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val txt = YamlIO.stringify(catalogo)
            storeFile(filename).writeText(txt, Charsets.UTF_8)
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Error de I/O guardando catálogo", e)
            Result.failure(RepositoryException.IOError("Error escribiendo archivo: ${e.message}", e))
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos guardando catálogo", e)
            Result.failure(RepositoryException.PermissionError("Sin permisos para escribir archivo", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado guardando catálogo", e)
            Result.failure(RepositoryException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    /**
     * Elimina un archivo YAML del almacenamiento interno
     *
     * @param filename Nombre del archivo a eliminar
     * @return Result indicando éxito o error
     */
    suspend fun delete(filename: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = storeFile(filename)
            if (file.exists()) {
                if (!file.delete()) {
                    return@withContext Result.failure(
                        RepositoryException.IOError("No se pudo eliminar el archivo")
                    )
                }
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Error eliminando archivo", e)
            Result.failure(RepositoryException.IOError("Error eliminando archivo: ${e.message}", e))
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos eliminando archivo", e)
            Result.failure(RepositoryException.PermissionError("Sin permisos para eliminar archivo", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado eliminando archivo", e)
            Result.failure(RepositoryException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    /**
     * Importa desde un URI (SAF). Devuelve el catálogo importado ya persistido.
     *
     * @param uri URI del archivo a importar
     * @param filename Nombre para guardar el archivo (si null, genera uno único)
     * @return Result con par (nombre de archivo, catálogo) o error
     */
    suspend fun importFromUri(uri: Uri, filename: String? = null): Result<Pair<String, Catalogo>> = withContext(Dispatchers.IO) {
        try {
            val cat = context.contentResolver.openInputStream(uri)?.use { input ->
                YamlIO.parseCatalog(input)
            } ?: return@withContext Result.failure(
                RepositoryException.IOError("No se pudo abrir el archivo")
            )

            // Generar nombre único si no se especifica
            val finalFilename = filename ?: generateUniqueFilename()

            // Guardar el catálogo importado
            save(cat, finalFilename).onFailure { error ->
                return@withContext Result.failure(error)
            }

            Result.success(Pair(finalFilename, cat))
        } catch (e: IOException) {
            Log.e(TAG, "Error de I/O importando", e)
            Result.failure(RepositoryException.IOError("Error leyendo archivo: ${e.message}", e))
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos importando", e)
            Result.failure(RepositoryException.PermissionError("Sin permisos para leer archivo", e))
        } catch (e: YamlParseException) {
            Log.e(TAG, "Error parseando YAML al importar", e)
            Result.failure(RepositoryException.ParseError(e.message ?: "Error parseando YAML", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado importando", e)
            Result.failure(RepositoryException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    /**
     * Genera un nombre de archivo único basado en timestamp
     */
    private fun generateUniqueFilename(): String {
        val timestamp = System.currentTimeMillis()
        return "checklist_$timestamp.yaml"
    }

    /**
     * Crea un nuevo archivo de checklist vacío
     *
     * @param filename Nombre para el nuevo archivo (si null, genera uno único)
     * @return Result con el nombre del archivo creado o error
     */
    suspend fun createEmptyChecklist(filename: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val finalFilename = filename ?: generateUniqueFilename()
            val emptyCatalog = Catalogo(emptyList())

            save(emptyCatalog, finalFilename).onFailure { error ->
                return@withContext Result.failure(error)
            }

            Result.success(finalFilename)
        } catch (e: Exception) {
            Log.e(TAG, "Error creando checklist vacío", e)
            Result.failure(RepositoryException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    /**
     * Exporta un catálogo al URI (SAF).
     *
     * @param uri URI de destino
     * @param filename Nombre del archivo a exportar (por defecto: Taifun17E_ES.yaml)
     * @return Result indicando éxito o error
     */
    suspend fun exportToUri(uri: Uri, filename: String = defaultFileName): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val current = load(filename).getOrElse { error ->
                return@withContext Result.failure(error)
            }

            val txt = YamlIO.stringify(current)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(txt.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return@withContext Result.failure(
                RepositoryException.IOError("No se pudo abrir el archivo para escritura")
            )

            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Error de I/O exportando", e)
            Result.failure(RepositoryException.IOError("Error escribiendo archivo: ${e.message}", e))
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos exportando", e)
            Result.failure(RepositoryException.PermissionError("Sin permisos para escribir archivo", e))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado exportando", e)
            Result.failure(RepositoryException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    companion object {
        private const val TAG = "ChecklistRepository"
    }
}

/**
 * Excepciones específicas del repositorio para mejor manejo de errores
 */
sealed class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class IOError(message: String, cause: Throwable? = null) : RepositoryException(message, cause)
    class PermissionError(message: String, cause: Throwable? = null) : RepositoryException(message, cause)
    class ParseError(message: String, cause: Throwable? = null) : RepositoryException(message, cause)
    class UnknownError(message: String, cause: Throwable? = null) : RepositoryException(message, cause)
}
