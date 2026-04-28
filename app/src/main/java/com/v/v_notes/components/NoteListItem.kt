package com.v.v_notes.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v.v_notes.control.removeHtmlTags
import com.v.v_notes.data.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListItem(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            //alpha动画
            .then(modifier),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //笔记内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title.ifEmpty { "无标题" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val plainContent = removeHtmlTags(note.content)
                if (plainContent.isNotEmpty()) {
                    Text(
                        text = plainContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (note.todoItems.isNotEmpty()) {
                    val showTodoItems = note.todoItems.take(3) //最多3个
                    val remainingCount = note.todoItems.size - 3

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        showTodoItems.forEachIndexed { index, todoItem ->
                            val textColor = if (isSelected) {
                                if (todoItem.isCompleted) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                }
                            } else {
                                if (todoItem.isCompleted) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            }

                            val textDecoration = if (todoItem.isCompleted) {
                                androidx.compose.ui.text.style.TextDecoration.LineThrough
                            } else {
                                androidx.compose.ui.text.style.TextDecoration.None
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor,
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                Text(
                                    text = todoItem.text,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = textDecoration
                                    ),
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (index == 2 && remainingCount > 0) {
                                Text(
                                    text = "... 还有${remainingCount}个待办项",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    },
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                } else if (plainContent.isEmpty() && note.todoItems.isEmpty()) {
                    Text(
                        text = "空笔记",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", LocalLocale.current.platformLocale)
                    val dateStr = dateFormat.format(Date(note.createdAt))

                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (note.imageUris.isNotEmpty()) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_gallery),
                                contentDescription = "图片",
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                }
                            )
                            Text(
                                text = " ${note.imageUris.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        if (note.todoItems.isNotEmpty()) {
                            val completedCount = note.todoItems.count { it.isCompleted }
                            Text(
                                text = "待办: $completedCount/${note.todoItems.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .padding(start = 8.dp)
            ) {
                val checkboxAlpha by animateFloatAsState(
                    targetValue = if (isSelectionMode) 1f else 0f,
                    animationSpec = tween(200)
                )

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    enabled = isSelectionMode,
                    modifier = Modifier.alpha(checkboxAlpha)
                )
            }
        }
    }
}