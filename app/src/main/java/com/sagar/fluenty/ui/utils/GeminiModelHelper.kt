package com.sagar.fluenty.ui.utils

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.sagar.fluenty.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

interface GeminiApiManager {
    fun initListener(listener: GeminiApiListener)
    suspend fun generateResponse(prompt: String)
    suspend fun generateResponseFromAudio(file: File)
}

object GeminiApiManagerImpl : GeminiApiManager {
    private val model =
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

    override suspend fun generateResponseFromAudio(file: File) {
        return withContext(Dispatchers.IO) {
            val bytes = readAudioFromAssets(file)
            val content = content {
                bytes?.let { blob("audio/mp3", it) }
                text("Understand the audio and respond accordingly.")
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

interface GeminiApiListener {
    fun onResponseGenerated(response: String)
    fun onErrorGeneratingResponse(e: Exception)
}