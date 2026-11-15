package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.StopRed
import com.example.ui.theme.StopRedDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.carbonFiberBackground

@Composable
fun LogoSplashScreen() {
    EcuSplashVisualContent(
        showBootLoading = false,
        testTagName = "logo_splash_screen"
    )
}

@Composable
fun BootLoadingScreen() {
    EcuSplashVisualContent(
        showBootLoading = true,
        testTagName = "boot_loading_screen"
    )
}

@Composable
private fun EcuSplashVisualContent(
    showBootLoading: Boolean,
    testTagName: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .carbonFiberBackground()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag(testTagName),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Glowing Vehicle Front Silhouette
            Image(
                painter = painterResource(id = R.drawable.img_car_front_glow_1794742208220),
                contentDescription = "Automotive Vehicle Silhouette",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Center High-Fi AK ECU STIMULATOR Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(if (showBootLoading) 1f else pulseScale)
            ) {
                HighFiAppLogoBadge(size = 120.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AK ECU STIMULATOR",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 27.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(listOf(StopRedDark, StopRed, StopRedDark))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "ECU & SENSOR BENCH",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Test  •  Simulate  •  Diagnose",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp
                    ),
                    color = TextWhite.copy(alpha = 0.9f)
                )
            }

            // Bottom ECU Module Hardware Render + Boot Loading Spinner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ecu_hardware_box_1794742207159),
                    contentDescription = "ECU Simulator Module",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 3.dp,
                        color = CyanGlow,
                        trackColor = CyanGlow.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (showBootLoading) "BOOT LOADING..." else "INITIALIZING AK ECU STIMULATOR...",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (showBootLoading) CyanGlow else TextSecondary,
                    modifier = Modifier.testTag(if (showBootLoading) "boot_loading_text" else "splash_status_text")
                )
            }
        }
    }
}
