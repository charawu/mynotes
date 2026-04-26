package com.v.v_notes.addlist

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.v.v_notes.R
import com.v.v_notes.components.FormattingToolbar
import com.v.v_notes.control.ImageFileManager
import com.v.v_notes.control.removeFormattingKeepStructure
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem as DbTodoItem
import com.v.v_notes.factory.NoteViewModelFactory
import com.v.v_notes.ui.theme.MyNotesTheme
import com.v.v_notes.viewmodel.NoteViewModel
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.delay
import com.v.v_notes.components.EnhancedImagePreviewGrid
import com.v.v_notes.components.EnhancedTodoList
import com.v.v_notes.control.PhotoViewerEditor
import com.v.v_notes.control.ViewMode
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RichTextEditorActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val noteId = intent.getStringExtra("noteId")
        val quickAction = intent.getStringExtra("quickAction")

        setContent {
            MyNotesTheme() {
                RichTextEditorScreen(
                    noteId = noteId,
                    quickAction = quickAction, // 传递快捷操作类型
                    onBackClick = { finish() },
                    onSaveClick = { note ->
                        finish()
                    }
                )
            }
        }
    }
}
class HistoryManager(maxHistorySize: Int = 50) {
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private val maxSize = maxHistorySize

    fun pushState(state: String) {
        if (undoStack.isEmpty() || undoStack.last() != state) {
            undoStack.add(state)

            //限制历史记录大小
            if (undoStack.size > maxSize) {
                undoStack.removeAt(0)
            }

            redoStack.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun undo(currentState: String): String? {
        if (undoStack.size > 1) {
            redoStack.add(currentState)

            undoStack.removeLast()

            //返回上一个状态
            return undoStack.lastOrNull()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun redo(): String? {
        if (redoStack.isNotEmpty()) {
            val nextState = redoStack.removeLast()
            undoStack.add(nextState)
            return nextState
        }
        return null
    }

    //是否可以撤销
    fun canUndo(): Boolean = undoStack.size > 1

    //是否可以重做
    fun canRedo(): Boolean = redoStack.isNotEmpty()

}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalRichTextApi::class, ExperimentalFoundationApi::class)
@Composable
fun RichTextEditorScreen(
    noteId: String? = null,
    quickAction: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: (EditorNoteItem) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    val editorState = rememberRichTextState()
    val insertedImages = remember { mutableStateListOf<Uri>() }
    val todoItems = remember { mutableStateListOf<EditorTodoItem>() }
    val historyManager = remember { HistoryManager() }
    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }
    var handledQuickAction by remember { mutableStateOf(false) }
    val imageFileManager = remember { ImageFileManager(context) }

    // 添加一个状态来控制是否显示富文本编辑器
    var showRichTextEditor by remember { mutableStateOf(true) }

    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            application = (context.applicationContext as Application)
        )
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            insertedImages.add(it)
        }
    }

    val database = NoteDatabase.getInstance(context)
    val noteFlow = database.noteDao().getAllNoteBy()
    val allNotes by noteFlow.collectAsState(initial = emptyList<Note>())

    val existingNote: Note? = if (noteId != null) {
        allNotes.find { it.id == noteId }
    } else {
        null
    }

    LaunchedEffect(editorState.toHtml()) {
        val currentHtml = editorState.toHtml()
        if (currentHtml.isNotBlank()) {
            historyManager.pushState(currentHtml)
        }
        canUndo = historyManager.canUndo()
        canRedo = historyManager.canRedo()
    }

    // 编辑模式,加载现有数据
    LaunchedEffect(existingNote) {
        if (existingNote != null) {
            title = existingNote.title
            editorState.setHtml(existingNote.content)

            insertedImages.clear()
            existingNote.imageUris.forEach { uriString ->
                try {
                    val accessibleUri = imageFileManager.getAccessibleImageUri(uriString)
                    insertedImages.add(accessibleUri)
                } catch (e: Exception) {
                    println("无法解析图片URI: $uriString, 错误: ${e.message}")
                }
            }

            todoItems.clear()
            existingNote.todoItems.forEach { todo ->
                todoItems.add(
                    EditorTodoItem(
                        id = todo.id,
                        text = todo.text,
                        isCompleted = todo.isCompleted,
                        createdAt = Date(todo.createdAt)
                    )
                )
            }

            // 如果有待办事项，默认显示待办列表
            if (existingNote.todoItems.isNotEmpty()) {
                showRichTextEditor = false
            }
        }
    }

    LaunchedEffect(quickAction, existingNote) {
        if (noteId == null && quickAction != null && !handledQuickAction) {
            delay(100)

            when (quickAction) {
                "image" -> {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    handledQuickAction = true
                }
                "todo" -> {
                    todoItems.add(EditorTodoItem(text = ""))
                    // 添加待办时切换到待办列表视图
                    showRichTextEditor = false
                    handledQuickAction = true
                }
                else -> {
                    handledQuickAction = true
                }
            }
        } else if (noteId != null) {
            handledQuickAction = true
        }
    }

    val onUndo = {
        val currentState = editorState.toHtml()
        val previousState = historyManager.undo(currentState)

        if (previousState != null) {
            editorState.setHtml(previousState)
        }

        canUndo = historyManager.canUndo()
        canRedo = historyManager.canRedo()
    }

    val onRedo = {
        val nextState = historyManager.redo()

        if (nextState != null) {
            editorState.setHtml(nextState)
        }

        canUndo = historyManager.canUndo()
        canRedo = historyManager.canRedo()
    }

    val onSaveNote = {
        val savedImageUris = imageFileManager.saveImagesToPrivateStorage(insertedImages)

        val noteContent = editorState.toHtml()

        if (noteId != null && existingNote != null) {
            val oldImages = existingNote.imageUris.toMutableList()
            val newImages = savedImageUris.toMutableList()

            val imagesToDelete = oldImages.filter { oldImageUri ->
                !newImages.any { newImageUri ->
                    newImageUri.contains(oldImageUri.substringAfterLast("/"))
                }
            }

            imageFileManager.deleteImagesFromPrivateStorage(imagesToDelete)
        }

        val note = Note(
            id = noteId ?: UUID.randomUUID().toString(),
            title = title,
            content = noteContent,
            imageUris = savedImageUris,
            todoItems = todoItems.map { todoItem ->
                DbTodoItem(
                    id = todoItem.id,
                    text = todoItem.text,
                    isCompleted = todoItem.isCompleted,
                    createdAt = todoItem.createdAt.time
                )
            },
            createdAt = if (noteId != null) {
                existingNote?.createdAt ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            },
            updatedAt = System.currentTimeMillis()
        )

        if (noteId != null) {
            noteViewModel.updateNote(note)
            Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
        } else {
            noteViewModel.insertNote(note)
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
        }

        onSaveClick(EditorNoteItem(
            id = note.id,
            title = note.title,
            content = note.content,
            imageUris = note.imageUris,
            todoItems = todoItems,
            createdAt = Date(note.createdAt),
            updatedAt = Date(note.updatedAt)
        ))
    }

    val hasTodoItems = todoItems.isNotEmpty()

    // 图片编辑对话框状态
    var showImageEditDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageIndex by remember { mutableIntStateOf(-1) }

    fun showEditDialog(index: Int, uri: Uri) {
        selectedImageIndex = index
        selectedImageUri = uri
        showImageEditDialog = true
    }

    val onImageEditSaveSuccess: (File) -> Unit = { savedFile: File ->
        coroutineScope.launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "图片编辑已保存", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (noteId != null) "编辑笔记" else "新建笔记",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                painter = painterResource(R.drawable.baseline_arrow_back_24),
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        // 撤销
                        IconButton(
                            onClick = onUndo,
                            enabled = canUndo
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_undo_24),
                                contentDescription = "撤销",
                                tint = if (canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        // 重做
                        IconButton(
                            onClick = onRedo,
                            enabled = canRedo
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_redo_24),
                                contentDescription = "重做",
                                tint = if (canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        // 切换按钮：只有在有待办事项时才显示
                        if (hasTodoItems) {
                            IconButton(
                                onClick = {
                                    showRichTextEditor = !showRichTextEditor
                                }
                            ) {
                                Icon(
                                    painter = if (showRichTextEditor) {
                                        painterResource(R.drawable.outline_check_box_24) // 切换到待办列表图标
                                    } else {
                                        painterResource(R.drawable.outline_text_fields_24) // 切换到文本编辑器图标
                                    },
                                    contentDescription = if (showRichTextEditor) "显示待办列表" else "显示文本编辑器",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // 保存按钮
                        IconButton(onClick = onSaveNote) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                painter = painterResource(R.drawable.outline_done_24),
                                contentDescription = if (noteId != null) "更新" else "保存"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            stringResource(R.string.edit_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp),
                    placeholder = { Text("输入笔记标题...") }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 使用showRichTextEditor变量控制显示哪个视图
                if (showRichTextEditor) {
                    // 显示富文本编辑器
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // 文本编辑器
                        RichTextEditor(
                            state = editorState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 0.dp),
                            placeholder = { Text("开始输入笔记内容...") }
                        )

                        // 插入的图片预览
                        if (insertedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "已插入图片:",
                                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            // 关键修改：添加底部间距，避免被工具栏遮挡
                            EnhancedImagePreviewGrid(
                                images = insertedImages,
                                onRemoveImage = { index ->
                                    insertedImages.removeAt(index)
                                },
                                onAddImage = {
                                    imagePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onEditImage = { index, uri ->
                                    showEditDialog(index, uri)
                                },
                                modifier = Modifier.padding(bottom = 64.dp) // 添加底部间距
                            )
                        }
                    }
                } else {
                    // 显示待办列表
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        EnhancedTodoList(
                            todoItems = todoItems,
                            onItemChanged = { index, todo ->
                                todoItems[index] = todo
                            },
                            onItemRemoved = { index ->
                                todoItems.removeAt(index)
                            },
                            onAddNewItem = {
                                todoItems.add(EditorTodoItem(text = ""))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (insertedImages.isNotEmpty()) {
                            Text(
                                text = "已插入图片:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // 关键修改：在待办列表模式下也添加底部间距
                            EnhancedImagePreviewGrid(
                                images = insertedImages,
                                onRemoveImage = { index ->
                                    insertedImages.removeAt(index)
                                },
                                onAddImage = {
                                    imagePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onEditImage = { index, uri ->
                                    showEditDialog(index, uri)
                                },
                                modifier = Modifier.padding(bottom = 64.dp) // 添加底部间距
                            )
                        } else {
                            FilledTonalButton(
                                onClick = {
                                    imagePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(bottom = 16.dp) // 为添加图片按钮添加底部间距
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加图片"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "添加图片")
                            }
                        }
                    }
                }
            }
        }

        // 格式化工具栏只在显示文本编辑器时显示
        if (showRichTextEditor) {
            FormattingToolbar(
                modifier = Modifier
                    .imePadding()
                    .align(Alignment.BottomCenter),
                editorState = editorState,
                onImageClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onAddTodo = {
                    todoItems.add(EditorTodoItem(text = ""))
                    // 添加待办事项后切换到待办列表视图
                    showRichTextEditor = false
                }
            )
        }

        // 图片编辑对话框
        if (showImageEditDialog && selectedImageUri != null) {
            ImageEditDialog(
                imageUri = selectedImageUri!!,
                onDismiss = {
                    showImageEditDialog = false
                    selectedImageUri = null
                    selectedImageIndex = -1
                },
                onSaveSuccess = onImageEditSaveSuccess
            )
        }
    }
}

// 图片编辑对话框组件
@Composable
fun ImageEditDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onSaveSuccess: (File) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PhotoViewerEditor(
                    imageLocalPath = imageUri,
                    initialMode = ViewMode.EDIT, // 直接进入编辑模式
                    onModeChange = { /* 处理模式切换 */ },
                    onSaveSuccess = { savedFile ->
                        onSaveSuccess(savedFile)
                        onDismiss()
                    },
                    onError = { error ->
                        coroutineScope.launch {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "图片编辑失败: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

                // 关闭按钮
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "关闭编辑",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 通过HTML处理重置为纯文本（备用方案）
 */
@OptIn(ExperimentalRichTextApi::class)
fun resetToPlainText(editorState: RichTextState) {
    try {
        val html = editorState.toHtml()

        val cleanedHtml = removeFormattingKeepStructure(html)

        editorState.setHtml(cleanedHtml)
    } catch (e: Exception) {
        e.message?.let { android.util.Log.d("HTML重置失败", it) }
        //备用,清空编辑器
        editorState.setHtml("")
    }
}

/**
 * 笔记项数据模型
 * 注意：添加Room注解
 * @Entity(tableName = "notes")
 */
data class EditorNoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val imageUris: List<String> = emptyList(),
    val todoItems: List<EditorTodoItem> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * 待办事项数据模型
 * 注意：如果存储为独立表，需要添加@Entity注解
 */
data class EditorTodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date()
) {
    fun copy(
        text: String = this.text,
        isCompleted: Boolean = this.isCompleted
    ): EditorTodoItem {
        return EditorTodoItem(
            id = this.id,
            text = text,
            isCompleted = isCompleted,
            createdAt = this.createdAt
        )
    }
}