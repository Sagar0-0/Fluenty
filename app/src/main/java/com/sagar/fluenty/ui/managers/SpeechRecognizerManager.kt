package com.sagar.fluenty.ui.managers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

interface SpeechRecognizerManager {
    fun initListener(listener: SpeechRecognitionListener)
    fun startListening()
    fun stopListening()
    fun destroyRecognizer()
}

class SpeechRecognizerManagerImpl(
    context: Context
) : SpeechRecognizerManager {
    private var speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private var listener: SpeechRecognitionListener? = null

    override fun initListener(listener: SpeechRecognitionListener) {
        this.listener = listener
        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle) {
                    listener.onStartRecognition()
                }

                override fun onBeginningOfSpeech() {
                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray) {

                }

                override fun onEndOfSpeech() {
                }

                override fun onError(error: Int) {
                    listener.onErrorRecognition()
                }

                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.size > 0) {
                        val command = matches[0]
                        listener.onCompleteRecognition(command)
                    }
                }

                override fun onPartialResults(partialResults: Bundle) {
                    val matches =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.size > 0) {
                        val partialText = matches[0]
                        listener.onPartialRecognition(partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle) {}
            }
        )
    }

    override fun startListening() {
        speechRecognizer.startListening(createIntent())
    }

    override fun stopListening() {
        speechRecognizer.cancel()
    }

    override fun destroyRecognizer() {
        speechRecognizer.stopListening()
        speechRecognizer.destroy()
    }

    private fun createIntent(): Intent {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        return i
    }

}

interface SpeechRecognitionListener {
    fun onStartRecognition() {}
    fun onErrorRecognition() {}
    fun onPartialRecognition(currentResult: String)
    fun onCompleteRecognition(result: String)
}