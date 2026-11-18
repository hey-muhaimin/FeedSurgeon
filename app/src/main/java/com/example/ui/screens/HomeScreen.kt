package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TargetAppRuleEntity
import com.example.ui.MainUiState
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSkeletonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.components.surgeonSharedElement
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BarInactive
import com.example.ui.theme.MintTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onStatusClick: () -> Unit,
    onRuleHealthCardClick: () -> Unit,
    onAppClick: (String) -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    onAddCustomAppClick: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )

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
                            .background(if (uiState.isAccessibilityEnabled) MintTeal else AmberWarning)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isAccessibilityEnabled) {
                            stringResource(R.string.status_running)
                        } else {
                            stringResource(R.string.status_service_off)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Hero card: Feeds cut today + 5-bar trend
        item {
            AnimatedContent(
                targetState = uiState.isInitialLoading,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "home_hero_loading"
            ) { loading ->
                if (loading) {
                    SurgeonSkeletonCard(heightDp = 112)
                } else {
                    SurgeonCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.feeds_cut_today),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.todayCutsCount.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                color = TextPrimary,
                                modifier = Modifier.testTag("today_cuts_count")
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(64.dp)
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

        // Reclaimed + Rule health row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SurgeonCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "~${uiState.todayReclaimedMinutes} min",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.reclaimed_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                SurgeonCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_rule_health_card"),
                    onClick = onRuleHealthCardClick
                ) {
                    val stale = uiState.staleRulesCount
                    Text(
                        text = if (stale > 0) "$stale stale" else "Healthy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (stale > 0) AmberWarning else MintTeal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.rule_health_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.surgeonSharedBounds("rule_health_title")
                    )
                }
            }
        }

        // TARGET APPS header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(text = stringResource(R.string.target_apps_header))
                TextButton(
                    onClick = onAddCustomAppClick,
                    modifier = Modifier.testTag("add_custom_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MintTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add custom",
                        style = MaterialTheme.typography.labelLarge,
                        color = MintTeal
                    )
                }
            }
        }

        if (uiState.isInitialLoading) {
            items(2) {
                SurgeonSkeletonCard(heightDp = 80)
            }
        } else {
            items(uiState.rules, key = { it.packageName }) { rule ->
                TargetAppRowCard(
                    modifier = Modifier.animateItem(),
                    rule = rule,
                    onClick = { onAppClick(rule.packageName) },
                    onToggle = { enabled -> onToggleApp(rule.packageName, enabled) }
                )
            }
        }
    }
}

@Composable
private fun TargetAppRowCard(
    rule: TargetAppRuleEntity,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SurgeonCard(
        modifier = modifier
            .surgeonSharedBounds("app_card_${rule.packageName}")
            .testTag("target_app_card_${rule.packageName}"),
        onClick = onClick
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
                    text = rule.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.surgeonSharedBounds("app_title_${rule.packageName}")
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (rule.isOutdated) {
                    StatusBadgePill(
                        text = "Selector outdated",
                        isWarning = true
                    )
                } else if (rule.isCurrentlySnoozed()) {
                    StatusBadgePill(
                        text = "${rule.feedLabel} snoozed",
                        isWarning = true
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
