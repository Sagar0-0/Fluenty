package com.sagar.fluenty.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sagar.fluenty.ui.utils.AppTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel = viewModel(
        factory = SettingsScreenViewModel.getFactory(LocalContext.current.applicationContext)
    ),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var textField by remember {
        mutableStateOf(viewModel.apiKey ?: "")
    }
    var isDropDownExpanded by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier
            .background(Color.Black)
            .statusBarsPadding(),
        topBar = {
            AppTopBar(
                text = "Settings",
                leadingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                onLeadingIconClick = onBack
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(20.dp)
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = textField,
                onValueChange = {
                    textField = it
                },
                trailingIcon = if (textField.isNotEmpty() && textField != viewModel.apiKey) {
                    {
                        IconButton(
                            onClick = {
                                viewModel.saveKey(textField)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                } else null,
                placeholder = {
                    Text(text = "Enter your API Key", color = Color.White)
                }
            )

            Column {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        isDropDownExpanded = !isDropDownExpanded
                    }
                ) {
                    Text("Current Model: ${viewModel.currentModel}")
                }

                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    expanded = isDropDownExpanded,
                    onDismissRequest = {
                        isDropDownExpanded = !isDropDownExpanded
                    },
                    scrollState = rememberScrollState(),
                    containerColor = Color.DarkGray
                ) {
                    viewModel.availableModels.forEach {
                        DropdownMenuItem(
                            text = {
                                Text(text = it, color = Color.White)
                            },
                            onClick = {
                                viewModel.saveModel(it)
                                isDropDownExpanded = !isDropDownExpanded
                            }
                        )
                    }
                }
            }

            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = "Note: You can safely use your api key to test this application. We do not exploit your key or share it with anyone. We use EncryptedSharedPreferences to store it in your device, see below",
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.goToURL("https://aistudio.google.com/apikey")
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text("Generate Api key")
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    context.goToURL("https://github.com/Sagar0-0/Fluenty")
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text("Source Code")
                }
            }

        }
    }
}

fun Context.goToURL(url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
}