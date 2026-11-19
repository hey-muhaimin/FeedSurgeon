package com.example.service

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.R
import kotlin.math.PI
import kotlin.math.cos
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
        breathingHalfCycleSeconds: Int = 4,
        onPaceChanged: (Int) -> Unit = {},
        onExitFeed: () -> Unit,
        onAllowOnce: (String?) -> Unit,
        onOverlayShown: () -> Unit = {},
        onAttachmentFailed: () -> Unit = {}
    ) {
        mainHandler.post {
            if (overlayView != null) return@post

            val density = context.resources.displayMetrics.density
            fun dp(value: Int): Int = (value * density).toInt()

            val manrope = runCatching {
                ResourcesCompat.getFont(context, R.font.manrope)
            }.getOrNull() ?: Typeface.SANS_SERIF
            val jakarta = runCatching {
                ResourcesCompat.getFont(context, R.font.plus_jakarta_sans)
            }.getOrNull() ?: Typeface.SANS_SERIF

            var currentPaceSeconds = if (breathingHalfCycleSeconds == 2) 2 else 4
            var isInhaling = true
            var secondsLeft = currentPaceSeconds
            var intentionTrayVisible = false

            // Dimmed translucent scrim letting the paused app peek behind the adaptive floating modal
            val root = FrameLayout(context).apply {
                setBackgroundColor(Color.parseColor("#66142119"))
                setPadding(dp(20), dp(24), dp(20), dp(24))
                isFocusable = true
                isFocusableInTouchMode = true
                isClickable = true
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                contentDescription = "Outloop Active Pause for $appName $feedLabel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    accessibilityPaneTitle = "Outloop Active Pause"
                }
                setOnClickListener {
                    dismiss()
                    onExitFeed()
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

            val maxCardWidthPx = min(
                context.resources.displayMetrics.widthPixels - dp(40),
                dp(396)
            )

            val scrollContainer = ScrollView(context).apply {
                isFillViewport = false
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                isVerticalScrollBarEnabled = false
            }

            // Frosted glassomorphic adaptive surface: translucent ivory gradient + crisp glass inner specular rim
            val outerFrost = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#ECFAF9F5"),
                    Color.parseColor("#DEFAF9F5")
                )
            ).apply {
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), Color.parseColor("#42142119"))
            }
            val innerSpecularRim = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                cornerRadius = dp(27).toFloat()
                setStroke(dp(1), Color.parseColor("#CCFFFFFF"))
            }
            val glassCardBackground = LayerDrawable(arrayOf(outerFrost, innerSpecularRim)).apply {
                setLayerInset(1, dp(1), dp(1), dp(1), dp(1))
            }

            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(18), dp(20), dp(20))
                background = glassCardBackground
                isClickable = true
                isFocusable = false
            }

            // Header Row: OUTLOOP · Active Pause pill + 48dp dismiss icon
            val headerRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val brandPill = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(11), dp(6), dp(11), dp(6))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#B8FFFFFF"))
                    cornerRadius = dp(999).toFloat()
                    setStroke(dp(1), Color.parseColor("#30142119"))
                }
            }

            val loopGlyph = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                    marginEnd = dp(7)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#FF29362E"))
                }
            }

            val brandPillText = TextView(context).apply {
                text = "OUTLOOP · Active Pause"
                textSize = 11f
                typeface = Typeface.create(manrope, Typeface.BOLD)
                setTextColor(Color.parseColor("#FF29362E"))
                letterSpacing = 0.04f
            }

            brandPill.addView(loopGlyph)
            brandPill.addView(brandPillText)

            val headerSpacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
            }

            val dismissButton = FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
                contentDescription = "Dismiss pause and return from $appName $feedLabel"
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    dismiss()
                    onExitFeed()
                }
            }
            val dismissCircle = TextView(context).apply {
                text = "✕"
                textSize = 13.5f
                gravity = Gravity.CENTER
                typeface = Typeface.create(manrope, Typeface.BOLD)
                setTextColor(Color.parseColor("#FF5A625B"))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#B8FFFFFF"))
                    setStroke(dp(1), Color.parseColor("#30142119"))
                }
                layoutParams = FrameLayout.LayoutParams(dp(34), dp(34), Gravity.CENTER)
            }
            dismissButton.addView(dismissCircle)

            headerRow.addView(brandPill)
            headerRow.addView(headerSpacer)
            headerRow.addView(dismissButton)

            // Compact frosted breathing pace toggle (2s quick reset / 4s calm pause)
            val paceTrack = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(3), dp(3), dp(3), dp(3))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#8CEAE7DF"))
                    cornerRadius = dp(999).toFloat()
                    setStroke(dp(1), Color.parseColor("#2B142119"))
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = dp(8)
                }
            }

            val pace2sBtn = TextView(context).apply {
                text = "2s quick reset"
                textSize = 11f
                gravity = Gravity.CENTER
                setPadding(dp(11), dp(6), dp(11), dp(6))
                minHeight = dp(34)
                isClickable = true
                isFocusable = true
            }

            val pace4sBtn = TextView(context).apply {
                text = "4s calm pause"
                textSize = 11f
                gravity = Gravity.CENTER
                setPadding(dp(11), dp(6), dp(11), dp(6))
                minHeight = dp(34)
                isClickable = true
                isFocusable = true
            }

            paceTrack.addView(pace2sBtn)
            paceTrack.addView(pace4sBtn)

            // Compact Open Loop breathing ring
            val pacerContainer = FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(124), dp(124)).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = dp(10)
                }
                isClickable = true
                isFocusable = true
                contentDescription = "Tap to synchronize breathing rhythm"
            }

            val loopBreathingView = OpenLoopBreathingView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            val centerTextStack = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            }

            val phaseText = TextView(context).apply {
                text = "INHALE"
                textSize = 13.5f
                gravity = Gravity.CENTER
                typeface = Typeface.create(manrope, Typeface.BOLD)
                setTextColor(Color.parseColor("#FF1B1C1A"))
                letterSpacing = 0.14f
            }

            val phaseSubText = TextView(context).apply {
                text = "${currentPaceSeconds}s"
                textSize = 11.5f
                gravity = Gravity.CENTER
                typeface = Typeface.create(jakarta, Typeface.NORMAL)
                setTextColor(Color.parseColor("#FF5A625B"))
                setPadding(0, dp(2), 0, 0)
            }

            centerTextStack.addView(phaseText)
            centerTextStack.addView(phaseSubText)

            pacerContainer.addView(loopBreathingView)
            pacerContainer.addView(centerTextStack)

            val title = TextView(context).apply {
                text = "Take a breath."
                textSize = 20f
                gravity = Gravity.CENTER_HORIZONTAL
                typeface = Typeface.create(manrope, Typeface.BOLD)
                setTextColor(Color.parseColor("#FF1B1C1A"))
                setPadding(0, dp(8), 0, dp(4))
                isFocusable = true
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }

            val body = TextView(context).apply {
                text = "$appName $feedLabel opened \u00B7 Pause before scrolling."
                textSize = 13f
                gravity = Gravity.CENTER_HORIZONTAL
                typeface = Typeface.create(jakarta, Typeface.NORMAL)
                setTextColor(Color.parseColor("#FF434844"))
                setPadding(dp(6), 0, dp(6), dp(14))
            }

            // Inline horizontal button row: [ Return ] [ Intended Visit ]
            val actionRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val returnButton = Button(context).apply {
                text = "Return"
                contentDescription = "Return and leave $appName $feedLabel"
                isAllCaps = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSize = 14f
                typeface = Typeface.create(manrope, Typeface.BOLD)
                setTextColor(Color.parseColor("#FFFAF9F5"))
                setPadding(dp(12), dp(10), dp(12), dp(10))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FF29362E"))
                    cornerRadius = dp(16).toFloat()
                }
                minimumHeight = dp(48)
                minHeight = dp(48)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    dismiss()
                    onExitFeed()
                }
            }

            val actionGap = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(10), 1)
            }

            val intendedVisitButton = Button(context).apply {
                text = "Intended Visit"
                contentDescription = "Open quick intention tray to grant a 5 minute visit"
                isAllCaps = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSize = 13.5f
                typeface = Typeface.create(manrope, Typeface.BOLD)
                setTextColor(Color.parseColor("#FF1B1C1A"))
                setPadding(dp(12), dp(10), dp(12), dp(10))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#D9FFFFFF"))
                    cornerRadius = dp(16).toFloat()
                    setStroke(dp(1), Color.parseColor("#3D142119"))
                }
                minimumHeight = dp(48)
                minHeight = dp(48)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            actionRow.addView(returnButton)
            actionRow.addView(actionGap)
            actionRow.addView(intendedVisitButton)

            // Expandable Quick Intention Tray: preset chips + custom text input + tick-icon-only submit
            val intentionTray = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                setPadding(0, dp(12), 0, 0)
            }

            val intentionLabel = TextView(context).apply {
                text = "QUICK INTENTION"
                textSize = 10.5f
                gravity = Gravity.START
                typeface = Typeface.create(manrope, Typeface.BOLD)
                letterSpacing = 0.08f
                setTextColor(Color.parseColor("#FF5A625B"))
                setPadding(dp(2), 0, 0, dp(7))
            }

            val presetRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            fun makePresetChip(label: String): Button = Button(context).apply {
                text = label
                isAllCaps = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSize = 12.5f
                typeface = Typeface.create(manrope, Typeface.BOLD)
                setTextColor(Color.parseColor("#FF1B1C1A"))
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#CCFFFFFF"))
                    cornerRadius = dp(14).toFloat()
                    setStroke(dp(1), Color.parseColor("#38142119"))
                }
                minimumHeight = dp(44)
                minHeight = dp(44)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    dismiss()
                    onAllowOnce(label)
                }
            }

            val dmChip = makePresetChip("Direct message")
            val chipGap = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 1)
            }
            val savedChip = makePresetChip("Saved post")

            presetRow.addView(dmChip)
            presetRow.addView(chipGap)
            presetRow.addView(savedChip)

            val inputRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10), dp(4), dp(4), dp(4))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F2FFFFFF"))
                    cornerRadius = dp(16).toFloat()
                    setStroke(dp(1), Color.parseColor("#40142119"))
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                }
            }

            val intentionInput = EditText(context).apply {
                hint = "Write quick intention..."
                textSize = 13f
                typeface = Typeface.create(jakarta, Typeface.NORMAL)
                setTextColor(Color.parseColor("#FF1B1C1A"))
                setHintTextColor(Color.parseColor("#FF737874"))
                background = null
                isSingleLine = true
                maxLines = 1
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                imeOptions = EditorInfo.IME_ACTION_DONE
                minHeight = dp(44)
                setPadding(dp(4), dp(8), dp(8), dp(8))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            fun submitWrittenIntention() {
                val customReason = intentionInput.text?.toString()?.trim().orEmpty().ifEmpty { null }
                dismiss()
                onAllowOnce(customReason)
            }

            intentionInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    submitWrittenIntention()
                    true
                } else {
                    false
                }
            }

            val submitTickButton = ImageButton(context).apply {
                setImageResource(R.drawable.ic_check_tick)
                scaleType = ImageView.ScaleType.CENTER
                contentDescription = "Submit intention and grant 5 minute pass"
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FF29362E"))
                    cornerRadius = dp(12).toFloat()
                }
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(44))
                setOnClickListener {
                    submitWrittenIntention()
                }
            }

            inputRow.addView(intentionInput)
            inputRow.addView(submitTickButton)

            intentionTray.addView(intentionLabel)
            intentionTray.addView(presetRow)
            intentionTray.addView(inputRow)

            intendedVisitButton.setOnClickListener {
                intentionTrayVisible = !intentionTrayVisible
                intentionTray.visibility = if (intentionTrayVisible) View.VISIBLE else View.GONE
                intendedVisitButton.background = GradientDrawable().apply {
                    setColor(
                        if (intentionTrayVisible) Color.parseColor("#FFD8E3CF")
                        else Color.parseColor("#D9FFFFFF")
                    )
                    cornerRadius = dp(16).toFloat()
                    setStroke(
                        dp(1),
                        if (intentionTrayVisible) Color.parseColor("#FFAFC1A3")
                        else Color.parseColor("#3D142119")
                    )
                }
                if (intentionTrayVisible) {
                    intentionInput.post {
                        intentionInput.requestFocus()
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showSoftInput(intentionInput, InputMethodManager.SHOW_IMPLICIT)
                    }
                }
            }

            fun refreshPaceUi() {
                fun styleButton(btn: TextView, selected: Boolean) {
                    btn.typeface = Typeface.create(manrope, Typeface.BOLD)
                    btn.setTextColor(
                        if (selected) Color.parseColor("#FF1B1C1A") else Color.parseColor("#FF5A625B")
                    )
                    btn.background = if (selected) {
                        GradientDrawable().apply {
                            setColor(Color.parseColor("#E6FFFFFF"))
                            cornerRadius = dp(999).toFloat()
                            setStroke(dp(1), Color.parseColor("#30142119"))
                        }
                    } else {
                        null
                    }
                }
                styleButton(pace2sBtn, currentPaceSeconds == 2)
                styleButton(pace4sBtn, currentPaceSeconds == 4)
                loopBreathingView.setHalfCycleSeconds(currentPaceSeconds)
            }

            fun refreshBreathCopy() {
                phaseText.text = if (isInhaling) "INHALE" else "EXHALE"
                phaseSubText.text = "${secondsLeft}s"
            }

            pace2sBtn.setOnClickListener {
                if (currentPaceSeconds != 2) {
                    currentPaceSeconds = 2
                    isInhaling = true
                    secondsLeft = 2
                    refreshPaceUi()
                    refreshBreathCopy()
                    onPaceChanged(2)
                }
            }
            pace4sBtn.setOnClickListener {
                if (currentPaceSeconds != 4) {
                    currentPaceSeconds = 4
                    isInhaling = true
                    secondsLeft = 4
                    refreshPaceUi()
                    refreshBreathCopy()
                    onPaceChanged(4)
                }
            }
            pacerContainer.setOnClickListener {
                isInhaling = !isInhaling
                secondsLeft = currentPaceSeconds
                refreshBreathCopy()
            }

            refreshPaceUi()
            refreshBreathCopy()

            val tickRunnable = object : Runnable {
                override fun run() {
                    if (overlayView == null) return
                    secondsLeft -= 1
                    if (secondsLeft <= 0) {
                        isInhaling = !isInhaling
                        secondsLeft = currentPaceSeconds
                    }
                    refreshBreathCopy()
                    mainHandler.postDelayed(this, 1000L)
                }
            }

            card.addView(headerRow)
            card.addView(paceTrack)
            card.addView(pacerContainer)
            card.addView(title)
            card.addView(body)
            card.addView(actionRow)
            card.addView(intentionTray)

            scrollContainer.addView(
                card,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val scrollParams = FrameLayout.LayoutParams(
                maxCardWidthPx,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            root.addView(scrollContainer, scrollParams)

            val animatorsEnabled = ValueAnimator.areAnimatorsEnabled()
            if (animatorsEnabled) {
                root.alpha = 0f
                card.scaleX = 0.94f
                card.scaleY = 0.94f
                card.alpha = 0f
            }

            val windowFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            } else {
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            }

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                windowFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                dimAmount = 0.42f
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    blurBehindRadius = dp(24)
                }
            }

            try {
                windowManager.addView(root, layoutParams)
                overlayView = root
                root.tag = tickRunnable
                mainHandler.postDelayed(tickRunnable, 1000L)
                onOverlayShown()
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
                        .setInterpolator(OvershootInterpolator(1.04f))
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
            (view.tag as? Runnable)?.let { mainHandler.removeCallbacks(it) }
            try {
                view.animate().cancel()
                windowManager.removeView(view)
            } catch (_: Exception) {
            } finally {
                overlayView = null
            }
        }
    }

    private class OpenLoopBreathingView(context: Context) : View(context) {
        private val density = context.resources.displayMetrics.density
        private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val innerPlatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6FFFFFF")
            style = Paint.Style.FILL
        }
        private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 3f * density
            color = Color.parseColor("#FFEAE7DF")
        }
        private val activeArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4.2f * density
            color = Color.parseColor("#FF29362E")
        }
        private val amberDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#FFE5A93C")
        }
        private val arcRect = RectF()

        private var phase = 0.25f
        private var halfCycleSeconds = 4
        private var animator: ValueAnimator? = null
        private var centerX = 0f
        private var centerY = 0f
        private var maxRadius = 0f

        fun setHalfCycleSeconds(seconds: Int) {
            val clamped = if (seconds == 2) 2 else 4
            if (halfCycleSeconds == clamped) return
            halfCycleSeconds = clamped
            restartAnimator()
        }

        private fun restartAnimator() {
            animator?.cancel()
            animator = null
            if (isAttachedToWindow && ValueAnimator.areAnimatorsEnabled()) {
                animator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = (halfCycleSeconds * 2 * 1000L)
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
                        Color.argb(120, 216, 227, 207),
                        Color.argb(45, 216, 227, 207),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.58f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            restartAnimator()
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
            val haloRadius = maxRadius * (0.86f + 0.12f * breath)
            canvas.drawCircle(centerX, centerY, haloRadius, haloPaint)

            val arcRadius = maxRadius * (0.72f + 0.04f * breath)
            canvas.drawCircle(centerX, centerY, arcRadius - 4f * density, innerPlatePaint)

            arcRect.set(
                centerX - arcRadius,
                centerY - arcRadius,
                centerX + arcRadius,
                centerY + arcRadius
            )
            canvas.drawArc(arcRect, -15f, 298f, false, trackPaint)
            val activeSweep = 90f + 208f * breath
            canvas.drawArc(arcRect, -15f, activeSweep, false, activeArcPaint)

            val dotAngle = (-46f * (PI / 180.0))
            val dotX = centerX + (arcRadius * cos(dotAngle)).toFloat()
            val dotY = centerY + (arcRadius * sin(dotAngle)).toFloat()
            canvas.drawCircle(dotX, dotY, (4.2f + 1.2f * breath) * density, amberDotPaint)
        }
    }
}
