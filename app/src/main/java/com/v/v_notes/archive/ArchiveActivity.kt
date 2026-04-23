package com.v.v_notes.archive

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import com.v.v_notes.components.*
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.ui.theme.MyNotesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import com.v.v_notes.NoteDetailActivity
import com.v.v_notes.data.model.Note
import com.v.v_notes.viewmodel.NoteViewModel
import com.v.v_notes.factory.NoteViewModelFactory
import com.v.v_notes.MainActivity
import com.v.v_notes.R
import com.v.v_notes.control.SettingsManager
import com.v.v_notes.setting.SettingActivity
import com.v.v_notes.trash.TrashActivity

class ArchiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ArchiveScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ArchiveScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 选中的笔记ID列表
    val selectedNoteIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    // 控制删除确认对话框
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnarchiveDialog by remember { mutableStateOf(false) }

    // 菜单展开状态
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(3) } // 默认选中Archive(3)

    // 获取是否使用底部菜单的设置
    val useBottomMenu by remember { mutableStateOf(SettingsManager.getBoolean("fixed_menu")) }

    // 获取数据库实例
    val database = remember { NoteDatabase.getInstance(context) }

    // 查询归档笔记
    val archivedNotesFlow = database.noteDao().getAllArchivedNotes()
    val archivedNotes by archivedNotesFlow.collectAsState(initial = emptyList())

    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            application = LocalContext.current.applicationContext as Application
        )
    )

    // 删除已归档笔记
    val deleteSelectedNotes = {
        selectedNoteIds.forEach { noteId ->
            coroutineScope.launch(Dispatchers.IO) {
                database.noteDao().updateDeleteStatus(noteId, true)
            }
        }
        selectedNoteIds.clear()
    }

    // 取消归档（恢复到正常状态）
    val unarchiveSelectedNotes = {
        selectedNoteIds.forEach { noteId ->
            coroutineScope.launch(Dispatchers.IO) {
                database.noteDao().updateArchiveStatus(noteId, false)
            }
        }
        selectedNoteIds.clear()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            Crossfade(
                targetState = isSelectionMode,
                animationSpec = tween(300)
            ) { isSelection ->
                if (isSelection) {
                    SelectionTopBar(
                        selectedCount = selectedNoteIds.size,
                        onDeleteClick = { showDeleteDialog = true },
                        onUnarchiveClick = { showUnarchiveDialog = true },
                        onShareClick = { /* TODO: 实现分享功能 */ },
                        onCancelSelection = { selectedNoteIds.clear() }
                    )
                } else {
                    ArchiveTopBar(
                        onMenuClick = { isMenuExpanded = true },
                        archiveCount = archivedNotes.size
                    )
                }
            }

            // 顶部菜单
            Menu(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart),
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                onItemSelected = { itemId ->
                    selectedItem = itemId
                    when (itemId) {
                        1 -> { // Keep/主界面
                            selectedItem = 1
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                            onBackClick() // 关闭当前页面
                        }
                        2 -> { // Alert/提醒
                            selectedItem = 2
                            // TODO: 实现提醒页面
                        }
                        3 -> { // Archive/归档 - 当前页面，不跳转
                            selectedItem = 3
                        }
                        4 -> { // Trash/回收站
                            selectedItem = 4
                            val intent = Intent(context, TrashActivity::class.java)
                            context.startActivity(intent)
                            onBackClick() // 关闭当前页面
                        }
                        5 -> { // Setting/设置
                            selectedItem = 5
                            val intent = Intent(context, SettingActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                },
                selectedItem = selectedItem,
                showOnlyAlertAndSetting = useBottomMenu
            )

            // 笔记列表区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                ArchiveNotesListScreen(
                    archivedNotes = archivedNotes,
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

            // 如果使用底部菜单，则显示底部导航
            if (useBottomMenu) {
                BottomNavMenu(
                    selectedItem = selectedItem,
                    onItemSelected = { itemId ->
                        selectedItem = itemId
                        when (itemId) {
                            1 -> { // Keep/主界面
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                                onBackClick() // 关闭当前页面
                            }
                            3 -> { // Archive/归档 - 当前页面，不跳转
                                selectedItem = 3
                            }
                            4 -> { // Trash/回收站
                                val intent = Intent(context, TrashActivity::class.java)
                                context.startActivity(intent)
                                onBackClick() // 关闭当前页面
                            }
                        }
                    }
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除归档笔记") },
            text = {
                Text("确定要永久删除选中的 ${selectedNoteIds.size} 条归档笔记吗？此操作不可恢复。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteSelectedNotes()
                    }
                ) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 取消归档确认对话框
    if (showUnarchiveDialog) {
        AlertDialog(
            onDismissRequest = { showUnarchiveDialog = false },
            title = { Text("取消归档") },
            text = {
                Text("确定要将选中的 ${selectedNoteIds.size} 条笔记移出归档吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnarchiveDialog = false
                        unarchiveSelectedNotes()
                    }
                ) {
                    Text("移出归档")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnarchiveDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 归档页面顶部工具栏（普通模式）
 */
@Composable
fun ArchiveTopBar(
    onMenuClick: () -> Unit,
    archiveCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .statusBarsPadding()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 菜单按钮（替换原来的返回按钮）
        IconButton(onClick = onMenuClick) {
            Icon(
                painter = painterResource(R.drawable.baseline_menu_24),
                contentDescription = "菜单"
            )
        }

        // 标题和归档数量
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = "归档",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$archiveCount 条笔记",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 归档页面选择模式顶部工具栏
 */
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onDeleteClick: () -> Unit,
    onUnarchiveClick: () -> Unit,
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

        // 取消归档按钮
        IconButton(
            onClick = onUnarchiveClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "取消归档",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ArchiveNotesListScreen(
    archivedNotes: List<Note>,
    selectedNoteIds: List<String>,
    isSelectionMode: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongPress: (String) -> Unit
) {
    if (archivedNotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = "空归档",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    text = "归档为空",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "归档的笔记将在这里显示",
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
            items(archivedNotes) { note ->
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