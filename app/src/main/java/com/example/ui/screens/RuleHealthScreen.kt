package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.ui.theme.AmberWarningBorder
import com.example.ui.theme.CardBorder
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RuleHealthScreen(
    uiState: MainUiState,
    onUpdateRulesClick: () -> Unit
) {
    val prefs = uiState.preferences
    val sortedRules = remember(uiState.rules, uiState.installedPackages) {
        uiState.rules.sortedByDescending { it.packageName in uiState.installedPackages }
    }
    val outdatedRule = sortedRules.firstOrNull {
        it.isOutdated && it.packageName in uiState.installedPackages
    }
    val checkedLabel = if (prefs.lastRulesetCheckMs == 0L) {
        val modeLabel = if (prefs.autoUpdateRules) "auto-update on" else "auto-update off"
        "Saved locally on device \u00B7 $modeLabel"
    } else {
        val rel = DateUtils.getRelativeTimeSpanString(
            prefs.lastRulesetCheckMs,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        "Checked $rel \u00B7 saved locally"
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
                color = TextPrimary
            )
        }

        // Ruleset version & automatic sync card
        item {
            SurgeonCard {
                Text(
                    text = "Protection rules",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = prefs.rulesetVersion,
                    style = MonoTypography.version,
                    color = TextPrimary,
                    modifier = Modifier.testTag("ruleset_version_text")
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
                            style = MaterialTheme.typography.bodyMedium,
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
                        text = if (uiState.isSyncingRules) "Checking..." else stringResource(R.string.update_rules),
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

        items(sortedRules, key = { it.packageName }) { rule ->
            val isInstalled = rule.packageName in uiState.installedPackages
            val subtitle = when {
                !isInstalled -> "${rule.feedLabel} \u00B7 app not installed"
                rule.isOutdated -> "${rule.feedLabel} \u00B7 layout update needed"
                else -> "${rule.feedLabel} \u00B7 protection ready"
            }
            SurgeonCard(modifier = Modifier.alpha(if (isInstalled) 1f else 0.72f)) {
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isInstalled) TextSecondary else TextMuted
                        )
                    }
                    when {
                        !isInstalled -> Text(
                            text = "Not installed",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                        else -> StatusBadgePill(
                            text = if (rule.isOutdated) "Update needed" else "Healthy",
                            isWarning = rule.isOutdated
                        )
                    }
                }
            }
        }

        // Contextual status card
        item {
            SurgeonCard(
                borderColor = if (outdatedRule != null) AmberWarningBorder else CardBorder
            ) {
                Text(
                    text = if (outdatedRule != null) {
                        "${outdatedRule.feedLabel} may slip through"
                    } else {
                        "Automatic protection active"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (outdatedRule != null) {
                        "${outdatedRule.displayName} updated its app layout. Tap Check for updates above to refresh local rules."
                    } else {
                        "Blocking rules are cached locally in DataStore and updated periodically in the background via WorkManager."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
