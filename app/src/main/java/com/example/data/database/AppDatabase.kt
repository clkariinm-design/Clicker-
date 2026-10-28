package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.database.dao.ActionDao
import com.example.data.database.dao.GestureDao
import com.example.data.database.dao.LogDao
import com.example.data.database.dao.ProfileDao
import com.example.data.database.dao.ScheduleDao
import com.example.data.database.entity.ActionEntity
import com.example.data.database.entity.LogEntity
import com.example.data.database.entity.ProfileEntity
import com.example.data.database.entity.RecordedGestureEntity
import com.example.data.database.entity.ScheduleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProfileEntity::class,
        ActionEntity::class,
        LogEntity::class,
        ScheduleEntity::class,
        RecordedGestureEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun actionDao(): ActionDao
    abstract fun logDao(): LogDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun gestureDao(): GestureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_autoclicker.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate starter profiles on first launch
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                populateInitialData(database)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val p1Id = database.profileDao().insertProfile(
                ProfileEntity(
                    name = "Quick Rapid Clicker",
                    mode = "SINGLE_TARGET",
                    clickIntervalMs = 80L,
                    startDelayMs = 500L,
                    repeatCount = 100,
                    isInfinite = true,
                    randomJitterRadius = 4
                )
            )
            database.actionDao().insertAction(
                ActionEntity(
                    profileId = p1Id,
                    orderIndex = 0,
                    type = "CLICK",
                    x = 540,
                    y = 960,
                    delayBeforeMs = 80,
                    durationMs = 30,
                    label = "Rapid Target #1"
                )
            )

            val p2Id = database.profileDao().insertProfile(
                ProfileEntity(
                    name = "Daily Quest & Scroll Macro",
                    mode = "MULTI_TARGET",
                    clickIntervalMs = 250L,
                    startDelayMs = 1000L,
                    repeatCount = 5,
                    isInfinite = false,
                    randomJitterRadius = 0
                )
            )
            database.actionDao().insertActions(
                listOf(
                    ActionEntity(
                        profileId = p2Id,
                        orderIndex = 0,
                        type = "CLICK",
                        x = 540,
                        y = 600,
                        delayBeforeMs = 300,
                        durationMs = 40,
                        label = "Tap Start Mission"
                    ),
                    ActionEntity(
                        profileId = p2Id,
                        orderIndex = 1,
                        type = "WAIT",
                        delayBeforeMs = 1200,
                        label = "Wait for Loading Screen"
                    ),
                    ActionEntity(
                        profileId = p2Id,
                        orderIndex = 2,
                        type = "SWIPE",
                        x = 540,
                        y = 1400,
                        endX = 540,
                        endY = 400,
                        delayBeforeMs = 200,
                        durationMs = 350,
                        label = "Scroll Content Up"
                    ),
                    ActionEntity(
                        profileId = p2Id,
                        orderIndex = 3,
                        type = "CLICK",
                        x = 800,
                        y = 1250,
                        delayBeforeMs = 500,
                        durationMs = 40,
                        label = "Tap Collect Reward"
                    )
                )
            )

            val p3Id = database.profileDao().insertProfile(
                ProfileEntity(
                    name = "Smart OCR Bonus Collector",
                    mode = "VISUAL_FLOW",
                    clickIntervalMs = 300L,
                    startDelayMs = 1000L,
                    repeatCount = 10,
                    isInfinite = false,
                    randomJitterRadius = 0
                )
            )
            database.actionDao().insertActions(
                listOf(
                    ActionEntity(
                        profileId = p3Id,
                        orderIndex = 0,
                        type = "CONDITION_TEXT",
                        conditionTarget = "Claim",
                        conditionMatchMode = "CONTAINS",
                        conditionFailureAction = "CONTINUE_NEXT",
                        delayBeforeMs = 400,
                        label = "Check for 'Claim' Text"
                    ),
                    ActionEntity(
                        profileId = p3Id,
                        orderIndex = 1,
                        type = "CLICK",
                        x = 540,
                        y = 1100,
                        delayBeforeMs = 200,
                        durationMs = 40,
                        label = "Tap Claim Bonus"
                    ),
                    ActionEntity(
                        profileId = p3Id,
                        orderIndex = 2,
                        type = "WAIT",
                        delayBeforeMs = 1000,
                        label = "Wait for Animation"
                    )
                )
            )

            database.logDao().insertLog(
                LogEntity(
                    profileId = p1Id,
                    profileName = "Quick Rapid Clicker",
                    message = "Aura Engine initialized. Profiles and templates ready.",
                    level = "INFO",
                    executionTimeMs = 0
                )
            )
        }
    }
}
