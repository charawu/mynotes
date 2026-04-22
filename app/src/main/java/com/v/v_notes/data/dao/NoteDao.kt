package com.v.v_notes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.v.v_notes.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note? // 改为String

    @Insert
    suspend fun insertNote(note: Note) // 不再返回Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // 搜索功能
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun searchNotes(query: String): List<Note>

    // 按时间排序
    @Query("SELECT * FROM notes ORDER BY updatedAt ASC")
    fun getNotesByTimeAscending(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getNotesByTimeDescending(): Flow<List<Note>>

    // 清空所有笔记
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}