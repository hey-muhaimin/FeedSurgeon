package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.CircularBackButton
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.rememberCountdownLabel
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.MintTeal
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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

    if (rule == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularBackButton(onClick = onBack)
            Text("App settings not found.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        }
        return
    }

    val haptics = LocalHapticFeedback.current
    val snoozeCountdown = rememberCountdownLabel(
        untilMs = rule.snoozeUntilMs,
        onExpired = onResumeSnooze
    )
    val isSnoozed = snoozeCountdown != null
    val currentPreset = ResponseSpeedPreset.fromCooldownMs(rule.cooldownMs)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = rule.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        // Master Toggle Card
        item {
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
                            text = "Block ${rule.feedLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val pillText = when {
                            snoozeCountdown != null -> "Break \u00B7 $snoozeCountdown"
                            rule.enabled -> "Protected"
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
                        text = "Blocker health",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    StatusBadgePill(
                        text = if (rule.isOutdated) "Update recommended" else "Healthy",
                        isWarning = rule.isOutdated
                    )
                }
            }
        }

        // Intervention Mode Segmented Pill Card
        item {
            SurgeonCard {
                SectionLabel(text = "HOW TO STEP IN")
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .background(ObsidianBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    InterventionMode.entries.forEach { mode ->
                        val selected = mode == rule.interventionMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (selected) MintTeal else Color.Transparent)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectMode(mode)
                                }
                                .padding(vertical = 12.dp)
                                .testTag("mode_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.shortLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (selected) OnMintTeal else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedContent(
                    targetState = rule.interventionMode,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                    label = "mode_explanation"
                ) { mode ->
                    Text(
                        text = mode.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Response Speed Presets
        item {
            SurgeonCard {
                SectionLabel(text = "RESPONSE SPEED")
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .background(ObsidianBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ResponseSpeedPreset.entries.forEach { preset ->
                        val selected = preset == currentPreset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (selected) MintTeal else Color.Transparent)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onChangeCooldown(preset.cooldownMs)
                                }
                                .padding(vertical = 12.dp)
                                .testTag("speed_preset_${preset.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (selected) OnMintTeal else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedContent(
                    targetState = currentPreset,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                    label = "speed_preset_explanation"
                ) { preset ->
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Take a Break Card with live countdown timer
        item {
            SurgeonCard {
                SectionLabel(text = "TAKE A BREAK")
                Spacer(modifier = Modifier.height(8.dp))
                if (snoozeCountdown != null) {
                    Text(
                        text = "$snoozeCountdown remaining",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = AmberWarning,
                        modifier = Modifier.testTag("snooze_countdown_text")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${rule.displayName} ${rule.feedLabel} will automatically start blocking again when the timer ends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
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
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTeal,
                            contentColor = OnMintTeal
                        )
                    ) {
                        Text(
                            text = "Resume blocking ${rule.feedLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Pause blocking for ${rule.displayName} ${rule.feedLabel} across all visits until the timer ends (separate from 5m single-visit passes).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(5 to "5 min", 15 to "15 min", 60 to "1 hr").forEach { (minutes, label) ->
                            OutlinedButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSnoozeMinutes(minutes)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("snooze_${minutes}m_button"),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CardSurfaceElevated,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
