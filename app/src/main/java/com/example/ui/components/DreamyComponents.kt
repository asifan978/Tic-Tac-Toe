package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Dreamy Background with soft pastel gradient, drifting fluffy clouds, and twinkling sparkles.
 */
@Composable
fun DreamyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background_motion")

    // Slow drifting offset for clouds
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1"
    )

    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2"
    )

    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFEEF2), // soft blush pink top
                        Color(0xFFF6EEFF), // soft dreamy lilac middle
                        Color(0xFFEAF5FF), // soft sky blue bottom
                        Color(0xFFFFF7F9)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background soft drifting cloud 1
            drawFluffyCloud(
                centerX = w * 0.2f + cloudOffset1,
                centerY = h * 0.12f,
                scale = 1.1f,
                alpha = 0.75f,
                hasBow = true
            )

            // Background soft cloud 2
            drawFluffyCloud(
                centerX = w * 0.85f + cloudOffset2,
                centerY = h * 0.28f,
                scale = 0.85f,
                alpha = 0.65f,
                hasBow = false
            )

            // Background soft cloud 3
            drawFluffyCloud(
                centerX = w * 0.15f - cloudOffset2 * 0.6f,
                centerY = h * 0.72f,
                scale = 0.95f,
                alpha = 0.6f,
                hasBow = false
            )

            // Background soft cloud 4
            drawFluffyCloud(
                centerX = w * 0.88f + cloudOffset1 * 0.8f,
                centerY = h * 0.85f,
                scale = 1.05f,
                alpha = 0.7f,
                hasBow = true
            )

            // Twinkling pastel sparkles
            drawTwinklingStar(w * 0.12f, h * 0.24f, 10f, PastelYellowStar, sparkleAlpha)
            drawTwinklingStar(w * 0.78f, h * 0.15f, 12f, PastelPinkLight, 1f - sparkleAlpha * 0.5f)
            drawTwinklingStar(w * 0.92f, h * 0.42f, 9f, PastelSkyBlueLight, sparkleAlpha)
            drawTwinklingStar(w * 0.08f, h * 0.56f, 11f, PastelYellowStar, 1f - sparkleAlpha * 0.7f)
            drawTwinklingStar(w * 0.82f, h * 0.68f, 10f, PastelPinkLight, sparkleAlpha)
            drawTwinklingStar(w * 0.35f, h * 0.92f, 13f, PastelYellowStar, sparkleAlpha * 0.9f)
        }

        content()
    }
}

/**
 * Draws a fluffy pastel cloud with cute cheeks and optional pink bow ribbon.
 */
fun DrawScope.drawFluffyCloud(
    centerX: Float,
    centerY: Float,
    scale: Float,
    alpha: Float,
    hasBow: Boolean = false
) {
    val cloudColor = PastelWhite.copy(alpha = alpha)
    val r = 32f * scale

    // Cloud puffs
    drawCircle(cloudColor, r * 0.85f, Offset(centerX - r * 1.5f, centerY + r * 0.2f))
    drawCircle(cloudColor, r * 1.05f, Offset(centerX - r * 0.8f, centerY - r * 0.2f))
    drawCircle(cloudColor, r * 1.25f, Offset(centerX, centerY - r * 0.4f))
    drawCircle(cloudColor, r * 1.05f, Offset(centerX + r * 0.8f, centerY - r * 0.2f))
    drawCircle(cloudColor, r * 0.85f, Offset(centerX + r * 1.5f, centerY + r * 0.2f))
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(centerX - r * 1.6f, centerY - r * 0.1f),
        size = Size(r * 3.2f, r * 0.85f),
        cornerRadius = CornerRadius(r * 0.4f, r * 0.4f)
    )

    // Cute smiling face on cloud
    val eyeColor = Color(0xFF6B5872).copy(alpha = alpha * 0.85f)
    val blushColor = Color(0xFFFF9EB1).copy(alpha = alpha * 0.75f)

    // Eyes
    drawCircle(eyeColor, 2.2f * scale, Offset(centerX - 10f * scale, centerY + 3f * scale))
    drawCircle(eyeColor, 2.2f * scale, Offset(centerX + 10f * scale, centerY + 3f * scale))

    // Rosy cheeks
    drawCircle(blushColor, 4.5f * scale, Offset(centerX - 16f * scale, centerY + 7f * scale))
    drawCircle(blushColor, 4.5f * scale, Offset(centerX + 16f * scale, centerY + 7f * scale))

    // Cute curve smile
    val smilePath = Path().apply {
        moveTo(centerX - 4.5f * scale, centerY + 6f * scale)
        quadraticTo(centerX, centerY + 10f * scale, centerX + 4.5f * scale, centerY + 6f * scale)
    }
    drawPath(smilePath, eyeColor, style = Stroke(width = 1.6f * scale, cap = StrokeCap.Round))

    // Pink bow on right puff
    if (hasBow) {
        val bowColor = Color(0xFFFF8DA1).copy(alpha = alpha * 0.9f)
        val bowCenter = Offset(centerX + r * 1.1f, centerY - r * 0.5f)
        drawCircle(Color(0xFFFFB6C1).copy(alpha = alpha), 3.5f * scale, bowCenter)
        // bow loops
        drawCircle(bowColor, 4.2f * scale, Offset(bowCenter.x - 5f * scale, bowCenter.y))
        drawCircle(bowColor, 4.2f * scale, Offset(bowCenter.x + 5f * scale, bowCenter.y))
    }
}

/**
 * Draws a 4-point twinkling kawaii sparkle star.
 */
fun DrawScope.drawTwinklingStar(
    cx: Float,
    cy: Float,
    radius: Float,
    color: Color,
    alpha: Float
) {
    val starPath = Path().apply {
        moveTo(cx, cy - radius)
        quadraticTo(cx, cy, cx + radius, cy)
        quadraticTo(cx, cy, cx, cy + radius)
        quadraticTo(cx, cy, cx - radius, cy)
        quadraticTo(cx, cy, cx, cy - radius)
        close()
    }
    drawPath(starPath, color.copy(alpha = alpha))
}

/**
 * Animated Dreamy Button with soft bounce on press, subtle pastel gradient, and glow.
 */
@Composable
fun DreamyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PastelPinkPrimary,
    glowColor: Color = PastelPinkLight,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.88f)
                    )
                )
            )
            .border(2.5.dp, CardBorderAccent.copy(alpha = 0.85f), RoundedCornerShape(26.dp))
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            scope.launch {
                                scale.animateTo(0.92f, tween(100))
                            }
                            tryAwaitRelease()
                            scope.launch {
                                scale.animateTo(1f, tween(150))
                            }
                        },
                        onTap = {
                            if (enabled) onClick()
                        }
                    )
                }
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Dreamy Piece X: Soft pink/coral with animated strokes, highlight pulse, or dimming.
 */
@Composable
fun PieceXComposable(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    drawProgress: Float = 1f,
    isWinningPiece: Boolean = false,
    isDimmed: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "x_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x_scale"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                alpha = if (isDimmed) 0.55f else 1f
                if (isWinningPiece) {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            }
    ) {
        val w = this.size.width
        val pad = w * 0.22f
        val strokeWidth = w * 0.18f

        val start1 = Offset(pad, pad)
        val end1 = Offset(w - pad, w - pad)
        val start2 = Offset(w - pad, pad)
        val end2 = Offset(pad, w - pad)

        // Stroke 1 progress: 0f..0.5f mapped to 0f..1f
        val p1 = (drawProgress / 0.5f).coerceIn(0f, 1f)
        // Stroke 2 progress: 0.5f..1.0f mapped to 0f..1f
        val p2 = ((drawProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)

        if (p1 > 0f) {
            val curEnd1 = Offset(start1.x + (end1.x - start1.x) * p1, start1.y + (end1.y - start1.y) * p1)
            // Soft shadow
            drawLine(
                color = Color(0x33FF5D7A),
                start = Offset(start1.x, start1.y + 3f),
                end = Offset(curEnd1.x, curEnd1.y + 3f),
                strokeWidth = strokeWidth + 4f,
                cap = StrokeCap.Round
            )
            // Main stroke
            drawLine(
                color = PieceXColor,
                start = start1,
                end = curEnd1,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        if (p2 > 0f) {
            val curEnd2 = Offset(start2.x + (end2.x - start2.x) * p2, start2.y + (end2.y - start2.y) * p2)
            // Soft shadow
            drawLine(
                color = Color(0x33FF5D7A),
                start = Offset(start2.x, start2.y + 3f),
                end = Offset(curEnd2.x, curEnd2.y + 3f),
                strokeWidth = strokeWidth + 4f,
                cap = StrokeCap.Round
            )
            // Main stroke
            drawLine(
                color = PieceXColor,
                start = start2,
                end = curEnd2,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        if (drawProgress >= 0.95f) {
            // Cute glossy highlight dots
            drawCircle(PastelWhite.copy(alpha = 0.8f), strokeWidth * 0.25f, Offset(pad + 4f, pad + 4f))
            drawCircle(PastelWhite.copy(alpha = 0.8f), strokeWidth * 0.25f, Offset(w - pad - 4f, pad + 4f))
        }
    }
}

/**
 * Dreamy Piece O: Soft sky-blue ring with animated stroke, highlight pulse, or dimming.
 */
@Composable
fun PieceOComposable(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    drawProgress: Float = 1f,
    isWinningPiece: Boolean = false,
    isDimmed: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "o_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "o_scale"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                alpha = if (isDimmed) 0.55f else 1f
                if (isWinningPiece) {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            }
    ) {
        val w = this.size.width
        val strokeWidth = w * 0.18f
        val center = Offset(w / 2f, w / 2f)
        val radius = (w - strokeWidth * 1.4f) / 2f

        if (drawProgress > 0f) {
            val sweep = 360f * drawProgress.coerceIn(0f, 1f)

            // Soft shadow glow
            drawArc(
                color = Color(0x3338BDF8),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius + 3f),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth + 4f, cap = StrokeCap.Round)
            )

            // Main O ring
            drawArc(
                color = PieceOColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (drawProgress >= 0.95f) {
                // Cute glossy reflection arc
                drawArc(
                    color = PastelWhite.copy(alpha = 0.85f),
                    startAngle = 200f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth * 0.35f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Cute Kawaii Avatar Display (6 styles: cloud, bunny, bear, star kitty, moon, angel)
 */
@Composable
fun DreamyAvatarView(
    avatarId: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape, ambientColor = PastelPinkLight, spotColor = PastelPinkLight)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(PastelWhite, PastelPinkLight)
                )
            )
            .border(2.5.dp, PastelWhite, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val emoji = when (avatarId) {
            "cloud_bow" -> "☁️"
            "bunny_puff" -> "🐰"
            "bear_fluff" -> "🧸"
            "star_kitty" -> "🐱"
            "sleepy_moon" -> "🌙"
            "angel_puff" -> "👼"
            else -> "☁️"
        }
        Text(
            text = emoji,
            fontSize = (size.value * 0.52f).sp
        )
    }
}

/**
 * Confetti Particles for Level-Up and Victory
 */
data class ConfettiParticle(
    val x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val isStar: Boolean
)

@Composable
fun ConfettiCelebration(
    modifier: Modifier = Modifier
) {
    val particles = remember {
        mutableStateListOf<ConfettiParticle>().apply {
            val colors = listOf(
                PastelPinkPrimary,
                PastelSkyBlue,
                PastelLavenderLight,
                PastelYellowStar,
                PastelPinkLight,
                Color(0xFFA7F3D0)
            )
            for (i in 0 until 40) {
                add(
                    ConfettiParticle(
                        x = Random.nextFloat(),
                        y = -Random.nextFloat() * 0.8f,
                        speed = Random.nextFloat() * 0.008f + 0.005f,
                        size = Random.nextFloat() * 12f + 8f,
                        color = colors.random(),
                        isStar = Random.nextBoolean()
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            for (p in particles) {
                p.y += p.speed
                if (p.y > 1.2f) {
                    p.y = -0.1f
                }
            }
            kotlinx.coroutines.delay(16L)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (p in particles) {
            val px = p.x * w
            val py = p.y * h
            if (p.isStar) {
                drawTwinklingStar(px, py, p.size, p.color, 0.9f)
            } else {
                drawCircle(p.color, p.size * 0.6f, Offset(px, py))
            }
        }
    }
}
