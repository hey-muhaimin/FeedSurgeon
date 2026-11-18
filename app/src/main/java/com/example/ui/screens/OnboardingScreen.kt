package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.SettingsAccessibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TargetAppRuleEntity
import com.example.service.AccessibilityStatusHelper
import com.example.service.ProtectionHealthHelper
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.CircularBackButton
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.SectionLabel
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBorder
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MintTealBorder
import com.example.ui.theme.MintTealDark
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    rules: List<TargetAppRuleEntity>,
    installedPackages: Set<String>,
    isAccessibilityEnabled: Boolean,
    isBatteryOptimizationIgnored: Boolean = false,
    isNotificationPermissionGranted: Boolean = true,
    onRefreshProtectionHealth: () -> Unit = {},
    onToggleApp: (String, Boolean) -> Unit,
    onAcceptDisclosure: () -> Unit,
    onDeclineDisclosure: () -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    var step by remember { mutableIntStateOf(0) }
    var showRestrictedTip by remember { mutableStateOf(false) }
    var showPausePreview by remember { mutableStateOf(false) }
    var openedSystemSettings by remember { mutableStateOf(false) }

    BackHandler(enabled = step > 0) {
        step = (step - 1).coerceAtLeast(0)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshProtectionHealth()
    }

    val sortedRules = remember(rules, installedPackages) {
        rules.sortedWith(
            compareByDescending<TargetAppRuleEntity> { it.packageName in installedPackages }
                .thenBy { it.displayName }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 4-step top indicator bar + back button on steps 1..3
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 0) {
                CircularBackButton(onClick = { step = (step - 1).coerceAtLeast(0) })
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { index ->
                    val segmentColor by animateColorAsState(
                        targetValue = if (index <= step) MintTeal else CardBorder,
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        label = "step_segment_$index"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(segmentColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = targetState >= initialState
                (fadeIn(tween(220)) + slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) {
                    if (forward) it / 4 else -it / 4
                }) togetherWith (fadeOut(tween(160)) + slideOutHorizontally(tween(200)) {
                    if (forward) -it / 4 else it / 4
                })
            },
            label = "onboarding_step_transition"
        ) { currentStep ->
            Column(modifier = Modifier.fillMaxSize()) {
                when (currentStep) {
                    // Step 0: Privacy Promise
                    0 -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_onboarding_sanctuary),
                                contentDescription = "Calm digital sanctuary illustration",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = stringResource(R.string.onboarding_title),
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(R.string.onboarding_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            SurgeonCard {
                                Text(
                                    text = stringResource(R.string.onboarding_what_we_read_title),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintTeal
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.onboarding_what_we_read_body),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SurgeonCard {
                                Text(
                                    text = stringResource(R.string.onboarding_what_we_never_send_title),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoralDanger
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.onboarding_what_we_never_send_body),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Optional troubleshooting logs are off by default. If enabled in Settings \u2192 Advanced, only technical screen IDs are saved locally until you choose to share them.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.testTag("onboarding_diagnostic_disclosure_note")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Choosing Not now leaves protection off until you enable it later.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.testTag("onboarding_decline_note")
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { uriHandler.openUri(privacyPolicyUrl) },
                                modifier = Modifier.testTag("onboarding_privacy_link")
                            ) {
                                Text(
                                    text = "Read Privacy Policy \u00B7 suprbuild.com/privacy",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MintTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDeclineDisclosure,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("onboarding_decline_button"),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CardSurface,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.onboarding_decline),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Button(
                                onClick = {
                                    onAcceptDisclosure()
                                    step = 1
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("onboarding_agree_button"),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = ObsidianBg
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.onboarding_agree),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Step 1: Choose Apps to Guard
                    1 -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MintTealDark.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Apps,
                                    contentDescription = null,
                                    tint = MintTeal,
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

                            val installedRules = sortedRules.filter { it.packageName in installedPackages }
                            val notInstalledRules = sortedRules.filter { it.packageName !in installedPackages }

                            if (installedRules.isNotEmpty()) {
                                SectionLabel(
                                    text = "INSTALLED ON YOUR PHONE",
                                    modifier = Modifier.testTag("onboarding_installed_section")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    installedRules.forEach { rule ->
                                        SurgeonCard(
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
                                                        text = "Block ${rule.feedLabel}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = TextSecondary
                                                    )
                                                }
                                                SurgeonSwitch(
                                                    checked = rule.enabled,
                                                    onCheckedChange = { enabled ->
                                                        onToggleApp(rule.packageName, enabled)
                                                    },
                                                    testTag = "onboarding_app_toggle_${rule.packageName}"
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                            }

                            if (notInstalledRules.isNotEmpty()) {
                                SectionLabel(
                                    text = "NOT INSTALLED YET",
                                    modifier = Modifier.testTag("onboarding_not_installed_section")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    notInstalledRules.forEach { rule ->
                                        SurgeonCard(
                                            modifier = Modifier.alpha(0.72f),
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
                                                        text = "Block ${rule.feedLabel} \u00B7 Not installed yet",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = TextMuted
                                                    )
                                                }
                                                SurgeonSwitch(
                                                    checked = rule.enabled,
                                                    onCheckedChange = { enabled ->
                                                        onToggleApp(rule.packageName, enabled)
                                                    },
                                                    testTag = "onboarding_app_toggle_${rule.packageName}"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { step = 2 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_apps_continue_button"),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintTeal,
                                contentColor = OnMintTeal
                            )
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Step 2: Visual Accessibility Settings Guide
                    2 -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MintTealDark.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SettingsAccessibility,
                                    contentDescription = null,
                                    tint = MintTeal,
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

                            // Live permission status card
                            SurgeonCard(
                                borderColor = if (isAccessibilityEnabled) MintTealBorder else AmberWarningBorder
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isAccessibilityEnabled) MintTeal else AmberWarning)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isAccessibilityEnabled) {
                                            "Protection is turned on!"
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

                            Spacer(modifier = Modifier.height(14.dp))

                            if (openedSystemSettings && !isAccessibilityEnabled) {
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
                                        text = "Tap Open System Settings again, open Downloaded apps \u2192 Feed Surgeon, and switch it on. If it is grayed out, expand the restricted settings tip below.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // Visual 4-step Android Settings walkthrough card
                            SurgeonCard {
                                Text(
                                    text = "HOW TO TURN IT ON",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintTeal
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
                                    title = "Select Feed Surgeon",
                                    detail = "Switch Use Feed Surgeon ON and tap Allow"
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Collapsible Restricted Settings help for Android 13+
                            SurgeonCard(
                                onClick = { showRestrictedTip = !showRestrictedTip }
                            ) {
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
                                        text = "On newer Android phones, open phone Settings \u2192 Apps \u2192 Feed Surgeon \u2192 tap the \u22EE menu in the top corner \u2192 Allow restricted settings, then return here.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    openedSystemSettings = true
                                    AccessibilityStatusHelper.openAccessibilitySettings(context)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("open_accessibility_settings_button"),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintTeal,
                                    contentColor = OnMintTeal
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.onboarding_open_settings),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { step = 3 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_continue_step2"),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CardSurface,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text(
                                    text = if (isAccessibilityEnabled) {
                                        "Continue"
                                    } else {
                                        stringResource(R.string.onboarding_continue_without)
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    // Step 3: Ready to Go & Background Reliability
                    else -> {
                        val guardedCount = rules.count { it.enabled && it.packageName in installedPackages }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MintTealDark.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MintTeal,
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

                            Spacer(modifier = Modifier.height(22.dp))

                            SurgeonCard {
                                Text(
                                    text = "WHAT HAPPENS NEXT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintTeal
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                val appsSummary = if (guardedCount > 0) {
                                    "Guarding $guardedCount installed app${if (guardedCount == 1) "" else "s"}"
                                } else {
                                    "Ready whenever you install or enable apps"
                                }
                                Text(
                                    text = "\u2022 $appsSummary\n\u2022 Take a 5, 15, or 60-minute break anytime\n\u2022 All stats stay completely private on your device",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }

                            if (!isBatteryOptimizationIgnored) {
                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedButton(
                                    onClick = {
                                        ProtectionHealthHelper.requestIgnoreBatteryOptimizations(context)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("onboarding_battery_optimization_button"),
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = CardSurfaceElevated,
                                        contentColor = MintTeal
                                    )
                                ) {
                                    Text("Keep running smoothly in background", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationPermissionGranted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
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
                                    Text("Show active status notification")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showPausePreview = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("onboarding_try_preview_button"),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CardSurface,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(
                                text = "Preview how it works",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onCompleteOnboarding,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_finish_button"),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintTeal,
                                contentColor = OnMintTeal
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_finish),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
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
                .background(MintTealDark)
                .border(1.dp, MintTealBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                color = MintTeal,
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
