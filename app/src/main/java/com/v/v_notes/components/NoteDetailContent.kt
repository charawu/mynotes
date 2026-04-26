package com.v.v_notes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem
import com.v.v_notes.components.ImagePreviewGrid
import com.v.v_notes.components.NoteMetaInfo
import com.v.v_notes.components.TodoListPreview

@Composable
fun NoteDetailContent(
    note: Note,
    richTextState: com.mohamedrejeb.richeditor.model.RichTextState,
    onImageClick: (String, List<String>) -> Unit = { _, _ -> },
    onTodoItemToggled: ((Int, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 将待办项转换为可观察的状态列表
    val todoItemsState = remember { mutableStateListOf<TodoItem>().apply {
        addAll(note.todoItems)
    } }

    // 当传入的note更新时，同步更新本地状态
    LaunchedEffect(note.todoItems) {
        if (todoItemsState.size != note.todoItems.size ||
            !todoItemsState.zip(note.todoItems).all { (a, b) -> a == b }) {
            todoItemsState.clear()
            todoItemsState.addAll(note.todoItems)
        }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 笔记标题
            Text(
                text = note.title.ifEmpty { "无标题" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 🔧 修复：无论是否有待办事项，只要有内容就显示
        if (note.content.isNotEmpty()) {
            item {
                // 内容区域标题
                Text(
                    text = "内容",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                // 富文本编辑器（只读模式显示HTML内容）
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp),
                        readOnly = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // 如果有图片URI，显示图片预览
        if (note.imageUris.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "图片",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "图片 (${note.imageUris.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // 图片预览网格
                            ImagePreviewGrid(
                                imageUris = note.imageUris,
                                onImageClick = { index ->
                                    onImageClick(note.imageUris[index], note.imageUris)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 如果有待办事项，显示待办列表
        if (todoItemsState.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "待办事项",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        TodoListPreviewWithToggle(
                            todoItems = todoItemsState,
                            onTodoItemToggled = onTodoItemToggled,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // 底部元信息
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                NoteMetaInfo(note = note)
            }
        }

        // 添加一些底部空白
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 可点击切换的待办列表预览组件
 */
@Composable
fun TodoListPreviewWithToggle(
    todoItems: MutableList<TodoItem>,
    onTodoItemToggled: ((Int, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val completedCount = todoItems.count { it.isCompleted }
    val progress = if (todoItems.isNotEmpty()) {
        completedCount.toFloat() / todoItems.size.toFloat()
    } else {
        0f
    }

    Column(
        modifier = modifier
    ) {
        // 进度条和统计信息
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            // 进度条
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            // 统计信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "$completedCount/${todoItems.size} 已完成",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // 显示待办事项列表
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            todoItems.forEachIndexed { index, todoItem ->
                TodoItemWithToggle(
                    todoItem = todoItem,
                    index = index,
                    onToggled = { isChecked ->
                        // 1. 首先更新本地UI状态
                        val updatedTodoItem = todoItem.copy(isCompleted = isChecked)
                        todoItems[index] = updatedTodoItem

                        // 2. 通知父组件更新数据库
                        onTodoItemToggled?.invoke(index, isChecked)
                    }
                )
            }
        }
    }
}

/**
 * 可点击切换的待办项组件
 */
@Composable
fun TodoItemWithToggle(
    todoItem: TodoItem,
    index: Int,
    onToggled: ((Boolean) -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 点击时切换待办项状态
                onToggled?.invoke(!todoItem.isCompleted)
            }
    ) {
        Checkbox(
            checked = todoItem.isCompleted,
            onCheckedChange = { isChecked ->
                // 复选框点击时更新状态
                onToggled?.invoke(isChecked)
            },
            modifier = Modifier.padding(end = 12.dp)
        )

        Text(
            text = "${index + 1}. ${todoItem.text}",
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (todoItem.isCompleted)
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                else
                    androidx.compose.ui.text.style.TextDecoration.None
            ),
            color = if (todoItem.isCompleted)
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}