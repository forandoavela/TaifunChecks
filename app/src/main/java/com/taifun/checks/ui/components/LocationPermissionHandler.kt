package com.taifun.checks.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

/**
 * Data class que contiene el launcher y funciones útiles para manejar permisos de ubicación.
 */
data class LocationPermissionState(
    val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    val requestPermissions: () -> Unit
)

/**
 * Permisos de ubicación requeridos para la app.
 */
val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * Verifica si los permisos de ubicación están concedidos.
 *
 * @param context Contexto para verificar permisos
 * @return true si al menos uno de los permisos está concedido
 */
fun hasLocationPermission(context: Context): Boolean {
    return LOCATION_PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Composable que crea y recuerda un launcher para solicitar permisos de ubicación.
 * Simplifica el manejo de permisos de ubicación en múltiples pantallas.
 *
 * @param onPermissionGranted Callback cuando al menos un permiso es concedido
 * @param onPermissionDenied Callback opcional cuando todos los permisos son denegados
 * @return LocationPermissionState con el launcher y función para solicitar permisos
 *
 * Ejemplo de uso:
 * ```kotlin
 * val locationPermission = rememberLocationPermissionHandler(
 *     onPermissionGranted = { sensorRepo.startLocationTracking() },
 *     onPermissionDenied = { Toast.makeText(ctx, "Permiso denegado", Toast.LENGTH_SHORT).show() }
 * )
 *
 * // Solicitar permisos
 * locationPermission.requestPermissions()
 * ```
 */
@Composable
fun rememberLocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: (() -> Unit)? = null
): LocationPermissionState {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            onPermissionGranted()
        } else {
            onPermissionDenied?.invoke()
        }
    }

    val requestPermissions = remember(launcher) {
        { launcher.launch(LOCATION_PERMISSIONS) }
    }

    return remember(launcher, requestPermissions) {
        LocationPermissionState(
            launcher = launcher,
            requestPermissions = requestPermissions
        )
    }
}
