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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.InterventionToastState
import com.example.ui.theme.HabitBg
import com.example.ui.theme.HabitForest
import com.example.ui.theme.HabitForestInk
import com.example.ui.theme.HabitLine
import com.example.ui.theme.HabitMuted
import com.example.ui.theme.HabitReadyBg
import com.example.ui.theme.HabitReadyBorder
import com.example.ui.theme.HabitReadyText
import com.example.ui.theme.HabitSageTint
import com.example.ui.theme.HabitSubtle
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.HabitSurfaceLow
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

@Composable
fun SurgeonCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    containerColor: Color? = null,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 0.dp,
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
                pressedScale = if (onClick != null) 0.985f else 0.996f,
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
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(shape)
            .background(containerColor),
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
    val scheme = MaterialTheme.colorScheme
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .neumorphicInset(cornerRadius = 14.dp, containerColor = HabitSurfaceLow, borderColor = HabitLine)
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
                        containerColor = HabitForest,
                        borderColor = Color.Transparent
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
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) HabitBg else scheme.onSurfaceVariant,
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
            .border(1.dp, borderColor, pillShape)
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
    modifier: Modifier = Modifier,
    darkStyle: Boolean = false
) {
    val neu = StillspaceTheme.colors
    val pillShape = RoundedCornerShape(100.dp)
    val bgColor = if (darkStyle) neu.amberContainer else neu.statusWarningBg
    val borderColor = if (darkStyle) Color.Transparent else neu.statusWarningBorder
    val textColor = if (darkStyle) neu.amberBadgeText else neu.statusWarningText

    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clip(pillShape)
            .background(bgColor)
            .then(
                if (borderColor.alpha > 0.01f) {
                    Modifier.border(1.dp, borderColor, pillShape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
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
fun IndigoTintPill(
    text: String,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(100.dp)
    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .clip(pillShape)
            .background(Color(0xFFF2F1EC))
            .border(1.dp, HabitLine, pillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = HabitForest,
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
    val logoRes = when (packageName) {
        "com.google.android.youtube" -> R.drawable.ic_logo_youtube
        "com.instagram.android" -> R.drawable.ic_logo_instagram
        "com.facebook.katana" -> R.drawable.ic_logo_facebook
        "com.snapchat.android" -> R.drawable.ic_logo_snapchat
        "com.reddit.frontpage" -> R.drawable.ic_logo_reddit
        else -> null
    }
    val pastelBg = when (packageName) {
        "com.google.android.youtube" -> Color(0xFFFBEAE9)
        "com.instagram.android" -> Color(0xFFFBEEF3)
        "com.facebook.katana" -> Color(0xFFECF2FB)
        "com.snapchat.android" -> Color(0xFFFAF5E4)
        "com.reddit.frontpage" -> Color(0xFFFBEEE7)
        else -> HabitSageTint
    }
    val cornerRadius = (badgeSize * 0.30f)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(shape)
            .background(pastelBg)
            .border(1.dp, HabitLine.copy(alpha = 0.65f), shape),
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
                tint = HabitForestInk,
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
            !enabled -> neu.switchInactiveTrack.copy(alpha = 0.7f)
            checked -> HabitForest
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
                .background(neu.switchThumb),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = HabitForest,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MindfulPausePreviewDialog(
    appName: String = "YouTube",
    feedLabel: String = "Shorts",
    breathingPauseSeconds: Int = 4,
    onBreathingPauseChange: ((Int) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    var selectedPaceSeconds by remember(breathingPauseSeconds) {
        mutableIntStateOf(if (breathingPauseSeconds <= 2) 2 else 4)
    }
    var remainingSeconds by remember(selectedPaceSeconds) { mutableIntStateOf(selectedPaceSeconds) }
    var isInhaling by remember(selectedPaceSeconds) { mutableStateOf(true) }
    var showIntentTray by remember { mutableStateOf(false) }
    var intentionText by remember { mutableStateOf("") }
    var demoOutcome by remember { mutableStateOf<String?>(null) }

    fun submitCustomIntention() {
        val reason = intentionText.trim()
        demoOutcome = if (reason.isNotEmpty()) {
            "5-min mindful pass started for \"$reason\"."
        } else {
            "5-min mindful pass started \u2014 protection resumes automatically after 5 minutes."
        }
    }

    LaunchedEffect(selectedPaceSeconds) {
        while (true) {
            delay(1000L)
            if (remainingSeconds > 1) {
                remainingSeconds -= 1
            } else {
                isInhaling = !isInhaling
                remainingSeconds = selectedPaceSeconds
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x66142119))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .widthIn(max = 396.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xECFAF9F5), Color(0xDEFAF9F5))
                        )
                    )
                    .border(1.dp, HabitForestInk.copy(alpha = 0.24f), RoundedCornerShape(28.dp))
                    .padding(1.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.78f), RoundedCornerShape(27.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .testTag("mindful_pause_preview_dialog")
            ) {
                // Header: OUTLOOP · Active Pause + Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.72f))
                            .border(1.dp, HabitForestInk.copy(alpha = 0.16f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(HabitForest)
                        )
                        Text(
                            text = "OUTLOOP \u00B7 Active Pause",
                            style = MaterialTheme.typography.labelSmall,
                            color = HabitForest
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("preview_close_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.72f))
                                .border(1.dp, HabitForestInk.copy(alpha = 0.16f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close preview",
                                tint = HabitMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Frosted Breathing Pace Segmented Toggle (2s quick reset / 4s calm pause)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0x94EAE7DF))
                        .border(1.dp, HabitForestInk.copy(alpha = 0.14f), RoundedCornerShape(100.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(2 to "2s quick reset", 4 to "4s calm pause").forEach { (secs, label) ->
                        val active = selectedPaceSeconds == secs
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (active) Color.White.copy(alpha = 0.90f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedPaceSeconds = secs
                                    onBreathingPauseChange?.invoke(secs)
                                }
                                .padding(horizontal = 11.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) HabitForest else HabitSubtle,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Open Loop Breathing Sanctuary Ring
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidShieldOverlay(
                        orbSize = 114.dp,
                        showObsidianBackdrop = false,
                        cycleSeconds = selectedPaceSeconds,
                        modifier = Modifier.size(114.dp)
                    ) {
                        Column(
                            modifier = Modifier.testTag("liquid_breathing_orb"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isInhaling) "Inhale" else "Exhale",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HabitForest
                            )
                            Text(
                                text = "${remainingSeconds}s",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFeatureSettings = "tnum"
                                ),
                                color = HabitSubtle
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Take a breath.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$appName $feedLabel opened \u00B7 Pause before scrolling.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HabitMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (demoOutcome != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HabitReadyBg)
                            .border(1.dp, HabitReadyBorder, RoundedCornerShape(14.dp))
                            .padding(11.dp)
                            .testTag("preview_demo_outcome")
                    ) {
                        Text(
                            text = demoOutcome.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = HabitReadyText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inline Dual Action Row: [ Return ] [ Intended Visit ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            demoOutcome = "Loop closed! In $appName, Outloop steps back from $feedLabel to your home tab."
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("preview_leave_feed_button"),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HabitForest,
                            contentColor = HabitBg
                        )
                    ) {
                        Text(
                            text = "Return",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showIntentTray = !showIntentTray
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("preview_allow_5m_button"),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (showIntentTray) Color(0xFFAFC1A3) else HabitForestInk.copy(alpha = 0.22f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showIntentTray) HabitSageTint else Color.White.copy(alpha = 0.85f),
                            contentColor = scheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Intended Visit",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Expandable Quick Intention Tray: preset chips + custom text input + tick-only submit
                AnimatedVisibility(visible = showIntentTray) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .testTag("preview_intention_tray")
                    ) {
                        Text(
                            text = "QUICK INTENTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = HabitSubtle
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Direct message" to "preview_intention_chip_dm",
                                "Saved post" to "preview_intention_chip_saved"
                            ).forEach { (intentLabel, chipTag) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.80f))
                                        .border(1.dp, HabitForestInk.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            demoOutcome = "5-min mindful pass started for \"$intentLabel\"."
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .testTag(chipTag),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = intentLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HabitForest,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.94f))
                                .border(1.dp, HabitForestInk.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                .padding(start = 12.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (intentionText.isEmpty()) {
                                    Text(
                                        text = "Write quick intention...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = HabitSubtle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = intentionText,
                                    onValueChange = { intentionText = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = scheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(HabitForest),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        submitCustomIntention()
                                    }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("preview_intention_input")
                                )
                            }

                            IconButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    submitCustomIntention()
                                },
                                modifier = Modifier
                                    .size(width = 48.dp, height = 44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HabitForest)
                                    .testTag("preview_intention_submit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Submit intention",
                                    tint = HabitBg,
                                    modifier = Modifier.size(20.dp)
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
                containerColor = HabitSurface,
                borderColor = HabitLine
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .neumorphicExtruded(
                cornerRadius = 18.dp,
                containerColor = HabitSurface,
                borderColor = HabitLine
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
            style = MaterialTheme.typography.titleSmall,
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
                color = HabitForest,
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
                    style = MaterialTheme.typography.labelSmall,
                    color = HabitSubtle,
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
                    color = HabitSubtle,
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
 * Quiet Intent Floating Pill Bottom Navigation Dock:
 * Centered translucent Warm Ivory capsule above navigationBars inset with
 * Deep Forest active tab pill.
 */
@Composable
fun StillspaceBottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    alertTabs: Set<BottomTab> = emptySet()
) {
    val haptics = LocalHapticFeedback.current
    val neu = StillspaceTheme.colors
    val tabs = BottomTab.entries

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .background(neu.dockSurface)
                .border(1.dp, HabitLine, RoundedCornerShape(100.dp))
                .padding(6.dp)
                .testTag("stillspace_bottom_nav"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.04f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "pill_nav_scale_${tab.name}"
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) HabitForest else Color.Transparent)
                        .clickable {
                            if (!isSelected) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onTabSelected(tab)
                        }
                        .padding(horizontal = 8.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            tint = if (isSelected) HabitBg else HabitSubtle,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(iconScale)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) HabitBg else HabitSubtle,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Wide-screen Material 3 NavigationRail framed in a clean Quiet Intent porcelain rail
 * with the Deep Forest Open Loop emblem header.
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
                    containerColor = HabitSurface,
                    borderColor = HabitLine
                )
                .testTag("stillspace_side_rail"),
            containerColor = Color.Transparent,
            contentColor = scheme.onSurface,
            header = {
                Spacer(modifier = Modifier.height(12.dp))
                OutloopEmblemMark(
                    boxSize = 46.dp,
                    cornerRadius = 14.dp,
                    modifier = Modifier.semantics(mergeDescendants = true) {}
                )
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
                        selectedIconColor = HabitBg,
                        selectedTextColor = HabitForest,
                        indicatorColor = HabitForest,
                        unselectedIconColor = HabitSubtle,
                        unselectedTextColor = HabitSubtle
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
            HabitSurface,
            HabitSurfaceLow,
            HabitSurface
        ),
        start = Offset(shift, 0f),
        end = Offset(shift + 320f, 180f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(shimmerBrush)
            .border(1.dp, HabitLine, RoundedCornerShape(24.dp))
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
                            colors = listOf(HabitForest, HabitReadyText)
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
        color = HabitSubtle,
        modifier = modifier
    )
}
