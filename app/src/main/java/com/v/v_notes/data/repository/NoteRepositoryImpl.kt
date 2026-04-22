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
}