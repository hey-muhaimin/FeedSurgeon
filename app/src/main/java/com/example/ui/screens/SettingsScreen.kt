package com.example.ui.screens

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppPreferences
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.ProtectionStatus
import com.example.data.model.ThemeMode
import com.example.service.AccessibilityEventLogger
import com.example.service.AccessibilityStatusHelper
import com.example.service.ProtectionHealthHelper
import com.example.ui.MainUiState
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.GroupedListDivider
import com.example.ui.components.HabitIconBadge
import com.example.ui.components.IndigoTintPill
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.NeumorphicSegmentedTrack
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonScreenHeader
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.SurgicalSyncLoadingBar
import com.example.ui.components.rememberCountdownLabel
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.StaggeredReveal
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.neumorphicExtruded

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onReopenDisclosure: () -> Unit,
    onToggleGlobalPause: (Boolean) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit = {},
    onSetAutoUpdateRules: (Boolean) -> Unit,
    onCheckRulesNow: () -> Unit,
    onSetEventCaptureEnabled: (Boolean) -> Unit,
    onRefreshEventLog: () -> Unit,
    onClearEventLog: () -> Unit,
    onShareEventLog: () -> Unit,
    onSetReclaimedSeconds: (Int) -> Unit,
    onSetShowBanner: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onRefreshProtectionHealth: () -> Unit = {},
    bottomContentPadding: Dp = 96.dp
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SurgeonScreenHeader(
                    title = stringResource(R.string.settings_title),
                    subtitle = "Local rules & privacy controls",
                    trailingContent = {
                        IndigoTintPill(text = prefs.rulesetVersion)
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
                                .testTag("settings_mosaic_top_row"),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Protection",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = scheme.onSurfaceVariant
                                )
                                ProtectionCard(
                                    uiState = uiState,
                                    activeInstalledCount = activeInstalledCount,
                                    context = context,
                                    onToggleGlobalPause = onToggleGlobalPause,
                                    onReopenDisclosure = onReopenDisclosure
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Experience",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = scheme.onSurfaceVariant
                                )
                                ExperienceCard(
                                    prefs = prefs,
                                    onSetShowBanner = onSetShowBanner,
                                    onSetReclaimedSeconds = onSetReclaimedSeconds
                                )
                                PrivacyActionsCard(
                                    onShowPausePreview = { showPausePreview = true },
                                    onOpenPrivacyPolicy = { uriHandler.openUri(privacyPolicyUrl) },
                                    onRequestClearHistory = { showClearConfirmDialog = true }
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Protection",
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurfaceVariant
                    )
                }

                item {
                    StaggeredReveal(index = 0) {
                        ProtectionCard(
                            uiState = uiState,
                            activeInstalledCount = activeInstalledCount,
                            context = context,
                            onToggleGlobalPause = onToggleGlobalPause,
                            onReopenDisclosure = onReopenDisclosure
                        )
                    }
                }

                item {
                    Text(
                        text = "Experience",
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurfaceVariant
                    )
                }

                item {
                    StaggeredReveal(index = 1) {
                        ExperienceCard(
                            prefs = prefs,
                            onSetShowBanner = onSetShowBanner,
                            onSetReclaimedSeconds = onSetReclaimedSeconds
                        )
                    }
                }

                item {
                    StaggeredReveal(index = 2) {
                        PrivacyActionsCard(
                            onShowPausePreview = { showPausePreview = true },
                            onOpenPrivacyPolicy = { uriHandler.openUri(privacyPolicyUrl) },
                            onRequestClearHistory = { showClearConfirmDialog = true }
                        )
                    }
                }
            }

            // Appearance ThemeMode Card
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurfaceVariant
                )
            }

            item {
                StaggeredReveal(index = 3) {
                    AppearanceCard(
                        selectedMode = prefs.themeMode,
                        onSelectMode = onSetThemeMode
                    )
                }
            }

            // Collapsible Advanced & Diagnostics Section
            item {
                StaggeredReveal(index = 4) {
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
                                    color = scheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val staleNote = if (uiState.staleRulesCount > 0) {
                                    "${uiState.staleRulesCount} app focus rule needs refresh"
                                } else {
                                    "Rule sync updates, battery reliability, and logs"
                                }
                                Text(
                                    text = staleNote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (showAdvancedSection) {
                                    Icons.Filled.KeyboardArrowUp
                                } else {
                                    Icons.Filled.KeyboardArrowDown
                                },
                                contentDescription = if (showAdvancedSection) "Hide advanced settings" else "Show advanced settings",
                                tint = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = showAdvancedSection,
                    enter = fadeIn(tween(200)) + expandVertically(),
                    exit = fadeOut(tween(150)) + shrinkVertically()
                ) {
                    if (isMosaic) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            BlockerUpdatesCard(
                                uiState = uiState,
                                prefs = prefs,
                                sortedRules = sortedRules,
                                onSetAutoUpdateRules = onSetAutoUpdateRules,
                                onCheckRulesNow = onCheckRulesNow,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                BackgroundReliabilityCard(
                                    uiState = uiState,
                                    context = context,
                                    onRequestNotificationPermission = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                )
                                DiagnosticLogCard(
                                    uiState = uiState,
                                    prefs = prefs,
                                    onSetEventCaptureEnabled = onSetEventCaptureEnabled,
                                    onViewLog = {
                                        onRefreshEventLog()
                                        showLogDialog = true
                                    },
                                    onClearEventLog = onClearEventLog
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            BlockerUpdatesCard(
                                uiState = uiState,
                                prefs = prefs,
                                sortedRules = sortedRules,
                                onSetAutoUpdateRules = onSetAutoUpdateRules,
                                onCheckRulesNow = onCheckRulesNow
                            )
                            BackgroundReliabilityCard(
                                uiState = uiState,
                                context = context,
                                onRequestNotificationPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            )
                            DiagnosticLogCard(
                                uiState = uiState,
                                prefs = prefs,
                                onSetEventCaptureEnabled = onSetEventCaptureEnabled,
                                onViewLog = {
                                    onRefreshEventLog()
                                    showLogDialog = true
                                },
                                onClearEventLog = onClearEventLog
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = scheme.surface,
            title = {
                Text(
                    text = "Clear saved stats?",
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This resets your weekly chart and time-saved counters. Your blocked apps and preferences won\u2019t change.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    onClick = {
                        showClearConfirmDialog = false
                        onClearHistory()
                    }
                ) {
                    Text(
                        text = "Clear stats",
                        color = neu.statusDanger,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            dismissButton = {
                TextButton(
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    onClick = { showClearConfirmDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = scheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        )
    }

    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            containerColor = scheme.surface,
            title = {
                Text(
                    text = "Local event log",
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onSurface
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
                        color = scheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.eventLogSizeBytes > 0L) {
                        TextButton(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            onClick = onShareEventLog
                        ) {
                            Text("Share file", color = scheme.primary, maxLines = 1, softWrap = false)
                        }
                    }
                    TextButton(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        onClick = onRefreshEventLog
                    ) {
                        Text("Refresh", color = scheme.onSurface, maxLines = 1, softWrap = false)
                    }
                    TextButton(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        onClick = { showLogDialog = false }
                    ) {
                        Text("Close", color = scheme.onSurfaceVariant, maxLines = 1, softWrap = false)
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

@Composable
private fun ProtectionCard(
    uiState: MainUiState,
    activeInstalledCount: Int,
    context: Context,
    onToggleGlobalPause: (Boolean) -> Unit,
    onReopenDisclosure: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val globalCountdown = rememberCountdownLabel(untilMs = uiState.globalPauseUntilMs)
    val isGloballyPausedNow = globalCountdown != null
    val effectiveStatus = when {
        uiState.protectionStatus == ProtectionStatus.PERMISSION_NEEDED -> ProtectionStatus.PERMISSION_NEEDED
        uiState.protectionStatus == ProtectionStatus.SERVICE_DISCONNECTED -> ProtectionStatus.SERVICE_DISCONNECTED
        uiState.protectionStatus == ProtectionStatus.NO_APPS_ACTIVE -> ProtectionStatus.NO_APPS_ACTIVE
        isGloballyPausedNow -> ProtectionStatus.PAUSED
        else -> ProtectionStatus.OPERATIONAL
    }
    val pillText = when (effectiveStatus) {
        ProtectionStatus.PERMISSION_NEEDED -> "Off"
        ProtectionStatus.SERVICE_DISCONNECTED -> "Disconnected"
        ProtectionStatus.NO_APPS_ACTIVE -> "0 apps"
        ProtectionStatus.PAUSED -> "Paused"
        ProtectionStatus.OPERATIONAL -> "Ready"
    }
    val statusSubtitle = when (effectiveStatus) {
        ProtectionStatus.PERMISSION_NEEDED ->
            "Turn on Stillspace in Android Settings"
        ProtectionStatus.SERVICE_DISCONNECTED ->
            "Service stopped by Android. Toggle off and on in Settings."
        ProtectionStatus.NO_APPS_ACTIVE ->
            "No installed apps are enabled on Home"
        ProtectionStatus.PAUSED ->
            if (uiState.globalPauseUntilMs == Long.MAX_VALUE) {
                "Paused until you turn it back on"
            } else {
                "Paused \u00B7 $globalCountdown remaining"
            }
        ProtectionStatus.OPERATIONAL ->
            "Guarding $activeInstalledCount installed app${if (activeInstalledCount == 1) "" else "s"}"
    }

    SurgeonCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HabitIconBadge(
                icon = Icons.Outlined.Shield,
                tint = if (effectiveStatus == ProtectionStatus.OPERATIONAL) neu.statusActiveText else neu.statusWarningText,
                containerColor = if (effectiveStatus == ProtectionStatus.OPERATIONAL) neu.statusActiveBg else neu.statusWarningBg
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Protection status",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.testTag("settings_protection_subtitle")
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            StatusBadgePill(
                text = pillText,
                isWarning = effectiveStatus != ProtectionStatus.OPERATIONAL,
                showDot = true,
                modifier = Modifier.testTag("settings_protection_pill")
            )
        }

        if (effectiveStatus == ProtectionStatus.OPERATIONAL || effectiveStatus == ProtectionStatus.PAUSED) {
            GroupedListDivider(
                modifier = Modifier.padding(start = 54.dp, top = 14.dp, bottom = 14.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HabitIconBadge(
                    icon = Icons.Outlined.Pause,
                    tint = neu.statusWarningText,
                    containerColor = neu.statusWarningBg
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pause all blocking",
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )
                    Text(
                        text = if (isGloballyPausedNow && uiState.globalPauseUntilMs != Long.MAX_VALUE) {
                            "Resumes automatically in $globalCountdown"
                        } else {
                            "Temporarily allow Shorts & Reels everywhere"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                SurgeonSwitch(
                    checked = isGloballyPausedNow,
                    onCheckedChange = onToggleGlobalPause,
                    testTag = "settings_global_pause_switch"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { AccessibilityStatusHelper.openAccessibilitySettings(context) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .neumorphicExtruded(
                        cornerRadius = 16.dp,
                        elevation = 4.dp,
                        containerColor = neu.primaryActionBg,
                        borderColor = Color.Transparent,
                        shadowTint = neu.primaryActionBg.copy(alpha = 0.25f)
                    )
                    .testTag("settings_accessibility_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
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
            OutlinedButton(
                onClick = onReopenDisclosure,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("view_disclosure_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = neu.secondaryButtonBg,
                    contentColor = scheme.onSurface
                )
            ) {
                Text(
                    text = "Guided setup",
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

@Composable
private fun ExperienceCard(
    prefs: AppPreferences,
    onSetShowBanner: (Boolean) -> Unit,
    onSetReclaimedSeconds: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    SurgeonCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HabitIconBadge(
                icon = Icons.Outlined.Bolt,
                tint = scheme.onPrimaryContainer,
                containerColor = scheme.primaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quick pass bar",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Text(
                    text = "Show a 5-min pass / Undo bar after stepping in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            SurgeonSwitch(
                checked = prefs.showInterventionBanner,
                onCheckedChange = onSetShowBanner,
                testTag = "settings_banner_switch"
            )
        }

        GroupedListDivider(
            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
        )

        val minutePresets = listOf(60 to "1 min", 120 to "2 min", 180 to "3 min", 300 to "5 min")
        val selectedPreset = minutePresets.minByOrNull { (sec, _) ->
            kotlin.math.abs(sec - prefs.reclaimedSecondsPerCut)
        } ?: (180 to "3 min")
        Text(
            text = "Time saved per avoided feed",
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        NeumorphicSegmentedTrack(
            items = minutePresets,
            selectedItem = selectedPreset,
            labelProvider = { it.second },
            testTagProvider = { "time_saved_preset_${it.first}s" },
            onSelect = { (seconds, _) -> onSetReclaimedSeconds(seconds) }
        )
    }
}

@Composable
private fun PrivacyActionsCard(
    onShowPausePreview: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onRequestClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    SurgeonCard(modifier = modifier, contentPadding = PaddingValues(0.dp)) {
        ActionRowItem(
            icon = Icons.Outlined.Visibility,
            iconTint = scheme.onPrimaryContainer,
            iconBg = scheme.primaryContainer,
            title = "Preview pause screen",
            textColor = scheme.onSurface,
            showChevron = true,
            testTag = "settings_preview_pause_button",
            onClick = onShowPausePreview
        )
        GroupedListDivider(modifier = Modifier.padding(start = 70.dp, end = 16.dp))
        ActionRowItem(
            icon = Icons.Outlined.Lock,
            iconTint = neu.statusActiveText,
            iconBg = neu.statusActiveBg,
            title = "Privacy Policy",
            textColor = scheme.onSurface,
            showChevron = true,
            testTag = "settings_privacy_policy_button",
            onClick = onOpenPrivacyPolicy
        )
        GroupedListDivider(modifier = Modifier.padding(start = 70.dp, end = 16.dp))
        ActionRowItem(
            icon = Icons.Outlined.DeleteOutline,
            iconTint = neu.statusDanger,
            iconBg = neu.statusDangerBg,
            title = "Clear my saved stats",
            textColor = neu.statusDanger,
            showChevron = false,
            testTag = "clear_history_button",
            onClick = onRequestClearHistory
        )
    }
}

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    textColor: Color,
    showChevron: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HabitIconBadge(
            icon = icon,
            tint = iconTint,
            containerColor = iconBg,
            badgeSize = 40.dp,
            iconSize = 19.dp,
            cornerRadius = 12.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppearanceCard(
    selectedMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    SurgeonCard(modifier = modifier.testTag("settings_appearance_card")) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Choose Light, Midnight Indigo Dark, or match system",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        NeumorphicSegmentedTrack(
            items = ThemeMode.entries,
            selectedItem = selectedMode,
            labelProvider = { it.label },
            testTagProvider = { "theme_mode_${it.name.lowercase()}" },
            onSelect = onSelectMode
        )
    }
}

@Composable
private fun BlockerUpdatesCard(
    uiState: MainUiState,
    prefs: AppPreferences,
    sortedRules: List<TargetAppRuleEntity>,
    onSetAutoUpdateRules: (Boolean) -> Unit,
    onCheckRulesNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
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
    SurgeonCard(modifier = modifier) {
        SectionLabel(text = "FOCUS RULE UPDATES & SYNC")
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto-update focus rules",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lastCheckedSubtitle,
                    style = MonoTypography.caption,
                    color = scheme.onSurfaceVariant
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
                    color = scheme.primary,
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
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            border = BorderStroke(1.dp, neu.secondaryButtonBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = neu.secondaryButtonBg,
                contentColor = scheme.onSurface
            )
        ) {
            if (uiState.isSyncingRules) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = scheme.primary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isSyncingRules) "Checking..." else "Check for updates",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
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
                            color = scheme.onSurface
                        )
                        Text(
                            text = rule.feedLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    if (!isInstalled) {
                        Text(
                            text = "Not installed",
                            style = MaterialTheme.typography.labelMedium,
                            color = neu.textMuted
                        )
                    } else {
                        StatusBadgePill(
                            text = if (rule.isOutdated) "Needs update" else "Synced",
                            isWarning = rule.isOutdated
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundReliabilityCard(
    uiState: MainUiState,
    context: Context,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    SurgeonCard(modifier = modifier) {
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
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (uiState.isBatteryOptimizationIgnored) {
                        "Unrestricted \u2014 stays running reliably in the background"
                    } else {
                        "Phone battery saver may pause Stillspace in the background"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
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
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
                border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = neu.secondaryButtonBg,
                    contentColor = scheme.primary
                )
            ) {
                Text(
                    text = "Unrestricted battery",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
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
                        color = scheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (uiState.isNotificationPermissionGranted) {
                            "Active notification badge allowed"
                        } else {
                            "Allow notifications so Android keeps protection active"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
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
                    onClick = onRequestNotificationPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_notification_permission_button"),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = neu.secondaryButtonBg,
                        contentColor = scheme.onSurface
                    )
                ) {
                    Text(
                        text = "Enable notifications",
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

@Composable
private fun DiagnosticLogCard(
    uiState: MainUiState,
    prefs: AppPreferences,
    onSetEventCaptureEnabled: (Boolean) -> Unit,
    onViewLog: () -> Unit,
    onClearEventLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val kb = (uiState.eventLogSizeBytes / 1024L).coerceAtLeast(
        if (uiState.eventLogSizeBytes > 0L) 1L else 0L
    )
    SurgeonCard(modifier = modifier) {
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
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${AccessibilityEventLogger.LOG_FILE_NAME} \u00B7 $kb KB",
                    style = MonoTypography.caption,
                    color = scheme.onSurfaceVariant
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
                onClick = onViewLog,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("view_event_log_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = neu.secondaryButtonBg,
                    contentColor = scheme.onSurface
                )
            ) {
                Text(
                    text = "View log",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = onClearEventLog,
                enabled = uiState.eventLogSizeBytes > 0L,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("clear_event_log_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = neu.secondaryButtonBg,
                    contentColor = neu.statusDanger
                )
            ) {
                Text(
                    text = "Clear log",
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
