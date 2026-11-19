package com.example

import android.content.ComponentName
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.down
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppPreferences
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.model.InterventionMode
import com.example.data.model.ProtectionStatus
import com.example.data.model.ThemeMode
import com.example.ui.DayBarStat
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.OnboardingStep
import com.example.ui.WeeklyFunnelStats
import com.example.ui.components.BottomTab
import com.example.ui.components.LiquidShieldOverlay
import com.example.ui.components.MindfulPausePreviewDialog
import com.example.ui.components.StillspaceBottomNavBar
import com.example.ui.components.StillspaceNavigationRail
import com.example.ui.components.SurgeonCard
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FeedSurgeonTheme
import com.example.ui.theme.HabitBg
import com.example.ui.theme.HabitSurface
import com.example.ui.theme.HabitSurfaceHigh
import com.example.ui.theme.LightSemanticColors
import com.example.ui.theme.StillspaceTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FeedSurgeonUiRobolectricTest {

    @Before
    fun cancelBackgroundStartupSync() {
        val app = ApplicationProvider.getApplicationContext<android.content.Context>() as? FeedSurgeonApp
        app?.initializationJob?.cancel()
    }

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
        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("home_reconnect_service_button"))
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
        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("home_resume_protection_button"))
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
    fun `settings screen hides dark theme switcher when light-only theme is enforced`() {
        composeRule.setContent {
            FeedSurgeonTheme(themeMode = ThemeMode.DARK) {
                SettingsScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        preferences = AppPreferences(themeMode = ThemeMode.DARK),
                        rules = FeedSurgeonDatabase.defaultBuiltInRules(),
                        installedPackages = setOf("com.google.android.youtube"),
                        isAccessibilityEnabled = true,
                        isServiceConnected = true,
                        protectionStatus = ProtectionStatus.OPERATIONAL,
                        activeInstalledCount = 1,
                        installedSupportedCount = 1
                    ),
                    onReopenDisclosure = {},
                    onToggleGlobalPause = {},
                    onSetThemeMode = {},
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

        composeRule.onNodeWithTag("settings_appearance_card").assertDoesNotExist()
        composeRule.onNodeWithTag("theme_mode_dark").assertDoesNotExist()
    }

    @Test
    fun `m3 bottom nav bar always shows all labels and contextual alert badges`() {
        var selected = BottomTab.HOME
        composeRule.setContent {
            FeedSurgeonTheme {
                StillspaceBottomNavBar(
                    selectedTab = selected,
                    onTabSelected = { selected = it },
                    alertTabs = setOf(BottomTab.HOME, BottomTab.SETTINGS)
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("stillspace_bottom_nav").assertIsDisplayed()
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
    fun `m3 side navigation rail displays labels and alert indicators`() {
        var selected = BottomTab.INSIGHTS
        composeRule.setContent {
            FeedSurgeonTheme {
                StillspaceNavigationRail(
                    selectedTab = selected,
                    onTabSelected = { selected = it },
                    alertTabs = setOf(BottomTab.SETTINGS)
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("stillspace_side_rail").assertIsDisplayed()
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
    fun `mindful pause preview dialog displays glassomorphic orb, inline actions, and expandable intention tray`() {
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
        composeRule.onNodeWithTag("liquid_shield_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("liquid_breathing_orb").assertIsDisplayed()

        val returnNode = composeRule.onNodeWithTag("preview_leave_feed_button")
        val intendedNode = composeRule.onNodeWithTag("preview_allow_5m_button")
        returnNode.assertIsDisplayed().assertTextContains("Return")
        intendedNode.assertIsDisplayed().assertTextContains("Intended Visit")

        val returnBounds = returnNode.getBoundsInRoot()
        val intendedBounds = intendedNode.getBoundsInRoot()
        assertEquals(returnBounds.top.value, intendedBounds.top.value, 2f)
        assertTrue(returnBounds.right < intendedBounds.left)

        composeRule.onNodeWithTag("preview_intention_tray").assertDoesNotExist()
        intendedNode.performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("preview_intention_tray").assertIsDisplayed()
        composeRule.onNodeWithTag("preview_intention_chip_dm").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("preview_intention_chip_saved").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("preview_intention_input")
            .performScrollTo()
            .assertIsDisplayed()
            .performTextInput("Check reply from Alex")
        composeRule.onNodeWithTag("preview_intention_submit_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("preview_demo_outcome")
            .performScrollTo()
            .assertTextContains("Check reply from Alex", substring = true)
    }

    @Test
    fun `liquid shield overlay draws central breathing orb canvas and child content`() {
        composeRule.setContent {
            FeedSurgeonTheme {
                LiquidShieldOverlay {
                    Text("Shield Active")
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("liquid_shield_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("liquid_shield_orb_canvas").assertIsDisplayed()
        composeRule.onNodeWithText("Shield Active").assertIsDisplayed()
    }

    @Test
    fun `dashboard card pointerInput tactile press physics preserves click handling`() {
        var clicks = 0
        composeRule.setContent {
            FeedSurgeonTheme {
                SurgeonCard(
                    modifier = Modifier.testTag("tactile_dashboard_card"),
                    onClick = { clicks++ }
                ) {
                    Text("Dashboard Card")
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tactile_dashboard_card").performTouchInput {
            down(center)
            advanceEventTime(80L)
            up()
        }
        composeRule.waitForIdle()

        assertEquals(1, clicks)
    }

    @Test
    fun `welcome screen displays hero illustration and choose apps displays official app logos`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        composeRule.setContent {
            FeedSurgeonTheme {
                OnboardingScreen(
                    rules = rules,
                    installedPackages = setOf(
                        "com.google.android.youtube",
                        "com.instagram.android",
                        "com.facebook.katana"
                    ),
                    isAccessibilityEnabled = false,
                    onToggleApp = { _, _ -> },
                    onAcceptDisclosure = {},
                    onDeclineDisclosure = {},
                    onCompleteOnboarding = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_hero_illustration").assertIsDisplayed()
        composeRule.onNodeWithText("Reels paused").assertIsDisplayed()
        composeRule.onNodeWithText("Shorts paused").assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("app_logo_com.google.android.youtube", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("app_logo_com.instagram.android", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("app_logo_com.facebook.katana", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp")
    fun `onboarding welcome features, consent line, and step3 and step4 mockup buttons align cleanly`() {
        val installed = setOf("com.google.android.youtube")
        val rules = FeedSurgeonDatabase.defaultBuiltInRules(installed)
        val accessibilityEnabled = androidx.compose.runtime.mutableStateOf(false)
        composeRule.setContent {
            FeedSurgeonTheme {
                OnboardingScreen(
                    rules = rules,
                    installedPackages = installed,
                    isAccessibilityEnabled = accessibilityEnabled.value,
                    onToggleApp = { _, _ -> },
                    onAcceptDisclosure = {},
                    onDeclineDisclosure = {},
                    onCompleteOnboarding = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Step 0: Hero illustration, title, privacy cards, consent line, and split buttons
        composeRule.onNodeWithTag("onboarding_hero_illustration").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_hero_title").assertIsDisplayed()
        composeRule.onNodeWithText("What it checks").assertIsDisplayed()
        composeRule.onNodeWithText("What stays private").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_privacy_consent_line").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_privacy_link").assertIsDisplayed()

        val declineBounds = composeRule.onNodeWithTag("onboarding_decline_button").getBoundsInRoot()
        val agreeBounds = composeRule.onNodeWithTag("onboarding_agree_button").getBoundsInRoot()
        assertEquals(declineBounds.top.value, agreeBounds.top.value, 1f)
        assertTrue(
            "Expected Get started (${agreeBounds.right - agreeBounds.left}) wider than Not now (${declineBounds.right - declineBounds.left})",
            (agreeBounds.right - agreeBounds.left) > (declineBounds.right - declineBounds.left)
        )

        // Advance to Step 1 -> Verify installed ON, not-installed OFF by default
        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_app_toggle_com.google.android.youtube").assertIsOn()
        composeRule.onNodeWithTag("onboarding_app_toggle_com.instagram.android").performScrollTo().assertIsOff()

        // Advance to Step 2 (3rd onboarding screen)
        composeRule.onNodeWithTag("onboarding_apps_continue_button").performClick()
        composeRule.waitForIdle()

        val mutedContinueBounds = composeRule.onNodeWithTag("onboarding_continue_step2").getBoundsInRoot()
        val openSettingsBoundsOff = composeRule.onNodeWithTag("open_accessibility_settings_button").getBoundsInRoot()
        assertEquals(mutedContinueBounds.top.value, openSettingsBoundsOff.top.value, 1f)
        assertTrue(
            "Expected Open Settings on left and wider than muted Continue while Accessibility is off",
            openSettingsBoundsOff.left < mutedContinueBounds.left &&
                (openSettingsBoundsOff.right - openSettingsBoundsOff.left) > (mutedContinueBounds.right - mutedContinueBounds.left)
        )
        composeRule.onNodeWithTag("onboarding_continue_step2").assertIsNotEnabled()

        // Enable Accessibility -> Continue becomes clickable and takes wider primary emphasis
        accessibilityEnabled.value = true
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_continue_step2").assertIsEnabled()
        val activeContinueBounds = composeRule.onNodeWithTag("onboarding_continue_step2").getBoundsInRoot()
        val openSettingsBoundsOn = composeRule.onNodeWithTag("open_accessibility_settings_button").getBoundsInRoot()
        assertTrue(
            "Expected active Continue wider than Open Settings once Focus Guard is enabled",
            (activeContinueBounds.right - activeContinueBounds.left) > (openSettingsBoundsOn.right - openSettingsBoundsOn.left)
        )

        // Advance to Step 3 (4th onboarding screen) — Preview removed, single Go to Home button
        composeRule.onNodeWithTag("onboarding_continue_step2").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_try_preview_button").assertDoesNotExist()
        composeRule.onNodeWithTag("onboarding_finish_button").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp")
    fun `shared viewmodel manages onboarding state across all four steps with slide transitions`() {
        val app = ApplicationProvider.getApplicationContext<FeedSurgeonApp>()
        Settings.Secure.putString(app.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "")
        val sharedViewModel = MainViewModel(app)
        sharedViewModel.refreshAccessibilityStatus(app)
        var completedNavigation = false

        composeRule.setContent {
            FeedSurgeonTheme {
                OnboardingScreen(
                    viewModel = sharedViewModel,
                    onCompleteNavigation = { completedNavigation = true }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_responsive_scaffold").assertIsDisplayed()
        assertEquals(OnboardingStep.WELCOME, sharedViewModel.uiState.value.onboardingState.currentStep)

        // Step 0 -> Step 1 via shared ViewModel
        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()
        assertEquals(OnboardingStep.TARGET_APPS, sharedViewModel.uiState.value.onboardingState.currentStep)

        // Navigate back to Step 0, then forward again to verify bidirectional transitions
        sharedViewModel.previousOnboardingStep()
        composeRule.waitForIdle()
        assertEquals(OnboardingStep.WELCOME, sharedViewModel.uiState.value.onboardingState.currentStep)
        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()

        // Step 1 -> Step 2
        composeRule.onNodeWithTag("onboarding_apps_continue_button").performClick()
        composeRule.waitForIdle()
        assertEquals(OnboardingStep.ACCESSIBILITY, sharedViewModel.uiState.value.onboardingState.currentStep)

        composeRule.onNodeWithTag("open_accessibility_settings_button").performClick()
        composeRule.waitForIdle()
        assertTrue(sharedViewModel.uiState.value.onboardingState.openedSystemSettings)
        composeRule.onNodeWithTag("onboarding_continue_step2").assertIsNotEnabled()

        // Simulate enabling Accessibility Service in Android Settings & resuming
        val component = ComponentName(app, com.example.service.FeedSurgeonService::class.java).flattenToString()
        Settings.Secure.putString(app.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, component)
        sharedViewModel.refreshAccessibilityStatus(app)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_continue_step2").assertIsEnabled()

        // Step 2 -> Step 3
        composeRule.onNodeWithTag("onboarding_continue_step2").performClick()
        composeRule.waitForIdle()
        assertEquals(OnboardingStep.READY, sharedViewModel.uiState.value.onboardingState.currentStep)
        composeRule.onNodeWithTag("onboarding_try_preview_button").assertDoesNotExist()

        // Complete onboarding resets step cleanly and triggers navigation callback
        composeRule.onNodeWithTag("onboarding_finish_button").performClick()
        composeRule.waitForIdle()
        assertTrue(completedNavigation)
        assertEquals(OnboardingStep.WELCOME, sharedViewModel.uiState.value.onboardingState.currentStep)
        Settings.Secure.putString(app.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "")
    }

    @Test
    @Config(sdk = [36], qualifiers = "w900dp-h900dp")
    fun `onboarding scaffold switches to two-pane responsive layout on wide tablet width`() {
        val installed = setOf("com.google.android.youtube", "com.instagram.android")
        val rules = FeedSurgeonDatabase.defaultBuiltInRules(installed)
        val accessibilityEnabled = androidx.compose.runtime.mutableStateOf(false)
        composeRule.setContent {
            FeedSurgeonTheme {
                OnboardingScreen(
                    rules = rules,
                    installedPackages = installed,
                    isAccessibilityEnabled = accessibilityEnabled.value,
                    onToggleApp = { _, _ -> },
                    onAcceptDisclosure = {},
                    onDeclineDisclosure = {},
                    onCompleteOnboarding = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_wide_step_badge").assertTextContains("STEP 1 OF 4", substring = true)
        composeRule.onNodeWithTag("onboarding_welcome_wide_pane").assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_wide_step_badge").assertTextContains("STEP 2 OF 4", substring = true)
        composeRule.onNodeWithTag("onboarding_apps_wide_grid").assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_apps_continue_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_wide_step_badge").assertTextContains("STEP 3 OF 4", substring = true)
        composeRule.onNodeWithTag("onboarding_accessibility_wide_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_continue_step2").assertIsNotEnabled()

        accessibilityEnabled.value = true
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_continue_step2").assertIsEnabled().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_wide_step_badge").assertTextContains("STEP 4 OF 4", substring = true)
        composeRule.onNodeWithTag("onboarding_ready_wide_pane").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w360dp-h800dp")
    fun `compact width split buttons remain aligned and single-line without wrapping`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        composeRule.setContent {
            FeedSurgeonTheme {
                OnboardingScreen(
                    rules = rules,
                    installedPackages = setOf("com.google.android.youtube"),
                    isAccessibilityEnabled = false,
                    onToggleApp = { _, _ -> },
                    onAcceptDisclosure = {},
                    onDeclineDisclosure = {},
                    onCompleteOnboarding = {}
                )
            }
        }
        composeRule.waitForIdle()

        val notNowBounds = composeRule.onNodeWithTag("onboarding_decline_button").getBoundsInRoot()
        val getStartedBounds = composeRule.onNodeWithTag("onboarding_agree_button").getBoundsInRoot()
        assertEquals(notNowBounds.top.value, getStartedBounds.top.value, 1f)
        assertTrue((notNowBounds.bottom - notNowBounds.top).value <= 56f)
        assertTrue((getStartedBounds.bottom - getStartedBounds.top).value <= 56f)

        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_apps_continue_button").performClick()
        composeRule.waitForIdle()

        val openSettingsBounds = composeRule.onNodeWithTag("open_accessibility_settings_button").getBoundsInRoot()
        val skipBounds = composeRule.onNodeWithTag("onboarding_continue_step2").getBoundsInRoot()
        assertEquals(openSettingsBounds.top.value, skipBounds.top.value, 1f)
        assertTrue((openSettingsBounds.bottom - openSettingsBounds.top).value <= 56f)
        assertTrue((skipBounds.bottom - skipBounds.top).value <= 56f)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w360dp-h800dp")
    fun `settings screen renders cleanly in dark blue mode with single-line protection buttons`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        composeRule.setContent {
            FeedSurgeonTheme(themeMode = ThemeMode.DARK) {
                SettingsScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        preferences = AppPreferences(themeMode = ThemeMode.DARK),
                        rules = rules,
                        installedPackages = setOf("com.google.android.youtube"),
                        isAccessibilityEnabled = true,
                        isServiceConnected = true,
                        protectionStatus = ProtectionStatus.OPERATIONAL,
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

        composeRule.onNodeWithTag("settings_protection_pill").assertIsDisplayed()
        composeRule.onNodeWithText("Open Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Guided setup").assertIsDisplayed()

        val openBounds = composeRule.onNodeWithText("Open Settings").getBoundsInRoot()
        val guidedBounds = composeRule.onNodeWithText("Guided setup").getBoundsInRoot()
        assertTrue((openBounds.bottom - openBounds.top).value <= 56f)
        assertTrue((guidedBounds.bottom - guidedBounds.top).value <= 56f)
    }

    @Test
    fun `light theme enforces Quiet Intent warm ivory background and white porcelain cards`() {
        var lightBg: Color? = null
        var lightSurface: Color? = null
        var lightSurfaceVariant: Color? = null
        var lightCardSurface: Color? = null
        var lightSurfaceHigh: Color? = null
        var lightDock: Color? = null
        var darkRequestedBg: Color? = null
        var darkRequestedSurface: Color? = null

        composeRule.setContent {
            FeedSurgeonTheme(themeMode = ThemeMode.LIGHT) {
                lightBg = MaterialTheme.colorScheme.background
                lightSurface = MaterialTheme.colorScheme.surface
                lightSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
                lightCardSurface = MaterialTheme.colorScheme.surfaceContainer
                lightSurfaceHigh = MaterialTheme.colorScheme.surfaceContainerHigh
                lightDock = StillspaceTheme.colors.dockSurface
            }
            FeedSurgeonTheme(themeMode = ThemeMode.DARK) {
                darkRequestedBg = MaterialTheme.colorScheme.background
                darkRequestedSurface = MaterialTheme.colorScheme.surfaceContainer
            }
        }
        composeRule.waitForIdle()

        assertEquals(HabitBg, lightBg)
        assertEquals(Color(0xFFFAF9F5), lightBg)
        assertEquals(HabitBg, lightSurface)
        assertEquals(HabitSurface, lightSurfaceVariant)
        assertEquals(Color(0xFFFFFFFF), lightSurfaceVariant)
        assertEquals(Color(0xFFFFFFFF), lightCardSurface)
        assertEquals(HabitSurfaceHigh, lightSurfaceHigh)
        assertEquals(LightSemanticColors.dockSurface, lightDock)
        assertEquals(Color(0xFFFAF9F5), darkRequestedBg)
        assertEquals(Color(0xFFFFFFFF), darkRequestedSurface)
    }

    @Test
    fun `insights screen renders activity funnel card with all four verifiable stages`() {
        val dayBars = listOf(
            DayBarStat("M", "Monday", "Nov 12", count = 3, reclaimedMinutes = 8, fraction = 0.7f, isToday = false),
            DayBarStat("T", "Tuesday", "Nov 13", count = 2, reclaimedMinutes = 5, fraction = 0.5f, isToday = false),
            DayBarStat("W", "Wednesday", "Nov 14", count = 0, reclaimedMinutes = 0, fraction = 0.14f, isToday = false),
            DayBarStat("T", "Thursday", "Nov 15", count = 0, reclaimedMinutes = 0, fraction = 0.14f, isToday = false),
            DayBarStat("F", "Friday", "Nov 16", count = 1, reclaimedMinutes = 3, fraction = 0.35f, isToday = false),
            DayBarStat("S", "Saturday", "Nov 17", count = 1, reclaimedMinutes = 3, fraction = 0.35f, isToday = false),
            DayBarStat("S", "Sunday", "Nov 18", count = 0, reclaimedMinutes = 0, fraction = 0.14f, isToday = true)
        )

        composeRule.setContent {
            FeedSurgeonTheme {
                InsightsScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        preferences = AppPreferences(reclaimedSecondsPerCut = 180),
                        weeklyCutsCount = 7,
                        weeklyDayBars = dayBars,
                        weeklyReclaimedText = "~21 min",
                        weeklyAllowedOnceCount = 3,
                        weeklyFunnel = WeeklyFunnelStats(
                            detectedCount = 12,
                            attemptedCount = 10,
                            completedCount = 7,
                            continuedCount = 3
                        ),
                        streakDays = 2
                    )
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("insights_activity_funnel_card"))
        composeRule.onNodeWithTag("insights_activity_funnel_card").assertIsDisplayed()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("funnel_stage_detected"))
        composeRule.onNodeWithTag("funnel_stage_detected")
            .assertIsDisplayed()
            .assertTextContains("Feed detected", substring = true)
            .assertTextContains("12", substring = true)

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("funnel_stage_attempted"))
        composeRule.onNodeWithTag("funnel_stage_attempted")
            .assertIsDisplayed()
            .assertTextContains("Intervention attempted", substring = true)
            .assertTextContains("10", substring = true)

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("funnel_stage_completed"))
        composeRule.onNodeWithTag("funnel_stage_completed")
            .assertIsDisplayed()
            .assertTextContains("Intervention completed", substring = true)
            .assertTextContains("7", substring = true)

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("funnel_stage_continued"))
        composeRule.onNodeWithTag("funnel_stage_continued")
            .assertIsDisplayed()
            .assertTextContains("User chose to continue", substring = true)
            .assertTextContains("3", substring = true)
    }

    @Test
    fun `home screen shows customize hint, app chevrons, and separates row click from switch toggle`() {
        val rules = FeedSurgeonDatabase.defaultBuiltInRules()
        val installed = setOf("com.google.android.youtube", "com.instagram.android")
        var clickedApp: String? = null
        var toggledPair: Pair<String, Boolean>? = null

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
                        installedSupportedCount = 2
                    ),
                    onStatusClick = {},
                    onSetupClick = {},
                    onToggleGlobalPause = {},
                    onPauseForMinutes = {},
                    onGlobalBreakExpired = {},
                    onAppSnoozeExpired = {},
                    onRefreshBlockersClick = {},
                    onAppClick = { pkg -> clickedApp = pkg },
                    onToggleApp = { pkg, enabled -> toggledPair = pkg to enabled }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("home_apps_customize_hint"))
        composeRule.onNodeWithTag("home_apps_customize_hint")
            .assertIsDisplayed()
            .assertTextContains("Tap any app to customize")

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("target_app_card_com.google.android.youtube"))
        composeRule.onNodeWithTag("app_detail_chevron_com.google.android.youtube", useUnmergedTree = true)
            .assertIsDisplayed()

        // Toggling the switch invokes onToggleApp without opening AppDetail
        composeRule.onNodeWithTag("toggle_com.google.android.youtube").performClick()
        composeRule.waitForIdle()
        assertEquals("com.google.android.youtube" to false, toggledPair)
        assertEquals(null, clickedApp)

        // Tapping the row opens AppDetail independently
        composeRule.onNodeWithTag("target_app_card_com.google.android.youtube").performClick()
        composeRule.waitForIdle()
        assertEquals("com.google.android.youtube", clickedApp)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp")
    fun `onboarding renders concise flow, uninstalled apps off by default, and gated accessibility continue`() {
        val installed = setOf("com.google.android.youtube", "com.instagram.android")
        val rules = FeedSurgeonDatabase.defaultBuiltInRules(installed)
        val accessibilityEnabledState = androidx.compose.runtime.mutableStateOf(false)

        composeRule.setContent {
            FeedSurgeonTheme {
                OnboardingScreen(
                    rules = rules,
                    installedPackages = installed,
                    isAccessibilityEnabled = accessibilityEnabledState.value,
                    isBatteryOptimizationIgnored = false,
                    isNotificationPermissionGranted = false,
                    onToggleApp = { _, _ -> },
                    onAcceptDisclosure = {},
                    onDeclineDisclosure = {},
                    onCompleteOnboarding = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Step 1: Concise hero & compact disclosure card
        composeRule.onNodeWithTag("onboarding_compact_step_badge")
            .assertIsDisplayed()
            .assertTextContains("1/4 · Mission")
        composeRule.onNodeWithTag("onboarding_hero_illustration").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_hero_title").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_disclosure_card")
            .performScrollTo()
            .assertIsDisplayed()

        // Step 2: Installed ON, not-installed OFF by default
        composeRule.onNodeWithTag("onboarding_agree_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_compact_step_badge")
            .assertTextContains("2/4 · Boundaries")
        composeRule.onNodeWithTag("onboarding_selected_count_pill")
            .assertIsDisplayed()
            .assertTextContains("2 of 2 active")
        composeRule.onNodeWithTag("onboarding_app_toggle_com.google.android.youtube").assertIsOn()
        composeRule.onNodeWithTag("onboarding_app_toggle_com.facebook.katana")
            .performScrollTo()
            .assertIsOff()

        // Step 3: Muted Continue while Accessibility is off -> active accent Continue once enabled
        composeRule.onNodeWithTag("onboarding_apps_continue_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_compact_step_badge")
            .assertTextContains("3/4 · Permission")
        composeRule.onNodeWithTag("onboarding_accessibility_status_card")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Waiting for Accessibility")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_continue_step2")
            .assertTextContains("Continue")
            .assertIsNotEnabled()

        accessibilityEnabledState.value = true
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_accessibility_status_card")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Focus Guard active")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_continue_step2")
            .assertTextContains("Continue")
            .assertIsEnabled()

        // Step 4: Ready screen without Preview button
        composeRule.onNodeWithTag("onboarding_continue_step2").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_compact_step_badge")
            .assertTextContains("4/4 · Ready")
        composeRule.onNodeWithTag("onboarding_try_preview_button").assertDoesNotExist()
        composeRule.onNodeWithTag("onboarding_finish_button").assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_optional_reliability_card")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_battery_optimization_button")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `settings screen toggles breathing pause duration between 4s and 2s`() {
        var selectedPace = -1
        composeRule.setContent {
            FeedSurgeonTheme {
                SettingsScreen(
                    uiState = MainUiState(
                        isInitialLoading = false,
                        preferences = AppPreferences(breathingPauseSeconds = 4),
                        rules = FeedSurgeonDatabase.defaultBuiltInRules(),
                        installedPackages = setOf("com.google.android.youtube"),
                        isAccessibilityEnabled = true,
                        isServiceConnected = true,
                        protectionStatus = ProtectionStatus.OPERATIONAL,
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
                    onSetBreathingPauseSeconds = { selectedPace = it },
                    onClearHistory = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasTestTag("settings_breathing_pause_pill"))
        composeRule.onNodeWithTag("settings_breathing_pause_pill")
            .assertIsDisplayed()
            .assertTextContains("4s pace")
            .performClick()

        assertEquals(2, selectedPace)
    }

    @Test
    fun `outloop launch transition displays brand mark and invokes enter callback`() {
        var entered = false
        composeRule.setContent {
            FeedSurgeonTheme {
                OutloopLaunchTransition(
                    isLoading = false,
                    onEnterApp = { entered = true }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("outloop_launch_transition").assertIsDisplayed()
        composeRule.onNodeWithText("outloop").assertIsDisplayed()
        composeRule.onNodeWithText("Mindful digital focus").assertIsDisplayed()
        composeRule.onNodeWithTag("launch_open_dashboard_button").performClick()

        assertTrue(entered)
    }
}
