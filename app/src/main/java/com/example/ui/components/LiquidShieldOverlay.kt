package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HabitAmberGlow
import com.example.ui.theme.HabitBg
import com.example.ui.theme.HabitForest
import com.example.ui.theme.HabitSageSoft
import com.example.ui.theme.HabitSageTint
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.rememberReducedMotionEnabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidShieldOverlay(
    modifier: Modifier = Modifier,
    orbSize: Dp = 180.dp,
    accentColor: Color = HabitForest,
    showObsidianBackdrop: Boolean = true,
    cycleSeconds: Int = 4,
    testTag: String = "liquid_shield_overlay",
    content: @Composable BoxScope.() -> Unit = {}
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val halfCycleMs = (cycleSeconds.coerceIn(2, 4) * 1000)

    val breathPhase: Float
    if (reducedMotion) {
        breathPhase = 0.6f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "sanctuary_overlay_$cycleSeconds")
        val animatedPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = halfCycleMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sanctuary_breath_phase"
        )
        breathPhase = animatedPhase
    }

    Box(
        modifier = modifier
            .then(
                if (showObsidianBackdrop) {
                    Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(
                                HabitSageTint.copy(alpha = 0.55f),
                                HabitBg.copy(alpha = 0.98f)
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(orbSize)
                .testTag("liquid_shield_orb_canvas")
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f
            val outerAuraRadius = maxRadius * (0.82f + 0.14f * breathPhase)
            val ringRadius = maxRadius * (0.56f + 0.14f * breathPhase)

            // Soft Sage breathing mist aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HabitSageTint.copy(alpha = 0.72f),
                        HabitSageSoft.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerAuraRadius
                ),
                radius = outerAuraRadius,
                center = center
            )

            // Porcelain center disk
            drawCircle(
                color = HabitSurface,
                radius = ringRadius,
                center = center
            )

            // Crisp Deep Forest Open Loop arc
            val strokePx = (size.minDimension * 0.065f).coerceAtLeast(4.dp.toPx())
            drawArc(
                color = accentColor,
                startAngle = -14f,
                sweepAngle = 298f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Warm Amber focal anchor dot
            val dotRad = (-46f * (PI / 180.0))
            drawCircle(
                color = HabitAmberGlow,
                radius = strokePx * 0.65f,
                center = Offset(
                    x = center.x + (ringRadius * cos(dotRad)).toFloat(),
                    y = center.y + (ringRadius * sin(dotRad)).toFloat()
                )
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}
