package com.v.v_notes.data.repository

import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>

    suspend fun getNoteById(id: String): Note?

    suspend fun insertNote(note: Note)

    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun searchNotes(query: String): List<Note>
    fun getNotesSortedByTime(ascending: Boolean = false): Flow<List<Note>>

    fun getNotesInTimeRange(startTime: Long, endTime: Long): Flow<List<Note>>

    suspend fun insertNotes(notes: List<Note>)

    suspend fun deleteNotes(notes: List<Note>)

    //获取不同状态的笔记列表
    fun getAllArchivedNotes(): Flow<List<Note>>
    fun getAllDeletedNotes(): Flow<List<Note>>
    fun getAllPinnedNotes(): Flow<List<Note>>

    //更新笔记状态
    suspend fun toggleArchiveStatus(note: Note)
    suspend fun moveNoteToTrash(note: Note)
    suspend fun restoreNoteFromTrash(note: Note)
    suspend fun togglePinStatus(note: Note)

    //永久删除
    suspend fun emptyTrash()

    // ============ 新增的待办事项操作方法 ============

    // 更新单个待办事项的完成状态
    suspend fun updateTodoItemStatus(noteId: String, todoItemId: String, isCompleted: Boolean)

    // 更新整个待办事项列表
    suspend fun updateTodoItemsList(noteId: String, todoItems: List<TodoItem>)

    // 添加新的待办事项
    suspend fun addTodoItem(noteId: String, text: String)

    // 删除待办事项
    suspend fun removeTodoItem(noteId: String, todoItemId: String)
}