package com.taifun.checks.domain

/**
 * Representa un paso individual dentro de un checklist de aviación.
 *
 * @property id Identificador único del paso
 * @property texto Texto descriptivo del paso a realizar
 * @property icono Nombre del icono opcional a mostrar (ver IconsRepo)
 * @property altitud Unidad de altitud a mostrar: "m" (metros) o "ft" (pies)
 * @property qnh Unidad de QNH a mostrar: "hPa" (hectopascales) o "inHg" (pulgadas de mercurio)
 * @property link URL opcional para abrir en navegador
 * @property app Package name de aplicación a lanzar (ej: com.google.android.apps.maps)
 * @property localtime Si true, muestra la hora local actual
 * @property utctime Si true, muestra la hora UTC actual
 * @property log Texto para el botón de log (ej: "Despegue pista 32L"). Si presente, habilita registro GPS
 */
data class Paso(
    val id: String,
    val texto: String,
    val icono: String? = null,
    val altitud: String? = null,
    val qnh: String? = null,
    val link: String? = null,
    val app: String? = null,
    val localtime: Boolean? = null,
    val utctime: Boolean? = null,
    val log: String? = null
)

/**
 * Representa un checklist completo con sus pasos y metadatos.
 *
 * @property id Identificador único del checklist
 * @property titulo Título descriptivo del checklist
 * @property categoria Categoría para agrupar checklists relacionados
 * @property fullList Si true, muestra todos los pasos en lista. Si false, modo paso a paso
 * @property color Color de fondo en formato hex (#RRGGBB) para distinguir visualmente
 * @property pasos Lista ordenada de pasos que componen el checklist
 */
data class Checklist(
    val id: String,
    val titulo: String,
    val categoria: String? = null,
    val fullList: Boolean? = null,
    val color: String? = null,
    val pasos: List<Paso> = emptyList()
)

/**
 * Contenedor raíz que agrupa múltiples checklists.
 * Corresponde a la estructura de un archivo YAML de checklists.
 *
 * @property checklists Lista de todos los checklists disponibles
 */
data class Catalogo(
    val checklists: List<Checklist> = emptyList()
)
