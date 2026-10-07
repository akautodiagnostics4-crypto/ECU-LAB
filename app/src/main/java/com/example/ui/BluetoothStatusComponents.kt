package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bluetooth.BtConnectionState
import com.example.bluetooth.BtDeviceItem
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
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber

/**
 * Clean top-of-screen Bluetooth Connection Status Bar.
 * Hides all technical hardware/protocol details and shows only the friendly Bluetooth status.
 */
@Composable
fun Hc05StatusIndicatorBar(
    btState: BtConnectionState,
    packetsSent: Int,
    packetsReceived: Int,
    onOpenQuickManager: () -> Unit,
    onQuickAction: () -> Unit
) {
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onQuickAction()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bt_led_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led_alpha"
    )

    val (ledColor, statusTitle, statusSubtitle, isBusy) = when (btState) {
        is BtConnectionState.Connected -> Quad(
            SignalGreen,
            "BLUETOOTH CONNECTED",
            btState.deviceName,
            false
        )
        is BtConnectionState.Connecting -> Quad(
            WarningAmber,
            "CONNECTING...",
            btState.deviceName,
            true
        )
        is BtConnectionState.Pairing -> Quad(
            CyanGlow,
            "PAIRING...",
            btState.deviceName,
            true
        )
        is BtConnectionState.Scanning -> Quad(
            CyanGlow,
            "SCANNING BLUETOOTH...",
            "Searching for nearby devices",
            true
        )
        is BtConnectionState.Error -> Quad(
            StopRed,
            "BLUETOOTH DISCONNECTED",
            btState.message,
            false
        )
        BtConnectionState.Disconnected -> Quad(
            StopRed,
            "BLUETOOTH NOT CONNECTED",
            "Tap to select a Bluetooth device",
            false
        )
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = when (btState) {
            is BtConnectionState.Connected -> SignalGreen.copy(alpha = 0.6f)
            is BtConnectionState.Connecting,
            is BtConnectionState.Pairing,
            is BtConnectionState.Scanning -> CyanGlow.copy(alpha = 0.6f)
            is BtConnectionState.Error -> StopRed.copy(alpha = 0.6f)
            BtConnectionState.Disconnected -> BorderSubtle
        },
        label = "bt_bar_border"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenQuickManager() }
            .testTag("hc05_global_status_bar"),
        shape = RoundedCornerShape(12.dp),
        color = CardDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, animatedBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ledColor.copy(alpha = 0.15f))
                        .border(1.dp, ledColor.copy(alpha = if (isBusy) pulseAlpha else 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ledColor
                        )
                    } else {
                        Icon(
                            imageVector = when (btState) {
                                is BtConnectionState.Connected -> Icons.Default.BluetoothConnected
                                is BtConnectionState.Scanning -> Icons.Default.BluetoothSearching
                                else -> Icons.Default.BluetoothDisabled
                            },
                            contentDescription = "Bluetooth Status",
                            tint = ledColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alpha(if (isBusy || btState is BtConnectionState.Connected) pulseAlpha else 1f)
                                .clip(CircleShape)
                                .background(ledColor)
                                .testTag("hc05_status_led")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("hc05_status_title")
                        )
                    }
                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (btState is BtConnectionState.Connected) SignalGreen else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val actionLabel = if (btState is BtConnectionState.Connected) "CONNECTED" else "PAIR"
            val actionBg = if (btState is BtConnectionState.Connected) SignalGreenBg else ElectricBlue.copy(alpha = 0.2f)
            val actionTextColor = if (btState is BtConnectionState.Connected) SignalGreen else CyanGlow

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(actionBg)
                    .border(1.dp, actionTextColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        if (btState is BtConnectionState.Disconnected || btState is BtConnectionState.Error) {
                            onOpenQuickManager()
                            permissionLauncher.launch(permissionsToRequest)
                        } else {
                            onOpenQuickManager()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("hc05_quick_action_button")
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = actionTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * Clean Modal Bottom Sheet displaying only the Bluetooth device list for pairing and connecting.
 * Hides all technical hardware naming conventions, MAC addresses, and RSSI numbers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Hc05ConnectionBottomSheet(
    btState: BtConnectionState,
    pairedDevices: List<BtDeviceItem>,
    discoveredDevices: List<BtDeviceItem>,
    isScanning: Boolean,
    packetsSent: Int,
    packetsReceived: Int,
    onDismiss: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectPaired: (BtDeviceItem) -> Unit,
    onPairAndConnect: (BtDeviceItem) -> Unit,
    onDisconnect: () -> Unit,
    onSendTestPing: () -> Unit,
    onGoToDeviceTab: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    val scanPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onStartScan()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardElevatedSurface,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .testTag("hc05_connection_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BLUETOOTH DEVICES",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select a Bluetooth device to pair and connect",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (btState is BtConnectionState.Connected) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DeepObsidian,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SignalGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = btState.deviceName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.labelSmall,
                                color = SignalGreen
                            )
                        }
                        OutlinedButton(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StopRed),
                            modifier = Modifier.testTag("sheet_disconnect_button")
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DISCONNECT")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (isScanning) onStopScan() else scanPermissionLauncher.launch(permissionsToRequest)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) StopRed else ElectricBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sheet_scan_hc05_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScanning) "STOP SCANNING" else "SCAN FOR BLUETOOTH DEVICES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pairedDevices.isNotEmpty()) {
                    item {
                        Text(
                            text = "PAIRED DEVICES",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanGlow
                        )
                    }
                    items(pairedDevices, key = { "paired_${it.address}" }) { device ->
                        CleanBtDeviceRowCard(
                            device = device,
                            statusLabel = "Paired",
                            actionLabel = "CONNECT",
                            onClick = { onConnectPaired(device) }
                        )
                    }
                }

                if (discoveredDevices.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AVAILABLE DEVICES",
                            style = MaterialTheme.typography.labelSmall,
                            color = SignalGreen
                        )
                    }
                    items(discoveredDevices, key = { "disc_${it.address}" }) { device ->
                        CleanBtDeviceRowCard(
                            device = device,
                            statusLabel = "Available to pair",
                            actionLabel = "PAIR",
                            onClick = { onPairAndConnect(device) }
                        )
                    }
                }

                if (pairedDevices.isEmpty() && discoveredDevices.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap 'SCAN FOR BLUETOOTH DEVICES' to find nearby devices for pairing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CleanBtDeviceRowCard(
    device: BtDeviceItem,
    statusLabel: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("sheet_bt_device_${device.address}"),
        shape = RoundedCornerShape(10.dp),
        color = CardDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (device.isHc05Candidate) ElectricBlue else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (device.isPaired) Icons.Default.Bluetooth else Icons.Default.BluetoothSearching,
                    contentDescription = null,
                    tint = if (device.isHc05Candidate) CyanGlow else TextSecondary,
                    modifier = Modifier.size(20.dp)
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
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ButtonDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
