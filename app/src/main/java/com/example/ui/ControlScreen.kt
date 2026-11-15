package com.example.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BtConnectionState
import com.example.data.ObdFaultCode
import com.example.data.StimulableSensor
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrushedMetalBottom
import com.example.ui.theme.ButtonDarkSurface
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.CardElevatedSurface
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlossBlueBottom
import com.example.ui.theme.GlossBlueTop
import com.example.ui.theme.GlossGreenBottom
import com.example.ui.theme.GlossGreenTop
import com.example.ui.theme.GlossRedBottom
import com.example.ui.theme.GlossRedTop
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MetalHighlightEdge
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenBg
import com.example.ui.theme.StopRed
import com.example.ui.theme.TealReadout
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.brushedMetalPanel
import com.example.ui.theme.carbonFiberBackground
import com.example.viewmodel.CrankingModePreset
import com.example.viewmodel.EcuControlUiState
import com.example.viewmodel.EcuLabViewModel
import kotlin.math.roundToInt

/**
 * Step 4 inside ECU STIMULATION:
 * - Selected LCV Vehicle Banner
 * - Cranking Modes:
 *   MODE 1: 300 RPM & Rail 250 bar
 *   MODE 2: 700 RPM & Rail 350 bar
 *   MODE 3: 1200 RPM & Rail 600 bar
 *   CUSTOM: Custom RPM & Rail values
 * - Custom RPM (200 - 2500) & Rail Pressure (0 - 1200 bar) sliders + Cam Sync switch
 * - Pick Sensors section with custom Sensor Logo + Tick toggles + live sensor sliders
 * - CTA: CONTINUE TO START (opens EcuRunOnlyScreen)
 */
@Composable
fun EcuCrankingSetupScreen(
    uiState: EcuControlUiState,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onSelectVehicleSwitch: () -> Unit,
    onSelectCrankingMode: (CrankingModePreset) -> Unit,
    onRpmChange: (Int) -> Unit,
    onRailChange: (Int) -> Unit,
    onCamSyncToggle: (Boolean) -> Unit,
    onToggleSensorPick: (StimulableSensor) -> Unit,
    onSensorValueChange: (StimulableSensor, Int) -> Unit,
    onToggleObdFault: (ObdFaultCode) -> Unit,
    onGenerateRandomObdFault: () -> Unit,
    onClearAllObdFaults: () -> Unit,
    onProceedToStartPage: () -> Unit,
    onResetAllToZero: () -> Unit,
    onTapSavePreset: () -> Unit,
    onSavePresetConfirm: (String) -> Unit,
    onDismissSaveDialog: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("ecu_cranking_setup_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
        ) {
            SubScreenTopBar(
                title = "CRANKING & SENSORS",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "cranking_setup_back_button",
                trailingContent = {
                    IconButton(
                        onClick = onTapSavePreset,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RecessedWell)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag("save_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save ECU Preset",
                            tint = CyanGlow,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selected LCV Vehicle Banner (No Crank Tooth Pattern shown)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brushedMetalPanel(cornerRadius = 14.dp, borderColor = BorderSubtle, elevated = true)
                            .clickable { onSelectVehicleSwitch() }
                            .padding(10.dp)
                            .testTag("setup_selected_vehicle_card"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = vehiclePreviewDrawable(uiState.selectedVehicle)),
                            contentDescription = uiState.selectedVehicle.modelName,
                            modifier = Modifier
                                .width(116.dp)
                                .height(68.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CyanGlow.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VehicleBrandBadge(brand = uiState.selectedVehicle.brand, sizeDp = 18.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.selectedVehicle.brand,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = uiState.selectedVehicle.modelName,
                                style = MaterialTheme.typography.titleLarge,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = uiState.selectedVehicle.ecuSystem,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // CRANKING MODES Header
                item {
                    Text(
                        text = "CRANKING MODES",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanGlow,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3 Preset Cranking Modes + Custom Mode (2x2 Grid)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CrankingModeCard(
                                mode = CrankingModePreset.MODE_1,
                                subtitle = "300 RPM • Rail 250 bar",
                                isSelected = uiState.selectedCrankingMode == CrankingModePreset.MODE_1,
                                onClick = { onSelectCrankingMode(CrankingModePreset.MODE_1) },
                                modifier = Modifier.weight(1f)
                            )
                            CrankingModeCard(
                                mode = CrankingModePreset.MODE_2,
                                subtitle = "700 RPM • Rail 350 bar",
                                isSelected = uiState.selectedCrankingMode == CrankingModePreset.MODE_2,
                                onClick = { onSelectCrankingMode(CrankingModePreset.MODE_2) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CrankingModeCard(
                                mode = CrankingModePreset.MODE_3,
                                subtitle = "1200 RPM • Rail 600 bar",
                                isSelected = uiState.selectedCrankingMode == CrankingModePreset.MODE_3,
                                onClick = { onSelectCrankingMode(CrankingModePreset.MODE_3) },
                                modifier = Modifier.weight(1f)
                            )
                            CrankingModeCard(
                                mode = CrankingModePreset.CUSTOM,
                                subtitle = "Custom Values Below",
                                isSelected = uiState.selectedCrankingMode == CrankingModePreset.CUSTOM,
                                onClick = { onSelectCrankingMode(CrankingModePreset.CUSTOM) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Custom Engine RPM & Rail Pressure Sliders
                item {
                    ParameterSliderCard(
                        title = "Engine RPM",
                        rangeLabel = "(${EcuLabViewModel.RPM_MIN} - ${EcuLabViewModel.RPM_MAX} RPM)",
                        value = uiState.rpm,
                        unit = "RPM",
                        minRange = EcuLabViewModel.RPM_MIN,
                        maxRange = EcuLabViewModel.RPM_MAX,
                        step = 25,
                        icon = Icons.Default.Speed,
                        iconColor = Color(0xFFFF6E40),
                        trackColor = CyanGlow,
                        testTagPrefix = "param_rpm",
                        onValueChange = onRpmChange
                    )
                }

                item {
                    ParameterSliderCard(
                        title = "Rail Pressure",
                        rangeLabel = "(${EcuLabViewModel.RAIL_MIN} - ${EcuLabViewModel.RAIL_MAX} bar)",
                        value = uiState.railPressure,
                        unit = "bar",
                        minRange = EcuLabViewModel.RAIL_MIN,
                        maxRange = EcuLabViewModel.RAIL_MAX,
                        step = 10,
                        icon = Icons.Default.Tune,
                        iconColor = GoldAccent,
                        trackColor = Color(0xFFFF8C24),
                        testTagPrefix = "param_rail",
                        onValueChange = onRailChange
                    )
                }

                // Cam Sync Switch
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brushedMetalPanel(cornerRadius = 14.dp, borderColor = BorderSubtle)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Cam Sync",
                                tint = if (uiState.camSyncEnabled) SignalGreen else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Camshaft Sync Signal",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (uiState.camSyncEnabled) "Enabled with Crank" else "Crank Only",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.camSyncEnabled) SignalGreen else TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = uiState.camSyncEnabled,
                            onCheckedChange = onCamSyncToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = SignalGreen
                            ),
                            modifier = Modifier.testTag("cam_sync_switch")
                        )
                    }
                }

                // PICK SENSORS Header
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PICK SENSORS",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyanGlow,
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.ecuPickedSensors.size} Selected",
                            style = MaterialTheme.typography.labelSmall,
                            color = SignalGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Pickable Sensors with SensorLogoBadge + Tick + Live Slider when picked
                items(StimulableSensor.entries) { sensor ->
                    val isPicked = sensor in uiState.ecuPickedSensors
                    val currentVal = sensorValueFromState(uiState, sensor)
                    SensorPickAndTuneCard(
                        sensor = sensor,
                        currentValue = currentVal,
                        isTicked = isPicked,
                        testTagPrefix = "ecu_sensor_${sensor.id}",
                        onToggleTick = { onToggleSensorPick(sensor) },
                        onValueChange = { onSensorValueChange(sensor, it) }
                    )
                }

                // Simulated OBD-II Diagnostic Fault Code Generator
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ObdFaultSetupSection(
                        activeFaults = uiState.activeObdFaults,
                        eventLog = uiState.obdFaultEventLog,
                        onToggleFault = onToggleObdFault,
                        onGenerateRandomFault = onGenerateRandomObdFault,
                        onClearAllFaults = onClearAllObdFaults
                    )
                }
            }

            // Bottom Footer: RESET (0) + CONTINUE TO START
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(CardDarkSurface, DeepObsidian)))
                    .border(1.dp, BorderSubtle)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResetZeroButton(
                        onClick = onResetAllToZero,
                        modifier = Modifier.weight(0.35f)
                    )

                    GlossyActionButton(
                        text = "NEXT: START",
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        topColor = GlossBlueTop,
                        bottomColor = GlossBlueBottom,
                        borderColor = CyanGlow.copy(alpha = 0.8f),
                        testTag = "proceed_to_ecu_start_button",
                        modifier = Modifier.weight(0.65f),
                        height = 52.dp,
                        onClick = onProceedToStartPage
                    )
                }
            }
        }
    }

    if (uiState.showSaveDialog) {
        SavePresetDialog(
            defaultName = "${uiState.selectedVehicle.brand} ${uiState.selectedVehicle.modelName} (${uiState.rpm} RPM)",
            accentColor = ElectricBlue,
            onConfirm = onSavePresetConfirm,
            onDismiss = onDismissSaveDialog
        )
    }
}

/**
 * Step 5 inside ECU STIMULATION:
 * Clean dedicated execution page showing active configuration summary and START / STOP
 * (NEVER displays "via Bluetooth").
 * If Bluetooth is disconnected when Start or Stop is pressed, triggers "Connect Bluetooth first".
 */
@Composable
fun EcuRunOnlyScreen(
    uiState: EcuControlUiState,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onOpenObdFaultSheet: () -> Unit,
    onGenerateRandomObdFault: () -> Unit,
    onClearAllObdFaults: () -> Unit,
    onToggleStartStop: () -> Unit,
    onResetAllToZero: () -> Unit
) {
    val isRunning = uiState.isSignalRunning

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("ecu_run_only_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SubScreenTopBar(
                title = "ECU STIMULATION",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "ecu_run_back_button"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vehicle + Active Parameters Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .brushedMetalPanel(
                            cornerRadius = 18.dp,
                            borderColor = if (isRunning) SignalGreen else BorderSubtle,
                            borderWidth = 1.4.dp,
                            elevated = true
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = vehiclePreviewDrawable(uiState.selectedVehicle)),
                                contentDescription = uiState.selectedVehicle.modelName,
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(66.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    VehicleBrandBadge(brand = uiState.selectedVehicle.brand, sizeDp = 18.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = uiState.selectedVehicle.brand,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CyanGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = uiState.selectedVehicle.modelName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = uiState.selectedCrankingMode.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Compact Readout Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RunMetricBox("RPM", "${uiState.rpm}", CyanGlow, Modifier.weight(1f))
                            RunMetricBox("RAIL", "${uiState.railPressure} bar", GoldAccent, Modifier.weight(1f))
                            RunMetricBox(
                                "SENSORS",
                                "${uiState.ecuPickedSensors.size} Active",
                                SignalGreen,
                                Modifier.weight(1f)
                            )
                        }

                        // Running / Ready Pill (NO "via Bluetooth")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRunning) SignalGreenBg else RecessedWell)
                                .border(
                                    1.dp,
                                    if (isRunning) SignalGreen else BorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(vertical = 8.dp)
                                .testTag("ecu_run_status_pill"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isRunning) "RUNNING..." else "READY TO START",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isRunning) SignalGreen else TitaniumSilver,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ObdFaultRunStrip(
                    activeFaults = uiState.activeObdFaults,
                    onOpenFaultSheet = onOpenObdFaultSheet,
                    onGenerateRandomFault = onGenerateRandomObdFault,
                    onClearAllFaults = onClearAllObdFaults
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Page Control: ONLY Start / Stop Button
                GlossyActionButton(
                    text = if (isRunning) "STOP" else "START",
                    icon = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    topColor = if (isRunning) GlossRedTop else GlossGreenTop,
                    bottomColor = if (isRunning) GlossRedBottom else GlossGreenBottom,
                    borderColor = if (isRunning) StopRed else SignalGreen,
                    testTag = "ecu_only_start_stop_button",
                    modifier = Modifier.fillMaxWidth(),
                    height = 66.dp,
                    onClick = onToggleStartStop
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResetZeroButton(
                    onClick = onResetAllToZero,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * SENSOR STIMULATION Category Screen:
 * - Strictly Sensor Simulation ONLY (NO Crank and NO Cam signals)
 * - Uses existing sensor parameters:
 *   Rail Pressure (0 - 1200 bar)
 *   Coolant Temp (0 - 125 °C)
 *   Vehicle Speed (0 - 220 km/h)
 *   Boost Pressure Sensor (100 - 300 kPa)
 *   Air Mass Sensor (0 - 500 g/s)
 * - Custom graphic logo (`SensorLogoBadge`) for EVERY sensor
 * - User can select up to 2 particular sensors using a Tick button, then adjust & stimulate them
 */
@Composable
fun SensorStimulationScreen(
    uiState: EcuControlUiState,
    btState: BtConnectionState,
    onBack: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onToggleTickSensor: (StimulableSensor) -> Unit,
    onSensorValueChange: (StimulableSensor, Int) -> Unit,
    onToggleStartStop: () -> Unit,
    onResetAllToZero: () -> Unit
) {
    val tickedSensors = uiState.sensorStimulationTicked
    val isRunning = uiState.isSensorStimulationRunning

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("sensor_stimulation_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
        ) {
            SubScreenTopBar(
                title = "SENSOR STIMULATION",
                btState = btState,
                onBack = onBack,
                onOpenBluetoothSheet = onOpenBluetoothSheet,
                backTestTag = "sensor_stim_back_button"
            )

            // Info Banner: No Crank / No Cam + Max 2 Ticked Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .brushedMetalPanel(
                        cornerRadius = 12.dp,
                        borderColor = SignalGreen.copy(alpha = 0.65f)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SENSOR ONLY MODE",
                        style = MaterialTheme.typography.labelLarge,
                        color = SignalGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No Crank & No Cam signals • Tick sensors to stimulate",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RecessedWell)
                        .border(1.dp, CyanGlow.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("sensor_tick_counter")
                ) {
                    Text(
                        text = "Ticked: ${tickedSensors.size} Active",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanGlow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(StimulableSensor.entries, key = { it.id }) { sensor ->
                    val isTicked = sensor in tickedSensors
                    val currentVal = sensorValueFromState(uiState, sensor)
                    SensorPickAndTuneCard(
                        sensor = sensor,
                        currentValue = currentVal,
                        isTicked = isTicked,
                        testTagPrefix = "sensor_only_${sensor.id}",
                        onToggleTick = { onToggleTickSensor(sensor) },
                        onValueChange = { onSensorValueChange(sensor, it) }
                    )
                }
            }

            // Bottom Footer: RESET (0) + Start / Stop (NO "via Bluetooth")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(CardDarkSurface, DeepObsidian)))
                    .border(1.dp, BorderSubtle)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResetZeroButton(
                        onClick = onResetAllToZero,
                        modifier = Modifier.weight(0.36f)
                    )

                    GlossyActionButton(
                        text = if (isRunning) "STOP" else "START",
                        icon = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        topColor = if (isRunning) GlossRedTop else GlossGreenTop,
                        bottomColor = if (isRunning) GlossRedBottom else GlossGreenBottom,
                        borderColor = if (isRunning) StopRed else SignalGreen,
                        testTag = "sensor_start_stop_button",
                        modifier = Modifier.weight(0.64f),
                        height = 52.dp,
                        onClick = onToggleStartStop
                    )
                }
            }
        }
    }
}

@Composable
private fun CrankingModeCard(
    mode: CrankingModePreset,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .brushedMetalPanel(
                cornerRadius = 12.dp,
                borderColor = if (isSelected) CyanGlow else BorderSubtle,
                borderWidth = if (isSelected) 1.6.dp else 1.dp,
                elevated = isSelected
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("cranking_mode_${mode.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) CyanGlow else TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) GoldAccent else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "${mode.title} Selected",
                    tint = SignalGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SensorPickAndTuneCard(
    sensor: StimulableSensor,
    currentValue: Int,
    isTicked: Boolean,
    testTagPrefix: String,
    onToggleTick: () -> Unit,
    onValueChange: (Int) -> Unit
) {
    val isOverheat = sensor == StimulableSensor.COOLANT_TEMP && currentValue >= 105
    val accentColor = when {
        isOverheat -> StopRed
        sensor == StimulableSensor.RAIL_PRESSURE -> GoldAccent
        sensor == StimulableSensor.COOLANT_TEMP -> SignalGreen
        sensor == StimulableSensor.VEHICLE_SPEED -> ElectricBlue
        sensor == StimulableSensor.BOOST_PRESSURE -> CyanGlow
        else -> SignalGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(
                cornerRadius = 14.dp,
                borderColor = if (isTicked) accentColor.copy(alpha = 0.85f) else BorderSubtle,
                borderWidth = if (isTicked) 1.5.dp else 1.dp,
                elevated = isTicked
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("${testTagPrefix}_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleTick() }
            ) {
                SensorLogoBadge(
                    sensor = sensor,
                    accentColor = accentColor,
                    sizeDp = 44.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOverheat) "${sensor.title} (OVERHEAT)" else sensor.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isOverheat) StopRed else TextWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isOverheat) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Overheat Alert",
                                tint = StopRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${sensor.shortLabel} • Range ${sensor.minValue} - ${sensor.maxValue} ${sensor.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Digital Value Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF062738))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$currentValue ${sensor.unit}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TealReadout,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("${testTagPrefix}_value")
                    )
                }

                // 48dp Tick Selection Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTicked) SignalGreenBg else RecessedWell)
                        .border(
                            width = 1.5.dp,
                            color = if (isTicked) SignalGreen else BorderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggleTick() }
                        .testTag("${testTagPrefix}_tick_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = if (isTicked) "Untick ${sensor.title}" else "Tick ${sensor.title}",
                        tint = if (isTicked) SignalGreen else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Expanded Interactive Slider & Steppers when ticked
        if (isTicked) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StepperButton(
                    label = "−",
                    contentDescription = "Decrease ${sensor.title}",
                    testTag = "${testTagPrefix}_dec",
                    onClick = { onValueChange(currentValue - sensor.step) }
                )

                Slider(
                    value = currentValue.toFloat().coerceIn(sensor.minValue.toFloat(), sensor.maxValue.toFloat()),
                    onValueChange = { onValueChange(it.roundToInt()) },
                    valueRange = sensor.minValue.toFloat()..sensor.maxValue.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = TextWhite,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = RecessedWell
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_slider")
                )

                StepperButton(
                    label = "+",
                    contentDescription = "Increase ${sensor.title}",
                    testTag = "${testTagPrefix}_inc",
                    onClick = { onValueChange(currentValue + sensor.step) }
                )
            }
        }
    }
}

@Composable
private fun ParameterSliderCard(
    title: String,
    rangeLabel: String,
    value: Int,
    unit: String,
    minRange: Int,
    maxRange: Int,
    step: Int,
    icon: ImageVector,
    iconColor: Color,
    trackColor: Color,
    testTagPrefix: String,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(cornerRadius = 14.dp, borderColor = BorderSubtle)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("${testTagPrefix}_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(RecessedWell)
                        .border(1.2.dp, iconColor.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rangeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF062738))
                    .border(1.dp, CyanGlow.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$value $unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = TealReadout,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("${testTagPrefix}_value")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepperButton(
                label = "−",
                contentDescription = "Decrease $title by $step",
                testTag = "${testTagPrefix}_dec",
                onClick = { onValueChange(value - step) }
            )

            Slider(
                value = value.toFloat().coerceIn(minRange.toFloat(), maxRange.toFloat()),
                onValueChange = { onValueChange(it.roundToInt()) },
                valueRange = minRange.toFloat()..maxRange.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = TextWhite,
                    activeTrackColor = trackColor,
                    inactiveTrackColor = RecessedWell
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("${testTagPrefix}_slider")
            )

            StepperButton(
                label = "+",
                contentDescription = "Increase $title by $step",
                testTag = "${testTagPrefix}_inc",
                onClick = { onValueChange(value + step) }
            )
        }
    }
}

@Composable
private fun RunMetricBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RecessedWell)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun ResetZeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(RecessedWell)
            .border(1.2.dp, StopRed.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("footer_reset_all_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = "Reset All Values to 0",
                tint = StopRed,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RESET (0)",
                style = MaterialTheme.typography.labelLarge,
                color = StopRed,
                fontWeight = FontWeight.Bold
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
            .background(Brush.verticalGradient(listOf(ButtonDarkSurface, BrushedMetalBottom)))
            .border(1.dp, MetalHighlightEdge.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .clickable(onClickLabel = contentDescription) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = TitaniumSilver,
            fontWeight = FontWeight.Bold
        )
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
                    text = "Save current RPM, Rail Pressure, ECT, Speed, Boost MAP, and MAF configuration.",
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

private fun sensorValueFromState(uiState: EcuControlUiState, sensor: StimulableSensor): Int {
    return when (sensor) {
        StimulableSensor.RAIL_PRESSURE -> uiState.railPressure
        StimulableSensor.COOLANT_TEMP -> uiState.ectTemp
        StimulableSensor.VEHICLE_SPEED -> uiState.vehicleSpeed
        StimulableSensor.BOOST_PRESSURE -> uiState.boostMapKpa
        StimulableSensor.AIR_MASS -> uiState.mafGramsSec
    }
}
