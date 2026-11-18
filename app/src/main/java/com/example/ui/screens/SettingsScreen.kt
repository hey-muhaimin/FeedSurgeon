package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.service.AccessibilityEventLogger
import com.example.service.AccessibilityStatusHelper
import com.example.service.ProtectionHealthHelper
import com.example.ui.MainUiState
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.SurgicalSyncLoadingBar
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onReopenDisclosure: () -> Unit,
    onToggleGlobalPause: (Boolean) -> Unit,
    onSetAutoUpdateRules: (Boolean) -> Unit,
    onCheckRulesNow: () -> Unit,
    onSetEventCaptureEnabled: (Boolean) -> Unit,
    onRefreshEventLog: () -> Unit,
    onClearEventLog: () -> Unit,
    onShareEventLog: () -> Unit,
    onSetReclaimedSeconds: (Int) -> Unit,
    onSetShowBanner: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onRefreshProtectionHealth: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    val prefs = uiState.preferences
    var showLogDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showAdvancedSection by remember { mutableStateOf(false) }
    var showPausePreview by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshProtectionHealth()
    }

    val activeInstalledCount = uiState.rules.count {
        it.enabled && it.packageName in uiState.installedPackages
    }
    val sortedRules = remember(uiState.rules, uiState.installedPackages) {
        uiState.rules.sortedByDescending { it.packageName in uiState.installedPackages }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = 170.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }

        // Protection & Permission Card
        item {
            SurgeonCard {
                SectionLabel(text = "PROTECTION")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device permission",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.isAccessibilityEnabled) {
                                "Guarding $activeInstalledCount installed app${if (activeInstalledCount == 1) "" else "s"} on your phone"
                            } else {
                                "Turn on Feed Surgeon in Android Settings so it can spot short feeds"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    StatusBadgePill(
                        text = if (uiState.isAccessibilityEnabled) "Ready" else "Off",
                        isWarning = !uiState.isAccessibilityEnabled
                    )
                }

                if (uiState.isAccessibilityEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pause all blocking",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Temporarily allow Shorts & Reels across every app",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        SurgeonSwitch(
                            checked = uiState.isGloballyPaused,
                            onCheckedChange = onToggleGlobalPause,
                            testTag = "settings_global_pause_switch"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { AccessibilityStatusHelper.openAccessibilitySettings(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("settings_accessibility_button"),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTeal,
                            contentColor = OnMintTeal
                        )
                    ) {
                        Text("Android Settings", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onReopenDisclosure,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("view_disclosure_button"),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurfaceElevated,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Guided setup")
                    }
                }
            }
        }

        // Comfort, Reminders & Privacy Card
        item {
            SurgeonCard {
                SectionLabel(text = "EXPERIENCE & PRIVACY")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quick pass bar",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Show a 5-min pass / Undo bar after stepping in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    SurgeonSwitch(
                        checked = prefs.showInterventionBanner,
                        onCheckedChange = onSetShowBanner,
                        testTag = "settings_banner_switch"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                val minutePresets = listOf(60 to "1 min", 120 to "2 min", 180 to "3 min", 300 to "5 min")
                val selectedPresetSeconds = minutePresets.minByOrNull { (sec, _) ->
                    kotlin.math.abs(sec - prefs.reclaimedSecondsPerCut)
                }?.first ?: 180
                Text(
                    text = "Time saved per avoided feed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .background(ObsidianBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    minutePresets.forEach { (seconds, label) ->
                        val isSelected = seconds == selectedPresetSeconds
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) MintTeal else Color.Transparent)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetReclaimedSeconds(seconds)
                                }
                                .padding(vertical = 10.dp)
                                .testTag("time_saved_preset_${seconds}s"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) OnMintTeal else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showPausePreview = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_preview_pause_button"),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardSurfaceElevated,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Preview pause screen")
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { uriHandler.openUri(privacyPolicyUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_privacy_policy_button")
                ) {
                    Text(
                        text = "Privacy Policy (suprbuild.com/privacy)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MintTeal
                    )
                }

                TextButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_history_button")
                ) {
                    Text(
                        text = "Clear my saved stats",
                        style = MaterialTheme.typography.labelLarge,
                        color = CoralDanger
                    )
                }
            }
        }

        // Collapsible Advanced & Diagnostics Section (tucked away for non-technical users)
        item {
            SurgeonCard(
                onClick = { showAdvancedSection = !showAdvancedSection },
                modifier = Modifier.testTag("settings_advanced_toggle_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Advanced & diagnostics",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val staleNote = if (uiState.staleRulesCount > 0) {
                            "${uiState.staleRulesCount} app blocker needs refresh"
                        } else {
                            "Blocker updates, battery reliability, and logs"
                        }
                        Text(
                            text = staleNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = if (showAdvancedSection) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = if (showAdvancedSection) "Hide advanced settings" else "Show advanced settings",
                        tint = TextSecondary
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = showAdvancedSection,
                enter = fadeIn(tween(200)) + expandVertically(),
                exit = fadeOut(tween(150)) + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // 1. Automatic Blocker Updates & Per-App Health
                    val lastCheckedSubtitle = if (prefs.lastRulesetCheckMs == 0L) {
                        "Built-in rules \u00B7 ${prefs.rulesetVersion}"
                    } else {
                        val rel = DateUtils.getRelativeTimeSpanString(
                            prefs.lastRulesetCheckMs,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        )
                        "Checked $rel \u00B7 ${prefs.rulesetVersion}"
                    }
                    SurgeonCard {
                        SectionLabel(text = "BLOCKER UPDATES & HEALTH")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-update blockers",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lastCheckedSubtitle,
                                    style = MonoTypography.caption,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            SurgeonSwitch(
                                checked = prefs.autoUpdateRules,
                                onCheckedChange = onSetAutoUpdateRules,
                                testTag = "settings_auto_update_switch"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        SurgicalSyncLoadingBar(visible = uiState.isSyncingRules)
                        AnimatedContent(
                            targetState = uiState.syncStatusMessage,
                            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                            label = "settings_sync_msg"
                        ) { msg ->
                            if (msg != null) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MintTeal,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onCheckRulesNow,
                            enabled = !uiState.isSyncingRules,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_check_updates_button"),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CardSurfaceElevated,
                                contentColor = TextPrimary
                            )
                        ) {
                            if (uiState.isSyncingRules) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (uiState.isSyncingRules) "Checking..." else "Check for blocker updates")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            sortedRules.forEach { rule ->
                                val isInstalled = rule.packageName in uiState.installedPackages
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (isInstalled) 1f else 0.65f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppBadgeIcon(packageName = rule.packageName)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rule.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = rule.feedLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                    if (!isInstalled) {
                                        Text(
                                            text = "Not installed",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextMuted
                                        )
                                    } else {
                                        StatusBadgePill(
                                            text = if (rule.isOutdated) "Needs update" else "Healthy",
                                            isWarning = rule.isOutdated
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Background Reliability
                    SurgeonCard {
                        SectionLabel(text = "BACKGROUND RELIABILITY")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Battery saver exception",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uiState.isBatteryOptimizationIgnored) {
                                        "Unrestricted — stays running reliably in the background"
                                    } else {
                                        "Phone battery saver may pause Feed Surgeon in the background"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            StatusBadgePill(
                                text = if (uiState.isBatteryOptimizationIgnored) "Unrestricted" else "Limited",
                                isWarning = !uiState.isBatteryOptimizationIgnored
                            )
                        }

                        if (!uiState.isBatteryOptimizationIgnored) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { ProtectionHealthHelper.requestIgnoreBatteryOptimizations(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_battery_optimization_button"),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CardSurfaceElevated,
                                    contentColor = MintTeal
                                )
                            ) {
                                Text("Allow unrestricted battery", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ongoing status notification",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (uiState.isNotificationPermissionGranted) {
                                            "Active notification badge allowed"
                                        } else {
                                            "Allow notifications so Android keeps protection active"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                StatusBadgePill(
                                    text = if (uiState.isNotificationPermissionGranted) "Allowed" else "Hidden",
                                    isWarning = !uiState.isNotificationPermissionGranted
                                )
                            }

                            if (!uiState.isNotificationPermissionGranted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("settings_notification_permission_button"),
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = CardSurfaceElevated,
                                        contentColor = TextPrimary
                                    )
                                ) {
                                    Text("Enable status notification")
                                }
                            }
                        }
                    }

                    // 3. Diagnostic Event Log
                    val kb = (uiState.eventLogSizeBytes / 1024L).coerceAtLeast(
                        if (uiState.eventLogSizeBytes > 0L) 1L else 0L
                    )
                    SurgeonCard {
                        SectionLabel(text = "TROUBLESHOOTING LOG")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Record diagnostic log",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${AccessibilityEventLogger.LOG_FILE_NAME} \u00B7 $kb KB",
                                    style = MonoTypography.caption,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            SurgeonSwitch(
                                checked = prefs.eventCaptureEnabled,
                                onCheckedChange = onSetEventCaptureEnabled,
                                testTag = "settings_event_capture_switch"
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onRefreshEventLog()
                                    showLogDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("view_event_log_button"),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CardSurfaceElevated,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text("View log")
                            }
                            OutlinedButton(
                                onClick = onClearEventLog,
                                enabled = uiState.eventLogSizeBytes > 0L,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("clear_event_log_button"),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CardSurfaceElevated,
                                    contentColor = CoralDanger
                                )
                            ) {
                                Text("Clear log")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = CardSurface,
            title = {
                Text(
                    text = "Clear saved stats?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This resets your weekly chart and time-saved counters. Your blocked apps and preferences won\u2019t change.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearHistory()
                    }
                ) {
                    Text("Clear stats", color = CoralDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            }
        )
    }

    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            containerColor = CardSurface,
            title = {
                Text(
                    text = "Local event log",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                val logContent = uiState.eventLogPreview.ifBlank {
                    "No events recorded yet.\nTurn on \"Record diagnostic log\", then open a supported app (like YouTube Shorts) to inspect screen labels."
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 340.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = logContent,
                        style = MonoTypography.caption,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.eventLogSizeBytes > 0L) {
                        TextButton(onClick = onShareEventLog) {
                            Text("Share file", color = MintTeal)
                        }
                    }
                    TextButton(onClick = onRefreshEventLog) {
                        Text("Refresh", color = TextPrimary)
                    }
                    TextButton(onClick = { showLogDialog = false }) {
                        Text("Close", color = TextSecondary)
                    }
                }
            }
        )
    }

    if (showPausePreview) {
        MindfulPausePreviewDialog(
            appName = "YouTube",
            feedLabel = "Shorts",
            onDismiss = { showPausePreview = false }
        )
    }
}
