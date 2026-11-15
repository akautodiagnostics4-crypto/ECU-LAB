package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Midnight-blue radial glow + subtle diagnostic circuit grid background
 * matching the AK ECU SIGNAL LAB aesthetic.
 */
fun Modifier.carbonFiberBackground(): Modifier = this.drawBehind {
    // Base midnight navy background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF040A19),
                Color(0xFF07142E),
                Color(0xFF030712)
            )
        )
    )

    // Radial electric-blue studio spotlight
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF123578).copy(alpha = 0.45f),
                Color(0xFF0A1D44).copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.5f, size.height * 0.28f),
            radius = size.maxDimension * 0.65f
        )
    )

    // Subtle diagnostic circuit trace lines
    val traceColor = Color(0xFF1A4B9E).copy(alpha = 0.14f)
    val step = 44.dp.toPx()
    var y = step
    while (y < size.height) {
        drawLine(
            color = traceColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
        y += step
    }
    var x = step
    while (x < size.width) {
        drawLine(
            color = traceColor.copy(alpha = 0.08f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx()
        )
        x += step
    }
}

/**
 * Sleek metallic dark-navy automotive card panel with specular top rim.
 */
fun Modifier.brushedMetalPanel(
    cornerRadius: Dp = 14.dp,
    borderColor: Color = BorderSubtle,
    borderWidth: Dp = 1.dp,
    elevated: Boolean = false
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val topColor = if (elevated) Color(0xFF162B4E) else CardElevatedSurface
    val midColor = if (elevated) Color(0xFF0E1C36) else CardDarkSurface
    val botColor = if (elevated) Color(0xFF091224) else BrushedMetalBottom

    return this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, midColor, botColor)
            )
        )
        .border(
            border = BorderStroke(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (borderColor == BorderSubtle) MetalHighlightEdge else borderColor,
                        borderColor.copy(alpha = 0.6f)
                    )
                )
            ),
            shape = shape
        )
}

@Composable
fun recessedWellModifier(
    cornerRadius: Dp = 10.dp,
    borderColor: Color = BorderSubtle
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return Modifier
        .clip(shape)
        .background(RecessedWell)
        .border(1.dp, borderColor.copy(alpha = 0.75f), shape)
}
