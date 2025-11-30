package com.taifun.checks.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.taifun.checks.R
import com.taifun.checks.data.BluetoothGpsRepository
import com.taifun.checks.data.GpsSource
import com.taifun.checks.data.SensorDataRepository
import com.taifun.checks.data.SettingsRepository
import com.taifun.checks.ui.rememberHapticFeedback
import kotlinx.coroutines.launch

/**
 * Bluetooth GPS configuration section for SettingsScreen
 * Allows user to select GPS source (internal/Bluetooth) and connect to Bluetooth GPS devices
 */
@Composable
fun BluetoothGpsSettings(
    settingsRepo: SettingsRepository,
    sensorDataRepo: SensorDataRepository,
    bluetoothGpsRepo: BluetoothGpsRepository,
    bluetoothVarioRepo: BluetoothGpsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    // Settings
    val gpsSource by settingsRepo.gpsSourceFlow.collectAsState(initial = "INTERNAL")
    val btDeviceName by settingsRepo.btGpsDeviceNameFlow.collectAsState(initial = null)
    val btDeviceAddress by settingsRepo.btGpsDeviceAddressFlow.collectAsState(initial = null)
    val btAutoConnect by settingsRepo.btGpsAutoConnectFlow.collectAsState(initial = false)

    // Variometer settings
    val btVarioDeviceName by settingsRepo.btVarioDeviceNameFlow.collectAsState(initial = null)
    val btVarioDeviceAddress by settingsRepo.btVarioDeviceAddressFlow.collectAsState(initial = null)
    val btVarioAutoConnect by settingsRepo.btVarioAutoConnectFlow.collectAsState(initial = false)

    // Bluetooth state
    val isConnected by bluetoothGpsRepo.isConnected.collectAsState()
    val connectionStatus by bluetoothGpsRepo.connectionStatus.collectAsState()
    val nmeaData by bluetoothGpsRepo.nmeaData.collectAsState()

    // Variometer Bluetooth state
    val isVarioConnected by bluetoothVarioRepo.isConnected.collectAsState()
    val varioConnectionStatus by bluetoothVarioRepo.connectionStatus.collectAsState()
    val varioNmeaData by bluetoothVarioRepo.nmeaData.collectAsState()

    // UI state
    var showDeviceDialog by remember { mutableStateOf(false) }
    var showVarioDeviceDialog by remember { mutableStateOf(false) }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var hasBluetoothPermission by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasBluetoothPermission = permissions.values.all { it }
        if (hasBluetoothPermission) {
            pairedDevices = bluetoothGpsRepo.getPairedDevices()
        }
    }

    // Check permissions on first composition
    LaunchedEffect(Unit) {
        hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (hasBluetoothPermission) {
            pairedDevices = bluetoothGpsRepo.getPairedDevices()

            // Auto-connect if enabled and not connected
            if (btAutoConnect && btDeviceAddress != null && !isConnected) {
                bluetoothGpsRepo.connect(btDeviceAddress!!)
            }
        }

        // If device has no GPS hardware, automatically switch to Bluetooth
        if (!sensorDataRepo.hasGpsHardware() && gpsSource == "INTERNAL") {
            settingsRepo.setGpsSource("BLUETOOTH")
            sensorDataRepo.setGpsSource(GpsSource.BLUETOOTH)
        }
    }

    // Note: MainActivity now handles updating SensorDataRepository with NMEA data
    // This ensures the Bluetooth GPS connection persists across screen changes

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section title
        Text(
            text = stringResource(R.string.gps_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.medium
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // GPS Source selection
                Text(
                    text = stringResource(R.string.gps_source_label),
                    style = MaterialTheme.typography.bodyLarge
                )

                // Internal GPS option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            stringResource(R.string.gps_source_internal),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        // Show message if device has no GPS hardware
                        if (!sensorDataRepo.hasGpsHardware()) {
                            Text(
                                stringResource(R.string.gps_not_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    RadioButton(
                        selected = gpsSource == "INTERNAL",
                        enabled = sensorDataRepo.hasGpsHardware(), // Disable if no GPS hardware
                        onClick = {
                            haptic.performHapticFeedback()
                            scope.launch {
                                settingsRepo.setGpsSource("INTERNAL")
                                sensorDataRepo.setGpsSource(GpsSource.INTERNAL)
                                bluetoothGpsRepo.disconnect()
                            }
                        }
                    )
                }

                // Bluetooth GPS option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            stringResource(R.string.gps_source_bluetooth),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            stringResource(R.string.gps_source_bluetooth_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RadioButton(
                        selected = gpsSource == "BLUETOOTH",
                        onClick = {
                            haptic.performHapticFeedback()
                            scope.launch {
                                settingsRepo.setGpsSource("BLUETOOTH")
                                sensorDataRepo.setGpsSource(GpsSource.BLUETOOTH)
                            }
                        }
                    )
                }

                // Bluetooth configuration (only show if Bluetooth source is selected)
                if (gpsSource == "BLUETOOTH") {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Connection status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.bt_gps_status),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isConnected -> Icons.Default.BluetoothConnected
                                        bluetoothGpsRepo.isBluetoothAvailable() -> Icons.Default.Bluetooth
                                        else -> Icons.Default.BluetoothDisabled
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isConnected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = when {
                                        isConnected -> "$connectionStatus"
                                        btDeviceName != null -> stringResource(R.string.bt_gps_disconnected, btDeviceName!!)
                                        else -> stringResource(R.string.bt_gps_no_device)
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // GPS data indicator (when connected)
                    if (isConnected && nmeaData.latitude != null) {
                        Text(
                            text = stringResource(
                                R.string.bt_gps_data,
                                nmeaData.latitude ?: 0.0,
                                nmeaData.longitude ?: 0.0,
                                nmeaData.altitude ?: 0.0
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Vario/Barometer data indicator (when connected and LK8EX1 data available)
                    if (isConnected && (nmeaData.pressure != null || nmeaData.vario != null)) {
                        Text(
                            text = buildString {
                                append("Vario: ")
                                nmeaData.pressure?.let { append("P: %.1f hPa  ".format(it)) }
                                nmeaData.baroAltitude?.let { append("Alt: %.0fm  ".format(it)) }
                                nmeaData.vario?.let {
                                    append("V: %+.1f m/s".format(it))
                                }
                                nmeaData.temperature?.let { append("  T: %.1f°C".format(it)) }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Select device button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback()
                            if (!hasBluetoothPermission) {
                                // Request permission
                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                } else {
                                    arrayOf(Manifest.permission.BLUETOOTH)
                                }
                                permissionLauncher.launch(permissions)
                            } else {
                                pairedDevices = bluetoothGpsRepo.getPairedDevices()
                                showDeviceDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (btDeviceName != null) {
                                stringResource(R.string.bt_gps_change_device)
                            } else {
                                stringResource(R.string.bt_gps_select_device)
                            }
                        )
                    }

                    // Connect/Disconnect button
                    if (btDeviceAddress != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback()
                                    scope.launch {
                                        if (isConnected) {
                                            bluetoothGpsRepo.disconnect()
                                        } else {
                                            bluetoothGpsRepo.connect(btDeviceAddress!!)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (isConnected) {
                                        stringResource(R.string.bt_gps_disconnect)
                                    } else {
                                        stringResource(R.string.bt_gps_connect)
                                    }
                                )
                            }

                            // Reconnect button (only show when disconnected)
                            if (!isConnected) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback()
                                        scope.launch {
                                            bluetoothGpsRepo.autoReconnect(btDeviceAddress!!)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.bt_gps_reconnect))
                                }
                            }
                        }
                    }

                    // Auto-connect option
                    if (btDeviceAddress != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = stringResource(R.string.bt_gps_auto_connect),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.bt_gps_auto_connect_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = btAutoConnect,
                                onCheckedChange = {
                                    haptic.performHapticFeedback()
                                    scope.launch {
                                        settingsRepo.setBtGpsAutoConnect(it)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Variometer Card (independent device)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.vario_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.medium
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description
                Text(
                    text = stringResource(R.string.vario_section_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                // Connection status
                Text(
                    text = stringResource(R.string.bt_vario_status),
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusIcon = when {
                        isVarioConnected -> Icons.Default.BluetoothConnected
                        btVarioDeviceAddress != null -> Icons.Default.Bluetooth
                        else -> Icons.Default.BluetoothDisabled
                    }

                    val statusText = when {
                        isVarioConnected -> btVarioDeviceName ?: btVarioDeviceAddress ?: "Connected"
                        btVarioDeviceAddress != null -> stringResource(
                            R.string.bt_vario_disconnected,
                            btVarioDeviceName ?: btVarioDeviceAddress ?: ""
                        )
                        else -> stringResource(R.string.bt_vario_no_device)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            statusIcon,
                            contentDescription = null,
                            tint = if (isVarioConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isVarioConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Device selection/change button
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback()
                            if (hasBluetoothPermission) {
                                showVarioDeviceDialog = true
                            } else {
                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                } else {
                                    arrayOf(Manifest.permission.BLUETOOTH)
                                }
                                permissionLauncher.launch(permissions)
                            }
                        },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (btVarioDeviceAddress != null) R.string.bt_vario_change_device
                                else R.string.bt_vario_select_device
                            )
                        )
                    }
                }

                // Show variometer data when connected
                if (isVarioConnected && (varioNmeaData.pressure != null || varioNmeaData.vario != null || varioNmeaData.baroAltitude != null)) {
                    HorizontalDivider()

                    Text(
                        text = buildString {
                            varioNmeaData.vario?.let { append("Vario: %+.1f m/s  ".format(it)) }
                            varioNmeaData.pressure?.let { append("P: %.1f hPa  ".format(it)) }
                            varioNmeaData.baroAltitude?.let { append("Alt: %.0fm  ".format(it)) }
                            varioNmeaData.temperature?.let { append("T: %.1f°C".format(it)) }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Connection buttons (only show if device is configured)
                if (btVarioDeviceAddress != null) {
                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Connect/Disconnect button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback()
                                scope.launch {
                                    if (isVarioConnected) {
                                        bluetoothVarioRepo.disconnect()
                                    } else {
                                        bluetoothVarioRepo.connect(btVarioDeviceAddress!!)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isVarioConnected) Icons.Default.BluetoothDisabled else Icons.Default.Bluetooth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(
                                    if (isVarioConnected) R.string.bt_vario_disconnect
                                    else R.string.bt_vario_connect
                                )
                            )
                        }

                        // Reconnect button (only when disconnected)
                        if (!isVarioConnected) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback()
                                    scope.launch {
                                        bluetoothVarioRepo.disconnect()
                                        bluetoothVarioRepo.connect(btVarioDeviceAddress!!)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.bt_vario_reconnect))
                            }
                        }
                    }

                    // Auto-connect switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                stringResource(R.string.bt_vario_auto_connect),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                stringResource(R.string.bt_vario_auto_connect_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = btVarioAutoConnect,
                            onCheckedChange = {
                                scope.launch {
                                    settingsRepo.setBtVarioAutoConnect(it)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Device selection dialog
    if (showDeviceDialog) {
        BluetoothDeviceDialog(
            devices = pairedDevices,
            currentDeviceAddress = btDeviceAddress,
            onDeviceSelected = { device ->
                haptic.performHapticFeedback()
                scope.launch {
                    settingsRepo.setBtGpsDevice(device.name, device.address)
                    // Auto-connect to new device
                    bluetoothGpsRepo.connect(device.address)
                }
                showDeviceDialog = false
            },
            onDismiss = {
                showDeviceDialog = false
            }
        )
    }

    // Variometer device selection dialog
    if (showVarioDeviceDialog) {
        BluetoothDeviceDialog(
            devices = pairedDevices,
            currentDeviceAddress = btVarioDeviceAddress,
            onDeviceSelected = { device ->
                haptic.performHapticFeedback()
                scope.launch {
                    settingsRepo.setBtVarioDevice(device.name, device.address)
                    // Auto-connect to new device
                    bluetoothVarioRepo.connect(device.address)
                }
                showVarioDeviceDialog = false
            },
            onDismiss = {
                showVarioDeviceDialog = false
            },
            title = stringResource(R.string.bt_vario_select_device)
        )
    }
}

/**
 * Dialog to select a Bluetooth device from paired devices
 */
@SuppressLint("MissingPermission")
@Composable
private fun BluetoothDeviceDialog(
    devices: List<BluetoothDevice>,
    currentDeviceAddress: String?,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onDismiss: () -> Unit,
    title: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title ?: stringResource(R.string.bt_gps_select_device)) },
        text = {
            if (devices.isEmpty()) {
                Text(stringResource(R.string.bt_gps_no_paired_devices))
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.bt_gps_paired_devices),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    devices.forEach { device ->
                        val isSelected = device.address == currentDeviceAddress
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeviceSelected(device) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name ?: stringResource(R.string.bt_gps_unknown_device),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = device.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.BluetoothConnected,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (device != devices.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
