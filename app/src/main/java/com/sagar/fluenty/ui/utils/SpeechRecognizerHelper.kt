package com.sagar.fluenty.ui.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognizerHelper(
    context: Context
) {
    private var speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    private var listener: SpeechRecognitionListener? = null

    fun setSpeechListener(listener1: SpeechRecognitionListener) {
        this.listener = listener1
        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle) {
                    listener?.onStartListening()
                }
                override fun onBeginningOfSpeech() {

                }
                override fun onRmsChanged(rmsdB: Float) {

                }
                override fun onBufferReceived(buffer: ByteArray) {

                }

                override fun onEndOfSpeech() {
                    listener?.onEndOfSpeech()
                }

                override fun onError(error: Int) {
                    listener?.onErrorOfSpeech()
                }

                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.size > 0) {
                        val command = matches[0]
                        listener?.onResults(command)
                    }
                }

                override fun onPartialResults(partialResults: Bundle) {
                    val matches =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.size > 0) {
                        val partialText = matches[0]
                        listener?.onPartialResults(partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle) {}
            }
        )
    }

    fun startListening() {
        speechRecognizer.startListening(createIntent())
    }

    fun stopListening() {
        speechRecognizer.cancel()
    }

    fun destroyRecognizer() {
        speechRecognizer.destroy()
        listener = null
    }

    private fun createIntent(): Intent {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        return i
    }

    interface SpeechRecognitionListener {
        fun onStartListening() {}
        fun onEndOfSpeech() {}
        fun onErrorOfSpeech() {}
        fun onPartialResults(currentResult: String)
        fun onResults(result: String)
    }

}