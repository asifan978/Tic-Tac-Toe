package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Cell
import com.example.model.Piece
import com.example.ui.theme.BoardFrameColor
import com.example.ui.theme.PastelWhite
import com.example.ui.theme.PastelYellowStar
import com.example.ui.theme.StrikeLineColorO
import com.example.ui.theme.StrikeLineColorX
import com.example.ui.theme.StrikeLineOutline
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Strike-through winning/losing line component that accurately draws across winning cells.
 * Extended ~30% of a cell past both ends, with dark outline, inner core, glow, and traveling sparkles.
 */
@Composable
fun StrikeLineOverlay(
    boardSize: Int,
    winningLines: List<List<Cell>>,
    winner: Piece,
    isHumanWin: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "strike_glow_pulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val coreColor = if (winner == Piece.X) StrikeLineColorX else StrikeLineColorO
    val glowColor = if (isHumanWin) {
        if (winner == Piece.X) Color(0x77FF6B9D) else Color(0x774FC3F7)
    } else {
        Color(0x444FC3F7) // calmer, softer glow for loss
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cellW = w / boardSize
        val cellH = h / boardSize

        // Stroke thickness: 35% - 40% of cell width
        val strokeThickness = (cellW * 0.38f).coerceIn(8f, 32f)
        val outlineThickness = strokeThickness + (if (boardSize <= 3) 7f else 4f)

        for (line in winningLines) {
            if (line.isEmpty()) continue
            val first = line.first()
            val last = line.last()

            val center1 = Offset(cellW * (first.col + 0.5f), cellH * (first.row + 0.5f))
            val center2 = Offset(cellW * (last.col + 0.5f), cellH * (last.row + 0.5f))

            // Angle of the line
            val angle = atan2(center2.y - center1.y, center2.x - center1.x)
            val extension = cellW * 0.30f // ~30% of a cell extension past both ends

            val extStartX = center1.x - cos(angle) * extension
            val extStartY = center1.y - sin(angle) * extension
            val extEndX = center2.x + cos(angle) * extension
            val extEndY = center2.y + sin(angle) * extension

            val startOffset = Offset(extStartX, extStartY)
            val targetOffset = Offset(extEndX, extEndY)

            val currentEnd = Offset(
                startOffset.x + (targetOffset.x - startOffset.x) * progress,
                startOffset.y + (targetOffset.y - startOffset.y) * progress
            )

            if (progress > 0.01f) {
                // 1. Outer soft glow / shadow
                drawLine(
                    color = glowColor.copy(alpha = glowColor.alpha * glowPulse),
                    start = startOffset,
                    end = currentEnd,
                    strokeWidth = outlineThickness * 1.6f,
                    cap = StrokeCap.Round
                )

                // 2. Darker outline stroke (#5E3A82 at low opacity) for depth & contrast
                drawLine(
                    color = StrikeLineOutline,
                    start = startOffset,
                    end = currentEnd,
                    strokeWidth = outlineThickness,
                    cap = StrokeCap.Round
                )

                // 3. Main vibrant strike line
                drawLine(
                    color = coreColor,
                    start = startOffset,
                    end = currentEnd,
                    strokeWidth = strokeThickness,
                    cap = StrokeCap.Round
                )

                // 4. Inner glossy highlight line
                drawLine(
                    color = PastelWhite.copy(alpha = 0.55f),
                    start = startOffset,
                    end = currentEnd,
                    strokeWidth = strokeThickness * 0.28f,
                    cap = StrokeCap.Round
                )

                // 5. Small sparkles traveling along the line tip
                if (progress < 0.98f) {
                    drawTwinklingStar(
                        cx = currentEnd.x,
                        cy = currentEnd.y,
                        radius = (strokeThickness * 0.7f).coerceAtLeast(8f),
                        color = PastelYellowStar,
                        alpha = 0.95f
                    )
                } else {
                    // Sparkling stars at both tips once complete
                    drawTwinklingStar(
                        cx = currentEnd.x,
                        cy = currentEnd.y,
                        radius = (strokeThickness * 0.6f).coerceAtLeast(7f),
                        color = PastelYellowStar,
                        alpha = glowPulse
                    )
                    drawTwinklingStar(
                        cx = startOffset.x,
                        cy = startOffset.y,
                        radius = (strokeThickness * 0.6f).coerceAtLeast(7f),
                        color = PastelYellowStar,
                        alpha = glowPulse
                    )
                }
            }
        }
    }
}
