package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AppConfig
import com.example.engine.AiBrainService
import com.example.engine.AiResponse
import com.example.engine.VoiceEngine
import com.example.model.AssistantStatus
import com.example.model.ChatMessage
import com.example.model.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MotoAiViewModel(application: Application) : AndroidViewModel(application) {

    private val aiBrainService = AiBrainService()

    private val _status = MutableStateFlow(AssistantStatus.ONLINE)
    val status: StateFlow<AssistantStatus> = _status.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _continuousListening = MutableStateFlow(true)
    val continuousListening: StateFlow<Boolean> = _continuousListening.asStateFlow()

    private val _voicePitch = MutableStateFlow(0.85f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(1.0f)
    val voiceRate: StateFlow<Float> = _voiceRate.asStateFlow()

    private val _isMicPermissionGranted = MutableStateFlow(false)
    val isMicPermissionGranted: StateFlow<Boolean> = _isMicPermissionGranted.asStateFlow()

    private var voiceEngine: VoiceEngine? = null

    init {
        // Initial greeting
        addMessage(
            ChatMessage(
                type = MessageType.AI_TEXT_RESPONSE,
                sender = AppConfig.APP_NAME,
                text = "MOTO AI neural core online. Say \"Hey Moto\" or tap the core to initiate voice command."
            )
        )
    }

    fun onPermissionResult(granted: Boolean) {
        _isMicPermissionGranted.value = granted
        if (granted) {
            setupVoiceEngine()
        } else {
            addMessage(
                ChatMessage(
                    type = MessageType.SYSTEM_ALERT,
                    sender = "SYSTEM",
                    text = "Microphone access required for real-time speech and wake-word detection."
                )
            )
        }
    }

    private fun setupVoiceEngine() {
        if (voiceEngine != null) return

        voiceEngine = VoiceEngine(
            context = getApplication(),
            onWakeWordDetected = { cleanQuery, fullTranscript ->
                handleWakeWordActivated(cleanQuery, fullTranscript)
            },
            onIgnoredSpeech = { transcript ->
                handleIgnoredSpeech(transcript)
            },
            onRmsChanged = { level ->
                if (_status.value == AssistantStatus.LISTENING) {
                    _audioLevel.value = level
                }
            },
            onSpeakingStateChanged = { isSpeaking ->
                if (isSpeaking) {
                    _status.value = AssistantStatus.SPEAKING
                } else {
                    _audioLevel.value = 0f
                    if (_continuousListening.value && _isMicPermissionGranted.value) {
                        _status.value = AssistantStatus.LISTENING
                    } else {
                        _status.value = AssistantStatus.ONLINE
                    }
                }
            },
            onErrorLogged = { error ->
                // System debug log
            }
        ).apply {
            this.voicePitch = _voicePitch.value
            this.voiceRate = _voiceRate.value
        }

        if (_continuousListening.value) {
            startContinuousListening()
        }
    }

    private fun handleWakeWordActivated(cleanQuery: String, fullTranscript: String) {
        viewModelScope.launch {
            // User query message with wake word indicator
            addMessage(
                ChatMessage(
                    type = MessageType.USER_QUERY,
                    sender = "USER",
                    text = fullTranscript,
                    wakeWordDetected = true
                )
            )

            processQuery(cleanQuery)
        }
    }

    private fun handleIgnoredSpeech(transcript: String) {
        viewModelScope.launch {
            // Log telemetry indicator for ignored non-wake-word sound
            if (transcript.isNotBlank()) {
                addMessage(
                    ChatMessage(
                        type = MessageType.SYSTEM_ALERT,
                        sender = "SYSTEM",
                        text = "Filtered: \"$transcript\" (Ignored: Missing \"Hey Moto\")"
                    )
                )
            }
        }
    }

    fun triggerOrbTapped() {
        if (!_isMicPermissionGranted.value) return

        if (_status.value == AssistantStatus.SPEAKING) {
            voiceEngine?.stopSpeaking()
            return
        }

        voiceEngine?.triggerHapticFeedback()
        _status.value = AssistantStatus.LISTENING
        voiceEngine?.startListening(continuous = false)
    }

    fun submitQuery(userQuery: String) {
        viewModelScope.launch {
            addMessage(
                ChatMessage(
                    type = MessageType.USER_QUERY,
                    sender = "USER",
                    text = userQuery,
                    wakeWordDetected = true
                )
            )
            processQuery(userQuery)
        }
    }

    private suspend fun processQuery(query: String) {
        _status.value = AssistantStatus.THINKING
        _audioLevel.value = 0f

        val response = aiBrainService.processQuery(query)

        when (response) {
            is AiResponse.Text -> {
                addMessage(
                    ChatMessage(
                        type = MessageType.AI_TEXT_RESPONSE,
                        sender = AppConfig.APP_NAME,
                        text = response.content
                    )
                )
                voiceEngine?.speak(response.content)
            }

            is AiResponse.Image -> {
                _status.value = AssistantStatus.GENERATING_MEDIA
                addMessage(
                    ChatMessage(
                        type = MessageType.AI_IMAGE_GENERATION,
                        sender = AppConfig.APP_NAME,
                        text = response.explanation,
                        mediaItem = response.mediaItem
                    )
                )
                voiceEngine?.speak(response.explanation)
            }

            is AiResponse.Video -> {
                _status.value = AssistantStatus.GENERATING_MEDIA
                addMessage(
                    ChatMessage(
                        type = MessageType.AI_VIDEO_GENERATION,
                        sender = AppConfig.APP_NAME,
                        text = response.explanation,
                        mediaItem = response.mediaItem
                    )
                )
                voiceEngine?.speak(response.explanation)
            }

            is AiResponse.Error -> {
                addMessage(
                    ChatMessage(
                        type = MessageType.AI_TEXT_RESPONSE,
                        sender = AppConfig.APP_NAME,
                        text = response.message
                    )
                )
                voiceEngine?.speak(response.message)
            }
        }
    }

    fun speakText(text: String) {
        voiceEngine?.speak(text)
    }

    fun regenerateMedia(prompt: String) {
        submitQuery("generate image of $prompt")
    }

    fun startContinuousListening() {
        if (_isMicPermissionGranted.value) {
            _continuousListening.value = true
            _status.value = AssistantStatus.LISTENING
            voiceEngine?.startListening(continuous = true)
        }
    }

    fun stopContinuousListening() {
        _continuousListening.value = false
        _status.value = AssistantStatus.ONLINE
        voiceEngine?.stopListening()
    }

    fun toggleContinuousListening(enabled: Boolean) {
        if (enabled) {
            startContinuousListening()
        } else {
            stopContinuousListening()
        }
    }

    fun setPitch(pitch: Float) {
        _voicePitch.value = pitch
        voiceEngine?.voicePitch = pitch
    }

    fun setSpeed(speed: Float) {
        _voiceRate.value = speed
        voiceEngine?.voiceRate = speed
    }

    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(
                type = MessageType.SYSTEM_ALERT,
                sender = "SYSTEM",
                text = "Telemetry logs purged. Neural link ready."
            )
        )
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine?.destroy()
    }
}
