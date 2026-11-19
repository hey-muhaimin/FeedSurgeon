package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.OnboardingStep
import com.example.ui.components.CircularBackButton
import com.example.ui.theme.StillspaceTheme
import com.example.ui.theme.rememberReducedMotionEnabled

@Composable
fun OnboardingScaffold(
    currentStep: OnboardingStep,
    onPreviousStep: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (isWideLayout: Boolean) -> Unit,
    stepContent: @Composable (step: OnboardingStep, isWideLayout: Boolean) -> Unit
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val scheme = MaterialTheme.colorScheme
    val neu = StillspaceTheme.colors

    BackHandler(enabled = currentStep.index > 0) {
        onPreviousStep()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideLayout = maxWidth >= 600.dp
        val maxContainerWidth = if (isWideLayout) 960.dp else 560.dp
        val horizontalPadding = if (isWideLayout) 32.dp else 20.dp

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding_responsive_scaffold"),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = maxContainerWidth)
                            .fillMaxWidth()
                            .padding(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = 16.dp,
                                bottom = 12.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep.index > 0) {
                            CircularBackButton(onClick = onPreviousStep)
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("onboarding_progress_bar"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(OnboardingStep.entries.size) { index ->
                                val isActive = index <= currentStep.index
                                val segmentColor by animateColorAsState(
                                    targetValue = if (isActive) scheme.primary else neu.trackInactive,
                                    animationSpec = tween(
                                        durationMillis = if (reducedMotion) 0 else 280,
                                        easing = FastOutSlowInEasing
                                    ),
                                    label = "onboarding_step_segment_$index"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(segmentColor)
                                )
                            }
                        }
                        if (isWideLayout) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STEP ${currentStep.index + 1} OF ${OnboardingStep.entries.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = neu.textMuted,
                                modifier = Modifier.testTag("onboarding_wide_step_badge")
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = maxContainerWidth)
                            .fillMaxWidth()
                            .padding(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = 8.dp,
                                bottom = 18.dp
                            )
                    ) {
                        bottomBar(isWideLayout)
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier
                        .widthIn(max = maxContainerWidth)
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding),
                    transitionSpec = {
                        if (reducedMotion) {
                            fadeIn(tween(120)) togetherWith fadeOut(tween(120))
                        } else {
                            val forward = targetState.index >= initialState.index
                            val enter = slideInHorizontally(
                                animationSpec = tween(
                                    durationMillis = 340,
                                    easing = FastOutSlowInEasing
                                )
                            ) { fullWidth ->
                                if (forward) fullWidth else -fullWidth
                            } + fadeIn(
                                animationSpec = tween(durationMillis = 240)
                            )
                            val exit = slideOutHorizontally(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = FastOutSlowInEasing
                                )
                            ) { fullWidth ->
                                if (forward) -fullWidth / 3 else fullWidth / 3
                            } + fadeOut(
                                animationSpec = tween(durationMillis = 180)
                            )
                            enter.togetherWith(exit)
                                .using(SizeTransform(clip = false))
                        }
                    },
                    label = "onboarding_slide_transition"
                ) { step ->
                    stepContent(step, isWideLayout)
                }
            }
        }
    }
}
