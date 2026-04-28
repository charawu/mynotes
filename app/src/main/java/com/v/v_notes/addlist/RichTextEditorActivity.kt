package com.v.v_notes.addlist

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import androidx.compose.material.icons.filled.Brush
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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.delay
import com.v.v_notes.components.EnhancedImagePreviewGrid
import com.v.v_notes.components.EnhancedTodoList
import com.v.v_notes.control.PhotoViewerEditor
import com.v.v_notes.control.ViewMode
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    quickAction = quickAction,
                    onBackClick = { finish() },
                    onSaveClick = { note ->
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        //销毁临时文件
        val imageFileManager = ImageFileManager(this)
        imageFileManager.clearTempStorage()
        super.onDestroy()
    }
}

class HistoryManager(maxHistorySize: Int = 50) {
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private val maxSize = maxHistorySize

    fun pushState(state: String) {
        if (undoStack.isEmpty() || undoStack.last() != state) {
            undoStack.add(state)

            //限制历史大小
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
    var showRichTextEditor by remember { mutableStateOf(true) }

    var showImageEditDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageIndex by remember { mutableIntStateOf(-1) }
    var imagePreviewRefreshKey by remember { mutableIntStateOf(0) }

    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            application = (context.applicationContext as Application)
        )
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val tempUri = withContext(Dispatchers.IO) {
                    imageFileManager.saveImageToTempStorage(it)
                }
                tempUri?.let { safeUri ->
                    insertedImages.add(safeUri)
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "图片保存失败，请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

    val createBlankCanvasAndOpenEditor: () -> Unit = {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val width = 1080
                    val height = 1920
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)

                    val tempDir = File(context.filesDir, "temp_notes_images")
                    if (!tempDir.exists() && !tempDir.mkdirs()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "创建画布失败：无法准备临时目录", Toast.LENGTH_SHORT).show()
                        }
                        return@withContext
                    }

                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "TEMP_CANVAS_${timeStamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
                    val tempFile = File(tempDir, fileName)

                    FileOutputStream(tempFile).use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                    }

                    val tempUri = Uri.fromFile(tempFile)

                    withContext(Dispatchers.Main) {
                        insertedImages.add(tempUri)
                        selectedImageIndex = insertedImages.lastIndex
                        selectedImageUri = tempUri
                        showImageEditDialog = true
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "创建画布失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(editorState.toHtml()) {
        val currentHtml = editorState.toHtml()
        if (currentHtml.isNotBlank()) {
            historyManager.pushState(currentHtml)
        }
        canUndo = historyManager.canUndo()
        canRedo = historyManager.canRedo()
    }

    //编辑末世加载数据
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

            //判断待办
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
                    //待办,切换
                    showRichTextEditor = false
                    handledQuickAction = true
                }
                "draw" -> {
                    createBlankCanvasAndOpenEditor()
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

    val onSaveNote: () -> Unit = {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val savedImageUris = mutableListOf<String>()

                for (uri in insertedImages) {
                    if (imageFileManager.isTempUri(uri)) {
                        //来自临时目录，移动到正式目录
                        val permanentUri = imageFileManager.moveTempImageToPrivate(uri)
                        permanentUri?.let { savedImageUris.add(it) }
                    } else {
                        //正式目录
                        savedImageUris.add(uri.toString())
                    }
                }

                val noteContent = editorState.toHtml()

                if (noteId != null && existingNote != null) {
                    val oldImages = existingNote.imageUris.toMutableList()
                    val newImages = savedImageUris.toMutableList()

                    val imagesToDelete = oldImages.filter { oldImageUri ->
                        !newImages.any { newImageUri ->
                            val oldFile = File(Uri.parse(oldImageUri).path ?: "")
                            val newFile = File(Uri.parse(newImageUri).path ?: "")
                            oldFile.name == newFile.name
                        }
                    }
                    //删除不使用的图片
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

                withContext(Dispatchers.Main) {
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
            }
        }
    }

    val hasTodoItems = todoItems.isNotEmpty()

    val toolbarHeight = 48.dp

    Box(modifier = Modifier.fillMaxSize()) {
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
                        //撤销
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

                        //重做
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

                        //切换按钮紧在有待办才显示
                        if (hasTodoItems) {
                            IconButton(
                                onClick = {
                                    showRichTextEditor = !showRichTextEditor
                                }
                            ) {
                                Icon(
                                    painter = if (showRichTextEditor) {
                                        painterResource(R.drawable.outline_check_box_24)
                                    } else {
                                        painterResource(R.drawable.outline_text_fields_24)
                                    },
                                    contentDescription = if (showRichTextEditor) "显示待办列表" else "显示文本编辑器",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        //保存按钮
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

                if (showRichTextEditor) {
                    //文本编辑器
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        RichTextEditor(
                            state = editorState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 0.dp),
                            placeholder = { Text("开始输入笔记内容...") }
                        )

                        //插入图片预览
                        if (insertedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "已插入图片:",
                                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
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
                                    selectedImageIndex = index
                                    selectedImageUri = uri
                                    showImageEditDialog = true
                                },
                                key = imagePreviewRefreshKey //key变化时重载
                            )
                        }
                    }
                } else {
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
                                    selectedImageIndex = index
                                    selectedImageUri = uri
                                    showImageEditDialog = true
                                },
                                key = imagePreviewRefreshKey //key变化重载
                            )
                        } else {
                            Column {
                                FilledTonalButton(
                                    onClick = {
                                        imagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "添加图片"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "添加图片")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                FilledTonalButton(
                                    onClick = {
                                        createBlankCanvasAndOpenEditor()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Brush,
                                        contentDescription = "创建画布"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "创建画布")
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(toolbarHeight + 8.dp))
            }
        }

        //判断是否在编辑界面
        if (showRichTextEditor) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                FormattingToolbar(
                    modifier = Modifier
                        .imePadding()
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    editorState = editorState,
                    onImageClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddTodo = {
                        todoItems.add(EditorTodoItem(text = ""))
                        showRichTextEditor = false
                    },
                    onDrawClick = {
                        createBlankCanvasAndOpenEditor()
                    }
                )
            }
        }

        if (showImageEditDialog && selectedImageUri != null) {
            ImageEditDialog(
                imageUri = selectedImageUri!!,
                onDismiss = {
                    showImageEditDialog = false
                    selectedImageUri = null
                    selectedImageIndex = -1
                },
                onSaveSuccess = { savedFile: File ->
                    coroutineScope.launch {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "图片编辑已保存", Toast.LENGTH_SHORT).show()
                            // 刷新逻辑
                            imagePreviewRefreshKey++
                        }
                    }
                }
            )
        }
    }
}

//图片编辑框组件
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
            color = ComposeColor.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PhotoViewerEditor(
                    imageLocalPath = imageUri,
                    initialMode = ViewMode.EDIT,
                    onModeChange = {
                        //TODO编辑末世切换
                    },
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

                //关闭
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭编辑",
                        tint = ComposeColor.White
                    )
                }
            }
        }
    }
}

//文本重置
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

data class EditorNoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val imageUris: List<String> = emptyList(),
    val todoItems: List<EditorTodoItem> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

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