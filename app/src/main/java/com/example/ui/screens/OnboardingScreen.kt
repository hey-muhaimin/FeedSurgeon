package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.TargetAppRuleEntity
import com.example.service.AccessibilityStatusHelper
import com.example.ui.MainViewModel
import com.example.ui.OnboardingFlowState
import com.example.ui.OnboardingStep
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.OnElectricAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onCompleteNavigation: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingContentScaffold(
        onboardingState = uiState.onboardingState,
        rules = uiState.rules,
        installedPackages = uiState.installedPackages,
        isAccessibilityEnabled = uiState.isAccessibilityEnabled,
        isBatteryOptimizationIgnored = uiState.isBatteryOptimizationIgnored,
        isNotificationPermissionGranted = uiState.isNotificationPermissionGranted,
        onRefreshProtectionHealth = { viewModel.refreshProtectionHealth(context) },
        onToggleApp = { pkg, enabled -> viewModel.setRuleEnabled(pkg, enabled) },
        onPreviousStep = { viewModel.previousOnboardingStep() },
        onNextStep = { viewModel.nextOnboardingStep() },
        onToggleRestrictedTip = { viewModel.toggleOnboardingRestrictedTip() },
        onMarkSettingsOpened = { viewModel.markOnboardingSettingsOpened() },
        onSetPausePreviewVisible = { visible -> viewModel.setOnboardingPausePreviewVisible(visible) },
        onAcceptDisclosure = { viewModel.acceptDisclosure() },
        onDeclineDisclosure = {
            viewModel.declineDisclosure()
            onCompleteNavigation()
        },
        onCompleteOnboarding = {
            viewModel.completeOnboarding()
            onCompleteNavigation()
        }
    )
}

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
    var localState by remember { mutableStateOf(OnboardingFlowState()) }

    OnboardingContentScaffold(
        onboardingState = localState,
        rules = rules,
        installedPackages = installedPackages,
        isAccessibilityEnabled = isAccessibilityEnabled,
        isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
        isNotificationPermissionGranted = isNotificationPermissionGranted,
        onRefreshProtectionHealth = onRefreshProtectionHealth,
        onToggleApp = onToggleApp,
        onPreviousStep = {
            val prev = OnboardingStep.fromIndex((localState.currentStep.index - 1).coerceAtLeast(0))
            localState = localState.copy(currentStep = prev)
        },
        onNextStep = {
            val next = OnboardingStep.fromIndex(
                (localState.currentStep.index + 1).coerceAtMost(OnboardingStep.READY.index)
            )
            localState = localState.copy(currentStep = next)
        },
        onToggleRestrictedTip = {
            localState = localState.copy(showRestrictedTip = !localState.showRestrictedTip)
        },
        onMarkSettingsOpened = {
            localState = localState.copy(openedSystemSettings = true)
        },
        onSetPausePreviewVisible = { visible ->
            localState = localState.copy(showPausePreview = visible)
        },
        onAcceptDisclosure = {
            onAcceptDisclosure()
            localState = localState.copy(currentStep = OnboardingStep.TARGET_APPS)
        },
        onDeclineDisclosure = onDeclineDisclosure,
        onCompleteOnboarding = onCompleteOnboarding
    )
}

@Composable
private fun OnboardingContentScaffold(
    onboardingState: OnboardingFlowState,
    rules: List<TargetAppRuleEntity>,
    installedPackages: Set<String>,
    isAccessibilityEnabled: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    isNotificationPermissionGranted: Boolean,
    onRefreshProtectionHealth: () -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onToggleRestrictedTip: () -> Unit,
    onMarkSettingsOpened: () -> Unit,
    onSetPausePreviewVisible: (Boolean) -> Unit,
    onAcceptDisclosure: () -> Unit,
    onDeclineDisclosure: () -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val sortedRules = remember(rules, installedPackages) {
        rules.sortedWith(
            compareByDescending<TargetAppRuleEntity> { it.packageName in installedPackages }
                .thenBy { it.displayName }
        )
    }

    OnboardingScaffold(
        currentStep = onboardingState.currentStep,
        onPreviousStep = onPreviousStep,
        bottomBar = { _ ->
            OnboardingBottomActionBar(
                currentStep = onboardingState.currentStep,
                isAccessibilityEnabled = isAccessibilityEnabled,
                onDeclineDisclosure = onDeclineDisclosure,
                onAcceptDisclosure = onAcceptDisclosure,
                onNextStep = onNextStep,
                onOpenSystemSettings = {
                    onMarkSettingsOpened()
                    AccessibilityStatusHelper.openAccessibilitySettings(context)
                },
                onShowPreview = { onSetPausePreviewVisible(true) },
                onCompleteOnboarding = onCompleteOnboarding
            )
        }
    ) { step, isWideLayout ->
        when (step) {
            OnboardingStep.WELCOME -> {
                OnboardingWelcomeStep(
                    rules = rules,
                    isWideLayout = isWideLayout
                )
            }
            OnboardingStep.TARGET_APPS -> {
                OnboardingAppsStep(
                    sortedRules = sortedRules,
                    installedPackages = installedPackages,
                    isWideLayout = isWideLayout,
                    onToggleApp = onToggleApp
                )
            }
            OnboardingStep.ACCESSIBILITY -> {
                OnboardingAccessibilityStep(
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    openedSystemSettings = onboardingState.openedSystemSettings,
                    showRestrictedTip = onboardingState.showRestrictedTip,
                    isWideLayout = isWideLayout,
                    onToggleRestrictedTip = onToggleRestrictedTip
                )
            }
            OnboardingStep.READY -> {
                val guardedCount = rules.count { it.enabled && it.packageName in installedPackages }
                OnboardingReadyStep(
                    guardedCount = guardedCount,
                    isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                    isNotificationPermissionGranted = isNotificationPermissionGranted,
                    isWideLayout = isWideLayout,
                    onRefreshProtectionHealth = onRefreshProtectionHealth
                )
            }
        }
    }

    if (onboardingState.showPausePreview) {
        MindfulPausePreviewDialog(
            appName = "YouTube",
            feedLabel = "Shorts",
            onDismiss = { onSetPausePreviewVisible(false) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingBottomActionBar(
    currentStep: OnboardingStep,
    isAccessibilityEnabled: Boolean,
    onDeclineDisclosure: () -> Unit,
    onAcceptDisclosure: () -> Unit,
    onNextStep: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onShowPreview: () -> Unit,
    onCompleteOnboarding: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)

    when (currentStep) {
        OnboardingStep.WELCOME -> {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_privacy_consent_line"),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.onboarding_consent_prefix),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = stringResource(R.string.privacy_policy_label),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = ElectricAmber,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { uriHandler.openUri(privacyPolicyUrl) }
                        .padding(horizontal = 2.dp)
                        .testTag("onboarding_privacy_link")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_step0_actions_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDeclineDisclosure,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("onboarding_decline_button"),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_decline),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onAcceptDisclosure,
                    modifier = Modifier
                        .weight(1.85f)
                        .height(52.dp)
                        .testTag("onboarding_agree_button"),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricAmber,
                        contentColor = OnElectricAmber
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_agree),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        OnboardingStep.TARGET_APPS -> {
            Button(
                onClick = onNextStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_apps_continue_button"),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricAmber,
                    contentColor = OnElectricAmber
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OnboardingStep.ACCESSIBILITY -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_step2_actions_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNextStep,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("onboarding_continue_step2"),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp),
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
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onOpenSystemSettings,
                    modifier = Modifier
                        .weight(1.85f)
                        .height(52.dp)
                        .testTag("open_accessibility_settings_button"),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricAmber,
                        contentColor = OnElectricAmber
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_open_settings),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        OnboardingStep.READY -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_step3_actions_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onShowPreview,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("onboarding_try_preview_button"),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_preview_action),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onCompleteOnboarding,
                    modifier = Modifier
                        .weight(1.85f)
                        .height(52.dp)
                        .testTag("onboarding_finish_button"),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricAmber,
                        contentColor = OnElectricAmber
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_finish),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
