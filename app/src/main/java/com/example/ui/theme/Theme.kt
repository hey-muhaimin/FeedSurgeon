package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FeedSurgeonDarkColorScheme = darkColorScheme(
    primary = MintTeal,
    onPrimary = OnMintTeal,
    primaryContainer = MintTealDark,
    onPrimaryContainer = MintTeal,
    secondary = AmberWarning,
    onSecondary = ObsidianBg,
    tertiary = CoralDanger,
    onTertiary = TextPrimary,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = NavPillBorder,
    error = CoralDanger,
    onError = TextPrimary
)

@Composable
fun FeedSurgeonTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FeedSurgeonDarkColorScheme,
        typography = FeedSurgeonTypography,
        content = content
    )
}
