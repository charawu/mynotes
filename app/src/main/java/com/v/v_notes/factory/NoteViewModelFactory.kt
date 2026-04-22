package com.v.v_notes.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.data.repository.NoteRepositoryImpl
import com.v.v_notes.viewmodel.NoteViewModel

class NoteViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            // 直接在工厂中创建依赖
            val database = NoteDatabase.getInstance(application)
            val noteDao = database.noteDao()
            val repository = NoteRepositoryImpl(noteDao)

            return NoteViewModel(
                application = application,
                noteRepository = repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}