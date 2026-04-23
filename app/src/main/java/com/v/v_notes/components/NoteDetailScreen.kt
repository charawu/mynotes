package com.v.v_notes.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.v.v_notes.addlist.RichTextEditorActivity
import com.v.v_notes.data.database.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,  // 注意：这里改为String类型
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit, // 编辑按钮回调，只传递笔记ID
    onImageClick: (String, List<String>) -> Unit = { _, _ -> }, // 图片点击回调
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = NoteDatabase.getInstance(context)
    val scope = rememberCoroutineScope()

    // 控制对话框状态
    var showPermanentDeleteDialog by remember { mutableStateOf(false) }

    // 从数据库读取笔记
    val noteFlow = database.noteDao().getAllNoteBy()
    val allNotes by noteFlow.collectAsState(initial = emptyList())

    // 从列表中查找特定笔记
    val targetNote = allNotes.find { it.id == noteId }

    // 使用RichTextState来显示HTML内容
    val richTextState = rememberRichTextState()

    // 当笔记加载完成后，设置HTML内容
    LaunchedEffect(targetNote) {
        targetNote?.let { loadedNote ->
            withContext(Dispatchers.Main) {
                // 设置HTML内容到RichTextEditor
                richTextState.setHtml(loadedNote.content)
            }
        }
    }

    // 控制菜单展开状态
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = targetNote?.title?.ifEmpty { "无标题" } ?: "笔记详情",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 菜单按钮
                    if (targetNote != null) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "菜单"
                                )
                            }

                            // 下拉菜单
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                // 1. 编辑菜单项 - 只显示在未删除的笔记
                                if (!targetNote.isDeleted) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("编辑")
                                            }
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            val intent = Intent(
                                                context,
                                                RichTextEditorActivity::class.java
                                            ).apply {
                                                putExtra("noteId", noteId)
                                            }
                                            context.startActivity(intent)
                                        }
                                    )
                                }

                                // 2. 删除/恢复菜单项 - 根据笔记状态显示
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // 根据笔记状态显示不同的文本
                                            Text(
                                                if (targetNote.isDeleted) {
                                                    "恢复"
                                                } else {
                                                    "删除"
                                                }
                                            )
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onBackClick()

                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                if (targetNote.isDeleted) {
                                                    // 恢复笔记
                                                    database.noteDao().updateDeleteStatus(noteId, false)
                                                } else {
                                                    // 移到回收站
                                                    database.noteDao().updateDeleteStatus(noteId, true)
                                                }
                                            }

                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    if (targetNote.isDeleted) "已恢复" else "已移到回收站",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            // 如果是恢复操作，延迟关闭页面
                                            if (targetNote.isDeleted) {
                                                delay(500)
                                                onBackClick()
                                            }
                                        }
                                    }
                                )

                                // 3. 永久删除菜单项 - 只显示在已删除的笔记
                                if (targetNote.isDeleted) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("永久删除")
                                            }
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            showPermanentDeleteDialog = true
                                        }
                                    )
                                }

                                // 4. 归档/取消归档菜单项 - 只显示在未删除的笔记
                                if (!targetNote.isDeleted) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Archive,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    if (targetNote.isArchived) "取消归档"
                                                    else "归档"
                                                )
                                            }
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onBackClick()
                                            scope.launch {
                                                val newArchiveStatus = !targetNote.isArchived

                                                withContext(Dispatchers.IO) {
                                                    database.noteDao().updateArchiveStatus(noteId, newArchiveStatus)
                                                }

                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        if (newArchiveStatus) "已归档" else "已取消归档",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                // 如果是归档操作，延迟关闭页面
                                                if (newArchiveStatus) {
                                                    delay(500)
                                                    onBackClick()
                                                }
                                            }
                                        }
                                    )
                                }

                                // 5. 分享菜单项 - 总是显示
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("分享")
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        // TODO: 实现分享功能
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (targetNote != null) {
            NoteDetailContent(
                note = targetNote,
                richTextState = richTextState,
                onImageClick = onImageClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            // 笔记不存在或加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (allNotes.isNotEmpty()) {
                    // 笔记未找到
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "笔记不存在",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "无法找到ID为 $noteId 的笔记",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                } else {
                    // 正在加载
                    CircularProgressIndicator()
                }
            }
        }
    }

    // 永久删除确认对话框
    if (showPermanentDeleteDialog && targetNote != null) {
        AlertDialog(
            onDismissRequest = { showPermanentDeleteDialog = false },
            title = { Text("永久删除") },
            text = { Text("确定要永久删除这条笔记吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermanentDeleteDialog = false
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                // 永久删除笔记
                                database.noteDao().deleteNote(targetNote)
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "已永久删除",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        onBackClick()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermanentDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}