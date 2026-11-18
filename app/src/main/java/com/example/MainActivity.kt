package com.example

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.MainViewModel
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.BottomTab
import com.example.ui.components.CustomAppRuleDialog
import com.example.ui.components.FloatingPillNavBar
import com.example.ui.components.InterventionBanner
import com.example.ui.components.LocalAnimatedVisibilityScope
import com.example.ui.components.LocalSharedTransitionScope
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.DeveloperPanelScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RuleHealthScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FeedSurgeonTheme

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val RULES = "rules"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
    const val APP_DETAIL = "app_detail/{packageName}"
    const val DEVELOPER_PANEL = "developer_panel"

    fun appDetail(packageName: String): String = "app_detail/$packageName"
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
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var showCustomAppDialog by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshAccessibilityStatus(context)
    }

    val startDestination = if (uiState.preferences.onboardingCompleted) {
        Routes.HOME
    } else {
        Routes.ONBOARDING
    }

    val selectedBottomTab = when (currentRoute) {
        Routes.HOME -> BottomTab.HOME
        Routes.RULES -> BottomTab.RULES
        Routes.INSIGHTS -> BottomTab.INSIGHTS
        Routes.SETTINGS -> BottomTab.SETTINGS
        else -> null
    }

    fun navigateToTopLevel(tab: BottomTab) {
        val route = when (tab) {
            BottomTab.HOME -> Routes.HOME
            BottomTab.RULES -> Routes.RULES
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

    fun tabOrder(route: String?): Int = when (route) {
        Routes.HOME -> 0
        Routes.RULES -> 1
        Routes.INSIGHTS -> 2
        Routes.SETTINGS -> 3
        else -> -1
    }

    val m3StandardEasing = remember { CubicBezierEasing(0.2f, 0f, 0f, 1f) }

    AtmosphericBackground {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                Box(modifier = Modifier.fillMaxSize()) {
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
                                    isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                                    onAcceptDisclosure = { viewModel.acceptDisclosure() },
                                    onDeclineDisclosure = {
                                        viewModel.declineDisclosure()
                                        navController.navigate(Routes.HOME) {
                                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                                        }
                                    },
                                    onCompleteOnboarding = {
                                        viewModel.completeOnboarding()
                                        navController.navigate(Routes.HOME) {
                                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                                        }
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
                                    onRuleHealthCardClick = { navigateToTopLevel(BottomTab.RULES) },
                                    onAppClick = { pkg ->
                                        navController.navigate(Routes.appDetail(pkg))
                                    },
                                    onToggleApp = { pkg, enabled ->
                                        viewModel.setRuleEnabled(pkg, enabled)
                                    },
                                    onAddCustomAppClick = { showCustomAppDialog = true }
                                )
                            }
                        }

                        composable(Routes.RULES) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                RuleHealthScreen(
                                    uiState = uiState,
                                    onUpdateRulesClick = { viewModel.updateRemoteRules() },
                                    onTestNowClick = { pkg -> viewModel.runRuleHealthCheck(pkg) },
                                    onOpenDeveloperPanel = {
                                        navController.navigate(Routes.DEVELOPER_PANEL)
                                    }
                                )
                            }
                        }

                        composable(Routes.INSIGHTS) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                InsightsScreen(uiState = uiState)
                            }
                        }

                        composable(Routes.SETTINGS) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                SettingsScreen(
                                    uiState = uiState,
                                    onOpenDeveloperPanel = {
                                        navController.navigate(Routes.DEVELOPER_PANEL)
                                    },
                                    onReopenDisclosure = {
                                        viewModel.reopenOnboarding()
                                        navController.navigate(Routes.ONBOARDING)
                                    },
                                    onAddCustomAppClick = { showCustomAppDialog = true },
                                    onSaveEndpointUrl = { url -> viewModel.saveRulesetEndpointUrl(url) },
                                    onSyncRemoteRules = { url -> viewModel.updateRemoteRules(url) },
                                    onRestoreBundledRules = { viewModel.restoreBundledRules() },
                                    onSetReclaimedSeconds = { sec -> viewModel.setReclaimedSecondsPerCut(sec) },
                                    onSetShowBanner = { show -> viewModel.setShowInterventionBanner(show) },
                                    onClearHistory = { viewModel.clearHistory() }
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
                                    onSnooze15Minutes = { viewModel.snooze15Minutes(pkg) },
                                    onResumeSnooze = { viewModel.resumeFromSnooze(pkg) },
                                    onDeleteCustomRule = { viewModel.deleteCustomRule(pkg) }
                                )
                            }
                        }

                        composable(Routes.DEVELOPER_PANEL) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                DeveloperPanelScreen(
                                    uiState = uiState,
                                    onBack = { navController.popBackStack() },
                                    onQueryChange = { q -> viewModel.updateSelectorTestQuery(q) },
                                    onRunTest = { viewModel.runSelectorTest() },
                                    onTriggerDiagnosticIntervention = { rule ->
                                        viewModel.triggerDiagnosticIntervention(rule)
                                    }
                                )
                            }
                        }
                    }

                    if (selectedBottomTab != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
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
                                        onUndo = { viewModel.undoFromToast(toast) }
                                    )
                                }
                            }

                            FloatingPillNavBar(
                                selectedTab = selectedBottomTab,
                                onTabSelected = ::navigateToTopLevel
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomAppDialog) {
        CustomAppRuleDialog(
            onDismiss = { showCustomAppDialog = false },
            onSave = { pkg, name, feedLabel, viewIdsText, tabFallback, mode ->
                viewModel.saveCustomRule(
                    packageName = pkg,
                    displayName = name,
                    feedLabel = feedLabel,
                    viewIdsText = viewIdsText,
                    tabFallbackLabel = tabFallback,
                    homeTabLabel = "Home",
                    interventionMode = mode
                )
                showCustomAppDialog = false
            }
        )
    }
}
