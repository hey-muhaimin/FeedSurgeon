package com.example.data.repository

import com.example.data.local.AppPreferences
import com.example.data.local.FeedSurgeonDao
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.local.InterventionEventEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.data.model.InterventionOutcome
import com.example.data.model.InterventionToastState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class InterventionRepository(
    private val dao: FeedSurgeonDao,
    private val settingsDataStore: SettingsDataStore
) {
    val allRules: Flow<List<TargetAppRuleEntity>> = dao.observeAllRules()
    val recentEvents: Flow<List<InterventionEventEntity>> = dao.observeRecentEvents()
    val preferences: Flow<AppPreferences> = settingsDataStore.preferencesFlow

    fun observeRule(packageName: String): Flow<TargetAppRuleEntity?> =
        dao.observeRuleByPackage(packageName)

    suspend fun ensureDefaultRulesSeeded() {
        val existingPackages = dao.getAllRulesSnapshot().map { it.packageName }.toSet()
        val missingDefaults = FeedSurgeonDatabase.defaultBuiltInRules().filter {
            it.packageName !in existingPackages
        }
        if (missingDefaults.isNotEmpty()) {
            dao.upsertRules(missingDefaults)
        }
    }

    suspend fun setRuleEnabled(packageName: String, enabled: Boolean) {
        val current = dao.getRuleByPackage(packageName) ?: return
        dao.updateRule(current.copy(enabled = enabled))
    }

    suspend fun setInterventionMode(packageName: String, mode: InterventionMode) {
        val current = dao.getRuleByPackage(packageName) ?: return
        dao.updateRule(current.copy(interventionMode = mode))
    }

    suspend fun setCooldownMs(packageName: String, cooldownMs: Int) {
        val current = dao.getRuleByPackage(packageName) ?: return
        dao.updateRule(current.copy(cooldownMs = cooldownMs.coerceIn(500, 1500)))
    }

    suspend fun snoozeFor15Minutes(packageName: String) {
        snoozeForMinutes(packageName, 15)
    }

    suspend fun snoozeForMinutes(packageName: String, minutes: Int) {
        val current = dao.getRuleByPackage(packageName) ?: return
        val until = System.currentTimeMillis() + minutes.coerceAtLeast(1) * 60 * 1000L
        dao.updateRule(current.copy(snoozeUntilMs = until))
    }

    suspend fun clearSnooze(packageName: String) {
        val current = dao.getRuleByPackage(packageName) ?: return
        dao.updateRule(current.copy(snoozeUntilMs = 0L, allowOnceUntilMs = 0L))
    }

    suspend fun setGlobalPause(pausedIndefinitely: Boolean) {
        settingsDataStore.setGlobalPauseUntilMs(if (pausedIndefinitely) Long.MAX_VALUE else 0L)
    }

    suspend fun pauseGloballyForMinutes(minutes: Int) {
        val until = System.currentTimeMillis() + minutes.coerceAtLeast(1) * 60 * 1000L
        settingsDataStore.setGlobalPauseUntilMs(until)
    }

    suspend fun resumeGlobalProtection() {
        settingsDataStore.setGlobalPauseUntilMs(0L)
    }

    suspend fun allowOnceForEvent(eventId: Long, packageName: String) {
        dao.markEventAllowedOnce(eventId)
        val current = dao.getRuleByPackage(packageName) ?: return
        val until = System.currentTimeMillis() + 5 * 60 * 1000L
        dao.updateRule(current.copy(allowOnceUntilMs = until))
        DiagnosticsBridge.dismissToastIfEvent(eventId)
    }

    suspend fun allowOnceTemporary(packageName: String) {
        val current = dao.getRuleByPackage(packageName) ?: return
        val until = System.currentTimeMillis() + 5 * 60 * 1000L
        dao.updateRule(current.copy(allowOnceUntilMs = until))
    }

    suspend fun undoIntervention(eventId: Long, packageName: String) {
        dao.deleteEventById(eventId)
        val current = dao.getRuleByPackage(packageName)
        if (current != null) {
            dao.updateRule(current.copy(allowOnceUntilMs = System.currentTimeMillis() + 60 * 1000L))
        }
        DiagnosticsBridge.dismissToastIfEvent(eventId)
    }

    suspend fun recordIntervention(
        rule: TargetAppRuleEntity,
        matchedSelector: String,
        actionTaken: InterventionMode,
        outcome: InterventionOutcome = InterventionOutcome.DETECTED
    ): Long {
        val prefs = settingsDataStore.preferencesFlow.first()
        val now = System.currentTimeMillis()
        val eventId = dao.insertEvent(
            InterventionEventEntity(
                packageName = rule.packageName,
                appName = rule.displayName,
                feedLabel = rule.feedLabel,
                matchedSelector = matchedSelector,
                actionTaken = actionTaken,
                timestampMs = now,
                reclaimedSeconds = prefs.reclaimedSecondsPerCut,
                wasAllowedOnce = outcome == InterventionOutcome.ALLOWED_ONCE,
                wasAttempted = outcome != InterventionOutcome.DETECTED,
                outcome = outcome
            )
        )
        if (prefs.showInterventionBanner && outcome == InterventionOutcome.CONFIRMED) {
            DiagnosticsBridge.publishToast(
                InterventionToastState(
                    eventId = eventId,
                    packageName = rule.packageName,
                    feedLabel = rule.feedLabel,
                    message = "${rule.displayName} ${rule.feedLabel} paused",
                    timestampMs = now
                )
            )
        }
        return eventId
    }

    suspend fun markEventAttempted(eventId: Long) {
        dao.markEventAttempted(eventId)
    }

    suspend fun updateInterventionOutcome(eventId: Long, outcome: InterventionOutcome) {
        dao.updateEventOutcome(eventId, outcome)
        when (outcome) {
            InterventionOutcome.CONFIRMED -> {
                val prefs = settingsDataStore.preferencesFlow.first()
                if (prefs.showInterventionBanner) {
                    val event = dao.getEventById(eventId)
                    if (event != null) {
                        DiagnosticsBridge.publishToast(
                            InterventionToastState(
                                eventId = event.id,
                                packageName = event.packageName,
                                feedLabel = event.feedLabel,
                                message = "${event.appName} ${event.feedLabel} paused",
                                timestampMs = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            InterventionOutcome.FAILED,
            InterventionOutcome.ALLOWED_ONCE -> {
                DiagnosticsBridge.dismissToastIfEvent(eventId)
            }
            InterventionOutcome.DETECTED,
            InterventionOutcome.ATTEMPTED -> Unit
        }
    }

    suspend fun updateSelectorHealth(
        packageName: String,
        matchedCount: Int,
        totalCount: Int,
        outdated: Boolean
    ) {
        val current = dao.getRuleByPackage(packageName) ?: return
        dao.updateRule(
            current.copy(
                matchedSelectorCount = matchedCount,
                totalSelectorCount = totalCount,
                isOutdated = outdated
            )
        )
    }

    suspend fun upsertCustomRule(
        packageName: String,
        displayName: String,
        feedLabel: String,
        viewIds: List<String>,
        tabFallbackLabel: String,
        homeTabLabel: String,
        interventionMode: InterventionMode
    ) {
        val cleanPkg = packageName.trim()
        val existing = dao.getRuleByPackage(cleanPkg)
        val cleanIds = viewIds.map { it.trim() }.filter { it.isNotEmpty() }
        dao.upsertRule(
            TargetAppRuleEntity(
                packageName = cleanPkg,
                displayName = displayName.trim().ifEmpty { cleanPkg },
                feedLabel = feedLabel.trim().ifEmpty { "Short feed" },
                enabled = existing?.enabled ?: true,
                interventionMode = interventionMode,
                cooldownMs = existing?.cooldownMs ?: 800,
                homeTabLabel = homeTabLabel.trim().ifEmpty { "Home" },
                viewIdsRaw = cleanIds.joinToString("|"),
                tabFallbackLabel = tabFallbackLabel.trim(),
                snoozeUntilMs = existing?.snoozeUntilMs ?: 0L,
                allowOnceUntilMs = existing?.allowOnceUntilMs ?: 0L,
                isBuiltIn = existing?.isBuiltIn ?: false,
                matchedSelectorCount = cleanIds.size,
                totalSelectorCount = cleanIds.size,
                isOutdated = false
            )
        )
    }

    suspend fun deleteCustomRule(packageName: String) {
        dao.deleteCustomRule(packageName)
    }

    suspend fun clearHistory() {
        dao.clearAllEvents()
        DiagnosticsBridge.dismissToast()
    }

    companion object {
        fun startOfTodayMs(nowMs: Long = System.currentTimeMillis()): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun startOfWeekDaysAgoMs(daysAgo: Int, nowMs: Long = System.currentTimeMillis()): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = startOfTodayMs(nowMs)
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            return cal.timeInMillis
        }
    }
}
