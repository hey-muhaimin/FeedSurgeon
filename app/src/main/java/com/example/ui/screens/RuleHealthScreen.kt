package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.MainUiState
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgicalSyncLoadingBar
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.theme.AmberWarningBorder
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RuleHealthScreen(
    uiState: MainUiState,
    onUpdateRulesClick: () -> Unit,
    onTestNowClick: (String) -> Unit,
    onOpenDeveloperPanel: () -> Unit
) {
    val prefs = uiState.preferences
    val outdatedRule = uiState.rules.firstOrNull { it.isOutdated }
    val checkedLabel = if (prefs.lastRulesetCheckMs == 0L) {
        "Bundled ruleset \u00B7 tap to check remote JSON"
    } else {
        val rel = DateUtils.getRelativeTimeSpanString(
            prefs.lastRulesetCheckMs,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        "Checked $rel \u00B7 remote JSON cached"
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
                text = stringResource(R.string.rule_health_title),
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.surgeonSharedBounds("rule_health_title")
            )
        }

        // Ruleset version & update card
        item {
            SurgeonCard {
                Text(
                    text = "Ruleset",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = prefs.rulesetVersion,
                    style = MonoTypography.version,
                    color = TextPrimary,
                    modifier = Modifier
                        .surgeonSharedBounds("ruleset_version_text")
                        .testTag("ruleset_version_text")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = checkedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                SurgicalSyncLoadingBar(visible = uiState.isSyncingRules)
                AnimatedContent(
                    targetState = uiState.syncStatusMessage,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                    label = "sync_status_anim"
                ) { statusMsg ->
                    if (statusMsg != null) {
                        Text(
                            text = statusMsg,
                            style = MonoTypography.caption,
                            color = MintTeal,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onUpdateRulesClick,
                    enabled = !uiState.isSyncingRules,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("update_rules_button"),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintTeal,
                        contentColor = OnMintTeal
                    )
                ) {
                    if (uiState.isSyncingRules) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = OnMintTeal,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (uiState.isSyncingRules) "Updating rules..." else stringResource(R.string.update_rules),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionLabel(text = stringResource(R.string.per_app_header))
        }

        items(uiState.rules, key = { it.packageName }) { rule ->
            val total = rule.totalSelectorCount.coerceAtLeast(rule.viewIds.size)
            val matched = rule.matchedSelectorCount.coerceAtLeast(0)
            val subtitle = if (rule.isOutdated) {
                val missing = (total - matched).coerceAtLeast(1)
                "$total selectors \u00B7 $missing missing"
            } else {
                "$total selectors \u00B7 all match"
            }
            SurgeonCard(onClick = { onTestNowClick(rule.packageName) }) {
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MonoTypography.caption,
                            color = TextSecondary
                        )
                    }
                    StatusBadgePill(
                        text = if (rule.isOutdated) "Outdated" else "Healthy",
                        isWarning = rule.isOutdated
                    )
                }
            }
        }

        // Contextual Test / Warning Card
        item {
            val focusRule = outdatedRule ?: uiState.rules.firstOrNull()
            if (focusRule != null) {
                SurgeonCard(
                    borderColor = if (outdatedRule != null) AmberWarningBorder else CardBorder
                ) {
                    Text(
                        text = if (outdatedRule != null) {
                            "${outdatedRule.feedLabel} may slip through"
                        } else {
                            "Inspect live view hierarchy"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (outdatedRule != null) {
                            "${outdatedRule.displayName} changed its layout. Update rules, then test."
                        } else {
                            "Verify active selectors against the latest Accessibility node tree or inspect View IDs in the Developer panel."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                onTestNowClick(focusRule.packageName)
                                onOpenDeveloperPanel()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("test_now_button"),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CardSurfaceElevated,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.test_now),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
