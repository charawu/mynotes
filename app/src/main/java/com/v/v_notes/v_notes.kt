package com.v.v_notes

import android.app.Application
import com.v.v_notes.control.SettingsManager

class v_notes: Application() {
    override fun onCreate() {
        super.onCreate()

        SettingsManager.init(this)
    }
}