package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppConfig
import com.example.model.AssistantStatus
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.MetallicSurfaceHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopBarGlassmorphism(
    status: AssistantStatus,
    continuousListeningEnabled: Boolean,
    onToggleContinuousListening: (Boolean) -> Unit,
    pitch: Float,
    onPitchChange: (Float) -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Pulsing dot for live status
    val infiniteTransition = rememberInfiniteTransition(label = "StatusPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .border(
                1.dp,
                GlassBorder,
                RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            ),
        color = GlassSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MetallicSurfaceHighlight)
                        .border(1.dp, NeonCyan, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "MOTO AI",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "WAKE: \"${AppConfig.PRIMARY_WAKE_WORD}\"",
                        color = NeonCyan.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status Indicator & Quick Access Settings Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live Status Badge
                val statusColor = when (status) {
                    AssistantStatus.LISTENING -> NeonCyan
                    AssistantStatus.THINKING -> ElectricViolet
                    AssistantStatus.SPEAKING -> NeonMint
                    AssistantStatus.GENERATING_MEDIA -> LaserBlue
                    AssistantStatus.ONLINE -> NeonCyan
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MetallicSurfaceHighlight)
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = dotAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status.name,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Settings Button
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MetallicSurfaceHighlight)
                        .testTag("quick_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Assistant Settings",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Quick Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("CLOSE", color = NeonCyan, fontFamily = FontFamily.Monospace)
                }
            },
            title = {
                Text(
                    text = "CYBERNETIC TELEMETRY",
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Continuous Wake Word Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Continuous Listening",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Auto-listen for \"Hey Moto\"",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = continuousListeningEnabled,
                            onCheckedChange = onToggleContinuousListening,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = LaserBlue,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = DarkMetallicSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Robotic TTS Pitch Control (default 0.85f)
                    Text(
                        text = "Robotic Voice Pitch: ${String.format("%.2f", pitch)}x",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = pitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = LaserBlue,
                            inactiveTrackColor = DarkMetallicSurface
                        )
                    )

                    // Robotic TTS Speed Control (default 1.0f)
                    Text(
                        text = "Voice Rate: ${String.format("%.2f", speed)}x",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = speed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = LaserBlue,
                            inactiveTrackColor = DarkMetallicSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Model & Engine specs
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MetallicSurfaceHighlight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "CORE SPECS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Model: ${AppConfig.GEMINI_MODEL_NAME}\nWake Word: ${AppConfig.PRIMARY_WAKE_WORD} / ${AppConfig.SECONDARY_WAKE_WORD}\nTTS Engine: Android Cyber Synth",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clear logs action
                    OutlinedButton(
                        onClick = {
                            onClearHistory()
                            showSettingsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear logs",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PURGE LOGS", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            },
            containerColor = DeepSpaceBackground
        )
    }
}
