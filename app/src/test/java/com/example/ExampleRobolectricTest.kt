package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppPreferences
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.repository.DiagnosticsBridge
import com.example.data.repository.RulesetRepository
import com.example.service.SelectorMatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Feed Surgeon", appName)
    }

    @Test
    fun `selector matcher detects youtube shorts in snapshot and skips when snoozed`() {
        val ytRule = FeedSurgeonDatabase.defaultBuiltInRules().first {
            it.packageName == "com.google.android.youtube"
        }
        val snapshot = DiagnosticsBridge.createDiagnosticSampleSnapshot("com.google.android.youtube")

        val matched = SelectorMatcher.matchSelectorInSnapshot(snapshot, ytRule)
        assertEquals("com.google.android.youtube:id/reel_recycler", matched)

        val snoozedRule = ytRule.copy(snoozeUntilMs = System.currentTimeMillis() + 60_000L)
        assertNull(SelectorMatcher.matchSelectorInSnapshot(snapshot, snoozedRule))
    }

    @Test
    fun `ruleset repository parses bundled remote json payload`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = FeedSurgeonDatabase.getInstance(context)
        val repo = RulesetRepository(db.feedSurgeonDao(), SettingsDataStore(context))

        val parsed = repo.parseAndValidate(AppPreferences.BUNDLED_RULESET_JSON)
        assertNotNull(parsed)
        assertEquals("v2026.11.18", parsed!!.version)
        assertTrue(parsed.apps.any { it.packageName == "com.google.android.youtube" })
        assertTrue(parsed.apps.any { it.packageName == "com.instagram.android" })
    }
}
