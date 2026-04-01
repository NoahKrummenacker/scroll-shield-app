package com.example.contentblocker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.contentblocker.ui.HomeScreen
import com.example.contentblocker.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                MainApp(context = this)
            }
        }
    }
}

enum class Screen(val label: String, val icon: ImageVector) {
    HOME("Accueil", Icons.Default.Home),
    SETTINGS("Paramètres", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(context: Context) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScrollShield") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                Screen.values().forEach { screen ->
                    NavigationBarItem(
                        icon     = { Icon(screen.icon, contentDescription = screen.label) },
                        label    = { Text(screen.label) },
                        selected = currentScreen == screen,
                        onClick  = { currentScreen = screen }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState  = currentScreen,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    (slideInHorizontally(tween(280)) { if (forward) it else -it } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(280)) { if (forward) -it else it } + fadeOut(tween(280)))
                },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    Screen.HOME     -> HomeScreen(context)
                    Screen.SETTINGS -> SettingsScreen(context)
                }
            }
        }
    }
}
