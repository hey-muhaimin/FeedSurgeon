package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Quiet Intent tonal card modifier:
 * Pure porcelain/linen surface elevation bounded by a crisp 1.dp Stone hairline border
 * without heavy drop shadows.
 */
fun Modifier.neumorphicExtruded(
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 0.dp,
    isPressed: Boolean = false,
    isHighlighted: Boolean = false,
    containerColor: Color? = null,
    borderColor: Color? = null,
    shadowTint: Color? = null
): Modifier = composed {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveContainer = containerColor ?: scheme.surfaceContainer
    val effectiveBorder = borderColor ?: scheme.outlineVariant
    val resolvedBorder = if (isHighlighted && effectiveBorder.alpha > 0.01f) {
        HabitOutline
    } else {
        effectiveBorder
    }

    this
        .clip(shape)
        .background(effectiveContainer, shape)
        .then(
            if (resolvedBorder.alpha > 0.01f) {
                Modifier.border(1.dp, resolvedBorder, shape)
            } else {
                Modifier
            }
        )
}

/**
 * Quiet Intent recessed well (#F4F4F0 / #F3F1EA) with optional 1.dp warm hairline border.
 */
fun Modifier.neumorphicInset(
    cornerRadius: Dp = 14.dp,
    containerColor: Color? = null,
    borderColor: Color? = null
): Modifier = composed {
    val neu = StillspaceTheme.colors
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveContainer = containerColor ?: scheme.surfaceContainerLow
    val effectiveBorder = borderColor ?: neu.trackInactiveBorder

    this
        .clip(shape)
        .background(effectiveContainer, shape)
        .then(
            if (effectiveBorder.alpha > 0.01f) {
                Modifier.border(1.dp, effectiveBorder, shape)
            } else {
                Modifier
            }
        )
}
