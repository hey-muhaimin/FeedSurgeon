package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.AppBreakdownStat
import com.example.ui.DayBarStat
import com.example.ui.MainUiState
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.SectionLabel
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.StillspaceEmptyFocusArtwork
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonScreenHeader
import com.example.ui.theme.BarInactive
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CalmSageBorder
import com.example.ui.theme.CalmSageDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.StaggeredReveal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InsightsScreen(
    uiState: MainUiState,
    bottomContentPadding: Dp = 112.dp
) {
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dayBars = uiState.weeklyDayBars
    var selectedDayIndex by remember(dayBars.size) {
        mutableIntStateOf((dayBars.lastIndex).coerceAtLeast(0))
    }
    var showPausePreview by remember { mutableStateOf(false) }

    val selectedDay = dayBars.getOrNull(selectedDayIndex)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMosaic = maxWidth >= 600.dp

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
                    "${uiState.streakDays}d focus streak"
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

            if (isMosaic) {
                item {
                    StaggeredReveal(index = 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .testTag("insights_mosaic_top_row"),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            WeeklyChartCard(
                                weeklyCutsCount = uiState.weeklyCutsCount,
                                dayBars = dayBars,
                                selectedDayIndex = selectedDayIndex,
                                selectedDay = selectedDay,
                                onSelectDay = { selectedDayIndex = it },
                                modifier = Modifier
                                    .weight(1.35f)
                                    .fillMaxHeight()
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatTripletRow(
                                    streakDays = uiState.streakDays,
                                    weeklyReclaimedText = uiState.weeklyReclaimedText,
                                    weeklyAllowedOnceCount = uiState.weeklyAllowedOnceCount
                                )
                                SurgeonCard(modifier = Modifier.weight(1f)) {
                                    SectionLabel(text = "HOW FOCUS TIME IS ESTIMATED")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateNoteText(
                                        reclaimedSecondsPerCut = uiState.preferences.reclaimedSecondsPerCut
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Tappable Weekly Chart Card
                item {
                    StaggeredReveal(index = 0) {
                        WeeklyChartCard(
                            weeklyCutsCount = uiState.weeklyCutsCount,
                            dayBars = dayBars,
                            selectedDayIndex = selectedDayIndex,
                            selectedDay = selectedDay,
                            onSelectDay = { selectedDayIndex = it }
                        )
                    }
                }

                // Streak + Reclaimed + 5-min passes row
                item {
                    StaggeredReveal(index = 1) {
                        StatTripletRow(
                            streakDays = uiState.streakDays,
                            weeklyReclaimedText = uiState.weeklyReclaimedText,
                            weeklyAllowedOnceCount = uiState.weeklyAllowedOnceCount
                        )
                    }
                }

                // Estimation methodology transparency note
                item {
                    StaggeredReveal(index = 2) {
                        EstimateNoteText(
                            reclaimedSecondsPerCut = uiState.preferences.reclaimedSecondsPerCut,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // Calm empty state artwork + interactive preview when no weekly events yet
            if (uiState.weeklyCutsCount == 0) {
                item {
                    StaggeredReveal(index = 3) {
                        SurgeonCard(modifier = Modifier.testTag("insights_empty_card")) {
                            if (isMosaic) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StillspaceEmptyFocusArtwork(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(148.dp)
                                    )
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(
                                            text = "Your focus momentum starts here",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Everything stays private on your phone. Once Stillspace steps in on Shorts or Reels, your focus wins, streak, and time reclaimed appear here.",
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
                                                contentColor = ElectricAmber
                                            )
                                        ) {
                                            Text("Preview Mindful Pause")
                                        }
                                    }
                                }
                            } else {
                                StillspaceEmptyFocusArtwork(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(132.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Your focus momentum starts here",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Everything stays private on your phone. Once Stillspace steps in on Shorts or Reels, your focus wins, streak, and time reclaimed appear here.",
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
                                        contentColor = ElectricAmber
                                    )
                                ) {
                                    Text("Preview Mindful Pause")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionLabel(text = stringResource(R.string.by_app_header))
            }

            item {
                StaggeredReveal(index = 4) {
                    SurgeonCard {
                        if (uiState.byAppBreakdown.isEmpty()) {
                            Text(
                                text = "Your focus wins by app will appear here once Focus Guard starts.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        } else if (isMosaic) {
                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                uiState.byAppBreakdown.chunked(2).forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        pair.forEach { stat ->
                                            AppBreakdownRow(
                                                stat = stat,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                uiState.byAppBreakdown.forEach { stat ->
                                    AppBreakdownRow(
                                        stat = stat,
                                        modifier = Modifier.fillMaxWidth()
                                    )
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

@Composable
private fun WeeklyChartCard(
    weeklyCutsCount: Int,
    dayBars: List<DayBarStat>,
    selectedDayIndex: Int,
    selectedDay: DayBarStat?,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    SurgeonCard(modifier = modifier) {
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
                color = ElectricAmber
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        AnimatedIntCounter(
            target = weeklyCutsCount,
            suffix = " focus wins",
            style = MaterialTheme.typography.headlineLarge,
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
                            onSelectDay(idx)
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
                                    isSelected -> ElectricAmber
                                    day.isToday -> ElectricAmber.copy(alpha = 0.55f)
                                    else -> BarInactive
                                }
                            )
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 1.5.dp,
                                        color = Color(0xFFFFF8EE).copy(alpha = 0.75f),
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
                        color = if (isSelected) ElectricAmber else TextSecondary
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
                        .background(CalmSageDark.copy(alpha = 0.48f))
                        .border(1.dp, CalmSageBorder, RoundedCornerShape(12.dp))
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
                            text = "${day.count} focus wins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    AnimatedIntCounter(
                        target = day.reclaimedMinutes,
                        prefix = "~",
                        suffix = " min reclaimed",
                        style = MaterialTheme.typography.labelLarge,
                        color = CalmSage
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTripletRow(
    streakDays: Int,
    weeklyReclaimedText: String,
    weeklyAllowedOnceCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SurgeonCard(modifier = Modifier.weight(1f)) {
            AnimatedIntCounter(
                target = streakDays,
                suffix = "d",
                style = MaterialTheme.typography.headlineMedium,
                color = if (streakDays > 0) CalmSage else TextPrimary,
                modifier = Modifier.testTag("insights_streak_value")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Focus Streak",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        SurgeonCard(modifier = Modifier.weight(1.25f)) {
            Text(
                text = weeklyReclaimedText,
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
            AnimatedIntCounter(
                target = weeklyAllowedOnceCount,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Intentional Passes",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EstimateNoteText(
    reclaimedSecondsPerCut: Int,
    modifier: Modifier = Modifier
) {
    val perVisitLabel = if (reclaimedSecondsPerCut % 60 == 0) {
        "${reclaimedSecondsPerCut / 60} min"
    } else {
        String.format(
            java.util.Locale.US,
            "%.1f min",
            reclaimedSecondsPerCut / 60f
        )
    }
    Text(
        text = "Estimated focus time reclaimed is based on confirmed loop breaks \u00D7 your $perVisitLabel per-visit estimate in Settings (not measured screen time). Uncompleted triggers are excluded.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        modifier = modifier.testTag("insights_estimate_note")
    )
}

@Composable
private fun AppBreakdownRow(
    stat: AppBreakdownStat,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppBadgeIcon(packageName = stat.packageName)
        Spacer(modifier = Modifier.width(12.dp))
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
                AnimatedIntCounter(
                    target = stat.count,
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
                        .background(ElectricAmber)
                )
            }
        }
    }
}
