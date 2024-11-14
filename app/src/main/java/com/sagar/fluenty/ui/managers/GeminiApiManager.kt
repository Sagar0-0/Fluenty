package com.sagar.fluenty.ui.managers

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

interface GeminiApiChatManager {
    fun initListener(listener: GeminiApiListener)
    suspend fun generateResponse(prompt: String)
}

interface GeminiApiAudioManager {
    fun initListener(listener: GeminiApiListener)
    suspend fun initialResponse(prompt: String)
    suspend fun generateResponseFromAudio(file: File)
}


class GeminiApiAudioManagerImpl(
    model: GenerativeModel
) : GeminiApiAudioManager {

    private var listener: GeminiApiListener? = null

    override fun initListener(listener: GeminiApiListener) {
        this.listener = listener
    }

    private val audioChatHistory = mutableListOf<Content>()
    private val audioChat = model.startChat(audioChatHistory)


    override suspend fun initialResponse(prompt: String) {
        return withContext(Dispatchers.IO) {
            val content = content {
                text(prompt)
            }
            audioChatHistory.add(content)
            try {
                val response = audioChat.sendMessage(content)
                if (response.text != null) {
                    listener?.onResponseGenerated(response.text ?: "")
                } else {
                    listener?.onErrorGeneratingResponse(Exception("Null response"))
                }
            } catch (e: Exception) {
                Log.e("TAG", "getResponse: $e")
                listener?.onErrorGeneratingResponse(
                    Exception("Free tier Limit Exceeded. Try after Sometime.")
                )
            }
        }
    }

    override suspend fun generateResponseFromAudio(file: File) {
        return withContext(Dispatchers.IO) {
            val bytes = readAudioFromAssets(file)
            val content = content {
                bytes?.let { blob("audio/mp3", it) }
                text("Understand the audio and respond accordingly.")
            }
            audioChatHistory.add(content)
            try {
                val response = audioChat.sendMessage(content)
                if (response.text != null) {
                    listener?.onResponseGenerated(response.text ?: "")
                } else {
                    listener?.onErrorGeneratingResponse(Exception("Null response"))
                }
            } catch (e: Exception) {
                Log.e("TAG", "getResponse: $e")
                listener?.onErrorGeneratingResponse(Exception("Free tier Limit Exceeded. Try after Sometime."))
            }
        }
    }

    private fun readAudioFromAssets(file: File): ByteArray? {
        return try {
            val inputStream = file.inputStream()
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            buffer
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}

class GeminiApiChatManagerImpl(
    model: GenerativeModel
) : GeminiApiChatManager {
    private var listener: GeminiApiListener? = null

    override fun initListener(listener: GeminiApiListener) {
        this.listener = listener
    }

    private val chatHistory = mutableListOf<Content>()
    private val chat = model.startChat(chatHistory)

    override suspend fun generateResponse(prompt: String) {
        return withContext(Dispatchers.IO) {
            val content = content {
                text(prompt)
            }
            chatHistory.add(content)
            try {
                val response = chat.sendMessage(content)
                if (response.text != null) {
                    listener?.onResponseGenerated(response.text ?: "")
                } else {
                    listener?.onErrorGeneratingResponse(Exception("Null response"))
                }
            } catch (e: Exception) {
                Log.e("TAG", "getResponse: $e")
                listener?.onErrorGeneratingResponse(
                    Exception("Free tier Limit Exceeded. Try after Sometime.")
                )
            }
        }
    }
}

interface GeminiApiListener {
    fun onResponseGenerated(response: String)
    fun onErrorGeneratingResponse(e: Exception)
}