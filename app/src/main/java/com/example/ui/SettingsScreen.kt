package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.bluetooth.BtConnectionState
import com.example.data.StimulationHistoryEntity
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ButtonDarkSurface
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlossBlueBottom
import com.example.ui.theme.GlossBlueTop
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MetalHighlightEdge
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.brushedMetalPanel
import com.example.ui.theme.carbonFiberBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings Screen for AK ECU STIMULATOR:
 * - Bluetooth Auto-Connect switch
 * - Bluetooth Devices
 * - Saved ECU Presets
 * - History (every ECU & Sensor stimulation run)
 * - Contact (Hardware Requirement & Support Email `akautodiagnostics4@gmail.com`)
 * - Bottom Showcase featuring Bus & Heavy Truck image
 */
@Composable
fun SettingsScreen(
    btState: BtConnectionState,
    autoReconnect: Boolean,
    onToggleAutoReconnect: (Boolean) -> Unit,
    onBack: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onOpenSavedPresets: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenContact: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("settings_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubScreenTopBar(
                    title = "Settings",
                    btState = btState,
                    onBack = onBack,
                    onOpenBluetoothSheet = onOpenBluetoothSettings,
                    backTestTag = "settings_back_button"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bluetooth Auto-Connect Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brushedMetalPanel(cornerRadius = 14.dp, borderColor = BorderSubtle, elevated = true)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(RecessedWell)
                                    .border(1.dp, CyanGlow.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothConnected,
                                    contentDescription = "Bluetooth Auto Connect",
                                    tint = if (autoReconnect) SignalGreen else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Bluetooth Auto-Connect",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (autoReconnect) "Enabled on launch" else "Manual connection only",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = autoReconnect,
                            onCheckedChange = onToggleAutoReconnect,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = SignalGreen
                            ),
                            modifier = Modifier.testTag("settings_bt_autoconnect_switch")
                        )
                    }

                    SettingsMenuRowCard(
                        icon = Icons.Default.Bluetooth,
                        iconBgColors = listOf(GlossBlueTop, GlossBlueBottom),
                        iconTint = TextWhite,
                        title = "Bluetooth Devices",
                        subtitle = "Pair & manage hardware connection",
                        testTag = "settings_bluetooth_item",
                        onClick = onOpenBluetoothSettings
                    )

                    SettingsMenuRowCard(
                        icon = Icons.Default.FolderOpen,
                        iconBgColors = listOf(Color(0xFF283A5A), Color(0xFF152238)),
                        iconTint = TitaniumSilver,
                        title = "Saved ECU Presets",
                        subtitle = "Load or remove saved test bench presets",
                        testTag = "settings_signal_item",
                        onClick = onOpenSavedPresets
                    )

                    SettingsMenuRowCard(
                        icon = Icons.Default.History,
                        iconBgColors = listOf(Color(0xFF0E4D42), Color(0xFF072B24)),
                        iconTint = SignalGreen,
                        title = "History",
                        subtitle = "View every ECU & Sensor stimulation log",
                        testTag = "settings_history_item",
                        onClick = onOpenHistory
                    )

                    SettingsMenuRowCard(
                        icon = Icons.Default.SupportAgent,
                        iconBgColors = listOf(Color(0xFF3E3114), Color(0xFF1D1708)),
                        iconTint = GoldAccent,
                        title = "Contact",
                        subtitle = "Hardware requirement & support email",
                        testTag = "settings_contact_item",
                        onClick = onOpenContact
                    )
                }
            }

            // Bottom Showcase featuring Bus & Truck Image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HighFiAppLogoBadge(size = 58.dp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "AK ECU STIMULATOR",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 21.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    ),
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Version : 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Powering Your ECU Tests !",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoldAccent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_bus_truck_showcase_1794747200092),
                    contentDescription = "AK ECU STIMULATOR Commercial Truck and Bus Showcase",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(1.dp, CyanGlow.copy(alpha = 0.45f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/**
 * Contact Screen inside Settings:
 * Displays Hardware Required notice + Contact Information (`akautodiagnostics4@gmail.com`).
 */
@Composable
fun ContactScreen(
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("contact_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SubScreenTopBar(
                title = "Contact & Hardware",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "contact_back_button"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hardware Required Notice Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .brushedMetalPanel(
                            cornerRadius = 14.dp,
                            borderColor = WarningAmber.copy(alpha = 0.7f),
                            borderWidth = 1.2.dp,
                            elevated = true
                        )
                        .padding(16.dp)
                        .testTag("contact_hardware_required_notice"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RecessedWell)
                            .border(1.dp, WarningAmber.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Hardware Required",
                            tint = WarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HARDWARE REQUIRED",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "You need an external AK ECU Stimulator Bluetooth hardware unit to use this application and generate live bench signals.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Contact Information Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .brushedMetalPanel(
                            cornerRadius = 14.dp,
                            borderColor = ElectricBlue.copy(alpha = 0.65f),
                            elevated = true
                        )
                        .testTag("contact_info_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HighFiAppLogoBadge(size = 48.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AK Auto Diagnostics",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "AK ECU STIMULATOR • Version 1.0.0",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldAccent
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RecessedWell)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                .clickable {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:akautodiagnostics4@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "AK ECU STIMULATOR Inquiry")
                                    }
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (_: Exception) {
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 14.dp)
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
        }
    }
}

/**
 * Stimulation History Screen inside Settings:
 * Lists every ECU Stimulation & Sensor Stimulation run recorded in Room.
 */
@Composable
fun StimulationHistoryScreen(
    historyEntries: List<StimulationHistoryEntity>,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onDeleteEntry: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("stimulation_history_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
        ) {
            SubScreenTopBar(
                title = "Stimulation History",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "history_back_button",
                trailingContent = {
                    if (historyEntries.isNotEmpty()) {
                        IconButton(
                            onClick = onClearAll,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RecessedWell)
                                .border(1.dp, StopRed.copy(alpha = 0.6f), CircleShape)
                                .testTag("history_clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = StopRed,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            )

            if (historyEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Stimulation History Yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Every time you start ECU Stimulation or Sensor Stimulation, your test details will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyEntries, key = { it.id }) { entry ->
                        val isEcu = entry.modeType == "ECU STIMULATION"
                        val badgeColor = if (isEcu) CyanGlow else SignalGreen

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .brushedMetalPanel(
                                    cornerRadius = 14.dp,
                                    borderColor = badgeColor.copy(alpha = 0.55f),
                                    elevated = true
                                )
                                .padding(14.dp)
                                .testTag("history_entry_${entry.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(RecessedWell)
                                            .border(1.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = entry.modeType,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Text(
                                        text = dateFormatter.format(Date(entry.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = entry.vehicleLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = entry.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            IconButton(
                                onClick = { onDeleteEntry(entry.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ButtonDarkSurface)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete History Entry",
                                    tint = StopRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuRowCard(
    icon: ImageVector,
    iconBgColors: List<Color>,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(cornerRadius = 14.dp, borderColor = BorderSubtle, elevated = true)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(iconBgColors))
                    .border(1.dp, MetalHighlightEdge, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
