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
 */
object HapticUtils {
    /**
     * Realiza feedback háptico estándar para interacciones de botones
     * Usa FLAG_IGNORE_GLOBAL_SETTING para asegurar que el feedback se ejecute
     * incluso si el usuario ha deshabilitado el feedback háptico a nivel de sistema
     */
    fun performHapticFeedback(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * Realiza feedback háptico suave para interacciones menores
     */
    fun performLightHapticFeedback(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * Realiza feedback háptico fuerte para confirmaciones importantes
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
