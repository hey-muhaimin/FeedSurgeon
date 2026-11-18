package com.example.data.model

enum class InterventionMode(
    val shortLabel: String,
    val subtitleLabel: String,
    val explanation: String
) {
    BACK(
        shortLabel = "Back",
        subtitleLabel = "Press Back",
        explanation = "Triggers Android's global Back action immediately when the short-form feed opens."
    ),
    HOME_TAB(
        shortLabel = "Home tab",
        subtitleLabel = "Go to Home tab",
        explanation = "Taps Home inside the app. Safest for cold launches from links."
    ),
    OVERLAY(
        shortLabel = "Overlay",
        subtitleLabel = "Show shield overlay",
        explanation = "Covers the short-form feed with a native accessibility shield requiring no extra permission."
    )
}
