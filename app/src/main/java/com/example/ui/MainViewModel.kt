package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FeedSurgeonApp
import com.example.data.local.AppPreferences
import com.example.data.local.InterventionEventEntity
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.data.model.InterventionToastState
import com.example.data.model.LiveTreeSnapshot
import com.example.data.repository.DiagnosticsBridge
import com.example.data.repository.InterventionRepository
import com.example.data.repository.RulesetSyncResult
import com.example.service.AccessibilityStatusHelper
import com.example.service.SelectorMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class DayBarStat(
    val dayLetter: String,
    val count: Int,
    val fraction: Float,
    val isToday: Boolean
)

data class AppBreakdownStat(
    val packageName: String,
    val label: String,
    val count: Int,
    val fraction: Float
)

data class MainUiState(
    val isInitialLoading: Boolean = true,
    val preferences: AppPreferences = AppPreferences(),
    val rules: List<TargetAppRuleEntity> = emptyList(),
    val isAccessibilityEnabled: Boolean = false,
    val todayCutsCount: Int = 0,
    val todayReclaimedMinutes: Int = 0,
    val fiveDayBars: List<Float> = listOf(0.15f, 0.15f, 0.15f, 0.15f, 0.15f),
    val staleRulesCount: Int = 0,
    val weeklyCutsCount: Int = 0,
    val weeklyDayBars: List<DayBarStat> = emptyList(),
    val weeklyReclaimedText: String = "0 min",
    val weeklyAllowedOnceCount: Int = 0,
    val byAppBreakdown: List<AppBreakdownStat> = emptyList(),
    val liveSnapshot: LiveTreeSnapshot? = null,
    val recentToast: InterventionToastState? = null,
    val isSyncingRules: Boolean = false,
    val syncStatusMessage: String? = null,
    val selectorTestQuery: String = "id/reel_watch_fragment_root",
    val selectorTestMatchCount: Int? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FeedSurgeonApp
    private val interventionRepo = app.interventionRepository
    private val rulesetRepo = app.rulesetRepository
    private val settingsDataStore = app.settingsDataStore

    private val _accessibilityEnabled = MutableStateFlow(
        AccessibilityStatusHelper.isServiceEnabled(application)
    )
    private val _isSyncingRules = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)
    private val _selectorQuery = MutableStateFlow("id/reel_watch_fragment_root")
    private val _selectorMatchCount = MutableStateFlow<Int?>(null)

    private data class TransientState(
        val accessibilityEnabled: Boolean,
        val isSyncing: Boolean,
        val syncMessage: String?,
        val selectorQuery: String,
        val selectorMatchCount: Int?,
        val liveSnapshot: LiveTreeSnapshot?,
        val toast: InterventionToastState?
    )

    private val transientFlow = combine(
        _accessibilityEnabled,
        _isSyncingRules,
        _syncMessage,
        _selectorQuery,
        _selectorMatchCount,
        DiagnosticsBridge.liveSnapshot,
        DiagnosticsBridge.recentToast
    ) { values ->
        TransientState(
            accessibilityEnabled = values[0] as Boolean,
            isSyncing = values[1] as Boolean,
            syncMessage = values[2] as String?,
            selectorQuery = values[3] as String,
            selectorMatchCount = values[4] as Int?,
            liveSnapshot = values[5] as LiveTreeSnapshot?,
            toast = values[6] as InterventionToastState?
        )
    }

    val uiState: StateFlow<MainUiState> = combine(
        interventionRepo.preferences,
        interventionRepo.allRules,
        interventionRepo.recentEvents,
        transientFlow
    ) { prefs, rules, events, transient ->
        val nowMs = System.currentTimeMillis()
        val todayStart = InterventionRepository.startOfTodayMs(nowMs)
        val todayEvents = events.filter { it.timestampMs >= todayStart }
        val todaySeconds = todayEvents.sumOf { it.reclaimedSeconds }

        val fiveDayBars = buildFiveDaySparkline(events, nowMs)
        val weeklyBars = buildWeeklyDayBars(events, nowMs)
        val weekStart = InterventionRepository.startOfWeekDaysAgoMs(6, nowMs)
        val weekEvents = events.filter { it.timestampMs >= weekStart }
        val weekSeconds = weekEvents.sumOf { it.reclaimedSeconds }
        val weekReclaimedText = formatReclaimedDuration(weekSeconds)
        val weekAllowedOnce = weekEvents.count { it.wasAllowedOnce }

        val byApp = buildAppBreakdown(rules, weekEvents)
        val staleCount = rules.count { it.isOutdated }

        val activeSnapshot = transient.liveSnapshot
            ?: DiagnosticsBridge.createDiagnosticSampleSnapshot("com.google.android.youtube")
        val initialMatchCount = transient.selectorMatchCount
            ?: DiagnosticsBridge.countSelectorMatches(transient.selectorQuery, activeSnapshot)

        MainUiState(
            isInitialLoading = false,
            preferences = prefs,
            rules = rules,
            isAccessibilityEnabled = transient.accessibilityEnabled,
            todayCutsCount = todayEvents.size,
            todayReclaimedMinutes = todaySeconds / 60,
            fiveDayBars = fiveDayBars,
            staleRulesCount = staleCount,
            weeklyCutsCount = weekEvents.size,
            weeklyDayBars = weeklyBars,
            weeklyReclaimedText = weekReclaimedText,
            weeklyAllowedOnceCount = weekAllowedOnce,
            byAppBreakdown = byApp,
            liveSnapshot = activeSnapshot,
            recentToast = transient.toast,
            isSyncingRules = transient.isSyncing,
            syncStatusMessage = transient.syncMessage,
            selectorTestQuery = transient.selectorQuery,
            selectorTestMatchCount = initialMatchCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun refreshAccessibilityStatus(context: Context) {
        _accessibilityEnabled.value = AccessibilityStatusHelper.isServiceEnabled(context)
    }

    fun acceptDisclosure() {
        viewModelScope.launch {
            settingsDataStore.setDisclosureAccepted(true)
        }
    }

    fun declineDisclosure() {
        viewModelScope.launch {
            settingsDataStore.setDisclosureAccepted(false)
            settingsDataStore.setOnboardingCompleted(true)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
        }
    }

    fun reopenOnboarding() {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(false)
        }
    }

    fun setRuleEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            interventionRepo.setRuleEnabled(packageName, enabled)
        }
    }

    fun setInterventionMode(packageName: String, mode: InterventionMode) {
        viewModelScope.launch {
            interventionRepo.setInterventionMode(packageName, mode)
        }
    }

    fun setCooldownMs(packageName: String, cooldownMs: Int) {
        viewModelScope.launch {
            interventionRepo.setCooldownMs(packageName, cooldownMs)
        }
    }

    fun snooze15Minutes(packageName: String) {
        viewModelScope.launch {
            interventionRepo.snoozeFor15Minutes(packageName)
        }
    }

    fun resumeFromSnooze(packageName: String) {
        viewModelScope.launch {
            interventionRepo.clearSnooze(packageName)
        }
    }

    fun allowOnceFromToast(toast: InterventionToastState) {
        viewModelScope.launch {
            interventionRepo.allowOnceForEvent(toast.eventId, toast.packageName)
        }
    }

    fun undoFromToast(toast: InterventionToastState) {
        viewModelScope.launch {
            interventionRepo.undoIntervention(toast.eventId, toast.packageName)
        }
    }

    fun dismissToast() {
        DiagnosticsBridge.dismissToast()
    }

    fun updateRemoteRules(customUrl: String? = null) {
        viewModelScope.launch {
            _isSyncingRules.value = true
            _syncMessage.value = null
            if (customUrl != null) {
                settingsDataStore.setRulesetEndpointUrl(customUrl)
            }
            when (val result = rulesetRepo.syncFromRemoteOrFallback(customUrl)) {
                is RulesetSyncResult.Success -> {
                    _syncMessage.value = if (result.fromBundledFallback) {
                        "Synced bundled ruleset ${result.version} (${result.updatedApps} apps)"
                    } else {
                        "Updated to ${result.version} (${result.updatedApps} apps)"
                    }
                }
                is RulesetSyncResult.Error -> {
                    _syncMessage.value = result.message
                }
            }
            _isSyncingRules.value = false
        }
    }

    fun saveRulesetEndpointUrl(url: String) {
        viewModelScope.launch {
            settingsDataStore.setRulesetEndpointUrl(url)
            _syncMessage.value = "Endpoint URL saved locally"
        }
    }

    fun restoreBundledRules() {
        viewModelScope.launch {
            _isSyncingRules.value = true
            when (val result = rulesetRepo.restoreBundledRuleset()) {
                is RulesetSyncResult.Success -> {
                    _syncMessage.value = "Restored ${result.version}"
                }
                is RulesetSyncResult.Error -> {
                    _syncMessage.value = result.message
                }
            }
            _isSyncingRules.value = false
        }
    }

    fun updateSelectorTestQuery(query: String) {
        _selectorQuery.value = query
        val snapshot = uiState.value.liveSnapshot
        _selectorMatchCount.value = DiagnosticsBridge.countSelectorMatches(query, snapshot)
    }

    fun runSelectorTest() {
        val query = _selectorQuery.value
        val snapshot = uiState.value.liveSnapshot
        _selectorMatchCount.value = DiagnosticsBridge.countSelectorMatches(query, snapshot)
    }

    fun runRuleHealthCheck(packageName: String) {
        viewModelScope.launch {
            val rules = uiState.value.rules
            val target = rules.firstOrNull { it.packageName == packageName } ?: return@launch
            val currentLive = DiagnosticsBridge.liveSnapshot.value
            val snapshot = if (currentLive != null && currentLive.packageName == packageName) {
                currentLive
            } else {
                DiagnosticsBridge.createDiagnosticSampleSnapshot(packageName).also {
                    DiagnosticsBridge.publishSnapshot(it)
                }
            }
            val matched = SelectorMatcher.matchSelectorInSnapshot(snapshot, target)
            val matchedCount = target.viewIds.count { id ->
                snapshot.nodes.any { it.viewId == id }
            }.let { if (it == 0 && matched != null) 1 else it }

            interventionRepo.updateSelectorHealth(
                packageName = packageName,
                matchedCount = matchedCount.coerceAtLeast(if (matched != null) 1 else 0),
                totalCount = target.viewIds.size.coerceAtLeast(1),
                outdated = matched == null
            )
            _syncMessage.value = if (matched != null) {
                "${target.displayName} verified against node tree ($matchedCount/${target.viewIds.size} IDs)"
            } else {
                "No selectors matched in current ${target.displayName} snapshot"
            }
        }
    }

    fun triggerDiagnosticIntervention(rule: TargetAppRuleEntity) {
        viewModelScope.launch {
            val selector = rule.viewIds.firstOrNull() ?: "tab:\"${rule.tabFallbackLabel}\""
            interventionRepo.recordIntervention(
                rule = rule,
                matchedSelector = selector,
                actionTaken = rule.interventionMode
            )
        }
    }

    fun saveCustomRule(
        packageName: String,
        displayName: String,
        feedLabel: String,
        viewIdsText: String,
        tabFallbackLabel: String,
        homeTabLabel: String,
        interventionMode: InterventionMode
    ) {
        viewModelScope.launch {
            val ids = viewIdsText.split(",", "\n", "|")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            interventionRepo.upsertCustomRule(
                packageName = packageName,
                displayName = displayName,
                feedLabel = feedLabel,
                viewIds = ids,
                tabFallbackLabel = tabFallbackLabel,
                homeTabLabel = homeTabLabel,
                interventionMode = interventionMode
            )
        }
    }

    fun deleteCustomRule(packageName: String) {
        viewModelScope.launch {
            interventionRepo.deleteCustomRule(packageName)
        }
    }

    fun setReclaimedSecondsPerCut(seconds: Int) {
        viewModelScope.launch {
            settingsDataStore.setReclaimedSecondsPerCut(seconds)
        }
    }

    fun setShowInterventionBanner(show: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShowInterventionBanner(show)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            interventionRepo.clearHistory()
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    private fun buildFiveDaySparkline(
        events: List<InterventionEventEntity>,
        nowMs: Long
    ): List<Float> {
        val counts = (4 downTo 0).map { daysAgo ->
            val dayStart = InterventionRepository.startOfWeekDaysAgoMs(daysAgo, nowMs)
            val dayEnd = dayStart + 24L * 60 * 60 * 1000
            events.count { it.timestampMs in dayStart until dayEnd }
        }
        val max = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
        return counts.map { count ->
            if (count == 0) 0.16f else (0.22f + 0.78f * (count.toFloat() / max.toFloat()))
        }
    }

    private fun buildWeeklyDayBars(
        events: List<InterventionEventEntity>,
        nowMs: Long
    ): List<DayBarStat> {
        val cal = Calendar.getInstance()
        val raw = (6 downTo 0).map { daysAgo ->
            val dayStart = InterventionRepository.startOfWeekDaysAgoMs(daysAgo, nowMs)
            val dayEnd = dayStart + 24L * 60 * 60 * 1000
            cal.timeInMillis = dayStart
            val letter = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "S"
                else -> "S"
            }
            Triple(letter, events.count { it.timestampMs in dayStart until dayEnd }, daysAgo == 0)
        }
        val max = raw.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
        return raw.map { (letter, count, isToday) ->
            DayBarStat(
                dayLetter = letter,
                count = count,
                fraction = if (count == 0) 0.14f else (0.22f + 0.78f * (count.toFloat() / max.toFloat())),
                isToday = isToday
            )
        }
    }

    private fun buildAppBreakdown(
        rules: List<TargetAppRuleEntity>,
        weekEvents: List<InterventionEventEntity>
    ): List<AppBreakdownStat> {
        val countsByPkg = weekEvents.groupingBy { it.packageName }.eachCount()
        val maxCount = (countsByPkg.values.maxOrNull() ?: 0).coerceAtLeast(1)
        return rules.map { rule ->
            val count = countsByPkg[rule.packageName] ?: 0
            AppBreakdownStat(
                packageName = rule.packageName,
                label = "${rule.displayName} ${rule.feedLabel}",
                count = count,
                fraction = if (count == 0) 0.04f else (count.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
            )
        }
    }

    private fun formatReclaimedDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        return if (minutes >= 60) {
            val hours = totalSeconds / 3600f
            String.format(Locale.US, "~%.1f h", hours)
        } else {
            "~$minutes min"
        }
    }
}
