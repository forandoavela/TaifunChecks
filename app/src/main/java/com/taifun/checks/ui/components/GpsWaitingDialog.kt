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
 * @param onDismiss Called when user cancels
 * @param onSaveAnyway Called when user chooses to save with current (inaccurate) data
 * @param onGpsReady Called when GPS meets accuracy requirements
 */
@Composable
fun GpsWaitingDialog(
    accuracy: Float?,
    altitude: Double?,
    onDismiss: () -> Unit,
    onSaveAnyway: () -> Unit,
    onGpsReady: () -> Unit
) {
    // Check if GPS is good enough using same logic as isGpsAccurateForLogging
    // For Bluetooth/NMEA GPS without accuracy data, only require altitude
    val isGpsGood = isGpsAccurateForLogging(accuracy, altitude)

    // Track if we've timed out
    var hasTimedOut by remember { mutableStateOf(false) }

    // Auto-close when GPS is good
    LaunchedEffect(isGpsGood) {
        if (isGpsGood) {
            onGpsReady()
        }
    }

    // Timeout after GPS_WAIT_TIMEOUT_MS
    LaunchedEffect(Unit) {
        delay(GPS_WAIT_TIMEOUT_MS)
        if (!isGpsGood) {
            hasTimedOut = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (hasTimedOut)
                    stringResource(R.string.gps_timeout).substringBefore(".")
                else
                    stringResource(R.string.gps_waiting_title)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                // For NMEA GPS without accuracy data, show neutral color if altitude is available
                Text(
                    text = if (accuracy != null) {
                        stringResource(R.string.gps_accuracy_current, String.format(Locale.US, "%.0f m", accuracy))
                    } else {
                        stringResource(R.string.gps_accuracy_unknown)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        accuracy != null && accuracy <= GPS_REQUIRED_ACCURACY_M -> MaterialTheme.colorScheme.primary
                        accuracy == null && altitude != null -> MaterialTheme.colorScheme.onSurfaceVariant // NMEA GPS OK
                        else -> MaterialTheme.colorScheme.error
                    }
                )

                // Show altitude status
                Text(
                    text = if (altitude != null) {
                        String.format(Locale.US, stringResource(R.string.gps_altitude_ok), altitude)
                    } else {
                        stringResource(R.string.gps_altitude_waiting)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (altitude != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        },
        confirmButton = {
            if (hasTimedOut) {
                TextButton(onClick = onSaveAnyway) {
                    Text(stringResource(R.string.gps_save_anyway))
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
 * For internal GPS: requires accuracy <= 50m and valid altitude
 * For Bluetooth/NMEA GPS: if accuracy data is not available (null),
 * only requires valid altitude (skip accuracy verification)
 *
 * @param accuracy GPS accuracy in meters (null for NMEA GPS without accuracy data)
 * @param altitude GPS altitude in meters
 * @return true if GPS data is accurate enough for logging
 */
fun isGpsAccurateForLogging(accuracy: Float?, altitude: Double?): Boolean {
    // Must have altitude in all cases
    if (altitude == null) return false
    // If accuracy is null (e.g., Bluetooth/NMEA GPS without accuracy data), skip accuracy check
    if (accuracy == null) return true
    // Otherwise require accuracy <= 50m
    return accuracy <= GPS_REQUIRED_ACCURACY_M
}
