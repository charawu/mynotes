package com.v.v_notes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v.v_notes.addlist.RichTextEditorActivity
import com.v.v_notes.components.AddButton
import com.v.v_notes.components.AddButtonList
import com.v.v_notes.components.Menu
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.data.model.Note
import com.v.v_notes.setting.SettingActivity
import com.v.v_notes.ui.theme.MyNotesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    MyNotesApp()
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MyNotesApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 状态管理
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(1) }
    var isActive by remember { mutableStateOf(false) }

    // 选中的笔记ID列表
    val selectedNoteIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    // 控制删除确认对话框
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 🔴 获取数据库实例
    val database = remember { NoteDatabase.getInstance(context) }

    // 🔴 修复：正确收集笔记流
    val noteFlow = database.noteDao().getAllNotes()
    val allNotes by noteFlow.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        Log.d("MainActivity", "应用启动，开始加载笔记")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔴 修复：使用Crossfade替代AnimatedVisibility避免布局抖动
            Crossfade(
                targetState = isSelectionMode,
                animationSpec = tween(300)
            ) { isSelection ->
                if (isSelection) {
                    SelectionTopBar(
                        selectedCount = selectedNoteIds.size,
                        onDeleteClick = { showDeleteDialog = true },
                        onArchiveClick = { /* TODO: 实现归档功能 */ },
                        onShareClick = { /* TODO: 实现分享功能 */ },
                        onCancelSelection = { selectedNoteIds.clear() }
                    )
                } else {
                    NormalTopBar(
                        onMenuClick = { isMenuExpanded = true },
                        onAccountClick = { /* 处理账户点击 */ }
                    )
                }
            }

            Menu(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart),
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                onItemSelected = { itemId ->
                    selectedItem = itemId
                    when (itemId) {
                        1 -> { selectedItem = 1 }
                        2 -> { selectedItem = 2 }
                        3 -> { selectedItem = 3 }
                        4 -> { selectedItem = 4 }
                        5 -> {
                            selectedItem = 5
                            val intent = Intent(context, SettingActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                },
                selectedItem = selectedItem
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NotesListScreen(
                    allNotes = allNotes,
                    selectedNoteIds = selectedNoteIds,
                    isSelectionMode = isSelectionMode,
                    onNoteClick = { noteId ->
                        if (isSelectionMode) {
                            // 在选择模式下，点击切换选中状态
                            if (selectedNoteIds.contains(noteId)) {
                                selectedNoteIds.remove(noteId)
                            } else {
                                selectedNoteIds.add(noteId)
                            }
                        } else {
                            // 普通模式下，打开笔记详情
                            val intent = NoteDetailActivity.newIntent(context, noteId)
                            context.startActivity(intent)
                        }
                    },
                    onNoteLongPress = { noteId ->
                        // 长按进入选择模式
                        if (!selectedNoteIds.contains(noteId)) {
                            selectedNoteIds.add(noteId)
                        }
                    }
                )
            }
        }

        // 浮动按钮区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 65.dp)
            ) {
                AddButtonList(
                    expanded = isActive,
                    onPhotoClick = { isActive = false },
                    onDrawClick = { isActive = false },
                    onCheckClick = { isActive = false },
                    onTextClick = {
                        isActive = false
                        val intent = Intent(context, RichTextEditorActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

            AddButton(
                isActive = isActive,
                onToggle = { isActive = it }
            )
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除笔记") },
            text = {
                Text("确定要删除选中的 ${selectedNoteIds.size} 条笔记吗？此操作不可恢复。")
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // 🔴 修复：使用新的删除方法
                        deleteSelectedNotes(context, selectedNoteIds.toList(), coroutineScope)
                        // 清空选择列表
                        selectedNoteIds.clear()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 🔴 完全重写的删除选中的笔记函数
 */
private fun deleteSelectedNotes(
    context: android.content.Context,
    noteIds: List<String>,
    coroutineScope: CoroutineScope
) {
    if (noteIds.isEmpty()) {
        Log.d("NoteDeletion", "没有选中任何笔记")
        return
    }

    coroutineScope.launch(Dispatchers.IO) {
        try {
            Log.d("NoteDeletion", "开始删除选中的 ${noteIds.size} 条笔记")

            // 获取数据库实例
            val database = NoteDatabase.getInstance(context)

            // 🔴 关键修复：直接通过ID查询单个笔记并删除
            noteIds.forEach { noteId ->
                try {
                    // 尝试通过ID获取笔记
                    val noteFlow = database.noteDao().getAllNotes()
                    val notes = noteFlow.firstOrNull() ?: emptyList()
                    val note = notes.find { it.id == noteId }

                    if (note != null) {
                        Log.d("NoteDeletion", "找到并删除笔记: ${note.title} (ID: ${note.id})")
                        database.noteDao().deleteNote(note)
                    } else {
                        Log.w("NoteDeletion", "未找到笔记 ID: $noteId")
                    }
                } catch (e: Exception) {
                    Log.e("NoteDeletion", "删除笔记 $noteId 时出错: ${e.message}", e)
                }
            }

            Log.d("NoteDeletion", "删除操作完成")
        } catch (e: Exception) {
            Log.e("NoteDeletion", "删除笔记过程中出错: ${e.message}", e)
        }
    }
}

/**
 * 普通模式顶部工具栏
 */
@Composable
fun NormalTopBar(
    onMenuClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                painter = painterResource(R.drawable.baseline_menu_24),
                contentDescription = "菜单"
            )
        }

        // 搜索栏
        Card(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = {})
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(0.5f),
                        text = stringResource(R.string.search_box),
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = {}) {
                        Icon(
                            tint = MaterialTheme.colorScheme.primary,
                            painter = painterResource(R.drawable.baseline_splitscreen_24),
                            contentDescription = "分屏"
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            tint = MaterialTheme.colorScheme.primary,
                            painter = painterResource(R.drawable.baseline_import_export_24),
                            contentDescription = "导入导出"
                        )
                    }
                }
            }
        }

        // 账户按钮
        IconButton(onClick = onAccountClick) {
            Icon(
                painter = painterResource(R.drawable.ic_account_box),
                contentDescription = "账户"
            )
        }
    }
}

/**
 * 选择模式顶部工具栏
 */
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onDeleteClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onShareClick: () -> Unit,
    onCancelSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回/取消选择按钮
        IconButton(
            onClick = onCancelSelection,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "取消选择",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // 选中数量
        Text(
            text = "已选中 $selectedCount 项",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        // 删除按钮
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // 分享按钮
        IconButton(
            onClick = onShareClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "分享",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // 归档按钮
        IconButton(
            onClick = onArchiveClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "归档",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun NotesListScreen(
    allNotes: List<Note>,
    selectedNoteIds: List<String>,
    isSelectionMode: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongPress: (String) -> Unit
) {
    if (allNotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "暂无笔记",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "点击右下角按钮创建第一条笔记",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allNotes) { note ->
                NoteListItem(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    isSelectionMode = isSelectionMode,
                    onClick = { onNoteClick(note.id) },
                    onLongPress = { onNoteLongPress(note.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListItem(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
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
            // 笔记内容
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
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

            // 🔴 修复：复选框移到右侧，并使用固定宽度避免布局抖动
            Box(
                modifier = Modifier
                    .width(48.dp)  // 固定宽度，防止布局抖动
                    .padding(start = 8.dp)
            ) {
                // 🔴 修复：使用Alpha动画而不是显示/隐藏，避免布局抖动
                val alpha by animateFloatAsState(
                    targetValue = if (isSelectionMode) 1f else 0f,
                    animationSpec = tween(300)
                )

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    enabled = isSelectionMode,
                    modifier = Modifier.alpha(alpha)
                )
            }
        }
    }
}

private fun removeHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyNotesTheme {
        MyNotesApp()
    }
}