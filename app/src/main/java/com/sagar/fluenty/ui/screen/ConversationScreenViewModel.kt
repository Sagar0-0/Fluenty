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
import com.sagar.fluenty.ui.utils.TextToSpeechHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class ConversationScreenViewModel(
    private val speechRecognizerHelper: SpeechRecognizerHelper,
    private val textToSpeechHelper: TextToSpeechHelper,
) : ViewModel(), SpeechRecognizerHelper.SpeechRecognitionListener, TextToSpeechHelper.Listener {

    var currentState by mutableStateOf<ConversationScreenState>(ConversationScreenState.Initial)
    var conversationList = mutableStateListOf<ConversationMessage>()

    init {
        speechRecognizerHelper.setSpeechListener(this)
        textToSpeechHelper.addListener(this)
    }

    fun startListening() {
        speechRecognizerHelper.startListening()
    }

    fun stopListening() {
        speechRecognizerHelper.stopListening()
    }

    override fun onStartListening() {
        currentState = ConversationScreenState.RecognizingSpeech
        conversationList.add(ConversationMessage("", true, UUID.randomUUID().toString()))
    }

    override fun onPartialResults(currentResult: String) {
        val lastItem = conversationList[conversationList.size-1]
        conversationList[conversationList.size - 1] = lastItem.copy(message = currentResult)
    }

    override fun onErrorOfSpeech() {
        currentState = ConversationScreenState.Retry
        if(conversationList.size>0 && conversationList[conversationList.size-1].isUser) {
            conversationList.removeAt(conversationList.size-1)
        }
    }

    override fun onResults(result: String) {
        val lastItem = conversationList[conversationList.size-1]
        conversationList[conversationList.size - 1] = lastItem.copy(message = result)

        // User is done talking now, start response
        currentState = ConversationScreenState.ProcessingSpeech
        textToSpeechHelper.readText(result)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.stopListening()
        speechRecognizerHelper.destroyRecognizer()
    }

    override fun onStartSpeaking() {
        currentState = ConversationScreenState.ListeningToResponse
        conversationList.add(ConversationMessage("",false,UUID.randomUUID().toString()))
    }

    override fun onSpeaking(text: String) {
        val lastItem = conversationList[conversationList.size-1]
        conversationList[conversationList.size - 1] = lastItem.copy(message = lastItem.message + " " + text)
    }

    override fun onDoneSpeaking() {
        currentState = ConversationScreenState.Initial
    }


    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val speechRecognizerHelper = SpeechRecognizerHelper(context)
                val textToSpeechHelper = TextToSpeechHelper(context)
                return ConversationScreenViewModel(speechRecognizerHelper, textToSpeechHelper) as T
            }
        }
    }
}

interface ConversationScreenState {
    data object Initial : ConversationScreenState
    data object Retry : ConversationScreenState
    data object ProcessingSpeech : ConversationScreenState
    data object RecognizingSpeech : ConversationScreenState
    data object ListeningToResponse : ConversationScreenState
}

data class ConversationMessage(
    val message: String,
    val isUser: Boolean,
    val id: String
)