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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
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
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.AppBadgeIcon
import com.example.ui.components.GroupedListDivider
import com.example.ui.components.HabitIconBadge
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.StillspaceEmptyFocusArtwork
import com.example.ui.components.StreakBadgePill
import com.example.ui.components.SurgeonCard
import com.example.ui.components.SurgeonScreenHeader
import com.example.ui.theme.StaggeredReveal
import com.example.ui.theme.StillspaceTheme
import java.util.Locale

@Composable
fun InsightsScreen(
    uiState: MainUiState,
    bottomContentPadding: Dp = 96.dp
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
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

                // 3-Column Bento Stat Cards
                item {
                    StaggeredReveal(index = 1) {
                        StatTripletRow(
                            streakDays = uiState.streakDays,
                            weeklyReclaimedText = uiState.weeklyReclaimedText,
                            weeklyAllowedOnceCount = uiState.weeklyAllowedOnceCount
                        )
                    }
                }

                // Estimation transparency note
                item {
                    StaggeredReveal(index = 2) {
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
                                            text = "Everything stays private on your phone. Once Stillspace steps in on Shorts or Reels, your focus wins, streak, and time reclaimed appear here.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = scheme.onSurfaceVariant
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
                                            border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = neu.secondaryButtonBg,
                                                contentColor = scheme.primary
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
                                    text = "Everything stays private on your phone. Once Stillspace steps in on Shorts or Reels, your focus wins, streak, and time reclaimed appear here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant
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
                                    border = BorderStroke(1.dp, neu.secondaryButtonBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = neu.secondaryButtonBg,
                                        contentColor = scheme.primary
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
                    text = "By app",
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onBackground
                )
            }

            item {
                StaggeredReveal(index = 4) {
                    SurgeonCard(contentPadding = PaddingValues(0.dp)) {
                        if (uiState.byAppBreakdown.isEmpty()) {
                            Text(
                                text = "Your avoided distractions by app will appear here once Stillspace steps in.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(18.dp)
                            )
                        } else if (isMosaic) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
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
                            Column(modifier = Modifier.fillMaxWidth()) {
                                uiState.byAppBreakdown.forEachIndexed { index, stat ->
                                    AppBreakdownRow(
                                        stat = stat,
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
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    SurgeonCard(modifier = modifier, contentPadding = PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.this_week),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = "Tap a day",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedIntCounter(
            target = weeklyCutsCount,
            suffix = " distractions avoided",
            style = MaterialTheme.typography.headlineMedium,
            color = scheme.onSurface,
            modifier = Modifier.testTag("weekly_cuts_count")
        )
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
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
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(neu.trackInactive)
                            .then(
                                if (neu.isDark) {
                                    Modifier.border(1.dp, neu.trackInactiveBorder, RoundedCornerShape(12.dp))
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (day.count > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((82f * animatedBar).dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected || day.isToday -> scheme.primary
                                            else -> scheme.primary.copy(alpha = 0.45f)
                                        }
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = day.dayLetter,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) scheme.primary else scheme.onSurfaceVariant
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
                        .background(scheme.primaryContainer)
                        .border(1.dp, neu.indigoTintBorder, RoundedCornerShape(14.dp))
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
                            text = "${day.count} avoided",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    AnimatedIntCounter(
                        target = day.reclaimedMinutes,
                        prefix = "~",
                        suffix = " min saved",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onPrimaryContainer
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
                containerColor = neu.statusWarningBg,
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
                color = scheme.onSurfaceVariant,
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
                tint = neu.statusActiveText,
                containerColor = neu.statusActiveBg,
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
                color = scheme.onSurfaceVariant,
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
                tint = scheme.onSurface,
                containerColor = neu.trackInactive,
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
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
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
            Locale.US,
            "%.1f min",
            reclaimedSecondsPerCut / 60f
        )
    }
    Text(
        text = "Estimated from confirmed exits \u00D7 your $perVisitLabel per-visit estimate in Settings, not measured screen time. Failed exits are excluded.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.testTag("insights_estimate_note")
    )
}

@Composable
private fun AppBreakdownRow(
    stat: AppBreakdownStat,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors
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
                    color = scheme.onSurfaceVariant
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
                    .background(neu.trackInactive)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedWidth)
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(scheme.primary)
                )
            }
        }
    }
}
