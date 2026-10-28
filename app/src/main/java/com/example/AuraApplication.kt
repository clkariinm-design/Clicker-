package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.AutoClickerRepository
import com.example.data.repository.SettingsRepository
import com.example.gesture.GestureRecorder
import com.example.overlay.AuraOverlayManager
import com.example.utils.AutomationEngine
import com.example.utils.SoundVibrationHelper

class AuraApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var repository: AutoClickerRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var soundVibrationHelper: SoundVibrationHelper
        private set
    lateinit var automationEngine: AutomationEngine
        private set
    lateinit var overlayManager: AuraOverlayManager
        private set
    lateinit var gestureRecorder: GestureRecorder
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        repository = AutoClickerRepository(database)
        settingsRepository = SettingsRepository(this)
        soundVibrationHelper = SoundVibrationHelper(this)
        automationEngine = AutomationEngine(this, repository, settingsRepository, soundVibrationHelper)
        overlayManager = AuraOverlayManager(this)
        gestureRecorder = GestureRecorder()

        // Connect overlay buttons to automation engine
        overlayManager.onPlayClicked = {
            // Trigger play on engine
            automationEngine.resumeAutomation()
        }
        overlayManager.onPauseClicked = {
            automationEngine.pauseAutomation()
        }
        overlayManager.onStopClicked = {
            automationEngine.stopAutomation()
        }
    }

    companion object {
        lateinit var instance: AuraApplication
            private set
    }
}
