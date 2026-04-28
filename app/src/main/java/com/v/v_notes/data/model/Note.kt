package com.v.v_notes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val imageUris: List<String> = emptyList(),
    val todoItems: List<TodoItem> = emptyList(),

    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)