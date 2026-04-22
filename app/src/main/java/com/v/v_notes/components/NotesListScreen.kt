package com.v.v_notes.components

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.v.v_notes.addlist.RichTextEditorActivity
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem
import com.v.v_notes.ui.theme.MyNotesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 笔记详情屏幕组件
 * 从数据库读取笔记并使用RichTextEditor显示HTML内容
 * 注意：数据库的getNoteById方法参数是String类型
 */
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

    // 从数据库读取笔记
    val noteFlow = database.noteDao().getAllNotes()
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
                                // 编辑菜单项
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

                                // 删除菜单项（暂不实现功能）
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
                                            Text("删除")
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        // TODO: 实现删除功能
                                    }
                                )

                                // 归档菜单项（暂不实现功能）
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
                                            Text("归档")
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        // TODO: 实现归档功能
                                    }
                                )

                                // 分享菜单项（暂不实现功能）
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
}

/**
 * 笔记详情内容组件
 */
@Composable
fun NoteDetailContent(
    note: Note,
    richTextState: com.mohamedrejeb.richeditor.model.RichTextState,
    onImageClick: (String, List<String>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 笔记标题
            Text(
                text = note.title.ifEmpty { "无标题" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            // 内容区域标题
            Text(
                text = "内容",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            // 富文本编辑器（只读模式显示HTML内容）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    readOnly = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // 如果有图片URI，显示图片预览
        if (note.imageUris.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "图片",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "图片附件 (${note.imageUris.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // 图片预览网格
                            ImagePreviewGrid(
                                imageUris = note.imageUris,
                                onImageClick = { index ->
                                    onImageClick(note.imageUris[index], note.imageUris)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )

                            // 显示图片URI列表
                            note.imageUris.forEachIndexed { index: Int, uri: String ->
                                Text(
                                    text = "${index + 1}. ${getFileNameFromUri(uri)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 如果有待办事项，显示待办列表
        if (note.todoItems.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "待办事项",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        TodoListPreview(
                            todoItems = note.todoItems,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // 底部元信息
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                NoteMetaInfo(note = note)
            }
        }

        // 添加一些底部空白
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 图片预览网格组件
 */
@Composable
fun ImagePreviewGrid(
    imageUris: List<String>,
    onImageClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(imageUris) { uri ->
            ImagePreviewItem(
                imageUri = uri,
                onClick = { onImageClick(imageUris.indexOf(uri)) }
            )
        }
    }
}

/**
 * 单个图片预览组件
 */
@Composable
fun ImagePreviewItem(
    imageUri: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
        modifier = modifier.size(100.dp)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "笔记图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    // 加载中显示进度条
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                is AsyncImagePainter.State.Error -> {
                    // 加载失败显示占位符
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "加载失败",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                else -> {
                    // 加载成功显示图片
                    SubcomposeAsyncImageContent()
                }
            }
        }
    }
}

/**
 * 待办列表预览组件
 */
@Composable
fun TodoListPreview(
    todoItems: List<TodoItem>,
    modifier: Modifier = Modifier
) {
    val completedCount = todoItems.count { it.isCompleted }
    val totalCount = todoItems.size

    Column(modifier = modifier) {
        // 进度信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "待办进度",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "$completedCount/$totalCount 完成",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 进度条
        if (totalCount > 0) {
            val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 待办事项列表
        todoItems.forEachIndexed { index, todoItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (todoItem.isCompleted) "已完成" else "未完成",
                    modifier = Modifier.size(20.dp),
                    tint = if (todoItem.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = todoItem.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (todoItem.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (todoItem.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
            }

            if (index < todoItems.size - 1) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            }
        }
    }
}

/**
 * 笔记元信息组件
 */
@Composable
fun NoteMetaInfo(
    note: Note
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 创建时间
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(note.createdAt))

        Text(
            text = "创建于: $dateStr",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        // 内容长度指示器
        val contentLength = note.content.length
        if (contentLength > 0) {
            Text(
                text = "${contentLength} 字符",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * 从URI中提取文件名
 */
private fun getFileNameFromUri(uri: String): String {
    return try {
        val lastSlashIndex = uri.lastIndexOf('/')
        if (lastSlashIndex != -1 && lastSlashIndex < uri.length - 1) {
            uri.substring(lastSlashIndex + 1)
        } else {
            uri
        }
    } catch (e: Exception) {
        uri
    }
}

/**
 * 预览函数 - 笔记详情屏幕预览
 */
@Preview(showBackground = true)
@Composable
fun NoteDetailScreenPreview() {
    MyNotesTheme {
        val sampleNote = Note(
            id = "1",
            title = "测试笔记标题",
            content = "<h1>这是一个标题</h1><p>这是<b>加粗</b>的文本内容，包含了<i>斜体</i>和<u>下划线</u>。</p><p>这是第二段文本。</p>",
            imageUris = listOf("invalid_uri_for_preview"), // 避免预览时网络请求
            todoItems = listOf(
                TodoItem(text = "第一个待办事项", isCompleted = true),
                TodoItem(text = "第二个待办事项", isCompleted = false),
                TodoItem(text = "第三个待办事项", isCompleted = true)
            ),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val richTextState = rememberRichTextState()

        LaunchedEffect(Unit) {
            richTextState.setHtml(sampleNote.content)
        }

        NoteDetailContent(
            note = sampleNote,
            richTextState = richTextState,
            onImageClick = { uri, allUris ->
                println("点击了图片: $uri")
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 预览函数 - 图片预览网格
 */
@Preview(showBackground = true)
@Composable
fun ImagePreviewGridPreview() {
    MyNotesTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "图片预览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ImagePreviewGrid(
                    imageUris = listOf("invalid_uri_for_preview"), // 避免预览时网络请求
                    onImageClick = { index ->
                        println("点击了第${index + 1}张图片")
                    }
                )
            }
        }
    }
}

/**
 * 预览函数 - 待办列表预览
 */
@Preview(showBackground = true)
@Composable
fun TodoListPreviewPreview() {
    MyNotesTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            TodoListPreview(
                todoItems = listOf(
                    TodoItem(text = "完成笔记详情页面开发", isCompleted = true),
                    TodoItem(text = "修复RichTextEditor显示问题", isCompleted = true),
                    TodoItem(text = "添加图片预览功能", isCompleted = false),
                    TodoItem(text = "优化性能", isCompleted = false)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}