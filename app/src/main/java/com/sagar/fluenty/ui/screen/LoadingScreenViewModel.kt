package com.sagar.fluenty.ui.screen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagar.fluenty.ui.utils.InferenceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoadingScreenViewModel : ViewModel() {

    var errorMessage by mutableStateOf("")

    fun initModel(context: Context, onModelLoaded: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                InferenceModel.getInstance(context)
                // Notify the UI that the model has finished loading
                withContext(Dispatchers.Main) {
                    onModelLoaded()
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "UnknownError"
            }
        }
    }
}