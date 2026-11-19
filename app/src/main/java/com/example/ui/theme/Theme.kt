package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StillspaceDarkColorScheme = darkColorScheme(
    primary = ElectricAmber,
    onPrimary = OnElectricAmber,
    primaryContainer = ElectricAmberDark,
    onPrimaryContainer = ElectricAmber,
    secondary = CalmSage,
    onSecondary = OnCalmSage,
    secondaryContainer = CalmSageDark,
    onSecondaryContainer = CalmSage,
    tertiary = SoftGold,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = NavPillBorder,
    error = MutedTerracotta,
    onError = TextPrimary
)

@Composable
fun FeedSurgeonTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StillspaceDarkColorScheme,
        typography = FeedSurgeonTypography,
        content = content
    )
}
