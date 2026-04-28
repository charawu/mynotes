package com.v.v_notes.components

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.v.v_notes.addlist.RichTextEditorActivity
import com.v.v_notes.control.NoteShareHelper
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.data.repository.NoteRepositoryImpl
import com.v.v_notes.factory.NoteDetailViewModelFactory
import com.v.v_notes.ui.note_detail.NoteDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalRichTextApi::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onBackClick: () -> Unit,
    onImageClick: (String, List<String>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val viewModel: NoteDetailViewModel = viewModel(
        factory = NoteDetailViewModelFactory(
            application = context.applicationContext as Application,
            noteRepository = NoteRepositoryImpl(NoteDatabase.getInstance(context).noteDao())
        )
    )

    val note by viewModel.note.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val database = NoteDatabase.getInstance(context)
    val scope = rememberCoroutineScope()

    var showPermanentDeleteDialog by remember { mutableStateOf(false) }
    var showShareOptionsDialog by remember { mutableStateOf(false) }

    val richTextState = rememberRichTextState()

    //图片预览刷新key
    var imageRefreshKey by remember { mutableIntStateOf(0) }

    //当笔记加载完成后,设置HTML内容
    LaunchedEffect(note) {
        note?.let { loadedNote ->
            withContext(Dispatchers.Main) {
                richTextState.setHtml(loadedNote.content)
            }
        }
    }

    val editNoteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        //强制刷新,没招了
        if (result.resultCode == android.app.Activity.RESULT_OK ||
            result.resultCode == android.app.Activity.RESULT_CANCELED) {
            scope.launch {
                viewModel.loadNote(noteId)

                imageRefreshKey++
            }
        }
    }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)

        imageRefreshKey = 0
    }

    val imageUris = remember(note) {
        if (note != null) {
            NoteShareHelper.convertImageUris(context, note!!.imageUris)
        } else {
            emptyList()
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = note?.title?.ifEmpty { "无标题" } ?: "笔记详情",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (note != null) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                //删除/恢复菜单
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
                                            Text(
                                                if (note!!.isDeleted) {
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
                                                if (note!!.isDeleted) {
                                                    database.noteDao().updateDeleteStatus(noteId, false)
                                                } else {
                                                    database.noteDao().updateDeleteStatus(noteId, true)
                                                }
                                            }

                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    if (note!!.isDeleted) "已恢复" else "已移到回收站",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            if (note!!.isDeleted) {
                                                delay(500)
                                                onBackClick()
                                            }
                                        }
                                    }
                                )

                                if (note!!.isDeleted) {
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

                                if (!note!!.isDeleted) {
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
                                                    if (note!!.isArchived) "取消归档"
                                                    else "归档"
                                                )
                                            }
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onBackClick()
                                            scope.launch {
                                                val newArchiveStatus = !note!!.isArchived

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

                                                if (newArchiveStatus) {
                                                    delay(500)
                                                    onBackClick()
                                                }
                                            }
                                        }
                                    )
                                }

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
                                        if (note == null) {
                                            Toast.makeText(context, "无法分享，笔记不存在", Toast.LENGTH_SHORT).show()
                                            return@DropdownMenuItem
                                        }

                                        if (imageUris.isEmpty()) {
                                            NoteShareHelper.shareNote(
                                                context = context,
                                                note = note!!,
                                                imageUris = emptyList(),
                                                shareType = NoteShareHelper.ShareType.TEXT,
                                                chooserTitle = "分享笔记: ${note!!.title}"
                                            )
                                        } else {
                                            showShareOptionsDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (note != null) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            if (!note!!.isDeleted) {
                                val intent = Intent(
                                    context,
                                    RichTextEditorActivity::class.java
                                ).apply {
                                    putExtra("noteId", noteId)
                                }
                                editNoteLauncher.launch(intent)
                            } else {
                                Toast.makeText(context, "已删除的笔记无法编辑", Toast.LENGTH_SHORT).show()
                            }
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "标题",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        //标题内容
                        Text(
                            text = note!!.title.ifEmpty { "无标题" },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth(),
                            color = if (note!!.isDeleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                //正文
                if (note!!.content.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                if (!note!!.isDeleted) {
                                    val intent = Intent(
                                        context,
                                        RichTextEditorActivity::class.java
                                    ).apply {
                                        putExtra("noteId", noteId)
                                    }

                                    editNoteLauncher.launch(intent)

                                } else {
                                    Toast.makeText(context, "已删除的笔记无法编辑", Toast.LENGTH_SHORT).show()
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "内容",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            RichTextEditor(
                                state = richTextState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(top = 8.dp),
                                readOnly = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                //待办事项
                if (note!!.todoItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                if (!note!!.isDeleted) {
                                    val intent = Intent(
                                        context,
                                        RichTextEditorActivity::class.java
                                    ).apply {
                                        putExtra("noteId", noteId)
                                    }
                                    editNoteLauncher.launch(intent)
                                } else {
                                    Toast.makeText(context, "已删除的笔记不能编辑", Toast.LENGTH_SHORT).show()
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        TodoListPreviewWithToggle(
                            todoItems = note!!.todoItems.toMutableList(),
                            onTodoItemToggled = { index, isCompleted ->
                                val todoItem = note!!.todoItems.getOrNull(index)
                                todoItem?.let {
                                    scope.launch {
                                        viewModel.updateTodoItemStatus(it.id, isCompleted)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                //图片预览
                if (note!!.imageUris.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                if (!note!!.isDeleted) {
                                    val intent = Intent(
                                        context,
                                        RichTextEditorActivity::class.java
                                    ).apply {
                                        putExtra("noteId", noteId)
                                    }
                                    editNoteLauncher.launch(intent)
                                } else {
                                    Toast.makeText(context, "已删除的笔记无法编辑", Toast.LENGTH_SHORT).show()
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "图片",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "图片 (${note!!.imageUris.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            ImagePreviewGrid(
                                imageUris = note!!.imageUris,
                                onImageClick = { index ->
                                    onImageClick(note!!.imageUris[index], note!!.imageUris)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                refreshKey = imageRefreshKey,
                                onImageEditSuccess = {
                                    imageRefreshKey++

                                    scope.launch {
                                        viewModel.loadNote(noteId)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                //统计
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    NoteMetaInfo(note = note!!)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "笔记不存在",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (showShareOptionsDialog && note != null) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("选择分享方式")
                    },
            text = {
                Column {
                    Text("请选择要分享的内容：")

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        item {
                            ShareOptionItem(
                                icon = Icons.Default.TextFields,
                                title = "仅分享文本",
                                description = "只分享笔记的文本内容",
                                onClick = {
                                    showShareOptionsDialog = false
                                    NoteShareHelper.shareNote(
                                        context = context,
                                        note = note!!,
                                        imageUris = emptyList(),
                                        shareType = NoteShareHelper.ShareType.TEXT,
                                        chooserTitle = "分享笔记: ${note!!.title}"
                                    )
                                }
                            )
                        }

                        if (imageUris.isNotEmpty()) {
                            item {
                                ShareOptionItem(
                                    icon = Icons.Default.Image,
                                    title = "仅分享图片",
                                    description = "只分享笔记中的图片",
                                    onClick = {
                                        showShareOptionsDialog = false
                                        NoteShareHelper.shareNote(
                                            context = context,
                                            note = note!!,
                                            imageUris = imageUris,
                                            shareType = NoteShareHelper.ShareType.IMAGE,
                                            chooserTitle = "分享图片: ${note!!.title}"
                                        )
                                    }
                                )
                            }
                        }

                        item {
                            ShareOptionItem(
                                icon = Icons.Default.Share,
                                title = "分享全部",
                                description = "分享文本和所有图片",
                                onClick = {
                                    showShareOptionsDialog = false
                                    NoteShareHelper.shareNote(
                                        context = context,
                                        note = note!!,
                                        imageUris = imageUris,
                                        shareType = NoteShareHelper.ShareType.ALL,
                                        chooserTitle = "分享笔记: ${note!!.title}"
                                    )
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { showShareOptionsDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    if (showPermanentDeleteDialog && note != null) {
        AlertDialog(
            onDismissRequest = { showPermanentDeleteDialog = false },
            title = { Text("移动到回收站") },
            text = { Text("确定要移动这条笔记吗?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermanentDeleteDialog = false
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                database.noteDao().deleteNote(note!!)
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "已移动到回收站",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        onBackClick()
                    }
                ) {
                    Text("移动")
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