package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.local.AppPreferences
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.model.InterventionMode
import com.example.data.model.ProtectionStatus
import com.example.ui.DayBarStat
import com.example.ui.MainUiState
import com.example.ui.components.BottomTab
import com.example.ui.components.FloatingPillNavBar
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.ObsidianSideRail
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FeedSurgeonTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FeedSurgeonUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `onboarding shows diagnostic disclosure note, installed grouping, and step2 recovery`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        val installed = setOf("com.google.android.youtube")

        composeRule.setContent {
            FeedSurgeonTheme {
                OnboardingScreen(
                    rules = rules,
                    installedPackages = installed,
                    isAccessibilityEnabled = false,
                    onToggleApp = { _, _ -> },
                    onAcceptDisclosure = {},
                    onDeclineDisclosure = {},
                    onCompleteOnboarding = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_diagnostic_disclosure_note")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_decline_note")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_installed_section").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_not_installed_section")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_apps_continue_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("open_accessibility_settings_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_step2_recovery_card")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `home screen renders disconnected and no-apps-active cards distinctly`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        val installed = setOf("com.google.android.youtube")

        composeRule.setContent {
            FeedSurgeonTheme {
                HomeScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        rules = rules,
                        installedPackages = installed,
                        isAccessibilityEnabled = true,
                        isServiceConnected = false,
                        protectionStatus = ProtectionStatus.SERVICE_DISCONNECTED,
                        activeInstalledCount = 1,
                        installedSupportedCount = 1
                    ),
                    onStatusClick = {},
                    onSetupClick = {},
                    onToggleGlobalPause = {},
                    onPauseForMinutes = {},
                    onGlobalBreakExpired = {},
                    onAppSnoozeExpired = {},
                    onRefreshBlockersClick = {},
                    onAppClick = {},
                    onToggleApp = { _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_service_disconnected_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_reconnect_service_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_protection_control_card").assertDoesNotExist()
    }

    @Test
    fun `home screen renders no apps active card when connected with zero active installed apps`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        val installed = setOf("com.google.android.youtube")

        composeRule.setContent {
            FeedSurgeonTheme {
                HomeScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        rules = rules.map { it.copy(enabled = false) },
                        installedPackages = installed,
                        isAccessibilityEnabled = true,
                        isServiceConnected = true,
                        protectionStatus = ProtectionStatus.NO_APPS_ACTIVE,
                        activeInstalledCount = 0,
                        installedSupportedCount = 1
                    ),
                    onStatusClick = {},
                    onSetupClick = {},
                    onToggleGlobalPause = {},
                    onPauseForMinutes = {},
                    onGlobalBreakExpired = {},
                    onAppSnoozeExpired = {},
                    onRefreshBlockersClick = {},
                    onAppClick = {},
                    onToggleApp = { _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_no_apps_active_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_protection_control_card").assertDoesNotExist()
    }

    @Test
    fun `home screen renders operational break controls and resume button when paused`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        val installed = setOf("com.google.android.youtube")
        val pausedUntil = System.currentTimeMillis() + 5 * 60_000L

        composeRule.setContent {
            FeedSurgeonTheme {
                HomeScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        rules = rules,
                        installedPackages = installed,
                        isAccessibilityEnabled = true,
                        isServiceConnected = true,
                        protectionStatus = ProtectionStatus.PAUSED,
                        activeInstalledCount = 1,
                        installedSupportedCount = 1,
                        isGloballyPaused = true,
                        globalPauseUntilMs = pausedUntil
                    ),
                    onStatusClick = {},
                    onSetupClick = {},
                    onToggleGlobalPause = {},
                    onPauseForMinutes = {},
                    onGlobalBreakExpired = {},
                    onAppSnoozeExpired = {},
                    onRefreshBlockersClick = {},
                    onAppClick = {},
                    onToggleApp = { _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_protection_control_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_resume_protection_button").assertIsDisplayed()
    }

    @Test
    fun `app detail screen displays blocker health and updates speed preset`() {
        val ytRule = FeedSurgeonDatabase.defaultBuiltInRules().first {
            it.packageName == "com.google.android.youtube"
        }
        var updatedCooldown = -1
        var updatedMode: InterventionMode? = null

        composeRule.setContent {
            FeedSurgeonTheme {
                AppDetailScreen(
                    rule = ytRule,
                    onBack = {},
                    onToggleEnabled = {},
                    onSelectMode = { updatedMode = it },
                    onChangeCooldown = { updatedCooldown = it },
                    onSnoozeMinutes = {},
                    onResumeSnooze = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("detail_blocker_health").assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("mode_back"))
        composeRule.onNodeWithTag("mode_back").performClick()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("speed_preset_instant"))
        composeRule.onNodeWithTag("speed_preset_instant").performClick()
        composeRule.waitForIdle()

        assertEquals(500, updatedCooldown)
        assertEquals(InterventionMode.BACK, updatedMode)
    }

    @Test
    fun `insights screen displays estimate note and updates selected day summary on bar tap`() {
        val dayBars = listOf(
            DayBarStat("M", "Monday", "Nov 12", count = 4, reclaimedMinutes = 11, fraction = 0.8f, isToday = false),
            DayBarStat("T", "Tuesday", "Nov 13", count = 2, reclaimedMinutes = 5, fraction = 0.5f, isToday = false),
            DayBarStat("W", "Wednesday", "Nov 14", count = 0, reclaimedMinutes = 0, fraction = 0.14f, isToday = false),
            DayBarStat("T", "Thursday", "Nov 15", count = 1, reclaimedMinutes = 2, fraction = 0.35f, isToday = false),
            DayBarStat("F", "Friday", "Nov 16", count = 3, reclaimedMinutes = 8, fraction = 0.65f, isToday = false),
            DayBarStat("S", "Saturday", "Nov 17", count = 5, reclaimedMinutes = 13, fraction = 1f, isToday = false),
            DayBarStat("S", "Sunday", "Nov 18", count = 2, reclaimedMinutes = 5, fraction = 0.5f, isToday = true)
        )

        composeRule.setContent {
            FeedSurgeonTheme {
                InsightsScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        preferences = AppPreferences(reclaimedSecondsPerCut = 180),
                        weeklyCutsCount = 17,
                        weeklyDayBars = dayBars,
                        weeklyReclaimedText = "~51 min",
                        streakDays = 3
                    )
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("weekly_bar_0").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("selected_day_summary").assertTextContains("Monday", substring = true)

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("insights_estimate_note"))
        composeRule.onNodeWithTag("insights_estimate_note").assertIsDisplayed()
    }

    @Test
    fun `settings screen shows disconnected status and expands advanced diagnostics`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        composeRule.setContent {
            FeedSurgeonTheme {
                SettingsScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        rules = rules,
                        installedPackages = setOf("com.google.android.youtube"),
                        isAccessibilityEnabled = true,
                        isServiceConnected = false,
                        protectionStatus = ProtectionStatus.SERVICE_DISCONNECTED,
                        activeInstalledCount = 1,
                        installedSupportedCount = 1
                    ),
                    onReopenDisclosure = {},
                    onToggleGlobalPause = {},
                    onSetAutoUpdateRules = {},
                    onCheckRulesNow = {},
                    onSetEventCaptureEnabled = {},
                    onRefreshEventLog = {},
                    onClearEventLog = {},
                    onShareEventLog = {},
                    onSetReclaimedSeconds = {},
                    onSetShowBanner = {},
                    onClearHistory = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings_protection_pill").assertTextContains("Disconnected")
        composeRule.onNodeWithTag("settings_global_pause_switch").assertDoesNotExist()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("settings_advanced_toggle_card"))
        composeRule.onNodeWithTag("settings_advanced_toggle_card").performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("settings_check_updates_button"))
        composeRule.onNodeWithTag("settings_check_updates_button").assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("view_event_log_button"))
        composeRule.onNodeWithTag("view_event_log_button").assertIsDisplayed()
    }

    @Test
    fun `compact floating dock always shows all labels, sliding pill, and contextual alert dots`() {
        var selected = BottomTab.HOME
        composeRule.setContent {
            FeedSurgeonTheme {
                FloatingPillNavBar(
                    selectedTab = selected,
                    onTabSelected = { selected = it },
                    alertTabs = setOf(BottomTab.HOME, BottomTab.SETTINGS)
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("compact_floating_dock").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_sliding_pill").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Insights").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_alert_home", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("nav_alert_settings", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("nav_alert_insights", useUnmergedTree = true).assertDoesNotExist()

        composeRule.onNodeWithTag("nav_tab_insights").performClick()
        assertEquals(BottomTab.INSIGHTS, selected)
    }

    @Test
    fun `obsidian side rail displays labels, sliding pill, and alert indicators`() {
        var selected = BottomTab.INSIGHTS
        composeRule.setContent {
            FeedSurgeonTheme {
                ObsidianSideRail(
                    selectedTab = selected,
                    onTabSelected = { selected = it },
                    alertTabs = setOf(BottomTab.SETTINGS)
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("obsidian_side_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("rail_sliding_pill").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Insights").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_alert_settings", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("nav_alert_home", useUnmergedTree = true).assertDoesNotExist()

        composeRule.onNodeWithTag("nav_tab_settings").performClick()
        assertEquals(BottomTab.SETTINGS, selected)
    }

    @Test
    fun `computeNavAlertTabs flags home and settings only when attention is needed`() {
        val healthy = computeNavAlertTabs(
            MainUiState(
                protectionStatus = ProtectionStatus.OPERATIONAL,
                staleRulesCount = 0,
                isBatteryOptimizationIgnored = true,
                isNotificationPermissionGranted = true
            )
        )
        assertEquals(emptySet<BottomTab>(), healthy)

        val attention = computeNavAlertTabs(
            MainUiState(
                protectionStatus = ProtectionStatus.SERVICE_DISCONNECTED,
                staleRulesCount = 1,
                isBatteryOptimizationIgnored = true,
                isNotificationPermissionGranted = true
            )
        )
        assertEquals(setOf(BottomTab.HOME, BottomTab.SETTINGS), attention)
    }

    @Test
    fun `home screen renders hero protection ring gauge and animated today cuts count`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        val installed = setOf("com.google.android.youtube")

        composeRule.setContent {
            FeedSurgeonTheme {
                HomeScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        rules = rules,
                        installedPackages = installed,
                        isAccessibilityEnabled = true,
                        isServiceConnected = true,
                        protectionStatus = ProtectionStatus.OPERATIONAL,
                        activeInstalledCount = 1,
                        installedSupportedCount = 1,
                        todayCutsCount = 7,
                        todayReclaimedMinutes = 21
                    ),
                    onStatusClick = {},
                    onSetupClick = {},
                    onToggleGlobalPause = {},
                    onPauseForMinutes = {},
                    onGlobalBreakExpired = {},
                    onAppSnoozeExpired = {},
                    onRefreshBlockersClick = {},
                    onAppClick = {},
                    onToggleApp = { _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("today_cuts_count")
            .assertIsDisplayed()
            .assertTextContains("7")
    }

    @Test
    @Config(sdk = [36], qualifiers = "w900dp-h900dp")
    fun `home screen renders two-column mosaic grid at wide tablet width`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        val installed = setOf("com.google.android.youtube", "com.instagram.android")

        composeRule.setContent {
            FeedSurgeonTheme {
                HomeScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        rules = rules,
                        installedPackages = installed,
                        isAccessibilityEnabled = true,
                        isServiceConnected = true,
                        protectionStatus = ProtectionStatus.OPERATIONAL,
                        activeInstalledCount = 2,
                        installedSupportedCount = 2,
                        todayCutsCount = 4
                    ),
                    onStatusClick = {},
                    onSetupClick = {},
                    onToggleGlobalPause = {},
                    onPauseForMinutes = {},
                    onGlobalBreakExpired = {},
                    onAppSnoozeExpired = {},
                    onRefreshBlockersClick = {},
                    onAppClick = {},
                    onToggleApp = { _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_mosaic_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("home_protection_control_card").assertIsDisplayed()
        composeRule.onNodeWithTag("today_cuts_count").assertIsDisplayed()
        composeRule.onNodeWithTag("home_apps_mosaic_row_0").assertIsDisplayed()
    }

    @Test
    fun `mindful pause preview dialog displays liquid breathing orb and actions`() {
        composeRule.setContent {
            FeedSurgeonTheme {
                MindfulPausePreviewDialog(
                    appName = "YouTube",
                    feedLabel = "Shorts",
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("mindful_pause_preview_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("liquid_breathing_orb").assertIsDisplayed()
        composeRule.onNodeWithTag("preview_leave_feed_button").assertIsDisplayed()
        composeRule.onNodeWithTag("preview_allow_5m_button").assertIsDisplayed()
    }
}
