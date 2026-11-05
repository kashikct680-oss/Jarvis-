package com.kishore.jarvis.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechOutputManager(
    context: Context
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS

        if (isReady) {
            setLanguage("en")
        }
    }

    fun setLanguage(language: String) {
        val locale = when (language.lowercase()) {
            "hi", "hindi" -> Locale("hi", "IN")
            else -> Locale.US
        }

        textToSpeech?.language = locale
    }

    fun speak(
        message: String,
        language: String = "en"
    ) {
        if (!isReady || message.isBlank()) return

        setLanguage(language)

        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "JARVIS_RESPONSE"
        )
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
    }
}
