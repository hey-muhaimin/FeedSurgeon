package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.InterventionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TargetAppRuleEntity::class, InterventionEventEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(FeedSurgeonConverters::class)
abstract class FeedSurgeonDatabase : RoomDatabase() {
    abstract fun feedSurgeonDao(): FeedSurgeonDao

    companion object {
        @Volatile
        private var INSTANCE: FeedSurgeonDatabase? = null

        val BUILT_IN_PACKAGE_NAMES = setOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.reddit.frontpage"
        )

        fun defaultBuiltInRules(): List<TargetAppRuleEntity> = listOf(
            TargetAppRuleEntity(
                packageName = "com.google.android.youtube",
                displayName = "YouTube",
                feedLabel = "Shorts",
                enabled = true,
                interventionMode = InterventionMode.HOME_TAB,
                cooldownMs = 800,
                homeTabLabel = "Home",
                viewIdsRaw = listOf(
                    "com.google.android.youtube:id/reel_recycler",
                    "com.google.android.youtube:id/reel_watch_fragment_root",
                    "com.google.android.youtube:id/shorts_player"
                ).joinToString("|"),
                tabFallbackLabel = "Shorts",
                isBuiltIn = true,
                matchedSelectorCount = 3,
                totalSelectorCount = 3,
                isOutdated = false
            ),
            TargetAppRuleEntity(
                packageName = "com.instagram.android",
                displayName = "Instagram",
                feedLabel = "Reels",
                enabled = true,
                interventionMode = InterventionMode.BACK,
                cooldownMs = 800,
                homeTabLabel = "Home",
                viewIdsRaw = listOf(
                    "com.instagram.android:id/clips_viewer_view_pager",
                    "com.instagram.android:id/clips_video_container"
                ).joinToString("|"),
                tabFallbackLabel = "Reels",
                isBuiltIn = true,
                matchedSelectorCount = 2,
                totalSelectorCount = 2,
                isOutdated = false
            ),
            TargetAppRuleEntity(
                packageName = "com.facebook.katana",
                displayName = "Facebook",
                feedLabel = "Reels",
                enabled = true,
                interventionMode = InterventionMode.BACK,
                cooldownMs = 800,
                homeTabLabel = "Home",
                viewIdsRaw = listOf(
                    "com.facebook.katana:id/reels_viewer_root",
                    "com.facebook.katana:id/fb_reels_container"
                ).joinToString("|"),
                tabFallbackLabel = "Reels",
                isBuiltIn = true,
                matchedSelectorCount = 2,
                totalSelectorCount = 2,
                isOutdated = false
            ),
            TargetAppRuleEntity(
                packageName = "com.snapchat.android",
                displayName = "Snapchat",
                feedLabel = "Spotlight",
                enabled = true,
                interventionMode = InterventionMode.BACK,
                cooldownMs = 800,
                homeTabLabel = "Chat",
                viewIdsRaw = listOf(
                    "com.snapchat.android:id/spotlight_feed_container",
                    "com.snapchat.android:id/spotlight_opera_view"
                ).joinToString("|"),
                tabFallbackLabel = "Spotlight",
                isBuiltIn = true,
                matchedSelectorCount = 2,
                totalSelectorCount = 2,
                isOutdated = false
            ),
            TargetAppRuleEntity(
                packageName = "com.reddit.frontpage",
                displayName = "Reddit",
                feedLabel = "Watch",
                enabled = true,
                interventionMode = InterventionMode.BACK,
                cooldownMs = 800,
                homeTabLabel = "Home",
                viewIdsRaw = listOf(
                    "com.reddit.frontpage:id/video_player_root",
                    "com.reddit.frontpage:id/watch_feed_recycler"
                ).joinToString("|"),
                tabFallbackLabel = "Watch",
                isBuiltIn = true,
                matchedSelectorCount = 2,
                totalSelectorCount = 2,
                isOutdated = false
            )
        )

        fun getInstance(context: Context): FeedSurgeonDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): FeedSurgeonDatabase {
            return Room.databaseBuilder(
                appContext,
                FeedSurgeonDatabase::class.java,
                "feed_surgeon.db"
            ).addCallback(
                object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(appContext).feedSurgeonDao().upsertRules(defaultBuiltInRules())
                        }
                    }
                }
            ).build()
        }
    }
}
