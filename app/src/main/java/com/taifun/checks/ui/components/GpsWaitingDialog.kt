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
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Required GPS accuracy for logging (in meters)
 */
const val GPS_REQUIRED_ACCURACY_M = 50f

/**
 * Timeout for waiting for GPS fix (in milliseconds)
 */
const val GPS_WAIT_TIMEOUT_MS = 30_000L

/**
 * Dialog that waits for accurate GPS fix before allowing log save
 *
 * @param accuracy Current GPS accuracy in meters (null if unknown)
 * @param altitude Current GPS altitude in meters (null if no fix)
 * @param hasValidAltitude Whether the altitude was actually measured by GPS (not just 0.0 default)
 * @param fixAgeMs Age of the last GPS fix in milliseconds (null if no fix)
 * @param onDismiss Called when user cancels
 * @param onSaveAnyway Called when user chooses to save with current (inaccurate) data
 * @param onKeepSearching Called when user wants to continue waiting for GPS
 * @param onGpsReady Called when GPS meets accuracy requirements
 */
@Composable
fun GpsWaitingDialog(
    accuracy: Float?,
    altitude: Double?,
    hasValidAltitude: Boolean = true,
    fixAgeMs: Long? = null,
    onDismiss: () -> Unit,
    onSaveAnyway: () -> Unit,
    onKeepSearching: () -> Unit = {},
    onGpsReady: () -> Unit
) {
    // Check if GPS is good enough using enhanced validation
    val isGpsGood = isGpsAccurateForLogging(accuracy, altitude, hasValidAltitude, fixAgeMs)

    // Track if we've timed out - use key to allow reset
    var timeoutKey by remember { mutableStateOf(0) }
    var hasTimedOut by remember { mutableStateOf(false) }

    // Auto-close when GPS is good
    LaunchedEffect(isGpsGood) {
        if (isGpsGood) {
            onGpsReady()
        }
    }

    // Timeout after GPS_WAIT_TIMEOUT_MS - reset when timeoutKey changes
    LaunchedEffect(timeoutKey) {
        hasTimedOut = false
        delay(GPS_WAIT_TIMEOUT_MS)
        if (!isGpsGood) {
            hasTimedOut = true
        }
    }

    // Calculate fix age for display
    val fixAgeSeconds = fixAgeMs?.let { it / 1000 }
    val isFixStale = fixAgeMs != null && fixAgeMs > SensorDataRepository.MAX_FIX_AGE_MS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (hasTimedOut)
                    stringResource(R.string.gps_timeout_title)
                else
                    stringResource(R.string.gps_waiting_title)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!hasTimedOut) {
                    // Show progress indicator while waiting
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = stringResource(R.string.gps_waiting_message),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Show timeout message
                    Text(
                        text = stringResource(R.string.gps_timeout),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Show current accuracy
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
            if (hasTimedOut) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Keep searching button
                    TextButton(onClick = {
                        timeoutKey++ // Reset timeout
                        onKeepSearching()
                    }) {
                        Text(stringResource(R.string.gps_keep_searching))
                    }
                    // Save anyway button
                    TextButton(onClick = onSaveAnyway) {
                        Text(stringResource(R.string.gps_save_anyway))
                    }
                }
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
 * 3. For internal GPS: accuracy <= 50m
 * 4. For Bluetooth/NMEA GPS: if accuracy not available, skip accuracy check
 *
 * @param accuracy GPS accuracy in meters (null for NMEA GPS without accuracy data)
 * @param altitude GPS altitude in meters
 * @param hasValidAltitude Whether altitude was actually measured (not default 0.0)
 * @param fixAgeMs Age of the GPS fix in milliseconds (null if unknown)
 * @return true if GPS data is accurate enough for logging
 */
fun isGpsAccurateForLogging(
    accuracy: Float?,
    altitude: Double?,
    hasValidAltitude: Boolean = true,
    fixAgeMs: Long? = null
): Boolean {
    // Must have altitude in all cases
    if (altitude == null) return false

    // Altitude must be actually measured, not default 0.0
    if (!hasValidAltitude) return false

    // Fix must not be too old (if we have age info)
    if (fixAgeMs != null && fixAgeMs > SensorDataRepository.MAX_FIX_AGE_MS) return false

    // If accuracy is null (e.g., Bluetooth/NMEA GPS without accuracy data), skip accuracy check
    if (accuracy == null) return true

    // Otherwise require accuracy <= 50m
    return accuracy <= GPS_REQUIRED_ACCURACY_M
}
