package com.v.v_notes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // 改为String类型UUID
    val title: String = "",
    val content: String = "", // 存储富文本HTML内容
    val imageUris: List<String> = emptyList(), // 改为imageUris
    val todoItems: List<TodoItem> = emptyList(), // 使用编辑器的TodoItem
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 使用编辑器中的TodoItem结构
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)