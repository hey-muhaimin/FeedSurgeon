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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.SurgeonMotion
import com.example.ui.theme.neumorphicExtruded
import com.example.ui.theme.rememberReducedMotionEnabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OnboardingPhoneIllustration(
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    val floatOffset: Float
    if (reducedMotion) {
        floatOffset = 0f
    } else {
        val transition = rememberInfiniteTransition(label = "welcome_illustration_float")
        val anim by transition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floating_pills_y"
        )
        floatOffset = anim
    }

    val containerBg = if (neu.isDark) Color(0xFF171B36) else scheme.surface
    val blobColor = if (neu.isDark) Color(0xFF232854) else Color(0xFFE8EBFC)
    val phoneBody = if (neu.isDark) Color(0xFF1E2340) else Color(0xFFFFFFFF)
    val phoneStroke = if (neu.isDark) Color(0xFF8E95B8) else Color(0xFF13162B)
    val screenTint = scheme.primary
    val placeholderLine = if (neu.isDark) Color(0xFF39416B) else Color(0xFFD9DCEB)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(containerBg)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .testTag("onboarding_hero_illustration"),
        contentAlignment = Alignment.Center
    ) {
        // Pastel atmospheric background blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = blobColor,
                radius = size.height * 0.42f,
                center = Offset(size.width * 0.14f, size.height * 0.24f)
            )
            drawCircle(
                color = blobColor,
                radius = size.height * 0.36f,
                center = Offset(size.width * 0.88f, size.height * 0.82f)
            )
        }

        // Stylized Smartphone Frame + Indigo Screen with Pause Bars
        Box(
            modifier = Modifier.size(width = 122.dp, height = 186.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(phoneBody)
                    .border(2.5.dp, phoneStroke, RoundedCornerShape(22.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Indigo video feed paused tile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(neu.heroGradientStart, neu.heroGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White)
                            )
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White)
                            )
                        }
                    }

                    // Bottom feed skeleton lines
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(placeholderLine)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.38f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(placeholderLine)
                        )
                    }
                }
            }

            // Green check badge overlapping bottom-right of phone frame
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = (-8).dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(phoneBody)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(neu.statusActive),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Top-left floating pill: "Reels paused"
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 18.dp, y = (54f + floatOffset).dp)
                .neumorphicExtruded(
                    cornerRadius = 100.dp,
                    elevation = 5.dp,
                    containerColor = scheme.surface,
                    borderColor = scheme.outlineVariant.copy(alpha = 0.7f)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Reels paused",
                style = MaterialTheme.typography.labelMedium,
                color = screenTint
            )
        }

        // Bottom-right floating pill: "Shorts paused"
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-56f - floatOffset).dp)
                .neumorphicExtruded(
                    cornerRadius = 100.dp,
                    elevation = 5.dp,
                    containerColor = scheme.surface,
                    borderColor = scheme.outlineVariant.copy(alpha = 0.7f)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Shorts paused",
                style = MaterialTheme.typography.labelMedium,
                color = screenTint
            )
        }
    }
}

@Composable
fun OnboardingReadyCelebrationBadge(
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    var appeared by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(Unit) {
        appeared = true
    }

    val badgeScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.76f,
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
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
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
        // Outer soft emerald halo
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(neu.statusActiveBg),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(neu.statusActive),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Playful celebratory accent dots
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 18.dp, y = (16f + dotPulse).dp)
                .size(9.dp)
                .clip(CircleShape)
                .background(neu.statusWarning)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-14).dp, y = (10f - dotPulse).dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(scheme.primary)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 12.dp, y = (-18f - dotPulse).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(scheme.primary)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-20f + dotPulse).dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFF472B6))
        )
    }
}

@Composable
fun LiquidBreathingOrb(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String = "liquid_breathing_orb"
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

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

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.26f + 0.08f * pulse),
                        scheme.secondary.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 0.98f
                ),
                radius = maxRadius * 0.98f,
                center = center
            )

            for (i in 0 until 3) {
                val progress = (ripplePhase + i / 3f) % 1f
                val rippleRadius = baseCoreRadius + (maxRadius * 0.92f - baseCoreRadius) * progress
                val alpha = (1f - progress) * (1f - progress) * 0.45f
                if (alpha > 0.01f) {
                    val rippleColor = if (i == 1) scheme.secondary else accentColor
                    drawCircle(
                        color = rippleColor.copy(alpha = alpha),
                        radius = rippleRadius,
                        center = center,
                        style = Stroke(width = (1.5.dp.toPx()) * (1f - 0.35f * progress))
                    )
                }
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neu.orbCoreHighlight,
                        accentColor,
                        neu.orbCoreDeep
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

            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f),
                        accentColor.copy(alpha = 0.48f),
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

    val primarySweepColor = if (isOperational) scheme.primary else neu.statusWarning
    val secondarySweepColor = if (isOperational) neu.statusActive else scheme.tertiary
    val activeGlow = if (isOperational) {
        scheme.primary.copy(alpha = if (neu.isDark) 0.30f else 0.18f)
    } else {
        neu.statusWarning.copy(alpha = 0.20f)
    }

    Box(
        modifier = modifier
            .size(ringSize)
            .clip(CircleShape)
            .background(scheme.surfaceContainerLow.copy(alpha = 0.65f))
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.7f), CircleShape)
            .semantics(mergeDescendants = false) {},
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 8.dp.toPx()
            val glowStrokePx = 14.dp.toPx()
            val inset = glowStrokePx / 2f + 4.dp.toPx()
            val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            val startAngle = 140f
            val totalSweep = 260f

            drawArc(
                color = neu.trackInactive,
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
                    color = activeGlow,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = glowStrokePx, cap = StrokeCap.Round)
                )

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
                    color = primarySweepColor.copy(alpha = 0.35f),
                    radius = strokePx * 0.9f,
                    center = tipOffset
                )
                drawCircle(
                    color = Color.White,
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
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.primaryContainer.copy(alpha = 0.55f))
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(16.dp))
            .testTag("insights_empty_artwork")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.52f)
            val minDim = minOf(w, h)

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neu.bgBloomPrimary,
                        scheme.surfaceContainerLow
                    ),
                    center = center,
                    radius = maxOf(w, h) * 0.72f
                )
            )

            drawCircle(
                color = scheme.primary.copy(alpha = 0.14f),
                radius = minDim * 0.44f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = neu.statusActive.copy(alpha = 0.25f),
                radius = minDim * 0.31f,
                center = center,
                style = Stroke(width = 1.25.dp.toPx())
            )

            val ringRadius = minDim * 0.19f
            drawArc(
                color = scheme.primary,
                startAngle = 32f,
                sweepAngle = 296f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
            )

            val barX = center.x + ringRadius * 0.96f
            val barHalfH = ringRadius * 0.34f
            drawLine(
                color = scheme.primary,
                start = Offset(barX, center.y - barHalfH),
                end = Offset(barX, center.y + barHalfH),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            val triSize = ringRadius * 0.42f
            val triangle = Path().apply {
                moveTo(center.x - triSize * 0.55f, center.y - triSize * 0.65f)
                lineTo(center.x + triSize * 0.72f, center.y)
                lineTo(center.x - triSize * 0.55f, center.y + triSize * 0.65f)
                close()
            }
            drawPath(path = triangle, color = neu.statusActive)
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
