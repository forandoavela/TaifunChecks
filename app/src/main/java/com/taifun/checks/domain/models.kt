package com.taifun.checks.domain

data class Paso(
    val id: String,
    val texto: String,
    val icono: String? = null,
    // Funciones opcionales
    val altitud: String? = null,      // "m" para metros, "ft" para pies
    val qnh: String? = null,          // "hPa" para hectopascales, "inHg" para pulgadas de mercurio
    val link: String? = null,         // URL para abrir
    val app: String? = null,          // Package name de la app (ej: com.google.android.apps.maps)
    val localtime: Boolean? = null,   // true para mostrar hora local
    val utctime: Boolean? = null,     // true para mostrar hora UTC
    val log: String? = null           // Texto para botón de log (ej: "Despegue pista 32L")
)

data class Checklist(
    val id: String,
    val titulo: String,
    val categoria: String? = null,
    // campo YAML: full-list -> propiedad Kotlin fullList
    val fullList: Boolean? = null,
    val color: String? = null, // Color en formato hex: #RRGGBB
    val pasos: List<Paso> = emptyList()
)

data class Catalogo(
    val checklists: List<Checklist> = emptyList()
)
