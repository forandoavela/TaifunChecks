package com.taifun.checks.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Iconos de aviación para checklists Taifun.
 * Usa Material Icons Extended para conceptos genéricos y iconos custom para aviación específica.
 * Incluye helper iconFor(nombre) para mapear desde YAML.
 */
object IconsRepo {

    private val primaryColor = Color(0xFF0A64C9)
    private val whiteColor = Color.White

    // ---- Check ----
    val Check: ImageVector = Builder("Check", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(9f, 16.2f); lineTo(4.8f, 12f); lineTo(3.4f, 13.4f); lineTo(9f, 19f)
            lineTo(21f, 7f); lineTo(19.6f, 5.6f); close()
        }
    }.build()

    // ---- Llave ----
    val Llave: ImageVector = Builder("Llave", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(7f, 14f); lineTo(11f, 18f); lineTo(13f, 16f); lineTo(12f, 15f)
            lineTo(13f, 14f); lineTo(12f, 13f); lineTo(13f, 12f); lineTo(18f, 7f)
            curveTo(19f, 5f, 18f, 3f, 16f, 3f)
            curveTo(14f, 3f, 13f, 4f, 13f, 6f)
            curveTo(13f, 6.5f, 13.2f, 7f, 13.5f, 7.3f)
            close()
        }
    }.build()

    // ---- Cabina ----
    val Cabina: ImageVector = Builder("Cabina", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(9f, 6f); lineTo(15f, 6f); lineTo(18f, 10f); lineTo(18f, 18f)
            lineTo(6f, 18f); lineTo(6f, 10f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(10f, 8f); lineTo(14f, 8f); lineTo(16f, 11f); lineTo(8f, 11f); close()
        }
    }.build()

    // ---- Flaps ----
    val Flaps: ImageVector = Builder("Flaps", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(3f, 7f); lineTo(21f, 7f); lineTo(21f, 10f); lineTo(3f, 10f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(15f, 10f); lineTo(21f, 10f); lineTo(21f, 17f); lineTo(15f, 14f); close()
        }
    }.build()

    // ---- Aerofreno ----
    val Aerofreno: ImageVector = Builder("Aerofreno", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 12f); lineTo(10f, 6f); lineTo(10f, 10f); lineTo(14f, 10f)
            lineTo(14f, 6f); lineTo(20f, 12f); lineTo(14f, 18f); lineTo(14f, 14f)
            lineTo(10f, 14f); lineTo(10f, 18f); close()
        }
    }.build()

    // ---- Alerón ----
    val Aleron: ImageVector = Builder("Aleron", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(2f, 10f); lineTo(11f, 10f); lineTo(11f, 6f); lineTo(13f, 6f)
            lineTo(13f, 10f); lineTo(22f, 10f); lineTo(22f, 14f); lineTo(13f, 14f)
            lineTo(13f, 18f); lineTo(11f, 18f); lineTo(11f, 14f); lineTo(2f, 14f); close()
        }
    }.build()

    // ---- Luz ----
    val Luz: ImageVector = Builder("Luz", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f); lineTo(12f, 5f)
            moveTo(12f, 19f); lineTo(12f, 22f)
            moveTo(4.2f, 4.2f); lineTo(6.3f, 6.3f)
            moveTo(17.7f, 17.7f); lineTo(19.8f, 19.8f)
            moveTo(2f, 12f); lineTo(5f, 12f)
            moveTo(19f, 12f); lineTo(22f, 12f)
            moveTo(4.2f, 19.8f); lineTo(6.3f, 17.7f)
            moveTo(17.7f, 6.3f); lineTo(19.8f, 4.2f)
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 7f)
            curveTo(9.2f, 7f, 7f, 9.2f, 7f, 12f)
            curveTo(7f, 14.8f, 9.2f, 17f, 12f, 17f)
            curveTo(14.8f, 17f, 17f, 14.8f, 17f, 12f)
            curveTo(17f, 9.2f, 14.8f, 7f, 12f, 7f)
            close()
        }
    }.build()

    // ---- Pitot ----
    val Pitot: ImageVector = Builder("Pitot", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(3f, 10f); lineTo(15f, 10f); lineTo(21f, 12f); lineTo(15f, 14f)
            lineTo(3f, 14f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(17f, 11f); lineTo(19f, 12f); lineTo(17f, 13f); close()
        }
    }.build()

    // ---- Combustible ----
    val Combustible: ImageVector = Builder("Combustible", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(7f,4f); lineTo(15f,4f); lineTo(17f,6f); lineTo(17f,20f); lineTo(7f,20f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(9f,9f); lineTo(15f,15f)
            moveTo(15f,9f); lineTo(9f,15f)
        }
    }.build()

    // ---- Aceite ----
    val Aceite: ImageVector = Builder("Aceite", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 3f)
            lineTo(8f, 8f); lineTo(8f, 20f); lineTo(16f, 20f); lineTo(16f, 8f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(10f, 12f); lineTo(14f, 12f); lineTo(14f, 18f); lineTo(10f, 18f); close()
        }
    }.build()

    // ---- Motor ----
    val Motor: ImageVector = Builder("Motor", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(6f, 8f); lineTo(18f, 8f); lineTo(18f, 16f); lineTo(6f, 16f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(4f, 10f); lineTo(6f, 10f); lineTo(6f, 14f); lineTo(4f, 14f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(18f, 10f); lineTo(20f, 10f); lineTo(20f, 14f); lineTo(18f, 14f); close()
        }
    }.build()

    // ---- Hélice ----
    val Helice: ImageVector = Builder("Helice", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) { moveTo(11f,3f); lineTo(13f,3f); lineTo(13f,12f); lineTo(11f,12f); close() }
        path(fill = SolidColor(primaryColor)) { moveTo(3f,11f); lineTo(12f,11f); lineTo(12f,13f); lineTo(3f,13f); close() }
        path(fill = SolidColor(primaryColor)) { moveTo(10f,10f); lineTo(14f,10f); lineTo(14f,14f); lineTo(10f,14f); close() }
    }.build()

    // ---- Inspección ----
    val Inspeccion: ImageVector = Builder("Inspeccion", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(15.5f, 14f); lineTo(20f, 20f); lineTo(18f, 22f); lineTo(12f, 17.5f)
            curveTo(10.7f, 18.2f, 9.2f, 18.5f, 7.5f, 18.5f)
            curveTo(3.4f, 18.5f, 0f, 15.1f, 0f, 11f)
            curveTo(0f, 6.9f, 3.4f, 3.5f, 7.5f, 3.5f)
            curveTo(11.6f, 3.5f, 15f, 6.9f, 15f, 11f)
            curveTo(15f, 12.2f, 14.7f, 13.3f, 14.2f, 14.3f)
            close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(7.5f, 7f)
            curveTo(5.6f, 7f, 4f, 8.6f, 4f, 10.5f)
            curveTo(4f, 12.4f, 5.6f, 14f, 7.5f, 14f)
            curveTo(9.4f, 14f, 11f, 12.4f, 11f, 10.5f)
            curveTo(11f, 8.6f, 9.4f, 7f, 7.5f, 7f)
            close()
        }
    }.build()

    // ---- Tren de aterrizaje ----
    val Tren: ImageVector = Builder("Tren", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 3f); lineTo(12f, 12f)
            moveTo(8f, 8f); lineTo(12f, 12f); lineTo(16f, 8f)
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(6f, 18f)
            curveTo(6f, 16.3f, 7.3f, 15f, 9f, 15f)
            curveTo(10.7f, 15f, 12f, 16.3f, 12f, 18f)
            curveTo(12f, 19.7f, 10.7f, 21f, 9f, 21f)
            curveTo(7.3f, 21f, 6f, 19.7f, 6f, 18f)
            close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 18f)
            curveTo(12f, 16.3f, 13.3f, 15f, 15f, 15f)
            curveTo(16.7f, 15f, 18f, 16.3f, 18f, 18f)
            curveTo(18f, 19.7f, 16.7f, 21f, 15f, 21f)
            curveTo(13.3f, 21f, 12f, 19.7f, 12f, 18f)
            close()
        }
    }.build()

    // ---- Antena ----
    val Antena: ImageVector = Builder("Antena", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(11f, 3f); lineTo(13f, 3f); lineTo(13f, 18f); lineTo(11f, 18f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(7f, 18f); lineTo(17f, 18f); lineTo(16f, 21f); lineTo(8f, 21f); close()
        }
    }.build()

    // ---- Puerto ----
    val Puerto: ImageVector = Builder("Puerto", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 12f)
            curveTo(12f, 9.8f, 10.2f, 8f, 8f, 8f)
            curveTo(5.8f, 8f, 4f, 9.8f, 4f, 12f)
            curveTo(4f, 14.2f, 5.8f, 16f, 8f, 16f)
            curveTo(10.2f, 16f, 12f, 14.2f, 12f, 12f)
            close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(10f, 12f)
            curveTo(10f, 10.9f, 9.1f, 10f, 8f, 10f)
            curveTo(6.9f, 10f, 6f, 10.9f, 6f, 12f)
            curveTo(6f, 13.1f, 6.9f, 14f, 8f, 14f)
            curveTo(9.1f, 14f, 10f, 13.1f, 10f, 12f)
            close()
        }
    }.build()

    // ---- Timón ----
    val Timon: ImageVector = Builder("Timon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 8f); lineTo(18f, 12f); lineTo(12f, 16f)
            lineTo(12f, 14f); lineTo(6f, 14f); lineTo(6f, 10f); lineTo(12f, 10f); close()
        }
    }.build()

    // ---- Profundidad ----
    val Profundidad: ImageVector = Builder("Profundidad", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 12f); lineTo(20f, 12f); lineTo(20f, 14f); lineTo(4f, 14f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(16f, 8f); lineTo(20f, 12f); lineTo(16f, 16f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(8f, 8f); lineTo(4f, 12f); lineTo(8f, 16f); close()
        }
    }.build()

    // ---- Control ----
    val Control: ImageVector = Builder("Control", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(10f, 3f); lineTo(14f, 3f); lineTo(14f, 15f); lineTo(10f, 15f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 15f)
            curveTo(9.2f, 15f, 7f, 17.2f, 7f, 20f)
            lineTo(17f, 20f)
            curveTo(17f, 17.2f, 14.8f, 15f, 12f, 15f)
            close()
        }
    }.build()

    // ---- Alas ----
    val Alas: ImageVector = Builder("Alas", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(2f, 12f); lineTo(10f, 10f); lineTo(10f, 8f); lineTo(14f, 8f)
            lineTo(14f, 10f); lineTo(22f, 12f); lineTo(22f, 15f); lineTo(14f, 13f)
            lineTo(14f, 16f); lineTo(10f, 16f); lineTo(10f, 13f); lineTo(2f, 15f); close()
        }
    }.build()

    // ---- Vuelo ----
    val Vuelo: ImageVector = Builder("Vuelo", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(21f, 16f); lineTo(14f, 11f); lineTo(14f, 3f); lineTo(12f, 2f)
            lineTo(12f, 11f); lineTo(5f, 16f); lineTo(3f, 15f); lineTo(3f, 17f)
            lineTo(6f, 18f); lineTo(3f, 19f); lineTo(3f, 21f); lineTo(5f, 20f)
            lineTo(12f, 22f); lineTo(12f, 13f); lineTo(12f, 22f); lineTo(19f, 20f)
            lineTo(21f, 21f); lineTo(21f, 19f); lineTo(18f, 18f); lineTo(21f, 17f); close()
        }
    }.build()

    // ---- Documento ----
    val Documento: ImageVector = Builder("Documento", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor), pathFillType = PathFillType.NonZero) {
            moveTo(6f, 2f); lineTo(14f, 2f); lineTo(20f, 8f); lineTo(20f, 22f); lineTo(6f, 22f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(14f, 2f); lineTo(14f, 8f); lineTo(20f, 8f); close()
        }
    }.build()

    // ---- Carga ----
    val Carga: ImageVector = Builder("Carga", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(5f, 6f); lineTo(19f, 6f); lineTo(19f, 18f); lineTo(5f, 18f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(9f, 10f); lineTo(15f, 10f); lineTo(15f, 14f); lineTo(9f, 14f); close()
        }
    }.build()

    // ---- Balanza ----
    val Balanza: ImageVector = Builder("Balanza", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(11f, 3f); lineTo(13f, 3f); lineTo(13f, 18f); lineTo(11f, 18f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(6f, 6f); lineTo(10f, 10f); lineTo(14f, 10f); lineTo(18f, 6f)
            lineTo(16f, 6f); lineTo(12f, 8f); lineTo(8f, 6f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(7f, 18f); lineTo(17f, 18f); lineTo(17f, 21f); lineTo(7f, 21f); close()
        }
    }.build()

    // ---- Cinturón ----
    val Cinturon: ImageVector = Builder("Cinturon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 8f); lineTo(9f, 8f); lineTo(9f, 16f); lineTo(4f, 16f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(15f, 8f); lineTo(20f, 8f); lineTo(20f, 16f); lineTo(15f, 16f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(10f, 10f); lineTo(14f, 10f); lineTo(14f, 14f); lineTo(10f, 14f); close()
        }
    }.build()

    // ---- Batería ----
    val Bateria: ImageVector = Builder("Bateria", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(5f,8f); lineTo(18f,8f); lineTo(18f,16f); lineTo(5f,16f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(18f,10f); lineTo(20f,10f); lineTo(20f,14f); lineTo(18f,14f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(9f,9f); lineTo(12f,9f); lineTo(11f,13f); lineTo(14f,13f); lineTo(10f,17f); lineTo(11f,13f); lineTo(8f,13f); close()
        }
    }.build()

    // ---- Interruptor ----
    val Interruptor: ImageVector = Builder("Interruptor", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(6f, 6f); lineTo(18f, 6f); lineTo(18f, 18f); lineTo(6f, 18f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(11f, 8f); lineTo(13f, 8f); lineTo(13f, 12f); lineTo(11f, 12f); close()
        }
    }.build()

    // ---- Radio ----
    val Radio: ImageVector = Builder("Radio", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(1f, 12f); lineTo(3f, 12f)
            curveTo(3f, 7.6f, 6.6f, 4f, 11f, 4f)
            lineTo(11f, 2f)
            curveTo(5.5f, 2f, 1f, 6.5f, 1f, 12f)
            close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(5f, 12f); lineTo(7f, 12f)
            curveTo(7f, 8.7f, 8.7f, 7f, 11f, 7f)
            lineTo(11f, 5f)
            curveTo(7.6f, 5f, 5f, 7.6f, 5f, 12f)
            close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(11f, 10f)
            curveTo(9.9f, 10f, 9f, 10.9f, 9f, 12f)
            curveTo(9f, 13.1f, 9.9f, 14f, 11f, 14f)
            curveTo(12.1f, 14f, 13f, 13.1f, 13f, 12f)
            curveTo(13f, 10.9f, 12.1f, 10f, 11f, 10f)
            close()
        }
    }.build()

    // ---- Freno ----
    val Freno: ImageVector = Builder("Freno", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 4f)
            curveTo(7.6f, 4f, 4f, 7.6f, 4f, 12f)
            curveTo(4f, 16.4f, 7.6f, 20f, 12f, 20f)
            curveTo(16.4f, 20f, 20f, 16.4f, 20f, 12f)
            curveTo(20f, 7.6f, 16.4f, 4f, 12f, 4f)
            close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(12f, 8f)
            curveTo(9.8f, 8f, 8f, 9.8f, 8f, 12f)
            curveTo(8f, 14.2f, 9.8f, 16f, 12f, 16f)
            curveTo(14.2f, 16f, 16f, 14.2f, 16f, 12f)
            curveTo(16f, 9.8f, 14.2f, 8f, 12f, 8f)
            close()
        }
    }.build()

    // ---- Estrangulador ----
    val Estrangulador: ImageVector = Builder("Estrangulador", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(8f, 4f); lineTo(16f, 4f); lineTo(12f, 12f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(6f, 12f); lineTo(18f, 12f); lineTo(18f, 20f); lineTo(6f, 20f); close()
        }
    }.build()

    // ---- Gases ----
    val Gases: ImageVector = Builder("Gases", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(9f, 3f); lineTo(15f, 3f); lineTo(15f, 21f); lineTo(9f, 21f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(11f, 7f); lineTo(13f, 7f); lineTo(13f, 10f); lineTo(11f, 10f); close()
        }
    }.build()

    // ---- Instrumentos ----
    val Instrumentos: ImageVector = Builder("Instrumentos", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f)
            curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
            curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
            curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f)
            curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f)
            close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(7f, 12f); lineTo(9f, 12f)
            moveTo(15f, 12f); lineTo(17f, 12f)
            moveTo(12f, 7f); lineTo(12f, 9f)
            moveTo(12f, 15f); lineTo(12f, 17f)
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(12f, 10f); lineTo(15f, 15f)
        }
    }.build()

    // ---- Botón ----
    val Boton: ImageVector = Builder("Boton", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 6f)
            curveTo(8.7f, 6f, 6f, 8.7f, 6f, 12f)
            curveTo(6f, 15.3f, 8.7f, 18f, 12f, 18f)
            curveTo(15.3f, 18f, 18f, 15.3f, 18f, 12f)
            curveTo(18f, 8.7f, 15.3f, 6f, 12f, 6f)
            close()
        }
    }.build()

    // ---- Generador ----
    val Generador: ImageVector = Builder("Generador", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 8f); lineTo(20f, 8f); lineTo(20f, 16f); lineTo(4f, 16f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(10f, 10f); lineTo(10f, 14f)
            moveTo(14f, 10f); lineTo(14f, 14f)
        }
    }.build()

    // ---- Bomba ----
    val Bomba: ImageVector = Builder("Bomba", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(8f, 6f); lineTo(16f, 6f); lineTo(16f, 18f); lineTo(8f, 18f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(10f, 10f); lineTo(14f, 12f); lineTo(10f, 14f); close()
        }
    }.build()

    // ---- Ignición ----
    val Ignicion: ImageVector = Builder("Ignicion", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f); lineTo(8f, 10f); lineTo(12f, 10f)
            lineTo(12f, 14f); lineTo(16f, 6f); lineTo(12f, 6f); close()
        }
    }.build()

    // ---- Refrigeración ----
    val Refrigeracion: ImageVector = Builder("Refrigeracion", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f); lineTo(12f, 22f)
            moveTo(6f, 12f); lineTo(18f, 12f)
            moveTo(8f, 6f); lineTo(16f, 18f)
            moveTo(16f, 6f); lineTo(8f, 18f)
        }
    }.build()

    // ---- Trim ----
    val Trim: ImageVector = Builder("Trim", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 10f); lineTo(20f, 10f); lineTo(18f, 6f); lineTo(6f, 6f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 14f); lineTo(20f, 14f); lineTo(18f, 18f); lineTo(6f, 18f); close()
        }
    }.build()

    // ---- Transponder ----
    val Transponder: ImageVector = Builder("Transponder", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(6f, 7f); lineTo(18f, 7f); lineTo(18f, 17f); lineTo(6f, 17f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(8f, 10f); lineTo(10f, 10f); lineTo(10f, 14f); lineTo(8f, 14f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(12f, 10f); lineTo(14f, 10f); lineTo(14f, 14f); lineTo(12f, 14f); close()
        }
    }.build()

    // ---- Brújula ----
    val Brujula: ImageVector = Builder("Brujula", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f)
            curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
            curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
            curveTo(17.5f, 22f, 22f, 17.5f, 22f, 12f)
            curveTo(22f, 6.5f, 17.5f, 2f, 12f, 2f)
            close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(12f, 6f); lineTo(10f, 12f); lineTo(12f, 18f); lineTo(14f, 12f); close()
        }
    }.build()

    // ---- Viento ----
    val Viento: ImageVector = Builder("Viento", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 8f); lineTo(14f, 8f)
            curveTo(15.1f, 8f, 16f, 7.1f, 16f, 6f)
            curveTo(16f, 4.9f, 15.1f, 4f, 14f, 4f)
            lineTo(12f, 4f)
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 12f); lineTo(18f, 12f)
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(4f, 16f); lineTo(16f, 16f)
            curveTo(17.1f, 16f, 18f, 16.9f, 18f, 18f)
            curveTo(18f, 19.1f, 17.1f, 20f, 16f, 20f)
            lineTo(14f, 20f)
        }
    }.build()

    // ---- Anemómetro ----
    val Anemometro: ImageVector = Builder("Anemometro", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 12f)
            curveTo(10.3f, 12f, 9f, 10.7f, 9f, 9f)
            curveTo(9f, 7.3f, 10.3f, 6f, 12f, 6f)
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 12f)
            curveTo(13.7f, 12f, 15f, 10.7f, 15f, 9f)
            curveTo(15f, 7.3f, 13.7f, 6f, 12f, 6f)
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 12f)
            curveTo(10.3f, 12f, 9f, 13.3f, 9f, 15f)
            curveTo(9f, 16.7f, 10.3f, 18f, 12f, 18f)
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(11f, 12f); lineTo(13f, 12f); lineTo(13f, 22f); lineTo(11f, 22f); close()
        }
    }.build()

    // ---- Paracaídas ----
    val Paracaidas: ImageVector = Builder("Paracaidas", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f)
            curveTo(6f, 2f, 2f, 6f, 2f, 10f)
            lineTo(6f, 16f); lineTo(10f, 20f); lineTo(14f, 20f); lineTo(18f, 16f)
            lineTo(22f, 10f)
            curveTo(22f, 6f, 18f, 2f, 12f, 2f)
            close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(10f, 20f); lineTo(10f, 10f)
            moveTo(14f, 20f); lineTo(14f, 10f)
        }
    }.build()

    // ---- Palanca ----
    val Palanca: ImageVector = Builder("Palanca", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(10f, 8f); lineTo(14f, 8f); lineTo(14f, 18f); lineTo(10f, 18f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 8f)
            curveTo(10.3f, 8f, 9f, 6.7f, 9f, 5f)
            curveTo(9f, 3.3f, 10.3f, 2f, 12f, 2f)
            curveTo(13.7f, 2f, 15f, 3.3f, 15f, 5f)
            curveTo(15f, 6.7f, 13.7f, 8f, 12f, 8f)
            close()
        }
    }.build()

    // ---- Calefacción ----
    val Calefaccion: ImageVector = Builder("Calefaccion", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(8f, 3f); lineTo(8f, 16f)
            curveTo(6.3f, 17f, 5f, 18.7f, 5f, 20.5f)
            curveTo(5f, 22.4f, 6.6f, 24f, 8.5f, 24f)
            curveTo(10.4f, 24f, 12f, 22.4f, 12f, 20.5f)
            curveTo(12f, 18.7f, 10.7f, 17f, 9f, 16f)
            lineTo(9f, 3f)
            close()
        }
    }.build()

    // ---- Salida ----
    val Salida: ImageVector = Builder("Salida", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(19f, 3f); lineTo(5f, 3f); lineTo(5f, 9f); lineTo(7f, 9f)
            lineTo(7f, 5f); lineTo(17f, 5f); lineTo(17f, 19f); lineTo(7f, 19f)
            lineTo(7f, 15f); lineTo(5f, 15f); lineTo(5f, 21f); lineTo(19f, 21f); close()
        }
        path(fill = SolidColor(primaryColor)) {
            moveTo(10f, 8f); lineTo(14f, 12f); lineTo(10f, 16f); lineTo(10f, 13f)
            lineTo(3f, 13f); lineTo(3f, 11f); lineTo(10f, 11f); close()
        }
    }.build()

    // ---- Seguro ----
    val Seguro: ImageVector = Builder("Seguro", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f); lineTo(4f, 6f); lineTo(4f, 12f)
            curveTo(4f, 16.4f, 7.6f, 20f, 12f, 22f)
            curveTo(16.4f, 20f, 20f, 16.4f, 20f, 12f)
            lineTo(20f, 6f); close()
        }
        path(fill = SolidColor(whiteColor)) {
            moveTo(10f, 14f); lineTo(8f, 12f); lineTo(9f, 11f); lineTo(10f, 12f)
            lineTo(14f, 8f); lineTo(15f, 9f); close()
        }
    }.build()

    // ---- Altímetro ----
    val Altimetro: ImageVector = Builder("Altimetro", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f,2f); lineTo(17f,3.5f); lineTo(20.5f,7f); lineTo(22f,12f)
            lineTo(20.5f,17f); lineTo(17f,20.5f); lineTo(12f,22f); lineTo(7f,20.5f)
            lineTo(3.5f,17f); lineTo(2f,12f); lineTo(3.5f,7f); lineTo(7f,3.5f); close()
        }
        path(fill = SolidColor(whiteColor)) { moveTo(12f,6f); lineTo(13f,12f); lineTo(12f,13f); lineTo(11f,12f); close() }
    }.build()

    // ---- Logo Taifun ----
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

    // ---- Micrófono ----
    val Microfono: ImageVector = Builder("Microfono", 24.dp, 24.dp, 24f, 24f).apply {
        // Cápsula del micrófono
        path(fill = SolidColor(primaryColor)) {
            moveTo(12f, 2f)
            curveTo(10.3f, 2f, 9f, 3.3f, 9f, 5f)
            lineTo(9f, 12f)
            curveTo(9f, 13.7f, 10.3f, 15f, 12f, 15f)
            curveTo(13.7f, 15f, 15f, 13.7f, 15f, 12f)
            lineTo(15f, 5f)
            curveTo(15f, 3.3f, 13.7f, 2f, 12f, 2f)
            close()
        }
        // Soporte inferior
        path(fill = SolidColor(primaryColor)) {
            moveTo(17f, 11f)
            curveTo(17f, 13.8f, 14.8f, 16f, 12f, 16f)
            curveTo(9.2f, 16f, 7f, 13.8f, 7f, 11f)
            lineTo(5f, 11f)
            curveTo(5f, 14.5f, 7.6f, 17.4f, 11f, 17.9f)
            lineTo(11f, 21f)
            lineTo(13f, 21f)
            lineTo(13f, 17.9f)
            curveTo(16.4f, 17.4f, 19f, 14.5f, 19f, 11f)
            close()
        }
        // Base
        path(fill = SolidColor(primaryColor)) {
            moveTo(9f, 22f)
            lineTo(15f, 22f)
            lineTo(15f, 23f)
            lineTo(9f, 23f)
            close()
        }
    }.build()

    // ---- Helper para YAML ----
    /**
     * Mapea nombres de iconos a ImageVectors.
     * Usa Material Icons Extended para conceptos genéricos y iconos custom para aviación específica.
     * Esto mejora la representatividad y consistencia visual.
     */
    fun iconFor(nombre: String?): ImageVector = when (nombre?.lowercase()) {
        // Iconos Material3 - Conceptos genéricos más representativos
        "check" -> Icons.Filled.CheckCircle
        "llave" -> Icons.Filled.Key
        "cabina" -> Icons.Filled.AirlineSeatReclineNormal
        "luz" -> Icons.Filled.Lightbulb
        "inspeccion" -> Icons.Filled.Search
        "documento" -> Icons.Filled.Description
        "carga" -> Icons.Filled.Inventory
        "balanza" -> Icons.Filled.Scale
        "cinturon" -> Icons.Filled.Security
        "bateria" -> Icons.Filled.BatteryFull
        "interruptor" -> Icons.Filled.ToggleOn
        "radio" -> Icons.Filled.Radio
        "freno" -> Icons.Filled.Block
        "boton" -> Icons.Filled.RadioButtonChecked
        "transponder" -> Icons.Filled.Sensors
        "brujula" -> Icons.Filled.Explore
        "viento" -> Icons.Filled.Air
        "paracaidas" -> Paracaidas  // Custom icon
        "salida" -> Icons.Filled.ExitToApp
        "seguro" -> Icons.Filled.Shield
        "puerto" -> Icons.Filled.Usb
        "calefaccion" -> Icons.Filled.Thermostat

        // Iconos custom - Específicos de aviación que no tienen equivalente en Material
        "flaps" -> Flaps
        "aerofreno" -> Aerofreno
        "aleron" -> Aleron
        "pitot" -> Pitot
        "combustible" -> Combustible
        "aceite" -> Aceite
        "motor" -> Motor
        "helice" -> Helice
        "tren" -> Tren
        "antena" -> Antena
        "timon" -> Timon
        "profundidad" -> Profundidad
        "control" -> Control
        "alas" -> Alas
        "vuelo" -> Icons.Filled.Flight
        "estrangulador" -> Estrangulador
        "gases" -> Icons.Filled.Speed
        "instrumentos" -> Icons.Filled.Dashboard
        "generador" -> Icons.Filled.PowerSettings
        "bomba" -> Bomba
        "ignicion" -> Icons.Filled.Bolt
        "refrigeracion" -> Icons.Filled.AcUnit
        "trim" -> Trim
        "anemometro" -> Anemometro
        "palanca" -> Icons.Filled.Tune
        "altimetro" -> Altimetro
        "taifun", "logo" -> TaifunLogo
        "microfono" -> Microfono
        else -> Icons.Filled.CheckCircle // default con icono Material más claro
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
     * Usa Material Icons Extended para mejor representatividad.
     */
    val availableIcons = listOf(
        // Controles de Vuelo
        IconOption("control", "Control", Control, "Controles"),
        IconOption("timon", "Timón", Timon, "Controles"),
        IconOption("profundidad", "Profundidad", Profundidad, "Controles"),
        IconOption("aleron", "Alerón", Aleron, "Controles"),
        IconOption("trim", "Trim", Trim, "Controles"),
        IconOption("palanca", "Palanca", Icons.Filled.Tune, "Controles"),

        // Superficies
        IconOption("flaps", "Flaps", Flaps, "Superficies"),
        IconOption("aerofreno", "Aerofreno", Aerofreno, "Superficies"),
        IconOption("alas", "Alas", Alas, "Superficies"),

        // Motor y Propulsión
        IconOption("motor", "Motor", Motor, "Motor"),
        IconOption("helice", "Hélice", Helice, "Motor"),
        IconOption("gases", "Gases", Icons.Filled.Speed, "Motor"),
        IconOption("estrangulador", "Estrangulador", Estrangulador, "Motor"),
        IconOption("ignicion", "Ignición", Icons.Filled.Bolt, "Motor"),
        IconOption("refrigeracion", "Refrigeración", Icons.Filled.AcUnit, "Motor"),

        // Combustible y Fluidos
        IconOption("combustible", "Combustible", Combustible, "Fluidos"),
        IconOption("aceite", "Aceite", Aceite, "Fluidos"),
        IconOption("bomba", "Bomba", Bomba, "Fluidos"),

        // Tren de Aterrizaje
        IconOption("tren", "Tren", Tren, "Tren"),
        IconOption("freno", "Freno", Icons.Filled.Block, "Tren"),

        // Eléctrico
        IconOption("bateria", "Batería", Icons.Filled.BatteryFull, "Eléctrico"),
        IconOption("generador", "Generador", Icons.Filled.PowerSettings, "Eléctrico"),
        IconOption("interruptor", "Interruptor", Icons.Filled.ToggleOn, "Eléctrico"),
        IconOption("luz", "Luz", Icons.Filled.Lightbulb, "Eléctrico"),
        IconOption("boton", "Botón", Icons.Filled.RadioButtonChecked, "Eléctrico"),

        // Aviónica
        IconOption("radio", "Radio", Icons.Filled.Radio, "Aviónica"),
        IconOption("transponder", "Transponder", Icons.Filled.Sensors, "Aviónica"),
        IconOption("antena", "Antena", Antena, "Aviónica"),

        // Instrumentos
        IconOption("instrumentos", "Instrumentos", Icons.Filled.Dashboard, "Instrumentos"),
        IconOption("altimetro", "Altímetro", Altimetro, "Instrumentos"),
        IconOption("brujula", "Brújula", Icons.Filled.Explore, "Instrumentos"),
        IconOption("anemometro", "Anemómetro", Anemometro, "Instrumentos"),

        // Inspección
        IconOption("inspeccion", "Inspección", Icons.Filled.Search, "Inspección"),
        IconOption("check", "Check", Icons.Filled.CheckCircle, "Inspección"),
        IconOption("llave", "Llave", Icons.Filled.Key, "Inspección"),
        IconOption("pitot", "Pitot", Pitot, "Inspección"),

        // Cabina
        IconOption("cabina", "Cabina", Icons.Filled.AirlineSeatReclineNormal, "Cabina"),
        IconOption("cinturon", "Cinturón", Icons.Filled.Security, "Cabina"),
        IconOption("puerto", "Puerto", Icons.Filled.Usb, "Cabina"),
        IconOption("calefaccion", "Calefacción", Icons.Filled.Thermostat, "Cabina"),

        // Seguridad
        IconOption("seguro", "Seguro", Icons.Filled.Shield, "Seguridad"),
        IconOption("salida", "Salida", Icons.Filled.ExitToApp, "Seguridad"),
        IconOption("paracaidas", "Paracaídas", Paracaidas, "Seguridad"),

        // Otros
        IconOption("vuelo", "Vuelo", Icons.Filled.Flight, "General"),
        IconOption("documento", "Documento", Icons.Filled.Description, "General"),
        IconOption("carga", "Carga", Icons.Filled.Inventory, "General"),
        IconOption("balanza", "Balanza", Icons.Filled.Scale, "General"),
        IconOption("viento", "Viento", Icons.Filled.Air, "General")
    )

    /**
     * Obtener opción de icono por ID
     */
    fun getIconOption(iconId: String?): IconOption? {
        if (iconId.isNullOrBlank()) return null
        return availableIcons.find { it.id.equals(iconId, ignoreCase = true) }
    }
}
