package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mode: String = "MULTI_TARGET",
    val clickIntervalMs: Long = 200L,
    val startDelayMs: Long = 1000L,
    val repeatCount: Int = 10,
    val isInfinite: Boolean = true,
    val randomJitterRadius: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class ActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val orderIndex: Int,
    val type: String,
    val x: Int = 540,
    val y: Int = 960,
    val endX: Int = 540,
    val endY: Int = 500,
    val delayBeforeMs: Long = 100,
    val durationMs: Long = 50,
    val repeatCount: Int = 1,
    val targetPackage: String = "",
    val conditionTarget: String = "",
    val conditionMatchMode: String = "CONTAINS",
    val conditionFailureAction: String = "CONTINUE_NEXT",
    val jumpTargetIndex: Int = 0,
    val label: String = ""
)

@Entity(tableName = "automation_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long = 0,
    val profileName: String = "",
    val message: String,
    val level: String = "INFO",
    val executionTimeMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val profileName: String,
    val scheduledHour: Int = 12,
    val scheduledMinute: Int = 0,
    val repeatIntervalMinutes: Int = 0, // 0 = one time, > 0 = repeat interval
    val isEnabled: Boolean = true,
    val lastRunTimestamp: Long = 0
)

@Entity(tableName = "recorded_gestures")
data class RecordedGestureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMs: Long,
    val pointCount: Int,
    val rawPointsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
