package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.InterventionToastState
import kotlinx.coroutines.delay
import java.util.Locale
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.AmberWarningBorder
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CalmSageBorder
import com.example.ui.theme.CalmSageDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DockGlassSurface
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.ElectricAmberBorder
import com.example.ui.theme.ElectricAmberDark
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.NavPillBorder
import com.example.ui.theme.NavPillSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianGlow
import com.example.ui.theme.OnElectricAmber
import com.example.ui.theme.SurgeonMotion
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.rememberReducedMotionEnabled
import com.example.ui.theme.specularRimBrush
import com.example.ui.theme.tactilePressPhysics

enum class BottomTab(val label: String) {
    HOME("Home"),
    INSIGHTS("Insights"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
private val SurgeonBoundsTransform = BoundsTransform { _, _ ->
    tween(durationMillis = 320, easing = FastOutSlowInEasing)
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.surgeonSharedElement(key: String): Modifier = composed {
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalAnimatedVisibilityScope.current
    if (sharedScope != null && animScope != null) {
        with(sharedScope) {
            this@composed.sharedElement(
                sharedScope.rememberSharedContentState(key = key),
                animatedVisibilityScope = animScope,
                boundsTransform = SurgeonBoundsTransform
            )
        }
    } else {
        this
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.surgeonSharedBounds(key: String): Modifier = composed {
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalAnimatedVisibilityScope.current
    if (sharedScope != null && animScope != null) {
        with(sharedScope) {
            this@composed.sharedBounds(
                sharedScope.rememberSharedContentState(key = key),
                animatedVisibilityScope = animScope,
                boundsTransform = SurgeonBoundsTransform
            )
        }
    } else {
        this
    }
}

@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(ObsidianGlow.copy(alpha = 0.85f), Color.Transparent),
                        center = Offset(size.width * 0.82f, size.height * 0.04f),
                        radius = size.width * 0.95f
                    )
                )
            }
    ) {
        content()
    }
}

@Composable
fun SurgeonCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    containerColor: Color = GlassSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var pointerPressed by remember { mutableStateOf(false) }

    val rimBrush = remember(borderColor, pointerPressed, isHovered) {
        specularRimBrush(
            baseBorder = borderColor,
            isHighlighted = pointerPressed || isHovered
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .tactilePressPhysics(
                pressedScale = 0.98f,
                enabled = true,
                onPressedChange = { pointerPressed = it }
            )
            .clip(shape)
            .hoverable(interactionSource = interactionSource)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, rimBrush)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun StatusBadgePill(
    text: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isWarning) AmberWarningBorder else CalmSageBorder
    val bgColor = if (isWarning) AmberWarningBg else CalmSageDark.copy(alpha = 0.50f)
    val textColor = if (isWarning) AmberWarning else CalmSage
    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clip(RoundedCornerShape(100.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun AppBadgeIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 44.dp
) {
    val logoRes = when (packageName) {
        "com.google.android.youtube" -> R.drawable.ic_logo_youtube
        "com.instagram.android" -> R.drawable.ic_logo_instagram
        "com.facebook.katana" -> R.drawable.ic_logo_facebook
        "com.snapchat.android" -> R.drawable.ic_logo_snapchat
        "com.reddit.frontpage" -> R.drawable.ic_logo_reddit
        else -> null
    }
    val accentColor = when (packageName) {
        "com.google.android.youtube" -> Color(0xFFFF0000)
        "com.instagram.android" -> Color(0xFFD62976)
        "com.facebook.katana" -> Color(0xFF1877F2)
        "com.snapchat.android" -> Color(0xFFFFFC00)
        "com.reddit.frontpage" -> Color(0xFFFF4500)
        else -> TextPrimary
    }
    val cornerRadius = (badgeSize * 0.28f)
    val hasBrandTint = accentColor != TextPrimary
    val bgColor = if (hasBrandTint) accentColor.copy(alpha = 0.12f) else CardSurfaceElevated
    val borderColor = if (hasBrandTint) accentColor.copy(alpha = 0.34f) else CardBorder

    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (logoRes != null) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                modifier = Modifier
                    .size(badgeSize * 0.58f)
                    .testTag("app_logo_$packageName")
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(badgeSize * 0.46f)
            )
        }
    }
}

@Composable
fun SurgeonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val haptics = LocalHapticFeedback.current
    Switch(
        checked = checked,
        onCheckedChange = { newValue ->
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onCheckedChange(newValue)
        },
        modifier = Modifier.testTag(testTag),
        colors = SwitchDefaults.colors(
            checkedThumbColor = OnElectricAmber,
            checkedTrackColor = ElectricAmber,
            checkedBorderColor = ElectricAmber,
            uncheckedThumbColor = TextSecondary,
            uncheckedTrackColor = CardSurfaceElevated,
            uncheckedBorderColor = CardBorder
        )
    )
}

@Composable
fun rememberCountdownLabel(
    untilMs: Long,
    onExpired: () -> Unit = {}
): String? {
    if (untilMs == Long.MAX_VALUE) return "Paused until resumed"
    if (untilMs <= 0L) return null

    var nowMs by remember(untilMs) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(untilMs) {
        while (true) {
            val current = System.currentTimeMillis()
            nowMs = current
            if (current >= untilMs) {
                onExpired()
                break
            }
            delay(1000L)
        }
    }

    val remainingMs = (untilMs - nowMs).coerceAtLeast(0L)
    if (remainingMs <= 0L) return null

    val totalSeconds = (remainingMs / 1000L).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%dh %02dm", hours, minutes)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

@Composable
fun MindfulPausePreviewDialog(
    appName: String = "YouTube",
    feedLabel: String = "Shorts",
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var demoOutcome by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        SurgeonCard(
            borderColor = CalmSageBorder,
            containerColor = GlassSurfaceElevated,
            modifier = Modifier.testTag("mindful_pause_preview_dialog")
        ) {
            LiquidShieldOverlay(
                orbSize = 86.dp,
                showObsidianBackdrop = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("liquid_breathing_orb")
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "MINDFUL PAUSE \u00B7 PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = CalmSage,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Take a quick breath",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$appName $feedLabel opened. Step back to protect your focus, or allow 5 minutes for this visit.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (demoOutcome != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CalmSageDark.copy(alpha = 0.65f))
                        .border(1.dp, CalmSageBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = demoOutcome.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CalmSage
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    demoOutcome = "Focus win! In $appName, this exits $feedLabel right back to your main feed."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("preview_leave_feed_button"),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricAmber,
                    contentColor = OnElectricAmber
                )
            ) {
                Text(
                    text = "Leave $feedLabel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    demoOutcome = "Got it — $feedLabel stays open for 5 minutes, then protection turns back on automatically."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("preview_allow_5m_button"),
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardSurfaceElevated,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = "Allow 5 min (this visit)",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preview_close_button")
            ) {
                Text(
                    text = "Close preview",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun CircularBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .testTag("back_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
    }
}

@Composable
fun InterventionBanner(
    toast: InterventionToastState,
    onAllowOnce: () -> Unit,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("intervention_banner"),
        shape = RoundedCornerShape(18.dp),
        color = DockGlassSurface,
        border = BorderStroke(1.dp, specularRimBrush(CardBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CoralDanger)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = toast.message,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onAllowOnce,
                modifier = Modifier.testTag("banner_allow_once")
            ) {
                Text(
                    text = "5m pass (visit)",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
            TextButton(
                onClick = onUndo,
                modifier = Modifier.testTag("banner_undo")
            ) {
                Text(
                    text = "Undo",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricAmber
                )
            }
        }
    }
}

@Composable
fun SurgeonScreenHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailingContent()
        }
    }
}

@Composable
fun FloatingPillNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    alertTabs: Set<BottomTab> = emptySet()
) {
    val haptics = LocalHapticFeedback.current
    val reducedMotion = rememberReducedMotionEnabled()
    val tabs = BottomTab.entries
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    var previousIndex by remember { mutableIntStateOf(selectedIndex) }
    val pillStretch = remember { Animatable(0f) }

    LaunchedEffect(selectedIndex, reducedMotion) {
        if (!reducedMotion && previousIndex != selectedIndex) {
            pillStretch.animateTo(1f, animationSpec = tween(durationMillis = 85))
            pillStretch.animateTo(0f, animationSpec = SurgeonMotion.LiquidPillSpring)
        } else {
            pillStretch.snapTo(0f)
        }
        previousIndex = selectedIndex
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 276.dp, max = 352.dp)
                .fillMaxWidth()
                .testTag("compact_floating_dock"),
            shape = RoundedCornerShape(30.dp),
            color = DockGlassSurface,
            border = BorderStroke(1.dp, specularRimBrush(NavPillBorder))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                val tabWidth = maxWidth / tabs.size
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedIndex,
                    animationSpec = if (reducedMotion) {
                        snap()
                    } else {
                        spring(
                            dampingRatio = 0.78f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    },
                    label = "compact_dock_sliding_pill"
                )

                // Continuous sliding active pill with subtle liquid horizontal stretch during transitions
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .height(54.dp)
                        .graphicsLayer {
                            scaleX = 1f + 0.14f * pillStretch.value
                            scaleY = 1f - 0.04f * pillStretch.value
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(ElectricAmberDark.copy(alpha = 0.92f))
                        .border(
                            1.dp,
                            specularRimBrush(ElectricAmberBorder, isHighlighted = true),
                            RoundedCornerShape(24.dp)
                        )
                        .testTag("nav_sliding_pill")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val hasAlert = tab in alertTabs
                        val icon = when (tab) {
                            BottomTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                            BottomTab.INSIGHTS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                            BottomTab.SETTINGS -> if (isSelected) Icons.Filled.Tune else Icons.Outlined.Tune
                        }
                        val tint by animateColorAsState(
                            targetValue = if (isSelected) ElectricAmber else TextSecondary,
                            animationSpec = tween(durationMillis = 200),
                            label = "dock_tint_${tab.name}"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "dock_scale_${tab.name}"
                        )

                        Column(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(54.dp)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    if (!isSelected) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onTabSelected(tab)
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .testTag("nav_tab_${tab.name.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label,
                                    tint = tint,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(iconScale)
                                )
                                if (hasAlert) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 4.dp, y = (-2).dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AmberWarning)
                                            .border(1.5.dp, NavPillSurface, CircleShape)
                                            .testTag("nav_alert_${tab.name.lowercase()}")
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 0.2.sp
                                ),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObsidianSideRail(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    alertTabs: Set<BottomTab> = emptySet()
) {
    val haptics = LocalHapticFeedback.current
    val reducedMotion = rememberReducedMotionEnabled()
    val tabs = BottomTab.entries
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    var previousIndex by remember { mutableIntStateOf(selectedIndex) }
    val railStretch = remember { Animatable(0f) }

    LaunchedEffect(selectedIndex, reducedMotion) {
        if (!reducedMotion && previousIndex != selectedIndex) {
            railStretch.animateTo(1f, animationSpec = tween(durationMillis = 85))
            railStretch.animateTo(0f, animationSpec = SurgeonMotion.LiquidPillSpring)
        } else {
            railStretch.snapTo(0f)
        }
        previousIndex = selectedIndex
    }

    val itemHeight = 68.dp
    val itemGap = 8.dp

    Box(
        modifier = modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .windowInsetsPadding(WindowInsets.displayCutout)
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .width(88.dp)
                .fillMaxHeight()
                .testTag("obsidian_side_rail"),
            shape = RoundedCornerShape(28.dp),
            color = DockGlassSurface,
            border = BorderStroke(1.dp, specularRimBrush(NavPillBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stillspace geometric broken-ring brand emblem
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElectricAmberDark.copy(alpha = 0.72f))
                        .border(1.dp, specularRimBrush(ElectricAmberBorder), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_notification),
                        contentDescription = "Stillspace",
                        tint = ElectricAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    val indicatorOffset by animateDpAsState(
                        targetValue = (itemHeight + itemGap) * selectedIndex,
                        animationSpec = if (reducedMotion) {
                            snap()
                        } else {
                            spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        },
                        label = "rail_sliding_pill"
                    )

                    Box(
                        modifier = Modifier
                            .offset(y = indicatorOffset)
                            .fillMaxWidth()
                            .height(itemHeight)
                            .graphicsLayer {
                                scaleY = 1f + 0.14f * railStretch.value
                                scaleX = 1f - 0.04f * railStretch.value
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(ElectricAmberDark.copy(alpha = 0.92f))
                            .border(
                                1.dp,
                                specularRimBrush(ElectricAmberBorder, isHighlighted = true),
                                RoundedCornerShape(20.dp)
                            )
                            .testTag("rail_sliding_pill")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(itemGap)
                    ) {
                        tabs.forEach { tab ->
                            val isSelected = tab == selectedTab
                            val hasAlert = tab in alertTabs
                            val icon = when (tab) {
                                BottomTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                BottomTab.INSIGHTS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                                BottomTab.SETTINGS -> if (isSelected) Icons.Filled.Tune else Icons.Outlined.Tune
                            }
                            val tint by animateColorAsState(
                                targetValue = if (isSelected) ElectricAmber else TextSecondary,
                                animationSpec = tween(durationMillis = 200),
                                label = "rail_tint_${tab.name}"
                            )
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.08f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "rail_scale_${tab.name}"
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        if (!isSelected) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onTabSelected(tab)
                                    }
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .testTag("nav_tab_${tab.name.lowercase()}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = tab.label,
                                        tint = tint,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .scale(iconScale)
                                    )
                                    if (hasAlert) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 4.dp, y = (-2).dp)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(AmberWarning)
                                                .border(1.5.dp, NavPillSurface, CircleShape)
                                                .testTag("nav_alert_${tab.name.lowercase()}")
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 0.2.sp
                                    ),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = tint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurgeonSkeletonCard(
    heightDp: Int = 92,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val shift by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_shift"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            CardSurface,
            CardSurfaceElevated,
            CardSurface
        ),
        start = Offset(shift, 0f),
        end = Offset(shift + 320f, 180f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(shimmerBrush)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    )
}

@Composable
fun SurgicalSyncLoadingBar(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160)),
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        val transition = rememberInfiniteTransition(label = "sync_pulse")
        val offsetFraction by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sync_offset"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CardSurfaceElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.38f + 0.32f * offsetFraction)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ElectricAmber)
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = modifier
    )
}
