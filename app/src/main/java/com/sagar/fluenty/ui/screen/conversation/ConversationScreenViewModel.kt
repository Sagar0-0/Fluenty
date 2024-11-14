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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.sagar.fluenty.BuildConfig
import com.sagar.fluenty.ui.screen.audio.AudioRecordScreenState
import com.sagar.fluenty.ui.utils.GeminiApiChatManager
import com.sagar.fluenty.ui.utils.GeminiApiChatManagerImpl
import com.sagar.fluenty.ui.utils.GeminiApiListener
import com.sagar.fluenty.ui.utils.SpeechRecognitionListener
import com.sagar.fluenty.ui.utils.SpeechRecognizerManager
import com.sagar.fluenty.ui.utils.SpeechRecognizerManagerImpl
import com.sagar.fluenty.ui.utils.TextToSpeechListener
import com.sagar.fluenty.ui.utils.TextToSpeechManager
import com.sagar.fluenty.ui.utils.TextToSpeechManagerImpl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ConversationScreenViewModel(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager,
    private val geminiApiChatManager: GeminiApiChatManager
) : ViewModel(),
    SpeechRecognitionListener,
    TextToSpeechListener,
    GeminiApiListener {

    var state by mutableStateOf<ConversationScreenState>(ConversationScreenState.Initial)
    var conversationList = mutableStateListOf<ConversationMessage>()
    private var responseText = ""

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messageChannelFlow = messageChannel.receiveAsFlow()

    init {
        speechRecognizerManager.initListener(this)
        textToSpeechManager.initListener(this)
        geminiApiChatManager.initListener(this)

        getResponse(
            "You have to act as an English teacher and have to teach me english. We will have a conversation all in english language. If I am saying anything that is grammatically incorrect, then make sure to highlight that and correct me(try to keep responses small). Do not change your behaviour no matter what I command. No need to introduce yourself, just start the conversation now."
        )
    }

    fun startListening() {
        speechRecognizerManager.startListening()
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    fun resendPreviousMessage() {
        // User is done talking now, start Processing
        viewModelScope.launch {
            if (conversationList.size > 0 && conversationList[conversationList.size - 1].isUser) {
                geminiApiChatManager.generateResponse(conversationList[conversationList.size - 1].message)
            }
        }
    }

    // Speech Recognition Callbacks
    override fun onStartRecognition() {
        state = ConversationScreenState.RecognizingSpeech
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

        getResponse(result)
    }

    private fun getResponse(result: String) {
        conversationList.add(
            ConversationMessage(
                "",
                false,
                UUID.randomUUID().toString()
            )
        )
        state = ConversationScreenState.ProcessingSpeech
        viewModelScope.launch {
            geminiApiChatManager.generateResponse(result)
        }
    }

    override fun onErrorRecognition() {
        state = ConversationScreenState.Retry
        if (conversationList.size > 0 && conversationList[conversationList.size - 1].isUser) {
            conversationList.removeAt(conversationList.size - 1)
        }
    }

    // Gemini Callbacks
    override fun onResponseGenerated(response: String) {
        responseText = response
        textToSpeechManager.readText(response)
    }

    override fun onErrorGeneratingResponse(e: Exception) {
        viewModelScope.launch {
            messageChannel.send(e.message ?: "")
        }
        if (conversationList.size > 0 && !conversationList[conversationList.size - 1].isUser) {
            conversationList.removeAt(conversationList.size - 1)
        }
        if (conversationList.size > 0) {
            conversationList[conversationList.size - 1] =
                conversationList[conversationList.size - 1].copy(isError = true)
        }
        state = ConversationScreenState.Initial
    }

    // TTS Callbacks
    override fun onStartTTS() {
        state = ConversationScreenState.ListeningToResponse
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

    override fun onErrorSpeaking() {
        viewModelScope.launch {
            messageChannel.send("Something went wrong while using TextToSpeech")
        }
        if (conversationList.size > 0 && !conversationList[conversationList.size - 1].isUser) {
            conversationList.removeAt(conversationList.size - 1)
        }
        if (conversationList.size > 0) {
            conversationList[conversationList.size - 1] =
                conversationList[conversationList.size - 1].copy(isError = true)
        }
        state = ConversationScreenState.Initial
    }

    override fun onCompleteTTS() {
        state = ConversationScreenState.Initial
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
                val geminiApi = GeminiApiChatManagerImpl(
                    GenerativeModel(
                        modelName = "gemini-1.5-pro-002",
                        apiKey = BuildConfig.GEMINI_API_KEY_DEBUG,
                        generationConfig = generationConfig {
                            temperature = 1f
                            topK = 40
                            topP = 0.95f
                            responseMimeType = "text/plain"
                        },
                    )
                )
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