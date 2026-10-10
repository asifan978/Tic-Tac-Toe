package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.DreamyBackground
import com.example.ui.theme.DreamyTextPrimary
import com.example.ui.theme.DreamyTextSecondary
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.PastelPinkDark
import com.example.ui.theme.PastelPinkLight
import com.example.ui.theme.PastelPinkPrimary
import com.example.ui.theme.PastelSkyBlue
import com.example.ui.theme.PastelWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val bounceAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Bounce mascot gently
        bounceAnim.animateTo(
            targetValue = -16f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        // Smooth loading progress to 100%
        val steps = 40
        for (i in 1..steps) {
            delay(40L)
            progress = i / steps.toFloat()
        }
        delay(250L)
        onFinish()
    }

    DreamyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cute Mascot with gentle bounce
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = bounceAnim.value
                    }
                    .size(170.dp)
                    .shadow(16.dp, CircleShape, ambientColor = PastelPinkPrimary, spotColor = PastelPinkLight)
                    .clip(CircleShape)
                    .background(PastelWhite)
                    .border(4.dp, PastelPinkLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mascot),
                    contentDescription = "Cute mascot cloud with pink bow",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Big Dreamy Title
            Text(
                text = "Dreamy Tac Toe",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = DreamyTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Pastel clouds & cozy moves ✨",
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = DreamyTextSecondary
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Soft rounded progress bar
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(18.dp)
                    .shadow(4.dp, RoundedCornerShape(9.dp), ambientColor = PastelPinkLight)
                    .clip(RoundedCornerShape(9.dp))
                    .background(PastelWhite.copy(alpha = 0.85f))
                    .border(1.5.dp, PastelPinkLight, RoundedCornerShape(9.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PastelPinkPrimary, PastelSkyBlue)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Percentage pill
            Text(
                text = "${(progress * 100).toInt()}%",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = PastelPinkDark
            )
        }
    }
}
