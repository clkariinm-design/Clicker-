package com.example.utils

import android.content.Context
import android.content.Intent
import com.example.data.database.entity.ActionEntity
import com.example.data.database.entity.ProfileEntity
import com.example.data.model.ActionType
import com.example.data.model.AutomationState
import com.example.data.model.AutomationStats
import com.example.data.model.ConditionFailureAction
import com.example.data.model.ConditionMatchMode
import com.example.data.repository.AutoClickerRepository
import com.example.data.repository.SettingsRepository
import com.example.ocr.TextRecognitionHelper
import com.example.service.AuraAccessibilityService
import com.example.service.AuraForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class VisualClickEvent(
    val x: Float,
    val y: Float,
    val id: Long = System.currentTimeMillis()
)

class AutomationEngine(
    private val context: Context,
    private val repository: AutoClickerRepository,
    private val settingsRepository: SettingsRepository,
    private val soundVibrationHelper: SoundVibrationHelper
) {
    private val engineScope = CoroutineScope(Dispatchers.Default)
    private var automationJob: Job? = null

    private val _engineState = MutableStateFlow(AutomationState.IDLE)
    val engineState = _engineState.asStateFlow()

    private val _stats = MutableStateFlow(AutomationStats())
    val stats = _stats.asStateFlow()

    private val _visualClickEvents = MutableSharedFlow<VisualClickEvent>(extraBufferCapacity = 64)
    val visualClickEvents = _visualClickEvents.asSharedFlow()

    private var activeProfile: ProfileEntity? = null
    private var isPaused = false

    fun startAutomation(profile: ProfileEntity, actions: List<ActionEntity>) {
        if (_engineState.value == AutomationState.RUNNING) return

        activeProfile = profile
        isPaused = false
        _engineState.value = AutomationState.RUNNING
        _stats.value = AutomationStats(currentStepName = "Starting...")

        AuraForegroundService.startService(context, profile.name)

        automationJob?.cancel()
        automationJob = engineScope.launch {
            repository.logEvent(
                profileId = profile.id,
                profileName = profile.name,
                message = "Automation started: ${profile.name} (${profile.mode})",
                level = "INFO"
            )

            // Start delay
            if (profile.startDelayMs > 0) {
                _stats.value = _stats.value.copy(currentStepName = "Start delay (${profile.startDelayMs}ms)")
                delay(profile.startDelayMs)
            }

            val startTime = System.currentTimeMillis()
            var totalClicks = 0L
            var completedCycles = 0L

            try {
                if (actions.isEmpty()) {
                    // Fallback single target action
                    val defaultAction = ActionEntity(
                        profileId = profile.id,
                        orderIndex = 0,
                        type = "CLICK",
                        x = 540,
                        y = 960,
                        delayBeforeMs = profile.clickIntervalMs,
                        label = "Default Target"
                    )
                    runAutomationLoop(listOf(defaultAction), profile, startTime) { clicks, cycles, step ->
                        totalClicks = clicks
                        completedCycles = cycles
                        _stats.value = AutomationStats(
                            totalClicks = totalClicks,
                            completedCycles = completedCycles,
                            elapsedTimeSeconds = (System.currentTimeMillis() - startTime) / 1000,
                            currentStepIndex = 0,
                            currentStepName = step
                        )
                    }
                } else {
                    runAutomationLoop(actions, profile, startTime) { clicks, cycles, step ->
                        totalClicks = clicks
                        completedCycles = cycles
                        _stats.value = AutomationStats(
                            totalClicks = totalClicks,
                            completedCycles = completedCycles,
                            elapsedTimeSeconds = (System.currentTimeMillis() - startTime) / 1000,
                            currentStepIndex = 0,
                            currentStepName = step
                        )
                    }
                }

                repository.logEvent(
                    profileId = profile.id,
                    profileName = profile.name,
                    message = "Automation finished successfully. Total clicks: $totalClicks",
                    level = "SUCCESS",
                    executionTimeMs = System.currentTimeMillis() - startTime
                )
            } catch (e: Exception) {
                repository.logEvent(
                    profileId = profile.id,
                    profileName = profile.name,
                    message = "Automation error: ${e.localizedMessage ?: "Unknown error"}",
                    level = "ERROR"
                )
                soundVibrationHelper.performHapticError()
            } finally {
                _engineState.value = AutomationState.IDLE
                AuraForegroundService.stopService(context)
            }
        }
    }

    private suspend fun runAutomationLoop(
        actions: List<ActionEntity>,
        profile: ProfileEntity,
        startTime: Long,
        onProgress: (clicks: Long, cycles: Long, step: String) -> Unit
    ) {
        var cycle = 0
        var clicks = 0L
        val maxCycles = if (profile.isInfinite) Int.MAX_VALUE else profile.repeatCount

        val soundEnabled = settingsRepository.clickSound.first()
        val vibrationEnabled = settingsRepository.clickVibration.first()

        while (cycle < maxCycles && engineScope.isActive && _engineState.value == AutomationState.RUNNING) {
            var actionIndex = 0
            while (actionIndex < actions.size && engineScope.isActive && _engineState.value == AutomationState.RUNNING) {
                while (isPaused && engineScope.isActive) {
                    delay(200)
                }

                val action = actions[actionIndex]
                val actionType = try {
                    ActionType.valueOf(action.type)
                } catch (e: Exception) {
                    ActionType.CLICK
                }

                onProgress(clicks, cycle.toLong(), action.label.ifBlank { actionType.displayName })

                // Delay before action
                if (action.delayBeforeMs > 0) {
                    delay(action.delayBeforeMs)
                }

                // Execute action
                val success = executeSingleAction(action, actionType, profile.randomJitterRadius)

                if (actionType == ActionType.CLICK || actionType == ActionType.DOUBLE_CLICK || actionType == ActionType.LONG_CLICK) {
                    clicks++
                    if (soundEnabled) soundVibrationHelper.playClickSound()
                    if (vibrationEnabled) soundVibrationHelper.performHapticTick()

                    val targetX = applyJitter(action.x.toFloat(), profile.randomJitterRadius)
                    val targetY = applyJitter(action.y.toFloat(), profile.randomJitterRadius)
                    _visualClickEvents.tryEmit(VisualClickEvent(targetX, targetY))
                }

                // Condition handling
                if (actionType == ActionType.CONDITION_TEXT || actionType == ActionType.CONDITION_IMAGE) {
                    if (!success) {
                        val failAction = try {
                            ConditionFailureAction.valueOf(action.conditionFailureAction)
                        } catch (e: Exception) {
                            ConditionFailureAction.CONTINUE_NEXT
                        }
                        when (failAction) {
                            ConditionFailureAction.CONTINUE_NEXT -> {
                                actionIndex++
                            }
                            ConditionFailureAction.STOP_AUTOMATION -> {
                                repository.logEvent(
                                    profileId = profile.id,
                                    profileName = profile.name,
                                    message = "Condition failed at '${action.label}'. Automation stopped.",
                                    level = "WARN"
                                )
                                return
                            }
                            ConditionFailureAction.JUMP_TO_ACTION -> {
                                actionIndex = action.jumpTargetIndex.coerceIn(0, actions.size - 1)
                                continue
                            }
                        }
                    } else {
                        actionIndex++
                    }
                } else if (actionType == ActionType.LOOP) {
                    // Jump back to start or specified jump target
                    actionIndex = action.jumpTargetIndex.coerceIn(0, actions.size - 1)
                    continue
                } else {
                    actionIndex++
                }
            }
            cycle++
            onProgress(clicks, cycle.toLong(), "Cycle #$cycle completed")
        }
    }

    private suspend fun executeSingleAction(
        action: ActionEntity,
        type: ActionType,
        jitterRadius: Int
    ): Boolean {
        val service = AuraAccessibilityService.getInstance()
        val targetX = applyJitter(action.x.toFloat(), jitterRadius)
        val targetY = applyJitter(action.y.toFloat(), jitterRadius)

        return when (type) {
            ActionType.CLICK -> {
                if (service != null) {
                    service.dispatchClick(targetX, targetY, action.durationMs)
                }
                delay(action.durationMs)
                true
            }
            ActionType.DOUBLE_CLICK -> {
                if (service != null) {
                    service.dispatchClick(targetX, targetY, 30L)
                    delay(80L)
                    service.dispatchClick(targetX, targetY, 30L)
                } else {
                    delay(120L)
                }
                true
            }
            ActionType.LONG_CLICK -> {
                val duration = action.durationMs.coerceAtLeast(600L)
                if (service != null) {
                    service.dispatchClick(targetX, targetY, duration)
                }
                delay(duration)
                true
            }
            ActionType.SWIPE -> {
                val endX = applyJitter(action.endX.toFloat(), jitterRadius)
                val endY = applyJitter(action.endY.toFloat(), jitterRadius)
                val duration = action.durationMs.coerceAtLeast(150L)
                if (service != null) {
                    service.dispatchSwipe(targetX, targetY, endX, endY, duration)
                }
                delay(duration)
                true
            }
            ActionType.DRAG -> {
                val endX = applyJitter(action.endX.toFloat(), jitterRadius)
                val endY = applyJitter(action.endY.toFloat(), jitterRadius)
                val duration = action.durationMs.coerceAtLeast(800L)
                if (service != null) {
                    service.dispatchSwipe(targetX, targetY, endX, endY, duration)
                }
                delay(duration)
                true
            }
            ActionType.WAIT -> {
                delay(action.delayBeforeMs.coerceAtLeast(50L))
                true
            }
            ActionType.LOOP, ActionType.REPEAT -> {
                true
            }
            ActionType.OPEN_APP -> {
                if (action.targetPackage.isNotBlank()) {
                    try {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(action.targetPackage)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                            delay(1000L) // Wait for app to open
                            return true
                        }
                    } catch (e: Exception) {
                        return false
                    }
                }
                true
            }
            ActionType.BACK -> {
                service?.performBack() ?: true
            }
            ActionType.HOME -> {
                service?.performHome() ?: true
            }
            ActionType.RECENTS -> {
                service?.performRecents() ?: true
            }
            ActionType.CONDITION_TEXT -> {
                val matchMode = try {
                    ConditionMatchMode.valueOf(action.conditionMatchMode)
                } catch (e: Exception) {
                    ConditionMatchMode.CONTAINS
                }
                TextRecognitionHelper.checkTextExists(action.conditionTarget, matchMode)
            }
            ActionType.CONDITION_IMAGE -> {
                // Condition passes if target configured or in simulation
                action.conditionTarget.isNotBlank()
            }
        }
    }

    private fun applyJitter(coord: Float, radius: Int): Float {
        if (radius <= 0) return coord
        val offset = Random.nextInt(-radius, radius + 1)
        return (coord + offset).coerceAtLeast(0f)
    }

    fun pauseAutomation() {
        if (_engineState.value == AutomationState.RUNNING) {
            isPaused = true
            _engineState.value = AutomationState.PAUSED
        }
    }

    fun resumeAutomation() {
        if (_engineState.value == AutomationState.PAUSED) {
            isPaused = false
            _engineState.value = AutomationState.RUNNING
        }
    }

    fun stopAutomation() {
        isPaused = false
        _engineState.value = AutomationState.IDLE
        automationJob?.cancel()
        automationJob = null
        AuraForegroundService.stopService(context)
    }
}
