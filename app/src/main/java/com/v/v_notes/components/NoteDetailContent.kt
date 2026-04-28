package com.v.v_notes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem

@Composable
fun NoteDetailContent(
    note: Note,
    richTextState: com.mohamedrejeb.richeditor.model.RichTextState,
    onImageClick: (String, List<String>) -> Unit = { _, _ -> },
    onTodoItemToggled: ((Int, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    //转换待办
    val todoItemsState = remember { mutableStateListOf<TodoItem>().apply {
        addAll(note.todoItems)
    } }

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
            //标题
            Text(
                text = note.title.ifEmpty { "无标题" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (note.content.isNotEmpty()) {
            item {
                //内容标题
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

        //判断待办
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

        //信息
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

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


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
        //进度统计
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )


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

        //待办列表
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            todoItems.forEachIndexed { index, todoItem ->
                TodoItemWithToggle(
                    todoItem = todoItem,
                    index = index,
                    onToggled = { isChecked ->
                        val updatedTodoItem = todoItem.copy(isCompleted = isChecked)
                        todoItems[index] = updatedTodoItem

                        onTodoItemToggled?.invoke(index, isChecked)
                    }
                )
            }
        }
    }
}


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
                //点击切换待办项状态
                onToggled?.invoke(!todoItem.isCompleted)
            }
    ) {
        Checkbox(
            checked = todoItem.isCompleted,
            onCheckedChange = { isChecked ->
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