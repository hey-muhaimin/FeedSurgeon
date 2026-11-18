package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.MainUiState
import com.example.ui.components.SectionLabel
import com.example.ui.components.SurgeonCard
import com.example.ui.theme.BarInactive
import com.example.ui.theme.MintTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InsightsScreen(
    uiState: MainUiState
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
            Text(
                text = stringResource(R.string.insights_title),
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }

        // Weekly chart card
        item {
            SurgeonCard {
                Text(
                    text = stringResource(R.string.this_week),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${uiState.weeklyCutsCount} feeds cut",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    modifier = Modifier.testTag("weekly_cuts_count")
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    uiState.weeklyDayBars.forEachIndexed { idx, day ->
                        val animatedBar by animateFloatAsState(
                            targetValue = day.fraction,
                            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                            label = "weekly_bar_$idx"
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((78f * animatedBar).dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (day.isToday) MintTeal else BarInactive)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = day.dayLetter,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (day.isToday) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Split stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SurgeonCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.weeklyReclaimedText,
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

                SurgeonCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.weeklyAllowedOnceCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.allow_once),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionLabel(text = stringResource(R.string.by_app_header))
        }

        item {
            SurgeonCard {
                if (uiState.byAppBreakdown.isEmpty()) {
                    Text(
                        text = "No target apps configured yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        uiState.byAppBreakdown.forEach { stat ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stat.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stat.count.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                val animatedWidth by animateFloatAsState(
                                    targetValue = stat.fraction,
                                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                                    label = "app_progress_${stat.packageName}"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BarInactive)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedWidth)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MintTeal)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.weeklyCutsCount == 0) {
            item {
                SurgeonCard {
                    Text(
                        text = "All metrics stay 100% on-device. Open YouTube, Instagram, Facebook, Snapchat, or Reddit with the Accessibility Service enabled to see real-time feed blocks appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
