package com.sagar.fluenty.ui.screen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.sagar.fluenty.ui.utils.SpeechRecognizerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class ConversationScreenViewModel(
    private val speechRecognizerHelper: SpeechRecognizerHelper
) : ViewModel(), SpeechRecognizerHelper.SpeechRecognitionListener {

    var currentState by mutableStateOf<ConversationScreenState>(ConversationScreenState.Initial)
    var conversationList = mutableStateListOf<ConversationMessage>()

    init {
        speechRecognizerHelper.setSpeechListener(this)
    }

    fun startListening() {
        speechRecognizerHelper.startListening()
    }

    fun stopListening() {
        speechRecognizerHelper.stopListening()
    }

    override fun onStartListening() {
        currentState = ConversationScreenState.RecognizingSpeech("")
    }

    override fun onPartialResults(currentResult: String) {
        currentState = ConversationScreenState.RecognizingSpeech(currentResult)
    }

    override fun onErrorOfSpeech() {
        currentState = ConversationScreenState.Retry
    }

    override fun onResults(result: String) {
        conversationList.add(ConversationMessage(result, true, UUID.randomUUID().toString()))
        currentState = ConversationScreenState.ProcessingSpeech
        viewModelScope.launch {
            delay(5000) // Fake Processing
            currentState = ConversationScreenState.Initial
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.stopListening()
        speechRecognizerHelper.destroyRecognizer()
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val inferenceModel = SpeechRecognizerHelper(context)
                return ConversationScreenViewModel(inferenceModel) as T
            }
        }
    }

}

interface ConversationScreenState {
    data object Initial : ConversationScreenState
    data object Retry : ConversationScreenState
    data object ProcessingSpeech : ConversationScreenState
    data class RecognizingSpeech(val text: String) : ConversationScreenState
    data class ListeningToResponse(val text: String) : ConversationScreenState
}

data class ConversationMessage(
    val message: String,
    val isUser: Boolean,
    val id: String
)