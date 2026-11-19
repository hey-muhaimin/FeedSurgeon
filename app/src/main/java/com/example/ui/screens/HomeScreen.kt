package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.ProtectionStatus
import com.example.service.AccessibilityStatusHelper
import com.example.ui.MainUiState
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonScreenHeader
import com.example.ui.components.SurgeonSkeletonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.rememberCountdownLabel
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBorder
import com.example.ui.theme.BarInactive
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MintTealBorder
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    bottomContentPadding: Dp = 112.dp
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
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
        ProtectionStatus.PERMISSION_NEEDED -> stringResource(R.string.status_service_off)
        ProtectionStatus.SERVICE_DISCONNECTED -> stringResource(R.string.status_disconnected)
        ProtectionStatus.NO_APPS_ACTIVE -> stringResource(R.string.status_no_apps)
        ProtectionStatus.PAUSED -> stringResource(R.string.status_paused)
        ProtectionStatus.OPERATIONAL -> stringResource(R.string.status_running)
    }
    val headerSubtitle = when (effectiveStatus) {
        ProtectionStatus.PERMISSION_NEEDED -> "Finish setup to guard short feeds"
        ProtectionStatus.SERVICE_DISCONNECTED -> "Accessibility service needs reconnect"
        ProtectionStatus.NO_APPS_ACTIVE -> "Enable an installed app below"
        ProtectionStatus.PAUSED -> "Protection paused temporarily"
        ProtectionStatus.OPERATIONAL -> {
            val count = uiState.activeInstalledCount
            "Guarding $count installed app${if (count == 1) "" else "s"}"
        }
    }
    val statusColor = if (effectiveStatus == ProtectionStatus.OPERATIONAL) MintTeal else AmberWarning

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = bottomContentPadding + navBarBottom
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SurgeonScreenHeader(
                title = stringResource(R.string.app_name),
                subtitle = headerSubtitle,
                trailingContent = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(onClick = onStatusClick)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("service_status_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            )
        }

        // Actionable status card matching the 5 explicit protection states
        if (!uiState.isInitialLoading) {
            item {
                when (effectiveStatus) {
                    ProtectionStatus.PERMISSION_NEEDED -> {
                        SurgeonCard(
                            borderColor = AmberWarningBorder,
                            modifier = Modifier.testTag("home_setup_needed_card")
                        ) {
                            Text(
                                text = "Protection isn\u2019t active yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Turn on Feed Surgeon in Android Settings so it can gently close Shorts and Reels for you.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onSetupClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("home_turn_on_protection_button"),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintTeal,
                                    contentColor = OnMintTeal
                                )
                            ) {
                                Text(
                                    text = "Turn on protection",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { showPausePreview = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_preview_pause_button")
                            ) {
                                Text(
                                    text = "Preview how Mindful Pause works",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MintTeal
                                )
                            }
                        }
                    }

                    ProtectionStatus.SERVICE_DISCONNECTED -> {
                        SurgeonCard(
                            borderColor = AmberWarningBorder,
                            modifier = Modifier.testTag("home_service_disconnected_card")
                        ) {
                            Text(
                                text = "Protection needs a quick reconnect",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Accessibility permission is enabled, but Android stopped the background service. Open Accessibility Settings and switch Feed Surgeon off and back on.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { AccessibilityStatusHelper.openAccessibilitySettings(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("home_reconnect_service_button"),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintTeal,
                                    contentColor = OnMintTeal
                                )
                            ) {
                                Text(
                                    text = "Open Accessibility Settings",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { showPausePreview = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_preview_pause_button")
                            ) {
                                Text(
                                    text = "Preview how Mindful Pause works",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MintTeal
                                )
                            }
                        }
                    }

                    ProtectionStatus.NO_APPS_ACTIVE -> {
                        SurgeonCard(
                            borderColor = AmberWarningBorder,
                            modifier = Modifier.testTag("home_no_apps_active_card")
                        ) {
                            Text(
                                text = "No apps guarded right now",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Feed Surgeon is ready, but none of your installed apps are switched on below. Turn on at least one app to start guarding.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { showPausePreview = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_preview_pause_button")
                            ) {
                                Text(
                                    text = "See how Mindful Pause looks",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MintTeal
                                )
                            }
                        }
                    }

                    ProtectionStatus.PAUSED,
                    ProtectionStatus.OPERATIONAL -> {
                        val isPausedCard = effectiveStatus == ProtectionStatus.PAUSED
                        SurgeonCard(
                            borderColor = if (isPausedCard) AmberWarningBorder else MintTealBorder,
                            modifier = Modifier.testTag("home_protection_control_card")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isPausedCard) "Taking a break" else "Protection is active",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
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
                                        color = if (isPausedCard) AmberWarning else TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                SurgeonSwitch(
                                    checked = !isPausedCard,
                                    onCheckedChange = { active -> onToggleGlobalPause(!active) },
                                    testTag = "home_global_protection_switch"
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            if (isPausedCard) {
                                Button(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onToggleGlobalPause(false)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("home_resume_protection_button"),
                                    shape = RoundedCornerShape(100.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MintTeal,
                                        contentColor = OnMintTeal
                                    )
                                ) {
                                    Text(
                                        text = "Resume protection now",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(5 to "5m break", 15 to "15m break", 60 to "1h break").forEach { (mins, label) ->
                                        OutlinedButton(
                                            onClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onPauseForMinutes(mins)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("home_break_${mins}m_button"),
                                            contentPadding = PaddingValues(horizontal = 6.dp),
                                            shape = RoundedCornerShape(100.dp),
                                            border = BorderStroke(1.dp, CardBorder),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = CardSurfaceElevated,
                                                contentColor = TextPrimary
                                            )
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = { showPausePreview = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_preview_pause_button")
                                ) {
                                    Text(
                                        text = "See how Mindful Pause looks",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MintTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hero wellbeing card: Distractions avoided + Estimated time reclaimed + 5-day trend
        item {
            AnimatedContent(
                targetState = uiState.isInitialLoading,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "home_hero_loading"
            ) { loading ->
                if (loading) {
                    SurgeonSkeletonCard(heightDp = 132)
                } else {
                    SurgeonCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.feeds_cut_today),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = uiState.todayCutsCount.toString(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("today_cuts_count")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "~${uiState.todayReclaimedMinutes} min est. reclaimed today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MintTeal
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.height(68.dp)
                            ) {
                                uiState.fiveDayBars.forEachIndexed { index, fraction ->
                                    val isToday = index == uiState.fiveDayBars.lastIndex
                                    val animatedFraction by animateFloatAsState(
                                        targetValue = fraction,
                                        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                                        label = "home_spark_bar_$index"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(10.dp)
                                            .height((60f * animatedFraction).dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (isToday) MintTeal else BarInactive)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Contextual blocker refresh card ONLY when an installed app layout actually needs an update
        if (uiState.staleRulesCount > 0 && !uiState.isInitialLoading) {
            item {
                SurgeonCard(
                    borderColor = AmberWarningBorder,
                    modifier = Modifier.testTag("home_stale_blocker_card")
                ) {
                    Text(
                        text = "An app updated its layout",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Refresh blockers so Feed Surgeon stays accurate after recent app updates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onRefreshBlockersClick,
                        enabled = !uiState.isSyncingRules,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("home_refresh_blockers_button"),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, AmberWarningBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurfaceElevated,
                            contentColor = AmberWarning
                        )
                    ) {
                        Text(if (uiState.isSyncingRules) "Refreshing..." else "Refresh blockers")
                    }
                }
            }
        }

        // YOUR APPS header with installed count
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(text = stringResource(R.string.target_apps_header))
                Text(
                    text = "${installedRules.size} on phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.testTag("installed_apps_count")
                )
            }
        }

        if (uiState.isInitialLoading) {
            items(2) {
                SurgeonSkeletonCard(heightDp = 80)
            }
        } else if (installedRules.isEmpty()) {
            item {
                SurgeonCard(modifier = Modifier.testTag("no_installed_apps_card")) {
                    Text(
                        text = "No supported apps found yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When YouTube, Instagram, Facebook, Snapchat, or Reddit are installed on your phone, they will show up here automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(installedRules, key = { it.packageName }) { rule ->
                TargetAppRowCard(
                    modifier = Modifier.animateItem(),
                    rule = rule,
                    isInstalled = true,
                    isGloballyPaused = isGloballyPausedNow,
                    onAppSnoozeExpired = { onAppSnoozeExpired(rule.packageName) },
                    onClick = { onAppClick(rule.packageName) },
                    onToggle = { enabled -> onToggleApp(rule.packageName, enabled) }
                )
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
                        color = TextMuted
                    )
                    Icon(
                        imageVector = if (showNotInstalled) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = if (showNotInstalled) "Hide other apps" else "Show other apps",
                        tint = TextMuted
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = showNotInstalled,
                    enter = fadeIn(tween(180)) + expandVertically(),
                    exit = fadeOut(tween(140)) + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        notInstalledRules.forEach { rule ->
                            TargetAppRowCard(
                                rule = rule,
                                isInstalled = false,
                                isGloballyPaused = isGloballyPausedNow,
                                onAppSnoozeExpired = { onAppSnoozeExpired(rule.packageName) },
                                onClick = { onAppClick(rule.packageName) },
                                onToggle = { enabled -> onToggleApp(rule.packageName, enabled) }
                            )
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
private fun TargetAppRowCard(
    rule: TargetAppRuleEntity,
    isInstalled: Boolean,
    isGloballyPaused: Boolean,
    onAppSnoozeExpired: () -> Unit,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val snoozeCountdown = rememberCountdownLabel(
        untilMs = rule.snoozeUntilMs,
        onExpired = onAppSnoozeExpired
    )

    SurgeonCard(
        modifier = modifier
            .then(if (isInstalled) Modifier.surgeonSharedBounds("app_card_${rule.packageName}") else Modifier)
            .alpha(if (isInstalled) 1f else 0.72f)
            .testTag("target_app_card_${rule.packageName}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppBadgeIcon(packageName = rule.packageName)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (!isInstalled) {
                    Text(
                        text = "${rule.feedLabel} \u00B7 Not on this phone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
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
                        color = TextMuted
                    )
                } else {
                    Text(
                        text = "${rule.feedLabel} \u00B7 ${rule.interventionMode.subtitleLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            SurgeonSwitch(
                checked = rule.enabled,
                onCheckedChange = onToggle,
                testTag = "toggle_${rule.packageName}"
            )
        }
    }
}
