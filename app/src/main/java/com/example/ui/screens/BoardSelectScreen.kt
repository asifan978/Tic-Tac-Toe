package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.model.AIDifficulty
import com.example.model.BoardSize
import com.example.model.GameMode
import com.example.ui.components.DreamyBackground
import com.example.ui.components.DreamyButton
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BoardSelectScreen(
    soundManager: SoundManager,
    currentMode: GameMode,
    currentDifficulty: AIDifficulty,
    onBack: () -> Unit,
    onStartGame: (BoardSize, GameMode, AIDifficulty) -> Unit
) {
    val boardSizes = listOf(BoardSize.THREE_BY_THREE, BoardSize.SIX_BY_SIX, BoardSize.NINE_BY_NINE)
    var selectedIndex by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf(currentMode) }
    var difficulty by remember { mutableStateOf(currentDifficulty) }

    val currentBoard = boardSizes[selectedIndex]
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
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playButtonTap()
                        onBack()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape, ambientColor = PastelPinkLight)
                        .clip(CircleShape)
                        .background(PastelWhite.copy(alpha = 0.9f))
                        .border(1.5.dp, PastelPinkLight, CircleShape)
                        .testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = PastelPinkDark
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Select Board",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DreamyTextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carousel Header & Left/Right Arrows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        soundManager.playPop()
                        selectedIndex = if (selectedIndex > 0) selectedIndex - 1 else boardSizes.size - 1
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, CircleShape, ambientColor = PastelPinkLight)
                        .clip(CircleShape)
                        .background(PastelWhite)
                        .border(1.5.dp, PastelPinkLight, CircleShape)
                        .testTag("prev_board_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous board",
                        tint = PastelLavenderDark,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Dot Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    boardSizes.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == selectedIndex) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == selectedIndex) PastelPinkPrimary else PastelLavenderLight
                                )
                        )
                    }
                }

                IconButton(
                    onClick = {
                        soundManager.playPop()
                        selectedIndex = if (selectedIndex < boardSizes.size - 1) selectedIndex + 1 else 0
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, CircleShape, ambientColor = PastelPinkLight)
                        .clip(CircleShape)
                        .background(PastelWhite)
                        .border(1.5.dp, PastelPinkLight, CircleShape)
                        .testTag("next_board_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next board",
                        tint = PastelLavenderDark,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally animated card for selected board size
            AnimatedContent(
                targetState = currentBoard,
                transitionSpec = {
                    (slideInHorizontally { width -> width / 2 } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -width / 2 } + fadeOut())
                },
                label = "board_card"
            ) { board ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = CardBorderAccent)
                        .clip(RoundedCornerShape(28.dp))
                        .background(PastelWhite.copy(alpha = 0.95f))
                        .border(3.dp, CardBorderAccent, RoundedCornerShape(28.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = board.title,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = DreamyTextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Win Rule Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelLavenderLight)
                                .border(1.5.dp, CardBorderAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✨ ${board.ruleLabel} ✨",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PastelLavenderDark
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live preview grid of this board size with pieces and animated glowing win line
                        BoardSizePreviewGrid(board = board)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = board.description,
                            fontFamily = NunitoFontFamily,
                            fontSize = 13.sp,
                            color = DreamyTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Game Mode Tabs: "Play vs AI" (robot) and "2 Players" (clouds)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = CardBorderAccent)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PastelWhite)
                    .border(2.dp, CardBorderAccent, RoundedCornerShape(22.dp))
                    .padding(4.dp)
            ) {
                // Play vs AI Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (mode == GameMode.VS_AI) PastelPinkPrimary else Color.Transparent
                        )
                        .clickable {
                            soundManager.playPop()
                            mode = GameMode.VS_AI
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🤖 ", fontSize = 15.sp)
                        Text(
                            text = "Play vs AI",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (mode == GameMode.VS_AI) PastelWhite else DreamyTextSecondary
                        )
                    }
                }

                // 2 Players Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (mode == GameMode.TWO_PLAYERS) PastelSkyBlue else Color.Transparent
                        )
                        .clickable {
                            soundManager.playPop()
                            mode = GameMode.TWO_PLAYERS
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☁️☁️ ", fontSize = 13.sp)
                        Text(
                            text = "2 Players",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (mode == GameMode.TWO_PLAYERS) PastelWhite else DreamyTextSecondary
                        )
                    }
                }
            }

            // Difficulty selector (if vs AI)
            if (mode == GameMode.VS_AI) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AIDifficulty.entries.forEach { diff ->
                        val isSelected = difficulty == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    if (isSelected) 4.dp else 1.dp,
                                    RoundedCornerShape(14.dp),
                                    ambientColor = PastelLavenderLight
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) PastelLavenderLight else PastelWhite)
                                .border(
                                    1.5.dp,
                                    if (isSelected) PastelLavenderDark else PastelLavenderLight,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    soundManager.playPop()
                                    difficulty = diff
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = diff.label,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) PastelLavenderDark else DreamyTextSecondary
                                )
                                Text(
                                    text = "+${diff.xpReward} XP",
                                    fontFamily = NunitoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = DreamyTextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Start Match Button
            DreamyButton(
                onClick = {
                    soundManager.playPop()
                    onStartGame(currentBoard, mode, difficulty)
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .testTag("start_match_button"),
                backgroundColor = PastelPinkPrimary,
                glowColor = PastelPinkLight
            ) {
                Text(
                    text = "Start Match ✨",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PastelWhite
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Preview Grid showing grid cells and looping animated winning lines.
 */
@Composable
fun BoardSizePreviewGrid(board: BoardSize) {
    val lineProgress = remember { Animatable(0f) }

    LaunchedEffect(board) {
        while (true) {
            lineProgress.snapTo(0f)
            delay(400L)
            lineProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .size(160.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = BoardFrameColor)
            .clip(RoundedCornerShape(18.dp))
            .background(PastelWhite)
            .border(5.dp, BoardFrameColor, RoundedCornerShape(18.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val n = board.size
            val cellW = size.width / n
            val cellH = size.height / n

            // Dark Grid lines (#7B4FA0)
            for (i in 1 until n) {
                // Vertical
                drawLine(
                    color = BoardGridLineColor,
                    start = Offset(cellW * i, 0f),
                    end = Offset(cellW * i, size.height),
                    strokeWidth = if (n > 6) 2.2f else 3.2f,
                    cap = StrokeCap.Round
                )
                // Horizontal
                drawLine(
                    color = BoardGridLineColor,
                    start = Offset(0f, cellH * i),
                    end = Offset(size.width, cellH * i),
                    strokeWidth = if (n > 6) 2.2f else 3.2f,
                    cap = StrokeCap.Round
                )
            }

            // Draw cute demo pieces
            val winLen = board.winLength
            val row = n / 2
            for (c in 0 until winLen) {
                val cx = cellW * (c + 0.5f)
                val cy = cellH * (row + 0.5f)
                val rad = (cellW * 0.32f).coerceAtLeast(4f)
                // draw demo piece
                if (c % 2 == 0) {
                    // Piece X mark
                    drawLine(
                        PieceXColor,
                        Offset(cx - rad, cy - rad),
                        Offset(cx + rad, cy + rad),
                        strokeWidth = (rad * 0.6f).coerceAtLeast(2f),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        PieceXColor,
                        Offset(cx + rad, cy - rad),
                        Offset(cx - rad, cy + rad),
                        strokeWidth = (rad * 0.6f).coerceAtLeast(2f),
                        cap = StrokeCap.Round
                    )
                } else {
                    // Piece O
                    drawCircle(
                        color = PieceOColor,
                        radius = rad,
                        center = Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = (rad * 0.55f).coerceAtLeast(2f)
                        )
                    )
                }
            }

            // Glowing strike line animation across the winning sequence
            if (lineProgress.value > 0f) {
                val start = Offset(cellW * 0.15f, cellH * (row + 0.5f))
                val targetEnd = Offset(cellW * (winLen - 0.15f), cellH * (row + 0.5f))
                val curEnd = Offset(
                    start.x + (targetEnd.x - start.x) * lineProgress.value,
                    start.y
                )

                // Glow
                drawLine(
                    color = Color(0x66FF6B9D),
                    start = start,
                    end = curEnd,
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
                // Outline
                drawLine(
                    color = StrikeLineOutline,
                    start = start,
                    end = curEnd,
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                // Core
                drawLine(
                    color = StrikeLineColorX,
                    start = start,
                    end = curEnd,
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
