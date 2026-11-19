package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode

private val StillspaceLightColorScheme = lightColorScheme(
    primary = HabitIndigo,
    onPrimary = Color.White,
    primaryContainer = HabitIndigoTint,
    onPrimaryContainer = HabitOnIndigoContainer,
    secondary = HabitProtected,
    onSecondary = Color.White,
    secondaryContainer = HabitProtectedTint,
    onSecondaryContainer = HabitProtectedText,
    tertiary = HabitStreak,
    onTertiary = Color.White,
    tertiaryContainer = HabitStreakTint,
    onTertiaryContainer = HabitStreakText,
    background = HabitBg,
    onBackground = HabitInk,
    surface = HabitSurface,
    onSurface = HabitInk,
    surfaceVariant = HabitSurfaceVariant,
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

private val StillspaceDarkColorScheme = darkColorScheme(
    primary = DarkHabitIndigo,
    onPrimary = DarkHabitOnIndigo,
    primaryContainer = DarkHabitIndigoTint,
    onPrimaryContainer = DarkHabitOnIndigoContainer,
    secondary = DarkHabitProtected,
    onSecondary = Color(0xFF04261B),
    secondaryContainer = DarkHabitProtectedTint,
    onSecondaryContainer = DarkHabitProtectedText,
    tertiary = DarkHabitStreak,
    onTertiary = Color(0xFF2B1404),
    tertiaryContainer = DarkHabitStreakTint,
    onTertiaryContainer = DarkHabitStreakText,
    background = DarkHabitBg,
    onBackground = DarkHabitInk,
    surface = DarkHabitSurface,
    onSurface = DarkHabitInk,
    surfaceVariant = DarkHabitSurfaceVariant,
    onSurfaceVariant = DarkHabitMuted,
    surfaceContainer = DarkHabitSurface,
    surfaceContainerLow = DarkHabitSurfaceLow,
    surfaceContainerHigh = DarkHabitSurfaceHigh,
    surfaceContainerHighest = Color(0xFF252B4D),
    outline = DarkHabitOutline,
    outlineVariant = DarkHabitLine,
    error = DarkHabitDanger,
    onError = Color(0xFF2B0710),
    errorContainer = DarkHabitDangerTint,
    onErrorContainer = DarkHabitDanger
)

val StillspaceShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object StillspaceTheme {
    val colors: StillspaceSemanticColors
        @Composable
        get() = LocalStillspaceColors.current
}

@Composable
fun resolveDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun FeedSurgeonTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val isDark = resolveDarkTheme(themeMode)
    val colorScheme = if (isDark) StillspaceDarkColorScheme else StillspaceLightColorScheme
    val semanticColors = if (isDark) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalStillspaceColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FeedSurgeonTypography,
            shapes = StillspaceShapes,
            content = content
        )
    }
}
