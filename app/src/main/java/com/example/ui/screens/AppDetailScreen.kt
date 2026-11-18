package com.example.ui.screens

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.components.surgeonSharedElement
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun AppDetailScreen(
    rule: TargetAppRuleEntity?,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectMode: (InterventionMode) -> Unit,
    onChangeCooldown: (Int) -> Unit,
    onSnooze15Minutes: () -> Unit,
    onResumeSnooze: () -> Unit
) {
    if (rule == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularBackButton(onClick = onBack)
            Text("Rule not found.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        }
        return
    }

    val isSnoozed = rule.isCurrentlySnoozed()

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
                    color = TextPrimary,
                    modifier = Modifier.surgeonSharedBounds("app_title_${rule.packageName}")
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
                    AppBadgeIcon(
                        packageName = rule.packageName,
                        modifier = Modifier.surgeonSharedElement("app_icon_${rule.packageName}")
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Block ${rule.feedLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val pillText = when {
                            isSnoozed -> "Snoozed"
                            rule.enabled -> "Active"
                            else -> "Paused"
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
            }
        }

        // Intervention Mode Segmented Pill Card
        item {
            SurgeonCard {
                SectionLabel(text = "WHEN ${rule.feedLabel.uppercase()} OPENS")
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
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (selected) MintTeal else Color.Transparent)
                                .clickable { onSelectMode(mode) }
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

        // Cooldown Slider Card
        item {
            SurgeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(text = "COOLDOWN")
                    Text(
                        text = "${rule.cooldownMs} ms",
                        style = MonoTypography.code,
                        color = TextPrimary,
                        modifier = Modifier.testTag("cooldown_value_text")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Slider(
                    value = rule.cooldownMs.toFloat(),
                    onValueChange = { raw ->
                        val snapped = ((raw / 50f).roundToInt() * 50).coerceIn(500, 1500)
                        onChangeCooldown(snapped)
                    },
                    valueRange = 500f..1500f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = MintTeal,
                        inactiveTrackColor = CardSurfaceElevated
                    ),
                    modifier = Modifier.testTag("cooldown_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("500 ms", style = MonoTypography.caption, color = TextSecondary)
                    Text("1500 ms", style = MonoTypography.caption, color = TextSecondary)
                }
            }
        }

        // Snooze Button
        item {
            OutlinedButton(
                onClick = { if (isSnoozed) onResumeSnooze() else onSnooze15Minutes() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("snooze_button"),
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, if (isSnoozed) MintTeal else CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardSurface,
                    contentColor = if (isSnoozed) MintTeal else TextPrimary
                )
            ) {
                Text(
                    text = if (isSnoozed) {
                        "Snoozed \u00B7 Resume blocking ${rule.feedLabel}"
                    } else {
                        "Allow ${rule.feedLabel} for 15 minutes"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
