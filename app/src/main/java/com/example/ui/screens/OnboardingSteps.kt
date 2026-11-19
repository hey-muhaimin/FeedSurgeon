package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SettingsAccessibility
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.TargetAppRuleEntity
import com.example.service.ProtectionHealthHelper
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.GroupedListDivider
import com.example.ui.components.HabitIconBadge
import com.example.ui.components.OnboardingPhoneIllustration
import com.example.ui.components.OnboardingReadyCelebrationBadge
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.theme.StillspaceTheme

@Composable
internal fun OnboardingWelcomeStep(
    isWideLayout: Boolean
) {
    val scrollModifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())

    if (isWideLayout) {
        Row(
            modifier = scrollModifier
                .padding(top = 12.dp, bottom = 12.dp)
                .testTag("onboarding_welcome_wide_pane"),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1.02f)) {
                WelcomeHeroBlock()
            }
            Column(modifier = Modifier.weight(0.98f)) {
                WelcomeDisclosureBlock()
            }
        }
    } else {
        Column(modifier = scrollModifier) {
            Spacer(modifier = Modifier.height(4.dp))
            WelcomeHeroBlock()
            Spacer(modifier = Modifier.height(18.dp))
            WelcomeDisclosureBlock()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WelcomeHeroBlock() {
    val scheme = MaterialTheme.colorScheme

    OnboardingPhoneIllustration()

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = "Take back your focus",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 30.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.6).sp
        ),
        color = scheme.onBackground,
        modifier = Modifier.testTag("onboarding_hero_title")
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Stillspace gently steps in when short-form videos open in apps you choose, so you can scroll with intention.",
        style = MaterialTheme.typography.bodyLarge,
        color = scheme.onSurfaceVariant
    )
}

@Composable
private fun WelcomeDisclosureBlock() {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    SurgeonCard(contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            HabitIconBadge(
                icon = Icons.Outlined.Visibility,
                tint = scheme.onPrimaryContainer,
                containerColor = scheme.primaryContainer,
                badgeSize = 44.dp,
                iconSize = 20.dp,
                cornerRadius = 14.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "What it checks",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.onboarding_what_we_read_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    SurgeonCard(contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            HabitIconBadge(
                icon = Icons.Outlined.Lock,
                tint = neu.statusActiveText,
                containerColor = neu.statusActiveBg,
                badgeSize = 44.dp,
                iconSize = 20.dp,
                cornerRadius = 14.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "What stays private",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.onboarding_what_we_never_send_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Optional troubleshooting logs are off by default in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = neu.textMuted,
                    modifier = Modifier.testTag("onboarding_diagnostic_disclosure_note")
                )
            }
        }
    }
}

@Composable
internal fun OnboardingAppsStep(
    sortedRules: List<TargetAppRuleEntity>,
    installedPackages: Set<String>,
    isWideLayout: Boolean,
    onToggleApp: (String, Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val installedRules = remember(sortedRules, installedPackages) {
        sortedRules.filter { it.packageName in installedPackages }
    }
    val notInstalledRules = remember(sortedRules, installedPackages) {
        sortedRules.filter { it.packageName !in installedPackages }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        HabitIconBadge(
            icon = Icons.Outlined.GridView,
            tint = scheme.onPrimaryContainer,
            containerColor = scheme.primaryContainer,
            badgeSize = 48.dp,
            iconSize = 22.dp,
            cornerRadius = 14.dp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Choose apps to guard",
            style = MaterialTheme.typography.headlineLarge,
            color = scheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pick which short-form feeds you want Stillspace to watch out for. You can change these anytime.",
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (installedRules.isNotEmpty()) {
            Text(
                text = "Installed on your phone",
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.testTag("onboarding_installed_section")
            )
            Spacer(modifier = Modifier.height(10.dp))
            OnboardingAppRuleGroup(
                rules = installedRules,
                isWideLayout = isWideLayout,
                isDimmed = false,
                onToggleApp = onToggleApp,
                gridTag = if (isWideLayout) "onboarding_apps_wide_grid" else null
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        if (notInstalledRules.isNotEmpty()) {
            Text(
                text = "Not installed yet",
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.testTag("onboarding_not_installed_section")
            )
            Spacer(modifier = Modifier.height(10.dp))
            OnboardingAppRuleGroup(
                rules = notInstalledRules,
                isWideLayout = isWideLayout,
                isDimmed = true,
                onToggleApp = onToggleApp,
                gridTag = null
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun OnboardingAppRuleGroup(
    rules: List<TargetAppRuleEntity>,
    isWideLayout: Boolean,
    isDimmed: Boolean,
    onToggleApp: (String, Boolean) -> Unit,
    gridTag: String?
) {
    val containerModifier = if (gridTag != null) Modifier.testTag(gridTag) else Modifier
    if (isWideLayout) {
        Column(
            modifier = containerModifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rules.chunked(2).forEach { rowRules ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowRules.forEach { rule ->
                        SurgeonCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            OnboardingAppRuleRowContent(
                                rule = rule,
                                isDimmed = isDimmed,
                                onToggleApp = onToggleApp
                            )
                        }
                    }
                    if (rowRules.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        SurgeonCard(
            modifier = containerModifier,
            contentPadding = PaddingValues(0.dp)
        ) {
            rules.forEachIndexed { index, rule ->
                OnboardingAppRuleRowContent(
                    rule = rule,
                    isDimmed = isDimmed,
                    onToggleApp = onToggleApp
                )
                if (index < rules.lastIndex) {
                    GroupedListDivider(
                        modifier = Modifier.padding(start = 74.dp, end = 18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingAppRuleRowContent(
    rule: TargetAppRuleEntity,
    isDimmed: Boolean,
    onToggleApp: (String, Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleApp(rule.packageName, !rule.enabled) }
            .then(if (isDimmed) Modifier.alpha(0.78f) else Modifier)
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
            Text(
                text = "Block ${rule.feedLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDimmed) neu.textMuted else scheme.onSurfaceVariant
            )
        }
        SurgeonSwitch(
            checked = rule.enabled,
            onCheckedChange = { enabled -> onToggleApp(rule.packageName, enabled) },
            testTag = "onboarding_app_toggle_${rule.packageName}"
        )
    }
}

@Composable
internal fun OnboardingAccessibilityStep(
    isAccessibilityEnabled: Boolean,
    openedSystemSettings: Boolean,
    showRestrictedTip: Boolean,
    isWideLayout: Boolean,
    onToggleRestrictedTip: () -> Unit
) {
    val scrollModifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())

    if (isWideLayout) {
        Row(
            modifier = scrollModifier
                .padding(top = 12.dp, bottom = 12.dp)
                .testTag("onboarding_accessibility_wide_pane"),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(0.95f)) {
                AccessibilityStatusColumn(
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    openedSystemSettings = openedSystemSettings
                )
            }
            Column(modifier = Modifier.weight(1.05f)) {
                AccessibilityGuideColumn(
                    showRestrictedTip = showRestrictedTip,
                    onToggleRestrictedTip = onToggleRestrictedTip
                )
            }
        }
    } else {
        Column(modifier = scrollModifier) {
            Spacer(modifier = Modifier.height(6.dp))
            AccessibilityStatusColumn(
                isAccessibilityEnabled = isAccessibilityEnabled,
                openedSystemSettings = openedSystemSettings
            )
            Spacer(modifier = Modifier.height(14.dp))
            AccessibilityGuideColumn(
                showRestrictedTip = showRestrictedTip,
                onToggleRestrictedTip = onToggleRestrictedTip
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AccessibilityStatusColumn(
    isAccessibilityEnabled: Boolean,
    openedSystemSettings: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    HabitIconBadge(
        icon = Icons.Outlined.SettingsAccessibility,
        tint = scheme.onPrimaryContainer,
        containerColor = scheme.primaryContainer,
        badgeSize = 48.dp,
        iconSize = 22.dp,
        cornerRadius = 14.dp
    )

    Spacer(modifier = Modifier.height(18.dp))

    Text(
        text = "Turn on protection",
        style = MaterialTheme.typography.headlineLarge,
        color = scheme.onBackground
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Android asks you to turn on Stillspace in Accessibility Settings so it can spot when a short-form feed opens.",
        style = MaterialTheme.typography.bodyLarge,
        color = scheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(18.dp))

    SurgeonCard(
        containerColor = if (isAccessibilityEnabled) neu.statusActiveBg else neu.statusWarningBg,
        borderColor = if (isAccessibilityEnabled) neu.statusActiveBorder else neu.statusWarningBorder,
        elevation = 2.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isAccessibilityEnabled) neu.statusActive else neu.statusWarning),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAccessibilityEnabled) Icons.Rounded.Check else Icons.Rounded.HourglassTop,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAccessibilityEnabled) {
                        "Protection is turned on"
                    } else {
                        "Waiting for permission"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isAccessibilityEnabled) neu.statusActiveText else neu.statusWarningText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isAccessibilityEnabled) {
                        "You\u2019re ready to continue to the final step."
                    } else {
                        "Follow these quick steps, then come back to this screen."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAccessibilityEnabled) {
                        neu.statusActiveText.copy(alpha = 0.85f)
                    } else {
                        neu.statusWarningText.copy(alpha = 0.85f)
                    }
                )
            }
        }
    }

    if (openedSystemSettings && !isAccessibilityEnabled) {
        Spacer(modifier = Modifier.height(14.dp))
        SurgeonCard(
            borderColor = neu.statusWarningBorder,
            modifier = Modifier.testTag("onboarding_step2_recovery_card")
        ) {
            Text(
                text = "Still turned off?",
                style = MaterialTheme.typography.titleMedium,
                color = neu.statusWarningText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap Open Settings again, open Downloaded apps \u2192 Stillspace, and switch it on. If it is grayed out, expand the restricted settings tip below.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccessibilityGuideColumn(
    showRestrictedTip: Boolean,
    onToggleRestrictedTip: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    SurgeonCard(contentPadding = PaddingValues(18.dp)) {
        Text(
            text = "How to turn it on",
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface
        )
        Spacer(modifier = Modifier.height(14.dp))
        VisualGuideStep(
            number = "1",
            title = "Tap Open Settings",
            detail = "Takes you straight to Android's Accessibility page"
        )
        GroupedListDivider(
            modifier = Modifier.padding(start = 46.dp, top = 12.dp, bottom = 12.dp)
        )
        VisualGuideStep(
            number = "2",
            title = "Tap Downloaded apps",
            detail = "Usually near the middle of the list"
        )
        GroupedListDivider(
            modifier = Modifier.padding(start = 46.dp, top = 12.dp, bottom = 12.dp)
        )
        VisualGuideStep(
            number = "3",
            title = "Select Stillspace",
            detail = "Switch it ON and tap Allow"
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    SurgeonCard(
        contentPadding = PaddingValues(16.dp),
        onClick = onToggleRestrictedTip
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Switch grayed out or blocked?",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface
            )
            Icon(
                imageVector = if (showRestrictedTip) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                },
                contentDescription = "Toggle restricted settings help",
                tint = scheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(
            visible = showRestrictedTip,
            enter = fadeIn(tween(180)) + expandVertically(),
            exit = fadeOut(tween(140)) + shrinkVertically()
        ) {
            Text(
                text = "On newer Android phones, open phone Settings \u2192 Apps \u2192 Stillspace \u2192 tap the \u22EE menu in the top corner \u2192 Allow restricted settings, then return here.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
internal fun OnboardingReadyStep(
    guardedCount: Int,
    isBatteryOptimizationIgnored: Boolean,
    isNotificationPermissionGranted: Boolean,
    isWideLayout: Boolean,
    onRefreshProtectionHealth: () -> Unit
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshProtectionHealth()
    }
    val scrollModifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())

    if (isWideLayout) {
        Row(
            modifier = scrollModifier
                .padding(top = 12.dp, bottom = 12.dp)
                .testTag("onboarding_ready_wide_pane"),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(0.95f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReadyHeaderBlock()
            }
            Column(modifier = Modifier.weight(1.05f)) {
                ReadySummaryBlock(
                    guardedCount = guardedCount,
                    isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                    isNotificationPermissionGranted = isNotificationPermissionGranted,
                    onRequestBatteryOptimization = {
                        ProtectionHealthHelper.requestIgnoreBatteryOptimizations(context)
                    },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    } else {
        Column(
            modifier = scrollModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            ReadyHeaderBlock()
            Spacer(modifier = Modifier.height(24.dp))
            ReadySummaryBlock(
                guardedCount = guardedCount,
                isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                isNotificationPermissionGranted = isNotificationPermissionGranted,
                onRequestBatteryOptimization = {
                    ProtectionHealthHelper.requestIgnoreBatteryOptimizations(context)
                },
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReadyHeaderBlock() {
    val scheme = MaterialTheme.colorScheme

    OnboardingReadyCelebrationBadge()

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "You\u2019re all set",
        style = MaterialTheme.typography.headlineLarge,
        color = scheme.onBackground,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Stillspace runs quietly on your phone, uses minimal battery, and can be paused anytime with one tap.",
        style = MaterialTheme.typography.bodyLarge,
        color = scheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ReadySummaryBlock(
    guardedCount: Int,
    isBatteryOptimizationIgnored: Boolean,
    isNotificationPermissionGranted: Boolean,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    SurgeonCard(contentPadding = PaddingValues(18.dp)) {
        Text(
            text = "What happens next",
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        val appsSummary = if (guardedCount > 0) {
            "Guarding $guardedCount installed app${if (guardedCount == 1) "" else "s"}"
        } else {
            "Ready whenever you enable supported apps"
        }
        ReadyNextRow(
            icon = Icons.Outlined.Shield,
            tint = neu.statusActiveText,
            containerColor = neu.statusActiveBg,
            label = appsSummary
        )
        GroupedListDivider(
            modifier = Modifier.padding(start = 52.dp, top = 12.dp, bottom = 12.dp)
        )
        ReadyNextRow(
            icon = Icons.Outlined.Schedule,
            tint = scheme.onPrimaryContainer,
            containerColor = scheme.primaryContainer,
            label = "Take a 5, 15 or 60-minute break anytime"
        )
        GroupedListDivider(
            modifier = Modifier.padding(start = 52.dp, top = 12.dp, bottom = 12.dp)
        )
        ReadyNextRow(
            icon = Icons.Outlined.Lock,
            tint = neu.statusActiveText,
            containerColor = neu.statusActiveBg,
            label = "All stats stay private on your device"
        )
    }

    if (!isBatteryOptimizationIgnored) {
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = onRequestBatteryOptimization,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("onboarding_battery_optimization_button"),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            border = BorderStroke(1.dp, neu.secondaryButtonBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = neu.secondaryButtonBg,
                contentColor = scheme.primary
            )
        ) {
            Text(
                text = "Keep active in background",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationPermissionGranted) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRequestNotificationPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("onboarding_notification_permission_button"),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
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

@Composable
private fun ReadyNextRow(
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    label: String
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HabitIconBadge(
            icon = icon,
            tint = tint,
            containerColor = containerColor,
            badgeSize = 38.dp,
            iconSize = 18.dp,
            cornerRadius = 11.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = scheme.onSurface
        )
    }
}

@Composable
private fun VisualGuideStep(
    number: String,
    title: String,
    detail: String
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}
