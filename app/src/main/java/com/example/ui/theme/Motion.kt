package com.example.ui.theme

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

fun specularRimBrush(
    baseBorder: Color,
    isHighlighted: Boolean = false
): Brush {
    val topLeft = when {
        isHighlighted -> MintTeal.copy(alpha = 0.68f)
        baseBorder == MintTealBorder -> MintTeal.copy(alpha = 0.50f)
        baseBorder == AmberWarningBorder -> AmberWarning.copy(alpha = 0.55f)
        else -> SpecularHighlightTop
    }
    val mid = if (isHighlighted) MintTealBorder else baseBorder
    val bottomRight = when {
        isHighlighted -> MintTeal.copy(alpha = 0.28f)
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
