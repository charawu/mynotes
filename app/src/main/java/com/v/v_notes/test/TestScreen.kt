package com.v.v_notes.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import android.util.Log
import kotlinx.coroutines.flow.first

/**
 * 数据库测试页面 - 用于验证数据的保存和读取功能
 */
@Composable
fun DatabaseTestScreen(
    database: com.v.v_notes.data.database.NoteDatabase,
    onBack: () -> Unit ={}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notes = remember { mutableStateListOf<com.v.v_notes.data.model.Note>() }
    val isLoading = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("点击按钮开始测试") }
    val testNoteCount = remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 返回按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("返回")
            }

            Text(
                text = "数据库测试",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF333333)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 测试操作区域
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. 添加测试数据按钮
            Button(
                onClick = {
                    isLoading.value = true
                    message.value = "正在添加测试数据..."

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            // 清空现有数据
                            database.noteDao().deleteAllNotes()

                            // 添加3条测试数据
                            for (i in 1..3) {
                                val note = com.v.v_notes.data.model.Note(
                                    title = "测试笔记 $i",
                                    content = "这是第 $i 条测试笔记的内容。\n" +
                                            "这是一条自动生成的测试数据，用于验证数据库功能。",
                                    imageUris = if (i == 1) listOf("image_$i.jpg") else emptyList(),
                                    todoItems = if (i == 2) listOf(
                                        com.v.v_notes.data.model.TodoItem(text = "任务1", isCompleted = true),
                                        com.v.v_notes.data.model.TodoItem(text = "任务2", isCompleted = false)
                                    ) else emptyList()
                                )

                                database.noteDao().insertNote(note)
                            }

                            withContext(Dispatchers.Main) {
                                message.value = "✅ 成功添加3条测试数据！"
                                isLoading.value = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                message.value = "❌ 添加测试数据失败: ${e.message}"
                                isLoading.value = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading.value,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("📝 添加测试数据 (3条)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. 读取所有数据按钮
            Button(
                onClick = {
                    isLoading.value = true
                    message.value = "正在读取数据..."

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            // 一次性读取所有数据（非Flow方式）
                            val noteList = database.noteDao().getAllNotes().first()

                            withContext(Dispatchers.Main) {
                                notes.clear()
                                notes.addAll(noteList)
                                testNoteCount.intValue = noteList.size

                                if (noteList.isEmpty()) {
                                    message.value = "📭 数据库中没有数据"
                                } else {
                                    message.value = "✅ 成功读取到 ${noteList.size} 条数据"
                                }
                                isLoading.value = false

                                // 在Logcat中打印详细信息
                                Log.d("DatabaseTest", "=== 数据库测试结果 ===")
                                Log.d("DatabaseTest", "读取到 ${noteList.size} 条笔记")
                                noteList.forEachIndexed { index, note ->
                                    Log.d("DatabaseTest",
                                        "笔记 #${index + 1}:\n" +
                                                "  ID: ${note.id}\n" +
                                                "  标题: ${note.title}\n" +
                                                "  内容长度: ${note.content.length} 字符\n" +
                                                "  图片数量: ${note.imageUris.size}\n" +
                                                "  Todo项目: ${note.todoItems.size} 个\n" +
                                                "  创建时间: ${note.createdAt}\n" +
                                                "  更新时间: ${note.updatedAt}"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                message.value = "❌ 读取数据失败: ${e.message}"
                                isLoading.value = false
                                Log.e("DatabaseTest", "读取失败: ${e.message}", e)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading.value,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("🔍 读取所有数据")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. 清空数据库按钮
            Button(
                onClick = {
                    isLoading.value = true
                    message.value = "正在清空数据..."

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            database.noteDao().deleteAllNotes()

                            withContext(Dispatchers.Main) {
                                notes.clear()
                                testNoteCount.intValue = 0
                                message.value = "🧹 数据库已清空"
                                isLoading.value = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                message.value = "❌ 清空数据失败: ${e.message}"
                                isLoading.value = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading.value,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Text("🗑️ 清空数据库")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 状态和信息显示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📊 测试信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading.value) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.value,
                    color = if (message.value.contains("✅")) Color(0xFF4CAF50)
                    else if (message.value.contains("❌")) Color(0xFFF44336)
                    else Color(0xFF666666)
                )

                if (testNoteCount.intValue > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "当前数据条数: ${testNoteCount.intValue}",
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 数据列表显示区域
        if (notes.isNotEmpty()) {
            Text(
                text = "📄 读取到的数据:",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF333333),
                                    maxLines = 1
                                )

                                Text(
                                    text = "ID: ${note.id.take(8)}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF666666)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (note.content.isNotEmpty()) {
                                Text(
                                    text = note.content.take(50) + if (note.content.length > 50) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666),
                                    maxLines = 2
                                )
                            }

                            if (note.todoItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📋 ${note.todoItems.size} 个待办事项",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }

                            if (note.imageUris.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🖼️ ${note.imageUris.size} 张图片",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📭",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无数据",
                        color = Color(0xFF999999)
                    )
                    Text(
                        text = "点击上方按钮添加测试数据",
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}