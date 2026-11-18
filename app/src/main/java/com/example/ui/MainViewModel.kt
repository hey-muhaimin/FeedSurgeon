package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FeedSurgeonApp
import com.example.data.local.AppPreferences
import com.example.data.local.InterventionEventEntity
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.data.model.InterventionToastState
import com.example.data.repository.DiagnosticsBridge
import com.example.data.repository.InterventionRepository
import com.example.data.repository.RulesetSyncResult
import com.example.service.AccessibilityStatusHelper
import com.example.service.InstalledAppDetector
import com.example.service.ProtectionHealthHelper
import com.example.worker.RulesetSyncWorker
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
    val installedPackages: Set<String> = emptySet(),
    val isAccessibilityEnabled: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isNotificationPermissionGranted: Boolean = true,
    val todayCutsCount: Int = 0,
    val todayReclaimedMinutes: Int = 0,
    val fiveDayBars: List<Float> = listOf(0.15f, 0.15f, 0.15f, 0.15f, 0.15f),
    val staleRulesCount: Int = 0,
    val weeklyCutsCount: Int = 0,
    val weeklyDayBars: List<DayBarStat> = emptyList(),
    val weeklyReclaimedText: String = "0 min",
    val weeklyAllowedOnceCount: Int = 0,
    val byAppBreakdown: List<AppBreakdownStat> = emptyList(),
    val recentToast: InterventionToastState? = null,
    val isSyncingRules: Boolean = false,
    val syncStatusMessage: String? = null,
    val eventLogPreview: String = "",
    val eventLogSizeBytes: Long = 0L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FeedSurgeonApp
    private val interventionRepo = app.interventionRepository
    private val rulesetRepo = app.rulesetRepository
    private val settingsDataStore = app.settingsDataStore
    private val eventLogger = app.eventLogger

    private val _accessibilityEnabled = MutableStateFlow(
        AccessibilityStatusHelper.isServiceEnabled(application)
    )
    private val _batteryOptimizationIgnored = MutableStateFlow(
        ProtectionHealthHelper.isIgnoringBatteryOptimizations(application)
    )
    private val _notificationPermissionGranted = MutableStateFlow(
        ProtectionHealthHelper.isNotificationPermissionGranted(application)
    )
    private val _installedPackages = MutableStateFlow<Set<String>>(emptySet())
    private val _isSyncingRules = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)
    private val _eventLogPreview = MutableStateFlow("")
    private val _eventLogSizeBytes = MutableStateFlow(0L)

    init {
        refreshEventLog()
        viewModelScope.launch {
            eventLogger.logVersion.collect {
                _eventLogPreview.value = eventLogger.readTail(120)
                _eventLogSizeBytes.value = eventLogger.getFileSizeBytes()
            }
        }
    }

    private data class TransientState(
        val accessibilityEnabled: Boolean,
        val batteryOptimizationIgnored: Boolean,
        val notificationPermissionGranted: Boolean,
        val installedPackages: Set<String>,
        val isSyncing: Boolean,
        val syncMessage: String?,
        val toast: InterventionToastState?,
        val logPreview: String,
        val logSizeBytes: Long
    )

    private val syncAndLogFlow = combine(
        _isSyncingRules,
        _syncMessage,
        DiagnosticsBridge.recentToast,
        _eventLogPreview,
        _eventLogSizeBytes
    ) { isSyncing, syncMsg, toast, logPreview, logBytes ->
        Tuple5(isSyncing, syncMsg, toast, logPreview, logBytes)
    }

    private data class Tuple5<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

    private val environmentFlow = combine(
        _accessibilityEnabled,
        _batteryOptimizationIgnored,
        _notificationPermissionGranted,
        _installedPackages
    ) { accessibilityEnabled, batteryIgnored, notifGranted, installedPackages ->
        Tuple5(accessibilityEnabled, batteryIgnored, notifGranted, installedPackages, Unit)
    }

    private val transientFlow = combine(
        environmentFlow,
        syncAndLogFlow
    ) { env, extra ->
        TransientState(
            accessibilityEnabled = env.first,
            batteryOptimizationIgnored = env.second,
            notificationPermissionGranted = env.third,
            installedPackages = env.fourth,
            isSyncing = extra.first,
            syncMessage = extra.second,
            toast = extra.third,
            logPreview = extra.fourth,
            logSizeBytes = extra.fifth
        )
    }

    val uiState: StateFlow<MainUiState> = combine(
        interventionRepo.preferences,
        interventionRepo.allRules,
        interventionRepo.recentEvents,
        transientFlow
    ) { prefs, rules, events, transient ->
        val installed = transient.installedPackages.ifEmpty {
            InstalledAppDetector.getInstalledPackages(app, rules.map { it.packageName })
        }
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
        val staleCount = rules.count { it.isOutdated && it.packageName in installed }

        MainUiState(
            isInitialLoading = false,
            preferences = prefs,
            rules = rules,
            installedPackages = installed,
            isAccessibilityEnabled = transient.accessibilityEnabled,
            isBatteryOptimizationIgnored = transient.batteryOptimizationIgnored,
            isNotificationPermissionGranted = transient.notificationPermissionGranted,
            todayCutsCount = todayEvents.size,
            todayReclaimedMinutes = todaySeconds / 60,
            fiveDayBars = fiveDayBars,
            staleRulesCount = staleCount,
            weeklyCutsCount = weekEvents.size,
            weeklyDayBars = weeklyBars,
            weeklyReclaimedText = weekReclaimedText,
            weeklyAllowedOnceCount = weekAllowedOnce,
            byAppBreakdown = byApp,
            recentToast = transient.toast,
            isSyncingRules = transient.isSyncing,
            syncStatusMessage = transient.syncMessage,
            eventLogPreview = transient.logPreview,
            eventLogSizeBytes = transient.logSizeBytes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun refreshAccessibilityStatus(context: Context) {
        _accessibilityEnabled.value = AccessibilityStatusHelper.isServiceEnabled(context)
        refreshProtectionHealth(context)
    }

    fun refreshProtectionHealth(context: Context) {
        _batteryOptimizationIgnored.value =
            ProtectionHealthHelper.isIgnoringBatteryOptimizations(context)
        _notificationPermissionGranted.value =
            ProtectionHealthHelper.isNotificationPermissionGranted(context)
    }

    fun refreshInstalledApps(context: Context) {
        val packages = uiState.value.rules.map { it.packageName }.ifEmpty {
            com.example.data.local.FeedSurgeonDatabase.BUILT_IN_PACKAGE_NAMES.toList()
        }
        _installedPackages.value = InstalledAppDetector.getInstalledPackages(context, packages)
    }

    fun triggerAutoSyncIfNeeded() {
        viewModelScope.launch {
            rulesetRepo.ensureLocalCacheSeeded()
            when (val result = rulesetRepo.autoSyncIfDue(force = false)) {
                is RulesetSyncResult.Success -> {
                    if (!result.fromLocalCache) {
                        _syncMessage.value = "Auto-updated to ${result.version} (${result.updatedApps} apps)"
                    }
                }
                is RulesetSyncResult.Error -> Unit
            }
        }
    }

    fun setAutoUpdateRules(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoUpdateRules(enabled)
            if (enabled) {
                RulesetSyncWorker.schedulePeriodicSync(app)
                triggerAutoSyncIfNeeded()
            } else {
                RulesetSyncWorker.cancelPeriodicSync(app)
            }
        }
    }

    fun setEventCaptureEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setEventCaptureEnabled(enabled)
            refreshEventLog()
        }
    }

    fun refreshEventLog() {
        viewModelScope.launch {
            _eventLogPreview.value = eventLogger.readTail(120)
            _eventLogSizeBytes.value = eventLogger.getFileSizeBytes()
        }
    }

    fun clearEventLog() {
        viewModelScope.launch {
            eventLogger.clear()
            _eventLogPreview.value = ""
            _eventLogSizeBytes.value = 0L
        }
    }

    fun createShareLogIntent(): Intent? {
        val uri = eventLogger.getShareUri() ?: return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Feed Surgeon Accessibility Event Log")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
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

    fun updateRemoteRules() {
        viewModelScope.launch {
            _isSyncingRules.value = true
            _syncMessage.value = null
            when (val result = rulesetRepo.syncFromRemoteOrFallback()) {
                is RulesetSyncResult.Success -> {
                    _syncMessage.value = when {
                        result.etagUnchanged ->
                            "Ruleset unchanged (ETag verified) \u00B7 ${result.version}"
                        result.revertedToHardcoded ->
                            "Using bundled rules ${result.version} (${result.updatedApps} apps)"
                        result.fromLocalCache ->
                            result.networkErrorReason
                                ?: "Using local ruleset ${result.version} (${result.updatedApps} apps)"
                        else ->
                            "Rules up to date \u00B7 ${result.version} (${result.updatedApps} apps)"
                    }
                }
                is RulesetSyncResult.Error -> {
                    _syncMessage.value = result.message
                }
            }
            _isSyncingRules.value = false
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
