package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.example.model.GeneratedMediaItem
import com.example.ui.theme.DarkMetallicSurface
import com.example.ui.theme.DeepSpaceBackground
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
import kotlinx.coroutines.delay

@Composable
fun GlassmorphicMediaCard(
    media: GeneratedMediaItem,
    onPlayAudio: (String) -> Unit,
    onRegenerate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlayingVideo by remember { mutableStateOf(false) }
    var videoProgress by remember { mutableFloatStateOf(0f) }
    var showFullscreenPreview by remember { mutableStateOf(false) }

    // Simulated video playback progress loop
    LaunchedEffect(isPlayingVideo) {
        if (isPlayingVideo) {
            while (isPlayingVideo) {
                delay(100)
                videoProgress = (videoProgress + 0.02f)
                if (videoProgress >= 1f) {
                    videoProgress = 0f
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .testTag("media_card_${media.id}"),
        color = GlassSurface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Category tag and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (media.isVideo) ElectricViolet else NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (media.isVideo) "AI NEURAL VIDEO // 1080P" else "AI NEURAL RENDER // 4K",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (media.isVideo) ElectricViolet else NeonCyan
                    )
                }

                Text(
                    text = "ID: ${media.id.take(6).uppercase()}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Media Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (media.isVideo) 16f / 9f else 1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkMetallicSurface)
                    .clickable {
                        if (media.isVideo) {
                            isPlayingVideo = !isPlayingVideo
                        } else {
                            showFullscreenPreview = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = media.mediaUrl,
                    contentDescription = media.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    },
                    error = {
                        // High-tech fallback graphic
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MetallicSurfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "[NEURAL MEDIA RENDERING]",
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan,
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                // Video Overlay & Play Button
                if (media.isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )

                    IconButton(
                        onClick = { isPlayingVideo = !isPlayingVideo },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DeepSpaceBackground.copy(alpha = 0.75f))
                            .border(1.dp, if (isPlayingVideo) NeonMint else NeonCyan, CircleShape)
                            .testTag("play_video_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingVideo) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingVideo) "Pause Video" else "Play Video",
                            tint = if (isPlayingVideo) NeonMint else NeonCyan
                        )
                    }

                    // Bottom scrub bar for video
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { if (isPlayingVideo) videoProgress else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = NeonCyan,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Prompt description
            Text(
                text = media.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "“${media.prompt}”",
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Play Audio, Download, Share, Re-generate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Audio Voice synthesis
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MetallicSurfaceHighlight)
                        .clickable { onPlayAudio(media.prompt) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("play_audio_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak Prompt",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VOICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Download action
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Asset saved to holographic storage", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("download_media_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share action
                    IconButton(
                        onClick = {
                            shareMedia(context, media)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_media_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Re-generate action
                    IconButton(
                        onClick = { onRegenerate(media.prompt) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("regenerate_media_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-generate",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Fullscreen Image Dialog Preview
    if (showFullscreenPreview) {
        Dialog(onDismissRequest = { showFullscreenPreview = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepSpaceBackground)
                    .border(1.dp, NeonCyan, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SubcomposeAsyncImage(
                        model = media.mediaUrl,
                        contentDescription = media.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = media.prompt,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

private fun shareMedia(context: Context, media: GeneratedMediaItem) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Generated via MOTO AI ASSISTANT: ${media.title}\n${media.mediaUrl}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share MOTO AI Asset")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
    }
}
