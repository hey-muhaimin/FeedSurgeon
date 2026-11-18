package com.example.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class AccessibilityOverlayShield(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null

    fun isShowing(): Boolean = overlayView != null

    fun show(
        appName: String,
        feedLabel: String,
        onExitFeed: () -> Unit,
        onAllowOnce: () -> Unit
    ) {
        mainHandler.post {
            if (overlayView != null) return@post

            val density = context.resources.displayMetrics.density
            fun dp(value: Int): Int = (value * density).toInt()

            val root = FrameLayout(context).apply {
                setBackgroundColor(Color.parseColor("#E60A0F0E"))
                setPadding(dp(24), dp(24), dp(24), dp(24))
                isFocusable = true
                isFocusableInTouchMode = true
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

            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(24), dp(24), dp(24), dp(24))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#131918"))
                    cornerRadius = dp(20).toFloat()
                    setStroke(dp(1), Color.parseColor("#232D2B"))
                }
            }

            val badge = TextView(context).apply {
                text = "MINDFUL PAUSE"
                textSize = 11f
                setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
                setTextColor(Color.parseColor("#34D3A8"))
                letterSpacing = 0.08f
            }

            val title = TextView(context).apply {
                text = "Take a quick breath"
                textSize = 22f
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(Color.parseColor("#F3F6F5"))
                setPadding(0, dp(10), 0, dp(8))
            }

            val body = TextView(context).apply {
                text = "$appName $feedLabel opened. Step back to stay intentional, or take a 5-minute pass if you meant to watch something specific."
                textSize = 14f
                setTextColor(Color.parseColor("#8B9794"))
                setPadding(0, 0, 0, dp(20))
            }

            val exitButton = Button(context).apply {
                text = "Leave $feedLabel"
                isAllCaps = false
                textSize = 15f
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextColor(Color.parseColor("#061B15"))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#34D3A8"))
                    cornerRadius = dp(100).toFloat()
                }
                minimumHeight = dp(48)
                setOnClickListener {
                    dismiss()
                    onExitFeed()
                }
            }

            val allowButton = Button(context).apply {
                text = "Allow for 5 minutes"
                isAllCaps = false
                textSize = 14f
                setTextColor(Color.parseColor("#F3F6F5"))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#192220"))
                    cornerRadius = dp(100).toFloat()
                    setStroke(dp(1), Color.parseColor("#232D2B"))
                }
                minimumHeight = dp(48)
                setOnClickListener {
                    dismiss()
                    onAllowOnce()
                }
            }

            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(1, dp(10))
            }

            card.addView(badge)
            card.addView(title)
            card.addView(body)
            card.addView(exitButton)
            card.addView(spacer)
            card.addView(allowButton)

            val cardParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            root.addView(card, cardParams)

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
                root.requestFocus()
            } catch (_: Exception) {
                overlayView = null
            }
        }
    }

    fun dismiss() {
        mainHandler.post {
            val view = overlayView ?: return@post
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
            } finally {
                overlayView = null
            }
        }
    }
}
