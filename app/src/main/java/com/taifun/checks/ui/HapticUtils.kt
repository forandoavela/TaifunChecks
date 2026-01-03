package com.taifun.checks.ui

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.taifun.checks.data.SettingsRepository

/**
 * Utilidad para realizar feedback háptico en la interfaz
 * Optimizado para uso en aviación: feedback más fuerte para uso con guantes
 * y en condiciones de turbulencia
 */
object HapticUtils {
    /**
     * Realiza feedback háptico estándar para interacciones de botones
     * Usa CONTEXT_CLICK que es más fuerte que VIRTUAL_KEY, apropiado para
     * uso en cabina con guantes de vuelo
     * FLAG_IGNORE_GLOBAL_SETTING asegura que funcione independientemente
     * de la configuración del sistema
     */
    fun performHapticFeedback(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONTEXT_CLICK,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * Realiza feedback háptico suave para interacciones menores
     * Usa CONTEXT_CLICK en lugar de CLOCK_TICK para mejor percepción
     * en condiciones de vuelo
     */
    fun performLightHapticFeedback(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONTEXT_CLICK,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * Realiza feedback háptico fuerte para confirmaciones importantes
     * Usa LONG_PRESS que es el feedback más fuerte disponible
     * universalmente (API < 30), ideal para acciones críticas
     */
    fun performStrongHapticFeedback(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}

/**
 * Extension function para facilitar el uso del feedback háptico
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackHelper {
    val view = LocalView.current
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository(context) }
    val hapticsEnabled by settingsRepo.hapticsFlow.collectAsState(initial = true)
    return HapticFeedbackHelper(view, hapticsEnabled)
}

/**
 * Helper class para manejar feedback háptico
 */
class HapticFeedbackHelper(private val view: View, private val enabled: Boolean) {
    fun performHapticFeedback() {
        if (enabled) {
            HapticUtils.performHapticFeedback(view)
        }
    }

    fun performLightFeedback() {
        if (enabled) {
            HapticUtils.performLightHapticFeedback(view)
        }
    }

    fun performStrongFeedback() {
        if (enabled) {
            HapticUtils.performStrongHapticFeedback(view)
        }
    }
}
