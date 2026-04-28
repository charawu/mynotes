package com.v.v_notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(
    application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    //未删除且未归档
    val allNotes = noteRepository.getAllNotes()

    //已归档
    val archivedNotes = noteRepository.getAllArchivedNotes()

    //已删除
    val deletedNotes = noteRepository.getAllDeletedNotes()

    //已置顶的笔记
    val pinnedNotes = noteRepository.getAllPinnedNotes()

    fun insertNote(note: Note) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    // 物理删除（慎用，一般用于清空回收站时）
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    //软删除
    fun moveNoteToTrash(note: Note) {
        viewModelScope.launch {
            noteRepository.moveNoteToTrash(note)
        }
    }

    //切换归档状态
    fun toggleArchiveStatus(note: Note) {
        viewModelScope.launch {
            noteRepository.toggleArchiveStatus(note)
        }
    }

    //从回收站恢复笔记
    fun restoreNoteFromTrash(note: Note) {
        viewModelScope.launch {
            noteRepository.restoreNoteFromTrash(note)
        }
    }

    //切换置顶状态
    fun togglePinStatus(note: Note) {
        viewModelScope.launch {
            noteRepository.togglePinStatus(note)
        }
    }

    //清空回收站
    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
        }
    }

    suspend fun getNoteById(id: String) = noteRepository.getNoteById(id)
}