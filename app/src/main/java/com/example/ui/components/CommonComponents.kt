package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
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
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.neumorphicExtruded
import com.example.ui.theme.neumorphicInset
import com.example.ui.theme.tactilePressPhysics
import kotlinx.coroutines.delay
import java.util.Locale

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
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .then(
                if (neu.isDark) {
                    Modifier.drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    neu.bgBloomPrimary.copy(alpha = 0.48f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.88f, size.height * 0.04f),
                                radius = size.width * 0.85f
                            )
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    neu.bgBloomSecondary.copy(alpha = 0.32f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.10f, size.height * 0.45f),
                                radius = size.width * 0.75f
                            )
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

@Composable
fun SurgeonCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    containerColor: Color? = null,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var pointerPressed by remember { mutableStateOf(false) }

    val resolvedContainer = containerColor ?: scheme.surfaceContainer
    val resolvedBorder = borderColor ?: scheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .tactilePressPhysics(
                pressedScale = if (onClick != null) 0.985f else 0.994f,
                enabled = true,
                onPressedChange = { pointerPressed = it }
            )
            .neumorphicExtruded(
                cornerRadius = cornerRadius,
                elevation = elevation,
                isPressed = pointerPressed,
                isHighlighted = pointerPressed || isHovered,
                containerColor = resolvedContainer,
                borderColor = resolvedBorder
            )
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
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun HabitIconBadge(
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 13.dp
) {
    val neu = StillspaceTheme.colors
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(shape)
            .background(containerColor)
            .then(
                if (neu.isDark) {
                    Modifier.border(1.dp, tint.copy(alpha = 0.22f), shape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun GroupedListDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = StillspaceTheme.colors.dividerLine
    )
}

@Composable
fun NeumorphicInsetWell(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    containerColor: Color? = null,
    borderColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .neumorphicInset(
                cornerRadius = cornerRadius,
                containerColor = containerColor,
                borderColor = borderColor
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun <T> NeumorphicSegmentedTrack(
    items: List<T>,
    selectedItem: T,
    labelProvider: (T) -> String,
    testTagProvider: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val neu = StillspaceTheme.colors
    val scheme = MaterialTheme.colorScheme
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .neumorphicInset(cornerRadius = 14.dp)
            .padding(4.dp)
    ) {
        val segmentWidth = if (items.isNotEmpty()) maxWidth / items.size else 0.dp
        val thumbOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "segmented_thumb_offset"
        )

        if (items.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .neumorphicExtruded(
                        cornerRadius = 11.dp,
                        elevation = 3.dp,
                        containerColor = if (neu.isDark) scheme.surfaceContainerHigh else scheme.surface,
                        borderColor = if (neu.isDark) scheme.outline else scheme.outlineVariant.copy(alpha = 0.7f)
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item == selectedItem
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(11.dp))
                        .clickable {
                            if (!selected) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(item)
                            }
                        }
                        .padding(horizontal = 4.dp)
                        .testTag(testTagProvider(item)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadgePill(
    text: String,
    isWarning: Boolean = false,
    showDot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val neu = StillspaceTheme.colors
    val dotColor = if (isWarning) neu.statusWarning else neu.statusActive
    val bgColor = if (isWarning) neu.statusWarningBg else neu.statusActiveBg
    val borderColor = if (isWarning) neu.statusWarningBorder else neu.statusActiveBorder
    val textColor = if (isWarning) neu.statusWarningText else neu.statusActiveText
    val pillShape = RoundedCornerShape(100.dp)

    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clip(pillShape)
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = if (neu.isDark) 0.9f else 0.7f), pillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StreakBadgePill(
    text: String,
    modifier: Modifier = Modifier
) {
    val neu = StillspaceTheme.colors
    val pillShape = RoundedCornerShape(100.dp)
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clip(pillShape)
            .background(neu.statusWarningBg)
            .border(1.dp, neu.statusWarningBorder.copy(alpha = if (neu.isDark) 0.9f else 0.7f), pillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = neu.statusWarningText,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = neu.statusWarningText,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IndigoTintPill(
    text: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val pillShape = RoundedCornerShape(100.dp)
    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clip(pillShape)
            .background(scheme.primaryContainer)
            .border(1.dp, neu.indigoTintBorder, pillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onPrimaryContainer,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppBadgeIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 44.dp
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val logoRes = when (packageName) {
        "com.google.android.youtube" -> R.drawable.ic_logo_youtube
        "com.instagram.android" -> R.drawable.ic_logo_instagram
        "com.facebook.katana" -> R.drawable.ic_logo_facebook
        "com.snapchat.android" -> R.drawable.ic_logo_snapchat
        "com.reddit.frontpage" -> R.drawable.ic_logo_reddit
        else -> null
    }
    val pastelLight = when (packageName) {
        "com.google.android.youtube" -> Color(0xFFFEECEB)
        "com.instagram.android" -> Color(0xFFFDECF2)
        "com.facebook.katana" -> Color(0xFFEAF2FF)
        "com.snapchat.android" -> Color(0xFFFEF8E6)
        "com.reddit.frontpage" -> Color(0xFFFFEFE6)
        else -> scheme.primaryContainer
    }
    val pastelDark = when (packageName) {
        "com.google.android.youtube" -> Color(0xFF34181D)
        "com.instagram.android" -> Color(0xFF341828)
        "com.facebook.katana" -> Color(0xFF18284B)
        "com.snapchat.android" -> Color(0xFF342E15)
        "com.reddit.frontpage" -> Color(0xFF382016)
        else -> scheme.primaryContainer
    }
    val cornerRadius = (badgeSize * 0.30f)
    val shape = RoundedCornerShape(cornerRadius)
    val bgColor = if (neu.isDark) pastelDark else pastelLight

    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(shape)
            .background(bgColor)
            .then(
                if (neu.isDark) {
                    Modifier.border(1.dp, scheme.outlineVariant.copy(alpha = 0.7f), shape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (logoRes != null) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                modifier = Modifier
                    .size(badgeSize * 0.54f)
                    .testTag("app_logo_$packageName")
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                tint = scheme.onPrimaryContainer,
                modifier = Modifier.size(badgeSize * 0.46f)
            )
        }
    }
}

@Composable
fun SurgeonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    enabled: Boolean = true
) {
    val haptics = LocalHapticFeedback.current
    val neu = StillspaceTheme.colors

    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> neu.switchInactiveTrack.copy(alpha = 0.65f)
            checked -> neu.primaryActionBg
            else -> neu.switchInactiveTrack
        },
        animationSpec = tween(durationMillis = 180),
        label = "switch_track_$testTag"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switch_thumb_$testTag"
    )

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .width(50.dp)
            .height(29.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(trackColor)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = { newValue ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(newValue)
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(23.dp)
                .clip(CircleShape)
                .background(neu.switchThumb)
        )
    }
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
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    var demoOutcome by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        SurgeonCard(
            borderColor = scheme.outlineVariant,
            containerColor = scheme.surfaceContainerHigh,
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
                color = scheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Take a quick breath",
                style = MaterialTheme.typography.headlineMedium,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$appName $feedLabel opened. Step back to protect your focus, or allow 5 minutes for this visit.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (demoOutcome != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicInset(
                            cornerRadius = 14.dp,
                            containerColor = neu.statusActiveBg,
                            borderColor = neu.statusActiveBorder
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = demoOutcome.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = neu.statusActiveText
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
                    .height(50.dp)
                    .neumorphicExtruded(
                        cornerRadius = 16.dp,
                        elevation = 5.dp,
                        containerColor = neu.primaryActionBg,
                        borderColor = Color.Transparent,
                        shadowTint = neu.primaryActionBg.copy(alpha = 0.28f)
                    )
                    .testTag("preview_leave_feed_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = neu.primaryActionBg,
                    contentColor = neu.primaryActionText
                )
            ) {
                Text(
                    text = "Leave $feedLabel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    demoOutcome = "Got it \u2014 $feedLabel stays open for 5 minutes, then protection turns back on automatically."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("preview_allow_5m_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = neu.secondaryButtonBg,
                    contentColor = scheme.onSurface
                )
            ) {
                Text(
                    text = "Allow 5 min",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preview_close_button"),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = "Close preview",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
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
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .tactilePressPhysics(pressedScale = 0.94f)
            .neumorphicExtruded(
                cornerRadius = 22.dp,
                elevation = 4.dp,
                containerColor = scheme.surface,
                borderColor = scheme.outlineVariant
            )
            .clickable(onClick = onClick)
            .testTag("back_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = "Back",
            tint = scheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
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
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .neumorphicExtruded(
                cornerRadius = 18.dp,
                elevation = 8.dp,
                containerColor = scheme.surface,
                borderColor = scheme.outlineVariant
            )
            .clickable(onClick = onDismiss)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("intervention_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(neu.statusActive)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = toast.message,
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onAllowOnce,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag("banner_allow_once")
        ) {
            Text(
                text = "5m pass",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(
            onClick = onUndo,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag("banner_undo")
        ) {
            Text(
                text = "Undo",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SurgeonScreenHeader(
    title: String,
    subtitle: String? = null,
    subtitleAbove: Boolean = false,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (subtitleAbove && !subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = scheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitleAbove && !subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
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

/**
 * Docked Material 3 NavigationBar matching the Focus Habit Design System:
 * Pure surface dock with 1.dp hairline top divider, soft primaryContainer active pill,
 * and contextual status badges.
 */
@Composable
fun StillspaceBottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    alertTabs: Set<BottomTab> = emptySet()
) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val tabs = BottomTab.entries

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(neu.dockSurface)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = scheme.outlineVariant
        )
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stillspace_bottom_nav"),
            containerColor = neu.dockSurface,
            contentColor = scheme.onSurface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                val hasAlert = tab in alertTabs
                val icon = when (tab) {
                    BottomTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                    BottomTab.INSIGHTS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                    BottomTab.SETTINGS -> if (isSelected) Icons.Filled.Tune else Icons.Outlined.Tune
                }
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.06f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "m3_nav_scale_${tab.name}"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onTabSelected(tab)
                    },
                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                    icon = {
                        BadgedBox(
                            badge = {
                                if (hasAlert) {
                                    Badge(
                                        containerColor = neu.statusWarning,
                                        contentColor = Color.White,
                                        modifier = Modifier.testTag("nav_alert_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.1.sp
                            ),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = scheme.onPrimaryContainer,
                        selectedTextColor = scheme.primary,
                        indicatorColor = scheme.primaryContainer,
                        unselectedIconColor = scheme.onSurfaceVariant,
                        unselectedTextColor = scheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Wide-screen Material 3 NavigationRail framed in a clean Focus Habit card rail.
 */
@Composable
fun StillspaceNavigationRail(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    alertTabs: Set<BottomTab> = emptySet()
) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val tabs = BottomTab.entries

    Box(
        modifier = modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .windowInsetsPadding(WindowInsets.displayCutout)
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        NavigationRail(
            modifier = Modifier
                .width(88.dp)
                .fillMaxHeight()
                .neumorphicExtruded(
                    cornerRadius = 24.dp,
                    elevation = 5.dp,
                    containerColor = scheme.surface,
                    borderColor = scheme.outlineVariant
                )
                .testTag("stillspace_side_rail"),
            containerColor = Color.Transparent,
            contentColor = scheme.onSurface,
            header = {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(scheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_notification),
                        contentDescription = "Stillspace",
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                val hasAlert = tab in alertTabs
                val icon = when (tab) {
                    BottomTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                    BottomTab.INSIGHTS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                    BottomTab.SETTINGS -> if (isSelected) Icons.Filled.Tune else Icons.Outlined.Tune
                }
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.06f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "m3_rail_scale_${tab.name}"
                )

                NavigationRailItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onTabSelected(tab)
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    icon = {
                        BadgedBox(
                            badge = {
                                if (hasAlert) {
                                    Badge(
                                        containerColor = neu.statusWarning,
                                        contentColor = Color.White,
                                        modifier = Modifier.testTag("nav_alert_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = scheme.onPrimaryContainer,
                        selectedTextColor = scheme.primary,
                        indicatorColor = scheme.primaryContainer,
                        unselectedIconColor = scheme.onSurfaceVariant,
                        unselectedTextColor = scheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun SurgeonSkeletonCard(
    heightDp: Int = 92,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
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
            scheme.surface,
            scheme.surfaceContainerLow,
            scheme.surface
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
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(20.dp))
    )
}

@Composable
fun SurgicalSyncLoadingBar(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
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
                .height(4.dp)
                .neumorphicInset(cornerRadius = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.38f + 0.32f * offsetFraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(scheme.primary, scheme.secondary)
                        )
                    )
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
