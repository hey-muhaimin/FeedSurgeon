package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.feedSurgeonDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "feed_surgeon_prefs"
)

data class AppPreferences(
    val disclosureAccepted: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val autoUpdateRules: Boolean = true,
    val eventCaptureEnabled: Boolean = false,
    val rulesetVersion: String = DEFAULT_RULESET_VERSION,
    val rulesetEndpointUrl: String = DEFAULT_GIST_URL,
    val rulesetEtag: String = "",
    val lastRulesetCheckMs: Long = 0L,
    val reclaimedSecondsPerCut: Int = 165,
    val showInterventionBanner: Boolean = true,
    val cachedRulesetJson: String = BUNDLED_RULESET_JSON,
    val globalPauseUntilMs: Long = 0L
) {
    fun isGloballyPaused(nowMs: Long = System.currentTimeMillis()): Boolean =
        globalPauseUntilMs == Long.MAX_VALUE || globalPauseUntilMs > nowMs
    companion object {
        const val DEFAULT_RULESET_VERSION = "v2026.11.18"
        const val DEFAULT_GIST_URL =
            "https://gist.githubusercontent.com/hey-muhaimin/1ebadd94effdba1255d42661ef218fc9/raw/ruleset.json"
        const val AUTO_SYNC_INTERVAL_MS = 6L * 60L * 60L * 1000L

        val BUNDLED_RULESET_JSON = """
            {
              "version": "v2026.11.18",
              "updatedAt": "2026-11-18",
              "apps": [
                {
                  "packageName": "com.google.android.youtube",
                  "displayName": "YouTube",
                  "feedLabel": "Shorts",
                  "homeTabLabel": "Home",
                  "viewIds": [
                    "com.google.android.youtube:id/reel_recycler",
                    "com.google.android.youtube:id/reel_watch_fragment_root",
                    "com.google.android.youtube:id/shorts_player"
                  ],
                  "tabContentDescription": "Shorts"
                },
                {
                  "packageName": "com.instagram.android",
                  "displayName": "Instagram",
                  "feedLabel": "Reels",
                  "homeTabLabel": "Home",
                  "viewIds": [
                    "com.instagram.android:id/clips_viewer_view_pager",
                    "com.instagram.android:id/clips_video_container"
                  ],
                  "tabContentDescription": "Reels"
                },
                {
                  "packageName": "com.facebook.katana",
                  "displayName": "Facebook",
                  "feedLabel": "Reels",
                  "homeTabLabel": "Home",
                  "viewIds": [
                    "com.facebook.katana:id/reels_viewer_root",
                    "com.facebook.katana:id/fb_reels_container"
                  ],
                  "tabContentDescription": "Reels"
                },
                {
                  "packageName": "com.snapchat.android",
                  "displayName": "Snapchat",
                  "feedLabel": "Spotlight",
                  "homeTabLabel": "Chat",
                  "viewIds": [
                    "com.snapchat.android:id/spotlight_feed_container",
                    "com.snapchat.android:id/spotlight_opera_view"
                  ],
                  "tabContentDescription": "Spotlight"
                },
                {
                  "packageName": "com.reddit.frontpage",
                  "displayName": "Reddit",
                  "feedLabel": "Watch",
                  "homeTabLabel": "Home",
                  "viewIds": [
                    "com.reddit.frontpage:id/video_player_root",
                    "com.reddit.frontpage:id/watch_feed_recycler"
                  ],
                  "tabContentDescription": "Watch"
                }
              ]
            }
        """.trimIndent()
    }
}

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val DISCLOSURE_ACCEPTED = booleanPreferencesKey("disclosure_accepted")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val AUTO_UPDATE_RULES = booleanPreferencesKey("auto_update_rules")
        val EVENT_CAPTURE_ENABLED = booleanPreferencesKey("event_capture_enabled")
        val RULESET_VERSION = stringPreferencesKey("ruleset_version")
        val RULESET_ENDPOINT_URL = stringPreferencesKey("ruleset_endpoint_url")
        val RULESET_ETAG = stringPreferencesKey("ruleset_etag")
        val LAST_RULESET_CHECK_MS = longPreferencesKey("last_ruleset_check_ms")
        val RECLAIMED_SECONDS_PER_CUT = intPreferencesKey("reclaimed_seconds_per_cut")
        val SHOW_INTERVENTION_BANNER = booleanPreferencesKey("show_intervention_banner")
        val CACHED_RULESET_JSON = stringPreferencesKey("cached_ruleset_json")
        val GLOBAL_PAUSE_UNTIL_MS = longPreferencesKey("global_pause_until_ms")
    }

    val preferencesFlow: Flow<AppPreferences> = context.feedSurgeonDataStore.data.map { prefs ->
        AppPreferences(
            disclosureAccepted = prefs[Keys.DISCLOSURE_ACCEPTED] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            autoUpdateRules = prefs[Keys.AUTO_UPDATE_RULES] ?: true,
            eventCaptureEnabled = prefs[Keys.EVENT_CAPTURE_ENABLED] ?: false,
            rulesetVersion = prefs[Keys.RULESET_VERSION] ?: AppPreferences.DEFAULT_RULESET_VERSION,
            rulesetEndpointUrl = prefs[Keys.RULESET_ENDPOINT_URL] ?: AppPreferences.DEFAULT_GIST_URL,
            rulesetEtag = prefs[Keys.RULESET_ETAG] ?: "",
            lastRulesetCheckMs = prefs[Keys.LAST_RULESET_CHECK_MS] ?: 0L,
            reclaimedSecondsPerCut = prefs[Keys.RECLAIMED_SECONDS_PER_CUT] ?: 165,
            showInterventionBanner = prefs[Keys.SHOW_INTERVENTION_BANNER] ?: true,
            cachedRulesetJson = prefs[Keys.CACHED_RULESET_JSON] ?: AppPreferences.BUNDLED_RULESET_JSON,
            globalPauseUntilMs = prefs[Keys.GLOBAL_PAUSE_UNTIL_MS] ?: 0L
        )
    }

    suspend fun setDisclosureAccepted(accepted: Boolean) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.DISCLOSURE_ACCEPTED] = accepted
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setAutoUpdateRules(enabled: Boolean) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.AUTO_UPDATE_RULES] = enabled
        }
    }

    suspend fun setEventCaptureEnabled(enabled: Boolean) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.EVENT_CAPTURE_ENABLED] = enabled
        }
    }

    suspend fun setReclaimedSecondsPerCut(seconds: Int) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.RECLAIMED_SECONDS_PER_CUT] = seconds.coerceIn(30, 600)
        }
    }

    suspend fun setShowInterventionBanner(show: Boolean) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.SHOW_INTERVENTION_BANNER] = show
        }
    }

    suspend fun setGlobalPauseUntilMs(untilMs: Long) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.GLOBAL_PAUSE_UNTIL_MS] = untilMs
        }
    }

    suspend fun saveFetchedRuleset(
        version: String,
        rawJson: String,
        checkedAtMs: Long,
        etag: String? = null
    ) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.RULESET_VERSION] = version
            prefs[Keys.CACHED_RULESET_JSON] = rawJson
            prefs[Keys.LAST_RULESET_CHECK_MS] = checkedAtMs
            if (etag != null) {
                prefs[Keys.RULESET_ETAG] = etag
            }
        }
    }

    suspend fun updateLastCheckedOnly(checkedAtMs: Long, etag: String? = null) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.LAST_RULESET_CHECK_MS] = checkedAtMs
            if (etag != null) {
                prefs[Keys.RULESET_ETAG] = etag
            }
        }
    }
}
