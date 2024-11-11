package com.sagar.fluenty.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.sagar.fluenty.ui.utils.GeminiModelHelper
import com.sagar.fluenty.ui.utils.SpeechRecognizerHelper
import com.sagar.fluenty.ui.utils.TextToSpeechHelper
import kotlinx.coroutines.launch
import java.util.UUID

class ConversationScreenViewModel(
    private val speechRecognizerHelper: SpeechRecognizerHelper,
    private val textToSpeechHelper: TextToSpeechHelper,
    private val geminiModelHelper: GeminiModelHelper
) : ViewModel(), SpeechRecognizerHelper.SpeechRecognitionListener, TextToSpeechHelper.Listener {

    var currentState by mutableStateOf<ConversationScreenState>(ConversationScreenState.Initial)
    var conversationList = mutableStateListOf<ConversationMessage>()
    private var responseText = ""

    init {
        speechRecognizerHelper.setSpeechListener(this)
        textToSpeechHelper.addListener(this)
    }

    // Speech Recognition Methods/Callbacks
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
        val lastItem = conversationList[conversationList.size - 1]
        conversationList[conversationList.size - 1] = lastItem.copy(message = currentResult)
    }

    override fun onErrorOfSpeech() {
        currentState = ConversationScreenState.Retry
        if (conversationList.size > 0 && conversationList[conversationList.size - 1].isUser) {
            conversationList.removeAt(conversationList.size - 1)
        }
    }

    override fun onResults(result: String) {
        val lastItem = conversationList[conversationList.size - 1]
        conversationList[conversationList.size - 1] = lastItem.copy(message = result)

        // User is done talking now, start Processing
        currentState = ConversationScreenState.ProcessingSpeech
        viewModelScope.launch {
            val response = geminiModelHelper.getResponse(result)
            if (response != null) {
                responseText = response
                textToSpeechHelper.readText(response)
            } else {
                responseText = ""
                currentState = ConversationScreenState.Retry
            }
        }
    }


    // Reading Callbacks
    override fun onStartSpeaking() {
        currentState = ConversationScreenState.ListeningToResponse
        conversationList.add(ConversationMessage("", false, UUID.randomUUID().toString()))
    }

    override fun onSpeaking(text: String) {
        Log.e("TAG", "onSpeaking: Current Spoken $text")
        val lastItem = conversationList[conversationList.size - 1]
        conversationList[conversationList.size - 1] = lastItem.copy(message = lastItem.message + showResponseTillRead(text))
    }

    private fun showResponseTillRead(target: String): String {
        val index = responseText.indexOf(target, ignoreCase = true)
        return if (index != -1) {
            // End Index is the end of the target
            var endIndex = index + target.length

            // Check if there's a character after the target
            if (endIndex < responseText.length) {
                // Include the next character if it's a special character
                val nextChar = responseText[endIndex]
                if (!nextChar.isLetterOrDigit() || nextChar == ' ') {
                    endIndex++
                }
                val result = responseText.substring(0, endIndex)
                responseText = responseText.removePrefix(result)
                result
            } else {
                ""
            }
        } else {
            responseText
        }
    }

    override fun onDoneSpeaking() {
        currentState = ConversationScreenState.Initial
    }


    override fun onCleared() {
        super.onCleared()
        speechRecognizerHelper.stopListening()
        speechRecognizerHelper.destroyRecognizer()
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val speechRecognizerHelper = SpeechRecognizerHelper(context)
                val textToSpeechHelper = TextToSpeechHelper(context)
                return ConversationScreenViewModel(
                    speechRecognizerHelper, textToSpeechHelper, GeminiModelHelper
                ) as T
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
    val message: String, val isUser: Boolean, val id: String
)