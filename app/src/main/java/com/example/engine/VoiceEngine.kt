package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.AppConfig
import java.util.Locale

class VoiceEngine(
    private val context: Context,
    private val onWakeWordDetected: (cleanQuery: String, fullTranscript: String) -> Unit,
    private val onIgnoredSpeech: (transcript: String) -> Unit,
    private val onRmsChanged: (rmsDbNormalized: Float) -> Unit,
    private val onSpeakingStateChanged: (isSpeaking: Boolean) -> Unit,
    private val onErrorLogged: (error: String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var isListening = false
    private var continuousMode = true

    // Robotic Voice parameters requested: Pitch 0.85f, SpeechRate 1.0f
    var voicePitch: Float = 0.85f
        set(value) {
            field = value
            textToSpeech?.setPitch(value)
        }

    var voiceRate: Float = 1.0f
        set(value) {
            field = value
            textToSpeech?.setSpeechRate(value)
        }

    init {
        initializeTts()
        initializeRecognizer()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                onErrorLogged("TTS language US is not supported or missing data")
            } else {
                textToSpeech?.setPitch(voicePitch)
                textToSpeech?.setSpeechRate(voiceRate)
                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onSpeakingStateChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        onSpeakingStateChanged(false)
                        // If continuous listening is enabled, resume listening after speaking
                        if (continuousMode) {
                            startListening()
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onSpeakingStateChanged(false)
                        if (continuousMode) {
                            startListening()
                        }
                    }
                })
            }
        } else {
            onErrorLogged("TTS initialization failed with code $status")
        }
    }

    private fun initializeRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceEngine)
                }
            } else {
                onErrorLogged("SpeechRecognizer is not available on this device")
            }
        } catch (e: Exception) {
            onErrorLogged("Failed to init SpeechRecognizer: ${e.message}")
        }
    }

    fun startListening(continuous: Boolean = true) {
        continuousMode = continuous
        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            isListening = false
            onErrorLogged("Start listening error: ${e.message}")
        }
    }

    fun stopListening() {
        isListening = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Stop listening error", e)
        }
    }

    fun speak(text: String, utteranceId: String = "moto_reply_${System.currentTimeMillis()}") {
        if (!isTtsReady || textToSpeech == null) {
            onErrorLogged("TTS engine not ready")
            return
        }
        // Temporarily pause listening while speaking to avoid echo loop
        stopListening()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        onSpeakingStateChanged(false)
    }

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(100)
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Haptic error", e)
        }
    }

    // Wake word filter implementation
    private fun processTranscript(transcript: String) {
        val lower = transcript.trim().lowercase()
        val primary = AppConfig.PRIMARY_WAKE_WORD.lowercase() // "hey moto"
        val secondary = AppConfig.SECONDARY_WAKE_WORD.lowercase() // "moto"

        val hasPrimary = lower.contains(primary)
        val hasSecondary = lower.contains(secondary)

        if (hasPrimary || hasSecondary) {
            triggerHapticFeedback()

            // Extract the query payload after the wake word
            val cleanQuery = when {
                lower.contains(primary) -> {
                    val index = lower.indexOf(primary)
                    transcript.substring(index + primary.length).trim().removePrefix(",").trim()
                }
                lower.contains(secondary) -> {
                    val index = lower.indexOf(secondary)
                    transcript.substring(index + secondary.length).trim().removePrefix(",").trim()
                }
                else -> transcript.trim()
            }

            onWakeWordDetected(
                if (cleanQuery.isBlank()) "Hello, Moto AI here. How can I assist you?" else cleanQuery,
                transcript
            )
        } else {
            // Ignored ambient or non-wake-word speech
            onIgnoredSpeech(transcript)
            // Continue listening if continuous mode is on
            if (continuousMode) {
                startListening(true)
            }
        }
    }

    // SpeechRecognizer listener methods
    override fun onReadyForSpeech(params: Bundle?) {
        isListening = true
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        // Map dB (-2 to 10 typical) to 0f..1f for dynamic UI visualization
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        onRmsChanged(normalized)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        onRmsChanged(0f)
    }

    override fun onError(error: Int) {
        onRmsChanged(0f)
        isListening = false
        // Re-listen automatically if continuous mode
        if (continuousMode) {
            // brief pause before restarting to avoid rapid error loops
            try {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (continuousMode) {
                        startListening(true)
                    }
                }, 800)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val topResult = matches?.firstOrNull() ?: ""
        if (topResult.isNotBlank()) {
            processTranscript(topResult)
        } else if (continuousMode) {
            startListening(true)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull() ?: ""
        if (partial.isNotBlank()) {
            val lower = partial.trim().lowercase()
            val primary = AppConfig.PRIMARY_WAKE_WORD.lowercase()
            val secondary = AppConfig.SECONDARY_WAKE_WORD.lowercase()
            if (lower.contains(primary) || lower.contains(secondary)) {
                // Wake word detected early in partial results! Provide instant haptic feedback
                triggerHapticFeedback()
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e("VoiceEngine", "Destroy error", e)
        }
    }
}
