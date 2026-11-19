package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.ProtectionStatus
import com.example.service.AccessibilityStatusHelper
import com.example.ui.DayBarStat
import com.example.ui.MainUiState
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.GroupedListDivider
import com.example.ui.components.HabitIconBadge
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonScreenHeader
import com.example.ui.components.SurgeonSkeletonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.rememberCountdownLabel
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.theme.StaggeredReveal
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.neumorphicExtruded
import com.example.ui.theme.tactilePressPhysics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onStatusClick: () -> Unit,
    onSetupClick: () -> Unit,
    onToggleGlobalPause: (Boolean) -> Unit,
    onPauseForMinutes: (Int) -> Unit,
    onGlobalBreakExpired: () -> Unit,
    onAppSnoozeExpired: (String) -> Unit,
    onRefreshBlockersClick: () -> Unit,
    onAppClick: (String) -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    bottomContentPadding: Dp = 96.dp
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val installedRules = remember(uiState.rules, uiState.installedPackages) {
        uiState.rules.filter { it.packageName in uiState.installedPackages }
    }
    val notInstalledRules = remember(uiState.rules, uiState.installedPackages) {
        uiState.rules.filter { it.packageName !in uiState.installedPackages }
    }
    var showNotInstalled by remember { mutableStateOf(false) }
    var showPausePreview by remember { mutableStateOf(false) }

    val globalCountdown = rememberCountdownLabel(
        untilMs = uiState.globalPauseUntilMs,
        onExpired = onGlobalBreakExpired
    )
    val isGloballyPausedNow = globalCountdown != null
    val effectiveStatus = when {
        uiState.protectionStatus == ProtectionStatus.PERMISSION_NEEDED -> ProtectionStatus.PERMISSION_NEEDED
        uiState.protectionStatus == ProtectionStatus.SERVICE_DISCONNECTED -> ProtectionStatus.SERVICE_DISCONNECTED
        uiState.protectionStatus == ProtectionStatus.NO_APPS_ACTIVE -> ProtectionStatus.NO_APPS_ACTIVE
        isGloballyPausedNow -> ProtectionStatus.PAUSED
        else -> ProtectionStatus.OPERATIONAL
    }

    val statusLabel = when (effectiveStatus) {
        ProtectionStatus.PERMISSION_NEEDED -> "Finish Setup"
        ProtectionStatus.SERVICE_DISCONNECTED -> "Reconnect"
        ProtectionStatus.NO_APPS_ACTIVE -> "Pick Apps"
        ProtectionStatus.PAUSED -> "On a Break"
        ProtectionStatus.OPERATIONAL -> "Protected"
    }
    val todayDateLabel = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMosaic = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = bottomContentPadding + navBarBottom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SurgeonScreenHeader(
                    title = "Today",
                    subtitle = todayDateLabel,
                    subtitleAbove = true,
                    trailingContent = {
                        StatusBadgePill(
                            text = statusLabel,
                            isWarning = effectiveStatus != ProtectionStatus.OPERATIONAL,
                            showDot = true,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable(onClick = onStatusClick)
                                .testTag("service_status_pill")
                        )
                    }
                )
            }

            if (isMosaic) {
                item {
                    StaggeredReveal(index = 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .testTag("home_mosaic_grid"),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (uiState.isInitialLoading) {
                                SurgeonSkeletonCard(
                                    heightDp = 196,
                                    modifier = Modifier.weight(1.04f)
                                )
                                SurgeonSkeletonCard(
                                    heightDp = 196,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                TodayIndigoHeroCard(
                                    uiState = uiState,
                                    modifier = Modifier
                                        .weight(1.04f)
                                        .fillMaxHeight()
                                )
                                ProtectionStatusCard(
                                    effectiveStatus = effectiveStatus,
                                    uiState = uiState,
                                    globalCountdown = globalCountdown,
                                    onSetupClick = onSetupClick,
                                    onOpenAccessibilitySettings = {
                                        AccessibilityStatusHelper.openAccessibilitySettings(context)
                                    },
                                    onToggleGlobalPause = onToggleGlobalPause,
                                    onPauseForMinutes = onPauseForMinutes,
                                    onShowPreview = { showPausePreview = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            } else {
                // Hero Indigo Gradient Summary Card
                item {
                    StaggeredReveal(index = 0) {
                        AnimatedContent(
                            targetState = uiState.isInitialLoading,
                            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                            label = "home_hero_loading"
                        ) { loading ->
                            if (loading) {
                                SurgeonSkeletonCard(heightDp = 212)
                            } else {
                                TodayIndigoHeroCard(uiState = uiState)
                            }
                        }
                    }
                }

                // Actionable Protection & Quick Break Card
                if (!uiState.isInitialLoading) {
                    item {
                        StaggeredReveal(index = 1) {
                            ProtectionStatusCard(
                                effectiveStatus = effectiveStatus,
                                uiState = uiState,
                                globalCountdown = globalCountdown,
                                onSetupClick = onSetupClick,
                                onOpenAccessibilitySettings = {
                                    AccessibilityStatusHelper.openAccessibilitySettings(context)
                                },
                                onToggleGlobalPause = onToggleGlobalPause,
                                onPauseForMinutes = onPauseForMinutes,
                                onShowPreview = { showPausePreview = true }
                            )
                        }
                    }
                }
            }

            // Contextual blocker refresh card when an installed app rule needs update
            if (uiState.staleRulesCount > 0 && !uiState.isInitialLoading) {
                item {
                    StaggeredReveal(index = 2) {
                        SurgeonCard(
                            borderColor = neu.statusWarningBorder,
                            modifier = Modifier.testTag("home_stale_blocker_card")
                        ) {
                            Text(
                                text = "An app updated its layout",
                                style = MaterialTheme.typography.titleMedium,
                                color = scheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Refresh focus rules so Stillspace stays accurate after recent app updates.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onRefreshBlockersClick,
                                enabled = !uiState.isSyncingRules,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("home_refresh_blockers_button"),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                border = BorderStroke(1.dp, neu.statusWarningBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = neu.statusWarningBg,
                                    contentColor = neu.statusWarningText
                                )
                            ) {
                                Text(
                                    text = if (uiState.isSyncingRules) "Refreshing..." else "Refresh rules",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Your apps section header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your apps",
                            style = MaterialTheme.typography.titleLarge,
                            color = scheme.onBackground
                        )
                        Text(
                            text = "${installedRules.size} on phone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.testTag("installed_apps_count")
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap any app to customize",
                        style = MaterialTheme.typography.bodySmall,
                        color = neu.textMuted,
                        modifier = Modifier.testTag("home_apps_customize_hint")
                    )
                }
            }

            if (uiState.isInitialLoading) {
                items(2) {
                    SurgeonSkeletonCard(heightDp = 82)
                }
            } else if (installedRules.isEmpty()) {
                item {
                    StaggeredReveal(index = 3) {
                        SurgeonCard(modifier = Modifier.testTag("no_installed_apps_card")) {
                            Text(
                                text = "No supported apps found yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = scheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "When YouTube, Instagram, Facebook, Snapchat, or Reddit are installed on your phone, they will show up here automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (isMosaic) {
                val chunked = installedRules.chunked(2)
                items(chunked.size) { rowIndex ->
                    val pair = chunked[rowIndex]
                    StaggeredReveal(index = 2 + rowIndex) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_apps_mosaic_row_$rowIndex"),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { rule ->
                                SurgeonCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .surgeonSharedBounds("app_card_${rule.packageName}")
                                        .testTag("target_app_card_${rule.packageName}"),
                                    contentPadding = PaddingValues(0.dp),
                                    onClick = { onAppClick(rule.packageName) }
                                ) {
                                    TargetAppRowContent(
                                        rule = rule,
                                        isInstalled = true,
                                        isGloballyPaused = isGloballyPausedNow,
                                        onAppSnoozeExpired = { onAppSnoozeExpired(rule.packageName) },
                                        onToggle = { enabled -> onToggleApp(rule.packageName, enabled) }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                // Single grouped white card with inset hairline dividers matching Home mockup
                item {
                    StaggeredReveal(index = 2) {
                        SurgeonCard(contentPadding = PaddingValues(0.dp)) {
                            installedRules.forEachIndexed { index, rule ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .surgeonSharedBounds("app_card_${rule.packageName}")
                                        .clickable { onAppClick(rule.packageName) }
                                        .testTag("target_app_card_${rule.packageName}")
                                ) {
                                    TargetAppRowContent(
                                        rule = rule,
                                        isInstalled = true,
                                        isGloballyPaused = isGloballyPausedNow,
                                        onAppSnoozeExpired = { onAppSnoozeExpired(rule.packageName) },
                                        onToggle = { enabled -> onToggleApp(rule.packageName, enabled) }
                                    )
                                }
                                if (index < installedRules.lastIndex) {
                                    GroupedListDivider(
                                        modifier = Modifier.padding(start = 74.dp, end = 18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (notInstalledRules.isNotEmpty() && !uiState.isInitialLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showNotInstalled = !showNotInstalled }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("toggle_not_installed_apps"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Other supported apps (${notInstalledRules.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = neu.textMuted
                        )
                        Icon(
                            imageVector = if (showNotInstalled) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
                            },
                            contentDescription = if (showNotInstalled) "Hide other apps" else "Show other apps",
                            tint = neu.textMuted
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = showNotInstalled,
                        enter = fadeIn(tween(180)) + expandVertically(),
                        exit = fadeOut(tween(140)) + shrinkVertically()
                    ) {
                        SurgeonCard(
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            notInstalledRules.forEachIndexed { index, rule ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAppClick(rule.packageName) }
                                        .alpha(0.78f)
                                        .testTag("target_app_card_${rule.packageName}")
                                ) {
                                    TargetAppRowContent(
                                        rule = rule,
                                        isInstalled = false,
                                        isGloballyPaused = isGloballyPausedNow,
                                        onAppSnoozeExpired = { onAppSnoozeExpired(rule.packageName) },
                                        onToggle = { enabled -> onToggleApp(rule.packageName, enabled) }
                                    )
                                }
                                if (index < notInstalledRules.lastIndex) {
                                    GroupedListDivider(
                                        modifier = Modifier.padding(start = 74.dp, end = 18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPausePreview) {
        val sampleApp = installedRules.firstOrNull()
        MindfulPausePreviewDialog(
            appName = sampleApp?.displayName ?: "YouTube",
            feedLabel = sampleApp?.feedLabel ?: "Shorts",
            onDismiss = { showPausePreview = false }
        )
    }
}

@Composable
private fun TodayIndigoHeroCard(
    uiState: MainUiState,
    modifier: Modifier = Modifier
) {
    val neu = StillspaceTheme.colors
    val weekCircles = remember(uiState.weeklyDayBars, uiState.todayCutsCount) {
        if (uiState.weeklyDayBars.size == 7) {
            uiState.weeklyDayBars.map { day ->
                val active = if (day.isToday) (day.count > 0 || uiState.todayCutsCount > 0) else day.count > 0
                Triple(day.dayLetter, active, day.isToday)
            }
        } else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            val formatter = SimpleDateFormat("EEEEE", Locale.getDefault())
            (0 until 7).map { offset ->
                val letter = formatter.format(cal.time)
                val isToday = offset == 6
                cal.add(Calendar.DAY_OF_YEAR, 1)
                Triple(letter, isToday && uiState.todayCutsCount > 0, isToday)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicExtruded(
                cornerRadius = 24.dp,
                elevation = 8.dp,
                containerColor = neu.heroGradientStart,
                borderColor = Color.Transparent,
                shadowTint = neu.heroGradientEnd.copy(alpha = 0.34f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(neu.heroGradientStart, neu.heroGradientEnd)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Distractions avoided today",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.90f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (uiState.streakDays > 0) {
                        "${uiState.streakDays} day streak"
                    } else {
                        "0 day streak"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedIntCounter(
            target = uiState.todayCutsCount,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 52.sp,
                lineHeight = 56.sp
            ),
            color = Color.White,
            modifier = Modifier.testTag("today_cuts_count")
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "~${uiState.todayReclaimedMinutes} min est. reclaimed today",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.86f)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekCircles.forEach { (letter, isChecked, isToday) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (isChecked) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = neu.heroGradientEnd,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.55f),
                                    style = Stroke(
                                        width = 1.6.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(3.dp.toPx(), 3.dp.toPx()),
                                            0f
                                        )
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) Color.White else Color.White.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtectionStatusCard(
    effectiveStatus: ProtectionStatus,
    uiState: MainUiState,
    globalCountdown: String?,
    onSetupClick: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleGlobalPause: (Boolean) -> Unit,
    onPauseForMinutes: (Int) -> Unit,
    onShowPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    when (effectiveStatus) {
        ProtectionStatus.PERMISSION_NEEDED -> {
            SurgeonCard(
                borderColor = neu.statusWarningBorder,
                modifier = modifier.testTag("home_setup_needed_card")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HabitIconBadge(
                        icon = Icons.Outlined.Shield,
                        tint = neu.statusWarningText,
                        containerColor = neu.statusWarningBg
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Protection isn\u2019t active yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Turn on Stillspace in Android Settings to pause Shorts and Reels.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onSetupClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .neumorphicExtruded(
                            cornerRadius = 16.dp,
                            elevation = 4.dp,
                            containerColor = neu.primaryActionBg,
                            borderColor = Color.Transparent,
                            shadowTint = neu.primaryActionBg.copy(alpha = 0.25f)
                        )
                        .testTag("home_turn_on_protection_button"),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = neu.primaryActionBg,
                        contentColor = neu.primaryActionText
                    )
                ) {
                    Text(
                        text = "Turn on protection",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onShowPreview,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_preview_pause_button")
                ) {
                    Text(
                        text = "Preview pause",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        ProtectionStatus.SERVICE_DISCONNECTED -> {
            SurgeonCard(
                borderColor = neu.statusWarningBorder,
                modifier = modifier.testTag("home_service_disconnected_card")
            ) {
                Text(
                    text = "Protection needs a quick reconnect",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Accessibility permission is enabled, but Android stopped the background service. Open Accessibility Settings and switch Stillspace off and back on.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .neumorphicExtruded(
                            cornerRadius = 16.dp,
                            elevation = 4.dp,
                            containerColor = neu.primaryActionBg,
                            borderColor = Color.Transparent,
                            shadowTint = neu.primaryActionBg.copy(alpha = 0.25f)
                        )
                        .testTag("home_reconnect_service_button"),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = neu.primaryActionBg,
                        contentColor = neu.primaryActionText
                    )
                ) {
                    Text(
                        text = "Open Settings",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        ProtectionStatus.NO_APPS_ACTIVE -> {
            SurgeonCard(
                borderColor = neu.statusWarningBorder,
                modifier = modifier.testTag("home_no_apps_active_card")
            ) {
                Text(
                    text = "No apps active right now",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Stillspace is ready, but none of your installed apps are switched on below. Turn on at least one app to protect your focus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onShowPreview,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_preview_pause_button")
                ) {
                    Text(
                        text = "Preview pause",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        ProtectionStatus.PAUSED,
        ProtectionStatus.OPERATIONAL -> {
            val isPausedCard = effectiveStatus == ProtectionStatus.PAUSED
            SurgeonCard(
                modifier = modifier.testTag("home_protection_control_card"),
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HabitIconBadge(
                        icon = if (isPausedCard) Icons.Outlined.PauseCircle else Icons.Outlined.VerifiedUser,
                        tint = if (isPausedCard) neu.statusWarningText else neu.statusActiveText,
                        containerColor = if (isPausedCard) neu.statusWarningBg else neu.statusActiveBg,
                        badgeSize = 42.dp,
                        iconSize = 20.dp,
                        cornerRadius = 13.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPausedCard) "Protection paused" else "Protection is on",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPausedCard) {
                                if (uiState.globalPauseUntilMs == Long.MAX_VALUE) {
                                    "Paused until you turn it back on"
                                } else {
                                    "$globalCountdown remaining before auto-resume"
                                }
                            } else {
                                val activeCount = uiState.activeInstalledCount
                                "Guarding $activeCount installed app${if (activeCount == 1) "" else "s"}"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (isPausedCard) neu.statusWarningText else scheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    SurgeonSwitch(
                        checked = !isPausedCard,
                        onCheckedChange = { active -> onToggleGlobalPause(!active) },
                        testTag = "home_global_protection_switch"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                if (isPausedCard) {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleGlobalPause(false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicExtruded(
                                cornerRadius = 16.dp,
                                elevation = 4.dp,
                                containerColor = neu.primaryActionBg,
                                borderColor = Color.Transparent,
                                shadowTint = neu.primaryActionBg.copy(alpha = 0.25f)
                            )
                            .testTag("home_resume_protection_button"),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = neu.primaryActionBg,
                            contentColor = neu.primaryActionText
                        )
                    ) {
                        Text(
                            text = "Resume protection",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = "Take a break",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(5 to "5 min", 15 to "15 min", 60 to "1 hour").forEach { (mins, label) ->
                            val pillShape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .height(44.dp)
                                    .tactilePressPhysics(pressedScale = 0.96f)
                                    .clip(pillShape)
                                    .background(scheme.primaryContainer)
                                    .border(1.dp, neu.indigoTintBorder, pillShape)
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPauseForMinutes(mins)
                                    }
                                    .padding(horizontal = 6.dp)
                                    .testTag("home_break_${mins}m_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = scheme.onPrimaryContainer,
                                    maxLines = 1,
                                    softWrap = false,
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
private fun TargetAppRowContent(
    rule: TargetAppRuleEntity,
    isInstalled: Boolean,
    isGloballyPaused: Boolean,
    onAppSnoozeExpired: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val snoozeCountdown = rememberCountdownLabel(
        untilMs = rule.snoozeUntilMs,
        onExpired = onAppSnoozeExpired
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppBadgeIcon(packageName = rule.packageName, badgeSize = 44.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (!isInstalled) {
                Text(
                    text = "${rule.feedLabel} \u00B7 Not on this phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = neu.textMuted
                )
            } else if (rule.isOutdated) {
                StatusBadgePill(
                    text = "Needs refresh",
                    isWarning = true
                )
            } else if (snoozeCountdown != null) {
                StatusBadgePill(
                    text = "Break \u00B7 $snoozeCountdown",
                    isWarning = true
                )
            } else if (isGloballyPaused) {
                StatusBadgePill(
                    text = "On break",
                    isWarning = true
                )
            } else if (!rule.enabled) {
                Text(
                    text = "${rule.feedLabel} \u00B7 Off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = neu.textMuted
                )
            } else {
                Text(
                    text = "${rule.feedLabel} \u00B7 ${rule.interventionMode.subtitleLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        SurgeonSwitch(
            checked = rule.enabled,
            onCheckedChange = onToggle,
            testTag = "toggle_${rule.packageName}"
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Configure ${rule.displayName}",
            tint = neu.textMuted,
            modifier = Modifier
                .size(20.dp)
                .testTag("app_detail_chevron_${rule.packageName}")
        )
    }
}
