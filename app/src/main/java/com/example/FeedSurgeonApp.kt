package com.example

import android.app.Application
import com.example.data.local.FeedSurgeonDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.repository.InterventionRepository
import com.example.data.repository.RulesetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FeedSurgeonApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: FeedSurgeonDatabase by lazy {
        FeedSurgeonDatabase.getInstance(this)
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(this)
    }

    val interventionRepository: InterventionRepository by lazy {
        InterventionRepository(database.feedSurgeonDao(), settingsDataStore)
    }

    val rulesetRepository: RulesetRepository by lazy {
        RulesetRepository(this, database.feedSurgeonDao(), settingsDataStore)
    }

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            interventionRepository.ensureDefaultRulesSeeded()
            rulesetRepository.ensureLocalCacheSeeded()
            rulesetRepository.autoSyncIfDue(force = false)
        }
    }
}
