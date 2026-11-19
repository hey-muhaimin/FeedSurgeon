package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode

private val OutloopLightColorScheme = lightColorScheme(
    primary = HabitForest,
    onPrimary = HabitBg,
    primaryContainer = HabitSageTint,
    onPrimaryContainer = HabitForestInk,
    secondary = HabitSageText,
    onSecondary = Color.White,
    secondaryContainer = HabitSageTint,
    onSecondaryContainer = HabitForestInk,
    tertiary = HabitAmber,
    onTertiary = Color.White,
    tertiaryContainer = HabitAmberContainer,
    onTertiaryContainer = HabitAmberText,
    background = HabitBg,
    onBackground = HabitInk,
    surface = HabitBg,
    onSurface = HabitInk,
    surfaceVariant = HabitSurface,
    onSurfaceVariant = HabitMuted,
    surfaceContainer = HabitSurface,
    surfaceContainerLow = HabitSurfaceLow,
    surfaceContainerHigh = HabitSurfaceHigh,
    surfaceContainerHighest = Color.White,
    outline = HabitOutline,
    outlineVariant = HabitLine,
    error = HabitDanger,
    onError = Color.White,
    errorContainer = HabitDangerTint,
    onErrorContainer = HabitDanger
)

val StillspaceShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

object StillspaceTheme {
    val colors: StillspaceSemanticColors
        @Composable
        get() = LocalStillspaceColors.current
}

@Composable
fun resolveDarkTheme(themeMode: ThemeMode): Boolean = false

@Composable
fun FeedSurgeonTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalStillspaceColors provides LightSemanticColors) {
        MaterialTheme(
            colorScheme = OutloopLightColorScheme,
            typography = FeedSurgeonTypography,
            shapes = StillspaceShapes,
            content = content
        )
    }
}
