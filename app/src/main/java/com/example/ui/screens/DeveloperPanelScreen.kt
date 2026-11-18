package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TargetAppRuleEntity
import com.example.ui.MainUiState
import com.example.ui.components.CircularBackButton
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MonoTypography
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnMintTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeveloperPanelScreen(
    uiState: MainUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onRunTest: () -> Unit,
    onTriggerDiagnosticIntervention: (TargetAppRuleEntity) -> Unit
) {
    val snapshot = uiState.liveSnapshot
    val matchCount = uiState.selectorTestMatchCount ?: 0

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
                    text = stringResource(R.string.developer_panel_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        // Live Node Tree Card
        item {
            SurgeonCard {
                val appLabel = snapshot?.appDisplayName?.uppercase() ?: "YOUTUBE"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(text = "LIVE NODE TREE \u00B7 $appLabel")
                    if (snapshot?.isDiagnosticSample == true) {
                        Text(
                            text = "sample",
                            style = MonoTypography.caption,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                val treeText = buildAnnotatedString {
                    val nodes = snapshot?.nodes.orEmpty()
                    nodes.forEachIndexed { idx, node ->
                        val indent = "  ".repeat(node.depth.coerceAtMost(4))
                        val lineText = when {
                            node.viewId != null -> {
                                val shortId = node.viewId.substringAfter(":id/", node.viewId)
                                if (node.viewId.contains(":id/")) "id/$shortId" else shortId
                            }
                            node.contentDescription != null -> {
                                val sel = if (node.isSelected) " selected" else ""
                                "desc=\"${node.contentDescription}\"$sel"
                            }
                            else -> node.className
                        }
                        append(indent)
                        if (node.isMatched) {
                            withStyle(SpanStyle(color = MintTeal, fontWeight = FontWeight.Medium)) {
                                append("$lineText \u2713")
                            }
                        } else {
                            withStyle(SpanStyle(color = TextSecondary)) {
                                append(lineText)
                            }
                        }
                        if (idx != nodes.lastIndex) append("\n")
                    }
                }

                Text(
                    text = treeText,
                    style = MonoTypography.code,
                    modifier = Modifier.testTag("live_node_tree_text")
                )
            }
        }

        // Selector Tester Card
        item {
            SurgeonCard {
                SectionLabel(text = "SELECTOR TESTER")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Resource ID",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.selectorTestQuery,
                    onValueChange = onQueryChange,
                    textStyle = MonoTypography.code,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedBorderColor = CardBorder,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selector_tester_input")
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRunTest,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("run_selector_test_button"),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTeal,
                            contentColor = OnMintTeal
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.run_test),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedContent(
                        targetState = matchCount,
                        transitionSpec = {
                            (fadeIn(tween(180)) + scaleIn(initialScale = 0.92f, animationSpec = tween(180)))
                                .togetherWith(fadeOut(tween(120)))
                        },
                        label = "match_count_badge"
                    ) { count ->
                        val matchLabel = if (count == 1) "1 match" else "$count matches"
                        StatusBadgePill(
                            text = matchLabel,
                            isWarning = count == 0,
                            modifier = Modifier.testTag("selector_match_badge")
                        )
                    }
                }
            }
        }

        // Rule Source Card
        item {
            SurgeonCard {
                SectionLabel(text = "RULE SOURCE")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "remote JSON \u00B7 cached \u00B7 ${uiState.preferences.rulesetVersion}",
                    style = MonoTypography.code,
                    color = TextSecondary
                )
            }
        }

        // Diagnostic Intervention Test
        val primaryRule = uiState.rules.firstOrNull()
        if (primaryRule != null) {
            item {
                OutlinedButton(
                    onClick = { onTriggerDiagnosticIntervention(primaryRule) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("diagnostic_test_block_button"),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardSurfaceElevated,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Test ${primaryRule.displayName} intervention toast")
                }
            }
        }
    }
}
