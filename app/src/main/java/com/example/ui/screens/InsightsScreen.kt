package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.MainUiState
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonScreenHeader
import com.example.ui.theme.BarInactive
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.MintTeal
import com.example.ui.theme.MintTealBorder
import com.example.ui.theme.MintTealDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InsightsScreen(
    uiState: MainUiState,
    bottomContentPadding: Dp = 112.dp
) {
    val haptics = LocalHapticFeedback.current
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dayBars = uiState.weeklyDayBars
    var selectedDayIndex by remember(dayBars.size) {
        mutableIntStateOf((dayBars.lastIndex).coerceAtLeast(0))
    }
    var showPausePreview by remember { mutableStateOf(false) }

    val selectedDay = dayBars.getOrNull(selectedDayIndex)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = bottomContentPadding + navBarBottom
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            val badgeText = if (uiState.streakDays > 0) {
                "${uiState.streakDays}d streak"
            } else {
                "7-day view"
            }
            SurgeonScreenHeader(
                title = stringResource(R.string.insights_title),
                subtitle = "Last 7 days \u00B7 100% on-device",
                trailingContent = {
                    StatusBadgePill(
                        text = badgeText,
                        isWarning = false
                    )
                }
            )
        }

        // Tappable Weekly Chart Card
        item {
            SurgeonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.this_week),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Tap a day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MintTeal
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${uiState.weeklyCutsCount} distractions avoided",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    color = TextPrimary,
                    modifier = Modifier.testTag("weekly_cuts_count")
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    dayBars.forEachIndexed { idx, day ->
                        val isSelected = idx == selectedDayIndex
                        val animatedBar by animateFloatAsState(
                            targetValue = day.fraction,
                            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                            label = "weekly_bar_$idx"
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedDayIndex = idx
                                }
                                .padding(horizontal = 2.dp, vertical = 4.dp)
                                .alpha(if (isSelected) 1f else 0.72f)
                                .testTag("weekly_bar_$idx"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((74f * animatedBar).dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSelected -> MintTeal
                                            day.isToday -> MintTeal.copy(alpha = 0.55f)
                                            else -> BarInactive
                                        }
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 1.5.dp,
                                                color = Color.White.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = day.dayLetter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) MintTeal else TextSecondary
                            )
                        }
                    }
                }

                if (selectedDay != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    AnimatedContent(
                        targetState = selectedDay,
                        transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                        label = "selected_day_detail"
                    ) { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {}
                                .clip(RoundedCornerShape(12.dp))
                                .background(MintTealDark.copy(alpha = 0.45f))
                                .border(1.dp, MintTealBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("selected_day_summary"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (day.isToday) "Today \u00B7 ${day.dateLabel}" else "${day.dayName} \u00B7 ${day.dateLabel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${day.count} avoided",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "~${day.reclaimedMinutes} min saved",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MintTeal
                            )
                        }
                    }
                }
            }
        }

        // Streak + Reclaimed + 5-min passes row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SurgeonCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${uiState.streakDays}d",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = if (uiState.streakDays > 0) MintTeal else TextPrimary,
                        modifier = Modifier.testTag("insights_streak_value")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                SurgeonCard(modifier = Modifier.weight(1.25f)) {
                    Text(
                        text = uiState.weeklyReclaimedText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.reclaimed_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                SurgeonCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.weeklyAllowedOnceCount.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Passes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Estimation methodology transparency note
        item {
            val perVisitLabel = if (uiState.preferences.reclaimedSecondsPerCut % 60 == 0) {
                "${uiState.preferences.reclaimedSecondsPerCut / 60} min"
            } else {
                String.format(
                    java.util.Locale.US,
                    "%.1f min",
                    uiState.preferences.reclaimedSecondsPerCut / 60f
                )
            }
            Text(
                text = "Estimated time reclaimed is based on confirmed exits \u00D7 your $perVisitLabel per-visit estimate in Settings (not measured screen time). Failed exits are excluded.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("insights_estimate_note")
            )
        }

        // Calm empty state artwork + interactive preview when no weekly events yet
        if (uiState.weeklyCutsCount == 0) {
            item {
                SurgeonCard(modifier = Modifier.testTag("insights_empty_card")) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empty_insights),
                        contentDescription = "Calm focus landscape illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Your calm progress starts here",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Everything stays private on your phone. Once protection steps in on Shorts or Reels, your daily streak and time saved appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { showPausePreview = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("insights_preview_pause_button"),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurfaceElevated,
                            contentColor = MintTeal
                        )
                    ) {
                        Text("Preview Mindful Pause")
                    }
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
                        text = "Your app activity will appear here once protection starts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        uiState.byAppBreakdown.forEach { stat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppBadgeIcon(packageName = stat.packageName)
                                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFeatureSettings = "tnum"
                                            ),
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
        }
    }

    if (showPausePreview) {
        MindfulPausePreviewDialog(
            appName = "YouTube",
            feedLabel = "Shorts",
            onDismiss = { showPausePreview = false }
        )
    }
}
