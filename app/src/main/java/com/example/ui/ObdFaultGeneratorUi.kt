package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ObdFaultCatalog
import com.example.data.ObdFaultCategory
import com.example.data.ObdFaultCode
import com.example.data.ObdFaultSeverity
import com.example.data.ObdSimulatedResponseSummary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.CardElevatedSurface
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenBg
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.brushedMetalPanel

@Composable
fun ObdFaultSetupSection(
    activeFaults: Set<ObdFaultCode>,
    eventLog: List<String>,
    onToggleFault: (ObdFaultCode) -> Unit,
    onGenerateRandomFault: () -> Unit,
    onClearAllFaults: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ObdFaultCategory.ALL) }
    val filteredFaults = remember(selectedCategory) {
        ObdFaultCatalog.byCategory(selectedCategory)
    }
    val responseSummary = remember(activeFaults) {
        ObdFaultCatalog.evaluateEcuResponse(activeFaults)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("obd_fault_setup_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header + MIL Indicator Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "OBD-II Fault Code Generator",
                    tint = if (responseSummary.milLampOn) WarningAmber else CyanGlow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OBD-II FAULT GENERATOR",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            MilStatusPill(
                milOn = responseSummary.milLampOn,
                activeCount = activeFaults.size
            )
        }

        // Simulated ECU Fault Response Card
        SimulatedEcuResponseCard(
            response = responseSummary,
            activeCount = activeFaults.size
        )

        // Quick Generator Controls: RANDOM FAULT + CLEAR DTC
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RecessedWell)
                    .border(1.2.dp, WarningAmber.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .clickable { onGenerateRandomFault() }
                    .testTag("obd_random_fault_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Generate Random Fault",
                        tint = WarningAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RANDOM DTC",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeFaults.isNotEmpty()) SignalGreenBg else RecessedWell)
                    .border(
                        1.2.dp,
                        if (activeFaults.isNotEmpty()) SignalGreen else BorderSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = activeFaults.isNotEmpty()) { onClearAllFaults() }
                    .testTag("obd_clear_all_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clear All Fault Codes",
                        tint = if (activeFaults.isNotEmpty()) SignalGreen else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLEAR DTC",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (activeFaults.isNotEmpty()) SignalGreen else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(ObdFaultCategory.entries) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CyanGlow.copy(alpha = 0.16f) else RecessedWell)
                        .border(
                            width = if (isSelected) 1.4.dp else 1.dp,
                            color = if (isSelected) CyanGlow else BorderSubtle,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp)
                        .testTag("obd_filter_${cat.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) CyanGlow else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Fault Code Cards
        filteredFaults.forEach { fault ->
            ObdFaultCodeItemCard(
                fault = fault,
                isActive = fault in activeFaults,
                onToggle = { onToggleFault(fault) }
            )
        }

        // Recent Fault Activity Log
        if (eventLog.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .brushedMetalPanel(cornerRadius = 12.dp, borderColor = BorderSubtle)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "RECENT FAULT LOG",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                eventLog.take(3).forEach { entry ->
                    Text(
                        text = "• $entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ObdFaultRunStrip(
    activeFaults: Set<ObdFaultCode>,
    onOpenFaultSheet: () -> Unit,
    onGenerateRandomFault: () -> Unit,
    onClearAllFaults: () -> Unit
) {
    val response = remember(activeFaults) {
        ObdFaultCatalog.evaluateEcuResponse(activeFaults)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(
                cornerRadius = 16.dp,
                borderColor = if (response.milLampOn) WarningAmber else BorderSubtle,
                borderWidth = if (response.milLampOn) 1.4.dp else 1.dp
            )
            .padding(12.dp)
            .testTag("obd_run_strip"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (response.milLampOn) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = "MIL Status",
                    tint = if (response.milLampOn) WarningAmber else SignalGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OBD-II FAULT INJECTOR",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            MilStatusPill(milOn = response.milLampOn, activeCount = activeFaults.size)
        }

        Text(
            text = if (response.milLampOn) {
                "${response.activeCodesLabel}: ${response.diagnosticAdvisory}"
            } else {
                "MIL OFF • Inject simulated sensor faults live during ECU stimulation."
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (response.milLampOn) GoldAccent else TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RecessedWell)
                    .border(1.dp, CyanGlow.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .clickable { onOpenFaultSheet() }
                    .testTag("obd_run_open_sheet_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SELECT DTC",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RecessedWell)
                    .border(1.dp, WarningAmber.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
                    .clickable { onGenerateRandomFault() }
                    .testTag("obd_run_random_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RANDOM",
                    style = MaterialTheme.typography.labelLarge,
                    color = WarningAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            if (activeFaults.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SignalGreenBg)
                        .border(1.dp, SignalGreen, RoundedCornerShape(10.dp))
                        .clickable { onClearAllFaults() }
                        .testTag("obd_run_clear_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLEAR",
                        style = MaterialTheme.typography.labelLarge,
                        color = SignalGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObdFaultInjectorBottomSheet(
    activeFaults: Set<ObdFaultCode>,
    onToggleFault: (ObdFaultCode) -> Unit,
    onGenerateRandomFault: () -> Unit,
    onClearAllFaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val responseSummary = remember(activeFaults) {
        ObdFaultCatalog.evaluateEcuResponse(activeFaults)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardDarkSurface,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OBD-II FAULT CODE GENERATOR",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold
                )
                MilStatusPill(milOn = responseSummary.milLampOn, activeCount = activeFaults.size)
            }

            SimulatedEcuResponseCard(response = responseSummary, activeCount = activeFaults.size)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RecessedWell)
                        .border(1.dp, WarningAmber, RoundedCornerShape(10.dp))
                        .clickable { onGenerateRandomFault() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RANDOM FAULT",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeFaults.isNotEmpty()) SignalGreenBg else RecessedWell)
                        .border(1.dp, if (activeFaults.isNotEmpty()) SignalGreen else BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable(enabled = activeFaults.isNotEmpty()) { onClearAllFaults() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLEAR ALL DTC",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (activeFaults.isNotEmpty()) SignalGreen else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ObdFaultCode.entries, key = { it.code }) { fault ->
                    ObdFaultCodeItemCard(
                        fault = fault,
                        isActive = fault in activeFaults,
                        onToggle = { onToggleFault(fault) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatedEcuResponseCard(
    response: ObdSimulatedResponseSummary,
    activeCount: Int
) {
    val borderCol = when {
        response.fuelCutRisk -> StopRed
        response.milLampOn -> WarningAmber
        else -> SignalGreen.copy(alpha = 0.6f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(cornerRadius = 14.dp, borderColor = borderCol, borderWidth = 1.3.dp)
            .padding(12.dp)
            .testTag("obd_ecu_response_card"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SIMULATED ECU RESPONSE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (activeCount > 0) response.activeCodesLabel else "NOMINAL",
                style = MaterialTheme.typography.labelMedium,
                color = if (activeCount > 0) WarningAmber else SignalGreen,
                fontWeight = FontWeight.Bold
            )
        }

        // Live Diagnostic State Flags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EcuFlagChip("MIL", response.milLampOn, WarningAmber, Modifier.weight(1f))
            EcuFlagChip("LIMP", response.limpModeActive, StopRed, Modifier.weight(1f))
            EcuFlagChip("FAN HI", response.coolingFanForced, CyanGlow, Modifier.weight(1f))
            EcuFlagChip("CUT/EGR", response.fuelCutRisk || response.egrInhibited, GoldAccent, Modifier.weight(1f))
        }

        Text(
            text = response.diagnosticAdvisory,
            style = MaterialTheme.typography.bodySmall,
            color = if (response.milLampOn) TextWhite else TextSecondary
        )
    }
}

@Composable
private fun EcuFlagChip(
    label: String,
    active: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) activeColor.copy(alpha = 0.16f) else RecessedWell)
            .border(
                1.dp,
                if (active) activeColor else BorderSubtle,
                RoundedCornerShape(8.dp)
            )
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (active) "$label: ON" else "$label: OFF",
            style = MaterialTheme.typography.labelSmall,
            color = if (active) activeColor else TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun MilStatusPill(
    milOn: Boolean,
    activeCount: Int
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (milOn) StopRed.copy(alpha = 0.16f) else SignalGreenBg)
            .border(
                1.dp,
                if (milOn) WarningAmber else SignalGreen,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp)
            .testTag("obd_mil_status_pill")
    ) {
        Text(
            text = if (milOn) "MIL ON • $activeCount DTC" else "MIL OFF • 0 DTC",
            style = MaterialTheme.typography.labelSmall,
            color = if (milOn) WarningAmber else SignalGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ObdFaultCodeItemCard(
    fault: ObdFaultCode,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    val severityColor = if (fault.severity == ObdFaultSeverity.CRITICAL) StopRed else WarningAmber

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(
                cornerRadius = 12.dp,
                borderColor = if (isActive) severityColor else BorderSubtle,
                borderWidth = if (isActive) 1.5.dp else 1.dp,
                elevated = isActive
            )
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("obd_dtc_${fault.code}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Code Pill
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) severityColor.copy(alpha = 0.22f) else RecessedWell)
                .border(1.dp, severityColor.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fault.code,
                style = MaterialTheme.typography.labelLarge,
                color = severityColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = fault.category.label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 8.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fault.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = fault.faultConditionSummary,
                style = MaterialTheme.typography.labelSmall,
                color = GoldAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = fault.expectedEcuResponse,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Inject / Active 48dp Action Pill
        Box(
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isActive) severityColor.copy(alpha = 0.2f) else CardElevatedSurface)
                .border(
                    1.2.dp,
                    if (isActive) severityColor else CyanGlow.copy(alpha = 0.65f),
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = if (isActive) "Clear ${fault.code}" else "Inject ${fault.code}",
                    tint = if (isActive) severityColor else CyanGlow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (isActive) "ACTIVE" else "INJECT",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) severityColor else CyanGlow,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
