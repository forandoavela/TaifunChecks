package com.taifun.checks.data.yaml

import android.util.Log
import com.taifun.checks.domain.Catalogo
import com.taifun.checks.domain.Checklist
import com.taifun.checks.domain.Paso
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.YAMLException
import java.io.InputStream

/**
 * Resultado de parsear un catálogo con información sobre warnings
 */
data class ParseResult(
    val catalogo: Catalogo,
    val warnings: List<String> = emptyList()
) {
    fun hasWarnings() = warnings.isNotEmpty()
}

/**
 * Parser y serializador YAML con manejo robusto de errores.
 */
object YamlIO {

    private const val TAG = "YamlIO"

    /**
     * Parsea un catálogo desde InputStream con información detallada sobre warnings.
     * @param input InputStream con el contenido YAML
     * @param filename Nombre del archivo (opcional, solo para logging mejorado)
     * @return ParseResult con el catálogo y lista de warnings si hubo elementos omitidos
     * @throws YamlParseException si el YAML es inválido
     */
    fun parseCatalogWithWarnings(input: InputStream, filename: String? = null): ParseResult {
        val warnings = mutableListOf<String>()
        val fileContext = filename?.let { " en '$it'" } ?: ""

        try {
            val yaml = Yaml(SafeConstructor(LoaderOptions()))
            val root = yaml.load<Any>(input)
                ?: throw YamlParseException("Archivo YAML vacío$fileContext")

            @Suppress("UNCHECKED_CAST")
            val map = root as? Map<String, Any?>
                ?: throw YamlParseException("Formato inválido: se esperaba un mapa en la raíz$fileContext")

            val rawChecklists = map["checklists"] as? List<*>
                ?: throw YamlParseException("Falta campo 'checklists'$fileContext")

            var omittedChecklists = 0
            val cl = rawChecklists.mapIndexedNotNull { index, item ->
                try {
                    parseChecklist(item, index)
                } catch (e: YamlParseException) {
                    // Logging mejorado: incluye filename, causa raíz, y stack trace completo en nivel DEBUG
                    Log.w(TAG, "Checklist #$index omitido$fileContext: ${e.message}. Causa: ${e.cause?.message ?: "N/A"}")
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Stack trace completo para checklist omitido #$index:", e)
                    }
                    warnings.add("Checklist #${index + 1} omitido: ${e.message}")
                    omittedChecklists++
                    null // omitir checklist inválido
                }
            }

            if (omittedChecklists > 0) {
                Log.w(TAG, "Se omitieron $omittedChecklists checklists inválidos$fileContext")
            }

            Log.i(TAG, "Catálogo cargado$fileContext: ${cl.size} checklists válidos, $omittedChecklists omitidos")
            return ParseResult(
                catalogo = Catalogo(checklists = cl),
                warnings = warnings
            )

        } catch (e: YAMLException) {
            // Logging mejorado: extraer información de línea/columna si está disponible
            val locationInfo = extractYamlErrorLocation(e)
            val errorMsg = "Error de sintaxis YAML$fileContext$locationInfo: ${e.message}"
            Log.e(TAG, errorMsg, e)
            throw YamlParseException(errorMsg, e)
        } catch (e: ClassCastException) {
            val errorMsg = "Error de tipo en YAML$fileContext: tipo de dato incorrecto - ${e.message}"
            Log.e(TAG, errorMsg, e)
            throw YamlParseException(errorMsg, e)
        } catch (e: YamlParseException) {
            throw e // re-throw our custom exception
        } catch (e: Exception) {
            val errorMsg = "Error inesperado parseando YAML$fileContext: ${e::class.simpleName} - ${e.message}"
            Log.e(TAG, errorMsg, e)
            throw YamlParseException(errorMsg, e)
        }
    }

    /**
     * Extrae información de ubicación (línea/columna) de YAMLException si está disponible
     */
    private fun extractYamlErrorLocation(e: YAMLException): String {
        return try {
            // YAMLException puede contener información de línea/columna en el mensaje
            // Formato común: "... at line N, column M"
            val msg = e.message ?: ""
            val linePattern = Regex("line\\s+(\\d+)")
            val columnPattern = Regex("column\\s+(\\d+)")

            val line = linePattern.find(msg)?.groupValues?.getOrNull(1)
            val column = columnPattern.find(msg)?.groupValues?.getOrNull(1)

            when {
                line != null && column != null -> " (línea $line, columna $column)"
                line != null -> " (línea $line)"
                else -> ""
            }
        } catch (ex: Exception) {
            "" // Si falla la extracción, retornar string vacío
        }
    }

    /**
     * Parsea un catálogo desde InputStream.
     * @param input InputStream con el contenido YAML
     * @param filename Nombre del archivo (opcional, solo para logging mejorado)
     * @throws YamlParseException si el YAML es inválido
     */
    fun parseCatalog(input: InputStream, filename: String? = null): Catalogo {
        return parseCatalogWithWarnings(input, filename).catalogo
    }

    /**
     * Parsea desde String (para validación en tiempo real).
     */
    fun parseCatalog(yamlString: String): Catalogo {
        return parseCatalog(yamlString.byteInputStream(), filename = null)
    }

    private fun parseChecklist(item: Any?, index: Int): Checklist {
        @Suppress("UNCHECKED_CAST")
        val m = item as? Map<String, Any?>
            ?: throw YamlParseException("Checklist #$index no es un mapa")

        val id = m["id"]?.toString()
            ?: throw YamlParseException("Checklist #$index: falta 'id'")
        val titulo = m["titulo"]?.toString() ?: id
        val categoria = m["categoria"]?.toString()
        val fullList = when (val fl = m["full-list"]) {
            is Boolean -> fl
            is String -> fl.equals("true", ignoreCase = true)
            else -> null
        }
        val color = m["color"]?.toString()

        val pasosRaw = m["pasos"] as? List<*> ?: emptyList<Any?>()
        var omittedSteps = 0
        val pasos = pasosRaw.mapIndexedNotNull { pIndex, p ->
            try {
                parsePaso(p, pIndex)
            } catch (e: YamlParseException) {
                // Logging mejorado: incluye ID de checklist, índice de paso, causa raíz
                Log.w(TAG, "Checklist '$id' (índice #$index) paso #$pIndex omitido: ${e.message}. Causa: ${e.cause?.message ?: "N/A"}")
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Stack trace completo para paso omitido '$id' #$pIndex:", e)
                }
                omittedSteps++
                null
            }
        }

        if (omittedSteps > 0) {
            Log.w(TAG, "Checklist '$id': se omitieron $omittedSteps pasos inválidos de ${pasosRaw.size} totales")
        }

        return Checklist(
            id = id,
            titulo = titulo,
            categoria = categoria,
            fullList = fullList,
            color = color,
            pasos = pasos
        )
    }

    private fun parsePaso(item: Any?, index: Int): Paso {
        @Suppress("UNCHECKED_CAST")
        val pm = item as? Map<String, Any?>
            ?: throw YamlParseException("Paso #$index no es un mapa")

        val pid = pm["id"]?.toString()
            ?: throw YamlParseException("Paso #$index: falta 'id'")
        val texto = pm["texto"]?.toString()
            ?: throw YamlParseException("Paso #$index: falta 'texto'")
        val icono = pm["icono"]?.toString()
        val altitud = pm["altitud"]?.toString()
        val qnh = pm["qnh"]?.toString()
        val link = pm["link"]?.toString()
        val app = pm["app"]?.toString()
        val localtime = when (val lt = pm["localtime"]) {
            is Boolean -> lt
            is String -> lt.equals("true", ignoreCase = true)
            else -> null
        }
        val utctime = when (val ut = pm["utctime"]) {
            is Boolean -> ut
            is String -> ut.equals("true", ignoreCase = true)
            else -> null
        }
        val log = pm["log"]?.toString()

        return Paso(
            id = pid,
            texto = texto,
            icono = icono,
            altitud = altitud,
            qnh = qnh,
            link = link,
            app = app,
            localtime = localtime,
            utctime = utctime,
            log = log
        )
    }

    /**
     * Serializa catálogo a String YAML.
     */
    fun stringify(catalogo: Catalogo): String {
        try {
            val opts = DumperOptions().apply {
                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                isPrettyFlow = true
                indent = 2
            }
            val yaml = Yaml(opts)

            val data = mapOf(
                "version" to "1.0",
                "checklists" to catalogo.checklists.map { c ->
                    linkedMapOf<String, Any?>(
                        "id" to c.id,
                        "titulo" to c.titulo
                    ).apply {
                        if (!c.categoria.isNullOrBlank()) put("categoria", c.categoria)
                        if (c.fullList != null) put("full-list", c.fullList)
                        if (!c.color.isNullOrBlank()) put("color", c.color)
                        put("pasos", c.pasos.map { p ->
                            linkedMapOf<String, Any?>(
                                "id" to p.id,
                                "texto" to p.texto
                            ).apply {
                                if (!p.icono.isNullOrBlank()) put("icono", p.icono)
                                if (!p.altitud.isNullOrBlank()) put("altitud", p.altitud)
                                if (!p.qnh.isNullOrBlank()) put("qnh", p.qnh)
                                if (!p.link.isNullOrBlank()) put("link", p.link)
                                if (!p.app.isNullOrBlank()) put("app", p.app)
                                if (p.localtime == true) put("localtime", p.localtime)
                                if (p.utctime == true) put("utctime", p.utctime)
                                if (!p.log.isNullOrBlank()) put("log", p.log)
                            }
                        })
                    }
                }
            )

            return yaml.dump(data)
        } catch (e: Exception) {
            Log.e(TAG, "Error serializando YAML", e)
            throw YamlParseException("Error al generar YAML: ${e.message}", e)
        }
    }
}

/**
 * Excepción customizada para errores de parsing YAML.
 */
class YamlParseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
