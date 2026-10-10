package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audio.SoundManager
import com.example.data.UserPreferences
import com.example.model.AIDifficulty
import com.example.model.BoardSize
import com.example.model.GameMode
import com.example.model.Screen
import com.example.ui.screens.BoardSelectScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DreamyTacToeTheme

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = UserPreferences(this)
        soundManager = SoundManager(this).apply {
            isMusicEnabled = prefs.isMusicEnabled
            isSfxEnabled = prefs.isSfxEnabled
            isVibrateEnabled = prefs.isVibrateEnabled
        }

        setContent {
            DreamyTacToeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DreamyTacToeApp(
                        prefs = prefs,
                        soundManager = soundManager
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::soundManager.isInitialized && prefs.isMusicEnabled) {
            soundManager.startBgm()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::soundManager.isInitialized) {
            soundManager.stopBgm()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.stopBgm()
        }
    }
}

@Composable
fun DreamyTacToeApp(
    prefs: UserPreferences,
    soundManager: SoundManager
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var selectedBoardSize by remember { mutableStateOf(BoardSize.THREE_BY_THREE) }
    var selectedGameMode by remember { mutableStateOf(GameMode.VS_AI) }
    var selectedDifficulty by remember { mutableStateOf(prefs.difficulty) }

    // Start background music once splash finishes or app loads
    DisposableEffect(currentScreen) {
        if (currentScreen != Screen.SPLASH && prefs.isMusicEnabled) {
            soundManager.startBgm()
        }
        onDispose {}
    }

    // Android back navigation handling
    BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.SPLASH) {
        when (currentScreen) {
            Screen.BOARD_SELECT -> currentScreen = Screen.HOME
            Screen.SETTINGS -> currentScreen = Screen.HOME
            Screen.GAMEPLAY -> currentScreen = Screen.BOARD_SELECT
            else -> {}
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_routing"
    ) { screen ->
        when (screen) {
            Screen.SPLASH -> {
                SplashScreen(
                    onFinish = {
                        currentScreen = Screen.HOME
                    }
                )
            }

            Screen.HOME -> {
                HomeScreen(
                    prefs = prefs,
                    soundManager = soundManager,
                    selectedGameMode = selectedGameMode,
                    onGameModeChange = { selectedGameMode = it },
                    onNavigateToBoardSelect = {
                        currentScreen = Screen.BOARD_SELECT
                    },
                    onNavigateToSettings = {
                        currentScreen = Screen.SETTINGS
                    }
                )
            }

            Screen.BOARD_SELECT -> {
                BoardSelectScreen(
                    soundManager = soundManager,
                    currentMode = selectedGameMode,
                    currentDifficulty = selectedDifficulty,
                    onBack = {
                        currentScreen = Screen.HOME
                    },
                    onStartGame = { boardSize, mode, diff ->
                        selectedBoardSize = boardSize
                        selectedGameMode = mode
                        selectedDifficulty = diff
                        currentScreen = Screen.GAMEPLAY
                    }
                )
            }

            Screen.GAMEPLAY -> {
                GameplayScreen(
                    boardSize = selectedBoardSize,
                    gameMode = selectedGameMode,
                    difficulty = selectedDifficulty,
                    prefs = prefs,
                    soundManager = soundManager,
                    onBackToHome = {
                        currentScreen = Screen.HOME
                    }
                )
            }

            Screen.SETTINGS -> {
                SettingsScreen(
                    prefs = prefs,
                    soundManager = soundManager,
                    onBack = {
                        currentScreen = Screen.HOME
                    }
                )
            }
        }
    }
}
