package com.example.model

enum class AssistantStatus(val label: String) {
    ONLINE("ONLINE // STANDBY"),
    LISTENING("LISTENING..."),
    THINKING("THINKING..."),
    SPEAKING("SPEAKING..."),
    GENERATING_MEDIA("GENERATING MEDIA...")
}

enum class MessageType {
    USER_QUERY,
    AI_TEXT_RESPONSE,
    AI_IMAGE_GENERATION,
    AI_VIDEO_GENERATION,
    SYSTEM_ALERT
}

data class GeneratedMediaItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isVideo: Boolean = false,
    val title: String,
    val prompt: String,
    val mediaUrl: String,
    val durationSec: Int = 10,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: MessageType,
    val sender: String,
    val text: String,
    val wakeWordDetected: Boolean = false,
    val mediaItem: GeneratedMediaItem? = null,
    val timestamp: Long = System.currentTimeMillis()
)
