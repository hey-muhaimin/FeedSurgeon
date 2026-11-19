package com.example.data.model

enum class InterventionMode(
    val shortLabel: String,
    val subtitleLabel: String,
    val explanation: String
) {
    BACK(
        shortLabel = "Go back",
        subtitleLabel = "Gently go back",
        explanation = "Automatically steps back out of the short-form feed as soon as it opens."
    ),
    HOME_TAB(
        shortLabel = "Home tab",
        subtitleLabel = "Switch to Home",
        explanation = "Returns you to the app's main Home tab. Best when opening links from friends."
    ),
    OVERLAY(
        shortLabel = "Calm pause",
        subtitleLabel = "Calm reminder",
        explanation = "Shows a calm full-screen breathing pause so you can step away intentionally."
    )
}
