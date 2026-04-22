package com.v.v_notes

import android.app.Application
import com.v.v_notes.control.SettingsManager
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.data.repository.NoteRepository
import com.v.v_notes.data.repository.NoteRepositoryImpl

class MyNotesApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // 通过懒加载初始化数据库
        val database by lazy { NoteDatabase.getInstance(this) }

        // 通过懒加载初始化Repository
        val noteRepository: NoteRepository by lazy {
            NoteRepositoryImpl(database.noteDao())
        }

        SettingsManager.init(this)
    }
}