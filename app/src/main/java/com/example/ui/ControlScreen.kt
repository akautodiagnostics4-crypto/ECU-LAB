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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.graphics.Color
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
import com.example.viewmodel.EcuControlUiState
import com.example.viewmodel.EcuLabViewModel
import kotlin.math.roundToInt

/**
 * Matches the user's uploaded ECU SIGNAL LAB Control screen screenshot while adhering strictly to:
 * - DONT SHOW THE CRANK TOOTH PATTERN ON THE DISPLAY
 * - rpm range 200 to 2500
 * - rail range 100 to 1000
 * - ect 0 to 200
 * - vehicle speed 0 to 220
 * - start and stop button for signal generation
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
    onToggleStartStop: () -> Unit,
    onSavePresetConfirm: (String) -> Unit,
    onDismissSaveDialog: () -> Unit
) {
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
            // Top App Header matching screenshot
            ControlTopHeader(
                selectedBrand = uiState.selectedVehicle.brand,
                selectedModel = uiState.selectedVehicle.modelName,
                onTapSelectVehicle = onTapSelectVehicle,
                onTapSavePreset = onTapSavePreset
            )

            // Status Pills Row: RUNNING / STOPPED + HC-05 Bluetooth status
            StatusPillsRow(
                isRunning = uiState.isSignalRunning,
                btState = btState,
                onTapBtStatus = onTapBtStatus
            )

            // Scrollable Parameter Adjustment Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        testTagPrefix = "param_accel",
                        onValueChange = onAccelChange
                    )
                }
            }

            // Sticky Bottom START / STOP Signal Generation Button matching screenshot
            StartStopActionFooter(
                isRunning = uiState.isSignalRunning,
                onToggleStartStop = onToggleStartStop
            )
        }
    }

    if (uiState.showSaveDialog) {
        SavePresetDialog(
            defaultName = "${uiState.selectedVehicle.brand} ${uiState.selectedVehicle.modelName} (${uiState.rpm} RPM)",
            onConfirm = onSavePresetConfirm,
            onDismiss = onDismissSaveDialog
        )
    }
}

@Composable
private fun ControlTopHeader(
    selectedBrand: String,
    selectedModel: String,
    onTapSelectVehicle: () -> Unit,
    onTapSavePreset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Circular AK ECU Signal Lab Logo Badge
            Image(
                painter = painterResource(id = R.drawable.img_ak_ecu_logo_1791276792510),
                contentDescription = "AK ECU Signal Lab Logo",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "ECU SIGNAL LAB",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp
                )

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
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$selectedBrand • $selectedModel (Tap to change)",
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

        // Save Preset Button (Floppy icon on top-right matching screenshot)
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

@Composable
private fun StatusPillsRow(
    isRunning: Boolean,
    btState: BtConnectionState,
    onTapBtStatus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // RUNNING / STOPPED pill
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

        // HC-05 Connection Status pill
        val isBtConnected = btState is BtConnectionState.Connected
        val btLabel = when (btState) {
            is BtConnectionState.Connected -> "HC-05 CONNECTED (${btState.deviceName})"
            is BtConnectionState.Connecting -> "CONNECTING ${btState.deviceName}..."
            is BtConnectionState.Pairing -> "PAIRING ${btState.deviceName}..."
            is BtConnectionState.Scanning -> "SCANNING HC-05 (${btState.foundCount})..."
            is BtConnectionState.Error -> "HC-05 ERROR (TAP TO RETRY)"
            BtConnectionState.Disconnected -> "HC-05 NOT CONNECTED"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(CardDarkSurface)
                .border(
                    width = 1.dp,
                    color = if (isBtConnected) ElectricBlue else BorderSubtle,
                    shape = RoundedCornerShape(50)
                )
                .clickable { onTapBtStatus() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("bt_status_pill")
        ) {
            Icon(
                imageVector = if (isBtConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                contentDescription = "HC-05 Status",
                tint = if (isBtConnected) ElectricBlue else TextSecondary,
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
            // Header row: Icon + Parameter Title on left, Range on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = ElectricBlue,
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

            // Value Display + 4 Stepper Buttons (<<, -, +, >>) matching screenshot
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
                        color = ElectricBlue,
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

            // Smooth Slider
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt()) },
                valueRange = minRange.toFloat()..maxRange.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = ElectricBlue,
                    activeTrackColor = ElectricBlue,
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
                    text = "Save current RPM, Rail Pressure, ECT, and Vehicle Speed configuration for quick recall.",
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
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
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
