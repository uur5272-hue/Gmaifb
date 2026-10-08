package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AssistantStatus
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MetallicCoreOrb(
    status: AssistantStatus,
    audioLevel: Float, // Normalized 0f..1f from mic or TTS
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")

    // Slow continuous rotation for idle outer mechanics
    val idleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "IdleRotation"
    )

    // Fast rotation for Thinking state
    val fastRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FastRotation"
    )

    // Subtle breathing pulse for idle
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingPulse"
    )

    // Dynamic wave expansion for listening / speaking
    val waveExpansion by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveExpansion"
    )

    // Equalizer oscillation for speaking
    val eqWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EqualizerWave"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("metallic_core_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.42f

            // Dynamic scale depending on status and audio amplitude
            val dynamicScale = when (status) {
                AssistantStatus.LISTENING -> 1.0f + (audioLevel * 0.28f)
                AssistantStatus.SPEAKING -> 1.02f + (sin(eqWave) * 0.08f)
                AssistantStatus.THINKING -> 1.0f
                else -> breathingPulse
            }
            val coreRadius = baseRadius * dynamicScale

            // 1. OUTER EXPANDING SHOCKWAVES (Listening & Speaking states)
            if (status == AssistantStatus.LISTENING || status == AssistantStatus.SPEAKING) {
                val rippleCount = 3
                for (i in 0 until rippleCount) {
                    val progress = (waveExpansion + (i.toFloat() / rippleCount)) % 1f
                    val rippleRadius = coreRadius + (progress * baseRadius * 1.3f * (if (status == AssistantStatus.LISTENING) (0.5f + audioLevel) else 0.8f))
                    val alpha = (1f - progress) * (if (status == AssistantStatus.LISTENING) 0.65f else 0.45f)
                    val rippleColor = if (status == AssistantStatus.LISTENING) NeonCyan else LaserBlue

                    drawCircle(
                        color = rippleColor.copy(alpha = alpha.coerceIn(0f, 1f)),
                        radius = rippleRadius,
                        center = center,
                        style = Stroke(width = (2.5f * (1f - progress)).coerceAtLeast(1f).dp.toPx())
                    )
                }
            }

            // 2. ORBITAL DUAL RINGS & SEGMENTED ARCS
            val outerRingRadius = baseRadius * 1.55f
            val currentRotation = if (status == AssistantStatus.THINKING) fastRotation else idleRotation

            // Ring A (Laser Blue & Neon Cyan Dash Arc)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(35f, 25f, 15f, 25f), currentRotation * 1.5f)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.8f),
                        LaserBlue.copy(alpha = 0.3f),
                        ElectricViolet.copy(alpha = 0.8f),
                        NeonCyan.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                radius = outerRingRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx(), pathEffect = dashEffect)
            )

            // Ring B (Counter-rotating Electric Violet ring)
            val innerRingRadius = baseRadius * 1.25f
            val counterRotation = -currentRotation * 1.2f
            val innerDash = PathEffect.dashPathEffect(floatArrayOf(50f, 30f), counterRotation * 2f)
            drawCircle(
                color = if (status == AssistantStatus.THINKING) ElectricViolet else LaserBlue.copy(alpha = 0.5f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx(), pathEffect = innerDash)
            )

            // 3. THINKING PARTICLES & SPARKS
            if (status == AssistantStatus.THINKING) {
                val particleCount = 12
                for (p in 0 until particleCount) {
                    val angle = (fastRotation * 2f + (p * 360f / particleCount)) * (PI / 180f)
                    val particleDist = innerRingRadius + (sin(p.toDouble() + fastRotation * 0.05) * 18f).toFloat()
                    val px = center.x + (cos(angle) * particleDist).toFloat()
                    val py = center.y + (sin(angle) * particleDist).toFloat()
                    drawCircle(
                        color = if (p % 2 == 0) NeonCyan else NeonMint,
                        radius = 3.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // 4. SPEAKING EQUALIZER WAVEFORM (Audio Spectrum Arc)
            if (status == AssistantStatus.SPEAKING) {
                val barCount = 28
                val eqRadius = baseRadius * 1.15f
                for (b in 0 until barCount) {
                    val barAngle = (b * (360f / barCount)) * (PI / 180f)
                    val barHeight = (10f + (sin(eqWave + b * 0.8) * 16f).toFloat()).coerceAtLeast(4f)
                    val x1 = center.x + (cos(barAngle) * eqRadius).toFloat()
                    val y1 = center.y + (sin(barAngle) * eqRadius).toFloat()
                    val x2 = center.x + (cos(barAngle) * (eqRadius + barHeight)).toFloat()
                    val y2 = center.y + (sin(barAngle) * (eqRadius + barHeight)).toFloat()

                    drawLine(
                        color = NeonCyan,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 5. GLOWING ENERGY FIELD (Radial Background Aura)
            val glowColor = when (status) {
                AssistantStatus.LISTENING -> NeonCyan
                AssistantStatus.THINKING -> ElectricViolet
                AssistantStatus.SPEAKING -> NeonMint
                AssistantStatus.GENERATING_MEDIA -> LaserBlue
                else -> NeonCyan
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.55f),
                        LaserBlue.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.5f
                ),
                radius = coreRadius * 1.5f,
                center = center
            )

            // 6. 3D METALLIC SPHERE CORE
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        NeonCyan,
                        LaserBlue,
                        DeepSpaceBackground
                    ),
                    center = Offset(center.x - coreRadius * 0.25f, center.y - coreRadius * 0.25f),
                    radius = coreRadius * 1.1f
                ),
                radius = coreRadius,
                center = center
            )

            // 7. HIGH-TECH INNER GEOMETRIC APERTURE
            val aperturePath = Path()
            val blades = 6
            val innerApertureRadius = coreRadius * 0.42f
            for (i in 0 until blades) {
                val angleRad = (i * 360f / blades + (currentRotation * 0.5f)) * (PI / 180f)
                val tipX = center.x + (cos(angleRad) * innerApertureRadius).toFloat()
                val tipY = center.y + (sin(angleRad) * innerApertureRadius).toFloat()
                if (i == 0) aperturePath.moveTo(tipX, tipY) else aperturePath.lineTo(tipX, tipY)
            }
            aperturePath.close()

            drawPath(
                path = aperturePath,
                color = DeepSpaceBackground.copy(alpha = 0.85f)
            )

            drawPath(
                path = aperturePath,
                color = NeonCyan,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Center Cybernetic Pupil / Status Core
            drawCircle(
                color = when (status) {
                    AssistantStatus.LISTENING -> NeonCyan
                    AssistantStatus.THINKING -> ElectricViolet
                    AssistantStatus.SPEAKING -> NeonMint
                    AssistantStatus.GENERATING_MEDIA -> Color.White
                    else -> NeonCyan
                },
                radius = coreRadius * 0.16f,
                center = center
            )
        }
    }
}
