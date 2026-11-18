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
    val rulesetVersion: String = DEFAULT_RULESET_VERSION,
    val rulesetEndpointUrl: String = DEFAULT_GIST_URL,
    val lastRulesetCheckMs: Long = 0L,
    val reclaimedSecondsPerCut: Int = 165,
    val showInterventionBanner: Boolean = true,
    val cachedRulesetJson: String = BUNDLED_RULESET_JSON
) {
    companion object {
        const val DEFAULT_RULESET_VERSION = "v2026.11.18"
        const val DEFAULT_GIST_URL =
            "https://gist.githubusercontent.com/hey-muhaimin/1ebadd94effdba1255d42661ef218fc9/raw/8df1bb113c19da45adc96e7f9d213662522bf58d/ruleset.json"

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
        val RULESET_VERSION = stringPreferencesKey("ruleset_version")
        val RULESET_ENDPOINT_URL = stringPreferencesKey("ruleset_endpoint_url")
        val LAST_RULESET_CHECK_MS = longPreferencesKey("last_ruleset_check_ms")
        val RECLAIMED_SECONDS_PER_CUT = intPreferencesKey("reclaimed_seconds_per_cut")
        val SHOW_INTERVENTION_BANNER = booleanPreferencesKey("show_intervention_banner")
        val CACHED_RULESET_JSON = stringPreferencesKey("cached_ruleset_json")
    }

    val preferencesFlow: Flow<AppPreferences> = context.feedSurgeonDataStore.data.map { prefs ->
        AppPreferences(
            disclosureAccepted = prefs[Keys.DISCLOSURE_ACCEPTED] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            rulesetVersion = prefs[Keys.RULESET_VERSION] ?: AppPreferences.DEFAULT_RULESET_VERSION,
            rulesetEndpointUrl = prefs[Keys.RULESET_ENDPOINT_URL] ?: AppPreferences.DEFAULT_GIST_URL,
            lastRulesetCheckMs = prefs[Keys.LAST_RULESET_CHECK_MS] ?: 0L,
            reclaimedSecondsPerCut = prefs[Keys.RECLAIMED_SECONDS_PER_CUT] ?: 165,
            showInterventionBanner = prefs[Keys.SHOW_INTERVENTION_BANNER] ?: true,
            cachedRulesetJson = prefs[Keys.CACHED_RULESET_JSON] ?: AppPreferences.BUNDLED_RULESET_JSON
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

    suspend fun setRulesetEndpointUrl(url: String) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.RULESET_ENDPOINT_URL] = url.trim()
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

    suspend fun saveFetchedRuleset(version: String, rawJson: String, checkedAtMs: Long) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.RULESET_VERSION] = version
            prefs[Keys.CACHED_RULESET_JSON] = rawJson
            prefs[Keys.LAST_RULESET_CHECK_MS] = checkedAtMs
        }
    }

    suspend fun updateLastCheckedOnly(checkedAtMs: Long) {
        context.feedSurgeonDataStore.edit { prefs ->
            prefs[Keys.LAST_RULESET_CHECK_MS] = checkedAtMs
        }
    }
}
