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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SettingsAccessibility
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.TargetAppRuleEntity
import com.example.service.ProtectionHealthHelper
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBorder
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CalmSageBorder
import com.example.ui.theme.CalmSageDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.ElectricAmberBorder
import com.example.ui.theme.ElectricAmberDark
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.specularRimBrush

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OnboardingWelcomeStep(
    rules: List<TargetAppRuleEntity>,
    isWideLayout: Boolean
) {
    val showcasePackages = remember(rules) {
        val defaults = listOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.reddit.frontpage"
        )
        val fromRules = rules.map { it.packageName }
        (fromRules.ifEmpty { defaults }).distinct().take(5)
    }

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
            Column(modifier = Modifier.weight(1.05f)) {
                WelcomeHeroBlock(showcasePackages = showcasePackages)
            }
            Column(modifier = Modifier.weight(0.95f)) {
                WelcomeDisclosureBlock()
            }
        }
    } else {
        Column(modifier = scrollModifier) {
            WelcomeHeroBlock(showcasePackages = showcasePackages)
            Spacer(modifier = Modifier.height(20.dp))
            WelcomeDisclosureBlock()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeHeroBlock(showcasePackages: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ElectricAmberDark.copy(alpha = 0.80f))
                .border(
                    1.dp,
                    specularRimBrush(ElectricAmberBorder, isHighlighted = true),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shield_notification),
                contentDescription = "Stillspace",
                tint = ElectricAmber,
                modifier = Modifier.size(26.dp)
            )
        }
        StatusBadgePill(text = "100% On-Device", isWarning = false)
    }

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.onboarding_title_lead))
            append("\n")
            withStyle(SpanStyle(color = ElectricAmber)) {
                append(stringResource(R.string.onboarding_title_accent))
            }
        },
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 31.sp,
            lineHeight = 37.sp,
            letterSpacing = (-0.7).sp
        ),
        color = TextPrimary,
        modifier = Modifier.testTag("onboarding_hero_title")
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = stringResource(R.string.onboarding_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_feature_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OnboardingGlassFeatureChip(
            icon = Icons.Outlined.Bolt,
            label = "Instant Loop Break",
            modifier = Modifier.testTag("onboarding_feature_chip_cut")
        )
        OnboardingGlassFeatureChip(
            icon = Icons.Outlined.Lock,
            label = "100% On-Device",
            modifier = Modifier.testTag("onboarding_feature_chip_privacy")
        )
        OnboardingGlassFeatureChip(
            icon = Icons.Outlined.Timer,
            label = "5-Min Intentional Pass",
            modifier = Modifier.testTag("onboarding_feature_chip_pass")
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_supported_logos_row"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        showcasePackages.forEach { pkg ->
            AppBadgeIcon(packageName = pkg, badgeSize = 38.dp)
        }
    }
}

@Composable
private fun WelcomeDisclosureBlock() {
    SurgeonCard(borderColor = CalmSageBorder) {
        SectionLabel(text = "PRIVACY & ACCESSIBILITY")
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = null,
                tint = CalmSage,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Checks active short-form feeds",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.onboarding_what_we_read_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = CardBorder
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = CalmSage,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Everything stays on your phone",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.onboarding_what_we_never_send_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Optional troubleshooting logs are off by default. If enabled in Settings \u2192 Advanced, only technical screen IDs are saved locally until you choose to share them.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.testTag("onboarding_diagnostic_disclosure_note")
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Declining leaves protection off.",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
        modifier = Modifier.testTag("onboarding_decline_note")
    )
}

@Composable
internal fun OnboardingAppsStep(
    sortedRules: List<TargetAppRuleEntity>,
    installedPackages: Set<String>,
    isWideLayout: Boolean,
    onToggleApp: (String, Boolean) -> Unit
) {
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
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ElectricAmberDark.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                tint = ElectricAmber,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = stringResource(R.string.onboarding_apps_title),
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.onboarding_apps_body),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (installedRules.isNotEmpty()) {
            SectionLabel(
                text = "INSTALLED ON YOUR PHONE",
                modifier = Modifier.testTag("onboarding_installed_section")
            )
            Spacer(modifier = Modifier.height(10.dp))
            OnboardingAppRuleGrid(
                rules = installedRules,
                isWideLayout = isWideLayout,
                isDimmed = false,
                onToggleApp = onToggleApp,
                gridTag = if (isWideLayout) "onboarding_apps_wide_grid" else null
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        if (notInstalledRules.isNotEmpty()) {
            SectionLabel(
                text = "NOT INSTALLED YET",
                modifier = Modifier.testTag("onboarding_not_installed_section")
            )
            Spacer(modifier = Modifier.height(10.dp))
            OnboardingAppRuleGrid(
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
private fun OnboardingAppRuleGrid(
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
                        Box(modifier = Modifier.weight(1f)) {
                            OnboardingAppRuleCard(rule = rule, isDimmed = isDimmed, onToggleApp = onToggleApp)
                        }
                    }
                    if (rowRules.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Column(
            modifier = containerModifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rules.forEach { rule ->
                OnboardingAppRuleCard(rule = rule, isDimmed = isDimmed, onToggleApp = onToggleApp)
            }
        }
    }
}

@Composable
private fun OnboardingAppRuleCard(
    rule: TargetAppRuleEntity,
    isDimmed: Boolean,
    onToggleApp: (String, Boolean) -> Unit
) {
    SurgeonCard(
        modifier = if (isDimmed) Modifier.alpha(0.72f) else Modifier,
        onClick = { onToggleApp(rule.packageName, !rule.enabled) }
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isDimmed) {
                        "Guard ${rule.feedLabel} \u00B7 Not installed yet"
                    } else {
                        "Guard ${rule.feedLabel}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDimmed) TextMuted else TextSecondary
                )
            }
            SurgeonSwitch(
                checked = rule.enabled,
                onCheckedChange = { enabled -> onToggleApp(rule.packageName, enabled) },
                testTag = "onboarding_app_toggle_${rule.packageName}"
            )
        }
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
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ElectricAmberDark.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SettingsAccessibility,
            contentDescription = null,
            tint = ElectricAmber,
            modifier = Modifier.size(28.dp)
        )
    }

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = stringResource(R.string.onboarding_step2_title),
        style = MaterialTheme.typography.headlineLarge,
        color = TextPrimary
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = stringResource(R.string.onboarding_step2_body),
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary
    )

    Spacer(modifier = Modifier.height(18.dp))

    SurgeonCard(
        borderColor = if (isAccessibilityEnabled) CalmSageBorder else AmberWarningBorder
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isAccessibilityEnabled) CalmSage else AmberWarning)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isAccessibilityEnabled) {
                    "Focus Guard is turned on!"
                } else {
                    "Waiting for permission"
                },
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isAccessibilityEnabled) {
                "You're ready to continue to the final step."
            } else {
                "Follow these quick steps, then come back to this screen."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }

    if (openedSystemSettings && !isAccessibilityEnabled) {
        Spacer(modifier = Modifier.height(14.dp))
        SurgeonCard(
            borderColor = AmberWarningBorder,
            modifier = Modifier.testTag("onboarding_step2_recovery_card")
        ) {
            Text(
                text = "Still turned off?",
                style = MaterialTheme.typography.titleMedium,
                color = AmberWarning
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap Open System Settings again, open Downloaded apps \u2192 Stillspace, and switch it on. If it is grayed out, expand the restricted settings tip below.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AccessibilityGuideColumn(
    showRestrictedTip: Boolean,
    onToggleRestrictedTip: () -> Unit
) {
    SurgeonCard {
        Text(
            text = "HOW TO TURN IT ON",
            style = MaterialTheme.typography.labelSmall,
            color = ElectricAmber
        )
        Spacer(modifier = Modifier.height(14.dp))
        VisualGuideStep(
            number = "1",
            title = "Tap Open System Settings below",
            detail = "Takes you straight to Android's Accessibility page"
        )
        Spacer(modifier = Modifier.height(12.dp))
        VisualGuideStep(
            number = "2",
            title = "Tap Downloaded apps",
            detail = "Usually near the middle of the Accessibility list"
        )
        Spacer(modifier = Modifier.height(12.dp))
        VisualGuideStep(
            number = "3",
            title = "Select Stillspace",
            detail = "Switch Use Stillspace ON and tap Allow"
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    SurgeonCard(onClick = onToggleRestrictedTip) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Switch grayed out or blocked?",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Icon(
                imageVector = if (showRestrictedTip) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                },
                contentDescription = "Toggle restricted settings help",
                tint = TextSecondary
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
                color = TextSecondary,
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
            Column(modifier = Modifier.weight(0.95f)) {
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
        Column(modifier = scrollModifier) {
            ReadyHeaderBlock()
            Spacer(modifier = Modifier.height(22.dp))
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
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CalmSageDark.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = CalmSage,
            modifier = Modifier.size(28.dp)
        )
    }

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = stringResource(R.string.onboarding_step3_title),
        style = MaterialTheme.typography.headlineLarge,
        color = TextPrimary
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = stringResource(R.string.onboarding_step3_body),
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary
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
    SurgeonCard {
        Text(
            text = "WHAT HAPPENS NEXT",
            style = MaterialTheme.typography.labelSmall,
            color = CalmSage
        )
        Spacer(modifier = Modifier.height(12.dp))
        val appsSummary = if (guardedCount > 0) {
            "Protecting focus across $guardedCount installed app${if (guardedCount == 1) "" else "s"}"
        } else {
            "Ready whenever you install or enable apps"
        }
        Text(
            text = "\u2022 $appsSummary\n\u2022 Take a 5, 15, or 60-minute intentional break anytime\n\u2022 All focus stats stay completely private on your device",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
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
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CardSurfaceElevated,
                contentColor = ElectricAmber
            )
        ) {
            Text("Keep running smoothly in background", fontWeight = FontWeight.SemiBold)
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
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CardSurfaceElevated,
                contentColor = TextPrimary
            )
        ) {
            Text("Show active focus notification")
        }
    }
}

@Composable
private fun VisualGuideStep(
    number: String,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(ElectricAmberDark)
                .border(1.dp, ElectricAmberBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                color = ElectricAmber,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun OnboardingGlassFeatureChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(GlassSurfaceElevated.copy(alpha = 0.92f))
            .border(
                1.dp,
                specularRimBrush(CalmSageBorder.copy(alpha = 0.65f)),
                RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricAmber,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.2.sp),
            color = TextPrimary,
            maxLines = 1,
            softWrap = false
        )
    }
}
