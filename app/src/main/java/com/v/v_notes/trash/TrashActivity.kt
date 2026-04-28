package com.v.v_notes.trash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import com.v.v_notes.components.NoteListItem
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.ui.theme.MyNotesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import com.v.v_notes.NoteDetailActivity
import com.v.v_notes.data.model.Note
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.v.v_notes.R

class TrashActivity : ComponentActivity() {
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
                    TrashScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun TrashScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedNoteIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var showPermanentDeleteDialog by remember { mutableStateOf(false) }

    //获取数据
    val database = remember { NoteDatabase.getInstance(context) }

    //查询已删除的笔记
    val deletedNotesFlow = database.noteDao().getAllDeletedNotes()
    val deletedNotes by deletedNotesFlow.collectAsState(initial = emptyList())

    //恢复
    val restoreSelectedNotes = {
        selectedNoteIds.forEach { noteId ->
            coroutineScope.launch(Dispatchers.IO) {
                database.noteDao().updateDeleteStatus(noteId, false)
            }
        }
        Toast.makeText(context,
            "已恢复 ${selectedNoteIds.size} 条笔记",
            Toast.LENGTH_SHORT
        ).show()
        selectedNoteIds.clear()
    }

    //永久删除
    val permanentlyDeleteSelectedNotes = {
        selectedNoteIds.forEach { noteId ->
            coroutineScope.launch(Dispatchers.IO) {
                val note = database.noteDao().getNoteById(noteId)
                note?.let {
                    database.noteDao().deleteNote(it)
                }
            }
        }
        Toast.makeText(context,
            "已永久删除 ${selectedNoteIds.size} 条笔记",
            Toast.LENGTH_SHORT
        ).show()
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
            Crossfade(
                targetState = isSelectionMode,
                animationSpec = tween(300)
            ) { isSelection ->
                if (isSelection) {
                    TrashSelectionTopBar(
                        selectedCount = selectedNoteIds.size,
                        onRestoreClick = { showRestoreDialog = true },
                        onPermanentDeleteClick = { showPermanentDeleteDialog = true },
                        onCancelSelection = { selectedNoteIds.clear() }
                    )
                } else {
                    TrashTopBar(
                        onBackClick = onBackClick,
                        trashCount = deletedNotes.size
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TrashNotesListScreen(
                    deletedNotes = deletedNotes,
                    selectedNoteIds = selectedNoteIds,
                    isSelectionMode = isSelectionMode,
                    onNoteClick = { noteId ->
                        if (isSelectionMode) {
                            if (selectedNoteIds.contains(noteId)) {
                                selectedNoteIds.remove(noteId)
                            } else {
                                selectedNoteIds.add(noteId)
                            }
                        } else {
                            val intent = NoteDetailActivity.newIntent(context, noteId)
                            context.startActivity(intent)
                        }
                    },
                    onNoteLongPress = { noteId ->
                        if (!selectedNoteIds.contains(noteId)) {
                            selectedNoteIds.add(noteId)
                        }
                    }
                )
            }
        }
    }

    //恢复确认对话
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("恢复笔记") },
            text = {
                Text("确定要恢复选中的 ${selectedNoteIds.size} 条笔记吗？恢复后笔记将出现在主界面。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        restoreSelectedNotes()
                    }
                ) {
                    Text("恢复")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    //永久删除对话
    if (showPermanentDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showPermanentDeleteDialog = false },
            title = { Text("永久删除") },
            text = {
                Text("确定要永久删除选中的 ${selectedNoteIds.size} 条笔记吗？此操作不可恢复，笔记将被彻底删除。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermanentDeleteDialog = false
                        permanentlyDeleteSelectedNotes()
                    }
                ) {
                    Text("永久删除", color = Color.Red)
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
fun TrashTopBar(
    onBackClick: () -> Unit,
    trashCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .statusBarsPadding()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回"
            )
        }

        //统计
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = "回收站",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$trashCount 条笔记",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TrashSelectionTopBar(
    selectedCount: Int,
    onRestoreClick: () -> Unit,
    onPermanentDeleteClick: () -> Unit,
    onCancelSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCancelSelection,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "取消选择",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Text(
            text = "已选中 $selectedCount 项",
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        //永久删除按钮
        IconButton(
            onClick = onPermanentDeleteClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "永久删除",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        //恢复按钮
        IconButton(
            onClick = onRestoreClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RestoreFromTrash,
                contentDescription = "恢复",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun TrashNotesListScreen(
    deletedNotes: List<Note>,
    selectedNoteIds: List<String>,
    isSelectionMode: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongPress: (String) -> Unit
) {
    if (deletedNotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_delete_forever_24), // 使用你的删除图标
                    contentDescription = "空回收站",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    text = "回收站为空",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "删除的笔记将在这里显示",
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
            items(deletedNotes) { note ->
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