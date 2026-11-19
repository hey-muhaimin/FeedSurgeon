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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppBreakdownStat
import com.example.ui.DayBarStat
import com.example.ui.MainUiState
import com.example.ui.WeeklyFunnelStats
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.GroupedListDivider
import com.example.ui.components.HabitIconBadge
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.StillspaceEmptyFocusArtwork
import com.example.ui.components.StreakBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonScreenHeader
import com.example.ui.theme.HabitAmberGlow
import com.example.ui.theme.HabitAmberWarmBg
import com.example.ui.theme.HabitForest
import com.example.ui.theme.HabitForestInk
import com.example.ui.theme.HabitLine
import com.example.ui.theme.HabitMuted
import com.example.ui.theme.HabitReadyBg
import com.example.ui.theme.HabitReadyText
import com.example.ui.theme.HabitSageBar
import com.example.ui.theme.HabitSageSoft
import com.example.ui.theme.HabitSageText
import com.example.ui.theme.HabitSageTint
import com.example.ui.theme.HabitSubtle
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.HabitSurfaceLow
import com.example.ui.theme.HabitSurfaceSunken
import com.example.ui.theme.StaggeredReveal
import com.example.ui.theme.StillspaceTheme
import java.util.Locale

@Composable
fun InsightsScreen(
    uiState: MainUiState,
    onSetBreathingPauseSeconds: (Int) -> Unit = {},
    bottomContentPadding: Dp = 112.dp
) {
    val scheme = MaterialTheme.colorScheme
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val badgeText = if (uiState.streakDays > 0) {
                    "${uiState.streakDays}d streak"
                } else {
                    "0d streak"
                }
                SurgeonScreenHeader(
                    title = "Insights",
                    subtitle = "Last 7 days \u00B7 100% on-device",
                    trailingContent = {
                        StreakBadgePill(text = badgeText)
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
                                    Text(
                                        text = "How focus time is estimated",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = scheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    EstimateNoteText(
                                        reclaimedSecondsPerCut = uiState.preferences.reclaimedSecondsPerCut
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    StaggeredReveal(index = 1) {
                        ActivityFunnelCard(funnel = uiState.weeklyFunnel)
                    }
                }
            } else {
                // Weekly Bar Chart Card
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

                // Activity Funnel Card directly below weekly chart
                item {
                    StaggeredReveal(index = 1) {
                        ActivityFunnelCard(funnel = uiState.weeklyFunnel)
                    }
                }

                // 3-Column Stat Trio
                item {
                    StaggeredReveal(index = 2) {
                        StatTripletRow(
                            streakDays = uiState.streakDays,
                            weeklyReclaimedText = uiState.weeklyReclaimedText,
                            weeklyAllowedOnceCount = uiState.weeklyAllowedOnceCount
                        )
                    }
                }

                // Estimation transparency note
                item {
                    StaggeredReveal(index = 3) {
                        EstimateNoteText(
                            reclaimedSecondsPerCut = uiState.preferences.reclaimedSecondsPerCut,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }

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
                                            color = scheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Everything stays private on your phone. Once Outloop steps in on Shorts or Reels, your focus wins, streak, and time reclaimed appear here.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = HabitMuted
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        OutlinedButton(
                                            onClick = { showPausePreview = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("insights_preview_pause_button"),
                                            shape = RoundedCornerShape(16.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp),
                                            border = BorderStroke(1.dp, HabitLine),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = HabitSurface,
                                                contentColor = HabitForest
                                            )
                                        ) {
                                            Text(
                                                text = "Preview pause",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
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
                                    color = scheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Everything stays private on your phone. Once Outloop steps in on Shorts or Reels, your focus wins, streak, and time reclaimed appear here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HabitMuted
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedButton(
                                    onClick = { showPausePreview = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("insights_preview_pause_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp),
                                    border = BorderStroke(1.dp, HabitLine),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = HabitSurface,
                                        contentColor = HabitForest
                                    )
                                ) {
                                    Text(
                                        text = "Preview pause",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Breakdown by app",
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onBackground
                )
            }

            item {
                StaggeredReveal(index = 4) {
                    SurgeonCard(contentPadding = PaddingValues(0.dp)) {
                        if (uiState.byAppBreakdown.isEmpty()) {
                            Text(
                                text = "Your avoided distractions by app will appear here once Outloop steps in.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HabitMuted,
                                modifier = Modifier.padding(18.dp)
                            )
                        } else if (isMosaic) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                uiState.byAppBreakdown.chunked(2).forEachIndexed { chunkIdx, pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        pair.forEachIndexed { innerIdx, stat ->
                                            AppBreakdownRow(
                                                stat = stat,
                                                barColor = breakdownColorForIndex(chunkIdx * 2 + innerIdx),
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
                            Column(modifier = Modifier.fillMaxWidth()) {
                                uiState.byAppBreakdown.forEachIndexed { index, stat ->
                                    AppBreakdownRow(
                                        stat = stat,
                                        barColor = breakdownColorForIndex(index),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    )
                                    if (index < uiState.byAppBreakdown.lastIndex) {
                                        GroupedListDivider(
                                            modifier = Modifier.padding(start = 74.dp, end = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calm on-device privacy footer matching Quiet Intent Insights mockup (Android adapted)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = HabitSubtle,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "All insights are calculated locally on your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HabitSubtle
                    )
                }
            }
        }
    }

    if (showPausePreview) {
        MindfulPausePreviewDialog(
            appName = "YouTube",
            feedLabel = "Shorts",
            breathingPauseSeconds = uiState.preferences.breathingPauseSeconds,
            onBreathingPauseChange = onSetBreathingPauseSeconds,
            onDismiss = { showPausePreview = false }
        )
    }
}

private fun breakdownColorForIndex(index: Int): Color = when (index % 3) {
    0 -> HabitForest
    1 -> HabitSageText
    else -> HabitSageSoft
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
    val scheme = MaterialTheme.colorScheme

    SurgeonCard(modifier = modifier, contentPadding = PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.this_week),
                style = MaterialTheme.typography.bodyMedium,
                color = HabitMuted
            )
            Text(
                text = "Tap a day",
                style = MaterialTheme.typography.labelMedium,
                color = HabitForest
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedIntCounter(
            target = weeklyCutsCount,
            suffix = " loops paused",
            style = MaterialTheme.typography.headlineMedium,
            color = scheme.onSurface,
            modifier = Modifier.testTag("weekly_cuts_count")
        )
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            dayBars.forEachIndexed { idx, day ->
                val isSelected = idx == selectedDayIndex
                val animatedBar by animateFloatAsState(
                    targetValue = if (day.count > 0) day.fraction.coerceAtLeast(0.18f) else 0f,
                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                    label = "weekly_bar_$idx"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectDay(idx)
                        }
                        .testTag("weekly_bar_$idx"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (isSelected && day.count > 0) day.count.toString() else " ",
                        style = MaterialTheme.typography.labelMedium,
                        color = HabitForest,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HabitSurfaceSunken),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (day.count > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((82f * animatedBar).dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected || day.isToday) HabitForest else HabitSageBar
                                    ),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                if (isSelected || day.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 5.dp)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (day.isToday) HabitAmberGlow else HabitSageTint)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = day.dayLetter,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) HabitForest else HabitSubtle
                    )
                }
            }
        }

        if (selectedDay != null) {
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                label = "selected_day_detail"
            ) { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {}
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF5F4EE))
                        .border(1.dp, HabitLine, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("selected_day_summary"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (day.isToday) "Today \u00B7 ${day.dateLabel}" else "${day.dayName} \u00B7 ${day.dateLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${day.count} paused",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HabitMuted
                        )
                    }
                    AnimatedIntCounter(
                        target = day.reclaimedMinutes,
                        prefix = "~",
                        suffix = " min saved",
                        style = MaterialTheme.typography.labelLarge,
                        color = HabitForestInk
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
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SurgeonCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(14.dp)
        ) {
            HabitIconBadge(
                icon = Icons.Rounded.LocalFireDepartment,
                tint = neu.statusWarningText,
                containerColor = HabitAmberWarmBg,
                badgeSize = 36.dp,
                iconSize = 18.dp,
                cornerRadius = 11.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedIntCounter(
                target = streakDays,
                suffix = "d",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp),
                color = scheme.onSurface,
                modifier = Modifier.testTag("insights_streak_value")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Streak",
                style = MaterialTheme.typography.bodySmall,
                color = HabitMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }

        SurgeonCard(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(14.dp)
        ) {
            HabitIconBadge(
                icon = Icons.Outlined.Schedule,
                tint = HabitReadyText,
                containerColor = HabitReadyBg,
                badgeSize = 36.dp,
                iconSize = 18.dp,
                cornerRadius = 11.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = weeklyReclaimedText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = scheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Reclaimed",
                style = MaterialTheme.typography.bodySmall,
                color = HabitMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }

        SurgeonCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(14.dp)
        ) {
            HabitIconBadge(
                icon = Icons.Rounded.Pause,
                tint = HabitForest,
                containerColor = HabitSurfaceLow,
                badgeSize = 36.dp,
                iconSize = 18.dp,
                cornerRadius = 11.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedIntCounter(
                target = weeklyAllowedOnceCount,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp),
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Passes",
                style = MaterialTheme.typography.bodySmall,
                color = HabitMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActivityFunnelCard(
    funnel: WeeklyFunnelStats,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
    val maxDenominator = funnel.detectedCount.coerceAtLeast(1)

    SurgeonCard(
        modifier = modifier.testTag("insights_activity_funnel_card"),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity funnel",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface
            )
            Text(
                text = "Last 7 days",
                style = MaterialTheme.typography.labelMedium,
                color = HabitSubtle
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        val stages = listOf(
            FunnelStageItem(
                tag = "funnel_stage_detected",
                label = "Feed detected",
                subtitle = "Matched short-form feed opened",
                count = funnel.detectedCount,
                icon = Icons.Outlined.Visibility,
                tint = HabitForestInk,
                containerColor = HabitSageTint,
                barColor = HabitSageText
            ),
            FunnelStageItem(
                tag = "funnel_stage_attempted",
                label = "Intervention attempted",
                subtitle = "Reminder shown or exit triggered",
                count = funnel.attemptedCount,
                icon = Icons.Outlined.Shield,
                tint = HabitForestInk,
                containerColor = HabitSageTint,
                barColor = HabitForest
            ),
            FunnelStageItem(
                tag = "funnel_stage_completed",
                label = "Intervention completed",
                subtitle = "Verified that the feed closed",
                count = funnel.completedCount,
                icon = Icons.Rounded.CheckCircle,
                tint = HabitReadyText,
                containerColor = HabitReadyBg,
                barColor = HabitReadyText
            ),
            FunnelStageItem(
                tag = "funnel_stage_continued",
                label = "User chose to continue",
                subtitle = "5-minute pass chosen by you",
                count = funnel.continuedCount,
                icon = Icons.Outlined.PauseCircle,
                tint = neu.statusWarningText,
                containerColor = HabitAmberWarmBg,
                barColor = neu.statusWarning
            )
        )

        stages.forEachIndexed { index, stage ->
            val targetFraction = if (stage.count <= 0) {
                0.04f
            } else {
                (stage.count.toFloat() / maxDenominator.toFloat()).coerceIn(0.08f, 1f)
            }
            val animatedFraction by animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                label = stage.tag
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {}
                    .testTag(stage.tag)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HabitIconBadge(
                        icon = stage.icon,
                        tint = stage.tint,
                        containerColor = stage.containerColor,
                        badgeSize = 36.dp,
                        iconSize = 18.dp,
                        cornerRadius = 11.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stage.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = stage.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = HabitMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedIntCounter(
                        target = stage.count,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = scheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(HabitSurfaceSunken)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .height(5.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(stage.barColor)
                    )
                }
            }

            if (index < stages.lastIndex) {
                GroupedListDivider(
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

private data class FunnelStageItem(
    val tag: String,
    val label: String,
    val subtitle: String,
    val count: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
    val containerColor: Color,
    val barColor: Color
)

@Composable
private fun EstimateNoteText(
    reclaimedSecondsPerCut: Int,
    modifier: Modifier = Modifier
) {
    val perVisitLabel = if (reclaimedSecondsPerCut % 60 == 0) {
        "${reclaimedSecondsPerCut / 60} min"
    } else {
        String.format(
            Locale.US,
            "%.1f min",
            reclaimedSecondsPerCut / 60f
        )
    }
    Text(
        text = "Estimated from verified exits \u00D7 your $perVisitLabel per-visit estimate in Settings, not measured screen time. Unverified attempts, passes, and failed exits are excluded.",
        style = MaterialTheme.typography.bodySmall,
        color = HabitMuted,
        modifier = modifier.testTag("insights_estimate_note")
    )
}

@Composable
private fun AppBreakdownRow(
    stat: AppBreakdownStat,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppBadgeIcon(packageName = stat.packageName, badgeSize = 44.dp)
        Spacer(modifier = Modifier.width(14.dp))
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
                    color = scheme.onSurface
                )
                AnimatedIntCounter(
                    target = stat.count,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HabitMuted
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
                    .clip(RoundedCornerShape(100.dp))
                    .background(HabitSurfaceSunken)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedWidth)
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(barColor)
                )
            }
        }
    }
}
