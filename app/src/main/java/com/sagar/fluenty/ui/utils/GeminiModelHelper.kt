package com.sagar.fluenty.ui.utils

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.sagar.fluenty.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object GeminiModelHelper {
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

    private val chatHistory = mutableListOf<Content>()
    private val chat = model.startChat(chatHistory)

    suspend fun getResponse(prompt: String): String? {
        return withContext(Dispatchers.IO) {
            chatHistory.add(
                content {
                    text(prompt)
                }
            )
            val response = chat.sendMessage(prompt)
            response.text
        }
    }

    suspend fun readFromAudioFile(context: Context, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            val bytes = readAudioFromAssets(context,fileName)
            val content = content {
                bytes?.let { blob("audio/mp3",it) }
            }
            chatHistory.add(
                content
            )
            val response = model.generateContent(content)
            response.text
        }
    }

    private fun readAudioFromAssets(context: Context, fileName: String): ByteArray? {
        return try {
            val inputStream = context.assets.open(fileName)
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