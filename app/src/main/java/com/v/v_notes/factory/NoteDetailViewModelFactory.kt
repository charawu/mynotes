package com.v.v_notes.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.v.v_notes.data.repository.NoteRepository
import com.v.v_notes.ui.note_detail.NoteDetailViewModel

class NoteDetailViewModelFactory(
    private val application: Application,
    private val noteRepository: NoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
            return NoteDetailViewModel(application, noteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}