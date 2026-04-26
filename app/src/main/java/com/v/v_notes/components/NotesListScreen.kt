package com.v.v_notes.components

import android.net.Uri
import android.text.Html
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.v.v_notes.control.PhotoViewerEditor
import com.v.v_notes.control.ViewMode
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem
import com.v.v_notes.ui.theme.MyNotesTheme
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

//import kotlinx.coroutines.rememberCoroutineScope

/**
 * 笔记详情屏幕组件
 * 从数据库读取笔记并使用RichTextEditor显示HTML内容
 * 注意：数据库的getNoteById方法参数是String类型
 */

@Composable
fun ImagePreviewGrid(
    imageUris: List<String>,
    onImageClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 状态变量控制预览对话框的显示
    var showPreviewDialog by remember { mutableStateOf(false) }
    // 当前选中的图片索引
    var selectedImageIndex by remember { mutableIntStateOf(-1) }

    // 添加调试日志
    LaunchedEffect(imageUris) {
        Log.d("ImagePreviewGrid", "图片列表大小: ${imageUris.size}")
        imageUris.forEachIndexed { index, uri ->
            Log.d("ImagePreviewGrid", "图片[$index]: $uri")
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(imageUris) { uri ->
            ImagePreviewItem(
                imageUri = uri,
                onClick = {
                    val index = imageUris.indexOf(uri)
                    Log.d("ImagePreviewGrid", "点击图片，索引: $index")
                    // 设置选中的图片索引
                    selectedImageIndex = index
                    // 显示预览对话框
                    showPreviewDialog = true
                    // 调用外部回调（可选）
                    onImageClick(index)
                }
            )
        }
    }

    // 图片预览对话框
    if (showPreviewDialog && selectedImageIndex >= 0 && selectedImageIndex < imageUris.size) {
        val selectedUri = imageUris[selectedImageIndex]
        Log.d("ImagePreviewGrid", "显示预览对话框，URI: $selectedUri")
        ImagePreviewDialog(
            imageUri = selectedUri,
            onDismiss = {
                showPreviewDialog = false
                selectedImageIndex = -1
            }
        )
    }
}

// 图片预览对话框组件
@Composable
fun ImagePreviewDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var actualImagePath by remember { mutableStateOf<Any?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUri) {
        isLoading = true
        errorMessage = null

        try {
            Log.d("ImagePreviewDialog", "开始加载图片，原始URI: $imageUri")

            // 处理可能的方括号问题
            val processedUri = cleanImageUri(imageUri)
            Log.d("ImagePreviewDialog", "处理后的URI: $processedUri")

            // 判断图片URI类型并处理
            actualImagePath = when {
                // 如果是 content:// 或 file:// URI
                processedUri.startsWith("content://") || processedUri.startsWith("file://") -> {
                    Log.d("ImagePreviewDialog", "检测为Uri，尝试解析")
                    try {
                        Uri.parse(processedUri)
                    } catch (e: Exception) {
                        Log.e("ImagePreviewDialog", "解析Uri失败", e)
                        null
                    }
                }
                // 如果是本地文件路径
                processedUri.startsWith("/") -> {
                    Log.d("ImagePreviewDialog", "检测为本地文件路径")
                    processedUri
                }
                // 其他情况，尝试作为本地文件路径
                else -> {
                    Log.d("ImagePreviewDialog", "未知格式，尝试作为文件路径处理")
                    processedUri
                }
            }

             //检查文件是否存在（如果是本地路径）
            if (actualImagePath is String) {
                val file = File(actualImagePath as String)
                Log.d("ImagePreviewDialog", "文件路径: ${file.absolutePath}")
                Log.d("ImagePreviewDialog", "文件存在: ${file.exists()}")
                if (!file.exists()) {
                    errorMessage = "文件不存在: ${file.absolutePath}"
                }
            } else if (actualImagePath is Uri) {
                Log.d("ImagePreviewDialog", "Uri路径: $actualImagePath")
            } else {
                Log.d("ImagePreviewDialog", "actualImagePath 为 null")
            }

        } catch (e: Exception) {
            errorMessage = "处理图片时出错: ${e.message}"
            Log.e("ImagePreviewDialog", "加载图片失败", e)
        } finally {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "正在加载图片...",
                                color = Color.White
                            )
                        }
                    }
                } else if (errorMessage != null) {
                    //错误信息
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "加载图片失败",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                errorMessage ?: "error",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onDismiss
                            ) {
                                Text("关闭")
                            }
                        }
                    }
                } else if (actualImagePath != null) {
                    PhotoViewerEditor(
                        imageLocalPath = actualImagePath!!,
                        initialMode = ViewMode.VIEW,
                        onModeChange = { //TODO模式
                            },
                        onSaveSuccess = { savedFile ->
                            coroutineScope.launch {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "图片已保存",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onError = { error ->
                            coroutineScope.launch {
                                withContext(Dispatchers.Main) {
                                    val errorMsg = "加载图片失败: ${error.message}"
                                    Toast.makeText(
                                        context,
                                        errorMsg,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )

                    //关闭按钮
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp,32.dp,16.dp,16.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                } else {
                    //没有图片路径
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "警告",
                                tint = Color.Yellow,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "无法加载图片",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "图片路径为空或格式不正确",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onDismiss
                            ) {
                                Text("关闭")
                            }
                        }
                    }
                }
            }
        }
    }
}

//处理路径
private fun cleanImageUri(uri: String): String {
    var cleaned = uri.trim()
    if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
        cleaned = cleaned.substring(1, cleaned.length - 1).trim()
    }
    return cleaned
}

@Composable
fun NoteMetaInfo(
    note: Note
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        //创建时间
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", LocalLocale.current.platformLocale)
        val dateStr = dateFormat.format(Date(note.createdAt))

        Text(
            text = "创建于: $dateStr",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        //去粗html标签
        val contentLength = remember(note.content) {
            getPlainTextLengthFromHtml(note.content)
        }

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

//去除html标签
private fun getPlainTextLengthFromHtml(html: String): Int {
    if (html.isBlank()) {
        return 0
    }

    return try {
        val plainText =
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

        //清理空白字符
        val cleanedText = plainText
            .replace(Regex("\\s+"), " ")
            .trim()                       // 去除首尾空白

        cleanedText.length
    } catch (e: Exception) {
        // 如果出错，回退到简单的方法
        html.replace(Regex("<[^>]*>"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .length
    }
}


@Preview(showBackground = true)
@Composable
fun NoteDetailScreenPreview() {
    MyNotesTheme {
        val sampleNote = Note(
            id = "1",
            title = "测试笔记标题",
            content = "<h1>标题</h1><p>1<b>加粗</b>文本，<i>斜体</i>2<u>下划线</u>。</p><p>2。</p>",
            imageUris = listOf(

            ),
            todoItems = listOf(
                TodoItem(text = "1", isCompleted = true),
                TodoItem(text = "2", isCompleted = false),
                TodoItem(text = "3", isCompleted = true)
            ),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val richTextState = rememberRichTextState()

        NoteDetailContent(
            note = sampleNote,
            richTextState = richTextState,
            onImageClick = { uri, allUris ->
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

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
                    imageUris = listOf(
                        "https://example.com/image1.jpg",
                        "https://example.com/image2.jpg"
                    ),
                    onImageClick = { index ->
                        println("点击了第${index + 1}张图片")
                    }
                )
            }
        }
    }
}

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
                    TodoItem(text = "1", isCompleted = true),
                    TodoItem(text = "2", isCompleted = false)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}