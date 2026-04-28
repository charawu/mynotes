package com.v.v_notes.sync.data

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val userId: Long = 0,
    val deviceId: String = "",
    val lastSyncTime: Long = 0,
    val notes: List<SyncNote> = emptyList()
)

@Serializable
data class SyncResponse(
    val success: Boolean = false,
    val message: String = "",
    val newLastSyncTime: Long = 0,
    val notes: List<SyncNote> = emptyList()
)

@Serializable
data class SyncNote(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val imageUris: List<String> = emptyList(),
    val todoItems: List<SyncTodoItem> = emptyList(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class SyncTodoItem(
    val id: String = "",
    val text: String = "",
    val isCompleted: Boolean = false
)

data class SyncResult(
    val isSuccess: Boolean = false,
    val message: String = "",
    val syncedCount: Int = 0
)

enum class SyncStatus {
    IDLE,
    PREPARING,
    UPLOADING,
    DOWNLOADING,
    MERGING,
    COMPLETED,
    FAILED
}