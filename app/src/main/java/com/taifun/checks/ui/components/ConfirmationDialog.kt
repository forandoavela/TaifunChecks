package com.taifun.checks.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.taifun.checks.R
import com.taifun.checks.ui.HapticFeedbackHelper

/**
 * Diálogo de confirmación reutilizable para acciones destructivas o importantes.
 *
 * @param title Título del diálogo
 * @param message Mensaje/texto explicativo del diálogo
 * @param confirmText Texto del botón de confirmar (default: "Aceptar")
 * @param dismissText Texto del botón de cancelar (default: "Cancelar")
 * @param confirmColor Color del texto del botón de confirmar (default: primary)
 * @param onConfirm Callback cuando se confirma la acción
 * @param onDismiss Callback cuando se cancela/cierra el diálogo
 * @param haptic Opcional: instancia de HapticFeedback para feedback táctil
 * @param isDestructive Si es true, usa color de error para el botón de confirmar
 *
 * Ejemplo de uso:
 * ```kotlin
 * if (showDeleteDialog) {
 *     ConfirmationDialog(
 *         title = stringResource(R.string.delete_title),
 *         message = stringResource(R.string.delete_message),
 *         confirmText = stringResource(R.string.delete),
 *         isDestructive = true,
 *         onConfirm = {
 *             // Ejecutar borrado
 *             showDeleteDialog = false
 *         },
 *         onDismiss = { showDeleteDialog = false },
 *         haptic = haptic
 *     )
 * }
 * ```
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.aceptar),
    dismissText: String = stringResource(R.string.cancelar),
    confirmColor: Color? = null,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    haptic: HapticFeedbackHelper? = null
) {
    val finalConfirmColor = confirmColor
        ?: if (isDestructive) MaterialTheme.colorScheme.error else Color.Unspecified

    AlertDialog(
        onDismissRequest = {
            haptic?.performLightFeedback()
            onDismiss()
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                haptic?.performStrongFeedback()
                onConfirm()
            }) {
                Text(
                    text = confirmText,
                    color = finalConfirmColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic?.performLightFeedback()
                onDismiss()
            }) {
                Text(dismissText)
            }
        }
    )
}

/**
 * Versión simplificada del diálogo de confirmación para borrado.
 * Usa automáticamente el color de error y el texto por defecto para borrar.
 *
 * @param title Título del diálogo
 * @param message Mensaje/texto explicativo del diálogo
 * @param onConfirm Callback cuando se confirma el borrado
 * @param onDismiss Callback cuando se cancela/cierra el diálogo
 * @param haptic Opcional: instancia de HapticFeedback para feedback táctil
 */
@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    haptic: HapticFeedbackHelper? = null
) {
    ConfirmationDialog(
        title = title,
        message = message,
        confirmText = stringResource(R.string.delete),
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        haptic = haptic
    )
}
