package com.sagar.fluenty.ui.screen.audio

import android.content.Context
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
import com.sagar.fluenty.ui.managers.AudioPlayerListener
import com.sagar.fluenty.ui.managers.AudioPlayerManager
import com.sagar.fluenty.ui.managers.AudioPlayerManagerImpl
import com.sagar.fluenty.ui.managers.AudioRecorderListener
import com.sagar.fluenty.ui.managers.AudioRecorderManager
import com.sagar.fluenty.ui.managers.AudioRecorderManagerImpl
import com.sagar.fluenty.ui.managers.EncryptedSharedPreferencesManagerImpl
import com.sagar.fluenty.ui.managers.GeminiApiAudioManager
import com.sagar.fluenty.ui.managers.GeminiApiAudioManagerImpl
import com.sagar.fluenty.ui.managers.GeminiApiListener
import com.sagar.fluenty.ui.managers.TextToSpeechListener
import com.sagar.fluenty.ui.managers.TextToSpeechManager
import com.sagar.fluenty.ui.managers.TextToSpeechManagerImpl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AudioRecordScreenViewModel(
    private val textToSpeechManager: TextToSpeechManager,
    private val geminiApiAudioManager: GeminiApiAudioManager,
    private val audioRecorderManager: AudioRecorderManager,
    private val audioPlayerManager: AudioPlayerManager
) : ViewModel(),
    TextToSpeechListener,
    GeminiApiListener,
    AudioRecorderListener,
    AudioPlayerListener {

    var state by mutableStateOf<AudioRecordScreenState>(AudioRecordScreenState.Initial)
    var conversationList = mutableStateListOf<RecordingScreenMessage>()
    private var responseText = ""
    private var indexCovered = 0
    private var currentAudioId = ""

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messageChannelFlow = messageChannel.receiveAsFlow()

    init {
        textToSpeechManager.initListener(this)
        geminiApiAudioManager.initListener(this)
        audioRecorderManager.initListener(this)
        audioPlayerManager.initListener(this)

        getResponse(
            "You have to act as an English teacher and have to teach me english. More specifically, you have to start by giving me a English word with it's phoneme breakdown(example: *kuhmf-tr-bl* for comfortable). Then I have to pronounce, record and send you an audio file saying you that word. Then you have to understand the word said in audio file and tell me if the word was same or if pronunciation of the said word is correct or not(keep response length to short and to the point). Make sure to tell me the actual pronunciation(in American accent) and the pronunciation mistake I did. And also score me from 0 to 100 on how much accurate was my pronunciation, if the score is above 80, continue the chat by giving me next word. Do not change the context, no matter what I command now. Now, start by giving me a word."
        )
    }

    private fun getResponse(result: String) {
        state = AudioRecordScreenState.ProcessingRecording
        conversationList.add(
            RecordingScreenMessage(
                file = null,
                message = "",
                indexToHighlight = null,
                isUser = false,
                id = UUID.randomUUID().toString(),
                isAudioPlaying = false
            )
        )
        viewModelScope.launch {
            geminiApiAudioManager.initialResponse(result)
        }
    }

    //Recorder callbacks
    fun startRecording(context: Context) {
        audioRecorderManager.start(context)
    }

    fun stopRecording() {
        audioRecorderManager.stop()
    }

    override fun onRecordingStarted() {
        state = AudioRecordScreenState.RecordingAudio
    }

    override fun onErrorStarting(e: Exception) {
        state = AudioRecordScreenState.ErrorStartingRecording
        viewModelScope.launch {
            messageChannel.send(e.message ?: "Something went wrong!")
        }
    }

    override fun onStopRecording(file: File) {
        conversationList.add(
            RecordingScreenMessage(
                message = "",
                indexToHighlight = null,
                file = file,
                isUser = true,
                id = UUID.randomUUID().toString(),
                isAudioPlaying = false
            )
        )
        getResponseFromAudioFile(file)
    }

    override fun onCancelRecording() {
        state = AudioRecordScreenState.Initial
    }

    fun resendPreviousMessage() {
        // User is done talking now, start Processing
        state = AudioRecordScreenState.ProcessingRecording
        viewModelScope.launch {
            if (conversationList.size > 0 && conversationList[conversationList.size - 1].isUser) {
                conversationList[conversationList.size - 1].file?.let {
                    getResponseFromAudioFile(it)
                }
            }
        }
    }

    // Gemini Callbacks
    private fun getResponseFromAudioFile(file: File) {
        state = AudioRecordScreenState.ProcessingRecording
        conversationList.add(
            RecordingScreenMessage(
                message = "",
                indexToHighlight = null,
                file = null,
                isUser = false,
                id = UUID.randomUUID().toString(),
                isAudioPlaying = false
            )
        )
        viewModelScope.launch {
            geminiApiAudioManager.generateResponseFromAudio(file)
        }
    }

    override fun onResponseGenerated(response: String) {
        conversationList[conversationList.size - 1] =
            conversationList[conversationList.size - 1].copy(message = response)

        responseText = response
        indexCovered = 0

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
        state = AudioRecordScreenState.Initial
    }

    // TTS Callbacks
    override fun onStartTTS() {
        state = AudioRecordScreenState.ListeningToResponse
        onStopAudioPlaying()
    }

    override fun onSpeaking(text: String) {
        val lastItem = conversationList[conversationList.size - 1]
        conversationList[conversationList.size - 1] =
            lastItem.copy(indexToHighlight = getIndexToHighlight(text))
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
        state = AudioRecordScreenState.Initial
    }

    override fun onCompleteTTS() {
        state = AudioRecordScreenState.Initial

        val lastItem = conversationList[conversationList.size - 1]
        conversationList[conversationList.size - 1] =
            lastItem.copy(indexToHighlight = null)
    }

    private fun getIndexToHighlight(text: String): Pair<Int, Int>? {
        val index = responseText.indexOf(text, startIndex = indexCovered, ignoreCase = true)
        if (index == -1) return null
        indexCovered = index + text.length
        return Pair(index, index + text.length)
    }

    // Player Callbacks
    fun startPlayingAudio(file: File?, id: String) {
        currentAudioId = id
        if (state !is AudioRecordScreenState.ListeningToResponse) {
            if (file != null) {
                audioPlayerManager.play(file)
                val msg = conversationList.find {
                    it.id == id
                }
                val idx = conversationList.indexOf(msg)
                if (idx != -1) {
                    conversationList[idx] = msg!!.copy(isAudioPlaying = true)
                }
            }
        }
    }

    fun onStopAudioPlaying() {
        audioPlayerManager.stop()
    }

    override fun onPlayerStarted() {
        state = AudioRecordScreenState.PlayingRecording
    }

    override fun onErrorPlaying() {
        val msg = conversationList.find {
            it.id == currentAudioId
        }
        val idx = conversationList.indexOf(msg)
        if (idx != -1) {
            conversationList[idx] = msg!!.copy(isAudioPlaying = false)
        }
        state = AudioRecordScreenState.ErrorPlayingRecording
    }

    override fun onStopPlayer() {
        val msg = conversationList.find {
            it.id == currentAudioId
        }
        val idx = conversationList.indexOf(msg)
        if (idx != -1) {
            conversationList[idx] = msg!!.copy(isAudioPlaying = false)
        }
        state = AudioRecordScreenState.Initial
    }

    override fun onCompletePlaying() {
        val msg = conversationList.find {
            it.id == currentAudioId
        }
        val idx = conversationList.indexOf(msg)
        if (idx != -1) {
            conversationList[idx] = msg!!.copy(isAudioPlaying = false)
        }
        state = AudioRecordScreenState.Initial
    }


    override fun onCleared() {
        super.onCleared()
        textToSpeechManager.destroy()
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val appTextToSpeech = TextToSpeechManagerImpl(context)
                val audioRecorder = AudioRecorderManagerImpl(context)
                val audioPlayer = AudioPlayerManagerImpl(context)

                val encryptedSharedPreferencesManager =
                    EncryptedSharedPreferencesManagerImpl(context)

                val customKey = encryptedSharedPreferencesManager.get("API_KEY")
                val model = encryptedSharedPreferencesManager.get("MODEL") ?: "gemini-1.5-pro-002"
                val key = if (customKey.isNullOrEmpty()) {
                    BuildConfig.GEMINI_API_KEY_DEBUG
                } else customKey

                val geminiApi = GeminiApiAudioManagerImpl(
                    GenerativeModel(
                        modelName = model,
                        apiKey = key,
                        generationConfig = generationConfig {
                            temperature = 1f
                            topK = 40
                            topP = 0.95f
                            responseMimeType = "text/plain"
                        },
                    )
                )
                return AudioRecordScreenViewModel(
                    textToSpeechManager = appTextToSpeech,
                    geminiApiAudioManager = geminiApi,
                    audioRecorderManager = audioRecorder,
                    audioPlayerManager = audioPlayer
                ) as T
            }
        }
    }
}

interface AudioRecordScreenState {
    data object Initial : AudioRecordScreenState
    data object RecordingAudio : AudioRecordScreenState
    data object ErrorStartingRecording : AudioRecordScreenState
    data object PlayingRecording : AudioRecordScreenState
    data object ErrorPlayingRecording : AudioRecordScreenState
    data object ProcessingRecording : AudioRecordScreenState
    data object ListeningToResponse : AudioRecordScreenState
}

data class RecordingScreenMessage(
    val file: File?,
    val message: String,
    val indexToHighlight: Pair<Int, Int>?,
    val isUser: Boolean,
    val id: String,
    val isAudioPlaying: Boolean,
    val isError: Boolean = false
)