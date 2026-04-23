package com.v.v_notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.v.v_notes.data.model.Note
import com.v.v_notes.data.model.TodoItem
import com.v.v_notes.ui.theme.MyNotesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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