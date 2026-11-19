package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.ProtectionStatus
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.BottomTab
import com.example.ui.components.FloatingPillNavBar
import com.example.ui.components.InterventionBanner
import com.example.ui.components.LocalAnimatedVisibilityScope
import com.example.ui.components.LocalSharedTransitionScope
import com.example.ui.components.ObsidianSideRail
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FeedSurgeonTheme
import kotlinx.coroutines.delay

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
    const val APP_DETAIL = "app_detail/{packageName}"

    fun appDetail(packageName: String): String = "app_detail/$packageName"
}

internal fun computeNavAlertTabs(uiState: MainUiState): Set<BottomTab> = buildSet {
    if (
        uiState.protectionStatus == ProtectionStatus.PERMISSION_NEEDED ||
        uiState.protectionStatus == ProtectionStatus.SERVICE_DISCONNECTED ||
        uiState.protectionStatus == ProtectionStatus.NO_APPS_ACTIVE
    ) {
        add(BottomTab.HOME)
    }
    if (
        uiState.staleRulesCount > 0 ||
        !uiState.isBatteryOptimizationIgnored ||
        !uiState.isNotificationPermissionGranted
    ) {
        add(BottomTab.SETTINGS)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        setContent {
            FeedSurgeonTheme {
                FeedSurgeonRoot()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedSurgeonRoot(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshAccessibilityStatus(context)
        viewModel.refreshInstalledApps(context)
        viewModel.triggerAutoSyncIfNeeded()
    }

    // Auto-dismiss transient intervention banner after 8 seconds so it never obscures navigation
    LaunchedEffect(uiState.recentToast?.eventId) {
        if (uiState.recentToast != null) {
            delay(8_000L)
            viewModel.dismissToast()
        }
    }

    // Wait for Room/DataStore initial emission before building NavHost so startDestination
    // accurately reflects persisted onboardingCompleted instead of resetting to Onboarding on cold launch.
    if (uiState.isInitialLoading) {
        AtmosphericBackground {
            Box(modifier = Modifier.fillMaxSize())
        }
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = remember {
        if (uiState.preferences.onboardingCompleted) Routes.HOME else Routes.ONBOARDING
    }

    val selectedBottomTab = when (currentRoute) {
        Routes.HOME -> BottomTab.HOME
        Routes.INSIGHTS -> BottomTab.INSIGHTS
        Routes.SETTINGS -> BottomTab.SETTINGS
        else -> null
    }

    fun navigateToTopLevel(tab: BottomTab) {
        val route = when (tab) {
            BottomTab.HOME -> Routes.HOME
            BottomTab.INSIGHTS -> Routes.INSIGHTS
            BottomTab.SETTINGS -> Routes.SETTINGS
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToHomeClearingOnboarding() {
        navController.navigate(Routes.HOME) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun tabOrder(route: String?): Int = when (route) {
        Routes.HOME -> 0
        Routes.INSIGHTS -> 1
        Routes.SETTINGS -> 2
        else -> -1
    }

    val m3StandardEasing = remember { CubicBezierEasing(0.2f, 0f, 0f, 1f) }
    val navAlertTabs = remember(
        uiState.protectionStatus,
        uiState.staleRulesCount,
        uiState.isBatteryOptimizationIgnored,
        uiState.isNotificationPermissionGranted
    ) {
        computeNavAlertTabs(uiState)
    }
    val hasBanner = uiState.recentToast != null

    AtmosphericBackground {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideLayout = maxWidth >= 600.dp
                    val showTopLevelNav = selectedBottomTab != null
                    val dynamicBottomPadding = when {
                        !showTopLevelNav -> 28.dp
                        isWideLayout -> if (hasBanner) 96.dp else 24.dp
                        else -> if (hasBanner) 176.dp else 104.dp
                    }

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (isWideLayout && selectedBottomTab != null) {
                            ObsidianSideRail(
                                selectedTab = selectedBottomTab,
                                onTabSelected = ::navigateToTopLevel,
                                alertTabs = navAlertTabs
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                enterTransition = {
                                    val fromTab = tabOrder(initialState.destination.route)
                                    val toTab = tabOrder(targetState.destination.route)
                                    if (fromTab >= 0 && toTab >= 0) {
                                        val forward = toTab >= fromTab
                                        fadeIn(tween(220, easing = m3StandardEasing)) + slideInHorizontally(
                                            animationSpec = tween(280, easing = m3StandardEasing)
                                        ) { if (forward) it / 5 else -it / 5 }
                                    } else {
                                        fadeIn(tween(240, easing = m3StandardEasing)) + slideInHorizontally(
                                            animationSpec = tween(300, easing = m3StandardEasing)
                                        ) { it / 3 }
                                    }
                                },
                                exitTransition = {
                                    val fromTab = tabOrder(initialState.destination.route)
                                    val toTab = tabOrder(targetState.destination.route)
                                    if (fromTab >= 0 && toTab >= 0) {
                                        val forward = toTab >= fromTab
                                        fadeOut(tween(180, easing = m3StandardEasing)) + slideOutHorizontally(
                                            animationSpec = tween(260, easing = m3StandardEasing)
                                        ) { if (forward) -it / 5 else it / 5 }
                                    } else {
                                        fadeOut(tween(180, easing = m3StandardEasing)) + slideOutHorizontally(
                                            animationSpec = tween(280, easing = m3StandardEasing)
                                        ) { -it / 5 }
                                    }
                                },
                                popEnterTransition = {
                                    fadeIn(tween(240, easing = m3StandardEasing)) + slideInHorizontally(
                                        animationSpec = tween(300, easing = m3StandardEasing)
                                    ) { -it / 5 }
                                },
                                popExitTransition = {
                                    fadeOut(tween(180, easing = m3StandardEasing)) + slideOutHorizontally(
                                        animationSpec = tween(300, easing = m3StandardEasing)
                                    ) { it / 3 }
                                },
                                modifier = Modifier
                                    .then(
                                        if (isWideLayout) Modifier else Modifier.widthIn(max = 760.dp)
                                    )
                                    .fillMaxSize()
                                    .then(
                                        if (currentRoute != Routes.ONBOARDING) {
                                            Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                composable(Routes.ONBOARDING) {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                        OnboardingScreen(
                                            rules = uiState.rules,
                                            installedPackages = uiState.installedPackages,
                                            isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                                            isBatteryOptimizationIgnored = uiState.isBatteryOptimizationIgnored,
                                            isNotificationPermissionGranted = uiState.isNotificationPermissionGranted,
                                            onRefreshProtectionHealth = {
                                                viewModel.refreshProtectionHealth(context)
                                            },
                                            onToggleApp = { pkg, enabled ->
                                                viewModel.setRuleEnabled(pkg, enabled)
                                            },
                                            onAcceptDisclosure = { viewModel.acceptDisclosure() },
                                            onDeclineDisclosure = {
                                                viewModel.declineDisclosure()
                                                navigateToHomeClearingOnboarding()
                                            },
                                            onCompleteOnboarding = {
                                                viewModel.completeOnboarding()
                                                navigateToHomeClearingOnboarding()
                                            }
                                        )
                                    }
                                }

                                composable(Routes.HOME) {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                        HomeScreen(
                                            uiState = uiState,
                                            onStatusClick = {
                                                if (uiState.isAccessibilityEnabled) {
                                                    navigateToTopLevel(BottomTab.SETTINGS)
                                                } else {
                                                    navController.navigate(Routes.ONBOARDING)
                                                }
                                            },
                                            onSetupClick = {
                                                navController.navigate(Routes.ONBOARDING)
                                            },
                                            onToggleGlobalPause = { paused ->
                                                viewModel.setGlobalProtectionPaused(paused)
                                            },
                                            onPauseForMinutes = { minutes ->
                                                viewModel.pauseGloballyForMinutes(minutes)
                                            },
                                            onGlobalBreakExpired = {
                                                viewModel.resumeGlobalProtection()
                                            },
                                            onAppSnoozeExpired = { pkg ->
                                                viewModel.resumeFromSnooze(pkg)
                                            },
                                            onRefreshBlockersClick = {
                                                viewModel.updateRemoteRules()
                                            },
                                            onAppClick = { pkg ->
                                                navController.navigate(Routes.appDetail(pkg))
                                            },
                                            onToggleApp = { pkg, enabled ->
                                                viewModel.setRuleEnabled(pkg, enabled)
                                            },
                                            bottomContentPadding = dynamicBottomPadding
                                        )
                                    }
                                }

                                composable(Routes.INSIGHTS) {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                        InsightsScreen(
                                            uiState = uiState,
                                            bottomContentPadding = dynamicBottomPadding
                                        )
                                    }
                                }

                                composable(Routes.SETTINGS) {
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                        SettingsScreen(
                                            uiState = uiState,
                                            onReopenDisclosure = {
                                                viewModel.reopenOnboarding()
                                                navController.navigate(Routes.ONBOARDING)
                                            },
                                            onToggleGlobalPause = { paused ->
                                                viewModel.setGlobalProtectionPaused(paused)
                                            },
                                            onSetAutoUpdateRules = { enabled ->
                                                viewModel.setAutoUpdateRules(enabled)
                                            },
                                            onCheckRulesNow = { viewModel.updateRemoteRules() },
                                            onSetEventCaptureEnabled = { enabled ->
                                                viewModel.setEventCaptureEnabled(enabled)
                                            },
                                            onRefreshEventLog = { viewModel.refreshEventLog() },
                                            onClearEventLog = { viewModel.clearEventLog() },
                                            onShareEventLog = {
                                                viewModel.createShareLogIntent()?.let { shareIntent ->
                                                    context.startActivity(
                                                        Intent.createChooser(shareIntent, "Share diagnostic log")
                                                    )
                                                }
                                            },
                                            onSetReclaimedSeconds = { sec -> viewModel.setReclaimedSecondsPerCut(sec) },
                                            onSetShowBanner = { show -> viewModel.setShowInterventionBanner(show) },
                                            onClearHistory = { viewModel.clearHistory() },
                                            onRefreshProtectionHealth = {
                                                viewModel.refreshProtectionHealth(context)
                                            },
                                            bottomContentPadding = dynamicBottomPadding
                                        )
                                    }
                                }

                                composable(
                                    route = Routes.APP_DETAIL,
                                    arguments = listOf(navArgument("packageName") { type = NavType.StringType })
                                ) { entry ->
                                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                        val pkg = entry.arguments?.getString("packageName").orEmpty()
                                        val rule = uiState.rules.firstOrNull { it.packageName == pkg }
                                        AppDetailScreen(
                                            rule = rule,
                                            onBack = { navController.popBackStack() },
                                            onToggleEnabled = { enabled -> viewModel.setRuleEnabled(pkg, enabled) },
                                            onSelectMode = { mode -> viewModel.setInterventionMode(pkg, mode) },
                                            onChangeCooldown = { ms -> viewModel.setCooldownMs(pkg, ms) },
                                            onSnoozeMinutes = { minutes -> viewModel.snoozeForMinutes(pkg, minutes) },
                                            onResumeSnooze = { viewModel.resumeFromSnooze(pkg) }
                                        )
                                    }
                                }
                            }

                            if (selectedBottomTab != null) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .then(
                                            if (isWideLayout) {
                                                Modifier
                                                    .widthIn(max = 560.dp)
                                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                                    .padding(bottom = 16.dp)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AnimatedVisibility(
                                        visible = uiState.recentToast != null,
                                        enter = slideInVertically { it } + fadeIn(),
                                        exit = slideOutVertically { it } + fadeOut()
                                    ) {
                                        uiState.recentToast?.let { toast ->
                                            InterventionBanner(
                                                toast = toast,
                                                onAllowOnce = { viewModel.allowOnceFromToast(toast) },
                                                onUndo = { viewModel.undoFromToast(toast) },
                                                onDismiss = { viewModel.dismissToast() }
                                            )
                                        }
                                    }

                                    if (!isWideLayout) {
                                        FloatingPillNavBar(
                                            selectedTab = selectedBottomTab,
                                            onTabSelected = ::navigateToTopLevel,
                                            alertTabs = navAlertTabs
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
}
