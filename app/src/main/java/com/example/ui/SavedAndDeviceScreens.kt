package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
 * Clean Bluetooth Device Discovery & Pairing Screen ("DEVICE" tab).
 * Hides all technical hardware naming conventions, MAC addresses, serial protocols,
 * baud rates, and raw logs, displaying ONLY the Bluetooth device list for pairing and connecting,
 * followed by the Contact Information card (`akautodiagnostics4@gmail.com`).
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
    lastDeviceAck: String?,
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
    loggedInUsername: String = "admin",
    showChangeCredentialsDialog: Boolean = false,
    onOpenChangeCredentials: () -> Unit = {},
    onDismissChangeCredentials: () -> Unit = {},
    onSaveNewCredentials: (String, String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

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
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BLUETOOTH",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Select a Bluetooth device below to pair and connect",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            strokeWidth = 2.5.dp,
                            color = CyanGlow
                        )
                    }
                }
            }

            // Connection Status & Scan Controls Card
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
                                        is BtConnectionState.Connected -> btState.deviceName
                                        is BtConnectionState.Connecting -> "Connecting to ${btState.deviceName}..."
                                        is BtConnectionState.Pairing -> "Pairing with ${btState.deviceName}..."
                                        is BtConnectionState.Scanning -> "Scanning for Bluetooth devices..."
                                        is BtConnectionState.Error -> "Bluetooth Disconnected"
                                        BtConnectionState.Disconnected -> "Not Connected"
                                    }
                                    val subText = when (btState) {
                                        is BtConnectionState.Connected -> "Bluetooth Connected"
                                        is BtConnectionState.Error -> btState.message
                                        else -> "Tap Scan to discover nearby Bluetooth devices"
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
                                        color = when (btState) {
                                            is BtConnectionState.Connected -> SignalGreen
                                            is BtConnectionState.Error -> StopRed
                                            else -> TextSecondary
                                        }
                                    )
                                }
                            }

                            if (btState is BtConnectionState.Connected) {
                                OutlinedButton(
                                    onClick = onDisconnect,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StopRed),
                                    modifier = Modifier.testTag("disconnect_hc05_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LinkOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DISCONNECT", fontWeight = FontWeight.Bold)
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
                                    text = if (isScanning) "STOP SCAN" else "SCAN DEVICES",
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
                                    text = "REFRESH LIST",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Paired Bluetooth Devices List
            item {
                Text(
                    text = "PAIRED BLUETOOTH DEVICES",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
                    letterSpacing = 1.4.sp
                )
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No paired Bluetooth devices found. Tap 'SCAN DEVICES' above to search for available devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(pairedDevices, key = { "paired_${it.address}" }) { device ->
                    val isCurrentlyConnected =
                        (btState is BtConnectionState.Connected) && btState.address == device.address
                    BluetoothDeviceListCard(
                        deviceName = device.name,
                        subtitle = if (isCurrentlyConnected) "Connected" else "Paired",
                        actionText = if (isCurrentlyConnected) "CONNECTED" else "CONNECT",
                        isConnected = isCurrentlyConnected,
                        testTag = "bt_device_${device.address}",
                        onClick = {
                            if (!isCurrentlyConnected) {
                                onConnectDevice(device)
                            }
                        }
                    )
                }
            }

            // Discovered Available Bluetooth Devices List (for Pairing)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AVAILABLE BLUETOOTH DEVICES",
                    style = MaterialTheme.typography.labelLarge,
                    color = SignalGreen,
                    letterSpacing = 1.4.sp
                )
            }

            if (discoveredDevices.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isScanning) {
                                "Scanning for nearby Bluetooth devices..."
                            } else {
                                "Tap 'SCAN DEVICES' to discover nearby Bluetooth devices for pairing."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(discoveredDevices, key = { "disc_${it.address}" }) { device ->
                    BluetoothDeviceListCard(
                        deviceName = device.name,
                        subtitle = "Available to pair",
                        actionText = "PAIR",
                        isConnected = false,
                        testTag = "disc_bt_device_${device.address}",
                        onClick = { onPairAndConnectDevice(device) }
                    )
                }
            }

            // Contact Information Section (akautodiagnostics4@gmail.com)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CONTACT INFORMATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
                    letterSpacing = 1.5.sp
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_info_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
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
                                    text = "ECU LAB • Multi-Vehicle ECU Simulator & Test Bench",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DeepObsidian)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                .clickable {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:akautodiagnostics4@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "ECU LAB Support & Inquiry")
                                    }
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (_: Exception) {}
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("contact_email_row")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Contact Email",
                                tint = CyanGlow,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Support & Inquiries Email",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = "akautodiagnostics4@gmail.com",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Account & Login Security Section (Change Username / Password & Log Out)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ACCOUNT & LOGIN SECURITY",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Signed in as: $loggedInUsername",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Manage your app login username and password",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = onOpenChangeCredentials,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("change_credentials_button")
                            ) {
                                Text(
                                    text = "CHANGE PASSWORD",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onLogout,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StopRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("logout_button")
                            ) {
                                Text(
                                    text = "LOG OUT",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChangeCredentialsDialog) {
        ChangeCredentialsDialog(
            currentUsername = loggedInUsername,
            onConfirm = onSaveNewCredentials,
            onDismiss = onDismissChangeCredentials
        )
    }
}

@Composable
private fun ChangeCredentialsDialog(
    currentUsername: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var newUsername by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentUsername) }
    var newPassword by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkSurface,
        titleContentColor = TextWhite,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "Set Login Username & Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter the new username and password required to log into ECU LAB.",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("New Username") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_username_input")
                )
                androidx.compose.material3.OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_password_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newUsername, newPassword) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.testTag("save_new_credentials_button")
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun BluetoothDeviceListCard(
    deviceName: String,
    subtitle: String,
    actionText: String,
    isConnected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = CardDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) SignalGreen else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) SignalGreenBg else DeepObsidian),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (isConnected) SignalGreen else CyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isConnected) SignalGreen else TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isConnected) SignalGreenBg else ButtonDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Link,
                    contentDescription = null,
                    tint = if (isConnected) SignalGreen else CyanGlow,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isConnected) SignalGreen else CyanGlow,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
