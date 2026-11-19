package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.rememberReducedMotionEnabled

@Composable
fun LiquidShieldOverlay(
    modifier: Modifier = Modifier,
    orbSize: Dp = 180.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    showObsidianBackdrop: Boolean = true,
    testTag: String = "liquid_shield_overlay",
    content: @Composable BoxScope.() -> Unit = {}
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    val radiusScale: Float
    val orbOpacity: Float
    val ripplePhase: Float

    if (reducedMotion) {
        radiusScale = 0.86f
        orbOpacity = 0.72f
        ripplePhase = 0.30f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "liquid_shield_overlay_transition")
        val animatedRadius by infiniteTransition.animateFloat(
            initialValue = 0.72f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "liquid_shield_orb_radius"
        )
        val animatedOpacity by infiniteTransition.animateFloat(
            initialValue = 0.50f,
            targetValue = 0.90f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "liquid_shield_orb_opacity"
        )
        val animatedRipple by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "liquid_shield_orb_ripple"
        )
        radiusScale = animatedRadius
        orbOpacity = animatedOpacity
        ripplePhase = animatedRipple
    }

    Box(
        modifier = modifier
            .then(
                if (showObsidianBackdrop) {
                    Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(
                                neu.bgBloomPrimary.copy(alpha = 0.92f),
                                scheme.background.copy(alpha = 0.96f)
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
            val coreRadius = maxRadius * 0.42f * radiusScale
            val haloRadius = maxRadius * (0.78f + 0.18f * (radiusScale - 0.72f) / 0.28f)

            // Outer translucent Electric Cyan & Glacial Cobalt aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.30f * orbOpacity),
                        scheme.primary.copy(alpha = 0.14f * orbOpacity),
                        neu.orbCoreDeep.copy(alpha = 0.28f * orbOpacity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = haloRadius
                ),
                radius = haloRadius,
                center = center
            )

            // Concentric living glass ripple waves
            for (i in 0 until 3) {
                val p = (ripplePhase + i / 3f) % 1f
                val waveRadius = coreRadius + (maxRadius * 0.92f - coreRadius) * p
                val waveAlpha = (1f - p) * (1f - p) * 0.48f * orbOpacity
                if (waveAlpha > 0.01f) {
                    val rippleColor = if (i == 1) scheme.primary else accentColor
                    drawCircle(
                        color = rippleColor.copy(alpha = waveAlpha),
                        radius = waveRadius,
                        center = center,
                        style = Stroke(width = (1.5.dp.toPx()) * (1f - 0.3f * p))
                    )
                }
            }

            // Central semi-transparent breathing orb with multi-stop radial gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neu.orbCoreHighlight.copy(alpha = 0.90f * orbOpacity),
                        accentColor.copy(alpha = 0.74f * orbOpacity),
                        neu.orbCoreDeep.copy(alpha = 0.80f * orbOpacity),
                        scheme.background.copy(alpha = 0.30f * orbOpacity)
                    ),
                    center = Offset(
                        center.x - coreRadius * 0.22f,
                        center.y - coreRadius * 0.22f
                    ),
                    radius = coreRadius * 1.25f
                ),
                radius = coreRadius,
                center = center
            )

            // Specular cyan/white glass rim highlight
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f * orbOpacity),
                        accentColor.copy(alpha = 0.45f * orbOpacity),
                        Color.Transparent
                    ),
                    start = Offset(center.x - coreRadius, center.y - coreRadius),
                    end = Offset(center.x + coreRadius, center.y + coreRadius)
                ),
                radius = coreRadius,
                center = center,
                style = Stroke(width = 1.25.dp.toPx())
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}
