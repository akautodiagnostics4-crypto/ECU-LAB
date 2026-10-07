package com.example.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.bluetooth.BtConnectionState
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
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AccentColorTheme
import com.example.viewmodel.EcuControlUiState
import com.example.viewmodel.EcuLabViewModel
import kotlin.math.roundToInt

/**
 * Upgraded ECU SIGNAL LAB Control Screen:
 * - Preserves strict rule: DONT SHOW THE CRANK TOOTH PATTERN ON THE DISPLAY
 * - Live Digital Gauge Cluster (RPM, Rail Pressure, ECT, Speed)
 * - Quick Bench Mode Selector (CRANKING, IDLE, CRUISE, FULL LOAD)
 * - Accent Theme Switcher (Cyber Blue, Diagnostic Green, Turbo Amber, Motorsport Red)
 * - Cam Sync (CMP) & Injector Load Pulse Hardware Toggles
 * - 7 Real-Time Parameter Cards:
 *   1. Engine RPM (200 - 2500 rpm)
 *   2. Rail Pressure (100 - 1000 bar)
 *   3. Coolant Temp ECT (0 - 200 °C)
 *   4. Vehicle Speed VSS (0 - 220 km/h)
 *   5. Accelerator Pedal APP (0 - 100 %)
 *   6. Boost Pressure / MAP (100 - 300 kPa)
 *   7. Mass Air Flow / MAF (0 - 500 g/s)
 * - Sticky START / STOP Signal Generation Footer
 */
@Composable
fun ControlScreen(
    uiState: EcuControlUiState,
    btState: BtConnectionState,
    onTapSelectVehicle: () -> Unit,
    onTapSavePreset: () -> Unit,
    onTapBtStatus: () -> Unit,
    onRpmChange: (Int) -> Unit,
    onRailChange: (Int) -> Unit,
    onEctChange: (Int) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onAccelChange: (Int) -> Unit,
    onBoostMapChange: (Int) -> Unit,
    onMafChange: (Int) -> Unit,
    onCamSyncToggle: (Boolean) -> Unit,
    onInjectorPulseToggle: (Boolean) -> Unit,
    onApplyQuickMode: (String) -> Unit,
    onCycleAccentTheme: () -> Unit,
    onToggleStartStop: () -> Unit,
    onSavePresetConfirm: (String) -> Unit,
    onDismissSaveDialog: () -> Unit
) {
    val accentColor = when (uiState.accentTheme) {
        AccentColorTheme.CyberBlue -> ElectricBlue
        AccentColorTheme.EmeraldGreen -> SignalGreen
        AccentColorTheme.RacingAmber -> WarningAmber
        AccentColorTheme.CrimsonRed -> Color(0xFFFF3B5C)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .testTag("control_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            // Top App Header with Branding, Vehicle Selector, Theme Switcher, and Save Preset Button
            ControlTopHeader(
                selectedBrand = uiState.selectedVehicle.brand,
                selectedModel = uiState.selectedVehicle.modelName,
                accentColor = accentColor,
                onTapSelectVehicle = onTapSelectVehicle,
                onCycleAccentTheme = onCycleAccentTheme,
                onTapSavePreset = onTapSavePreset
            )

            // Status Pills Row: RUNNING / STOPPED + HC-05 Bluetooth status
            StatusPillsRow(
                isRunning = uiState.isSignalRunning,
                btState = btState,
                accentColor = accentColor,
                onTapBtStatus = onTapBtStatus
            )

            // Scrollable Bench Cockpit & Parameter Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Digital Gauge Cluster
                item {
                    LiveBenchGaugeCluster(
                        rpm = uiState.rpm,
                        rail = uiState.railPressure,
                        ect = uiState.ectTemp,
                        speed = uiState.vehicleSpeed,
                        mapKpa = uiState.boostMapKpa,
                        mafGs = uiState.mafGramsSec,
                        isRunning = uiState.isSignalRunning,
                        accentColor = accentColor
                    )
                }

                // Quick Bench Test Mode Presets (Cranking, Idle, Cruise, Full Load)
                item {
                    QuickBenchModesBar(
                        accentColor = accentColor,
                        onSelectMode = onApplyQuickMode
                    )
                }

                // Camshaft Sync (CMP) & Injector Pulse Toggles
                item {
                    HardwarePulseTogglesCard(
                        camSyncEnabled = uiState.camSyncEnabled,
                        injectorPulseEnabled = uiState.injectorPulseEnabled,
                        accentColor = accentColor,
                        onCamSyncToggle = onCamSyncToggle,
                        onInjectorPulseToggle = onInjectorPulseToggle
                    )
                }

                // 1. Engine RPM (200 to 2500 rpm)
                item {
                    ParameterAdjustmentCard(
                        title = "Engine RPM",
                        value = uiState.rpm,
                        unit = "rpm",
                        minRange = EcuLabViewModel.RPM_MIN,
                        maxRange = EcuLabViewModel.RPM_MAX,
                        coarseStep = 100,
                        fineStep = 10,
                        icon = Icons.Default.Speed,
                        accentColor = accentColor,
                        testTagPrefix = "param_rpm",
                        onValueChange = onRpmChange
                    )
                }

                // 2. Rail Pressure (100 to 1000 bar)
                item {
                    ParameterAdjustmentCard(
                        title = "Rail Pressure",
                        value = uiState.railPressure,
                        unit = "bar",
                        minRange = EcuLabViewModel.RAIL_MIN,
                        maxRange = EcuLabViewModel.RAIL_MAX,
                        coarseStep = 50,
                        fineStep = 10,
                        icon = Icons.Default.Tune,
                        accentColor = accentColor,
                        testTagPrefix = "param_rail",
                        onValueChange = onRailChange
                    )
                }

                // 3. Coolant Temp (ECT) (0 to 200 °C)
                item {
                    ParameterAdjustmentCard(
                        title = "Coolant Temp (ECT)",
                        value = uiState.ectTemp,
                        unit = "°C",
                        minRange = EcuLabViewModel.ECT_MIN,
                        maxRange = EcuLabViewModel.ECT_MAX,
                        coarseStep = 10,
                        fineStep = 1,
                        icon = Icons.Default.Thermostat,
                        accentColor = accentColor,
                        testTagPrefix = "param_ect",
                        onValueChange = onEctChange
                    )
                }

                // 4. Vehicle Speed (0 to 220 km/h)
                item {
                    ParameterAdjustmentCard(
                        title = "Vehicle Speed",
                        value = uiState.vehicleSpeed,
                        unit = "km/h",
                        minRange = EcuLabViewModel.SPEED_MIN,
                        maxRange = EcuLabViewModel.SPEED_MAX,
                        coarseStep = 10,
                        fineStep = 1,
                        icon = Icons.Default.DirectionsCar,
                        accentColor = accentColor,
                        testTagPrefix = "param_speed",
                        onValueChange = onSpeedChange
                    )
                }

                // 5. Accelerator Pedal % (0 to 100 %)
                item {
                    ParameterAdjustmentCard(
                        title = "Accelerator",
                        value = uiState.accelerator,
                        unit = "%",
                        minRange = EcuLabViewModel.ACCEL_MIN,
                        maxRange = EcuLabViewModel.ACCEL_MAX,
                        coarseStep = 10,
                        fineStep = 1,
                        icon = Icons.Default.Speed,
                        accentColor = accentColor,
                        testTagPrefix = "param_accel",
                        onValueChange = onAccelChange
                    )
                }

                // 6. Boost Pressure / MAP Sensor (100 to 300 kPa)
                item {
                    ParameterAdjustmentCard(
                        title = "Boost Pressure (MAP)",
                        value = uiState.boostMapKpa,
                        unit = "kPa",
                        minRange = EcuLabViewModel.MAP_MIN,
                        maxRange = EcuLabViewModel.MAP_MAX,
                        coarseStep = 20,
                        fineStep = 5,
                        icon = Icons.Default.Compress,
                        accentColor = accentColor,
                        testTagPrefix = "param_map",
                        onValueChange = onBoostMapChange
                    )
                }

                // 7. Mass Air Flow (MAF) Sensor (0 to 500 g/s)
                item {
                    ParameterAdjustmentCard(
                        title = "Mass Air Flow (MAF)",
                        value = uiState.mafGramsSec,
                        unit = "g/s",
                        minRange = EcuLabViewModel.MAF_MIN,
                        maxRange = EcuLabViewModel.MAF_MAX,
                        coarseStep = 25,
                        fineStep = 5,
                        icon = Icons.Default.Air,
                        accentColor = accentColor,
                        testTagPrefix = "param_maf",
                        onValueChange = onMafChange
                    )
                }
            }

            // Sticky Bottom START / STOP Signal Generation Button
            StartStopActionFooter(
                isRunning = uiState.isSignalRunning,
                onToggleStartStop = onToggleStartStop
            )
        }
    }

    if (uiState.showSaveDialog) {
        SavePresetDialog(
            defaultName = "${uiState.selectedVehicle.brand} ${uiState.selectedVehicle.modelName} (${uiState.rpm} RPM)",
            accentColor = accentColor,
            onConfirm = onSavePresetConfirm,
            onDismiss = onDismissSaveDialog
        )
    }
}

@Composable
private fun ControlTopHeader(
    selectedBrand: String,
    selectedModel: String,
    accentColor: Color,
    onTapSelectVehicle: () -> Unit,
    onCycleAccentTheme: () -> Unit,
    onTapSavePreset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ak_ecu_logo_1791276792510),
                contentDescription = "AK ECU Signal Lab Logo",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ECU SIGNAL LAB",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Tap to select vehicle row (Notice: NO tooth pattern shown here!)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onTapSelectVehicle() }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .testTag("tap_select_vehicle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Selected Vehicle",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$selectedBrand • $selectedModel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Theme Color Accent Switcher Button
            IconButton(
                onClick = onCycleAccentTheme,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardElevatedSurface)
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("cycle_theme_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Change Cockpit Accent Theme",
                    tint = accentColor
                )
            }

            // Save Preset Button
            IconButton(
                onClick = onTapSavePreset,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardElevatedSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .testTag("save_preset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save ECU Preset",
                    tint = TextWhite
                )
            }
        }
    }
}

@Composable
private fun LiveBenchGaugeCluster(
    rpm: Int,
    rail: Int,
    ect: Int,
    speed: Int,
    mapKpa: Int,
    mafGs: Int,
    isRunning: Boolean,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE BENCH TELEMETRY CLUSTER",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRunning) "OUTPUT ACTIVE" else "STANDBY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isRunning) SignalGreen else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularMiniGauge(
                    label = "ENGINE RPM",
                    valueText = "$rpm",
                    unit = "rpm",
                    progress = (rpm - 200f) / (2500f - 200f),
                    color = accentColor
                )
                CircularMiniGauge(
                    label = "RAIL PRESS",
                    valueText = "$rail",
                    unit = "bar",
                    progress = (rail - 100f) / (1000f - 100f),
                    color = CyanGlow
                )
                CircularMiniGauge(
                    label = "BOOST MAP",
                    valueText = "$mapKpa",
                    unit = "kPa",
                    progress = (mapKpa - 100f) / (300f - 100f),
                    color = SignalGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniReadoutPill("ECT TEMP", "$ect °C")
                MiniReadoutPill("SPEED", "$speed km/h")
                MiniReadoutPill("MAF FLOW", "$mafGs g/s")
            }
        }
    }
}

@Composable
private fun CircularMiniGauge(
    label: String,
    valueText: String,
    unit: String,
    progress: Float,
    color: Color
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(86.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 7.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                drawArc(
                    color = ButtonDarkSurface,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = 270f * safeProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun MiniReadoutPill(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DeepObsidian)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun QuickBenchModesBar(
    accentColor: Color,
    onSelectMode: (String) -> Unit
) {
    val modes = listOf(
        "CRANKING" to "Crank (250)",
        "IDLE" to "Warm Idle",
        "CRUISE" to "Cruise (1650)",
        "FULL_LOAD" to "Full Boost"
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(modes) { (modeKey, label) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDarkSurface)
                    .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .clickable { onSelectMode(modeKey) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("quick_mode_${modeKey.lowercase()}")
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HardwarePulseTogglesCard(
    camSyncEnabled: Boolean,
    injectorPulseEnabled: Boolean,
    accentColor: Color,
    onCamSyncToggle: (Boolean) -> Unit,
    onInjectorPulseToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camshaft CMP Sync Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = if (camSyncEnabled) SignalGreen else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cam Sync (CMP)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (camSyncEnabled) "Signal Active" else "Signal Muted",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (camSyncEnabled) SignalGreen else TextMuted
                    )
                }
                Switch(
                    checked = camSyncEnabled,
                    onCheckedChange = onCamSyncToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextWhite,
                        checkedTrackColor = SignalGreen
                    ),
                    modifier = Modifier.testTag("cam_sync_switch")
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Injector Pulse Feedback Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (injectorPulseEnabled) accentColor else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Injector Load",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (injectorPulseEnabled) "Pulse Active" else "Disabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (injectorPulseEnabled) accentColor else TextMuted
                    )
                }
                Switch(
                    checked = injectorPulseEnabled,
                    onCheckedChange = onInjectorPulseToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextWhite,
                        checkedTrackColor = accentColor
                    ),
                    modifier = Modifier.testTag("injector_pulse_switch")
                )
            }
        }
    }
}

@Composable
private fun StatusPillsRow(
    isRunning: Boolean,
    btState: BtConnectionState,
    accentColor: Color,
    onTapBtStatus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val statusBg = if (isRunning) SignalGreenBg else Color(0xFF281317)
        val statusColor = if (isRunning) SignalGreen else StopRed
        val statusText = if (isRunning) "RUNNING" else "STOPPED"

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(statusBg)
                .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("signal_status_pill")
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        val isBtConnected = btState is BtConnectionState.Connected
        val btLabel = when (btState) {
            is BtConnectionState.Connected -> "BLUETOOTH CONNECTED (${btState.deviceName})"
            is BtConnectionState.Connecting -> "CONNECTING ${btState.deviceName}..."
            is BtConnectionState.Pairing -> "PAIRING ${btState.deviceName}..."
            is BtConnectionState.Scanning -> "SCANNING BLUETOOTH (${btState.foundCount})..."
            is BtConnectionState.Error -> "BLUETOOTH ERROR (TAP TO RETRY)"
            BtConnectionState.Disconnected -> "BLUETOOTH NOT CONNECTED"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(CardDarkSurface)
                .border(
                    width = 1.dp,
                    color = if (isBtConnected) accentColor else BorderSubtle,
                    shape = RoundedCornerShape(50)
                )
                .clickable { onTapBtStatus() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("bt_status_pill")
        ) {
            Icon(
                imageVector = if (isBtConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                contentDescription = "Bluetooth Status",
                tint = if (isBtConnected) accentColor else TextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = btLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (isBtConnected) CyanGlow else TextSecondary
            )
        }
    }
}

@Composable
private fun ParameterAdjustmentCard(
    title: String,
    value: Int,
    unit: String,
    minRange: Int,
    maxRange: Int,
    coarseStep: Int,
    fineStep: Int,
    icon: ImageVector,
    accentColor: Color,
    testTagPrefix: String,
    onValueChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_card"),
        shape = RoundedCornerShape(16.dp),
        color = CardDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "$minRange-$maxRange $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.testTag("${testTagPrefix}_range_label")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("${testTagPrefix}_value")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepperButton(
                        label = "«",
                        contentDescription = "Decrease $title by $coarseStep",
                        testTag = "${testTagPrefix}_dec_coarse",
                        onClick = { onValueChange(value - coarseStep) }
                    )
                    StepperButton(
                        label = "−",
                        contentDescription = "Decrease $title by $fineStep",
                        testTag = "${testTagPrefix}_dec_fine",
                        onClick = { onValueChange(value - fineStep) }
                    )
                    StepperButton(
                        label = "+",
                        contentDescription = "Increase $title by $fineStep",
                        testTag = "${testTagPrefix}_inc_fine",
                        onClick = { onValueChange(value + fineStep) }
                    )
                    StepperButton(
                        label = "»",
                        contentDescription = "Increase $title by $coarseStep",
                        testTag = "${testTagPrefix}_inc_coarse",
                        onClick = { onValueChange(value + coarseStep) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt()) },
                valueRange = minRange.toFloat()..maxRange.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = ButtonDarkSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_slider")
            )
        }
    }
}

@Composable
private fun StepperButton(
    label: String,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ButtonDarkSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClickLabel = contentDescription) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StartStopActionFooter(
    isRunning: Boolean,
    onToggleStartStop: () -> Unit
) {
    val buttonColor = if (isRunning) StopRed else SignalGreen
    val buttonLabel = if (isRunning) "STOP" else "START"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Button(
            onClick = onToggleStartStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_stop_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = TextWhite
            )
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = buttonLabel,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buttonLabel,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun SavePresetDialog(
    defaultName: String,
    accentColor: Color,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardElevatedSurface,
        titleContentColor = TextWhite,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "Save ECU Test Preset",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Save current RPM, Rail Pressure, ECT, Speed, MAP Boost, MAF, and Cam/Injector configuration.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preset_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(presetName) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.testTag("confirm_save_preset_button")
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}
