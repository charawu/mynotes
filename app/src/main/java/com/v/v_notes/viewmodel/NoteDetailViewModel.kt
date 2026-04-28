package com.v.v_notes.ui.note_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedNote = noteRepository.getNoteById(noteId)
                _note.value = loadedNote
            } finally {
                _isLoading.value = false
            }
        }
    }

    //更新待办事项状态
    fun updateTodoItemStatus(todoItemId: String, isCompleted: Boolean) {
        val currentNote = _note.value ?: return

        viewModelScope.launch {
            try {
                //更新数据库
                noteRepository.updateTodoItemStatus(currentNote.id, todoItemId, isCompleted)

                //更新本地状态
                updateLocalTodoItemStatus(todoItemId, isCompleted)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //更新本地状态
    private fun updateLocalTodoItemStatus(todoItemId: String, isCompleted: Boolean) {
        val currentNote = _note.value ?: return

        val updatedTodoItems = currentNote.todoItems.map { todoItem ->
            if (todoItem.id == todoItemId) {
                todoItem.copy(isCompleted = isCompleted)
            } else {
                todoItem
            }
        }

        _note.value = currentNote.copy(
            todoItems = updatedTodoItems,
            updatedAt = System.currentTimeMillis()
        )
    }
}