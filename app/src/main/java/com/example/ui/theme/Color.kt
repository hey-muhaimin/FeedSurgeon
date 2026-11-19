package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Outloop — Quiet Intent Linen & Forest Palette (Light Sanctuary)
// Grounded warm ivory linen, crisp white porcelain cards, deep forest anchors,
// misty sage accents, and warm amber streak highlights.
// ============================================================================
val HabitBg = Color(0xFFFAF9F5)
val HabitLinen = Color(0xFFEAE8DF)
val HabitSurface = Color(0xFFFFFFFF)
val HabitSurfaceLow = Color(0xFFF4F4F0)
val HabitSurfaceSunken = Color(0xFFF3F1EA)
val HabitSurfaceHigh = Color(0xFFE9E8E4)
val HabitSurfaceVariant = Color(0xFFFFFFFF)
val HabitLine = Color(0xFFE1DED5)
val HabitLineSoft = Color(0xFFEAE7DF)
val HabitOutline = Color(0xFFC3C8C2)

val HabitForest = Color(0xFF29362E)
val HabitForestInk = Color(0xFF142119)
val HabitForestHover = Color(0xFF35463B)
val HabitSageTint = Color(0xFFD8E3CF)
val HabitSageSoft = Color(0xFFADC4AA)
val HabitSageBar = Color(0xFFD4DDD0)
val HabitSageText = Color(0xFF576151)

val HabitReadyBg = Color(0xFFEBF3ED)
val HabitReadyBorder = Color(0xFFC5DDD0)
val HabitReadyText = Color(0xFF1E3E2B)

val HabitAmber = Color(0xFFC58B41)
val HabitAmberGlow = Color(0xFFE5A93C)
val HabitAmberContainer = Color(0xFF472E04)
val HabitAmberText = Color(0xFFFFDDB3)
val HabitAmberWarmBg = Color(0xFFFFECD1)
val HabitAmberBorder = Color(0xFFE9C089)

val HabitDanger = Color(0xFFBA1A1A)
val HabitDangerTint = Color(0xFFFFDAD6)
val HabitDangerBorder = Color(0xFFF0C7C3)

val HabitInk = Color(0xFF1B1C1A)
val HabitMuted = Color(0xFF434844)
val HabitSubtle = Color(0xFF737874)

// Compatibility aliases mapped cleanly to Quiet Intent Linen & Forest tokens
val HabitBloomPrimary = HabitSageTint
val HabitBloomSecondary = HabitReadyBg
val HabitIndigo = HabitForest
val HabitIndigoDeep = HabitForestInk
val HabitIndigoBright = HabitForestHover
val HabitIndigoTint = HabitSageTint
val HabitIndigoTintBorder = Color(0xFFC6D4BC)
val HabitOnIndigoContainer = HabitForestInk
val HabitProtected = HabitReadyText
val HabitProtectedTint = HabitReadyBg
val HabitProtectedBorder = HabitReadyBorder
val HabitProtectedText = HabitReadyText
val HabitStreak = HabitAmber
val HabitStreakTint = HabitAmberWarmBg
val HabitStreakBorder = HabitAmberBorder
val HabitStreakText = Color(0xFF6B4409)

@Immutable
data class StillspaceSemanticColors(
    val isDark: Boolean,
    val bgBloomPrimary: Color,
    val bgBloomSecondary: Color,
    val neuHighlight: Color,
    val neuShadow: Color,
    val neuInsetShadow: Color,
    val neuInsetHighlight: Color,
    val specularRimTop: Color,
    val specularRimBottom: Color,
    val primaryActionBg: Color,
    val primaryActionText: Color,
    val indigoTintBorder: Color,
    val secondaryButtonBg: Color,
    val secondaryButtonBorder: Color,
    val statusActive: Color,
    val statusActiveBg: Color,
    val statusActiveBorder: Color,
    val statusActiveText: Color,
    val statusWarning: Color,
    val statusWarningBg: Color,
    val statusWarningBorder: Color,
    val statusWarningText: Color,
    val statusDanger: Color,
    val statusDangerBg: Color,
    val textMuted: Color,
    val trackInactive: Color,
    val trackInactiveBorder: Color,
    val dockSurface: Color,
    val orbCoreHighlight: Color,
    val orbCoreMid: Color,
    val orbCoreDeep: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val dividerLine: Color,
    val switchInactiveTrack: Color,
    val switchThumb: Color,
    // Extended Quiet Intent semantic tokens
    val forestSurface: Color,
    val forestInk: Color,
    val sageTint: Color,
    val sageText: Color,
    val amberGlow: Color,
    val amberContainer: Color,
    val amberBadgeText: Color,
    val linenSurface: Color
)

val LightSemanticColors = StillspaceSemanticColors(
    isDark = false,
    bgBloomPrimary = HabitSageTint,
    bgBloomSecondary = HabitReadyBg,
    neuHighlight = HabitSurface,
    neuShadow = Color(0x0D1B1C1A),
    neuInsetShadow = Color(0x0A1B1C1A),
    neuInsetHighlight = Color(0xD9FFFFFF),
    specularRimTop = HabitSurface,
    specularRimBottom = HabitLine,
    primaryActionBg = HabitForest,
    primaryActionText = HabitBg,
    indigoTintBorder = HabitIndigoTintBorder,
    secondaryButtonBg = HabitSurface,
    secondaryButtonBorder = HabitLine,
    statusActive = HabitReadyText,
    statusActiveBg = HabitReadyBg,
    statusActiveBorder = HabitReadyBorder,
    statusActiveText = HabitReadyText,
    statusWarning = HabitAmber,
    statusWarningBg = HabitAmberWarmBg,
    statusWarningBorder = HabitAmberBorder,
    statusWarningText = Color(0xFF6B4409),
    statusDanger = HabitDanger,
    statusDangerBg = HabitDangerTint,
    textMuted = HabitSubtle,
    trackInactive = HabitSurfaceLow,
    trackInactiveBorder = HabitLineSoft,
    dockSurface = HabitBg.copy(alpha = 0.94f),
    orbCoreHighlight = HabitSageTint,
    orbCoreMid = HabitForest,
    orbCoreDeep = HabitForestInk,
    heroGradientStart = HabitForest,
    heroGradientEnd = HabitForestInk,
    dividerLine = Color(0xFFF0EEE7),
    switchInactiveTrack = Color(0xFFE3E2DF),
    switchThumb = HabitSurface,
    forestSurface = HabitForest,
    forestInk = HabitForestInk,
    sageTint = HabitSageTint,
    sageText = HabitSageText,
    amberGlow = HabitAmberGlow,
    amberContainer = HabitAmberContainer,
    amberBadgeText = HabitAmberText,
    linenSurface = HabitLinen
)

val DarkSemanticColors = LightSemanticColors

val LocalStillspaceColors = staticCompositionLocalOf { LightSemanticColors }
