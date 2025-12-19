package com.taifun.checks

/**
 * Constantes de configuración de la aplicación.
 * Centraliza valores mágicos para facilitar su mantenimiento.
 */
object AppConfig {

    // ============================================================
    // DELAYS Y TIMEOUTS
    // ============================================================

    /** Delay para debounce en búsquedas y validación YAML (ms) */
    const val DEBOUNCE_DELAY_MS = 500L

    /** Delay corto para animaciones y transiciones (ms) */
    const val SHORT_DELAY_MS = 300L

    /** Timeout base para reconexión Bluetooth (ms) */
    const val BT_RECONNECT_BASE_DELAY_MS = 2000L

    /** Máximo de intentos de reconexión Bluetooth */
    const val BT_MAX_RECONNECT_ATTEMPTS = 3

    // ============================================================
    // UI
    // ============================================================
    // Nota: Las constantes GPS (GPS_REQUIRED_ACCURACY_M, GPS_WAIT_TIMEOUT_MS) están
    // definidas en GpsWaitingDialog.kt para mantener la cohesión con el componente

    /** Altura aproximada de items en lista (dp) */
    const val LIST_ITEM_HEIGHT_DP = 80

    /** Número de pasos para slider de distancia ICAO */
    const val ICAO_DISTANCE_SLIDER_STEPS = 18

    /** Número de pasos para slider de altitud ICAO */
    const val ICAO_ALTITUDE_SLIDER_STEPS = 48
}
