package com.example.service

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

class AccessibilityOverlayShield(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null

    fun isShowing(): Boolean = overlayView != null

    fun show(
        appName: String,
        feedLabel: String,
        onExitFeed: () -> Unit,
        onAllowOnce: () -> Unit,
        onAttachmentFailed: () -> Unit = {}
    ) {
        mainHandler.post {
            if (overlayView != null) return@post

            val density = context.resources.displayMetrics.density
            fun dp(value: Int): Int = (value * density).toInt()

            val root = FrameLayout(context).apply {
                setBackgroundColor(Color.parseColor("#E80A0F0E"))
                setPadding(dp(20), dp(24), dp(20), dp(24))
                isFocusable = true
                isFocusableInTouchMode = true
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                contentDescription = "Mindful Pause dialog for $appName $feedLabel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    accessibilityPaneTitle = "Mindful Pause"
                }
                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        dismiss()
                        onExitFeed()
                        true
                    } else {
                        false
                    }
                }
            }

            val scrollContainer = ScrollView(context).apply {
                isFillViewport = false
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                isVerticalScrollBarEnabled = false
            }

            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(24), dp(24), dp(24), dp(24))
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        Color.parseColor("#F0192220"),
                        Color.parseColor("#EB131918")
                    )
                ).apply {
                    cornerRadius = dp(22).toFloat()
                    setStroke(dp(1), Color.parseColor("#247A63"))
                }
            }

            val orbView = BreathingOrbView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(76), dp(76)).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            }

            val badge = TextView(context).apply {
                text = "MINDFUL PAUSE"
                textSize = 11f
                gravity = Gravity.CENTER_HORIZONTAL
                setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
                setTextColor(Color.parseColor("#34D3A8"))
                letterSpacing = 0.08f
                setPadding(0, dp(10), 0, 0)
            }

            val title = TextView(context).apply {
                text = "Take a quick breath"
                textSize = 22f
                gravity = Gravity.CENTER_HORIZONTAL
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(Color.parseColor("#F3F6F5"))
                setPadding(0, dp(8), 0, dp(8))
                isFocusable = true
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }

            val body = TextView(context).apply {
                text = "$appName $feedLabel opened. Step back to stay intentional, or allow 5 minutes for this visit."
                textSize = 14f
                gravity = Gravity.CENTER_HORIZONTAL
                setTextColor(Color.parseColor("#8B9794"))
                setPadding(0, 0, 0, dp(20))
            }

            val exitButton = Button(context).apply {
                text = "Leave $feedLabel"
                contentDescription = "Leave $feedLabel and return to $appName Home"
                isAllCaps = false
                textSize = 15f
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(Color.parseColor("#061B15"))
                setPadding(dp(16), dp(12), dp(16), dp(12))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#34D3A8"))
                    cornerRadius = dp(100).toFloat()
                }
                minimumHeight = dp(48)
                minHeight = dp(48)
                setOnClickListener {
                    dismiss()
                    onExitFeed()
                }
            }

            val allowButton = Button(context).apply {
                text = "Allow 5 min (this visit)"
                contentDescription = "Allow $appName $feedLabel for 5 minutes on this visit"
                isAllCaps = false
                textSize = 14f
                setTextColor(Color.parseColor("#F3F6F5"))
                setPadding(dp(16), dp(12), dp(16), dp(12))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#192220"))
                    cornerRadius = dp(100).toFloat()
                    setStroke(dp(1), Color.parseColor("#232D2B"))
                }
                minimumHeight = dp(48)
                minHeight = dp(48)
                setOnClickListener {
                    dismiss()
                    onAllowOnce()
                }
            }

            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(1, dp(10))
            }

            card.addView(orbView)
            card.addView(badge)
            card.addView(title)
            card.addView(body)
            card.addView(exitButton)
            card.addView(spacer)
            card.addView(allowButton)

            scrollContainer.addView(
                card,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val scrollParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            root.addView(scrollContainer, scrollParams)

            val animatorsEnabled = ValueAnimator.areAnimatorsEnabled()
            if (animatorsEnabled) {
                root.alpha = 0f
                card.scaleX = 0.92f
                card.scaleY = 0.92f
                card.alpha = 0f
            }

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            try {
                windowManager.addView(root, layoutParams)
                overlayView = root
                if (animatorsEnabled) {
                    root.animate()
                        .alpha(1f)
                        .setDuration(180L)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                    card.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(260L)
                        .setInterpolator(OvershootInterpolator(1.05f))
                        .start()
                }
                root.requestFocus()
                title.post {
                    title.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            } catch (_: Exception) {
                overlayView = null
                onAttachmentFailed()
            }
        }
    }

    fun dismiss() {
        mainHandler.post {
            val view = overlayView ?: return@post
            try {
                view.animate().cancel()
                windowManager.removeView(view)
            } catch (_: Exception) {
            } finally {
                overlayView = null
            }
        }
    }

    private class BreathingOrbView(context: Context) : View(context) {
        private val density = context.resources.displayMetrics.density
        private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#34D3A8")
        }
        private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.25f * density
            color = Color.argb(145, 181, 247, 229)
        }

        private var phase = 0.25f
        private var animator: ValueAnimator? = null
        private var centerX = 0f
        private var centerY = 0f
        private var maxRadius = 0f

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            centerX = w / 2f
            centerY = h / 2f
            maxRadius = min(w, h) / 2f
            if (maxRadius > 0f) {
                haloPaint.shader = RadialGradient(
                    centerX,
                    centerY,
                    maxRadius * 0.98f,
                    intArrayOf(
                        Color.argb(78, 52, 211, 168),
                        Color.argb(20, 52, 211, 168),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                val coreRadius = maxRadius * 0.36f
                corePaint.shader = RadialGradient(
                    centerX - coreRadius * 0.28f,
                    centerY - coreRadius * 0.28f,
                    coreRadius * 1.45f,
                    intArrayOf(
                        Color.parseColor("#B5F7E5"),
                        Color.parseColor("#34D3A8"),
                        Color.parseColor("#14352C")
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (ValueAnimator.areAnimatorsEnabled() && animator == null) {
                animator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 3800L
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        phase = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }
        }

        override fun onDetachedFromWindow() {
            animator?.cancel()
            animator = null
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (maxRadius <= 0f) return

            val breath = ((sin(phase * 2.0 * PI) + 1.0) * 0.5).toFloat()
            val coreRadius = maxRadius * (0.34f + 0.04f * breath)

            canvas.drawCircle(centerX, centerY, maxRadius * 0.98f, haloPaint)

            for (i in 0 until 3) {
                val p = (phase + i / 3f) % 1f
                val rippleR = coreRadius + (maxRadius * 0.92f - coreRadius) * p
                val alpha = ((1f - p) * (1f - p) * 115f).toInt().coerceIn(0, 255)
                if (alpha > 2) {
                    ripplePaint.alpha = alpha
                    ripplePaint.strokeWidth = (1.5f * density) * (1f - 0.35f * p)
                    canvas.drawCircle(centerX, centerY, rippleR, ripplePaint)
                }
            }

            canvas.drawCircle(centerX, centerY, coreRadius, corePaint)
            canvas.drawCircle(centerX, centerY, coreRadius, rimPaint)
        }
    }
}
