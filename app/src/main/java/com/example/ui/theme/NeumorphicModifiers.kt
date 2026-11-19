package com.example.ui.theme

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Airy two-layer elevated surface:
 * Combines a crisp 1.dp outline border with a soft diffused ambient + contact drop shadow,
 * plus an optional directional highlight on dark surfaces or indigo-tinted glow on primary CTAs.
 */
fun Modifier.neumorphicExtruded(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    isPressed: Boolean = false,
    isHighlighted: Boolean = false,
    containerColor: Color? = null,
    borderColor: Color? = null,
    shadowTint: Color? = null
): Modifier = composed {
    val neu = StillspaceTheme.colors
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveContainer = containerColor ?: scheme.surfaceContainer
    val effectiveBorder = borderColor ?: scheme.outlineVariant
    val activeElevation = if (isPressed) (elevation * 0.4f) else elevation
    val baseShadow = shadowTint ?: neu.neuShadow

    this
        .drawBehind {
            val radiusPx = cornerRadius.toPx()
            val dropOffsetPx = (activeElevation * 0.65f).toPx()
            val ambientBlurPx = (activeElevation * 2.2f).toPx()
            val contactBlurPx = (activeElevation * 0.75f).toPx()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && ambientBlurPx > 0.5f) {
                drawIntoCanvas { canvas ->
                    // Layer 1: Wide diffused ambient shadow
                    val ambientPaint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = baseShadow.toArgb()
                            maskFilter = BlurMaskFilter(ambientBlurPx, BlurMaskFilter.Blur.NORMAL)
                        }
                    }
                    canvas.drawRoundRect(
                        left = 0f,
                        top = dropOffsetPx,
                        right = size.width,
                        bottom = size.height + dropOffsetPx,
                        radiusX = radiusPx,
                        radiusY = radiusPx,
                        paint = ambientPaint
                    )

                    // Layer 2: Tighter contact shadow underneath
                    val contactPaint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = baseShadow.copy(alpha = (baseShadow.alpha * 0.75f)).toArgb()
                            maskFilter = BlurMaskFilter(contactBlurPx.coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
                        }
                    }
                    canvas.drawRoundRect(
                        left = 1.dp.toPx(),
                        top = (dropOffsetPx * 0.35f),
                        right = size.width - 1.dp.toPx(),
                        bottom = size.height + (dropOffsetPx * 0.4f),
                        radiusX = radiusPx,
                        radiusY = radiusPx,
                        paint = contactPaint
                    )
                }
            } else {
                drawRoundRect(
                    color = baseShadow.copy(alpha = baseShadow.alpha * 0.65f),
                    topLeft = Offset(0f, dropOffsetPx * 0.55f),
                    size = size,
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
            }
        }
        .clip(shape)
        .background(effectiveContainer, shape)
        .then(
            if (effectiveBorder.alpha > 0.01f) {
                val rim = if (neu.isDark || isHighlighted) {
                    specularRimBrush(
                        baseBorder = effectiveBorder,
                        highlightTop = if (isHighlighted) scheme.primary.copy(alpha = 0.55f) else neu.specularRimTop,
                        highlightBottom = neu.specularRimBottom,
                        isHighlighted = isHighlighted || isPressed
                    )
                } else {
                    null
                }
                if (rim != null) {
                    Modifier.border(1.dp, rim, shape)
                } else {
                    Modifier.border(1.dp, effectiveBorder, shape)
                }
            } else {
                Modifier
            }
        )
}

/**
 * Clean recessed trough / well used for segmented tracks, bar chart channels, and summary wells.
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
    val effectiveBorder = borderColor ?: if (neu.isDark) {
        scheme.outlineVariant.copy(alpha = 0.65f)
    } else {
        neu.trackInactiveBorder.copy(alpha = 0.8f)
    }

    this
        .clip(shape)
        .background(effectiveContainer, shape)
        .border(1.dp, effectiveBorder, shape)
}
