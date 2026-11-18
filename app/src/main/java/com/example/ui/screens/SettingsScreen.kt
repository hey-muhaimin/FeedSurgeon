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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.service.AccessibilityStatusHelper
import com.example.ui.MainUiState
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonSwitch
import com.example.ui.components.SurgicalSyncLoadingBar
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onReopenDisclosure: () -> Unit,
    onSetAutoUpdateRules: (Boolean) -> Unit,
    onCheckRulesNow: () -> Unit,
    onSetReclaimedSeconds: (Int) -> Unit,
    onSetShowBanner: (Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    val prefs = uiState.preferences

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
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }

        // Accessibility Service card
        item {
            SurgeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accessibility Service",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.isAccessibilityEnabled) {
                                "Protecting ${uiState.rules.count { it.enabled }} active target apps on-device"
                            } else {
                                "Enable Feed Surgeon in Android Settings to block feeds"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    StatusBadgePill(
                        text = if (uiState.isAccessibilityEnabled) "Active" else "Off",
                        isWarning = !uiState.isAccessibilityEnabled
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { AccessibilityStatusHelper.openAccessibilitySettings(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("settings_accessibility_button"),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTeal,
                            contentColor = OnMintTeal
                        )
                    ) {
                        Text("System settings", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onReopenDisclosure,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("view_disclosure_button"),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurfaceElevated,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Disclosure")
                    }
                }
            }
        }

        // Automatic Rules card (local cache + background remote sync)
        item {
            val lastCheckedSubtitle = if (prefs.lastRulesetCheckMs == 0L) {
                "Bundled locally \u00B7 ${prefs.rulesetVersion}"
            } else {
                val rel = DateUtils.getRelativeTimeSpanString(
                    prefs.lastRulesetCheckMs,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                "Updated $rel \u00B7 ${prefs.rulesetVersion}"
            }
            SurgeonCard {
                SectionLabel(text = "AUTOMATIC RULES")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-update rules",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = lastCheckedSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    SurgeonSwitch(
                        checked = prefs.autoUpdateRules,
                        onCheckedChange = onSetAutoUpdateRules,
                        testTag = "settings_auto_update_switch"
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                SurgicalSyncLoadingBar(visible = uiState.isSyncingRules)
                AnimatedContent(
                    targetState = uiState.syncStatusMessage,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                    label = "settings_sync_msg"
                ) { msg ->
                    if (msg != null) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MintTeal,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCheckRulesNow,
                    enabled = !uiState.isSyncingRules,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_check_updates_button"),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardSurfaceElevated,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(if (uiState.isSyncingRules) "Checking..." else "Check for updates now")
                }
            }
        }

        // Preferences & Privacy
        item {
            SurgeonCard {
                SectionLabel(text = "PREFERENCES & PRIVACY")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Intervention banner",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Show Allow once / Undo bar after a block",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    SurgeonSwitch(
                        checked = prefs.showInterventionBanner,
                        onCheckedChange = onSetShowBanner,
                        testTag = "settings_banner_switch"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                var sliderSeconds by remember(prefs.reclaimedSecondsPerCut) {
                    mutableFloatStateOf(prefs.reclaimedSecondsPerCut.toFloat())
                }
                val snappedSeconds = ((sliderSeconds / 15f).roundToInt() * 15).coerceIn(60, 300)
                val minutesFloat = snappedSeconds / 60f
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Estimated time saved per block",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f min", minutesFloat),
                        style = MonoTypography.caption,
                        color = TextPrimary
                    )
                }
                Slider(
                    value = sliderSeconds,
                    onValueChange = { raw -> sliderSeconds = raw },
                    onValueChangeFinished = {
                        val snapped = ((sliderSeconds / 15f).roundToInt() * 15).coerceIn(60, 300)
                        onSetReclaimedSeconds(snapped)
                    },
                    valueRange = 60f..300f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = MintTeal,
                        inactiveTrackColor = CardSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { uriHandler.openUri(privacyPolicyUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_privacy_policy_button")
                ) {
                    Text(
                        text = "Privacy Policy (suprbuild.com/privacy)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MintTeal
                    )
                }

                TextButton(
                    onClick = onClearHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_history_button")
                ) {
                    Text(
                        text = "Clear local insights history",
                        style = MaterialTheme.typography.labelLarge,
                        color = CoralDanger
                    )
                }
            }
        }
    }
}
