package com.sagar.fluenty.ui.screen.audio

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
import com.sagar.fluenty.ui.utils.TextToSpeechManager
import com.sagar.fluenty.ui.utils.TextToSpeechManagerImpl
import com.sagar.fluenty.ui.utils.AudioPlayerManager
import com.sagar.fluenty.ui.utils.AudioPlayerManagerImpl
import com.sagar.fluenty.ui.utils.AudioPlayerListener
import com.sagar.fluenty.ui.utils.AudioRecorderManager
import com.sagar.fluenty.ui.utils.AudioRecorderManagerImpl
import com.sagar.fluenty.ui.utils.AudioRecorderListener
import com.sagar.fluenty.ui.utils.GeminiApiManager
import com.sagar.fluenty.ui.utils.GeminiApiManagerImpl
import com.sagar.fluenty.ui.utils.GeminiApiListener
import com.sagar.fluenty.ui.utils.TextToSpeechListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AudioRecordScreenViewModel(
    private val textToSpeechManager: TextToSpeechManager,
    private val geminiApiManager: GeminiApiManager,
    private val audioRecorderManager: AudioRecorderManager,
    private val audioPlayerManager: AudioPlayerManager
) : ViewModel(),
    TextToSpeechListener,
    GeminiApiListener,
    AudioRecorderListener,
    AudioPlayerListener {

    var screenState by mutableStateOf<AudioRecordScreenState>(AudioRecordScreenState.Initial)
    var conversationList = mutableStateListOf<ConversationMessage>()
    private var responseText = ""

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messageChannelFlow = messageChannel.receiveAsFlow()

    init {
        textToSpeechManager.initListener(this)
        geminiApiManager.initListener(this)
        audioRecorderManager.initListener(this)
        audioPlayerManager.initListener(this)
    }

    fun getResponseFromAudioFile(context: Context) {
        viewModelScope.launch {

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
        screenState = AudioRecordScreenState.Initial
    }

    // TTS Callbacks
    override fun onStartTTS() {
        screenState = AudioRecordScreenState.ListeningToResponse
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
        screenState = AudioRecordScreenState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeechManager.destroy()
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appTextToSpeech = TextToSpeechManagerImpl(context)
                val geminiApi = GeminiApiManagerImpl
                val audioRecorder = AudioRecorderManagerImpl(context)
                val audioPlayer = AudioPlayerManagerImpl(context)
                return AudioRecordScreenViewModel(
                    textToSpeechManager = appTextToSpeech,
                    geminiApiManager = geminiApi,
                    audioRecorderManager = audioRecorder,
                    audioPlayerManager = audioPlayer
                ) as T
            }
        }
    }

    override fun onPlayerStarted() {
        TODO("Not yet implemented")
    }

    override fun onErrorPlaying() {
        TODO("Not yet implemented")
    }

    override fun onStopPlayer() {
        TODO("Not yet implemented")
    }

    override fun onRecordingStarted() {
        TODO("Not yet implemented")
    }

    override fun onErrorStarting() {
        TODO("Not yet implemented")
    }
}

interface AudioRecordScreenState {
    data object Initial : AudioRecordScreenState
    data object Retry : AudioRecordScreenState
    data object ProcessingSpeech : AudioRecordScreenState
    data object RecognizingSpeech : AudioRecordScreenState
    data object ListeningToResponse : AudioRecordScreenState
}

data class ConversationMessage(
    val message: String,
    val isUser: Boolean,
    val id: String,
    val isEditingEnabled: Boolean = false,
    val isError: Boolean = false
)