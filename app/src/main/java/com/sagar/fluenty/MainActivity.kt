package com.sagar.fluenty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sagar.fluenty.ui.screen.ConversationScreen
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
                    startDestination = "CONVERSATION"
                ) {
                    composable("CONVERSATION") {
                        ConversationScreen()
                    }
                }
            }
        }
    }
}