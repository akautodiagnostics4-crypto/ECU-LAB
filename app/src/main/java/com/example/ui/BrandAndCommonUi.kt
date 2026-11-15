package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.bluetooth.BtConnectionState
import com.example.data.StimulableSensor
import com.example.data.VehicleModel
import com.example.data.VehicleVisualType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.WarningAmber

/**
 * Selects the authentic Indian studio vehicle render drawable for each LCV profile.
 */
fun vehiclePreviewDrawable(vehicle: VehicleModel): Int {
    return when (vehicle.visualType) {
        VehicleVisualType.MINI_TRUCK -> R.drawable.img_lcv_minitruck_ace_1794745518081
        VehicleVisualType.PICKUP -> R.drawable.img_lcv_pickup_bolero_1794745518078
        VehicleVisualType.VAN -> R.drawable.img_lcv_force_traveller_1794749348476
        VehicleVisualType.SUV -> R.drawable.img_lcv_utility_camper_1794749350453
        VehicleVisualType.COMMERCIAL_TRUCK -> R.drawable.img_lcv_indian_truck_1794749348585
    }
}

/**
 * High-Fi AK ECU STIMULATOR hexagonal badge image.
 */
@Composable
fun HighFiAppLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .border(1.2.dp, CyanGlow.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_ak_hi_fi_logo_1794745516657),
            contentDescription = "AK ECU STIMULATOR Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Compact header lockup with high-fi logo + AK ECU STIMULATOR title.
 */
@Composable
fun AkSignalLabTitleRow(
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp,
    fontSize: TextUnit = 17.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HighFiAppLogoBadge(size = logoSize)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "AK ECU STIMULATOR",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = fontSize,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = TextWhite,
            maxLines = 1
        )
    }
}

/**
 * Small Bluetooth status icon shown in top bars ("SHOW BLUTOOTH CONNECTED AS SMALL ICON").
 */
@Composable
fun SmallBluetoothStatusIcon(
    btState: BtConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = btState is BtConnectionState.Connected
    val isBusy = btState is BtConnectionState.Connecting ||
        btState is BtConnectionState.Pairing ||
        btState is BtConnectionState.Scanning

    val iconTint = when {
        isConnected -> SignalGreen
        isBusy -> CyanGlow
        else -> StopRed
    }
    val borderCol = when {
        isConnected -> SignalGreen.copy(alpha = 0.75f)
        isBusy -> CyanGlow.copy(alpha = 0.75f)
        else -> BorderSubtle
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(RecessedWell)
            .border(1.2.dp, borderCol, CircleShape)
            .clickable { onClick() }
            .testTag("small_bt_status_icon"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
            contentDescription = if (isConnected) "Bluetooth Connected" else "Bluetooth Disconnected",
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(iconTint)
        )
    }
}

/**
 * Filled official-style automotive brand emblems for ALL LCV, CAR, HCV, and BUS brands.
 */
@Composable
fun VehicleBrandBadge(
    brand: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 36.dp
) {
    val upper = brand.uppercase()
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when {
            upper.contains("TATA") -> {
                // Tata Motors Official Filled Tata Blue Oval + White Parallel Ribbon 'T'
                drawOval(
                    brush = Brush.verticalGradient(listOf(Color(0xFF2872E8), Color(0xFF1A3A9C))),
                    topLeft = Offset(w * 0.04f, h * 0.16f),
                    size = Size(w * 0.92f, h * 0.68f)
                )
                drawOval(
                    color = TitaniumSilver.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.04f, h * 0.16f),
                    size = Size(w * 0.92f, h * 0.68f),
                    style = Stroke(width = 1.4.dp.toPx())
                )
                val leftRibbon = Path().apply {
                    moveTo(w * 0.16f, h * 0.36f)
                    quadraticTo(w * 0.42f, h * 0.25f, w * 0.44f, h * 0.50f)
                    lineTo(w * 0.44f, h * 0.78f)
                }
                val rightRibbon = Path().apply {
                    moveTo(w * 0.84f, h * 0.36f)
                    quadraticTo(w * 0.58f, h * 0.25f, w * 0.56f, h * 0.50f)
                    lineTo(w * 0.56f, h * 0.78f)
                }
                drawPath(
                    path = leftRibbon,
                    color = TextWhite,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = rightRibbon,
                    color = TextWhite,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            upper.contains("MAHINDRA") -> {
                // Mahindra Official Twin-Peaks M Emblem (Crimson & Chrome on Dark Shield)
                drawRoundRect(
                    color = Color(0xFF141824),
                    topLeft = Offset(w * 0.04f, h * 0.12f),
                    size = Size(w * 0.92f, h * 0.76f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                val peaks = Path().apply {
                    moveTo(w * 0.10f, h * 0.76f)
                    lineTo(w * 0.26f, h * 0.24f)
                    lineTo(cx, h * 0.54f)
                    lineTo(w * 0.74f, h * 0.24f)
                    lineTo(w * 0.90f, h * 0.76f)
                    lineTo(w * 0.74f, h * 0.76f)
                    lineTo(w * 0.66f, h * 0.46f)
                    lineTo(cx, h * 0.72f)
                    lineTo(w * 0.34f, h * 0.46f)
                    lineTo(w * 0.26f, h * 0.76f)
                    close()
                }
                drawPath(
                    path = peaks,
                    brush = Brush.verticalGradient(listOf(Color(0xFFE31837), Color(0xFF9C0B20)))
                )
                drawPath(
                    path = peaks,
                    color = TitaniumSilver,
                    style = Stroke(width = 1.4.dp.toPx(), join = StrokeJoin.Round)
                )
            }

            upper.contains("ASHOK") -> {
                // Ashok Leyland Official Filled Spanish Blue (#016FB6) Seal + White Spinning Wheel & 'L'
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF0284D8), Color(0xFF015A99))),
                    topLeft = Offset(w * 0.06f, h * 0.06f),
                    size = Size(w * 0.88f, h * 0.88f),
                    cornerRadius = CornerRadius(7.dp.toPx())
                )
                drawCircle(
                    color = TextWhite,
                    radius = w * 0.31f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )
                // Inner turbine spokes + central 'L'
                drawLine(
                    color = TextWhite.copy(alpha = 0.75f),
                    start = Offset(w * 0.24f, cy),
                    end = Offset(w * 0.76f, cy),
                    strokeWidth = 1.2.dp.toPx()
                )
                drawLine(
                    color = TextWhite.copy(alpha = 0.75f),
                    start = Offset(cx, h * 0.24f),
                    end = Offset(cx, h * 0.76f),
                    strokeWidth = 1.2.dp.toPx()
                )
                val lMark = Path().apply {
                    moveTo(cx - w * 0.07f, h * 0.28f)
                    lineTo(cx - w * 0.07f, h * 0.64f)
                    lineTo(w * 0.68f, h * 0.64f)
                }
                drawPath(
                    path = lMark,
                    color = TextWhite,
                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            upper.contains("FORCE") -> {
                // Force Motors Official Filled Blue Circle (#176FC1) + White Italicized Forward 'F'
                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFF1A84E8), Color(0xFF0D55A8))),
                    radius = w * 0.44f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = TitaniumSilver.copy(alpha = 0.7f),
                    radius = w * 0.44f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.3.dp.toPx())
                )
                val fPath = Path().apply {
                    moveTo(w * 0.42f, h * 0.27f)
                    lineTo(w * 0.30f, h * 0.75f)
                    moveTo(w * 0.42f, h * 0.27f)
                    lineTo(w * 0.75f, h * 0.27f)
                    moveTo(w * 0.36f, h * 0.50f)
                    lineTo(w * 0.64f, h * 0.50f)
                }
                drawPath(
                    path = fPath,
                    color = TextWhite,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            upper.contains("BHARATBENZ") || upper.contains("BENZ") -> {
                // BharatBenz Chrome Circular Metallic Shield + Daimler 3-Pointed Star
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF283246), Color(0xFF0A0F1B))),
                    radius = w * 0.44f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = TitaniumSilver,
                    radius = w * 0.43f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.4.dp.toPx())
                )
                // Sculpted 3-Pointed Star
                drawLine(
                    color = TitaniumSilver,
                    start = Offset(cx, cy),
                    end = Offset(cx, h * 0.11f),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TitaniumSilver,
                    start = Offset(cx, cy),
                    end = Offset(w * 0.17f, h * 0.75f),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TitaniumSilver,
                    start = Offset(cx, cy),
                    end = Offset(w * 0.83f, h * 0.75f),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = TextWhite,
                    radius = w * 0.08f,
                    center = Offset(cx, cy)
                )
            }

            upper.contains("MARUTI") -> {
                // Maruti Suzuki Stylized 'S' in Suzuki Red & Blue
                drawRoundRect(
                    color = TextWhite,
                    topLeft = Offset(w * 0.06f, h * 0.08f),
                    size = Size(w * 0.88f, h * 0.84f),
                    cornerRadius = CornerRadius(7.dp.toPx())
                )
                val topS = Path().apply {
                    moveTo(w * 0.68f, h * 0.22f)
                    lineTo(w * 0.30f, h * 0.40f)
                    lineTo(w * 0.54f, h * 0.50f)
                }
                val botS = Path().apply {
                    moveTo(w * 0.46f, h * 0.50f)
                    lineTo(w * 0.70f, h * 0.60f)
                    lineTo(w * 0.32f, h * 0.78f)
                }
                drawPath(
                    path = topS,
                    color = Color(0xFFE41B23),
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = botS,
                    color = Color(0xFF005BAA),
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            upper.contains("HYUNDAI") -> {
                drawOval(
                    color = Color(0xFF003B7E),
                    topLeft = Offset(w * 0.04f, h * 0.18f),
                    size = Size(w * 0.92f, h * 0.64f)
                )
                drawOval(
                    color = TitaniumSilver,
                    topLeft = Offset(w * 0.04f, h * 0.18f),
                    size = Size(w * 0.92f, h * 0.64f),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawLine(
                    color = TextWhite,
                    start = Offset(w * 0.38f, h * 0.30f),
                    end = Offset(w * 0.27f, h * 0.70f),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TextWhite,
                    start = Offset(w * 0.73f, h * 0.30f),
                    end = Offset(w * 0.62f, h * 0.70f),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TextWhite,
                    start = Offset(w * 0.33f, h * 0.53f),
                    end = Offset(w * 0.67f, h * 0.47f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            upper.contains("HONDA") -> {
                val trap = Path().apply {
                    moveTo(w * 0.14f, h * 0.16f)
                    lineTo(w * 0.86f, h * 0.16f)
                    lineTo(w * 0.78f, h * 0.84f)
                    lineTo(w * 0.22f, h * 0.84f)
                    close()
                }
                drawPath(path = trap, color = Color(0xFFCC0000))
                drawPath(
                    path = trap,
                    color = TitaniumSilver,
                    style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round)
                )
                drawLine(
                    color = TextWhite,
                    start = Offset(w * 0.30f, h * 0.27f),
                    end = Offset(w * 0.34f, h * 0.73f),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TextWhite,
                    start = Offset(w * 0.70f, h * 0.27f),
                    end = Offset(w * 0.66f, h * 0.73f),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TextWhite,
                    start = Offset(w * 0.33f, h * 0.54f),
                    end = Offset(w * 0.67f, h * 0.54f),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            upper.contains("TOYOTA") -> {
                drawOval(
                    color = Color(0xFFEB0A1E),
                    topLeft = Offset(w * 0.06f, h * 0.16f),
                    size = Size(w * 0.88f, h * 0.68f),
                    style = Stroke(width = 2.4.dp.toPx())
                )
                drawOval(
                    color = TitaniumSilver,
                    topLeft = Offset(w * 0.38f, h * 0.20f),
                    size = Size(w * 0.24f, h * 0.60f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawOval(
                    color = TitaniumSilver,
                    topLeft = Offset(w * 0.20f, h * 0.22f),
                    size = Size(w * 0.60f, h * 0.24f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            upper.contains("KIA") -> {
                drawRoundRect(
                    color = Color(0xFF141824),
                    topLeft = Offset(w * 0.04f, h * 0.18f),
                    size = Size(w * 0.92f, h * 0.64f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                val kiaPath = Path().apply {
                    moveTo(w * 0.14f, h * 0.70f)
                    lineTo(w * 0.14f, h * 0.30f)
                    moveTo(w * 0.14f, h * 0.52f)
                    lineTo(w * 0.34f, h * 0.30f)
                    moveTo(w * 0.24f, h * 0.42f)
                    lineTo(w * 0.42f, h * 0.70f)
                    lineTo(w * 0.50f, h * 0.30f)
                    lineTo(w * 0.58f, h * 0.70f)
                    lineTo(w * 0.72f, h * 0.30f)
                    lineTo(w * 0.86f, h * 0.70f)
                }
                drawPath(
                    path = kiaPath,
                    color = TitaniumSilver,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            upper.contains("EICHER") -> {
                drawOval(
                    color = Color(0xFFE31E24),
                    topLeft = Offset(w * 0.04f, h * 0.18f),
                    size = Size(w * 0.92f, h * 0.64f)
                )
                listOf(0.38f, 0.50f, 0.62f).forEach { yFrac ->
                    drawLine(
                        color = TextWhite,
                        start = Offset(w * 0.24f, h * yFrac),
                        end = Offset(w * 0.76f, h * yFrac),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            upper.contains("VOLVO") -> {
                drawCircle(
                    color = Color(0xFF003057),
                    radius = w * 0.35f,
                    center = Offset(cx * 0.94f, cy * 1.06f)
                )
                drawCircle(
                    color = TitaniumSilver,
                    radius = w * 0.35f,
                    center = Offset(cx * 0.94f, cy * 1.06f),
                    style = Stroke(width = 2.4.dp.toPx())
                )
                drawLine(
                    color = TextWhite,
                    start = Offset(w * 0.18f, cy * 1.06f),
                    end = Offset(w * 0.76f, cy * 1.06f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TitaniumSilver,
                    start = Offset(w * 0.66f, h * 0.36f),
                    end = Offset(w * 0.86f, h * 0.16f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TitaniumSilver,
                    start = Offset(w * 0.70f, h * 0.16f),
                    end = Offset(w * 0.86f, h * 0.16f),
                    strokeWidth = 2.3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = TitaniumSilver,
                    start = Offset(w * 0.86f, h * 0.16f),
                    end = Offset(w * 0.86f, h * 0.32f),
                    strokeWidth = 2.3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            else -> {
                // Volkswagen Filled VW Blue Circle + Crisp White Stacked V/W
                drawCircle(
                    color = Color(0xFF003B7E),
                    radius = w * 0.43f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = TitaniumSilver,
                    radius = w * 0.43f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.6.dp.toPx())
                )
                val vPath = Path().apply {
                    moveTo(w * 0.32f, h * 0.24f)
                    lineTo(cx, h * 0.48f)
                    lineTo(w * 0.68f, h * 0.24f)
                }
                val wPath = Path().apply {
                    moveTo(w * 0.24f, h * 0.42f)
                    lineTo(w * 0.38f, h * 0.76f)
                    lineTo(cx, h * 0.56f)
                    lineTo(w * 0.62f, h * 0.76f)
                    lineTo(w * 0.76f, h * 0.42f)
                }
                drawPath(
                    path = vPath,
                    color = TextWhite,
                    style = Stroke(width = 2.3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = wPath,
                    color = TextWhite,
                    style = Stroke(width = 2.3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

/**
 * Custom graphic logo drawn for EVERY sensor in Sensor Stimulation & ECU Sensor Picker.
 */
@Composable
fun SensorLogoBadge(
    sensor: StimulableSensor,
    accentColor: Color,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 42.dp
) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(10.dp))
            .background(RecessedWell)
            .border(1.2.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            when (sensor) {
                StimulableSensor.RAIL_PRESSURE -> {
                    // Common Rail fuel tube with 3 vertical injector drop ports
                    drawLine(
                        color = accentColor,
                        start = Offset(w * 0.1f, h * 0.35f),
                        end = Offset(w * 0.9f, h * 0.35f),
                        strokeWidth = 3.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    listOf(0.25f, 0.5f, 0.75f).forEach { xFrac ->
                        drawLine(
                            color = TitaniumSilver,
                            start = Offset(w * xFrac, h * 0.35f),
                            end = Offset(w * xFrac, h * 0.80f),
                            strokeWidth = 2.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    drawCircle(
                        color = WarningAmber,
                        radius = w * 0.12f,
                        center = Offset(w * 0.85f, h * 0.35f)
                    )
                }

                StimulableSensor.COOLANT_TEMP -> {
                    // Brass probe thermometer + coolant wave lines
                    drawCircle(
                        color = accentColor,
                        radius = w * 0.17f,
                        center = Offset(cx, h * 0.72f)
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(cx, h * 0.18f),
                        end = Offset(cx, h * 0.68f),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = TitaniumSilver,
                        start = Offset(cx + w * 0.16f, h * 0.30f),
                        end = Offset(w * 0.86f, h * 0.30f),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = TitaniumSilver,
                        start = Offset(cx + w * 0.16f, h * 0.48f),
                        end = Offset(w * 0.82f, h * 0.48f),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                StimulableSensor.VEHICLE_SPEED -> {
                    // Speedometer dial + needle
                    drawArc(
                        color = accentColor,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = Offset(w * 0.1f, h * 0.1f),
                        size = Size(w * 0.8f, h * 0.8f),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawLine(
                        color = StopRed,
                        start = Offset(cx, cy),
                        end = Offset(w * 0.76f, h * 0.28f),
                        strokeWidth = 2.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = TitaniumSilver,
                        radius = w * 0.08f,
                        center = Offset(cx, cy)
                    )
                }

                StimulableSensor.BOOST_PRESSURE -> {
                    // Turbo snail housing + boost arrow
                    drawCircle(
                        color = accentColor,
                        radius = w * 0.32f,
                        center = Offset(cx * 0.9f, cy * 1.05f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawCircle(
                        color = TitaniumSilver,
                        radius = w * 0.10f,
                        center = Offset(cx * 0.9f, cy * 1.05f)
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(cx * 0.9f, h * 0.18f),
                        end = Offset(w * 0.90f, h * 0.18f),
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                StimulableSensor.AIR_MASS -> {
                    // Triple aerodynamic airflow streams
                    listOf(0.28f, 0.50f, 0.72f).forEachIndexed { idx, yFrac ->
                        val endX = if (idx == 1) 0.92f else 0.78f
                        drawLine(
                            color = if (idx == 1) accentColor else TitaniumSilver,
                            start = Offset(w * 0.10f, h * yFrac),
                            end = Offset(w * endX, h * yFrac),
                            strokeWidth = 2.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

/**
 * Glossy 3D gradient automotive pill button.
 */
@Composable
fun GlossyActionButton(
    text: String,
    icon: ImageVector?,
    topColor: Color,
    bottomColor: Color,
    borderColor: Color = Color.White.copy(alpha = 0.25f),
    testTag: String = "",
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(listOf(topColor, bottomColor))
            )
            .border(1.dp, borderColor, shape)
            .clickable { onClick() }
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = TextWhite,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = TextWhite,
                maxLines = 1
            )
        }
    }
}

/**
 * Ascending 4-bar signal strength meter.
 */
@Composable
fun AscendingSignalBars(
    fraction: Float,
    activeColor: Color = SignalGreen,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    maxHeight: Dp = 28.dp
) {
    val safeFrac = fraction.coerceIn(0f, 1f)
    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 1..barCount) {
            val threshold = i.toFloat() / (barCount + 0.35f)
            val isLit = safeFrac >= threshold || (safeFrac > 0.02f && i == 1)
            val heightRatio = 0.32f + (0.68f * (i.toFloat() / barCount))
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight(heightRatio)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isLit) activeColor else TextMuted.copy(alpha = 0.25f)
                    )
            )
        }
    }
}
