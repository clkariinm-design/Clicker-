package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.entity.ActionEntity
import com.example.data.database.entity.LogEntity
import com.example.data.database.entity.ProfileEntity
import com.example.data.database.entity.RecordedGestureEntity
import com.example.data.database.entity.ScheduleEntity
import com.example.data.model.ActionType
import com.example.data.model.ClickAction
import com.example.data.model.ConditionFailureAction
import com.example.data.model.ConditionMatchMode
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class AutoClickerRepository(private val database: AppDatabase) {

    val allProfiles: Flow<List<ProfileEntity>> = database.profileDao().getAllProfiles()
    val allLogs: Flow<List<LogEntity>> = database.logDao().getAllLogs()
    val allSchedules: Flow<List<ScheduleEntity>> = database.scheduleDao().getAllSchedules()
    val allGestures: Flow<List<RecordedGestureEntity>> = database.gestureDao().getAllGestures()

    fun getProfile(id: Long): Flow<ProfileEntity?> = database.profileDao().getProfileById(id)

    suspend fun getProfileSync(id: Long): ProfileEntity? = database.profileDao().getProfileByIdSync(id)

    fun getActionsForProfile(profileId: Long): Flow<List<ActionEntity>> =
        database.actionDao().getActionsForProfile(profileId)

    suspend fun getActionsForProfileSync(profileId: Long): List<ActionEntity> =
        database.actionDao().getActionsForProfileSync(profileId)

    suspend fun createProfile(
        name: String,
        mode: String,
        clickIntervalMs: Long = 100L,
        repeatCount: Int = 10,
        isInfinite: Boolean = true,
        randomJitterRadius: Int = 0
    ): Long {
        val entity = ProfileEntity(
            name = name,
            mode = mode,
            clickIntervalMs = clickIntervalMs,
            startDelayMs = 1000L,
            repeatCount = repeatCount,
            isInfinite = isInfinite,
            randomJitterRadius = randomJitterRadius,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val profileId = database.profileDao().insertProfile(entity)
        // Add default action
        database.actionDao().insertAction(
            ActionEntity(
                profileId = profileId,
                orderIndex = 0,
                type = "CLICK",
                x = 540,
                y = 960,
                delayBeforeMs = clickIntervalMs,
                label = "Point 1"
            )
        )
        return profileId
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        database.profileDao().updateProfile(profile.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProfile(id: Long) {
        database.actionDao().deleteActionsForProfile(id)
        database.profileDao().deleteProfileById(id)
    }

    suspend fun duplicateProfile(id: Long): Long {
        val original = database.profileDao().getProfileByIdSync(id) ?: return -1
        val duplicated = original.copy(
            id = 0,
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newProfileId = database.profileDao().insertProfile(duplicated)
        val actions = database.actionDao().getActionsForProfileSync(id)
        val duplicatedActions = actions.map { action ->
            action.copy(id = 0, profileId = newProfileId)
        }
        database.actionDao().insertActions(duplicatedActions)
        return newProfileId
    }

    suspend fun saveAction(action: ActionEntity): Long {
        return if (action.id == 0L) {
            database.actionDao().insertAction(action)
        } else {
            database.actionDao().updateAction(action)
            action.id
        }
    }

    suspend fun deleteAction(actionId: Long) {
        database.actionDao().deleteActionById(actionId)
    }

    suspend fun reorderActions(profileId: Long, actions: List<ActionEntity>) {
        database.actionDao().deleteActionsForProfile(profileId)
        val updated = actions.mapIndexed { index, action ->
            action.copy(id = 0, orderIndex = index)
        }
        database.actionDao().insertActions(updated)
    }

    suspend fun logEvent(
        profileId: Long = 0,
        profileName: String = "Aura Engine",
        message: String,
        level: String = "INFO",
        executionTimeMs: Long = 0
    ) {
        database.logDao().insertLog(
            LogEntity(
                profileId = profileId,
                profileName = profileName,
                message = message,
                level = level,
                executionTimeMs = executionTimeMs,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearLogs() {
        database.logDao().clearLogs()
    }

    suspend fun saveSchedule(schedule: ScheduleEntity): Long {
        return if (schedule.id == 0L) {
            database.scheduleDao().insertSchedule(schedule)
        } else {
            database.scheduleDao().updateSchedule(schedule)
            schedule.id
        }
    }

    suspend fun deleteSchedule(id: Long) {
        database.scheduleDao().deleteScheduleById(id)
    }

    suspend fun saveGesture(gesture: RecordedGestureEntity): Long {
        return database.gestureDao().insertGesture(gesture)
    }

    suspend fun deleteGesture(id: Long) {
        database.gestureDao().deleteGestureById(id)
    }

    suspend fun exportProfileToJson(profileId: Long): String {
        val profile = database.profileDao().getProfileByIdSync(profileId) ?: return "{}"
        val actions = database.actionDao().getActionsForProfileSync(profileId)

        val root = JSONObject().apply {
            put("version", 1)
            put("app", "Aura Auto Clicker")
            put("name", profile.name)
            put("mode", profile.mode)
            put("clickIntervalMs", profile.clickIntervalMs)
            put("startDelayMs", profile.startDelayMs)
            put("repeatCount", profile.repeatCount)
            put("isInfinite", profile.isInfinite)
            put("randomJitterRadius", profile.randomJitterRadius)

            val actionsArray = JSONArray()
            actions.forEach { act ->
                val actObj = JSONObject().apply {
                    put("orderIndex", act.orderIndex)
                    put("type", act.type)
                    put("x", act.x)
                    put("y", act.y)
                    put("endX", act.endX)
                    put("endY", act.endY)
                    put("delayBeforeMs", act.delayBeforeMs)
                    put("durationMs", act.durationMs)
                    put("repeatCount", act.repeatCount)
                    put("targetPackage", act.targetPackage)
                    put("conditionTarget", act.conditionTarget)
                    put("conditionMatchMode", act.conditionMatchMode)
                    put("conditionFailureAction", act.conditionFailureAction)
                    put("jumpTargetIndex", act.jumpTargetIndex)
                    put("label", act.label)
                }
                actionsArray.put(actObj)
            }
            put("actions", actionsArray)
        }
        return root.toString(2)
    }

    suspend fun importProfileFromJson(jsonString: String): Long {
        val root = JSONObject(jsonString)
        val name = root.optString("name", "Imported Profile")
        val mode = root.optString("mode", "MULTI_TARGET")
        val clickIntervalMs = root.optLong("clickIntervalMs", 200L)
        val startDelayMs = root.optLong("startDelayMs", 1000L)
        val repeatCount = root.optInt("repeatCount", 10)
        val isInfinite = root.optBoolean("isInfinite", true)
        val randomJitterRadius = root.optInt("randomJitterRadius", 0)

        val profileId = database.profileDao().insertProfile(
            ProfileEntity(
                name = name,
                mode = mode,
                clickIntervalMs = clickIntervalMs,
                startDelayMs = startDelayMs,
                repeatCount = repeatCount,
                isInfinite = isInfinite,
                randomJitterRadius = randomJitterRadius,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        val actionsArray = root.optJSONArray("actions")
        if (actionsArray != null) {
            val actionEntities = mutableListOf<ActionEntity>()
            for (i in 0 until actionsArray.length()) {
                val actObj = actionsArray.getJSONObject(i)
                actionEntities.add(
                    ActionEntity(
                        profileId = profileId,
                        orderIndex = actObj.optInt("orderIndex", i),
                        type = actObj.optString("type", "CLICK"),
                        x = actObj.optInt("x", 540),
                        y = actObj.optInt("y", 960),
                        endX = actObj.optInt("endX", 540),
                        endY = actObj.optInt("endY", 500),
                        delayBeforeMs = actObj.optLong("delayBeforeMs", 100),
                        durationMs = actObj.optLong("durationMs", 50),
                        repeatCount = actObj.optInt("repeatCount", 1),
                        targetPackage = actObj.optString("targetPackage", ""),
                        conditionTarget = actObj.optString("conditionTarget", ""),
                        conditionMatchMode = actObj.optString("conditionMatchMode", "CONTAINS"),
                        conditionFailureAction = actObj.optString("conditionFailureAction", "CONTINUE_NEXT"),
                        jumpTargetIndex = actObj.optInt("jumpTargetIndex", 0),
                        label = actObj.optString("label", "Action ${i + 1}")
                    )
                )
            }
            database.actionDao().insertActions(actionEntities)
        }

        return profileId
    }
}
