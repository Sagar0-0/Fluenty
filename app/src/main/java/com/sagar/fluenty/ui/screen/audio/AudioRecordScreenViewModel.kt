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
import com.sagar.fluenty.ui.utils.AudioPlayerHelper
import com.sagar.fluenty.ui.utils.AudioRecorderHelper
import com.sagar.fluenty.ui.utils.GeminiModelHelper
import com.sagar.fluenty.ui.utils.TextToSpeechHelper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AudioRecordScreenViewModel(
    private val textToSpeechHelper: TextToSpeechHelper,
    private val geminiModelHelper: GeminiModelHelper,
    private val audioRecorderHelper: AudioRecorderHelper,
    private val audioPlayerHelper: AudioPlayerHelper,
) : ViewModel(),
    TextToSpeechHelper.Listener,
    GeminiModelHelper.Listener,
    AudioRecorderHelper.Listener,
    AudioPlayerHelper.Listener {

    var audioRecordScreenState by mutableStateOf<AudioRecordScreenState>(AudioRecordScreenState.Initial)
    var conversationList = mutableStateListOf<ConversationMessage>()
    private var responseText = ""

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messageChannelFlow = messageChannel.receiveAsFlow()

    init {
        textToSpeechHelper.initListener(this)
        geminiModelHelper.initListener(this)
        audioRecorderHelper.initListener(this)
        audioPlayerHelper.initListener(this)
    }

    private fun disablePreviousMessageEditing() {
        var index = conversationList.size - 1
        while (index > 0 || !conversationList[index].isUser) {
            index--
        }
        conversationList[index] =
            conversationList[index].copy(isEditingEnabled = false, isError = false)
    }

    fun resendPreviousMessage() {
        // User is done talking now, start Processing
        audioRecordScreenState = AudioRecordScreenState.ProcessingSpeech
        viewModelScope.launch {
            if (conversationList.size > 0) {
                geminiModelHelper.getResponse(conversationList[conversationList.size - 1].message)
            }
        }
    }

    fun editPreviousMessage() {

    }

    fun getResponseFromAudioFile(context: Context) {
        viewModelScope.launch {
            geminiModelHelper.getResponseFromAudioFile(context, "audio.mp3")
        }
    }


    // Gemini Callbacks
    override fun onResponseGenerated(response: String) {
        responseText = response
        textToSpeechHelper.readText(response)
    }

    override fun onErrorOccurred(e: Exception) {
        viewModelScope.launch {
            messageChannel.send(e.message ?: "")
        }
        if (conversationList.size > 0) {
            conversationList[conversationList.size - 1] =
                conversationList[conversationList.size - 1].copy(isError = true)
        }
        audioRecordScreenState = AudioRecordScreenState.Initial
    }

    // TTS Callbacks
    override fun onStartTTS() {
        audioRecordScreenState = AudioRecordScreenState.ListeningToResponse
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
        audioRecordScreenState = AudioRecordScreenState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorderHelper.destroy()
        audioPlayerHelper.stopRecording()
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val textToSpeechHelper = TextToSpeechHelper(context)
                return AudioRecordScreenViewModel(
                    textToSpeechHelper, GeminiModelHelper, AudioRecorderHelper, AudioPlayerHelper
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