package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.FeedSurgeonApp
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.data.repository.DiagnosticsBridge
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

    @Volatile
    private var activeRulesByPackage: Map<String, TargetAppRuleEntity> = emptyMap()

    private var overlayShield: AccessibilityOverlayShield? = null

    private val app: FeedSurgeonApp
        get() = applicationContext as FeedSurgeonApp

    override fun onServiceConnected() {
        super.onServiceConnected()
        DiagnosticsBridge.setServiceConnected(true)
        overlayShield = AccessibilityOverlayShield(this)

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
    }

    private fun updateServicePackageScope(packages: List<String>) {
        val currentInfo = serviceInfo ?: AccessibilityServiceInfo()
        val scopedPackages = packages.distinct().ifEmpty {
            listOf(
                "com.google.android.youtube",
                "com.instagram.android",
                "com.facebook.katana",
                "com.snapchat.android",
                "com.reddit.frontpage"
            )
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
        val rule = activeRulesByPackage[pkg] ?: return
        if (!rule.enabled) return

        val rootNode = rootInActiveWindow ?: return
        val nowElapsed = SystemClock.elapsedRealtime()

        val matchedSelector = SelectorMatcher.findMatchingSelector(rootNode, rule)

        // Update selector health automatically: if only tab fallback matched while View IDs missed,
        // mark outdated so background rule updates know the host app layout shifted.
        if (matchedSelector != null) {
            val lastHealth = lastHealthTimestamps[pkg] ?: 0L
            if (nowElapsed - lastHealth >= 500L) {
                lastHealthTimestamps[pkg] = nowElapsed
                val viewIdsMatchedCount = rule.viewIds.count { id ->
                    rootNode.findAccessibilityNodeInfosByViewId(id).isNotEmpty()
                }
                val isTabFallbackOnly = matchedSelector.startsWith("tab:") && viewIdsMatchedCount == 0
                serviceScope.launch {
                    app.interventionRepository.updateSelectorHealth(
                        packageName = rule.packageName,
                        matchedCount = viewIdsMatchedCount,
                        totalCount = rule.viewIds.size.coerceAtLeast(1),
                        outdated = isTabFallbackOnly
                    )
                    if (isTabFallbackOnly) {
                        app.rulesetRepository.autoSyncIfDue(force = false)
                    }
                }
            }
        }

        if (matchedSelector == null) {
            return
        }

        if (rule.isCurrentlySnoozed(System.currentTimeMillis())) {
            return
        }

        val lastBlock = lastBlockTimestamps[pkg] ?: 0L
        if (nowElapsed - lastBlock < rule.cooldownMs) {
            return
        }
        lastBlockTimestamps[pkg] = nowElapsed

        executeIntervention(rootNode, rule, matchedSelector)
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
                        val freshRoot = rootInActiveWindow
                        val clickedHome = freshRoot?.let {
                            SelectorMatcher.findHomeTabNode(it, rule.homeTabLabel)
                                ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        } == true
                        if (!clickedHome) {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        }
                    },
                    onAllowOnce = {
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

    override fun onDestroy() {
        super.onDestroy()
        overlayShield?.dismiss()
        DiagnosticsBridge.setServiceConnected(false)
        serviceScope.cancel()
    }
}
