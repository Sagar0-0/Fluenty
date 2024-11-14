package com.sagar.fluenty.ui.screen.conversation

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
import com.sagar.fluenty.ui.utils.SpeechRecognizerManager
import com.sagar.fluenty.ui.utils.SpeechRecognizerManagerImpl
import com.sagar.fluenty.ui.utils.TextToSpeechManager
import com.sagar.fluenty.ui.utils.TextToSpeechManagerImpl
import com.sagar.fluenty.ui.utils.GeminiApiManager
import com.sagar.fluenty.ui.utils.GeminiApiManagerImpl
import com.sagar.fluenty.ui.utils.GeminiApiListener
import com.sagar.fluenty.ui.utils.SpeechRecognitionListener
import com.sagar.fluenty.ui.utils.TextToSpeechListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ConversationScreenViewModel(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager,
    private val geminiApiManager: GeminiApiManager
) : ViewModel(),
    SpeechRecognitionListener,
    TextToSpeechListener,
    GeminiApiListener {

    var currentState by mutableStateOf<ConversationScreenState>(ConversationScreenState.Initial)
    var conversationList = mutableStateListOf<ConversationMessage>()
    private var responseText = ""

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messageChannelFlow = messageChannel.receiveAsFlow()

    init {
        speechRecognizerManager.initListener(this)
        textToSpeechManager.initListener(this)
        geminiApiManager.initListener(this)
    }

    fun startListening() {
        speechRecognizerManager.startListening()
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    fun resendPreviousMessage() {
        // User is done talking now, start Processing
        currentState = ConversationScreenState.ProcessingSpeech
        viewModelScope.launch {
            if (conversationList.size > 0 && conversationList[conversationList.size - 1].isUser) {
                geminiApiManager.generateResponse(conversationList[conversationList.size - 1].message)
            }
        }
    }

    // Speech Recognition Callbacks
    override fun onStartRecognition() {
        currentState = ConversationScreenState.RecognizingSpeech
        conversationList.add(ConversationMessage("", true, UUID.randomUUID().toString()))
    }

    override fun onPartialRecognition(currentResult: String) {
        if (conversationList.size > 0) {
            val lastItem = conversationList[conversationList.size - 1]
            conversationList[conversationList.size - 1] = lastItem.copy(message = currentResult)
        }
    }

    override fun onCompleteRecognition(result: String) {
        if (conversationList.size > 0) {
            val lastItem = conversationList[conversationList.size - 1]
            conversationList[conversationList.size - 1] =
                lastItem.copy(message = result, isError = false)
        }

        // User is done talking now, start Processing
        currentState = ConversationScreenState.ProcessingSpeech
        viewModelScope.launch {
            geminiApiManager.generateResponse(result)
        }
    }

    override fun onErrorRecognition() {
        currentState = ConversationScreenState.Retry
        if (conversationList.size > 0 && conversationList[conversationList.size - 1].isUser) {
            conversationList.removeAt(conversationList.size - 1)
        }
    }

    // Gemini Callbacks
    override fun onResponseGenerated(response: String) {
        responseText = response
        textToSpeechManager.readText(response)
    }

    override fun onErrorOccurred(e: Exception) {
        viewModelScope.launch {
            messageChannel.send(e.message ?: "")
        }
        if (conversationList.size > 0) {
            conversationList[conversationList.size - 1] =
                conversationList[conversationList.size - 1].copy(isError = true)
        }
        currentState = ConversationScreenState.Initial
    }

    // TTS Callbacks
    override fun onStartTTS() {
        currentState = ConversationScreenState.ListeningToResponse
        conversationList.add(ConversationMessage("", false, UUID.randomUUID().toString()))
    }

    override fun onSpeaking(text: String) {
        Log.e("TAG", "onSpeaking: Current Spoken $text")
        if (conversationList.size > 0) {
            val lastItem = conversationList[conversationList.size - 1]
            conversationList[conversationList.size - 1] =
                lastItem.copy(message = lastItem.message + showResponseForTTSRead(text))
        }
    }

    private fun showResponseForTTSRead(target: String): String {
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

    override fun onCompleteTTS() {
        currentState = ConversationScreenState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerManager.destroyRecognizer()
        textToSpeechManager.destroy()
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appSpeechRecognizer = SpeechRecognizerManagerImpl(context)
                val appTextToSpeech = TextToSpeechManagerImpl(context)
                val geminiApi = GeminiApiManagerImpl
                return ConversationScreenViewModel(
                    appSpeechRecognizer, appTextToSpeech, geminiApi
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
    val message: String,
    val isUser: Boolean,
    val id: String,
    val isError: Boolean = false
)