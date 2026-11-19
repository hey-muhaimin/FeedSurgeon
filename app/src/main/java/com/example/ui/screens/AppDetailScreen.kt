package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.CircularBackButton
import com.example.ui.components.NeumorphicSegmentedTrack
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.rememberCountdownLabel
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.theme.HabitBg
import com.example.ui.theme.HabitForest
import com.example.ui.theme.HabitForestInk
import com.example.ui.theme.HabitLine
import com.example.ui.theme.HabitSageTint
import com.example.ui.theme.HabitSubtle
import com.example.ui.theme.StaggeredReveal
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.tactilePressPhysics

private enum class ResponseSpeedPreset(
    val label: String,
    val description: String,
    val cooldownMs: Int
) {
    INSTANT(
        label = "Instant",
        description = "Steps in right away as soon as the feed opens.",
        cooldownMs = 500
    ),
    BALANCED(
        label = "Balanced",
        description = "Recommended pace. Smoothly avoids accidental double-taps.",
        cooldownMs = 900
    ),
    GENTLE(
        label = "Gentle",
        description = "Waits briefly before stepping in so transitions feel calm.",
        cooldownMs = 1400
    );

    companion object {
        fun fromCooldownMs(cooldownMs: Int): ResponseSpeedPreset = when {
            cooldownMs <= 650 -> INSTANT
            cooldownMs <= 1150 -> BALANCED
            else -> GENTLE
        }
    }
}

@Composable
fun AppDetailScreen(
    rule: TargetAppRuleEntity?,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectMode: (InterventionMode) -> Unit,
    onChangeCooldown: (Int) -> Unit,
    onSnoozeMinutes: (Int) -> Unit,
    onResumeSnooze: () -> Unit
) {
    BackHandler(onBack = onBack)
    val scheme = MaterialTheme.colorScheme

    if (rule == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularBackButton(onClick = onBack)
            Text(
                "App settings not found.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant
            )
        }
        return
    }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val snoozeCountdown = rememberCountdownLabel(
        untilMs = rule.snoozeUntilMs,
        onExpired = onResumeSnooze
    )
    val isSnoozed = snoozeCountdown != null
    val currentPreset = ResponseSpeedPreset.fromCooldownMs(rule.cooldownMs)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMosaic = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 28.dp + navBarBottom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularBackButton(onClick = onBack)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rule.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = scheme.onBackground
                        )
                        Text(
                            text = "${rule.feedLabel} \u00B7 ${rule.interventionMode.shortLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isMosaic) {
                item {
                    StaggeredReveal(index = 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_detail_mosaic_grid"),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                MasterToggleCard(
                                    rule = rule,
                                    snoozeCountdown = snoozeCountdown,
                                    isSnoozed = isSnoozed,
                                    onToggleEnabled = onToggleEnabled
                                )
                                TakeABreakCard(
                                    rule = rule,
                                    snoozeCountdown = snoozeCountdown,
                                    onSnoozeMinutes = onSnoozeMinutes,
                                    onResumeSnooze = onResumeSnooze
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                HowToStepInCard(
                                    rule = rule,
                                    onSelectMode = onSelectMode
                                )
                                ResponseSpeedCard(
                                    currentPreset = currentPreset,
                                    onChangeCooldown = onChangeCooldown
                                )
                            }
                        }
                    }
                }
            } else {
                // Master Toggle Card
                item {
                    StaggeredReveal(index = 0) {
                        MasterToggleCard(
                            rule = rule,
                            snoozeCountdown = snoozeCountdown,
                            isSnoozed = isSnoozed,
                            onToggleEnabled = onToggleEnabled
                        )
                    }
                }

                // Intervention Mode Inset Segmented Pill Card
                item {
                    StaggeredReveal(index = 1) {
                        HowToStepInCard(
                            rule = rule,
                            onSelectMode = onSelectMode
                        )
                    }
                }

                // Response Speed Presets
                item {
                    StaggeredReveal(index = 2) {
                        ResponseSpeedCard(
                            currentPreset = currentPreset,
                            onChangeCooldown = onChangeCooldown
                        )
                    }
                }

                // Take a Break Card with live countdown timer
                item {
                    StaggeredReveal(index = 3) {
                        TakeABreakCard(
                            rule = rule,
                            snoozeCountdown = snoozeCountdown,
                            onSnoozeMinutes = onSnoozeMinutes,
                            onResumeSnooze = onResumeSnooze
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MasterToggleCard(
    rule: TargetAppRuleEntity,
    snoozeCountdown: String?,
    isSnoozed: Boolean,
    onToggleEnabled: (Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    SurgeonCard(
        modifier = Modifier.surgeonSharedBounds("app_card_${rule.packageName}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppBadgeIcon(packageName = rule.packageName)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Guard ${rule.feedLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                val pillText = when {
                    snoozeCountdown != null -> "Break \u00B7 $snoozeCountdown"
                    rule.enabled -> "Focus Active"
                    else -> "Off"
                }
                StatusBadgePill(
                    text = pillText,
                    isWarning = !rule.enabled || isSnoozed
                )
            }
            SurgeonSwitch(
                checked = rule.enabled,
                onCheckedChange = onToggleEnabled,
                testTag = "detail_toggle_enabled"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("detail_blocker_health"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rule Sync Status",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
            StatusBadgePill(
                text = if (rule.isOutdated) "Update recommended" else "Synced",
                isWarning = rule.isOutdated
            )
        }
    }
}

@Composable
private fun HowToStepInCard(
    rule: TargetAppRuleEntity,
    onSelectMode: (InterventionMode) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    SurgeonCard {
        SectionLabel(text = "HOW OUTLOOP STEPS IN")
        Spacer(modifier = Modifier.height(14.dp))
        NeumorphicSegmentedTrack(
            items = InterventionMode.entries,
            selectedItem = rule.interventionMode,
            labelProvider = { it.shortLabel },
            testTagProvider = { "mode_${it.name.lowercase()}" },
            onSelect = onSelectMode
        )
        Spacer(modifier = Modifier.height(14.dp))
        AnimatedContent(
            targetState = rule.interventionMode,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
            label = "mode_explanation"
        ) { mode ->
            Text(
                text = mode.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResponseSpeedCard(
    currentPreset: ResponseSpeedPreset,
    onChangeCooldown: (Int) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    SurgeonCard {
        SectionLabel(text = "RESPONSE SPEED")
        Spacer(modifier = Modifier.height(14.dp))
        NeumorphicSegmentedTrack(
            items = ResponseSpeedPreset.entries,
            selectedItem = currentPreset,
            labelProvider = { it.label },
            testTagProvider = { "speed_preset_${it.name.lowercase()}" },
            onSelect = { preset -> onChangeCooldown(preset.cooldownMs) }
        )
        Spacer(modifier = Modifier.height(14.dp))
        AnimatedContent(
            targetState = currentPreset,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
            label = "speed_preset_explanation"
        ) { preset ->
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TakeABreakCard(
    rule: TargetAppRuleEntity,
    snoozeCountdown: String?,
    onSnoozeMinutes: (Int) -> Unit,
    onResumeSnooze: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    SurgeonCard {
        SectionLabel(text = "TAKE AN INTENTIONAL BREAK")
        Spacer(modifier = Modifier.height(8.dp))
        if (snoozeCountdown != null) {
            Text(
                text = "$snoozeCountdown remaining",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFeatureSettings = "tnum"
                ),
                color = neu.statusWarningText,
                modifier = Modifier.testTag("snooze_countdown_text")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${rule.displayName} ${rule.feedLabel} will automatically start guarding focus again when the timer ends.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onResumeSnooze()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("snooze_button"),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HabitForest,
                    contentColor = HabitBg
                )
            ) {
                Text(
                    text = "Resume guarding",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = "Pause Focus Guard for ${rule.displayName} ${rule.feedLabel} across all visits until the timer ends (separate from 5m single-visit passes).",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(5 to "5 min", 15 to "15 min", 60 to "1 hour").forEach { (minutes, label) ->
                    val pillShape = RoundedCornerShape(100.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .height(44.dp)
                            .tactilePressPhysics(pressedScale = 0.96f)
                            .clip(pillShape)
                            .background(HabitSageTint)
                            .border(1.dp, HabitLine, pillShape)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSnoozeMinutes(minutes)
                            }
                            .padding(horizontal = 6.dp)
                            .testTag("snooze_${minutes}m_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = HabitForestInk,
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
