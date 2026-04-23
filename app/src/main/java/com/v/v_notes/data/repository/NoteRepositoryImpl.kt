package com.v.v_notes.data.repository

import com.v.v_notes.data.dao.NoteDao
import com.v.v_notes.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    // 修正：返回 Flow<List<Note>>
    override fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    // 使用String类型的id
    override suspend fun getNoteById(id: String): Note? {
        return withContext(Dispatchers.IO) {
            noteDao.getNoteById(id)
        }
    }

    // 不再返回Long
    override suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    override suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    override suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }

    override suspend fun searchNotes(query: String): List<Note> {
        return withContext(Dispatchers.IO) {
            noteDao.searchNotes(query)
        }
    }

    override fun getNotesSortedByTime(ascending: Boolean): Flow<List<Note>> {
        return if (ascending) {
            noteDao.getNotesByTimeAscending()
        } else {
            noteDao.getNotesByTimeDescending()
        }
    }

    override fun getNotesInTimeRange(startTime: Long, endTime: Long): Flow<List<Note>> {
        // 如果不需要这个方法，可以简化实现
        return noteDao.getAllNotes()
    }

    // 不再返回List<Long>
    override suspend fun insertNotes(notes: List<Note>) {
        withContext(Dispatchers.IO) {
            notes.forEach { noteDao.insertNote(it) }
        }
    }

    override suspend fun deleteNotes(notes: List<Note>) {
        withContext(Dispatchers.IO) {
            notes.forEach { noteDao.deleteNote(it) }
        }
    }

    // 1. 获取不同状态的笔记列表
    override fun getAllArchivedNotes(): Flow<List<Note>> = noteDao.getAllArchivedNotes()
    override fun getAllDeletedNotes(): Flow<List<Note>> = noteDao.getAllDeletedNotes()
    override fun getAllPinnedNotes(): Flow<List<Note>> = noteDao.getAllPinnedNotes()

    // 2. 更新笔记状态
    override suspend fun toggleArchiveStatus(note: Note) {
        withContext(Dispatchers.IO) {
            // 切换归档状态
            noteDao.updateArchiveStatus(note.id, !note.isArchived)
        }
    }

    override suspend fun moveNoteToTrash(note: Note) {
        withContext(Dispatchers.IO) {
            // 移动到回收站（软删除）
            noteDao.updateDeleteStatus(note.id, true)
        }
    }

    override suspend fun restoreNoteFromTrash(note: Note) {
        withContext(Dispatchers.IO) {
            // 从回收站恢复
            noteDao.updateDeleteStatus(note.id, false)
        }
    }

    override suspend fun togglePinStatus(note: Note) {
        withContext(Dispatchers.IO) {
            // 切换置顶状态
            noteDao.updatePinStatus(note.id, !note.isPinned)
        }
    }

    // 3. 永久删除（清空回收站）
    override suspend fun emptyTrash() {
        withContext(Dispatchers.IO) {
            noteDao.permanentlyDeleteAllTrashed()
        }
    }
}