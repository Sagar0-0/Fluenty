package com.sagar.fluenty.ui.screen.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sagar.fluenty.ui.managers.EncryptedSharedPreferencesManager
import com.sagar.fluenty.ui.managers.EncryptedSharedPreferencesManagerImpl

class SettingsScreenViewModel(
    private val encryptedSharedPreferencesManager: EncryptedSharedPreferencesManager
) : ViewModel() {

    val availableModels = listOf(
        "gemini-1.5-pro-002",
        "gemini-1.5-pro",
        "gemini-1.5-flash",
        "gemini-1.5-flash-002",
        "gemini-1.5-flash-8b",
        "gemini-exp-1114",
        "gemini-1.0-pro"
    )

    var apiKey by mutableStateOf(encryptedSharedPreferencesManager.get("API_KEY"))
    var currentModel by mutableStateOf(
        encryptedSharedPreferencesManager.get("MODEL") ?: availableModels[0]
    )

    fun saveKey(newKey: String) {
        encryptedSharedPreferencesManager.save("API_KEY", newKey)
        apiKey = encryptedSharedPreferencesManager.get("API_KEY")
    }

    fun saveModel(model: String) {
        encryptedSharedPreferencesManager.save("MODEL", model)
        currentModel = encryptedSharedPreferencesManager.get("MODEL") ?: availableModels[0]
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val encryptedSharedPreferencesManager =
                    EncryptedSharedPreferencesManagerImpl(context)

                return SettingsScreenViewModel(
                    encryptedSharedPreferencesManager
                ) as T
            }
        }
    }
}