package com.v.v_notes.data.repository

import com.v.v_notes.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>

    // 改为String类型
    suspend fun getNoteById(id: String): Note?

    // 不再返回Long
    suspend fun insertNote(note: Note)

    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun searchNotes(query: String): List<Note>
    fun getNotesSortedByTime(ascending: Boolean = false): Flow<List<Note>>

    // 可选：如果不需要可以删除这个方法
    fun getNotesInTimeRange(startTime: Long, endTime: Long): Flow<List<Note>>

    // 不再返回List<Long>
    suspend fun insertNotes(notes: List<Note>)

    suspend fun deleteNotes(notes: List<Note>)

    // 1. 获取不同状态的笔记列表
    fun getAllArchivedNotes(): Flow<List<Note>>
    fun getAllDeletedNotes(): Flow<List<Note>>
    fun getAllPinnedNotes(): Flow<List<Note>>

    // 2. 更新笔记状态的方法
    suspend fun toggleArchiveStatus(note: Note)
    suspend fun moveNoteToTrash(note: Note)
    suspend fun restoreNoteFromTrash(note: Note)
    suspend fun togglePinStatus(note: Note)

    // 3. 永久删除（清空回收站）
    suspend fun emptyTrash()
}