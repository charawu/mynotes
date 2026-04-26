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
import com.v.v_notes.control.NoteShareHelper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Unarchive

class ArchiveActivity : ComponentActivity() {
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

    // 🔴 新增：控制分享选项对话框
    var showShareOptionsDialog by remember { mutableStateOf(false) }
    var notesToShare by remember { mutableStateOf<List<Note>>(emptyList()) }
    var shareDialogTitle by remember { mutableStateOf("分享归档笔记") }

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

    // 🔴 新增：获取选中笔记的图片URI
    val selectedNotesImageUris = remember(selectedNoteIds, archivedNotes) {
        if (selectedNoteIds.isNotEmpty()) {
            val selectedNotes = archivedNotes.filter { it.id in selectedNoteIds }
            val allImageUris = selectedNotes.flatMap { note ->
                NoteShareHelper.convertImageUris(context, note.imageUris)
            }
            allImageUris
        } else {
            emptyList()
        }
    }

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
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                        onShareClick = {
                            // 🔴 修改：实现分享功能
                            val selectedNotes = archivedNotes.filter { it.id in selectedNoteIds }
                            if (selectedNotes.isEmpty()) {
                                Toast.makeText(context, "请先选择笔记", Toast.LENGTH_SHORT).show()
                                return@SelectionTopBar
                            }

                            notesToShare = selectedNotes
                            shareDialogTitle = "分享 ${selectedNotes.size} 条归档笔记"

                            // 检查是否有图片
                            if (selectedNotesImageUris.isEmpty()) {
                                // 没有图片，直接分享文本
                                if (selectedNotes.size == 1) {
                                    // 单条笔记
                                    NoteShareHelper.shareNote(
                                        context = context,
                                        note = selectedNotes.first(),
                                        imageUris = emptyList(),
                                        shareType = NoteShareHelper.ShareType.TEXT,
                                        chooserTitle = "分享归档笔记: ${selectedNotes.first().title}"
                                    )
                                } else {
                                    // 多条笔记，分享合并的文本
                                    shareMultipleNotes(context, selectedNotes, "归档笔记")
                                }
                            } else {
                                // 有图片，显示选项对话框
                                showShareOptionsDialog = true
                            }
                        },
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
            title = { Text("移动归档笔记") },
            text = {
                Text("确定要移动选中的 ${selectedNoteIds.size} 条归档笔记到回收站吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteSelectedNotes()
                    }
                ) {
                    Text("移动")
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
                    Text("移动")
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

    // 🔴 新增：分享选项对话框
    if (showShareOptionsDialog && notesToShare.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                showShareOptionsDialog = false
                notesToShare = emptyList()
            },
            title = { Text(shareDialogTitle) },
            text = {
                Column {
                    Text("请选择要分享的内容：")
                    Spacer(modifier = Modifier.height(16.dp))

                    // 分享选项列表
                    LazyColumn {
                        // 1. 仅分享文本
                        item {
                            ShareOptionItem(
                                icon = Icons.Default.TextFields,
                                title = "仅分享文本",
                                description = "只分享笔记的文本内容",
                                onClick = {
                                    showShareOptionsDialog = false
                                    if (notesToShare.size == 1) {
                                        // 单条笔记
                                        NoteShareHelper.shareNote(
                                            context = context,
                                            note = notesToShare.first(),
                                            imageUris = emptyList(),
                                            shareType = NoteShareHelper.ShareType.TEXT,
                                            chooserTitle = "分享归档笔记: ${notesToShare.first().title}"
                                        )
                                    } else {
                                        // 多条笔记，分享合并的文本
                                        shareMultipleNotes(context, notesToShare, "归档笔记")
                                    }
                                    notesToShare = emptyList()
                                }
                            )
                        }

                        // 2. 仅分享图片（如果有图片）
                        if (selectedNotesImageUris.isNotEmpty()) {
                            item {
                                ShareOptionItem(
                                    icon = Icons.Default.Image,
                                    title = "仅分享图片",
                                    description = "只分享笔记中的图片",
                                    onClick = {
                                        showShareOptionsDialog = false
                                        if (notesToShare.size == 1) {
                                            // 单条笔记
                                            NoteShareHelper.shareNote(
                                                context = context,
                                                note = notesToShare.first(),
                                                imageUris = selectedNotesImageUris,
                                                shareType = NoteShareHelper.ShareType.IMAGE,
                                                chooserTitle = "分享图片: ${notesToShare.first().title}"
                                            )
                                        } else {
                                            // 多条笔记的图片分享
                                            shareMultipleNotesImages(context, notesToShare, selectedNotesImageUris)
                                        }
                                        notesToShare = emptyList()
                                    }
                                )
                            }
                        }

                        // 3. 分享全部
                        item {
                            ShareOptionItem(
                                icon = Icons.Default.Share,
                                title = "分享全部",
                                description = "分享文本和所有图片",
                                onClick = {
                                    showShareOptionsDialog = false
                                    if (notesToShare.size == 1) {
                                        // 单条笔记
                                        NoteShareHelper.shareNote(
                                            context = context,
                                            note = notesToShare.first(),
                                            imageUris = selectedNotesImageUris,
                                            shareType = NoteShareHelper.ShareType.ALL,
                                            chooserTitle = "分享归档笔记: ${notesToShare.first().title}"
                                        )
                                    } else {
                                        // 多条笔记的全部内容分享
                                        shareMultipleNotesAll(context, notesToShare, selectedNotesImageUris)
                                    }
                                    notesToShare = emptyList()
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showShareOptionsDialog = false
                        notesToShare = emptyList()
                    }
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
                imageVector = Icons.Default.Unarchive,
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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
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

@Composable
fun ShareOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 🔴 新增：分享多条笔记的文本
 */
private fun shareMultipleNotes(context: android.content.Context, notes: List<Note>, noteType: String = "笔记") {
    if (notes.isEmpty()) {
        Toast.makeText(context, "没有可分享的$noteType", Toast.LENGTH_SHORT).show()
        return
    }

    val builder = StringBuilder()
    if (notes.size == 1) {
        // 单条笔记
        builder.append(NoteShareHelper.buildShareText(notes.first()))
    } else {
        // 多条笔记
        builder.append("分享 ${notes.size} 条$noteType\n\n")
        notes.forEachIndexed { index, note ->
            builder.append("${index + 1}. ${note.title}\n")
            val plainText = NoteShareHelper.buildShareText(note)
            builder.append(plainText)
            builder.append("\n\n")
        }
    }

    val shareText = builder.toString().trim()
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "分享$noteType")
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享$noteType"))
}

/**
 * 🔴 新增：分享多条笔记的图片
 */
private fun shareMultipleNotesImages(context: android.content.Context, notes: List<Note>, imageUris: List<android.net.Uri>) {
    if (imageUris.isEmpty()) {
        Toast.makeText(context, "没有可分享的图片", Toast.LENGTH_SHORT).show()
        return
    }

    val shareIntent = if (imageUris.size == 1) {
        // 单张图片
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUris[0])
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        // 多张图片
        Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    context.startActivity(Intent.createChooser(shareIntent, "分享图片"))
}

/**
 * 🔴 新增：分享多条笔记的全部内容（文本+图片）
 */
private fun shareMultipleNotesAll(context: android.content.Context, notes: List<Note>, imageUris: List<android.net.Uri>) {
    if (notes.isEmpty()) {
        Toast.makeText(context, "没有可分享的笔记", Toast.LENGTH_SHORT).show()
        return
    }

    // 构建文本内容
    val builder = StringBuilder()
    if (notes.size == 1) {
        builder.append(NoteShareHelper.buildShareText(notes.first()))
    } else {
        builder.append("分享 ${notes.size} 条归档笔记\n\n")
        notes.forEachIndexed { index, note ->
            builder.append("${index + 1}. ${note.title}\n")
            val plainText = NoteShareHelper.buildShareText(note)
            builder.append(plainText)
            builder.append("\n\n")
        }
    }

    val shareText = builder.toString().trim()

    val shareIntent = if (imageUris.isEmpty()) {
        // 没有图片，只分享文本
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "分享归档笔记")
        }
    } else if (imageUris.size == 1) {
        // 单张图片 + 文本
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUris[0])
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "分享归档笔记")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        // 多张图片 + 文本
        Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "分享归档笔记")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    context.startActivity(Intent.createChooser(shareIntent, "分享归档笔记"))
}