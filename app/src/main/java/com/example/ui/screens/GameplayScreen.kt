package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.UserPreferences
import com.example.game.AIEngine
import com.example.game.GameEngine
import com.example.model.AIDifficulty
import com.example.model.BoardSize
import com.example.model.Cell
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.MoveRecord
import com.example.model.Piece
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.DreamyBackground
import com.example.ui.components.DreamyButton
import com.example.ui.components.PieceOComposable
import com.example.ui.components.PieceXComposable
import com.example.ui.components.StrikeLineOverlay
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameplayScreen(
    boardSize: BoardSize,
    gameMode: GameMode,
    difficulty: AIDifficulty,
    prefs: UserPreferences,
    soundManager: SoundManager,
    onBackToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Game Board State
    val board = remember(boardSize) {
        mutableStateListOf<MutableList<Piece>>().apply {
            repeat(boardSize.size) {
                val row = mutableStateListOf<Piece>()
                repeat(boardSize.size) { row.add(Piece.NONE) }
                add(row)
            }
        }
    }

    var currentTurn by remember { mutableStateOf(Piece.X) }
    var gameStatus by remember { mutableStateOf<GameStatus>(GameStatus.InProgress) }
    var isAiThinking by remember { mutableStateOf(false) }
    var isBoardDisabled by remember { mutableStateOf(false) }
    var showResultOverlay by remember { mutableStateOf(false) }

    var highlightedHintCell by remember { mutableStateOf<Cell?>(null) }
    val moveHistory = remember { mutableStateListOf<MoveRecord>() }

    // Win Line Animated Progress
    val winLineAnim = remember { Animatable(0f) }

    // Simulated Ad Dialog state
    var showSimulatedAdDialog by remember { mutableStateOf(false) }
    var adTargetType by remember { mutableStateOf("hint") }
    var adCountdown by remember { mutableIntStateOf(3) }

    // Level-up celebration state
    var didLevelUp by remember { mutableStateOf(false) }
    var xpEarnedThisMatch by remember { mutableIntStateOf(0) }

    // Hints & Undos from prefs
    var hintsRemaining by remember { mutableIntStateOf(prefs.hintsRemaining) }
    var undosRemaining by remember { mutableIntStateOf(prefs.undosRemaining) }

    // Reset game function
    fun resetGame() {
        for (r in 0 until boardSize.size) {
            for (c in 0 until boardSize.size) {
                board[r][c] = Piece.NONE
            }
        }
        moveHistory.clear()
        currentTurn = Piece.X
        gameStatus = GameStatus.InProgress
        isAiThinking = false
        isBoardDisabled = false
        showResultOverlay = false
        highlightedHintCell = null
        didLevelUp = false
        xpEarnedThisMatch = 0
        coroutineScope.launch {
            winLineAnim.snapTo(0f)
        }
    }

    // Handle end of game with exact requested sequencing:
    // last piece pops in -> 300ms pause -> strike line draws (~700ms) -> sparkles/confetti -> 1.2s delay -> result overlay
    fun handleGameOver(status: GameStatus) {
        gameStatus = status
        isBoardDisabled = true
        prefs.totalGames += 1

        coroutineScope.launch {
            when (status) {
                is GameStatus.Won -> {
                    val isHumanWin = (gameMode == GameMode.VS_AI && status.winner == Piece.X) || (gameMode == GameMode.TWO_PLAYERS)

                    // 1. 300ms pause after piece placement
                    delay(300L)

                    // 2. Play sound
                    if (isHumanWin) {
                        soundManager.playWinJingle()
                    } else {
                        soundManager.playLoseBloop()
                    }

                    // 3. Strike line draws smoothly over ~700ms with ease-out-expo curve (0.22, 1, 0.36, 1)
                    winLineAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 700,
                            easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                        )
                    )

                    // XP Calculations
                    if (gameMode == GameMode.VS_AI && status.winner == Piece.X) {
                        prefs.totalWins += 1
                        prefs.dailyWinsCount = (prefs.dailyWinsCount + 1).coerceAtMost(1)

                        val multiplier = when (boardSize) {
                            BoardSize.THREE_BY_THREE -> 1.0f
                            BoardSize.SIX_BY_SIX -> 1.5f
                            BoardSize.NINE_BY_NINE -> 2.0f
                        }
                        val xpToAdd = (difficulty.xpReward * multiplier).toInt()
                        xpEarnedThisMatch = xpToAdd
                        val leveled = prefs.addXp(xpToAdd)
                        if (leveled) {
                            didLevelUp = true
                            soundManager.playLevelUp()
                        }
                    } else if (gameMode == GameMode.VS_AI && status.winner == Piece.O) {
                        xpEarnedThisMatch = 5
                        prefs.addXp(5)
                    }

                    // 4. Appears ~1.2 seconds AFTER the strike-through animation finishes so player sees winning line first
                    delay(1200L)
                    showResultOverlay = true
                }

                is GameStatus.Draw -> {
                    delay(400L)
                    soundManager.playLoseBloop()
                    xpEarnedThisMatch = 15
                    prefs.addXp(15)
                    delay(800L)
                    showResultOverlay = true
                }

                else -> {}
            }
        }
    }

    // Trigger AI move if vs AI (runs in coroutine/background thread to never block UI)
    fun triggerAiMoveIfNeeded() {
        if (gameMode == GameMode.VS_AI && currentTurn == Piece.O && gameStatus is GameStatus.InProgress && !isAiThinking) {
            isAiThinking = true
            coroutineScope.launch {
                // Short human-like thinking delay
                delay((400L..650L).random())

                val boardArray = Array(boardSize.size) { r ->
                    Array(boardSize.size) { c -> board[r][c] }
                }

                val aiMove = AIEngine.getBestMove(boardArray, Piece.O, boardSize.winLength, difficulty)
                if (aiMove != null && gameStatus is GameStatus.InProgress) {
                    board[aiMove.row][aiMove.col] = Piece.O
                    moveHistory.add(MoveRecord(aiMove, Piece.O))
                    soundManager.playPop()

                    val updatedBoard = Array(boardSize.size) { r ->
                        Array(boardSize.size) { c -> board[r][c] }
                    }
                    val win = GameEngine.checkWin(updatedBoard, boardSize.winLength)
                    if (win != null) {
                        handleGameOver(win)
                    } else if (GameEngine.isDraw(updatedBoard)) {
                        handleGameOver(GameStatus.Draw)
                    } else {
                        currentTurn = Piece.X
                    }
                }
                isAiThinking = false
            }
        }
    }

    // Cell Click
    fun onCellClick(row: Int, col: Int) {
        if (gameStatus !is GameStatus.InProgress || isAiThinking || isBoardDisabled) return
        if (board[row][col] != Piece.NONE) return

        highlightedHintCell = null
        val pieceToPlace = currentTurn
        board[row][col] = pieceToPlace
        moveHistory.add(MoveRecord(Cell(row, col), pieceToPlace))
        soundManager.playPop()

        val boardArray = Array(boardSize.size) { r ->
            Array(boardSize.size) { c -> board[r][c] }
        }

        val win = GameEngine.checkWin(boardArray, boardSize.winLength)
        if (win != null) {
            handleGameOver(win)
        } else if (GameEngine.isDraw(boardArray)) {
            handleGameOver(GameStatus.Draw)
        } else {
            currentTurn = currentTurn.opponent
            if (gameMode == GameMode.VS_AI) {
                triggerAiMoveIfNeeded()
            }
        }
    }

    // Hint function
    fun onHintClick() {
        if (gameStatus !is GameStatus.InProgress || isAiThinking || isBoardDisabled) return
        if (hintsRemaining <= 0) {
            adTargetType = "hint"
            showSimulatedAdDialog = true
            return
        }

        val boardArray = Array(boardSize.size) { r ->
            Array(boardSize.size) { c -> board[r][c] }
        }
        val bestMove = AIEngine.getBestMove(boardArray, currentTurn, boardSize.winLength, AIDifficulty.HARD)
        if (bestMove != null) {
            hintsRemaining--
            prefs.hintsRemaining = hintsRemaining
            soundManager.playWhoosh()
            highlightedHintCell = bestMove
        }
    }

    // Undo function: removes strike line cleanly with fade-out if used right after game ends
    fun onUndoClick() {
        if (moveHistory.isEmpty() || isAiThinking) return
        if (undosRemaining <= 0) {
            adTargetType = "undo"
            showSimulatedAdDialog = true
            return
        }

        soundManager.playSwoosh()
        highlightedHintCell = null
        showResultOverlay = false
        isBoardDisabled = false
        gameStatus = GameStatus.InProgress
        coroutineScope.launch {
            winLineAnim.animateTo(0f, tween(250))
        }

        if (gameMode == GameMode.VS_AI) {
            if (moveHistory.size >= 2) {
                val aiMove = moveHistory.removeAt(moveHistory.lastIndex)
                board[aiMove.cell.row][aiMove.cell.col] = Piece.NONE
                val playerMove = moveHistory.removeAt(moveHistory.lastIndex)
                board[playerMove.cell.row][playerMove.cell.col] = Piece.NONE
                currentTurn = Piece.X
            } else if (moveHistory.size == 1) {
                val playerMove = moveHistory.removeAt(moveHistory.lastIndex)
                board[playerMove.cell.row][playerMove.cell.col] = Piece.NONE
                currentTurn = Piece.X
            }
        } else {
            val lastMove = moveHistory.removeAt(moveHistory.lastIndex)
            board[lastMove.cell.row][lastMove.cell.col] = Piece.NONE
            currentTurn = lastMove.piece
        }

        undosRemaining--
        prefs.undosRemaining = undosRemaining
    }

    DreamyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playButtonTap()
                        onBackToHome()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape, ambientColor = CardBorderAccent)
                        .clip(CircleShape)
                        .background(PastelWhite.copy(alpha = 0.95f))
                        .border(2.dp, CardBorderAccent, CircleShape)
                        .testTag("gameplay_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BoardFrameColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Board Size & Rule Banner with darker border
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CardBorderAccent)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PastelWhite.copy(alpha = 0.95f))
                        .border(2.dp, CardBorderAccent, RoundedCornerShape(16.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = boardSize.title,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DreamyTextPrimary
                        )
                        Text(
                            text = boardSize.ruleLabel,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BoardGridLineColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Reset Game Button
                IconButton(
                    onClick = {
                        soundManager.playPop()
                        resetGame()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape, ambientColor = CardBorderAccent)
                        .clip(CircleShape)
                        .background(PastelWhite.copy(alpha = 0.95f))
                        .border(2.dp, CardBorderAccent, CircleShape)
                        .testTag("reset_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart match",
                        tint = BoardFrameColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Status Indicator Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Player X Indicator
                PlayerIndicatorCard(
                    piece = Piece.X,
                    title = if (gameMode == GameMode.VS_AI) prefs.playerName else "Player X",
                    isActive = currentTurn == Piece.X && gameStatus is GameStatus.InProgress,
                    glowColor = PieceXColor,
                    avatarEmoji = "☁️",
                    modifier = Modifier.weight(1f)
                )

                // Turn / Round Counter Pill
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CardBorderAccent)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PastelWhite)
                        .border(2.dp, CardBorderAccent, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TURN",
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = DreamyTextMuted
                        )
                        Text(
                            text = "${moveHistory.size + 1}",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DreamyTextPrimary
                        )
                    }
                }

                // Player O Indicator
                PlayerIndicatorCard(
                    piece = Piece.O,
                    title = if (gameMode == GameMode.VS_AI) "Cloud AI" else "Player O",
                    isActive = currentTurn == Piece.O && gameStatus is GameStatus.InProgress,
                    glowColor = PieceOColor,
                    avatarEmoji = if (gameMode == GameMode.VS_AI) "🤖" else "🌸",
                    isThinking = isAiThinking,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Board View with Darker Grid and StrikeLine
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                DreamyGameBoard(
                    boardSize = boardSize,
                    board = board,
                    gameStatus = gameStatus,
                    highlightedHintCell = highlightedHintCell,
                    winLineProgress = winLineAnim.value,
                    gameMode = gameMode,
                    onCellClick = { r, c -> onCellClick(r, c) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Bar: Hint and Undo Buttons with Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hint Button
                ActionButtonWithBadge(
                    icon = Icons.Default.Lightbulb,
                    label = "Hint",
                    badgeCount = hintsRemaining,
                    accentColor = PastelYellowStar,
                    textColor = Color(0xFFB45309),
                    enabled = gameStatus is GameStatus.InProgress && !isAiThinking && !isBoardDisabled,
                    onClick = { onHintClick() },
                    modifier = Modifier.testTag("hint_button")
                )

                // Undo Button (enabled even right after game over to remove strike line)
                ActionButtonWithBadge(
                    icon = Icons.Default.Undo,
                    label = "Undo",
                    badgeCount = undosRemaining,
                    accentColor = PastelLavenderLight,
                    textColor = PastelLavenderDark,
                    enabled = moveHistory.isNotEmpty() && !isAiThinking,
                    onClick = { onUndoClick() },
                    modifier = Modifier.testTag("undo_button")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Result Overlay Modal (appears ~1.2s after strike-through finishes)
        if (showResultOverlay && gameStatus !is GameStatus.InProgress) {
            ResultOverlayModal(
                status = gameStatus,
                gameMode = gameMode,
                xpEarned = xpEarnedThisMatch,
                leveledUp = didLevelUp,
                currentLevel = prefs.level,
                onPlayAgain = {
                    soundManager.playPop()
                    resetGame()
                },
                onHome = {
                    soundManager.playButtonTap()
                    onBackToHome()
                }
            )
        }

        // Simulated Ad Modal
        if (showSimulatedAdDialog) {
            SimulatedAdDialog(
                rewardType = if (adTargetType == "hint") "Hint" else "Undo",
                onRewardGranted = {
                    if (adTargetType == "hint") {
                        hintsRemaining++
                        prefs.hintsRemaining = hintsRemaining
                    } else {
                        undosRemaining++
                        prefs.undosRemaining = undosRemaining
                    }
                    soundManager.playLevelUp()
                    showSimulatedAdDialog = false
                },
                onDismiss = {
                    showSimulatedAdDialog = false
                }
            )
        }
    }
}

/**
 * Player Card Indicator with Defined Darker Pastel Outline
 */
@Composable
fun PlayerIndicatorCard(
    piece: Piece,
    title: String,
    isActive: Boolean,
    glowColor: Color,
    avatarEmoji: String,
    isThinking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isActive) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isActive) glowColor else Color.Transparent,
                spotColor = if (isActive) glowColor else Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) PastelWhite else PastelWhite.copy(alpha = 0.85f))
            .border(
                width = if (isActive) 2.5.dp else 1.5.dp,
                color = if (isActive) glowColor.copy(alpha = pulseAlpha) else CardBorderAccent.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (piece == Piece.X) {
                PieceXComposable(size = 24.dp)
            } else {
                PieceOComposable(size = 24.dp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = title,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isActive) DreamyTextPrimary else DreamyTextSecondary,
                    maxLines = 1
                )
                if (isThinking) {
                    Text(
                        text = "Thinking... 💭",
                        fontFamily = NunitoFontFamily,
                        fontSize = 10.sp,
                        color = PastelSkyBlue
                    )
                }
            }
        }
    }
}

/**
 * Dreamy Game Board with Dark Frame (#5E3A82), Dark Grid Lines (#7B4FA0),
 * Dimmed non-winning pieces, Highlighted winning pieces, and StrikeLineOverlay!
 */
@Composable
fun DreamyGameBoard(
    boardSize: BoardSize,
    board: List<List<Piece>>,
    gameStatus: GameStatus,
    highlightedHintCell: Cell?,
    winLineProgress: Float,
    gameMode: GameMode,
    onCellClick: (Int, Int) -> Unit
) {
    val n = boardSize.size
    val wonStatus = gameStatus as? GameStatus.Won
    val winningCells = wonStatus?.winningCells.orEmpty()
    val hasWinner = wonStatus != null

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(6.dp)
            .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = BoardFrameColor)
            .clip(RoundedCornerShape(26.dp))
            .background(PastelWhite)
            // Darker 5.5px outer frame (#5E3A82)
            .border(5.5.dp, BoardFrameColor, RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Draw Deep Purple / Plum Grid Lines (#7B4FA0)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / n
            val cellH = size.height / n
            val gridStroke = when {
                n > 6 -> 2.0f
                n > 3 -> 2.5f
                else -> 3.5f
            }

            for (i in 1 until n) {
                // Vertical grid lines
                drawLine(
                    color = BoardGridLineColor,
                    start = Offset(cellW * i, 6f),
                    end = Offset(cellW * i, size.height - 6f),
                    strokeWidth = gridStroke,
                    cap = StrokeCap.Round
                )
                // Horizontal grid lines
                drawLine(
                    color = BoardGridLineColor,
                    start = Offset(6f, cellH * i),
                    end = Offset(size.width - 6f, cellH * i),
                    strokeWidth = gridStroke,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Interactive Cells
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (r in 0 until n) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (c in 0 until n) {
                        val piece = board[r][c]
                        val isHint = highlightedHintCell?.row == r && highlightedHintCell.col == c
                        val isWinningCell = winningCells.any { it.row == r && it.col == c }
                        val isDimmed = hasWinner && !isWinningCell && piece != Piece.NONE

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable {
                                    onCellClick(r, c)
                                }
                                .testTag("cell_${r}_${c}"),
                            contentAlignment = Alignment.Center
                        ) {
                            when (piece) {
                                Piece.X -> {
                                    val iconSize = when (boardSize) {
                                        BoardSize.THREE_BY_THREE -> 56.dp
                                        BoardSize.SIX_BY_SIX -> 30.dp
                                        BoardSize.NINE_BY_NINE -> 20.dp
                                    }
                                    PieceXComposable(
                                        size = iconSize,
                                        isWinningPiece = isWinningCell,
                                        isDimmed = isDimmed
                                    )
                                }
                                Piece.O -> {
                                    val iconSize = when (boardSize) {
                                        BoardSize.THREE_BY_THREE -> 56.dp
                                        BoardSize.SIX_BY_SIX -> 30.dp
                                        BoardSize.NINE_BY_NINE -> 20.dp
                                    }
                                    PieceOComposable(
                                        size = iconSize,
                                        isWinningPiece = isWinningCell,
                                        isDimmed = isDimmed
                                    )
                                }
                                Piece.NONE -> {
                                    if (isHint) {
                                        Text(
                                            text = "✨",
                                            fontSize = if (boardSize == BoardSize.THREE_BY_THREE) 24.sp else 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Strike-Through Winning Line Overlay
        if (wonStatus != null && winLineProgress > 0f) {
            StrikeLineOverlay(
                boardSize = n,
                winningLines = wonStatus.winningLines,
                winner = wonStatus.winner,
                isHumanWin = (gameMode == GameMode.VS_AI && wonStatus.winner == Piece.X) || gameMode == GameMode.TWO_PLAYERS,
                progress = winLineProgress
            )
        }
    }
}

/**
 * Action Button with Count Badge and Defined Darker Pastel Outline
 */
@Composable
fun ActionButtonWithBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeCount: Int,
    accentColor: Color,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = CardBorderAccent)
            .clip(RoundedCornerShape(20.dp))
            .background(PastelWhite)
            .border(2.dp, CardBorderAccent, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) textColor else DreamyTextMuted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (enabled) DreamyTextPrimary else DreamyTextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (badgeCount > 0) accentColor else PastelPinkLight)
                    .border(1.dp, CardBorderAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (badgeCount > 0) "$badgeCount" else "+",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (badgeCount > 0) textColor else PastelPinkDark
                )
            }
        }
    }
}

/**
 * Result Overlay Modal with Animated XP Count-Up and Confetti
 */
@Composable
fun ResultOverlayModal(
    status: GameStatus,
    gameMode: GameMode,
    xpEarned: Int,
    leveledUp: Boolean,
    currentLevel: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val isWin = (status as? GameStatus.Won)?.winner == Piece.X
    val isDraw = status is GameStatus.Draw
    val wonPiece = (status as? GameStatus.Won)?.winner

    // Animated count-up for XP
    var displayXp by remember { mutableIntStateOf(0) }
    LaunchedEffect(xpEarned) {
        if (xpEarned > 0) {
            val steps = 20
            val stepTime = 700L / steps
            for (i in 1..steps) {
                delay(stepTime)
                displayXp = (xpEarned * i) / steps
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        if (isWin || (gameMode == GameMode.TWO_PLAYERS && status is GameStatus.Won)) {
            ConfettiCelebration()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = BoardFrameColor)
                .clip(RoundedCornerShape(32.dp))
                .background(PastelWhite)
                .border(3.5.dp, CardBorderAccent, RoundedCornerShape(32.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Kawaii Cloud Face
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isWin -> Color(0xFFFFF0F5)
                                isDraw -> Color(0xFFF3E8FF)
                                else -> Color(0xFFE0F2FE)
                            }
                        )
                        .border(2.dp, CardBorderAccent.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val faceEmoji = when {
                        isWin -> "✨☁️✨"
                        isDraw -> "☁️😐☁️"
                        gameMode == GameMode.TWO_PLAYERS -> "🎉☁️🎉"
                        else -> "☁️🥺☁️"
                    }
                    Text(text = faceEmoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                val titleText = when {
                    gameMode == GameMode.TWO_PLAYERS && status is GameStatus.Won -> {
                        if (wonPiece == Piece.X) "Player X Wins! 🎀" else "Player O Wins! 🌊"
                    }
                    isWin -> "You Win! ✨"
                    isDraw -> "It's a Draw! 🌸"
                    else -> "You Lose 💧"
                }

                Text(
                    text = titleText,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = if (isWin) PastelPinkDark else DreamyTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // XP Gained Pill with animated count-up
                if (xpEarned > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelMint)
                            .border(1.dp, CardBorderAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+$displayXp XP Gained ✨",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                // Level-Up Celebration Banner
                if (leveledUp) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PastelYellowStar, PastelPinkLight)
                                )
                            )
                            .border(1.5.dp, CardBorderAccent, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎉 LEVEL UP! Now Lv. $currentLevel 🎉",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DreamyTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Buttons: Play Again & Home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Home Button
                    DreamyButton(
                        onClick = onHome,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("result_home_button"),
                        backgroundColor = PastelLavenderDark,
                        glowColor = PastelLavenderLight
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = PastelWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Home",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = PastelWhite
                            )
                        }
                    }

                    // Play Again Button
                    DreamyButton(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("play_again_button"),
                        backgroundColor = PastelPinkPrimary,
                        glowColor = PastelPinkLight
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Play Again",
                                tint = PastelWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Replay",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = PastelWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cute Simulated Ad Dialog (3-second relaxing countdown + grants 1 reward)
 */
@Composable
fun SimulatedAdDialog(
    rewardType: String,
    onRewardGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        delay(300L)
        onRewardGranted()
    }

    AlertDialog(
        onDismissRequest = { /* absorb */ },
        title = {
            Text(
                text = "Dreamy Cloud Break 🌸",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                color = DreamyTextPrimary,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Out of ${rewardType}s! Relax for a 3-second cloud breath to recharge +1 $rewardType.",
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    color = DreamyTextSecondary
                )
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PastelPinkBg)
                        .border(2.5.dp, CardBorderAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (countdown > 0) {
                        Text(
                            text = "$countdown",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp,
                            color = PastelPinkDark
                        )
                    } else {
                        Text(
                            text = "✨",
                            fontSize = 32.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (countdown > 0) "Breathing in peace..." else "Recharged +1 $rewardType!",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    color = PastelLavenderDark
                )
            }
        },
        confirmButton = {
            if (countdown <= 0) {
                TextButton(onClick = onRewardGranted) {
                    Text(
                        text = "Claim ✨",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PastelPinkDark
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontFamily = NunitoFontFamily,
                    color = DreamyTextMuted
                )
            }
        },
        shape = RoundedCornerShape(26.dp),
        containerColor = PastelWhite
    )
}
