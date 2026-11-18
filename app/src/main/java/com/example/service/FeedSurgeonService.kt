package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.FeedSurgeonApp
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.data.repository.DiagnosticsBridge
import com.example.worker.RulesetSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class FeedSurgeonService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lastBlockTimestamps = ConcurrentHashMap<String, Long>()
    private val lastHealthTimestamps = ConcurrentHashMap<String, Long>()
    private val lastLogTimestamps = ConcurrentHashMap<String, Long>()
    private val consecutiveTabFallbackCounts = ConcurrentHashMap<String, Int>()

    @Volatile
    private var activeRulesByPackage: Map<String, TargetAppRuleEntity> = emptyMap()

    @Volatile
    private var isEventCaptureEnabled: Boolean = false

    private var overlayShield: AccessibilityOverlayShield? = null

    private val app: FeedSurgeonApp
        get() = applicationContext as FeedSurgeonApp

    override fun onServiceConnected() {
        super.onServiceConnected()
        DiagnosticsBridge.setServiceConnected(true)
        overlayShield = AccessibilityOverlayShield(this)

        // Pre-seed immediately so zero accessibility events are missed before Room emits
        activeRulesByPackage = FeedSurgeonDatabase.defaultBuiltInRules()
            .filter { it.enabled }
            .associateBy { it.packageName }
        updateServicePackageScope(FeedSurgeonDatabase.BUILT_IN_PACKAGE_NAMES.toList())

        // Schedule WorkManager periodic Gist sync directly from FeedSurgeonService lifecycle
        RulesetSyncWorker.schedulePeriodicSync(this)

        serviceScope.launch {
            app.interventionRepository.ensureDefaultRulesSeeded()
            app.rulesetRepository.ensureLocalCacheSeeded()
            app.rulesetRepository.autoSyncIfDue(force = false)
            app.interventionRepository.allRules.collectLatest { rules ->
                val enabledMap = rules.filter { it.enabled }.associateBy { it.packageName }
                activeRulesByPackage = enabledMap
                updateServicePackageScope(rules.map { it.packageName })
            }
        }

        serviceScope.launch {
            app.settingsDataStore.preferencesFlow.collectLatest { prefs ->
                isEventCaptureEnabled = prefs.eventCaptureEnabled
                if (prefs.autoUpdateRules) {
                    RulesetSyncWorker.schedulePeriodicSync(this@FeedSurgeonService)
                } else {
                    RulesetSyncWorker.cancelPeriodicSync(this@FeedSurgeonService)
                }
            }
        }
    }

    private fun updateServicePackageScope(packages: List<String>) {
        val currentInfo = serviceInfo ?: AccessibilityServiceInfo()
        val scopedPackages = packages.distinct().ifEmpty {
            FeedSurgeonDatabase.BUILT_IN_PACKAGE_NAMES.toList()
        }.toTypedArray()

        currentInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100L
            packageNames = scopedPackages
        }
        try {
            serviceInfo = currentInfo
        } catch (_: Exception) {
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val captureLogs = isEventCaptureEnabled
        val eventType = event.eventType
        val eventClassName = event.className?.toString()
        val nowElapsed = SystemClock.elapsedRealtime()

        val rule = activeRulesByPackage[pkg]
        if (rule == null) {
            if (captureLogs) {
                logEventThrottled(pkg, eventType, eventClassName, null, null, "SKIPPED:no_active_rule", nowElapsed)
            }
            return
        }
        if (!rule.enabled) {
            if (captureLogs) {
                logEventThrottled(pkg, eventType, eventClassName, null, null, "SKIPPED:rule_disabled", nowElapsed)
            }
            return
        }

        if (overlayShield?.isShowing() == true) {
            if (captureLogs) {
                logEventThrottled(pkg, eventType, eventClassName, null, null, "SKIPPED:overlay_showing", nowElapsed)
            }
            return
        }

        val rootNode = resolveTargetRootNode(pkg)
        if (rootNode == null) {
            if (captureLogs) {
                logEventThrottled(pkg, eventType, eventClassName, null, null, "SKIPPED:no_root_window", nowElapsed)
            }
            return
        }

        val matchedSelector = SelectorMatcher.findMatchingSelector(rootNode, rule)
        if (matchedSelector == null) {
            if (captureLogs) {
                logEventThrottled(pkg, eventType, eventClassName, rootNode, null, "NO_MATCH", nowElapsed)
            }
            return
        }

        if (rule.isCurrentlySnoozed(System.currentTimeMillis())) {
            if (captureLogs) {
                logEventThrottled(pkg, eventType, eventClassName, rootNode, matchedSelector, "SKIPPED:snoozed", nowElapsed)
            }
            return
        }

        val lastBlock = lastBlockTimestamps[pkg] ?: 0L
        if (nowElapsed - lastBlock < rule.cooldownMs) {
            if (captureLogs) {
                logEventThrottled(pkg, eventType, eventClassName, rootNode, matchedSelector, "SKIPPED:cooldown", nowElapsed)
            }
            return
        }
        lastBlockTimestamps[pkg] = nowElapsed

        if (captureLogs) {
            logEventThrottled(
                pkg,
                eventType,
                eventClassName,
                rootNode,
                matchedSelector,
                "TRIGGERED:${rule.interventionMode.name}",
                nowElapsed,
                force = true
            )
        }

        // Update selector health without false-positiving on a single pre-inflation frame
        val lastHealth = lastHealthTimestamps[pkg] ?: 0L
        if (nowElapsed - lastHealth >= 500L) {
            lastHealthTimestamps[pkg] = nowElapsed
            val viewIdsMatchedCount = SelectorMatcher.countVisibleViewIdMatches(rootNode, rule)
            val isTabFallbackOnly = matchedSelector.startsWith("tab:") && viewIdsMatchedCount == 0
            val missStreak = if (isTabFallbackOnly) {
                (consecutiveTabFallbackCounts[pkg] ?: 0) + 1
            } else {
                0
            }
            consecutiveTabFallbackCounts[pkg] = missStreak
            val shouldMarkOutdated = missStreak >= 5

            serviceScope.launch {
                app.interventionRepository.updateSelectorHealth(
                    packageName = rule.packageName,
                    matchedCount = viewIdsMatchedCount,
                    totalCount = rule.viewIds.size.coerceAtLeast(1),
                    outdated = shouldMarkOutdated
                )
                if (shouldMarkOutdated) {
                    app.rulesetRepository.autoSyncIfDue(force = false)
                }
            }
        }

        executeIntervention(rootNode, rule, matchedSelector)
    }

    private fun logEventThrottled(
        pkg: String,
        eventType: Int,
        eventClassName: String?,
        rootNode: AccessibilityNodeInfo?,
        matchedSelector: String?,
        outcome: String,
        nowElapsed: Long,
        force: Boolean = false
    ) {
        val lastLogged = lastLogTimestamps[pkg] ?: 0L
        if (!force && nowElapsed - lastLogged < 400L) return
        lastLogTimestamps[pkg] = nowElapsed
        serviceScope.launch {
            app.eventLogger.logEvent(
                packageName = pkg,
                eventType = eventType,
                eventClassName = eventClassName,
                rootNode = rootNode,
                matchedSelector = matchedSelector,
                outcome = outcome
            )
        }
    }

    private fun resolveTargetRootNode(pkg: String): AccessibilityNodeInfo? {
        val active = rootInActiveWindow
        if (active?.packageName?.toString() == pkg) {
            return active
        }
        val windowRoot = try {
            windows?.firstOrNull { it.root?.packageName?.toString() == pkg }?.root
        } catch (_: Exception) {
            null
        }
        return windowRoot ?: active
    }

    private fun executeIntervention(
        rootNode: AccessibilityNodeInfo,
        rule: TargetAppRuleEntity,
        matchedSelector: String
    ) {
        when (rule.interventionMode) {
            InterventionMode.BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }

            InterventionMode.HOME_TAB -> {
                val homeNode = SelectorMatcher.findHomeTabNode(rootNode, rule.homeTabLabel)
                val clicked = homeNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
                if (!clicked) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
            }

            InterventionMode.OVERLAY -> {
                overlayShield?.show(
                    appName = rule.displayName,
                    feedLabel = rule.feedLabel,
                    onExitFeed = {
                        lastBlockTimestamps[rule.packageName] = SystemClock.elapsedRealtime()
                        val freshRoot = resolveTargetRootNode(rule.packageName)
                        val clickedHome = freshRoot?.let {
                            SelectorMatcher.findHomeTabNode(it, rule.homeTabLabel)
                                ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        } == true
                        if (!clickedHome) {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }
                    },
                    onAllowOnce = {
                        lastBlockTimestamps[rule.packageName] = SystemClock.elapsedRealtime()
                        serviceScope.launch {
                            app.interventionRepository.allowOnceTemporary(rule.packageName)
                        }
                    }
                )
            }
        }

        serviceScope.launch {
            app.interventionRepository.recordIntervention(
                rule = rule,
                matchedSelector = matchedSelector,
                actionTaken = rule.interventionMode
            )
        }
    }

    override fun onInterrupt() {
        overlayShield?.dismiss()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        overlayShield?.dismiss()
        DiagnosticsBridge.setServiceConnected(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayShield?.dismiss()
        DiagnosticsBridge.setServiceConnected(false)
        serviceScope.cancel()
    }
}
