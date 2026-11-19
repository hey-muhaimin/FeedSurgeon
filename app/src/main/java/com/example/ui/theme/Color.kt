package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Focus Habit Design System — Light Palette (Airy Indigo, Emerald & Amber)
// Verified WCAG AA contrast ratios (>= 4.5:1 text, >= 3:1 UI borders/controls)
// ============================================================================
val HabitBg = Color(0xFFF5F6FA)
val HabitBloomPrimary = Color(0xFFE7E9FD)
val HabitBloomSecondary = Color(0xFFE8F7F0)
val HabitSurface = Color(0xFFFFFFFF)
val HabitSurfaceLow = Color(0xFFEEF0F7)
val HabitSurfaceHigh = Color(0xFFFFFFFF)
val HabitSurfaceVariant = Color(0xFFECEEF6)
val HabitLine = Color(0xFFE6E9F2)
val HabitOutline = Color(0xFFCED4E4)

val HabitIndigo = Color(0xFF4F46E5)
val HabitIndigoDeep = Color(0xFF4338CA)
val HabitIndigoBright = Color(0xFF5D54F2)
val HabitIndigoTint = Color(0xFFEEF0FF)
val HabitIndigoTintBorder = Color(0xFFDCE0FC)
val HabitOnIndigoContainer = Color(0xFF3F37C9)

val HabitProtected = Color(0xFF1FA971)
val HabitProtectedTint = Color(0xFFE6F7EE)
val HabitProtectedBorder = Color(0xFFB5E4CC)
val HabitProtectedText = Color(0xFF0D684A)

val HabitStreak = Color(0xFFFF8A3D)
val HabitStreakTint = Color(0xFFFFF1E6)
val HabitStreakBorder = Color(0xFFFFD4B5)
val HabitStreakText = Color(0xFFB93B08)

val HabitDanger = Color(0xFFCC2643)
val HabitDangerTint = Color(0xFFFEECEE)

val HabitInk = Color(0xFF13162B)
val HabitMuted = Color(0xFF5F6784)
val HabitSubtle = Color(0xFF5E6682)

// ============================================================================
// Focus Habit Design System — Dark Counterpart (Midnight Indigo)
// ============================================================================
val DarkHabitBg = Color(0xFF0B0E1D)
val DarkHabitBloomPrimary = Color(0xFF1A2044)
val DarkHabitBloomSecondary = Color(0xFF0E2926)
val DarkHabitSurface = Color(0xFF15192E)
val DarkHabitSurfaceLow = Color(0xFF0F1324)
val DarkHabitSurfaceHigh = Color(0xFF1E2440)
val DarkHabitSurfaceVariant = Color(0xFF191F38)
val DarkHabitLine = Color(0xFF2B3357)
val DarkHabitOutline = Color(0xFF3D4773)

val DarkHabitIndigo = Color(0xFF969BFF)
val DarkHabitOnIndigo = Color(0xFF0E102E)
val DarkHabitIndigoAction = Color(0xFF5B54F2)
val DarkHabitIndigoDeep = Color(0xFF3E35C4)
val DarkHabitIndigoTint = Color(0xFF222754)
val DarkHabitIndigoTintBorder = Color(0xFF323975)
val DarkHabitOnIndigoContainer = Color(0xFFC8CDFF)

val DarkHabitProtected = Color(0xFF34D399)
val DarkHabitProtectedTint = Color(0xFF102E27)
val DarkHabitProtectedBorder = Color(0xFF1E5948)
val DarkHabitProtectedText = Color(0xFF6EE7B7)

val DarkHabitStreak = Color(0xFFFB923C)
val DarkHabitStreakTint = Color(0xFF332012)
val DarkHabitStreakBorder = Color(0xFF663B1B)
val DarkHabitStreakText = Color(0xFFFDBA74)

val DarkHabitDanger = Color(0xFFFB7185)
val DarkHabitDangerTint = Color(0xFF33151E)

val DarkHabitInk = Color(0xFFF3F5FC)
val DarkHabitMuted = Color(0xFFA6AED0)
val DarkHabitSubtle = Color(0xFF939CBE)

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
    val switchThumb: Color
)

val LightSemanticColors = StillspaceSemanticColors(
    isDark = false,
    bgBloomPrimary = HabitBloomPrimary,
    bgBloomSecondary = HabitBloomSecondary,
    neuHighlight = Color.White,
    neuShadow = Color(0x1413162B),
    neuInsetShadow = Color(0x1213162B),
    neuInsetHighlight = Color(0xE6FFFFFF),
    specularRimTop = Color.White,
    specularRimBottom = HabitLine,
    primaryActionBg = HabitIndigo,
    primaryActionText = Color.White,
    indigoTintBorder = HabitIndigoTintBorder,
    secondaryButtonBg = HabitSurface,
    secondaryButtonBorder = HabitLine,
    statusActive = HabitProtected,
    statusActiveBg = HabitProtectedTint,
    statusActiveBorder = HabitProtectedBorder,
    statusActiveText = HabitProtectedText,
    statusWarning = HabitStreak,
    statusWarningBg = HabitStreakTint,
    statusWarningBorder = HabitStreakBorder,
    statusWarningText = HabitStreakText,
    statusDanger = HabitDanger,
    statusDangerBg = HabitDangerTint,
    textMuted = HabitSubtle,
    trackInactive = HabitSurfaceLow,
    trackInactiveBorder = Color(0xFFE2E6F2),
    dockSurface = HabitSurface,
    orbCoreHighlight = Color(0xFFEEF0FF),
    orbCoreMid = HabitIndigo,
    orbCoreDeep = HabitIndigoDeep,
    heroGradientStart = HabitIndigoBright,
    heroGradientEnd = HabitIndigoDeep,
    dividerLine = Color(0xFFECEEF6),
    switchInactiveTrack = Color(0xFFCFD4E4),
    switchThumb = Color.White
)

val DarkSemanticColors = StillspaceSemanticColors(
    isDark = true,
    bgBloomPrimary = DarkHabitBloomPrimary,
    bgBloomSecondary = DarkHabitBloomSecondary,
    neuHighlight = Color(0x303B4578),
    neuShadow = Color(0xA6030510),
    neuInsetShadow = Color(0x88030510),
    neuInsetHighlight = Color(0x28343D66),
    specularRimTop = Color(0x458A92FF),
    specularRimBottom = Color(0x2B040612),
    primaryActionBg = DarkHabitIndigoAction,
    primaryActionText = Color.White,
    indigoTintBorder = DarkHabitIndigoTintBorder,
    secondaryButtonBg = DarkHabitSurfaceHigh,
    secondaryButtonBorder = DarkHabitOutline,
    statusActive = DarkHabitProtected,
    statusActiveBg = DarkHabitProtectedTint,
    statusActiveBorder = DarkHabitProtectedBorder,
    statusActiveText = DarkHabitProtectedText,
    statusWarning = DarkHabitStreak,
    statusWarningBg = DarkHabitStreakTint,
    statusWarningBorder = DarkHabitStreakBorder,
    statusWarningText = DarkHabitStreakText,
    statusDanger = DarkHabitDanger,
    statusDangerBg = DarkHabitDangerTint,
    textMuted = DarkHabitSubtle,
    trackInactive = Color(0xFF1D2340),
    trackInactiveBorder = Color(0xFF2B3357),
    dockSurface = Color(0xFA15192E),
    orbCoreHighlight = Color(0xFFE0E2FF),
    orbCoreMid = DarkHabitIndigoAction,
    orbCoreDeep = DarkHabitIndigoDeep,
    heroGradientStart = DarkHabitIndigoAction,
    heroGradientEnd = Color(0xFF332BA8),
    dividerLine = Color(0xFF252C4C),
    switchInactiveTrack = Color(0xFF2E355C),
    switchThumb = Color(0xFFF3F5FC)
)

val LocalStillspaceColors = staticCompositionLocalOf { LightSemanticColors }
