package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.components.surgeonSharedBounds
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onOpenDeveloperPanel: () -> Unit,
    onReopenDisclosure: () -> Unit,
    onAddCustomAppClick: () -> Unit,
    onSaveEndpointUrl: (String) -> Unit,
    onSyncRemoteRules: (String) -> Unit,
    onRestoreBundledRules: () -> Unit,
    onSetReclaimedSeconds: (Int) -> Unit,
    onSetShowBanner: (Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    var endpointDraft by remember(uiState.preferences.rulesetEndpointUrl) {
        mutableStateOf(uiState.preferences.rulesetEndpointUrl)
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
                                "Inspecting ${uiState.rules.count { it.enabled }} active target apps on-device"
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

        // Developer panel link card
        item {
            SurgeonCard(
                modifier = Modifier
                    .surgeonSharedBounds("dev_panel_card")
                    .testTag("open_developer_panel_card"),
                onClick = onOpenDeveloperPanel
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        tint = MintTeal
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.developer_panel_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.surgeonSharedBounds("dev_panel_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Live node tree inspector & selector tester",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
        }

        // Remote GitHub Gist / raw JSON ruleset configuration
        item {
            SurgeonCard {
                SectionLabel(text = "REMOTE JSON RULESET (GIST / RAW)")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = endpointDraft,
                    onValueChange = { endpointDraft = it },
                    label = { Text("Ruleset JSON URL") },
                    textStyle = MonoTypography.caption,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintTeal,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = MintTeal,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ruleset_url_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                SurgicalSyncLoadingBar(visible = uiState.isSyncingRules)
                if (uiState.syncStatusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.syncStatusMessage,
                        style = MonoTypography.caption,
                        color = MintTeal
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onSyncRemoteRules(endpointDraft) },
                        enabled = !uiState.isSyncingRules,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("settings_sync_rules_button"),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTeal,
                            contentColor = OnMintTeal
                        )
                    ) {
                        Text("Sync JSON", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = {
                            onSaveEndpointUrl(endpointDraft)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurfaceElevated,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Save URL")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = onRestoreBundledRules,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Restore bundled ruleset",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Custom App Rules & Preferences
        item {
            SurgeonCard {
                SectionLabel(text = "BEHAVIOR & CUSTOM RULES")
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
                        checked = uiState.preferences.showInterventionBanner,
                        onCheckedChange = onSetShowBanner,
                        testTag = "settings_banner_switch"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                val minutesFloat = uiState.preferences.reclaimedSecondsPerCut / 60f
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
                        text = String.format("%.1f min", minutesFloat),
                        style = MonoTypography.caption,
                        color = TextPrimary
                    )
                }
                Slider(
                    value = uiState.preferences.reclaimedSecondsPerCut.toFloat(),
                    onValueChange = { raw ->
                        val snapped = ((raw / 15f).roundToInt() * 15).coerceIn(60, 300)
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
                OutlinedButton(
                    onClick = onAddCustomAppClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_add_custom_app"),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardSurfaceElevated,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Add custom target app rule")
                }

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
