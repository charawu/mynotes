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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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

class RichTextEditorActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val noteId = intent.getStringExtra("noteId")

        setContent {
            MyNotesTheme() {
                RichTextEditorScreen(
                    noteId = noteId,
                    onBackClick = { finish() },
                    onSaveClick = { note ->
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * 历史记录管理类
 */
class HistoryManager(maxHistorySize: Int = 50) {
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private val maxSize = maxHistorySize

    /**
     * 添加状态到历史记录
     */
    fun pushState(state: String) {
        // 如果当前栈顶状态与要添加的状态相同，则不添加
        if (undoStack.isEmpty() || undoStack.last() != state) {
            undoStack.add(state)

            // 限制历史记录大小
            if (undoStack.size > maxSize) {
                undoStack.removeAt(0)
            }

            // 添加新状态时清空重做栈
            redoStack.clear()
        }
    }

    /**
     * 撤销操作
     * @return 上一个状态，如果没有可撤销的操作则返回null
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun undo(currentState: String): String? {
        if (undoStack.size > 1) {
            // 将当前状态保存到重做栈
            redoStack.add(currentState)

            // 从撤销栈中移除当前状态
            undoStack.removeLast()

            // 返回上一个状态
            return undoStack.lastOrNull()
        }
        return null
    }

    /**
     * 重做操作
     * @return 下一个状态，如果没有可重做的操作则返回null
     */
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

    //清空历史记录
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

/**
 * 主编辑器界面
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalRichTextApi::class)
@Composable
fun RichTextEditorScreen(
    noteId: String? = null, // 新增：可选的笔记ID，null表示新建笔记
    onBackClick: () -> Unit,
    onSaveClick: (EditorNoteItem) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    val editorState = rememberRichTextState()

    val insertedImages = remember { mutableStateListOf<Uri>() }

    val todoItems = remember { mutableStateListOf<EditorTodoItem>() }

    // 创建历史记录管理器
    val historyManager = remember { HistoryManager() }

    // 跟踪是否可以进行撤销/重做
    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }

    // 创建图片文件管理器
    val imageFileManager = remember { ImageFileManager(context) }

    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            application = (context.applicationContext as Application)
        )
    )

    // 直接从数据库获取笔记
    val database = NoteDatabase.getInstance(context)
    val noteFlow = database.noteDao().getAllNotes()
    val allNotes by noteFlow.collectAsState(initial = emptyList<Note>())

    // 如果是编辑模式，查找对应笔记
    val existingNote: Note? = if (noteId != null) {
        allNotes.find { it.id == noteId }
    } else {
        null
    }

    // 监听编辑器内容变化，保存到历史记录
    LaunchedEffect(editorState.toHtml()) {
        val currentHtml = editorState.toHtml()
        if (currentHtml.isNotBlank()) {
            historyManager.pushState(currentHtml)
        }

        // 更新按钮状态
        canUndo = historyManager.canUndo()
        canRedo = historyManager.canRedo()
    }

    // 如果是编辑模式，加载现有数据
    LaunchedEffect(existingNote) {
        if (existingNote != null) {
            // 设置标题
            title = existingNote.title

            // 设置编辑器内容
            editorState.setHtml(existingNote.content)

            // 加载图片URI - 从数据库中的私有目录URI转换为可访问的URI
            insertedImages.clear()
            existingNote.imageUris.forEach { uriString ->
                try {
                    // 通过ImageFileManager获取可访问的URI
                    val accessibleUri = imageFileManager.getAccessibleImageUri(uriString)
                    insertedImages.add(accessibleUri)
                } catch (e: Exception) {
                    // 如果URI格式不正确，记录错误但继续
                    println("无法解析图片URI: $uriString, 错误: ${e.message}")
                }
            }

            // 加载待办事项
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
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            // 只将图片URI添加到列表中，不在编辑器中插入任何HTML
            insertedImages.add(it)
        }
    }

    // 撤销函数
    val onUndo = {
        val currentState = editorState.toHtml()
        val previousState = historyManager.undo(currentState)

        if (previousState != null) {
            editorState.setHtml(previousState)
        }

        // 更新按钮状态
        canUndo = historyManager.canUndo()
        canRedo = historyManager.canRedo()
    }

    // 重做函数
    val onRedo = {
        val nextState = historyManager.redo()

        if (nextState != null) {
            editorState.setHtml(nextState)
        }

        // 更新按钮状态
        canUndo = historyManager.canUndo()
        canRedo = historyManager.canRedo()
    }

    /**
     * 保存笔记的核心逻辑
     * 1. 将图片保存到私有目录
     * 2. 创建笔记对象并保存到数据库
     * 注意：编辑器内容中不包含图片HTML，图片URL单独存储
     */
    val onSaveNote = {
        // 1. 保存图片到私有目录
        val savedImageUris = imageFileManager.saveImagesToPrivateStorage(insertedImages)

        // 2. 获取编辑器内容（纯文本/HTML，不包含图片）
        val noteContent = editorState.toHtml()

        // 3. 清理旧的图片文件（如果是编辑模式）
        if (noteId != null && existingNote != null) {
            // 找出需要删除的旧图片（新列表中不包含的旧图片）
            val oldImages = existingNote.imageUris.toMutableList()
            val newImages = savedImageUris.toMutableList()

            val imagesToDelete = oldImages.filter { oldImageUri ->
                !newImages.any { newImageUri ->
                    // 简单的字符串比较，实际可能需要更复杂的逻辑
                    newImageUri.contains(oldImageUri.substringAfterLast("/"))
                }
            }

            // 删除不再使用的图片
            imageFileManager.deleteImagesFromPrivateStorage(imagesToDelete)
        }

        // 4. 创建笔记对象
        val note = Note(
            id = noteId ?: UUID.randomUUID().toString(),
            title = title,
            content = noteContent,
            imageUris = savedImageUris, // 保存私有目录的URI，单独存储
            todoItems = todoItems.map { todoItem ->
                DbTodoItem(
                    id = todoItem.id,
                    text = todoItem.text,
                    isCompleted = todoItem.isCompleted,
                    createdAt = todoItem.createdAt.time
                )
            },
            createdAt = if (noteId != null) {
                // 编辑模式：保持原创建时间
                existingNote?.createdAt ?: System.currentTimeMillis()
            } else {
                // 新建模式：设置当前时间
                System.currentTimeMillis()
            },
            updatedAt = System.currentTimeMillis()
        )

        // 5. 保存到数据库
        if (noteId != null) {
            // 编辑模式：更新笔记
            noteViewModel.updateNote(note)
            Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
        } else {
            // 新建模式：插入笔记
            noteViewModel.insertNote(note)
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
        }

        // 6. 回调
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
                        // 撤销按钮
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

                        // 重做按钮
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

                        // 保存按钮 - 使用新的保存逻辑
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
                // 标题输入
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

                // 待办事项列表
                if (todoItems.isNotEmpty()) {
                    TodoList(
                        todoItems = todoItems,
                        onItemChanged = { index, todo ->
                            todoItems[index] = todo
                        },
                        onItemRemoved = { index ->
                            todoItems.removeAt(index)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // 富文本编辑器
                RichTextEditor(
                    state = editorState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp, 0.dp),
                    placeholder = { Text("开始输入笔记内容...") }
                )

                // 已插入的图片预览
                if (insertedImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "已插入图片:",
                        modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    ImagePreviewGrid(
                        images = insertedImages,
                        onRemoveImage = { index ->
                            insertedImages.removeAt(index)
                        }
                    )
                }
            }
        }

        // 格式工具栏
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
            onAddTodo = { todoItems.add(EditorTodoItem(text = "新待办事项")) }
        )
    }
}

/**
 * 通过HTML处理重置为纯文本（备用方案）
 */
@OptIn(ExperimentalRichTextApi::class)
fun resetToPlainText(editorState: RichTextState) {
    try {
        // 获取当前HTML内容
        val html = editorState.toHtml()

        // 移除格式但保留结构（换行、段落）
        val cleanedHtml = removeFormattingKeepStructure(html)

        // 重新设置清理后的HTML
        editorState.setHtml(cleanedHtml)
    } catch (e: Exception) {
        println("HTML重置失败: ${e.message}")
        // 最终备用方案：清空编辑器
        editorState.setHtml("")
    }
}

/**
 * 待办事项列表
 */
@Composable
fun TodoList(
    todoItems: List<EditorTodoItem>,
    onItemChanged: (Int, EditorTodoItem) -> Unit,
    onItemRemoved: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp)
    ) {
        Text(
            text = "待办事项:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        todoItems.forEachIndexed { index, todoItem ->
            TodoItemRow(
                todo = todoItem,
                onCheckedChange = { isChecked ->
                    onItemChanged(index, todoItem.copy(isCompleted = isChecked))
                },
                onTextChange = { newText ->
                    onItemChanged(index, todoItem.copy(text = newText))
                },
                onRemove = { onItemRemoved(index) }
            )
        }
    }
}

/**
 * 单个待办事项行
 */
@Composable
fun TodoItemRow(
    todo: EditorTodoItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = onCheckedChange
        )

        OutlinedTextField(
            value = todo.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("待办事项内容...") },
            singleLine = true
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "删除", tint = Color.Gray)
        }
    }
}

/**
 * 图片预览网格
 */
@Composable
fun ImagePreviewGrid(
    images: List<Uri>,
    onRemoveImage: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp,16.dp,16.dp,45.dp)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.forEachIndexed { index, uri ->
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                androidx.compose.material3.Card(
                    modifier = Modifier.size(100.dp),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "插入的图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            IconButton(
                onClick = { onRemoveImage(index) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "删除图片")
            }

        }
    }
}

/**
 * 从编辑器状态创建笔记对象
 */
private fun createNoteFromState(
    title: String,
    content: String, // RichTextEditorState 返回 String
    images: List<Uri>,
    todoItems: List<EditorTodoItem>
): EditorNoteItem {
    return EditorNoteItem(
        title = title,
        content = content,
        imageUris = images.map { it.toString() },
        todoItems = todoItems,
        updatedAt = Date()
    )
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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview
@Composable
fun RichTextEditorScreenPreview() {
    MyNotesTheme() {
        RichTextEditorScreen(
            noteId = null, // 预览时使用新建模式
            onBackClick = { },
            onSaveClick = { },
        )
    }
}
