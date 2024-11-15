package com.sagar.fluenty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sagar.fluenty.ui.screen.audio.AudioRecordScreen
import com.sagar.fluenty.ui.screen.conversation.ConversationScreen
import com.sagar.fluenty.ui.screen.home.HomeScreen
import com.sagar.fluenty.ui.screen.settings.SettingsScreen
import com.sagar.fluenty.ui.theme.FluentyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FluentyTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "HOME"
                ) {
                    composable("HOME") {
                        HomeScreen(
                            onConversationClick = {
                                navController.navigate("CONVERSATION")
                            },
                            onAudioClick = {
                                navController.navigate("AUDIO")
                            },
                            onSettingsClick = {
                                navController.navigate("SETTINGS")
                            }
                        )
                    }
                    composable("CONVERSATION") {
                        ConversationScreen {
                            navController.navigateUp()
                        }
                    }
                    composable("AUDIO") {
                        AudioRecordScreen {
                            navController.navigateUp()
                        }
                    }
                    composable("SETTINGS") {
                        SettingsScreen {
                            navController.navigateUp()
                        }
                    }
                }
            }
        }
    }
}