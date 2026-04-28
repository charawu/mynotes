package com.v.v_notes.sync.manager

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v.v_notes.data.dao.NoteDao
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem
import com.v.v_notes.sync.data.SyncNote
import com.v.v_notes.sync.data.SyncRequest
import com.v.v_notes.sync.data.SyncResult
import com.v.v_notes.sync.data.SyncStatus
import com.v.v_notes.sync.data.SyncTodoItem
import com.v.v_notes.sync.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "sync_prefs")

class SyncManager(
    private val context: Context,
    private val noteDao: NoteDao
) {

    companion object {
        private const val TAG = "SyncManager"
        private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
    }

    //同步状态
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    //同步进度
    private val _syncProgress = MutableStateFlow(0)
    val syncProgress: StateFlow<Int> = _syncProgress

    //最后同步时间
    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime

    //设备id
    val deviceId: String by lazy {
        val prefs = context.getSharedPreferences("sync_settings", Context.MODE_PRIVATE)
        var id = prefs.getString("device_id", null)
        if (id.isNullOrEmpty()) {
            id = "android_${UUID.randomUUID().toString().substring(0, 8)}"
            prefs.edit().putString("device_id", id).apply()
        }
        id
    }

    private val syncScope = CoroutineScope(Dispatchers.IO)

    init {
        syncScope.launch {
            loadLastSyncTime()
        }
    }

    suspend fun performSync(userId: Long, token: String = ""): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                _syncStatus.value = SyncStatus.PREPARING
                _syncProgress.value = 10

                //获取最后同步时间
                val lastSync = _lastSyncTime.value
                Log.d(TAG, "开始同步,最后同步时间 $lastSync")

                //从数据库获取笔记
                _syncProgress.value = 20
                val notesToSync = getNotesToSync(lastSync)
                Log.d(TAG, "从数据库读取到 ${notesToSync.size} 条需要同步的笔记")

                if (notesToSync.isEmpty()) {
                    _syncStatus.value = SyncStatus.COMPLETED
                    _syncProgress.value = 100
                    return@withContext SyncResult(
                        isSuccess = true,
                        message = "没有需要同步的笔记"
                    )
                }

                //构建请求
                _syncStatus.value = SyncStatus.UPLOADING
                _syncProgress.value = 40

                val request = SyncRequest(
                    userId = userId,
                    deviceId = deviceId,
                    lastSyncTime = lastSync,
                    notes = notesToSync.map { it.toSyncNote() }
                )

                //发送请求
                Log.d(TAG, "发送同步请求...")
                val response = RetrofitClient.syncApiService.syncNotes(token, request)

                if (!response.isSuccessful) {
                    _syncStatus.value = SyncStatus.FAILED
                    _syncProgress.value = 0
                    return@withContext SyncResult(
                        isSuccess = false,
                        message = "请求失败: ${response.code()}"
                    )
                }

                val syncResponse = response.body() ?: run {
                    _syncStatus.value = SyncStatus.FAILED
                    _syncProgress.value = 0
                    return@withContext SyncResult(
                        isSuccess = false,
                        message = "响应为空"
                    )
                }

                if (!syncResponse.success) {
                    _syncStatus.value = SyncStatus.FAILED
                    _syncProgress.value = 0
                    return@withContext SyncResult(
                        isSuccess = false,
                        message = syncResponse.message
                    )
                }

                //处理服务器响应
                _syncStatus.value = SyncStatus.DOWNLOADING
                _syncProgress.value = 70

                val serverNotes = syncResponse.notes
                Log.d(TAG, "收到服务器笔记: ${serverNotes.size} 条")

                //合并数据到数据库
                _syncStatus.value = SyncStatus.MERGING
                _syncProgress.value = 85

                val mergedCount = mergeServerNotes(serverNotes)

                //更新同步时间
                saveLastSyncTime(syncResponse.newLastSyncTime)

                _syncStatus.value = SyncStatus.COMPLETED
                _syncProgress.value = 100

                val result = SyncResult(
                    isSuccess = true,
                    message = "同步完成",
                    syncedCount = notesToSync.size + mergedCount
                )

                Log.d(TAG, "同步成功: $result")
                return@withContext result

            } catch (e: Exception) {
                Log.e(TAG, "同步异常", e)
                _syncStatus.value = SyncStatus.FAILED
                _syncProgress.value = 0
                return@withContext SyncResult(
                    isSuccess = false,
                    message = "同步失败: ${e.message}"
                )
            }
        }
    }

    private suspend fun getNotesToSync(lastSyncTime: Long): List<Note> {
        return try {
            val allNotes = noteDao.getAllNoteBy().first()
            allNotes.filter { note ->
                note.updatedAt > lastSyncTime && !note.isDeleted
            }
        } catch (e: Exception) {
            Log.e(TAG, "从数据库读取笔记失败", e)
            emptyList()
        }
    }

    private suspend fun mergeServerNotes(serverNotes: List<SyncNote>): Int {
        var mergedCount = 0

        serverNotes.forEach { serverNote ->
            try {
                val localNote = noteDao.getNoteById(serverNote.id)

                if (localNote == null) {
                    //新增笔记
                    val noteToSave = serverNote.toNote()
                    noteDao.insertNote(noteToSave)
                    Log.d(TAG, "插入新笔记到数据库: ${noteToSave.id} - ${noteToSave.title}")
                    mergedCount++
                } else if (serverNote.updatedAt > localNote.updatedAt) {
                    //更新笔记
                    val noteToUpdate = serverNote.toNote()
                    noteDao.updateNote(noteToUpdate)
                    Log.d(TAG, "更新数据库笔记: ${noteToUpdate.id} - ${noteToUpdate.title}")
                    mergedCount++
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理服务器笔记失败: ${serverNote.id}", e)
            }
        }

        return mergedCount
    }

    //获取数据库中所有笔记
    suspend fun getAllNotesFromDb(): List<Note> {
        return try {
            noteDao.getAllNoteBy().first()
        } catch (e: Exception) {
            Log.e(TAG, "获取所有笔记失败", e)
            emptyList()
        }
    }

    //插入测试笔记到数据库
    suspend fun insertTestNoteToDb(title: String = "测试笔记"): Note {
        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            content = "自动生成的测试内容,创建于 ${System.currentTimeMillis()}",
            imageUris = emptyList(),
            todoItems = listOf(
                TodoItem(text = "测试待办1", isCompleted = false),
                TodoItem(text = "测试待办2", isCompleted = true)
            ),
            isArchived = false,
            isDeleted = false,
            isPinned = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        noteDao.insertNote(note)
        Log.d(TAG, "插入测试笔记: ${note.id}")
        return note
    }

    //删除数据库中的所有笔记
    suspend fun deleteAllNotesFromDb() {
        try {
            noteDao.deleteAllNotes()
            Log.d(TAG, "已删除所有笔记")
        } catch (e: Exception) {
            Log.e(TAG, "删除笔记失败", e)
        }
    }

    //获取数据库中的笔记数量
    suspend fun getNoteCountFromDb(): Int {
        return try {
            val allNotes = noteDao.getAllNoteBy().first()
            allNotes.size
        } catch (e: Exception) {
            Log.e(TAG, "获取笔记数量失败", e)
            0
        }
    }

    private suspend fun loadLastSyncTime() {
        try {
            context.dataStore.data.collect { prefs ->
                _lastSyncTime.value = prefs[LAST_SYNC_TIME_KEY] ?: 0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载最后通不时间失败", e)
        }
    }

    private suspend fun saveLastSyncTime(time: Long) {
        try {
            context.dataStore.edit { prefs ->
                prefs[LAST_SYNC_TIME_KEY] = time
            }
            _lastSyncTime.value = time
        } catch (e: Exception) {
            Log.e(TAG, "保存最后同步时间失败", e)
        }
    }

    fun resetSync() {
        _syncStatus.value = SyncStatus.IDLE
        _syncProgress.value = 0
    }

    fun startBackgroundSync(userId: Long, token: String = "") {
        syncScope.launch {
            performSync(userId, token)
        }
    }
}

// 转换函数
private fun Note.toSyncNote(): SyncNote {
    return SyncNote(
        id = this.id,
        title = this.title,
        content = this.content,
        imageUris = this.imageUris,
        todoItems = this.todoItems.map { it.toSyncTodoItem() },
        isArchived = this.isArchived,
        isDeleted = this.isDeleted,
        isPinned = this.isPinned,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

private fun TodoItem.toSyncTodoItem(): SyncTodoItem {
    return SyncTodoItem(
        id = this.id,
        text = this.text,
        isCompleted = this.isCompleted
    )
}

private fun SyncNote.toNote(): Note {
    return Note(
        id = this.id,
        title = this.title,
        content = this.content,
        imageUris = this.imageUris,
        todoItems = this.todoItems.map { it.toTodoItem() },
        isArchived = this.isArchived,
        isDeleted = this.isDeleted,
        isPinned = this.isPinned,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

private fun SyncTodoItem.toTodoItem(): TodoItem {
    return TodoItem(
        id = this.id,
        text = this.text,
        isCompleted = this.isCompleted
    )
}