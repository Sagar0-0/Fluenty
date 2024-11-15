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

    var apiKey by mutableStateOf(encryptedSharedPreferencesManager.get("API_KEY"))

    fun save(newKey: String) {
        encryptedSharedPreferencesManager.save("API_KEY",newKey)
        apiKey = encryptedSharedPreferencesManager.get("API_KEY")
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