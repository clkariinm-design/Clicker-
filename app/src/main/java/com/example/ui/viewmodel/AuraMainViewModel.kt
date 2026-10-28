package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.AuraApplication
import com.example.data.database.entity.ActionEntity
import com.example.data.database.entity.LogEntity
import com.example.data.database.entity.ProfileEntity
import com.example.data.database.entity.RecordedGestureEntity
import com.example.data.database.entity.ScheduleEntity
import com.example.data.model.AutomationState
import com.example.data.model.AutomationStats
import com.example.data.model.RecordedGesturePoint
import com.example.gesture.GestureRecorder
import com.example.service.AuraAccessibilityService
import com.example.utils.PermissionHelper
import com.example.utils.VisualClickEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PermissionsState(
    val isAccessibilityGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false
)

class AuraMainViewModel : ViewModel() {

    private val app = AuraApplication.instance
    private val repository = app.repository
    private val settingsRepository = app.settingsRepository
    private val engine = app.automationEngine
    private val overlayManager = app.overlayManager
    private val gestureRecorder = app.gestureRecorder

    val allProfiles: StateFlow<List<ProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<LogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<ScheduleEntity>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGestures: StateFlow<List<RecordedGestureEntity>> = repository.allGestures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProfileId = MutableStateFlow<Long?>(null)
    val selectedProfileId = _selectedProfileId.asStateFlow()

    private val _actions = MutableStateFlow<List<ActionEntity>>(emptyList())
    val actions = _actions.asStateFlow()

    val automationState: StateFlow<AutomationState> = engine.engineState
    val stats: StateFlow<AutomationStats> = engine.stats
    val visualClickEvents = engine.visualClickEvents

    val isOverlayShowing: StateFlow<Boolean> = overlayManager.isOverlayShowing
    val isGestureRecording: StateFlow<Boolean> = gestureRecorder.isRecording

    private val _permissionsState = MutableStateFlow(PermissionsState())
    val permissionsState = _permissionsState.asStateFlow()

    // Settings flows
    val themeMode = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    val clickSound = settingsRepository.clickSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val clickVibration = settingsRepository.clickVibration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val animationSpeed = settingsRepository.animationSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val clickEffect = settingsRepository.clickEffect
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "CYBER_RIPPLE")

    val rippleColor = settingsRepository.rippleColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFF00E5FF.toInt())

    val language = settingsRepository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "English")

    init {
        viewModelScope.launch {
            allProfiles.collect { profiles ->
                if (_selectedProfileId.value == null && profiles.isNotEmpty()) {
                    selectProfile(profiles.first().id)
                }
            }
        }
    }

    fun selectProfile(id: Long) {
        _selectedProfileId.value = id
        viewModelScope.launch {
            repository.getActionsForProfile(id).collect {
                _actions.value = it
            }
        }
    }

    fun checkPermissions(context: Context) {
        _permissionsState.value = PermissionsState(
            isAccessibilityGranted = PermissionHelper.isAccessibilityServiceEnabled(context),
            isOverlayGranted = PermissionHelper.hasOverlayPermission(context),
            isBatteryOptimizationIgnored = PermissionHelper.isIgnoringBatteryOptimizations(context)
        )
    }

    fun startAutomation() {
        viewModelScope.launch {
            val profileId = _selectedProfileId.value ?: return@launch
            val profile = repository.getProfileSync(profileId) ?: return@launch
            val currentActions = repository.getActionsForProfileSync(profileId)
            engine.startAutomation(profile, currentActions)
        }
    }

    fun pauseAutomation() {
        engine.pauseAutomation()
    }

    fun resumeAutomation() {
        engine.resumeAutomation()
    }

    fun stopAutomation() {
        engine.stopAutomation()
    }

    fun toggleOverlay(context: Context) {
        if (overlayManager.isOverlayShowing.value) {
            overlayManager.hideOverlay()
        } else {
            if (!PermissionHelper.hasOverlayPermission(context)) {
                PermissionHelper.openOverlaySettings(context)
                return
            }
            overlayManager.showOverlay()
        }
    }

    fun createProfile(name: String, mode: String, clickIntervalMs: Long = 100L) {
        viewModelScope.launch {
            val newId = repository.createProfile(name, mode, clickIntervalMs)
            selectProfile(newId)
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteProfile(id)
            if (_selectedProfileId.value == id) {
                _selectedProfileId.value = null
            }
        }
    }

    fun duplicateProfile(id: Long) {
        viewModelScope.launch {
            val newId = repository.duplicateProfile(id)
            if (newId > 0) {
                selectProfile(newId)
            }
        }
    }

    fun saveAction(action: ActionEntity) {
        viewModelScope.launch {
            repository.saveAction(action)
        }
    }

    fun deleteAction(actionId: Long) {
        viewModelScope.launch {
            repository.deleteAction(actionId)
        }
    }

    fun reorderActions(actions: List<ActionEntity>) {
        val profileId = _selectedProfileId.value ?: return
        viewModelScope.launch {
            repository.reorderActions(profileId, actions)
        }
    }

    fun exportActiveProfile(onResult: (String) -> Unit) {
        val profileId = _selectedProfileId.value ?: return
        viewModelScope.launch {
            val json = repository.exportProfileToJson(profileId)
            onResult(json)
        }
    }

    fun importProfile(jsonString: String, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val newId = repository.importProfileFromJson(jsonString)
                selectProfile(newId)
                onSuccess(newId)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun saveSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.saveSchedule(schedule)
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteSchedule(id)
        }
    }

    fun startGestureRecording() {
        gestureRecorder.startRecording()
    }

    fun addGesturePoint(x: Float, y: Float) {
        gestureRecorder.addPoint(x, y)
    }

    fun stopAndSaveGesture(name: String) {
        val points = gestureRecorder.stopRecording()
        if (points.isNotEmpty()) {
            val duration = points.last().timestampMs - points.first().timestampMs
            val json = gestureRecorder.serializePoints(points)
            viewModelScope.launch {
                repository.saveGesture(
                    RecordedGestureEntity(
                        name = name.ifBlank { "Gesture #${System.currentTimeMillis() % 1000}" },
                        durationMs = duration,
                        pointCount = points.size,
                        rawPointsJson = json
                    )
                )
            }
        }
    }

    fun replayGesture(gesture: RecordedGestureEntity, onComplete: (Boolean) -> Unit) {
        val points = gestureRecorder.deserializePoints(gesture.rawPointsJson)
        val service = AuraAccessibilityService.getInstance()
        gestureRecorder.replayGesture(points, service, onComplete)
    }

    fun deleteGesture(id: Long) {
        viewModelScope.launch {
            repository.deleteGesture(id)
        }
    }

    fun updateSettings(
        theme: String? = null,
        sound: Boolean? = null,
        vibration: Boolean? = null,
        speed: Float? = null,
        effect: String? = null,
        colorArgb: Int? = null,
        lang: String? = null
    ) {
        viewModelScope.launch {
            theme?.let { settingsRepository.setThemeMode(it) }
            sound?.let { settingsRepository.setClickSound(it) }
            vibration?.let { settingsRepository.setClickVibration(it) }
            speed?.let { settingsRepository.setAnimationSpeed(it) }
            effect?.let { settingsRepository.setClickEffect(it) }
            colorArgb?.let { settingsRepository.setRippleColor(it) }
            lang?.let { settingsRepository.setLanguage(it) }
        }
    }
}
