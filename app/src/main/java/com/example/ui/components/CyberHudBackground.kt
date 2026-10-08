package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberGridLine
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.NeonCyan

@Composable
fun CyberHudBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepSpaceBackground,
                        DarkMetallicSurface.copy(alpha = 0.85f),
                        DeepSpaceBackground
                    )
                )
            )
    ) {
        // High-tech HUD grid and framing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            val w = size.width
            val h = size.height

            // 1. Subtle Cyberpunk Grid Lines
            var x = 0f
            while (x <= w) {
                drawLine(
                    color = CyberGridLine,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 0.8f
                )
                x += step
            }

            var y = 0f
            while (y <= h) {
                drawLine(
                    color = CyberGridLine,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 0.8f
                )
                y += step
            }

            // 2. Corner High-Tech Reticle Brackets
            val bracketLen = 28.dp.toPx()
            val pad = 16.dp.toPx()
            val bracketColor = NeonCyan.copy(alpha = 0.35f)
            val strokeW = 1.5.dp.toPx()

            // Top-Left
            drawLine(bracketColor, Offset(pad, pad), Offset(pad + bracketLen, pad), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(pad, pad), Offset(pad, pad + bracketLen), strokeW, StrokeCap.Round)

            // Top-Right
            drawLine(bracketColor, Offset(w - pad, pad), Offset(w - pad - bracketLen, pad), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(w - pad, pad), Offset(w - pad, pad + bracketLen), strokeW, StrokeCap.Round)

            // Bottom-Left
            drawLine(bracketColor, Offset(pad, h - pad), Offset(pad + bracketLen, h - pad), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(pad, h - pad), Offset(pad, h - pad - bracketLen), strokeW, StrokeCap.Round)

            // Bottom-Right
            drawLine(bracketColor, Offset(w - pad, h - pad), Offset(w - pad - bracketLen, h - pad), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(w - pad, h - pad), Offset(w - pad, h - pad - bracketLen), strokeW, StrokeCap.Round)

            // 3. Central Radial Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LaserBlue.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h * 0.42f),
                    radius = w * 0.75f
                ),
                radius = w * 0.75f,
                center = Offset(w / 2f, h * 0.42f)
            )
        }

        content()
    }
}
