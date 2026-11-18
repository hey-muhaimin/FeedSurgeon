package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.InterventionMode
import com.example.data.model.InterventionToastState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.AmberWarningBorder
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MintTealBorder
import com.example.ui.theme.MintTealDark
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.NavPillBorder
import com.example.ui.theme.NavPillSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianGlow
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class BottomTab(val label: String) {
    HOME("Home"),
    RULES("Rules"),
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
    containerColor: Color = CardSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
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
    val borderColor = if (isWarning) AmberWarningBorder else MintTealBorder
    val bgColor = if (isWarning) AmberWarningBg else MintTealDark.copy(alpha = 0.45f)
    val textColor = if (isWarning) AmberWarning else MintTeal
    Box(
        modifier = modifier
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
    modifier: Modifier = Modifier
) {
    val icon = when (packageName) {
        "com.google.android.youtube" -> Icons.Filled.PlayArrow
        "com.instagram.android" -> Icons.Filled.PhotoCamera
        "com.facebook.katana" -> Icons.Filled.Movie
        "com.snapchat.android" -> Icons.Filled.Bolt
        "com.reddit.frontpage" -> Icons.Filled.Forum
        else -> Icons.Outlined.Apps
    }
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurfaceElevated)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SurgeonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.testTag(testTag),
        colors = SwitchDefaults.colors(
            checkedThumbColor = ObsidianBg,
            checkedTrackColor = MintTeal,
            checkedBorderColor = MintTeal,
            uncheckedThumbColor = TextSecondary,
            uncheckedTrackColor = CardSurfaceElevated,
            uncheckedBorderColor = CardBorder
        )
    )
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("intervention_banner"),
        shape = RoundedCornerShape(18.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "Allow once",
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
                    color = MintTeal
                )
            }
        }
    }
}

@Composable
fun FloatingPillNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = NavPillSurface,
            border = BorderStroke(1.dp, NavPillBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val icon = when (tab) {
                        BottomTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        BottomTab.RULES -> if (isSelected) Icons.Filled.Shield else Icons.Outlined.Shield
                        BottomTab.INSIGHTS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                        BottomTab.SETTINGS -> if (isSelected) Icons.Filled.Tune else Icons.Outlined.Tune
                    }
                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) MintTealDark else Color.Transparent,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "nav_pill_bg"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) MintTeal else TextSecondary,
                        animationSpec = tween(durationMillis = 200),
                        label = "nav_icon_tint"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "nav_icon_scale"
                    )

                    Row(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(pillBgColor)
                            .clickable { onTabSelected(tab) }
                            .padding(horizontal = if (isSelected) 18.dp else 16.dp, vertical = 10.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = iconTint,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(iconScale)
                        )
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(180)) + expandHorizontally(
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(tween(120)) + shrinkHorizontally(
                                animationSpec = tween(180, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MintTeal,
                                    maxLines = 1
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
                    .background(MintTeal)
            )
        }
    }
}

@Composable
fun CustomAppRuleDialog(
    onDismiss: () -> Unit,
    onSave: (
        packageName: String,
        displayName: String,
        feedLabel: String,
        viewIdsText: String,
        tabFallback: String,
        mode: InterventionMode
    ) -> Unit
) {
    var packageName by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var feedLabel by remember { mutableStateOf("Clips") }
    var viewIdsText by remember { mutableStateOf("") }
    var tabFallback by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(InterventionMode.BACK) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MintTeal,
        unfocusedBorderColor = CardBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = MintTeal,
        unfocusedLabelColor = TextSecondary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "Add custom app rule",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("App name (e.g. Reddit)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package ID (e.g. com.reddit.frontpage)") },
                    singleLine = true,
                    textStyle = MonoTypography.caption,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feedLabel,
                    onValueChange = { feedLabel = it },
                    label = { Text("Feed label (e.g. Watch)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = viewIdsText,
                    onValueChange = { viewIdsText = it },
                    label = { Text("View IDs (comma separated)") },
                    textStyle = MonoTypography.caption,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tabFallback,
                    onValueChange = { tabFallback = it },
                    label = { Text("Fallback tab label (optional)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InterventionMode.entries.forEach { mode ->
                        val active = mode == selectedMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (active) MintTeal else CardSurfaceElevated)
                                .clickable { selectedMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) OnMintTeal else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoralDanger
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pkg = packageName.trim()
                    if (!pkg.contains(".")) {
                        errorText = "Enter a valid Android package ID (e.g. com.example.app)."
                        return@Button
                    }
                    if (viewIdsText.isBlank() && tabFallback.isBlank()) {
                        errorText = "Provide at least one View ID or fallback tab label."
                        return@Button
                    }
                    onSave(pkg, displayName, feedLabel, viewIdsText, tabFallback, selectedMode)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintTeal,
                    contentColor = OnMintTeal
                )
            ) {
                Text("Save rule", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
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
