package com.example.data.model

enum class ThemeMode(val label: String) {
    SYSTEM("Auto"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromStored(value: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: LIGHT
    }
}
