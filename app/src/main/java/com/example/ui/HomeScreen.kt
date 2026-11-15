package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.bluetooth.BtConnectionState
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.brushedMetalPanel
import com.example.ui.theme.carbonFiberBackground

/**
 * Clean Home Page:
 * - Top Header: High-fi AK ECU STIMULATOR logo + Small Bluetooth Connected Icon + Settings icon
 * - Option 1: "ECU STIMULATION" card with ECU hardware image
 * - Option 2: "SENSOR STIMULATION" card with automotive Sensor array image
 */
@Composable
fun HomeScreen(
    btState: BtConnectionState,
    onTryAutoConnectBt: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectEcuStimulation: () -> Unit,
    onSelectSensorStimulation: () -> Unit
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

    val btPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onTryAutoConnectBt()
    }

    LaunchedEffect(Unit) {
        onTryAutoConnectBt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .testTag("home_dashboard_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar: High-Fi Logo + AK ECU STIMULATOR + Small Bluetooth Icon + Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AkSignalLabTitleRow(
                    logoSize = 38.dp,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallBluetoothStatusIcon(
                        btState = btState,
                        onClick = {
                            btPermLauncher.launch(permissionsToRequest)
                            onOpenBluetoothSheet()
                        }
                    )

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RecessedWell)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextWhite,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            // Option 1: ECU STIMULATION (with ECU Image)
            HomeStimulationOptionCard(
                title = "ECU STIMULATION",
                subtitle = "Vehicle Category • Cranking Modes • Engine RPM & Sensors",
                badgeText = "CRANK + CAM + SENSORS",
                imageResId = R.drawable.img_ecu_hardware_box_1794742207159,
                accentColor = CyanGlow,
                testTag = "home_ecu_stimulation_card",
                onClick = onSelectEcuStimulation
            )

            // Option 2: SENSOR STIMULATION (with Sensor Image)
            HomeStimulationOptionCard(
                title = "SENSOR STIMULATION",
                subtitle = "Rail Pressure • Coolant Temp • Speed • Boost MAP • Air Mass",
                badgeText = "DUAL SENSOR OUTPUT (NO CRANK/CAM)",
                imageResId = R.drawable.img_sensor_stimulation_card_1794745516868,
                accentColor = SignalGreen,
                testTag = "home_sensor_stimulation_card",
                onClick = onSelectSensorStimulation
            )
        }
    }
}

@Composable
private fun HomeStimulationOptionCard(
    title: String,
    subtitle: String,
    badgeText: String,
    imageResId: Int,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brushedMetalPanel(
                cornerRadius = 16.dp,
                borderColor = accentColor.copy(alpha = 0.7f),
                borderWidth = 1.3.dp,
                elevated = true
            )
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF061024).copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RecessedWell.copy(alpha = 0.88f))
                        .border(1.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open $title",
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
