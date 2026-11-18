package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedSurgeonDao {
    @Query("SELECT * FROM target_app_rules ORDER BY isBuiltIn DESC, displayName ASC")
    fun observeAllRules(): Flow<List<TargetAppRuleEntity>>

    @Query("SELECT * FROM target_app_rules WHERE packageName = :packageName LIMIT 1")
    fun observeRuleByPackage(packageName: String): Flow<TargetAppRuleEntity?>

    @Query("SELECT * FROM target_app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleByPackage(packageName: String): TargetAppRuleEntity?

    @Query("SELECT * FROM target_app_rules")
    suspend fun getAllRulesSnapshot(): List<TargetAppRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: TargetAppRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRules(rules: List<TargetAppRuleEntity>)

    @Update
    suspend fun updateRule(rule: TargetAppRuleEntity)

    @Query("DELETE FROM target_app_rules WHERE packageName = :packageName AND isBuiltIn = 0")
    suspend fun deleteCustomRule(packageName: String)

    @Query("SELECT * FROM intervention_events WHERE timestampMs >= :sinceMs ORDER BY timestampMs DESC")
    fun observeEventsSince(sinceMs: Long): Flow<List<InterventionEventEntity>>

    @Query("SELECT * FROM intervention_events ORDER BY timestampMs DESC LIMIT 200")
    fun observeRecentEvents(): Flow<List<InterventionEventEntity>>

    @Insert
    suspend fun insertEvent(event: InterventionEventEntity): Long

    @Query("UPDATE intervention_events SET wasAllowedOnce = 1 WHERE id = :eventId")
    suspend fun markEventAllowedOnce(eventId: Long)

    @Query("DELETE FROM intervention_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)

    @Query("DELETE FROM intervention_events")
    suspend fun clearAllEvents()
}
