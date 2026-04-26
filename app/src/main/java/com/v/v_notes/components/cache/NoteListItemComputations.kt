package com.v.v_notes.components.cache

import androidx.compose.runtime.derivedStateOf
import com.v.v_notes.control.removeHtmlTags
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 性能优化的计算缓存
 * 将耗时的计算放到这里，避免在Composable中重复计算
 */
internal class NoteListItemComputations(
    note: Note
) {
    val note = note

    // 缓存移除HTML标签的结果
    val plainContent = derivedStateOf {
        removeHtmlTags(note.content)
    }

    // 缓存日期格式化结果
    val formattedDate = derivedStateOf {
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            .format(Date(note.createdAt))
    }

    // 缓存待办事项统计
    val todoStats = derivedStateOf {
        if (note.todoItems.isEmpty()) {
            null
        } else {
            val completedCount = note.todoItems.count { it.isCompleted }
            val displayItems = note.todoItems.take(3)
            val remainingCount = maxOf(0, note.todoItems.size - 3)

            TodoStats(
                completedCount = completedCount,
                totalCount = note.todoItems.size,
                displayItems = displayItems,
                remainingCount = remainingCount
            )
        }
    }
}

/**
 * 待办事项统计信息
 */
internal data class TodoStats(
    val completedCount: Int,
    val totalCount: Int,
    val displayItems: List<TodoItem>,
    val remainingCount: Int
)