package com.taifun.checks.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Iconos para checklists Taifun.
 * Usa Material Icons Extended para todos los conceptos.
 * Incluye helper iconFor(nombre) para mapear desde YAML.
 */
object IconsRepo {

    private val primaryColor = Color(0xFF0A64C9)
    private val whiteColor = Color.White

    // ---- Logo Taifun (único icono custom) ----
    val TaifunLogo: ImageVector = Builder("TaifunLogo", 64.dp, 32.dp, 64f, 32f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(2f,16f)
            curveTo(16f,6f, 40f,6f, 62f,16f)
            curveTo(40f,26f, 16f,26f, 2f,16f)
            close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(26f,13f)
            curveTo(30f,11f, 34f,11f, 38f,13f)
            curveTo(34f,14f, 30f,14f, 26f,13f)
            close()
        }
    }.build()

    // ---- Helper para YAML ----
    /**
     * Mapea nombres de iconos a ImageVectors de Material Icons Extended.
     */
    fun iconFor(nombre: String?): ImageVector = when (nombre?.lowercase()) {
        // Controles de Vuelo
        "control" -> Icons.Filled.Gamepad
        "timon" -> Icons.Filled.SwapHoriz
        "profundidad" -> Icons.Filled.SwapVert
        "aleron" -> Icons.Filled.FlipCameraAndroid
        "trim" -> Icons.Filled.LinearScale
        "palanca" -> Icons.Filled.SportsEsports

        // Superficies
        "flaps" -> Icons.Filled.Layers
        "aerofreno" -> Icons.Filled.VerticalAlignBottom
        "alas" -> Icons.Filled.FlightTakeoff

        // Motor y Propulsión
        "motor" -> Icons.Filled.Factory
        "helice" -> Icons.Outlined.`mode_fan`
        "gases" -> Icons.Filled.WindPower
        "estrangulador" -> Icons.Filled.Compress
        "ignicion" -> Icons.Filled.Bolt
        "refrigeracion" -> Icons.Filled.AcUnit

        // Combustible y Fluidos
        "combustible" -> Icons.Filled.LocalGasStation
        "aceite" -> Icons.Filled.OilBarrel
        "bomba" -> Icons.Filled.WaterDrop

        // Tren de Aterrizaje
        "tren" -> Icons.Filled.Adjust
        "freno" -> Icons.Filled.StopCircle

        // Eléctrico
        "bateria" -> Icons.Filled.BatteryFull
        "generador" -> Icons.Filled.ElectricalServices
        "interruptor" -> Icons.Filled.ToggleOn
        "luz" -> Icons.Filled.Lightbulb
        "boton" -> Icons.Filled.RadioButtonChecked

        // Aviónica
        "radio" -> Icons.Filled.Podcasts
        "transponder" -> Icons.Filled.Sensors
        "antena" -> Icons.Filled.SettingsInputAntenna

        // Instrumentos
        "instrumentos" -> Icons.Filled.Dashboard
        "altimetro" -> Icons.Filled.Height
        "brujula" -> Icons.Filled.Explore
        "anemometro" -> Icons.Filled.Speed

        // Inspección
        "inspeccion" -> Icons.Filled.ManageSearch
        "check" -> Icons.Filled.CheckCircle
        "llave" -> Icons.Filled.Key
        "pitot" -> Icons.Filled.Straighten

        // Cabina
        "cabina" -> Icons.Filled.AirlineSeatReclineNormal
        "cinturon" -> Icons.Filled.HealthAndSafety
        "puerto" -> Icons.Filled.Usb
        "calefaccion" -> Icons.Filled.Thermostat

        // Seguridad
        "seguro" -> Icons.Filled.VerifiedUser
        "salida" -> Icons.Filled.Logout
        "paracaidas" -> Icons.Filled.Paragliding

        // General
        "vuelo" -> Icons.Filled.Flight
        "documento" -> Icons.Filled.Description
        "carga" -> Icons.Filled.Luggage
        "balanza" -> Icons.Filled.Balance
        "viento" -> Icons.Filled.Air
        "microfono" -> Icons.Filled.Mic

        // Logo
        "taifun", "logo" -> TaifunLogo

        // Default
        else -> Icons.Filled.CheckCircle
    }

    // ---- Opciones de iconos para selector ----
    data class IconOption(
        val id: String,
        val name: String,
        val icon: ImageVector,
        val category: String
    )

    /**
     * Lista completa de iconos disponibles organizados por categoría.
     * Todos son Material Icons Extended.
     */
    val availableIcons = listOf(
        // Controles de Vuelo
        IconOption("control", "Control", Icons.Filled.Gamepad, "Controles"),
        IconOption("timon", "Timón", Icons.Filled.SwapHoriz, "Controles"),
        IconOption("profundidad", "Profundidad", Icons.Filled.SwapVert, "Controles"),
        IconOption("aleron", "Alerón", Icons.Filled.FlipCameraAndroid, "Controles"),
        IconOption("trim", "Trim", Icons.Filled.LinearScale, "Controles"),
        IconOption("palanca", "Palanca", Icons.Filled.SportsEsports, "Controles"),

        // Superficies
        IconOption("flaps", "Flaps", Icons.Filled.Layers, "Superficies"),
        IconOption("aerofreno", "Aerofreno", Icons.Filled.VerticalAlignBottom, "Superficies"),
        IconOption("alas", "Alas", Icons.Filled.FlightTakeoff, "Superficies"),

        // Motor y Propulsión
        IconOption("motor", "Motor", Icons.Filled.Factory, "Motor"),
        IconOption("helice", "Hélice", Icons.Outlined.`mode_fan`, "Motor"),
        IconOption("gases", "Gases", Icons.Filled.WindPower, "Motor"),
        IconOption("estrangulador", "Estrangulador", Icons.Filled.Compress, "Motor"),
        IconOption("ignicion", "Ignición", Icons.Filled.Bolt, "Motor"),
        IconOption("refrigeracion", "Refrigeración", Icons.Filled.AcUnit, "Motor"),

        // Combustible y Fluidos
        IconOption("combustible", "Combustible", Icons.Filled.LocalGasStation, "Fluidos"),
        IconOption("aceite", "Aceite", Icons.Filled.OilBarrel, "Fluidos"),
        IconOption("bomba", "Bomba", Icons.Filled.WaterDrop, "Fluidos"),

        // Tren de Aterrizaje
        IconOption("tren", "Tren", Icons.Filled.Adjust, "Tren"),
        IconOption("freno", "Freno", Icons.Filled.StopCircle, "Tren"),

        // Eléctrico
        IconOption("bateria", "Batería", Icons.Filled.BatteryFull, "Eléctrico"),
        IconOption("generador", "Generador", Icons.Filled.ElectricalServices, "Eléctrico"),
        IconOption("interruptor", "Interruptor", Icons.Filled.ToggleOn, "Eléctrico"),
        IconOption("luz", "Luz", Icons.Filled.Lightbulb, "Eléctrico"),
        IconOption("boton", "Botón", Icons.Filled.RadioButtonChecked, "Eléctrico"),

        // Aviónica
        IconOption("radio", "Radio", Icons.Filled.Podcasts, "Aviónica"),
        IconOption("transponder", "Transponder", Icons.Filled.Sensors, "Aviónica"),
        IconOption("antena", "Antena", Icons.Filled.SettingsInputAntenna, "Aviónica"),

        // Instrumentos
        IconOption("instrumentos", "Instrumentos", Icons.Filled.Dashboard, "Instrumentos"),
        IconOption("altimetro", "Altímetro", Icons.Filled.Height, "Instrumentos"),
        IconOption("brujula", "Brújula", Icons.Filled.Explore, "Instrumentos"),
        IconOption("anemometro", "Anemómetro", Icons.Filled.Speed, "Instrumentos"),

        // Inspección
        IconOption("inspeccion", "Inspección", Icons.Filled.ManageSearch, "Inspección"),
        IconOption("check", "Check", Icons.Filled.CheckCircle, "Inspección"),
        IconOption("llave", "Llave", Icons.Filled.Key, "Inspección"),
        IconOption("pitot", "Pitot", Icons.Filled.Straighten, "Inspección"),

        // Cabina
        IconOption("cabina", "Cabina", Icons.Filled.AirlineSeatReclineNormal, "Cabina"),
        IconOption("cinturon", "Cinturón", Icons.Filled.HealthAndSafety, "Cabina"),
        IconOption("puerto", "Puerto", Icons.Filled.Usb, "Cabina"),
        IconOption("calefaccion", "Calefacción", Icons.Filled.Thermostat, "Cabina"),

        // Seguridad
        IconOption("seguro", "Seguro", Icons.Filled.VerifiedUser, "Seguridad"),
        IconOption("salida", "Salida", Icons.Filled.Logout, "Seguridad"),
        IconOption("paracaidas", "Paracaídas", Icons.Filled.Paragliding, "Seguridad"),

        // General
        IconOption("vuelo", "Vuelo", Icons.Filled.Flight, "General"),
        IconOption("documento", "Documento", Icons.Filled.Description, "General"),
        IconOption("carga", "Carga", Icons.Filled.Luggage, "General"),
        IconOption("balanza", "Balanza", Icons.Filled.Balance, "General"),
        IconOption("viento", "Viento", Icons.Filled.Air, "General"),
        IconOption("microfono", "Micrófono", Icons.Filled.Mic, "General")
    )

    /**
     * Obtener opción de icono por ID
     */
    fun getIconOption(iconId: String?): IconOption? {
        if (iconId.isNullOrBlank()) return null
        return availableIcons.find { it.id.equals(iconId, ignoreCase = true) }
    }
}
