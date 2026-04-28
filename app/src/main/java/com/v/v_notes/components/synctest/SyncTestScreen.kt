package com.v.v_notes.components.synctest

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v.v_notes.login.auth.AuthManager
import com.v.v_notes.login.LoginScreen
import com.v.v_notes.login.UserInfoPanel
import com.v.v_notes.sync.manager.SyncManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import androidx.compose.ui.platform.LocalLocale
import java.util.Date
import java.util.Locale

@Composable
fun SyncTestScreen(
    syncManager: SyncManager,
    authManager: AuthManager
) {
    val syncStatus by syncManager.syncStatus.collectAsState()
    val syncProgress by syncManager.syncProgress.collectAsState()
    val lastSyncTime by syncManager.lastSyncTime.collectAsState()

    //控制登录对话框显示
    var showLoginDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var resultMessage by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf(listOf<String>()) }
    var dbNotes by remember { mutableStateOf<List<com.v.v_notes.data.model.Note>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("HH:mm:ss", LocalLocale.current.platformLocale)
    val loginState = authManager.loginState.collectAsState().value

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            //用户信息面板
            UserInfoPanel(
                authManager = authManager,
                onLoginClick = {
                    showLoginDialog = true
                },
                onSyncClick = {
                    coroutineScope.launch {
                        if (loginState.isLoggedIn) {
                            val result = syncManager.performSync(
                                userId = loginState.userId,
                                token = loginState.token
                            )

                            resultMessage = if (result.isSuccess) {
                                "${result.message}\n同步笔记数: ${result.syncedCount}"
                            } else {
                                "${result.message}"
                            }

                            //同步后刷新数据库显示
                            refreshDbNotes(syncManager) { notes -> dbNotes = notes }
                        } else {
                            //如果未登录，显示登录对话框
                            showLoginDialog = true
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            //登录状态
            if (!loginState.isLoggedIn) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = "未登录",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "云同步功能已禁用",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击上方'登录'按钮以启用云同步",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            //原有的同步测试内容
            Text(
                text = "同步功能测试",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            //同步状态卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "同步状态: ${syncStatus.name}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (syncStatus != com.v.v_notes.sync.data.SyncStatus.IDLE &&
                        syncStatus != com.v.v_notes.sync.data.SyncStatus.COMPLETED &&
                        syncStatus != com.v.v_notes.sync.data.SyncStatus.FAILED) {
                        LinearProgressIndicator(
                            progress = { syncProgress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "进度: $syncProgress%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "最后同步: ${if (lastSyncTime > 0) dateFormat.format(
                            Date(
                                lastSyncTime
                            )
                        ) else "从未同步"}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (loginState.isLoggedIn) {
                                val result = syncManager.performSync(
                                    userId = loginState.userId,
                                    token = loginState.token
                                )

                                resultMessage = if (result.isSuccess) {
                                    "${result.message}\n同步笔记数: ${result.syncedCount}"
                                } else {
                                    "${result.message}"
                                }
                            } else {
                                resultMessage = "请先登录以使用同步功能"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = syncStatus == com.v.v_notes.sync.data.SyncStatus.IDLE ||
                            syncStatus == com.v.v_notes.sync.data.SyncStatus.COMPLETED ||
                            syncStatus == com.v.v_notes.sync.data.SyncStatus.FAILED
                ) {
                    Text("测试同步")
                }

                OutlinedButton(
                    onClick = {
                        syncManager.resetSync()
                        resultMessage = "同步状态已重置"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 数据库操作卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "数据库操作",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isRefreshing = true
                                coroutineScope.launch {
                                    refreshDbNotes(syncManager) { notes ->
                                        dbNotes = notes
                                        isRefreshing = false
                                    }
                                    addLog("刷新数据库笔记列表", logs) { logs = it }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("刷新列表")
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val note = syncManager.insertTestNoteToDb("测试笔记 ${Date().time}")
                                    addLog("插入测试笔记: ${note.title}", logs) { logs = it }
                                    refreshDbNotes(syncManager) { notes -> dbNotes = notes }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("插入测试")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 数据库笔记列表
            if (dbNotes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "数据库笔记列表 (${dbNotes.size} 条)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(
                                items = dbNotes.take(10),
                                key = { note -> note.id }
                            ) { note ->
                                DatabaseNoteItem(note = note)
                            }
                        }

                        if (dbNotes.size > 10) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "... 还有 ${dbNotes.size - 10} 条笔记未显示",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 结果展示
            if (resultMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = resultMessage,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 日志区域
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "操作日志",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (logs.isEmpty()) {
                        Text(
                            text = "暂无日志",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Column {
                            logs.takeLast(10).forEach { log ->
                                Text(
                                    text = "• $log",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (logs.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                logs = emptyList()
                                addLog("清除日志", logs) { logs = it }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("清除日志")
                        }
                    }
                }
            }
        }

        // 登录对话框
        if (showLoginDialog) {
            LoginScreen(
                authManager = authManager,
                syncManager = syncManager,
                onLoginSuccess = {
                    // 登录成功后可以立即执行一次同步
                    val newLoginState = authManager.loginState.value
                    if (newLoginState.isLoggedIn) {
                        coroutineScope.launch {
                            val result = syncManager.performSync(
                                userId = newLoginState.userId,
                                token = newLoginState.token
                            )
                            resultMessage = if (result.isSuccess) {
                                "登录成功，已开始同步"
                            } else {
                                "登录成功，但同步失败: ${result.message}"
                            }
                        }
                    }
                },
                onBackClick = { showLoginDialog = false }
            )
        }
    }
}

@Composable
fun DatabaseNoteItem(note: com.v.v_notes.data.model.Note) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", LocalLocale.current.platformLocale)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = note.title.ifEmpty { "(无标题)" },
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ID: ${note.id.take(8)}...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "更新: ${dateFormat.format(Date(note.updatedAt))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (note.isPinned) {
                    Text("pinned", fontSize = 12.sp)
                }
                if (note.isArchived) {
                    Text("archive", fontSize = 12.sp)
                }
            }

            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content.take(50) + if (note.content.length > 50) "..." else "",
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note.todoItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${note.todoItems.count { it.isCompleted }}/${note.todoItems.size} 待办",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun addLog(message: String, currentLogs: List<String>, updateLogs: (List<String>) -> Unit) {
    val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    val newLog = "[$timestamp] $message"
    val updatedLogs = currentLogs + newLog
    if (updatedLogs.size > 20) {
        updateLogs(updatedLogs.takeLast(20))
    } else {
        updateLogs(updatedLogs)
    }
}

private suspend fun refreshDbNotes(
    syncManager: SyncManager,
    onNotesLoaded: (List<com.v.v_notes.data.model.Note>) -> Unit
) {
    val notes = syncManager.getAllNotesFromDb()
    onNotesLoaded(notes)
}