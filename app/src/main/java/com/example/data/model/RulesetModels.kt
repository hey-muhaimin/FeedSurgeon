package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class RemoteRulesetPayload(
    val version: String,
    val updatedAt: String = "",
    val apps: List<RemoteAppSelectorRule>
)

@JsonClass(generateAdapter = false)
data class RemoteAppSelectorRule(
    val packageName: String,
    val displayName: String,
    val feedLabel: String,
    val homeTabLabel: String = "Home",
    val viewIds: List<String>,
    val tabContentDescription: String = ""
)
