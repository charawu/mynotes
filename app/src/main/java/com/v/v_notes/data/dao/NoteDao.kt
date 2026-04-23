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
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    // 新增：获取所有已归档且未删除的笔记
    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllArchivedNotes(): Flow<List<Note>>

    // 新增：获取所有已删除的笔记（回收站）
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getAllDeletedNotes(): Flow<List<Note>>

    // 新增：获取所有置顶的笔记（且未删除）
    @Query("SELECT * FROM notes WHERE isPinned = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllPinnedNotes(): Flow<List<Note>>

    // 按时间升序（只显示未删除的笔记）
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt ASC")
    fun getNotesByTimeAscending(): Flow<List<Note>>

    // 按时间降序（只显示未删除的笔记）
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getNotesByTimeDescending(): Flow<List<Note>>

    // 搜索功能（只搜索未删除的笔记）
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND isDeleted = 0")
    suspend fun searchNotes(query: String): List<Note>

    // 更新归档状态
    @Query("UPDATE notes SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchiveStatus(id: String, isArchived: Boolean)

    // 更新删除状态（软删除）
    @Query("UPDATE notes SET isDeleted = :isDeleted WHERE id = :id")
    suspend fun updateDeleteStatus(id: String, isDeleted: Boolean)

    // 更新置顶状态
    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: String, isPinned: Boolean)

    // 清空回收站（永久删除已软删除的笔记）
    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun permanentlyDeleteAllTrashed()

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND updatedAt BETWEEN :startTime AND :endTime ORDER BY updatedAt DESC")
    fun getNotesInTimeRange(startTime: Long, endTime: Long): Flow<List<Note>>

    // 以下是原有的方法
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNoteBy(): Flow<List<Note>>

    @Insert
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // 清空所有笔记（危险操作，物理删除所有）
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}