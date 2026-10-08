package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.ChatMessage
import com.example.model.MessageType
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LaserBlue
import com.example.ui.theme.MetallicSurfaceHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoiceChatTranscript(
    messages: List<ChatMessage>,
    listState: LazyListState,
    onSpeakText: (String) -> Unit,
    onRegenerateMedia: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    LazyColumn(
        state = listState,
        modifier = modifier.testTag("chat_transcript_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            when (msg.type) {
                MessageType.USER_QUERY -> {
                    UserMessageBubble(
                        message = msg,
                        formattedTime = timeFormat.format(Date(msg.timestamp))
                    )
                }

                MessageType.AI_TEXT_RESPONSE -> {
                    AiTextMessageBubble(
                        message = msg,
                        formattedTime = timeFormat.format(Date(msg.timestamp)),
                        onSpeakText = onSpeakText
                    )
                }

                MessageType.AI_IMAGE_GENERATION,
                MessageType.AI_VIDEO_GENERATION -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AiTextMessageBubble(
                            message = msg,
                            formattedTime = timeFormat.format(Date(msg.timestamp)),
                            onSpeakText = onSpeakText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        msg.mediaItem?.let { media ->
                            GlassmorphicMediaCard(
                                media = media,
                                onPlayAudio = onSpeakText,
                                onRegenerate = onRegenerateMedia
                            )
                        }
                    }
                }

                MessageType.SYSTEM_ALERT -> {
                    SystemAlertBubble(
                        message = msg,
                        formattedTime = timeFormat.format(Date(msg.timestamp))
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: ChatMessage,
    formattedTime: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (message.wakeWordDetected) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LaserBlue.copy(alpha = 0.35f))
                        .border(1.dp, LaserBlue, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WAKE DETECTED",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = formattedTime,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = DarkMetallicSurface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AiTextMessageBubble(
    message: ChatMessage,
    formattedTime: String,
    onSpeakText: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "MOTO AI",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formattedTime,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = GlassSurface,
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onSpeakText(message.text) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Replay audio",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemAlertBubble(
    message: ChatMessage,
    formattedTime: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MetallicSurfaceHighlight.copy(alpha = 0.6f))
                .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYS [$formattedTime]: ${message.text}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
        }
    }
}
