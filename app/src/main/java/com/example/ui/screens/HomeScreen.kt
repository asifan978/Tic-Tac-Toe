package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.UserPreferences
import com.example.model.GameMode
import com.example.ui.components.DreamyAvatarView
import com.example.ui.components.DreamyBackground
import com.example.ui.components.DreamyButton
import com.example.ui.components.PieceOComposable
import com.example.ui.components.PieceXComposable
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    prefs: UserPreferences,
    soundManager: SoundManager,
    selectedGameMode: GameMode,
    onGameModeChange: (GameMode) -> Unit,
    onNavigateToBoardSelect: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(prefs.playerName) }
    var showOnboarding by remember { mutableStateOf(!prefs.hasSeenOnboarding) }

    val scrollState = rememberScrollState()

    DreamyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Avatar, Name, Level badge on Left; Settings Gear on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Player Info Pill
                Row(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = PastelPinkLight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PastelWhite.copy(alpha = 0.9f))
                        .border(1.5.dp, PastelPinkLight, RoundedCornerShape(24.dp))
                        .clickable {
                            soundManager.playButtonTap()
                            tempName = prefs.playerName
                            showNameDialog = true
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DreamyAvatarView(
                        avatarId = prefs.avatarId,
                        size = 42.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = prefs.playerName,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = DreamyTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "✎",
                                fontSize = 11.sp,
                                color = DreamyTextMuted
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PastelLavenderLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lv. ${prefs.level}",
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PastelLavenderDark
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${prefs.xp}/${prefs.getXpForNextLevel(prefs.level)} XP",
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = DreamyTextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Settings Gear Button
                IconButton(
                    onClick = {
                        soundManager.playButtonTap()
                        onNavigateToSettings()
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, CircleShape, ambientColor = PastelPinkLight)
                        .clip(CircleShape)
                        .background(PastelWhite.copy(alpha = 0.9f))
                        .border(1.5.dp, PastelPinkLight, CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = PastelLavenderDark
                    )
                }
            }

            // First-time Onboarding banner if not dismissed
            AnimatedVisibility(
                visible = showOnboarding,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = PastelPinkLight)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFF0F5), Color(0xFFF3E8FF))
                            )
                        )
                        .border(1.5.dp, PastelPinkLight, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome to Dreamy Tac Toe!",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DreamyTextPrimary
                            )
                            Text(
                                text = "Choose classic 3×3 or big 6×6 and 9×9 boards. Tap Play to begin!",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = DreamyTextSecondary
                            )
                        }
                        IconButton(
                            onClick = {
                                showOnboarding = false
                                prefs.hasSeenOnboarding = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss tooltip",
                                tint = DreamyTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Center: Large Soft "MAIN GAME" Card with Mini 3x3 Animated Board Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = CardBorderAccent)
                    .clip(RoundedCornerShape(28.dp))
                    .background(PastelWhite.copy(alpha = 0.95f))
                    .border(3.dp, CardBorderAccent, RoundedCornerShape(28.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Badge Header
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelPinkBg)
                            .border(1.5.dp, CardBorderAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "☁️ MAIN GAME ☁️",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PastelPinkDark
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated 3x3 Preview Board
                    MiniBoardPreview()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Match lines on cozy cloud boards",
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = DreamyTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pill Toggles: "Classic / Offline" and "1P vs AI / 2P Local"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Classic / Offline Status Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(3.dp, RoundedCornerShape(18.dp), ambientColor = CardBorderAccent)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PastelSkyBlueBg)
                        .border(2.dp, CardBorderAccent.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌸 ", fontSize = 12.sp)
                        Text(
                            text = "Classic Offline",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = PastelSkyBlue
                        )
                    }
                }

                // 1P vs AI / 2P Local Mode Toggle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = CardBorderAccent)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PastelWhite)
                        .border(2.dp, CardBorderAccent, RoundedCornerShape(18.dp))
                        .clickable {
                            soundManager.playPop()
                            val nextMode = if (selectedGameMode == GameMode.VS_AI) {
                                GameMode.TWO_PLAYERS
                            } else {
                                GameMode.VS_AI
                            }
                            onGameModeChange(nextMode)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (selectedGameMode == GameMode.VS_AI) "🤖 " else "👥 ",
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (selectedGameMode == GameMode.VS_AI) "1P vs AI" else "2P Local",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PastelLavenderDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Rounded PLAY Button
            DreamyButton(
                onClick = {
                    soundManager.playPop()
                    onNavigateToBoardSelect()
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .testTag("play_button"),
                backgroundColor = PastelPinkPrimary,
                glowColor = PastelPinkLight
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Icon",
                        tint = PastelWhite,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = PastelWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tic Tac Toe",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = DreamyTextMuted
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Daily Challenge Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = PastelMint)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PastelWhite.copy(alpha = 0.95f))
                    .border(1.5.dp, PastelMint, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8FDF3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎁", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Challenge",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DreamyTextPrimary
                        )
                        Text(
                            text = if (prefs.dailyWinsCount >= 1) "Completed! +50 XP awarded" else "Win 1 game today  +50 XP",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = if (prefs.dailyWinsCount >= 1) PastelMintDark else DreamyTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (prefs.dailyWinsCount >= 1) PastelMint else PastelPinkLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (prefs.dailyWinsCount >= 1) "Claimed ✨" else "${prefs.dailyWinsCount}/1",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (prefs.dailyWinsCount >= 1) Color(0xFF065F46) else PastelPinkDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Name Edit Dialog
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = {
                    Text(
                        text = "Edit Your Name",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DreamyTextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "How would you like to be called in Dreamy Tac Toe?",
                            fontFamily = NunitoFontFamily,
                            fontSize = 13.sp,
                            color = DreamyTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { if (it.length <= 14) tempName = it },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                prefs.playerName = tempName.trim()
                            }
                            showNameDialog = false
                        }
                    ) {
                        Text(
                            text = "Save",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PastelPinkDark
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("Cancel", fontFamily = NunitoFontFamily)
                    }
                },
                shape = RoundedCornerShape(22.dp),
                containerColor = PastelWhite
            )
        }
    }
}

/**
 * Animated Mini 3x3 Preview Board with sequential move loop and animated winning line.
 */
@Composable
fun MiniBoardPreview() {
    // Step loop: 0 (empty), 1 (X top-left), 2 (O center), 3 (X top-mid), 4 (O bot-left), 5 (X top-right), 6 (win line draw!)
    var step by remember { mutableIntStateOf(0) }
    val lineProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            step = 0
            lineProgress.snapTo(0f)
            delay(500L)

            step = 1 // X at (0,0)
            delay(400L)
            step = 2 // O at (1,1)
            delay(400L)
            step = 3 // X at (0,1)
            delay(400L)
            step = 4 // O at (2,0)
            delay(400L)
            step = 5 // X at (0,2)
            delay(300L)

            // Draw glowing win line
            lineProgress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            delay(1200L)
        }
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = BoardFrameColor)
            .clip(RoundedCornerShape(20.dp))
            .background(PastelWhite)
            .border(5.dp, BoardFrameColor, RoundedCornerShape(20.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dark Grid lines (#7B4FA0)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / 3f
            val cellH = size.height / 3f
            val gridColor = BoardGridLineColor

            // Vertical lines
            drawLine(gridColor, Offset(cellW, 6f), Offset(cellW, size.height - 6f), strokeWidth = 3.5f, cap = StrokeCap.Round)
            drawLine(gridColor, Offset(cellW * 2f, 6f), Offset(cellW * 2f, size.height - 6f), strokeWidth = 3.5f, cap = StrokeCap.Round)

            // Horizontal lines
            drawLine(gridColor, Offset(6f, cellH), Offset(size.width - 6f, cellH), strokeWidth = 3.5f, cap = StrokeCap.Round)
            drawLine(gridColor, Offset(6f, cellH * 2f), Offset(size.width - 6f, cellH * 2f), strokeWidth = 3.5f, cap = StrokeCap.Round)

            // Strike-through line across top row (row 0), extended ~30%
            if (lineProgress.value > 0f) {
                val start = Offset(cellW * 0.15f, cellH * 0.5f)
                val targetEnd = Offset(cellW * 2.85f, cellH * 0.5f)
                val currentEnd = Offset(
                    start.x + (targetEnd.x - start.x) * lineProgress.value,
                    start.y
                )
                // Glow
                drawLine(
                    color = Color(0x66FF6B9D),
                    start = start,
                    end = currentEnd,
                    strokeWidth = 18f,
                    cap = StrokeCap.Round
                )
                // Outline
                drawLine(
                    color = StrikeLineOutline,
                    start = start,
                    end = currentEnd,
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
                // Main strike line in #FF6B9D
                drawLine(
                    color = StrikeLineColorX,
                    start = start,
                    end = currentEnd,
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                // Inner highlight
                drawLine(
                    color = PastelWhite.copy(alpha = 0.6f),
                    start = start,
                    end = currentEnd,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Preview pieces placed
        Box(modifier = Modifier.fillMaxSize()) {
            val cellFraction = 1f / 3f

            // (0,0) X
            if (step >= 1) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 0.dp, start = 0.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    PieceXComposable(size = 32.dp, modifier = Modifier.padding(4.dp))
                }
            }
            // (1,1) O
            if (step >= 2) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PieceOComposable(size = 32.dp)
                }
            }
            // (0,1) X
            if (step >= 3) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    PieceXComposable(size = 32.dp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            // (2,0) O
            if (step >= 4) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    PieceOComposable(size = 32.dp, modifier = Modifier.padding(4.dp))
                }
            }
            // (0,2) X
            if (step >= 5) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    PieceXComposable(size = 32.dp, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
