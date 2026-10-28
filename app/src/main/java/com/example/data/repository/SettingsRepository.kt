package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "aura_settings")

enum class ClickEffectType {
    CYBER_RIPPLE,
    PULSE_RETICLE,
    CROSSHAIR,
    AURA_ORB
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CLICK_SOUND = booleanPreferencesKey("click_sound")
        val CLICK_VIBRATION = booleanPreferencesKey("click_vibration")
        val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val CLICK_EFFECT = stringPreferencesKey("click_effect")
        val RIPPLE_COLOR = intPreferencesKey("ripple_color")
        val LANGUAGE = stringPreferencesKey("language")
        val TOOLBAR_TRANSPARENCY = floatPreferencesKey("toolbar_transparency")
        val POINTER_SIZE_DP = intPreferencesKey("pointer_size_dp")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "DARK"
    }

    val clickSound: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.CLICK_SOUND] ?: true
    }

    val clickVibration: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.CLICK_VIBRATION] ?: true
    }

    val animationSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.ANIMATION_SPEED] ?: 1.0f
    }

    val clickEffect: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CLICK_EFFECT] ?: ClickEffectType.CYBER_RIPPLE.name
    }

    val rippleColor: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.RIPPLE_COLOR] ?: 0xFF00E5FF.toInt()
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "English"
    }

    val toolbarTransparency: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.TOOLBAR_TRANSPARENCY] ?: 0.95f
    }

    val pointerSizeDp: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.POINTER_SIZE_DP] ?: 42
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setClickSound(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CLICK_SOUND] = enabled }
    }

    suspend fun setClickVibration(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CLICK_VIBRATION] = enabled }
    }

    suspend fun setAnimationSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.ANIMATION_SPEED] = speed }
    }

    suspend fun setClickEffect(effect: String) {
        context.dataStore.edit { it[Keys.CLICK_EFFECT] = effect }
    }

    suspend fun setRippleColor(colorArgb: Int) {
        context.dataStore.edit { it[Keys.RIPPLE_COLOR] = colorArgb }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setToolbarTransparency(alpha: Float) {
        context.dataStore.edit { it[Keys.TOOLBAR_TRANSPARENCY] = alpha }
    }

    suspend fun setPointerSizeDp(size: Int) {
        context.dataStore.edit { it[Keys.POINTER_SIZE_DP] = size }
    }
}
