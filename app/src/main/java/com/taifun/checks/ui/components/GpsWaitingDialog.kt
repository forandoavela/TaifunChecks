package com.taifun.checks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taifun.checks.R
import com.taifun.checks.data.SensorDataRepository
import java.util.Locale

/**
 * Required GPS horizontal accuracy for logging (in meters)
 */
const val GPS_REQUIRED_ACCURACY_M = 50f

/**
 * Dialog that waits for accurate GPS fix before allowing log save.
 * Continues searching indefinitely until user takes action or GPS becomes accurate.
 *
 * @param accuracy Current GPS horizontal accuracy in meters (null if unknown)
 * @param verticalAccuracy Current GPS vertical accuracy in meters (null if unknown/API < 26)
 * @param altitude Current GPS altitude in meters (null if no fix)
 * @param hasValidAltitude Whether the altitude was actually measured by GPS (not just 0.0 default)
 * @param fixAgeMs Age of the last GPS fix in milliseconds (null if no fix)
 * @param requiredVerticalAccuracy Required vertical accuracy in meters (from ICAO altitude diff setting)
 * @param onDismiss Called when user cancels
 * @param onSaveAnyway Called when user chooses to save with current (inaccurate) data
 * @param onGpsReady Called when GPS meets accuracy requirements
 */
@Composable
fun GpsWaitingDialog(
    accuracy: Float?,
    verticalAccuracy: Float? = null,
    altitude: Double?,
    hasValidAltitude: Boolean = true,
    fixAgeMs: Long? = null,
    requiredVerticalAccuracy: Float? = null,
    onDismiss: () -> Unit,
    onSaveAnyway: () -> Unit,
    onGpsReady: () -> Unit
) {
    // Check if GPS is good enough using enhanced validation
    val isGpsGood = isGpsAccurateForLogging(accuracy, verticalAccuracy, altitude, hasValidAltitude, fixAgeMs, requiredVerticalAccuracy)

    // Auto-close when GPS is good
    LaunchedEffect(isGpsGood) {
        if (isGpsGood) {
            onGpsReady()
        }
    }

    // Calculate fix age for display
    val fixAgeSeconds = fixAgeMs?.let { it / 1000 }
    val isFixStale = fixAgeMs != null && fixAgeMs > SensorDataRepository.MAX_FIX_AGE_MS

    // Check if vertical accuracy meets requirements
    val isVerticalAccuracyGood = when {
        requiredVerticalAccuracy == null -> true // No requirement
        verticalAccuracy == null -> true // Not available, skip check
        else -> verticalAccuracy <= requiredVerticalAccuracy
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.gps_waiting_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show progress indicator while waiting
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = stringResource(R.string.gps_waiting_message),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Show current horizontal accuracy
                Text(
                    text = if (accuracy != null) {
                        stringResource(R.string.gps_accuracy_current, String.format(Locale.US, "%.0f m", accuracy))
                    } else {
                        stringResource(R.string.gps_accuracy_unknown)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        accuracy != null && accuracy <= GPS_REQUIRED_ACCURACY_M -> MaterialTheme.colorScheme.primary
                        accuracy == null && altitude != null && hasValidAltitude -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.error
                    }
                )

                // Show vertical accuracy if available and required
                if (requiredVerticalAccuracy != null) {
                    Text(
                        text = if (verticalAccuracy != null) {
                            stringResource(R.string.gps_vertical_accuracy_current, String.format(Locale.US, "%.0f m", verticalAccuracy))
                        } else {
                            stringResource(R.string.gps_vertical_accuracy_unknown)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            verticalAccuracy != null && isVerticalAccuracyGood -> MaterialTheme.colorScheme.primary
                            verticalAccuracy == null -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                // Show altitude status (with validity check)
                Text(
                    text = when {
                        altitude != null && hasValidAltitude ->
                            String.format(Locale.US, stringResource(R.string.gps_altitude_ok), altitude)
                        altitude != null && !hasValidAltitude ->
                            stringResource(R.string.gps_altitude_invalid)
                        else ->
                            stringResource(R.string.gps_altitude_waiting)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        altitude != null && hasValidAltitude -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
                )

                // Show fix age status
                if (fixAgeSeconds != null) {
                    Text(
                        text = stringResource(R.string.gps_fix_age, fixAgeSeconds),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFixStale) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        },
        confirmButton = {
            // Always show save anyway button
            TextButton(onClick = onSaveAnyway) {
                Text(stringResource(R.string.gps_save_anyway))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

/**
 * Checks if GPS data meets logging requirements
 *
 * Requirements:
 * 1. Must have valid altitude (actually measured, not default 0.0)
 * 2. Fix must be recent (within MAX_FIX_AGE_MS, default 60 seconds)
 * 3. For internal GPS: horizontal accuracy <= 50m
 * 4. For Bluetooth/NMEA GPS: if accuracy not available, skip accuracy check
 * 5. If vertical accuracy is available and required, it must be <= requiredVerticalAccuracy
 *
 * @param accuracy GPS horizontal accuracy in meters (null for NMEA GPS without accuracy data)
 * @param verticalAccuracy GPS vertical accuracy in meters (null if unknown/API < 26)
 * @param altitude GPS altitude in meters
 * @param hasValidAltitude Whether altitude was actually measured (not default 0.0)
 * @param fixAgeMs Age of the GPS fix in milliseconds (null if unknown)
 * @param requiredVerticalAccuracy Required vertical accuracy in meters (null to skip check)
 * @return true if GPS data is accurate enough for logging
 */
fun isGpsAccurateForLogging(
    accuracy: Float?,
    verticalAccuracy: Float? = null,
    altitude: Double?,
    hasValidAltitude: Boolean = true,
    fixAgeMs: Long? = null,
    requiredVerticalAccuracy: Float? = null
): Boolean {
    // Must have altitude in all cases
    if (altitude == null) return false

    // Altitude must be actually measured, not default 0.0
    if (!hasValidAltitude) return false

    // Fix must not be too old (if we have age info)
    if (fixAgeMs != null && fixAgeMs > SensorDataRepository.MAX_FIX_AGE_MS) return false

    // Check horizontal accuracy if available
    // If accuracy is null (e.g., Bluetooth/NMEA GPS without accuracy data), skip accuracy check
    if (accuracy != null && accuracy > GPS_REQUIRED_ACCURACY_M) return false

    // Check vertical accuracy if both available and required
    if (requiredVerticalAccuracy != null && verticalAccuracy != null) {
        if (verticalAccuracy > requiredVerticalAccuracy) return false
    }

    return true
}
