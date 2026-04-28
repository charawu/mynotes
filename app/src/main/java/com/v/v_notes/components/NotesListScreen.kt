package com.v.v_notes.components

import android.net.Uri
import android.text.Html
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.runtime.key
import coil.compose.AsyncImage
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Composable
fun ImagePreviewGrid(
    imageUris: List<String>,
    onImageClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    refreshKey: Int = 0,
    onImageEditSuccess: (() -> Unit)? = null
) {
    var showPreviewDialog by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(-1) }

    key(refreshKey) {
        // 添加调试日志
        LaunchedEffect(imageUris, refreshKey) {
            Log.d("ImagePreviewGrid", "图片列表大小: ${imageUris.size}, 刷新key: $refreshKey")
            imageUris.forEachIndexed { index, uri ->
                Log.d("ImagePreviewGrid", "图片[$index]: $uri")
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(imageUris, key = { uri ->

                "${uri}_${refreshKey}_${imageUris.indexOf(uri)}"
            }) { uri ->
                val index = imageUris.indexOf(uri)
                //图片预览
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            Log.d("ImagePreviewGrid", "点击图片，索引: $index")
                            selectedImageIndex = index
                            showPreviewDialog = true
                            onImageClick(index)
                        }
                ) {
                    key("image_preview_${uri}_${refreshKey}_$index") {
                        // 添加时间戳参数强制重新加载
                        val uriWithTimestamp = if (uri.contains("?")) {
                            "$uri&t=$refreshKey"
                        } else {
                            "$uri?t=$refreshKey"
                        }

                        AsyncImage(
                            model = uriWithTimestamp,
                            contentDescription = "图片预览",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    if (showPreviewDialog && selectedImageIndex >= 0 && selectedImageIndex < imageUris.size) {
        val selectedUri = imageUris[selectedImageIndex]
        Log.d("ImagePreviewGrid", "显示预览对话框，URI: $selectedUri")
        ImagePreviewDialog(
            imageUri = selectedUri,
            onDismiss = {
                showPreviewDialog = false
                selectedImageIndex = -1
            },
            refreshKey = refreshKey,
            onSaveSuccess = { savedFile ->
                onImageEditSuccess?.invoke()
            }
        )
    }
}

//图片预览对话框
@Composable
fun ImagePreviewDialog(
    imageUri: String,
    onDismiss: () -> Unit,
    refreshKey: Int = 0,
    onSaveSuccess: (File) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var actualImagePath by remember { mutableStateOf<Any?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUri, refreshKey) {
        isLoading = true
        errorMessage = null

        try {
            Log.d("ImagePreviewDialog", "开始加载图片，原始URI: $imageUri, 刷新key: $refreshKey")

            val processedUri = cleanImageUri(imageUri)
            Log.d("ImagePreviewDialog", "处理后的URI: $processedUri")

            actualImagePath = when {
                processedUri.startsWith("content://") || processedUri.startsWith("file://") -> {
                    Log.d("ImagePreviewDialog", "检测为Uri，尝试解析")
                    try {
                        Uri.parse(processedUri)
                    } catch (e: Exception) {
                        Log.e("ImagePreviewDialog", "解析Uri失败", e)
                        null
                    }
                }
                processedUri.startsWith("/") -> {
                    Log.d("ImagePreviewDialog", "检测为本地文件路径")
                    processedUri
                }
                else -> {
                    Log.d("ImagePreviewDialog", "未知格式，尝试作为文件路径处理")
                    processedUri
                }
            }

            // 检查文件是否存在（如果是本地路径）
//            if (actualImagePath is String) {
//                val file = File(actualImagePath as String)
//                Log.d("ImagePreviewDialog", "文件路径: ${file.absolutePath}")
//                Log.d("ImagePreviewDialog", "文件存在: ${file.exists()}")
//                if (!file.exists()) {
//                    errorMessage = "文件不存在: ${file.absolutePath}"
//                }
//            } else if (actualImagePath is Uri) {
//                Log.d("ImagePreviewDialog", "Uri路径: $actualImagePath")
//            } else {
//                Log.d("ImagePreviewDialog", "actualImagePath 为 null")
//            }

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
                    key("photo_viewer_${refreshKey}") {
                        PhotoViewerEditor(
                            imageLocalPath = actualImagePath!!,
                            initialMode = ViewMode.VIEW,
                            onModeChange = { /* TODO: 处理模式切换 */ },
                            onSaveSuccess = { savedFile ->
                                coroutineScope.launch {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "图片已保存",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        onSaveSuccess(savedFile)

                                        onDismiss()
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
                    }

                    //关闭按钮
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp, 32.dp, 16.dp, 16.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                } else {
                    //没有图片
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
            .trim()

        cleanedText.length
    } catch (e: Exception) {
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
                    },
                    refreshKey = 0
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