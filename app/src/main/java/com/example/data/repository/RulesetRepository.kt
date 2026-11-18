package com.example.data.repository

import com.example.data.local.AppPreferences
import com.example.data.local.FeedSurgeonDao
import com.example.data.local.SettingsDataStore
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.data.model.RemoteRulesetPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface RulesetSyncResult {
    data class Success(val version: String, val updatedApps: Int, val fromBundledFallback: Boolean) : RulesetSyncResult
    data class Error(val message: String) : RulesetSyncResult
}

class RulesetRepository(
    private val dao: FeedSurgeonDao,
    private val settingsDataStore: SettingsDataStore,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build(),
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
) {
    private val adapter = moshi.adapter(RemoteRulesetPayload::class.java)

    suspend fun syncFromRemoteOrFallback(customUrl: String? = null): RulesetSyncResult =
        withContext(Dispatchers.IO) {
            val prefs = settingsDataStore.preferencesFlow.first()
            val targetUrl = (customUrl ?: prefs.rulesetEndpointUrl).trim()
            if (targetUrl.isEmpty() || (!targetUrl.startsWith("https://") && !targetUrl.startsWith("http://"))) {
                return@withContext RulesetSyncResult.Error("Enter a valid https:// GitHub Gist or raw JSON URL.")
            }

            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("Accept", "application/json")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        settingsDataStore.updateLastCheckedOnly(System.currentTimeMillis())
                        return@withContext RulesetSyncResult.Error(
                            "Endpoint returned HTTP ${response.code}. Keeping cached ruleset ${prefs.rulesetVersion}."
                        )
                    }
                    val body = response.body?.string().orEmpty()
                    val parsed = parseAndValidate(body)
                        ?: return@withContext RulesetSyncResult.Error("Remote JSON did not match ruleset schema.")

                    applyPayloadToDatabase(parsed)
                    settingsDataStore.saveFetchedRuleset(
                        version = parsed.version,
                        rawJson = body,
                        checkedAtMs = System.currentTimeMillis()
                    )
                    RulesetSyncResult.Success(
                        version = parsed.version,
                        updatedApps = parsed.apps.size,
                        fromBundledFallback = false
                    )
                }
            } catch (e: IOException) {
                // If user kept the placeholder Gist URL offline or unreachable, fall back cleanly to bundled JSON
                if (targetUrl == AppPreferences.DEFAULT_GIST_URL) {
                    val bundled = parseAndValidate(AppPreferences.BUNDLED_RULESET_JSON)
                    if (bundled != null) {
                        applyPayloadToDatabase(bundled)
                        settingsDataStore.saveFetchedRuleset(
                            version = bundled.version,
                            rawJson = AppPreferences.BUNDLED_RULESET_JSON,
                            checkedAtMs = System.currentTimeMillis()
                        )
                        return@withContext RulesetSyncResult.Success(
                            version = bundled.version,
                            updatedApps = bundled.apps.size,
                            fromBundledFallback = true
                        )
                    }
                }
                settingsDataStore.updateLastCheckedOnly(System.currentTimeMillis())
                RulesetSyncResult.Error(
                    "Network unreachable (${e.message ?: "offline"}). Keeping cached ruleset ${prefs.rulesetVersion}."
                )
            } catch (e: Exception) {
                RulesetSyncResult.Error("Failed to parse ruleset: ${e.message ?: "unknown error"}")
            }
        }

    suspend fun restoreBundledRuleset(): RulesetSyncResult = withContext(Dispatchers.IO) {
        val parsed = parseAndValidate(AppPreferences.BUNDLED_RULESET_JSON)
            ?: return@withContext RulesetSyncResult.Error("Bundled ruleset invalid.")
        applyPayloadToDatabase(parsed)
        settingsDataStore.saveFetchedRuleset(
            version = parsed.version,
            rawJson = AppPreferences.BUNDLED_RULESET_JSON,
            checkedAtMs = System.currentTimeMillis()
        )
        RulesetSyncResult.Success(
            version = parsed.version,
            updatedApps = parsed.apps.size,
            fromBundledFallback = true
        )
    }

    fun parseAndValidate(rawJson: String): RemoteRulesetPayload? {
        return try {
            val payload = adapter.fromJson(rawJson) ?: return null
            if (payload.version.isBlank() || payload.apps.isEmpty()) return null
            if (payload.apps.any { it.packageName.isBlank() || it.viewIds.isEmpty() }) return null
            payload
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun applyPayloadToDatabase(payload: RemoteRulesetPayload) {
        val existingByPkg = dao.getAllRulesSnapshot().associateBy { it.packageName }
        val updated = payload.apps.map { remote ->
            val prev = existingByPkg[remote.packageName]
            TargetAppRuleEntity(
                packageName = remote.packageName,
                displayName = remote.displayName,
                feedLabel = remote.feedLabel,
                enabled = prev?.enabled ?: true,
                interventionMode = prev?.interventionMode ?: if (remote.packageName == "com.google.android.youtube") {
                    InterventionMode.HOME_TAB
                } else {
                    InterventionMode.BACK
                },
                cooldownMs = prev?.cooldownMs ?: 800,
                homeTabLabel = remote.homeTabLabel.ifBlank { "Home" },
                viewIdsRaw = remote.viewIds.joinToString("|"),
                tabFallbackLabel = remote.tabContentDescription,
                snoozeUntilMs = prev?.snoozeUntilMs ?: 0L,
                allowOnceUntilMs = prev?.allowOnceUntilMs ?: 0L,
                isBuiltIn = prev?.isBuiltIn ?: (remote.packageName == "com.google.android.youtube" || remote.packageName == "com.instagram.android"),
                matchedSelectorCount = remote.viewIds.size,
                totalSelectorCount = remote.viewIds.size,
                isOutdated = false
            )
        }
        dao.upsertRules(updated)
    }
}
