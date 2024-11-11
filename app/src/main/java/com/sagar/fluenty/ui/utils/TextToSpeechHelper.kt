package com.sagar.fluenty.ui.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechHelper(
    context: Context
) {
    private val textToSpeech = TextToSpeech(
        context
    ) { status ->
        if (status == TextToSpeech.SUCCESS) {
            setLanguage()
            setListener()
        }
    }

    private var listener: Listener? = null
    private var currentText: String = ""

    fun addListener(listener: Listener) {
        this.listener = listener
    }

    private fun setListener() {
        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    listener?.onStartTTS()
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    listener?.onSpeaking(currentText.substring(start, end))
                }

                override fun onDone(utteranceId: String?) {
                    listener?.onCompleteTTS()
                }

                override fun onError(utteranceId: String?) {
                    listener?.onErrorSpeaking()
                }
            }
        )
    }

    private fun setLanguage() {
        textToSpeech.setLanguage(Locale.ENGLISH)
    }

    fun readText(text: String) {
        currentText = text
        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_ADD,
            null,
            TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
        )
    }


    interface Listener {
        fun onStartTTS()
        fun onSpeaking(text: String)
        fun onCompleteTTS()
        fun onErrorSpeaking() {}
    }

}