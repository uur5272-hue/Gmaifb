package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AssistantStatus
import com.example.ui.components.CyberHudBackground
import com.example.ui.components.MetallicCoreOrb
import com.example.ui.components.TopBarGlassmorphism
import com.example.ui.components.VoiceChatTranscript
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.MetallicSurfaceHighlight
import com.example.ui.theme.MotoAiTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MotoAiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MotoAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MotoAiTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MotoAiViewModel) {
    val context = LocalContext.current
    val status by viewModel.status.collectAsStateWithLifecycle()
    val audioLevel by viewModel.audioLevel.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val continuousListening by viewModel.continuousListening.collectAsStateWithLifecycle()
    val voicePitch by viewModel.voicePitch.collectAsStateWithLifecycle()
    val voiceRate by viewModel.voiceRate.collectAsStateWithLifecycle()
    val isMicGranted by viewModel.isMicPermissionGranted.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var textInput by remember { mutableStateOf("") }

    val quickSuggestions = listOf(
        "Hey Moto, tell me your capabilities",
        "Hey Moto, generate image of cybernetic skyline",
        "Hey Moto, create a video of robotic drone delivery",
        "Hey Moto, explain quantum superposition",
        "Hey Moto, synthesize an encrypted cyber-briefing"
    )

    CyberHudBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())) {
                    TopBarGlassmorphism(
                        status = status,
                        continuousListeningEnabled = continuousListening,
                        onToggleContinuousListening = { viewModel.toggleContinuousListening(it) },
                        pitch = voicePitch,
                        onPitchChange = { viewModel.setPitch(it) },
                        speed = voiceRate,
                        onSpeedChange = { viewModel.setSpeed(it) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
            },
            bottomBar = {
                // Bottom Input Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                        .imePadding()
                ) {
                    // Quick Action Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickSuggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkMetallicSurface.copy(alpha = 0.85f))
                                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.submitQuery(suggestion)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("suggestion_chip_${suggestion.take(10)}")
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonCyan
                                )
                            }
                        }
                    }

                    // Input bar with text input and microphone push-to-talk
                    Surface(
                        color = GlassSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = {
                                    Text(
                                        text = "Say \"Hey Moto...\" or type query...",
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("user_input_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = NeonCyan
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (textInput.isNotBlank()) {
                                            viewModel.submitQuery(textInput)
                                            textInput = ""
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            if (textInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.submitQuery(textInput)
                                        textInput = ""
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                        .testTag("send_query_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send Query",
                                        tint = DeepSpaceBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                // Mic trigger / push to talk button
                                IconButton(
                                    onClick = { viewModel.triggerOrbTapped() },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (status == AssistantStatus.LISTENING) NeonCyan else LaserBlue
                                        )
                                        .testTag("mic_push_to_talk_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = if (status == AssistantStatus.LISTENING) DeepSpaceBackground else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .widthIn(max = 640.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Permission Warning Banner if mic not granted
                if (!isMicGranted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MetallicSurfaceHighlight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MICROPHONE PERMISSION NEEDED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonCyan
                                )
                                Text(
                                    text = "Grant audio permission to enable \"Hey Moto\" wake-word voice detection.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                            ) {
                                Text(
                                    text = "GRANT",
                                    color = DeepSpaceBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Central Hero Section: 3D Pulsing Metallic Core & HUD Feedback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetallicCoreOrb(
                            status = status,
                            audioLevel = audioLevel,
                            size = 200.dp,
                            onClick = { viewModel.triggerOrbTapped() }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val statusLabel = when (status) {
                            AssistantStatus.ONLINE -> "SAY \"HEY MOTO\" OR TAP CORE"
                            AssistantStatus.LISTENING -> "LISTENING AUDIO SPECTRUM..."
                            AssistantStatus.THINKING -> "NEURAL QUANTUM COMPUTE..."
                            AssistantStatus.SPEAKING -> "VOICE SYNTHESIS ACTIVE"
                            AssistantStatus.GENERATING_MEDIA -> "SYNTHESIZING MEDIA MATRIX..."
                        }

                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.4.sp,
                            color = when (status) {
                                AssistantStatus.LISTENING -> NeonCyan
                                AssistantStatus.THINKING -> ElectricViolet
                                AssistantStatus.SPEAKING -> NeonMint
                                AssistantStatus.GENERATING_MEDIA -> LaserBlue
                                AssistantStatus.ONLINE -> TextSecondary
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Voice Transcript & Generated Media History
                VoiceChatTranscript(
                    messages = messages,
                    listState = listState,
                    onSpeakText = { viewModel.speakText(it) },
                    onRegenerateMedia = { viewModel.regenerateMedia(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}
