package com.example.ui.theme

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

object SurgeonMotion {
    val SpatialSpring: SpringSpec<Float> = SpringSpec(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow
    )

    val TactilePressSpring: SpringSpec<Float> = SpringSpec(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMedium
    )

    val LiquidPillSpring: SpringSpec<Float> = SpringSpec(
        dampingRatio = 0.72f,
        stiffness = Spring.StiffnessMediumLow
    )

    val RingGaugeSpring: SpringSpec<Float> = SpringSpec(
        dampingRatio = 0.80f,
        stiffness = Spring.StiffnessLow
    )
}

@Composable
fun rememberReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale <= 0f
        }.getOrDefault(false)
    }
}

fun Modifier.tactilePressPhysics(
    pressedScale: Float = 0.98f,
    enabled: Boolean = true,
    onPressedChange: ((Boolean) -> Unit)? = null
): Modifier = composed {
    val reducedMotion = rememberReducedMotionEnabled()
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed && !reducedMotion) pressedScale else 1f,
        animationSpec = if (reducedMotion) snap() else SurgeonMotion.TactilePressSpring,
        label = "tactile_press_physics_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (enabled) {
                Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitFirstDown(requireUnconsumed = false)
                            isPressed = true
                            onPressedChange?.invoke(true)
                            try {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Final)
                                    if (event.changes.all { !it.pressed } ||
                                        event.changes.any { it.isConsumed }
                                    ) {
                                        break
                                    }
                                }
                            } finally {
                                isPressed = false
                                onPressedChange?.invoke(false)
                            }
                        }
                    }
                }
            } else {
                Modifier
            }
        )
}

fun specularRimBrush(
    baseBorder: Color,
    isHighlighted: Boolean = false
): Brush {
    val topLeft = when {
        isHighlighted -> ElectricAmber.copy(alpha = 0.68f)
        baseBorder == ElectricAmberBorder -> ElectricAmber.copy(alpha = 0.52f)
        baseBorder == CalmSageBorder -> CalmSage.copy(alpha = 0.52f)
        baseBorder == AmberWarningBorder -> AmberWarning.copy(alpha = 0.55f)
        else -> SpecularHighlightTop
    }
    val mid = if (isHighlighted) ElectricAmberBorder else baseBorder
    val bottomRight = when {
        isHighlighted -> ElectricAmber.copy(alpha = 0.28f)
        baseBorder == CardBorder -> SpecularHighlightEdge
        else -> baseBorder.copy(alpha = 0.55f)
    }
    return Brush.linearGradient(
        colors = listOf(topLeft, mid, bottomRight),
        start = Offset(0f, 0f),
        end = Offset(720f, 480f)
    )
}

@Composable
fun StaggeredReveal(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reducedMotion = rememberReducedMotionEnabled()
    var visible by remember { mutableStateOf(reducedMotion) }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            visible = true
        } else {
            val delayMs = (index.coerceIn(0, 6) * 35L)
            if (delayMs > 0L) delay(delayMs)
            visible = true
        }
    }

    if (reducedMotion) {
        content()
    } else {
        AnimatedVisibility(
            visible = visible,
            modifier = modifier,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                slideInVertically(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.84f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { it / 10 }
        ) {
            content()
        }
    }
}
