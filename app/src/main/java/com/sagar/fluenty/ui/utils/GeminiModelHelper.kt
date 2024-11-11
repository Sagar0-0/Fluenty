package com.sagar.fluenty.ui.utils

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.sagar.fluenty.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiModelHelper {
    private val model =
        GenerativeModel(
            modelName = "gemini-1.5-pro-002",
            apiKey = BuildConfig.GEMINI_API_KEY_DEBUG,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
        )


    suspend fun getResponse(prompt: String): String? {
        return withContext(Dispatchers.IO) {
            model.generateContent(prompt).text
        }
    }
}