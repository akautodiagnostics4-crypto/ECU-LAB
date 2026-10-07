package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BtConnectionState
import com.example.bluetooth.BtDeviceItem
import com.example.bluetooth.SerialPacketMode
import com.example.data.EcuPresetEntity
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ButtonDarkSurface
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.CardElevatedSurface
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenBg
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite

/**
 * Saved Presets Screen (Room Database backed).
 * Lets technicians recall saved ECU test configurations or delete old ones.
 * Again, no crank tooth pattern is shown on the display.
 */
@Composable
fun SavedPresetsScreen(
    presets: List<EcuPresetEntity>,
    onLoadPreset: (EcuPresetEntity) -> Unit,
    onDeletePreset: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .testTag("saved_presets_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "SAVED ECU PRESETS",
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            )
            Text(
                text = "Quickly load saved bench test parameters into the signal generator",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (presets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Saved Presets Yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Adjust RPM, Rail Pressure, ECT, Speed, MAP Boost, and MAF in the Control tab, then tap the Save icon in the top-right corner.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onLoadPreset(preset) }
                                .testTag("preset_item_${preset.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = CardDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.presetName,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${preset.brand} • ${preset.modelName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyanGlow
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = { onLoadPreset(preset) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ElectricBlue.copy(alpha = 0.18f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Load Preset",
                                                tint = CyanGlow
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeletePreset(preset.id) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ButtonDarkSurface)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Preset",
                                                tint = StopRed
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PresetMetricBadge("RPM", "${preset.rpm}")
                                    PresetMetricBadge("RAIL", "${preset.railPressure}b")
                                    PresetMetricBadge("ECT", "${preset.ectTemp}°C")
                                    PresetMetricBadge("MAP", "${preset.boostMapKpa}kPa")
                                    PresetMetricBadge("MAF", "${preset.mafGramsSec}g/s")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetMetricBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DeepObsidian)
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

/**
 * Full HC-05 Bluetooth Connection Manager & Serial Command Console ("DEVICE" tab).
 * Includes:
 * - Paired device list + Active discovery scan for unpaired HC-05 modules
 * - In-app OS pairing (`createBond()`) + RFCOMM SPP connection
 * - Selectable Serial Packet Format (Extended 11-Ch vs Standard 6-Ch)
 * - Custom Serial Command Sender + Quick Diagnostic Macro Buttons
 * - Bi-directional TX/RX serial monitor
 */
@Composable
fun DeviceScreen(
    btState: BtConnectionState,
    pairedDevices: List<BtDeviceItem>,
    discoveredDevices: List<BtDeviceItem>,
    isScanning: Boolean,
    autoReconnect: Boolean,
    packetMode: SerialPacketMode,
    txLog: List<String>,
    packetsSent: Int,
    packetsReceived: Int,
    lastArduinoAck: String?,
    showSketchModal: Boolean,
    onRefreshPairedDevices: () -> Unit,
    onStartDiscoveryScan: () -> Unit,
    onStopDiscoveryScan: () -> Unit,
    onConnectDevice: (BtDeviceItem) -> Unit,
    onPairAndConnectDevice: (BtDeviceItem) -> Unit,
    onDisconnect: () -> Unit,
    onToggleAutoReconnect: (Boolean) -> Unit,
    onSelectPacketMode: (SerialPacketMode) -> Unit,
    onSendCustomCommand: (String) -> Unit,
    onSendTestPing: () -> Unit,
    onClearLogs: () -> Unit,
    onToggleSketchModal: (Boolean) -> Unit
) {
    var customCmdText by remember { mutableStateOf("") }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val refreshLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onRefreshPairedDevices()
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onStartDiscoveryScan()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .testTag("device_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "DEVICE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            item {
                Text(
                    text = "HC-05 BLUETOOTH CONNECTION MANAGER",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
                    letterSpacing = 1.5.sp
                )
            }

            // Primary Connection Status & Action Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (btState is BtConnectionState.Connected) SignalGreen else BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val isConnected = btState is BtConnectionState.Connected
                                Icon(
                                    imageVector = when (btState) {
                                        is BtConnectionState.Connected -> Icons.Default.BluetoothConnected
                                        is BtConnectionState.Scanning,
                                        is BtConnectionState.Pairing,
                                        is BtConnectionState.Connecting -> Icons.Default.BluetoothSearching
                                        else -> Icons.Default.BluetoothDisabled
                                    },
                                    contentDescription = null,
                                    tint = if (isConnected) SignalGreen else CyanGlow,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    val titleText = when (btState) {
                                        is BtConnectionState.Connected -> "Connected: ${btState.deviceName}"
                                        is BtConnectionState.Connecting -> "Connecting to ${btState.deviceName}..."
                                        is BtConnectionState.Pairing -> "Pairing with ${btState.deviceName}..."
                                        is BtConnectionState.Scanning -> "Scanning for HC-05 Modules..."
                                        is BtConnectionState.Error -> "HC-05 Connection Alert"
                                        BtConnectionState.Disconnected -> "HC-05 Bluetooth Classic SPP"
                                    }
                                    val subText = when (btState) {
                                        is BtConnectionState.Connected -> "MAC: ${btState.address} • TX: $packetsSent  RX: $packetsReceived"
                                        is BtConnectionState.Error -> btState.message
                                        else -> "Values are sent live to the Arduino Uno + HC-05 while connected"
                                    }
                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (btState is BtConnectionState.Error) StopRed else TextSecondary
                                    )
                                    if (lastArduinoAck != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Last Arduino RX: $lastArduinoAck",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SignalGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isScanning) {
                                        onStopDiscoveryScan()
                                    } else {
                                        scanLauncher.launch(permissionsToRequest)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isScanning) StopRed else ElectricBlue
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scan_unpaired_hc05_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isScanning) "STOP SCAN" else "SCAN HC-05",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { refreshLauncher.launch(permissionsToRequest) },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scan_paired_hc05_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REFRESH PAIRED",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (btState is BtConnectionState.Connected) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onSendTestPing,
                                    colors = ButtonDefaults.buttonColors(containerColor = SignalGreenBg),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("send_ping_hc05_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = SignalGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PING ARDUINO", color = SignalGreen, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onDisconnect,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StopRed),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("disconnect_hc05_button")
                                ) {
                                    Text("DISCONNECT", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Auto-Reconnect HC-05 Link",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Automatically retry connection if Arduino power cycles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = autoReconnect,
                                onCheckedChange = onToggleAutoReconnect,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = ElectricBlue
                                ),
                                modifier = Modifier.testTag("auto_reconnect_switch")
                            )
                        }
                    }
                }
            }

            // Paired Bluetooth Devices Section
            if (pairedDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "PAIRED BLUETOOTH DEVICES (TAP TO CONNECT)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow
                    )
                }
                items(pairedDevices, key = { "paired_${it.address}" }) { device ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onConnectDevice(device) }
                            .testTag("bt_device_${device.address}"),
                        shape = RoundedCornerShape(12.dp),
                        color = CardDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (device.isHc05Candidate) ElectricBlue else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (device.isHc05Candidate) CyanGlow else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${device.address} • PAIRED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Text(
                                text = "CONNECT",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Discovered Unpaired Devices Section (In-App Pairing)
            if (discoveredDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "DISCOVERED NEARBY DEVICES (TAP TO PAIR WITH HC-05)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SignalGreen
                    )
                }
                items(discoveredDevices, key = { "disc_${it.address}" }) { device ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPairAndConnectDevice(device) }
                            .testTag("disc_bt_device_${device.address}"),
                        shape = RoundedCornerShape(12.dp),
                        color = CardDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (device.isHc05Candidate) SignalGreen else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothSearching,
                                    contentDescription = null,
                                    tint = SignalGreen
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val rssiLabel = device.rssi?.let { " • $it dBm" } ?: ""
                                    Text(
                                        text = "${device.address}$rssiLabel • UNPAIRED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Text(
                                text = "PAIR & CONNECT",
                                style = MaterialTheme.typography.labelLarge,
                                color = SignalGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Arduino Uno + HC-05 Serial Packet Format Selector & Custom Command Console
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ARDUINO UNO + HC-05 SERIAL PROTOCOL",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SerialPacketMode.entries.forEach { mode ->
                                val selected = packetMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectPacketMode(mode) },
                                    label = {
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricBlue,
                                        selectedLabelColor = TextWhite,
                                        containerColor = DeepObsidian,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Text(
                            text = packetMode.description,
                            style = MaterialTheme.typography.labelLarge,
                            color = CyanGlow
                        )
                        Text(
                            text = "Ranges: RPM 200-2500 | RAIL 100-1000 | ECT 0-200 | SPD 0-220 | MAP 100-300 | MAF 0-500",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Custom Serial Command Input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = customCmdText,
                                onValueChange = { customCmdText = it },
                                placeholder = {
                                    Text("Custom command (e.g. \$CAL,1 or \$RESET)", color = TextMuted)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DeepObsidian,
                                    unfocusedContainerColor = DeepObsidian,
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_serial_cmd_input")
                            )

                            Button(
                                onClick = {
                                    if (customCmdText.isNotBlank()) {
                                        onSendCustomCommand(customCmdText)
                                        customCmdText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("send_custom_cmd_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send Command")
                            }
                        }

                        // Quick Diagnostic Macro Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "START" to "START\\n",
                                "STOP" to "STOP\\n",
                                "RPM=800" to "RPM=800\\n",
                                "RAIL=450" to "RAIL=450\\n",
                                "ECT=80" to "ECT=80\\n"
                            ).forEach { (cmd, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ButtonDarkSurface)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { onSendCustomCommand(cmd) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Arduino Uno Firmware Sketch Viewer Button
            item {
                OutlinedButton(
                    onClick = { onToggleSketchModal(true) },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("view_arduino_sketch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = CyanGlow
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIEW ARDUINO UNO + HC-05 WIRING & CODE",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanGlow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Live Serial Telemetry Log
            if (txLog.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE SERIAL TX/RX MONITOR (9600 BAUD)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SignalGreen
                                )
                                Text(
                                    text = "CLEAR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanGlow,
                                    modifier = Modifier.clickable { onClearLogs() }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            txLog.take(10).forEach { line ->
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bench Technician Profile Card
            item {
                Text(
                    text = "BENCH PROFILE",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    letterSpacing = 1.5.sp
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AK",
                                style = MaterialTheme.typography.titleLarge,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "AK Auto Diagnostics",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ECU LAB PRO • Multi-Vehicle Test Bench",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSketchModal) {
        ArduinoUnoSketchDialog(onDismiss = { onToggleSketchModal(false) })
    }
}

@Composable
private fun ArduinoUnoSketchDialog(onDismiss: () -> Unit) {
    val sketchCode = """
        // AK ECU LAB PRO - Arduino Uno + HC-05 Simulator Firmware
        // Connections:
        // HC-05 TX -> Arduino Pin 10 (SoftwareSerial RX)
        // HC-05 RX -> Arduino Pin 11 (SoftwareSerial TX via voltage divider)
        // Pin 2    -> CKP Crank Signal Output
        // Pin 3    -> CMP Cam Sync Pulse Output
        // Pin 4    -> Injector Load Pulse Output
        // Pin 5    -> Rail Pressure PWM (RC Filter -> 0.5V - 4.5V)
        // Pin 6    -> ECT Temp PWM
        // Pin 9    -> Vehicle Speed (VSS) Pulse Output
        // Pin 10/A0-> MAP Boost / MAF Analog PWM Output
        
        #include <SoftwareSerial.h>
        SoftwareSerial hc05(10, 11); // RX, TX
        
        int rpm = 800;
        int railBar = 300;
        int ectTemp = 90;
        int vssSpeed = 0;
        int accelPct = 0;
        int mapKpa = 105;
        int mafGs = 18;
        int camSync = 1;
        int injPulse = 1;
        int runSignal = 0;
        
        void setup() {
          Serial.begin(9600);
          hc05.begin(9600);
          pinMode(2, OUTPUT);
          pinMode(3, OUTPUT);
          pinMode(4, OUTPUT);
          pinMode(5, OUTPUT);
          pinMode(6, OUTPUT);
          pinMode(9, OUTPUT);
          hc05.println("ACK:ARDUINO_UNO_READY");
        }
        
        void loop() {
          if (hc05.available()) {
            String cmd = hc05.readStringUntil('\n');
            cmd.trim();
            if (cmd == "START") {
              runSignal = 1;
              hc05.println("ACK:STARTED");
            } else if (cmd == "STOP") {
              runSignal = 0;
              digitalWrite(2, LOW);
              digitalWrite(3, LOW);
              hc05.println("ACK:STOPPED");
            } else if (cmd.startsWith("RPM=")) {
              rpm = cmd.substring(4).toInt();
              hc05.println("ACK:RPM=" + String(rpm));
            } else if (cmd.startsWith("RAIL=")) {
              railBar = cmd.substring(5).toInt();
              analogWrite(5, map(railBar, 100, 1000, 25, 230));
              hc05.println("ACK:RAIL=" + String(railBar));
            } else if (cmd.startsWith("ECT=")) {
              ectTemp = cmd.substring(4).toInt();
              analogWrite(6, map(ectTemp, 0, 200, 230, 20));
              hc05.println("ACK:ECT=" + String(ectTemp));
            } else if (cmd.startsWith("SPEED=")) {
              vssSpeed = cmd.substring(6).toInt();
            } else if (cmd.startsWith("ACCEL=")) {
              accelPct = cmd.substring(6).toInt();
            } else if (cmd.startsWith("MAP=")) {
              mapKpa = cmd.substring(4).toInt();
            } else if (cmd.startsWith("MAF=")) {
              mafGs = cmd.substring(4).toInt();
            }
          }
        }
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardElevatedSurface,
        titleContentColor = TextWhite,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "Arduino Uno + HC-05 Pinout & Sketch",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepObsidian)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = sketchCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}
