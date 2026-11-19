package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BarInactive
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CalmSageDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.ElectricAmberGlow
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianGlow
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SurgeonMotion
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.rememberReducedMotionEnabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidBreathingOrb(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    accentColor: Color = CalmSage,
    testTag: String = "liquid_breathing_orb"
) {
    val reducedMotion = rememberReducedMotionEnabled()

    val pulse: Float
    val ripplePhase: Float
    if (reducedMotion) {
        pulse = 0.5f
        ripplePhase = 0.25f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "liquid_orb_transition")
        val animatedPulse by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_breath_pulse"
        )
        val animatedRipple by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "orb_ripple_phase"
        )
        pulse = animatedPulse
        ripplePhase = animatedRipple
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f
            val baseCoreRadius = maxRadius * (0.34f + 0.04f * pulse)

            // Ambient warm sage & amber halo behind the orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.26f + 0.08f * pulse),
                        ElectricAmber.copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 0.98f
                ),
                radius = maxRadius * 0.98f,
                center = center
            )

            // 3 phase-offset concentric expanding ripples
            for (i in 0 until 3) {
                val progress = (ripplePhase + i / 3f) % 1f
                val rippleRadius = baseCoreRadius + (maxRadius * 0.92f - baseCoreRadius) * progress
                val alpha = (1f - progress) * (1f - progress) * 0.45f
                if (alpha > 0.01f) {
                    val rippleColor = if (i == 1) ElectricAmber else accentColor
                    drawCircle(
                        color = rippleColor.copy(alpha = alpha),
                        radius = rippleRadius,
                        center = center,
                        style = Stroke(width = (1.5.dp.toPx()) * (1f - 0.35f * progress))
                    )
                }
            }

            // Breathing Calm Sage liquid core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFC9EAD6),
                        accentColor,
                        CalmSageDark
                    ),
                    center = Offset(
                        center.x - baseCoreRadius * 0.28f,
                        center.y - baseCoreRadius * 0.28f
                    ),
                    radius = baseCoreRadius * 1.45f
                ),
                radius = baseCoreRadius,
                center = center
            )

            // Crisp specular rim on the orb edge
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF8EE).copy(alpha = 0.65f),
                        accentColor.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    start = Offset(center.x - baseCoreRadius, center.y - baseCoreRadius),
                    end = Offset(center.x + baseCoreRadius, center.y + baseCoreRadius)
                ),
                radius = baseCoreRadius,
                center = center,
                style = Stroke(width = 1.25.dp.toPx())
            )
        }
    }
}

@Composable
fun ProtectionRingGauge(
    progress: Float,
    count: Int,
    modifier: Modifier = Modifier,
    isOperational: Boolean = true,
    ringSize: Dp = 118.dp
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val clampedProgress = progress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = if (reducedMotion) snap() else SurgeonMotion.RingGaugeSpring,
        label = "protection_ring_progress"
    )
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = if (reducedMotion) snap() else tween(450, easing = FastOutSlowInEasing),
        label = "protection_ring_count"
    )

    val activeColor = if (isOperational) ElectricAmber else SoftGold
    val activeGlow = if (isOperational) ElectricAmberGlow else SoftGold.copy(alpha = 0.24f)

    Box(
        modifier = modifier
            .size(ringSize)
            .semantics(mergeDescendants = false) {},
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 8.dp.toPx()
            val glowStrokePx = 14.dp.toPx()
            val inset = glowStrokePx / 2f + 2.dp.toPx()
            val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            val startAngle = 140f
            val totalSweep = 260f

            // Background Warm Obsidian track
            drawArc(
                color = BarInactive.copy(alpha = 0.9f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val visualFraction = if (count > 0) animatedProgress.coerceAtLeast(0.06f) else animatedProgress
            val activeSweep = totalSweep * visualFraction

            if (activeSweep > 0.5f) {
                // Soft ambient amber glow arc
                drawArc(
                    color = activeGlow,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = glowStrokePx, cap = StrokeCap.Round)
                )

                // Crisp foreground arc
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CalmSage.copy(alpha = 0.82f),
                            activeColor
                        ),
                        start = Offset(0f, this.size.height),
                        end = Offset(this.size.width, 0f)
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Luminous end-cap dot
                val endAngleRad = (startAngle + activeSweep) * (PI / 180.0)
                val arcRadius = arcSize.width / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val tipOffset = Offset(
                    x = center.x + (arcRadius * cos(endAngleRad)).toFloat(),
                    y = center.y + (arcRadius * sin(endAngleRad)).toFloat()
                )
                drawCircle(
                    color = activeColor.copy(alpha = 0.42f),
                    radius = strokePx * 0.9f,
                    center = tipOffset
                )
                drawCircle(
                    color = Color(0xFFFFF8EE),
                    radius = strokePx * 0.36f,
                    center = tipOffset
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = animatedCount.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFeatureSettings = "tnum",
                    fontSize = 34.sp,
                    lineHeight = 36.sp
                ),
                color = TextPrimary,
                modifier = Modifier.testTag("today_cuts_count")
            )
            Text(
                text = "TODAY",
                style = MaterialTheme.typography.labelSmall,
                color = if (count > 0) activeColor else TextSecondary
            )
        }
    }
}

@Composable
fun StillspaceEmptyFocusArtwork(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianBg)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .testTag("insights_empty_artwork")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.52f)
            val minDim = minOf(w, h)

            // Warm obsidian depth glow
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ObsidianGlow,
                        ObsidianBg
                    ),
                    center = center,
                    radius = maxOf(w, h) * 0.72f
                )
            )

            // Concentric Calm Sage stillness horizon rings
            drawCircle(
                color = CalmSage.copy(alpha = 0.14f),
                radius = minDim * 0.44f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = CalmSage.copy(alpha = 0.24f),
                radius = minDim * 0.31f,
                center = center,
                style = Stroke(width = 1.25.dp.toPx())
            )

            // Minimal broken ring motif in Electric Amber around a Calm Sage play triangle
            val ringRadius = minDim * 0.19f
            drawArc(
                color = ElectricAmber,
                startAngle = 32f,
                sweepAngle = 296f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Vertical pause-break bar on the right opening of the ring
            val barX = center.x + ringRadius * 0.96f
            val barHalfH = ringRadius * 0.34f
            drawLine(
                color = ElectricAmber,
                start = Offset(barX, center.y - barHalfH),
                end = Offset(barX, center.y + barHalfH),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Calm Sage play glyph inside the broken ring
            val triSize = ringRadius * 0.42f
            val triangle = Path().apply {
                moveTo(center.x - triSize * 0.55f, center.y - triSize * 0.65f)
                lineTo(center.x + triSize * 0.72f, center.y)
                lineTo(center.x - triSize * 0.55f, center.y + triSize * 0.65f)
                close()
            }
            drawPath(path = triangle, color = CalmSage)
        }
    }
}

@Composable
fun AnimatedIntCounter(
    target: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = ""
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val animatedValue by animateIntAsState(
        targetValue = target,
        animationSpec = if (reducedMotion) snap() else tween(420, easing = FastOutSlowInEasing),
        label = "animated_int_counter"
    )
    Text(
        text = "$prefix$animatedValue$suffix",
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        modifier = modifier
    )
}
