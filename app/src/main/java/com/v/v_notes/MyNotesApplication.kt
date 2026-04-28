package com.v.v_notes

import android.app.Application
import com.v.v_notes.control.SettingsManager
import com.v.v_notes.data.database.NoteDatabase

class MyNotesApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        //初始化数据库
        val database by lazy { NoteDatabase.getInstance(this) }

        SettingsManager.init(this)
    }
}