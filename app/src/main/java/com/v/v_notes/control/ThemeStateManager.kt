package com.v.v_notes.control

import androidx.compose.runtime.*
import android.content.Context

object ThemeStateManager {
    private var _currentThemeMode = mutableStateOf("follow_system")

    val currentThemeMode: State<String> get() = _currentThemeMode

    fun updateThemeMode(context: Context, newMode: String) {

        SettingsManager.putString("theme_mode", newMode)

        //跟新
        if (_currentThemeMode.value != newMode) {
            _currentThemeMode.value = newMode
        }
    }

    fun initialize(context: Context) {
        val savedMode = SettingsManager.getString("theme_mode", "follow_system")
        if (_currentThemeMode.value != savedMode) {
            _currentThemeMode.value = savedMode
        }
    }
}