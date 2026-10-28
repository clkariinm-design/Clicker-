package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.ActionEntity
import com.example.data.database.entity.LogEntity
import com.example.data.database.entity.ProfileEntity
import com.example.data.database.entity.RecordedGestureEntity
import com.example.data.database.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY updatedAt DESC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdSync(id: Long): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
}

@Dao
interface ActionDao {
    @Query("SELECT * FROM actions WHERE profileId = :profileId ORDER BY orderIndex ASC")
    fun getActionsForProfile(profileId: Long): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions WHERE profileId = :profileId ORDER BY orderIndex ASC")
    suspend fun getActionsForProfileSync(profileId: Long): List<ActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(actions: List<ActionEntity>)

    @Update
    suspend fun updateAction(action: ActionEntity)

    @Delete
    suspend fun deleteAction(action: ActionEntity)

    @Query("DELETE FROM actions WHERE id = :id")
    suspend fun deleteActionById(id: Long)

    @Query("DELETE FROM actions WHERE profileId = :profileId")
    suspend fun deleteActionsForProfile(profileId: Long)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM automation_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity): Long

    @Query("DELETE FROM automation_logs")
    suspend fun clearLogs()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY scheduledHour ASC, scheduledMinute ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)
}

@Dao
interface GestureDao {
    @Query("SELECT * FROM recorded_gestures ORDER BY createdAt DESC")
    fun getAllGestures(): Flow<List<RecordedGestureEntity>>

    @Query("SELECT * FROM recorded_gestures WHERE id = :id LIMIT 1")
    suspend fun getGestureById(id: Long): RecordedGestureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGesture(gesture: RecordedGestureEntity): Long

    @Query("DELETE FROM recorded_gestures WHERE id = :id")
    suspend fun deleteGestureById(id: Long)
}
