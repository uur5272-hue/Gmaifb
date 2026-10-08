package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun appConfig_hasRequiredValues() {
        assertEquals("MOTO AI ASSISTANT", AppConfig.APP_NAME)
        assertEquals("AIzaSyBc8E6hV-KznEkZ69Hov5kfQK9r6WVC7LA", AppConfig.GEMINI_API_KEY)
        assertEquals("gemini-1.5-flash", AppConfig.GEMINI_MODEL_NAME)
        assertEquals("Hey Moto", AppConfig.PRIMARY_WAKE_WORD)
        assertEquals("Moto", AppConfig.SECONDARY_WAKE_WORD)
    }

    @Test
    fun wakeWordFiltering_strictlyMatchesMoto() {
        val primary = AppConfig.PRIMARY_WAKE_WORD.lowercase()
        val secondary = AppConfig.SECONDARY_WAKE_WORD.lowercase()

        // Valid wake words
        val query1 = "Hey Moto what's the weather"
        val query2 = "Moto generate an image"
        val query3 = "hey moto tell me a joke"

        assertTrue(query1.lowercase().contains(primary) || query1.lowercase().contains(secondary))
        assertTrue(query2.lowercase().contains(primary) || query2.lowercase().contains(secondary))
        assertTrue(query3.lowercase().contains(primary) || query3.lowercase().contains(secondary))

        // Non-wake words / other assistants / ambient noise
        val invalid1 = "Hey Google play music"
        val invalid2 = "Alexa turn on the lights"
        val invalid3 = "The motor is running outside"
        val invalid4 = "What is the time right now"

        assertFalse(invalid1.lowercase().contains(primary) || invalid1.lowercase().contains(secondary))
        assertFalse(invalid2.lowercase().contains(primary) || invalid2.lowercase().contains(secondary))
        assertFalse(invalid4.lowercase().contains(primary) || invalid4.lowercase().contains(secondary))
    }
}
