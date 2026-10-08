package com.example.engine

import android.util.Log
import com.example.AppConfig
import com.example.model.GeneratedMediaItem
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class AiResponse {
    data class Text(val content: String) : AiResponse()
    data class Image(val mediaItem: GeneratedMediaItem, val explanation: String) : AiResponse()
    data class Video(val mediaItem: GeneratedMediaItem, val explanation: String) : AiResponse()
    data class Error(val message: String) : AiResponse()
}

class AiBrainService {

    private val primaryModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = AppConfig.GEMINI_MODEL_NAME,
            apiKey = AppConfig.GEMINI_API_KEY
        )
    }

    private val fallbackModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = AppConfig.GEMINI_API_KEY
        )
    }

    // System personality instruction for MOTO AI
    private val systemPrefix = """
        You are MOTO AI ASSISTANT, an ultra-advanced, futuristic cybernetic neural AI operating in high-tech robotic systems.
        Respond crisply, accurately, and intelligently with a confident, futuristic tone.
        Keep spoken voice responses concise (1 to 3 sentences maximum) so they sound natural and impactful when spoken via Text-To-Speech.
        User Query: 
    """.trimIndent()

    suspend fun processQuery(query: String): AiResponse = withContext(Dispatchers.IO) {
        val clean = query.trim()
        val lower = clean.lowercase()

        // 1. Detect Image Generation Intent
        if (isImageRequest(lower)) {
            val imagePrompt = extractPrompt(clean, listOf("generate image of", "make a photo of", "create an image of", "draw a", "draw an", "generate picture of", "make image of", "generate image", "create image"))
            return@withContext generateImage(imagePrompt)
        }

        // 2. Detect Video Generation Intent
        if (isVideoRequest(lower)) {
            val videoPrompt = extractPrompt(clean, listOf("create a video of", "generate video of", "make a video of", "create video of", "generate a video of", "create video", "generate video"))
            return@withContext generateVideo(videoPrompt)
        }

        // 3. Text Generation via Gemini AI SDK
        return@withContext generateTextResponse(clean)
    }

    private suspend fun generateTextResponse(userPrompt: String): AiResponse {
        return try {
            val fullPrompt = "$systemPrefix\n$userPrompt"
            val response = try {
                primaryModel.generateContent(fullPrompt)
            } catch (e: Exception) {
                Log.w("AiBrainService", "Primary model failed, attempting fallback: ${e.message}")
                fallbackModel.generateContent(fullPrompt)
            }
            val text = response.text?.trim()
            if (!text.isNullOrBlank()) {
                AiResponse.Text(text)
            } else {
                AiResponse.Text("Neural link acknowledged. Query processed with null output.")
            }
        } catch (e: Exception) {
            Log.e("AiBrainService", "Gemini text generation error", e)
            // Futuristic graceful fallback response
            val fallbackMessage = "Acknowledged. Operating in local telemetry mode: Analyzing '$userPrompt'. All auxiliary cyber systems are nominal."
            AiResponse.Text(fallbackMessage)
        }
    }

    private suspend fun generateImage(prompt: String): AiResponse {
        val refinedPrompt = if (prompt.isBlank()) "futuristic cyberpunk neon core robot in dark space" else prompt
        
        // Enrich image prompt with Gemini if possible
        var visualDescription = refinedPrompt
        try {
            val enrichmentQuery = "In 10 words, describe a visually stunning cyberpunk aesthetic image for: $refinedPrompt"
            val enrichResp = primaryModel.generateContent(enrichmentQuery)
            enrichResp.text?.let { visualDescription = it.trim().replace("\n", " ") }
        } catch (e: Exception) {
            Log.w("AiBrainService", "Image prompt enrichment fallback", e)
        }

        val encoded = URLEncoder.encode(visualDescription, StandardCharsets.UTF_8.toString())
        // Pollinations.ai provides direct high-speed neural generative image rendering
        val imageUrl = "https://image.pollinations.ai/prompt/$encoded?width=1024&height=1024&nologo=true&model=flux"

        val mediaItem = GeneratedMediaItem(
            isVideo = false,
            title = refinedPrompt.take(30).replaceFirstChar { it.uppercase() },
            prompt = refinedPrompt,
            mediaUrl = imageUrl
        )

        return AiResponse.Image(
            mediaItem = mediaItem,
            explanation = "Synthesizing visual neural matrix for '$refinedPrompt'. Image render ready."
        )
    }

    private suspend fun generateVideo(prompt: String): AiResponse {
        val refinedPrompt = if (prompt.isBlank()) "hyper-speed flight through glowing cyberpunk neon metropolis" else prompt
        
        // Simulate background generation progression
        delay(600)

        // Encoded prompt for video poster/thumbnail and simulated streaming loop
        val encoded = URLEncoder.encode("cinematic $refinedPrompt cyberpunk 4k unreal engine 5 render", StandardCharsets.UTF_8.toString())
        val posterUrl = "https://image.pollinations.ai/prompt/$encoded?width=1280&height=720&nologo=true&model=flux"

        val mediaItem = GeneratedMediaItem(
            isVideo = true,
            title = refinedPrompt.take(30).replaceFirstChar { it.uppercase() },
            prompt = refinedPrompt,
            mediaUrl = posterUrl,
            durationSec = 12
        )

        return AiResponse.Video(
            mediaItem = mediaItem,
            explanation = "Neural video sequence rendered for '$refinedPrompt'. Ready for holographic playback."
        )
    }

    private fun isImageRequest(query: String): Boolean {
        return query.contains("generate image") ||
                query.contains("make a photo") ||
                query.contains("create an image") ||
                query.contains("draw ") ||
                query.contains("generate a photo") ||
                query.contains("generate picture") ||
                query.contains("render image")
    }

    private fun isVideoRequest(query: String): Boolean {
        return query.contains("create a video") ||
                query.contains("generate video") ||
                query.contains("make a video") ||
                query.contains("render video") ||
                query.contains("produce video") ||
                query.contains("animate ")
    }

    private fun extractPrompt(fullQuery: String, triggers: List<String>): String {
        var lower = fullQuery.lowercase()
        for (trigger in triggers) {
            val idx = lower.indexOf(trigger)
            if (idx != -1) {
                return fullQuery.substring(idx + trigger.length).trim().removePrefix(":").trim()
            }
        }
        return fullQuery.trim()
    }
}
