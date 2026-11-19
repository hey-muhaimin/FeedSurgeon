package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HabitAmber
import com.example.ui.theme.HabitAmberGlow
import com.example.ui.theme.HabitBg
import com.example.ui.theme.HabitForest
import com.example.ui.theme.HabitForestInk
import com.example.ui.theme.HabitLine
import com.example.ui.theme.HabitReadyBg
import com.example.ui.theme.HabitReadyText
import com.example.ui.theme.HabitSageSoft
import com.example.ui.theme.HabitSageTint
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.SurgeonMotion
import com.example.ui.theme.neumorphicExtruded
import com.example.ui.theme.rememberReducedMotionEnabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OutloopEmblemMark(
    modifier: Modifier = Modifier,
    boxSize: Dp = 46.dp,
    cornerRadius: Dp = 14.dp,
    containerColor: Color = HabitForest,
    ringColor: Color = HabitBg,
    dotColor: Color = HabitAmberGlow,
    innerDotColor: Color = HabitSageSoft.copy(alpha = 0.45f)
) {
    Box(
        modifier = modifier
            .size(boxSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.62f)) {
            val radius = size.minDimension * 0.44f
            val strokePx = size.minDimension * 0.14f
            val center = Offset(size.width / 2f, size.height / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            drawArc(
                color = ringColor,
                startAngle = -15f,
                sweepAngle = 298f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawCircle(
                color = innerDotColor,
                radius = strokePx * 0.7f,
                center = center
            )
            val dotAngleRad = (-48f * (PI / 180.0))
            val dotCenter = Offset(
                x = center.x + (radius * cos(dotAngleRad)).toFloat(),
                y = center.y + (radius * sin(dotAngleRad)).toFloat()
            )
            drawCircle(
                color = dotColor,
                radius = strokePx * 0.72f,
                center = dotCenter
            )
        }
    }
}

@Composable
fun OnboardingPhoneIllustration(
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val scheme = MaterialTheme.colorScheme

    val floatOffset: Float
    if (reducedMotion) {
        floatOffset = 0f
    } else {
        val transition = rememberInfiniteTransition(label = "welcome_illustration_float")
        val anim by transition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floating_pills_y"
        )
        floatOffset = anim
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(216.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF3F0E8), Color(0xFFEAE5DB))
                )
            )
            .border(1.dp, HabitLine, RoundedCornerShape(24.dp))
            .testTag("onboarding_hero_illustration"),
        contentAlignment = Alignment.Center
    ) {
        // Warm soft concentric halo arcs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = size.minDimension * 0.44f,
                center = center
            )
            drawCircle(
                color = HabitSageTint.copy(alpha = 0.48f),
                radius = size.minDimension * 0.36f,
                center = center,
                style = Stroke(width = 1.25.dp.toPx())
            )
        }

        // Centered Deep Forest Open Loop ceramic badge
        OutloopEmblemMark(
            boxSize = 96.dp,
            cornerRadius = 26.dp,
            containerColor = HabitForest,
            ringColor = HabitBg,
            dotColor = HabitAmberGlow,
            innerDotColor = HabitSageSoft.copy(alpha = 0.55f)
        )

        // Top-left floating pill: "Reels paused"
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 18.dp, y = (26f + floatOffset).dp)
                .neumorphicExtruded(
                    cornerRadius = 100.dp,
                    containerColor = HabitSurface,
                    borderColor = HabitLine
                )
                .padding(horizontal = 13.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(HabitReadyText)
            )
            Text(
                text = "Reels paused",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurface
            )
        }

        // Bottom-right floating pill: "Shorts paused"
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-18).dp, y = (-24f - floatOffset).dp)
                .neumorphicExtruded(
                    cornerRadius = 100.dp,
                    containerColor = HabitSurface,
                    borderColor = HabitLine
                )
                .padding(horizontal = 13.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(HabitAmber)
            )
            Text(
                text = "Shorts paused",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurface
            )
        }
    }
}

@Composable
fun OnboardingReadyCelebrationBadge(
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotionEnabled()

    var appeared by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(Unit) {
        appeared = true
    }

    val badgeScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.78f,
        animationSpec = if (reducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ready_badge_scale"
    )

    val dotPulse: Float
    if (reducedMotion) {
        dotPulse = 0f
    } else {
        val transition = rememberInfiniteTransition(label = "ready_dots")
        val anim by transition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ready_dots_float"
        )
        dotPulse = anim
    }

    Box(
        modifier = modifier
            .size(132.dp)
            .scale(badgeScale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFECE5)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(HabitSageTint),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(HabitForest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFFA8D3B0),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 18.dp, y = (16f + dotPulse).dp)
                .size(9.dp)
                .clip(CircleShape)
                .background(HabitAmberGlow)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-14).dp, y = (12f - dotPulse).dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(HabitSageSoft)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-14).dp, y = (-16f + dotPulse).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(HabitReadyText)
        )
    }
}

@Composable
fun LiquidBreathingOrb(
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    accentColor: Color = HabitForest,
    cycleSeconds: Int = 4,
    testTag: String = "liquid_breathing_orb"
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val halfCycleMs = (cycleSeconds.coerceIn(2, 4) * 1000)

    val pulse: Float
    if (reducedMotion) {
        pulse = 0.5f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "liquid_orb_transition_$cycleSeconds")
        val animatedPulse by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = halfCycleMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_breath_pulse"
        )
        pulse = animatedPulse
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
            val ringRadius = maxRadius * (0.64f + 0.14f * pulse)

            // Outer Sage breath halo
            drawCircle(
                color = HabitSageTint.copy(alpha = 0.45f + 0.25f * pulse),
                radius = maxRadius * (0.86f + 0.10f * pulse),
                center = center
            )

            // Inner porcelain circle
            drawCircle(
                color = HabitSurface,
                radius = ringRadius,
                center = center
            )

            // Open Loop arc stroke
            val strokePx = (size.toPx() * 0.085f).coerceAtLeast(3.dp.toPx())
            drawArc(
                color = accentColor,
                startAngle = -14f,
                sweepAngle = 298f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Warm Amber focal anchor dot at gap entrance
            val dotRad = (-46f * (PI / 180.0))
            val dotCenter = Offset(
                x = center.x + (ringRadius * cos(dotRad)).toFloat(),
                y = center.y + (ringRadius * sin(dotRad)).toFloat()
            )
            drawCircle(
                color = HabitAmberGlow,
                radius = strokePx * 0.65f,
                center = dotCenter
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
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
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

    val primarySweepColor = if (isOperational) HabitForest else HabitAmber
    val secondarySweepColor = if (isOperational) HabitSageSoft else HabitAmberGlow

    Box(
        modifier = modifier
            .size(ringSize)
            .clip(CircleShape)
            .background(scheme.surfaceContainerLow)
            .border(1.dp, HabitLine, CircleShape)
            .semantics(mergeDescendants = false) {},
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 8.dp.toPx()
            val inset = strokePx / 2f + 6.dp.toPx()
            val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            val startAngle = 140f
            val totalSweep = 260f

            drawArc(
                color = neu.trackInactiveBorder,
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
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(secondarySweepColor, primarySweepColor),
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

                val endAngleRad = (startAngle + activeSweep) * (PI / 180.0)
                val arcRadius = arcSize.width / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val tipOffset = Offset(
                    x = center.x + (arcRadius * cos(endAngleRad)).toFloat(),
                    y = center.y + (arcRadius * sin(endAngleRad)).toFloat()
                )
                drawCircle(
                    color = HabitAmberGlow,
                    radius = strokePx * 0.45f,
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
                    fontSize = 32.sp,
                    lineHeight = 34.sp
                ),
                color = scheme.onSurface,
                modifier = Modifier.testTag("today_cuts_count")
            )
            Text(
                text = "TODAY",
                style = MaterialTheme.typography.labelSmall,
                color = if (count > 0) primarySweepColor else scheme.onSurfaceVariant
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
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF3F0E8))
            .border(1.dp, HabitLine, RoundedCornerShape(18.dp))
            .testTag("insights_empty_artwork"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minDim = size.minDimension
            drawCircle(
                color = HabitSageTint.copy(alpha = 0.65f),
                radius = minDim * 0.36f,
                center = center
            )
            val ringRadius = minDim * 0.22f
            drawArc(
                color = HabitForest,
                startAngle = -15f,
                sweepAngle = 298f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
            val dotAngle = (-46f * (PI / 180.0))
            drawCircle(
                color = HabitAmberGlow,
                radius = 3.dp.toPx(),
                center = Offset(
                    center.x + (ringRadius * cos(dotAngle)).toFloat(),
                    center.y + (ringRadius * sin(dotAngle)).toFloat()
                )
            )
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
