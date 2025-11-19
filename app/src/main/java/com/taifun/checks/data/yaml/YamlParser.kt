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
     * @return ParseResult con el catálogo y lista de warnings si hubo elementos omitidos
     * @throws YamlParseException si el YAML es inválido
     */
    fun parseCatalogWithWarnings(input: InputStream): ParseResult {
        val warnings = mutableListOf<String>()

        try {
            val yaml = Yaml(SafeConstructor(LoaderOptions()))
            val root = yaml.load<Any>(input)
                ?: throw YamlParseException("Archivo YAML vacío")

            @Suppress("UNCHECKED_CAST")
            val map = root as? Map<String, Any?>
                ?: throw YamlParseException("Formato inválido: se esperaba un mapa en la raíz")

            val rawChecklists = map["checklists"] as? List<*>
                ?: throw YamlParseException("Falta campo 'checklists'")

            var omittedChecklists = 0
            val cl = rawChecklists.mapIndexedNotNull { index, item ->
                try {
                    parseChecklist(item, index)
                } catch (e: YamlParseException) {
                    Log.w(TAG, "Checklist #$index omitido: ${e.message}")
                    warnings.add("Checklist #${index + 1} omitido: ${e.message}")
                    omittedChecklists++
                    null // omitir checklist inválido
                }
            }

            if (omittedChecklists > 0) {
                Log.w(TAG, "Se omitieron $omittedChecklists checklists inválidos")
            }

            Log.i(TAG, "Catálogo cargado: ${cl.size} checklists, $omittedChecklists omitidos")
            return ParseResult(
                catalogo = Catalogo(checklists = cl),
                warnings = warnings
            )

        } catch (e: YAMLException) {
            Log.e(TAG, "Error de sintaxis YAML", e)
            throw YamlParseException("Error de sintaxis YAML: ${e.message}", e)
        } catch (e: ClassCastException) {
            Log.e(TAG, "Error de tipo en YAML", e)
            throw YamlParseException("Tipo de dato incorrecto en YAML", e)
        } catch (e: YamlParseException) {
            throw e // re-throw our custom exception
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado parseando YAML", e)
            throw YamlParseException("Error inesperado: ${e.message}", e)
        }
    }

    /**
     * Parsea un catálogo desde InputStream.
     * @throws YamlParseException si el YAML es inválido
     */
    fun parseCatalog(input: InputStream): Catalogo {
        return parseCatalogWithWarnings(input).catalogo
    }

    /**
     * Parsea desde String (para validación en tiempo real).
     */
    fun parseCatalog(yamlString: String): Catalogo {
        return parseCatalog(yamlString.byteInputStream())
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
        val pasos = pasosRaw.mapIndexedNotNull { pIndex, p ->
            try {
                parsePaso(p, pIndex)
            } catch (e: YamlParseException) {
                Log.w(TAG, "Checklist '$id' paso #$pIndex omitido: ${e.message}")
                null
            }
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
