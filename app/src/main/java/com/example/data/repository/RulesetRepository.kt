package com.example.data.repository

import android.content.Context
import com.example.data.local.AppPreferences
import com.example.data.local.FeedSurgeonDao
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.local.TargetAppRuleEntity
import com.example.data.model.InterventionMode
import com.example.data.model.RemoteRulesetPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface RulesetSyncResult {
    data class Success(val version: String, val updatedApps: Int, val fromLocalCache: Boolean) : RulesetSyncResult
    data class Error(val message: String) : RulesetSyncResult
}

class RulesetRepository(
    private val context: Context,
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
    private val syncMutex = Mutex()
    private val localCacheFile: File
        get() = File(context.filesDir, "ruleset_cache.json")

    suspend fun ensureLocalCacheSeeded() = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            if (!localCacheFile.exists()) {
                val assetJson = loadBundledAssetOrFallbackJson()
                val parsed = parseAndValidate(assetJson)
                if (parsed != null) {
                    writeLocalCacheFile(assetJson)
                    applyPayloadToDatabase(parsed)
                    settingsDataStore.saveFetchedRuleset(
                        version = parsed.version,
                        rawJson = assetJson,
                        checkedAtMs = 0L
                    )
                }
            }
        }
    }

    suspend fun autoSyncIfDue(force: Boolean = false): RulesetSyncResult =
        withContext(Dispatchers.IO) {
            val prefs = settingsDataStore.preferencesFlow.first()
            if (!force && !prefs.autoUpdateRules) {
                return@withContext RulesetSyncResult.Success(
                    version = prefs.rulesetVersion,
                    updatedApps = dao.getAllRulesSnapshot().size,
                    fromLocalCache = true
                )
            }
            val now = System.currentTimeMillis()
            val isStale = prefs.lastRulesetCheckMs == 0L ||
                (now - prefs.lastRulesetCheckMs) >= AppPreferences.AUTO_SYNC_INTERVAL_MS
            if (!force && !isStale) {
                return@withContext RulesetSyncResult.Success(
                    version = prefs.rulesetVersion,
                    updatedApps = dao.getAllRulesSnapshot().size,
                    fromLocalCache = true
                )
            }
            syncFromRemoteOrFallback()
        }

    suspend fun syncFromRemoteOrFallback(): RulesetSyncResult = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            val prefs = settingsDataStore.preferencesFlow.first()
            val targetUrl = prefs.rulesetEndpointUrl.trim().ifEmpty { AppPreferences.DEFAULT_GIST_URL }

            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("Accept", "application/json")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withLock applyLocalCacheFallback(
                            prefs = prefs,
                            reason = "Using local ruleset ${prefs.rulesetVersion} (HTTP ${response.code})"
                        )
                    }
                    val body = response.body?.string().orEmpty()
                    val parsed = parseAndValidate(body)
                        ?: return@withLock applyLocalCacheFallback(
                            prefs = prefs,
                            reason = "Remote ruleset format invalid; kept local ${prefs.rulesetVersion}"
                        )

                    writeLocalCacheFile(body)
                    applyPayloadToDatabase(parsed)
                    settingsDataStore.saveFetchedRuleset(
                        version = parsed.version,
                        rawJson = body,
                        checkedAtMs = System.currentTimeMillis()
                    )
                    RulesetSyncResult.Success(
                        version = parsed.version,
                        updatedApps = parsed.apps.size,
                        fromLocalCache = false
                    )
                }
            } catch (_: IOException) {
                applyLocalCacheFallback(
                    prefs = prefs,
                    reason = "Offline \u00B7 using cached ruleset ${prefs.rulesetVersion}"
                )
            } catch (e: Exception) {
                RulesetSyncResult.Error("Ruleset check failed: ${e.message ?: "unknown error"}")
            }
        }
    }

    private suspend fun applyLocalCacheFallback(
        prefs: AppPreferences,
        reason: String
    ): RulesetSyncResult {
        val cachedJson = when {
            localCacheFile.exists() -> localCacheFile.readText()
            else -> loadBundledAssetOrFallbackJson()
        }
        val parsed = parseAndValidate(cachedJson)
        return if (parsed != null) {
            writeLocalCacheFile(cachedJson)
            applyPayloadToDatabase(parsed)
            settingsDataStore.saveFetchedRuleset(
                version = parsed.version,
                rawJson = cachedJson,
                checkedAtMs = System.currentTimeMillis()
            )
            RulesetSyncResult.Success(
                version = parsed.version,
                updatedApps = parsed.apps.size,
                fromLocalCache = true
            )
        } else {
            settingsDataStore.updateLastCheckedOnly(System.currentTimeMillis())
            RulesetSyncResult.Error(reason)
        }
    }

    fun loadBundledAssetOrFallbackJson(): String {
        return try {
            context.assets.open("ruleset.json").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            AppPreferences.BUNDLED_RULESET_JSON
        }
    }

    private fun writeLocalCacheFile(rawJson: String) {
        try {
            localCacheFile.writeText(rawJson)
        } catch (_: Exception) {
        }
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
                displayName = remote.displayName.ifBlank { prev?.displayName ?: remote.packageName },
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
                isBuiltIn = prev?.isBuiltIn ?: (remote.packageName in FeedSurgeonDatabase.BUILT_IN_PACKAGE_NAMES),
                matchedSelectorCount = remote.viewIds.size,
                totalSelectorCount = remote.viewIds.size,
                isOutdated = false
            )
        }
        dao.upsertRules(updated)
    }
}
