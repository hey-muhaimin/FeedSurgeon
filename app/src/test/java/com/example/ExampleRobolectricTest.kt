package com.example

import android.content.Context
import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.data.local.AppPreferences
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.model.InspectedNode
import com.example.data.model.InterventionMode
import com.example.data.model.LiveTreeSnapshot
import com.example.data.repository.RulesetRepository
import com.example.data.repository.RulesetSyncResult
import com.example.service.AccessibilityEventLogger
import com.example.service.InstalledAppDetector
import com.example.service.SelectorMatcher
import com.example.worker.RulesetSyncWorker
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name and privacy url from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Feed Surgeon", context.getString(R.string.app_name))
        assertEquals("https://suprbuild.com/privacy", context.getString(R.string.privacy_policy_url))
    }

    @Test
    fun `default built-in rules use overlay shield mode`() {
        val defaults = FeedSurgeonDatabase.defaultBuiltInRules()
        assertEquals(5, defaults.size)
        assertTrue(defaults.all { it.interventionMode == InterventionMode.OVERLAY })
    }

    @Test
    fun `selector matcher detects youtube shorts in snapshot and skips when snoozed`() {
        val ytRule = FeedSurgeonDatabase.defaultBuiltInRules().first {
            it.packageName == "com.google.android.youtube"
        }
        val snapshot = LiveTreeSnapshot(
            packageName = "com.google.android.youtube",
            appDisplayName = "YouTube",
            capturedAtMs = System.currentTimeMillis(),
            isDiagnosticSample = true,
            nodes = listOf(
                InspectedNode(0, "FrameLayout", null, null, isSelected = false),
                InspectedNode(
                    1,
                    "RecyclerView",
                    "com.google.android.youtube:id/reel_recycler",
                    null,
                    isSelected = false,
                    isMatched = true
                ),
                InspectedNode(2, "TabView", null, "Shorts", isSelected = true, isMatched = true)
            )
        )

        val matched = SelectorMatcher.matchSelectorInSnapshot(snapshot, ytRule)
        assertEquals("com.google.android.youtube:id/reel_recycler", matched)

        val snoozedRule = ytRule.copy(snoozeUntilMs = System.currentTimeMillis() + 60_000L)
        assertNull(SelectorMatcher.matchSelectorInSnapshot(snapshot, snoozedRule))
    }

    @Test
    fun `ruleset repository parses all five bundled apps and seeds local cache`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = FeedSurgeonDatabase.getInstance(context)
        val settings = SettingsDataStore(context)
        val repo = RulesetRepository(context, db.feedSurgeonDao(), settings)

        val parsed = repo.parseAndValidate(AppPreferences.BUNDLED_RULESET_JSON)
        assertNotNull(parsed)
        assertEquals("v2026.11.18", parsed!!.version)
        val packages = parsed.apps.map { it.packageName }.toSet()
        assertTrue(packages.containsAll(FeedSurgeonDatabase.BUILT_IN_PACKAGE_NAMES))
        assertEquals(5, packages.size)

        val cacheFile = File(context.filesDir, "ruleset_cache.json")
        cacheFile.delete()
        repo.ensureLocalCacheSeeded()
        assertTrue(cacheFile.exists())
        val cachedParsed = repo.parseAndValidate(cacheFile.readText())
        assertNotNull(cachedParsed)
        assertEquals(5, cachedParsed!!.apps.size)
    }

    @Test
    fun `autoSyncIfDue uses local cache when autoUpdateRules is disabled`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = FeedSurgeonDatabase.getInstance(context)
        val settings = SettingsDataStore(context)
        settings.setAutoUpdateRules(false)
        val repo = RulesetRepository(context, db.feedSurgeonDao(), settings)

        repo.ensureLocalCacheSeeded()
        val result = repo.autoSyncIfDue(force = false)
        assertTrue(result is RulesetSyncResult.Success)
        val success = result as RulesetSyncResult.Success
        assertTrue(success.fromLocalCache)
        assertEquals(5, success.updatedApps)
    }

    @Test
    fun `installed app detector finds installed packages accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowPm = shadowOf(context.packageManager)
        shadowPm.installPackage(PackageInfo().apply { packageName = "com.google.android.youtube" })

        val installed = InstalledAppDetector.getInstalledPackages(
            context,
            FeedSurgeonDatabase.BUILT_IN_PACKAGE_NAMES
        )
        assertTrue("com.google.android.youtube" in installed)
        assertFalse("com.instagram.android" in installed)
    }

    @Test
    fun `accessibility event logger writes reads and clears local file`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val logger = AccessibilityEventLogger(context)
        logger.clear()
        assertEquals(0L, logger.getFileSizeBytes())

        logger.logEvent(
            packageName = "com.google.android.youtube",
            eventType = 32,
            eventClassName = "com.google.android.apps.youtube.app.WatchWhileActivity",
            rootNode = null,
            matchedSelector = "com.google.android.youtube:id/reel_recycler",
            outcome = "TRIGGERED:OVERLAY"
        )

        val tail = logger.readTail(20)
        assertTrue(tail.contains("pkg=com.google.android.youtube"))
        assertTrue(tail.contains("match=com.google.android.youtube:id/reel_recycler"))
        assertTrue(tail.contains("outcome=TRIGGERED:OVERLAY"))
        assertTrue(logger.getFileSizeBytes() > 0L)

        logger.clear()
        assertEquals("", logger.readTail(20))
    }

    @Test
    fun `ruleset sync worker handles autoUpdateRules disabled cleanly`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val settings = SettingsDataStore(context)
        settings.setAutoUpdateRules(false)

        RulesetSyncWorker.schedulePeriodicSync(context)
        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(RulesetSyncWorker.UNIQUE_PERIODIC_WORK_NAME)
            .get()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.first().state)

        val worker = TestListenableWorkerBuilder<RulesetSyncWorker>(context).build()
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }
}
