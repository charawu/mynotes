package com.v.v_notes.setting

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.v.v_notes.R
import com.v.v_notes.components.SwitchListItem
import com.v.v_notes.components.synctest.SyncTestScreen
import com.v.v_notes.control.SettingsManager
import com.v.v_notes.control.ThemeStateManager
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.login.auth.AuthManager
import com.v.v_notes.sync.manager.SyncManager
import com.v.v_notes.ui.theme.MyNotesTheme

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val noteDatabase = NoteDatabase.getInstance(this)
        val noteDao = noteDatabase.noteDao()

        val authManager = AuthManager(this)
        val syncManager = SyncManager(this, noteDao)

        setContent {
            MyNotesTheme {
                val showSyncTestScreen = remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SettingActivityScreen(
                        onBackClick = { finish() },
                        onShowSyncTestScreen = { showSyncTestScreen.value = true }
                    )

                    if (showSyncTestScreen.value) {
                        SyncTestScreen(
                            syncManager = syncManager,
                            authManager = authManager
                        )
                    }
                }
            }
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    try {
        val formattedUrl = formatUrl(url)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val chooserIntent = Intent.createChooser(intent, "选择浏览器")
            context.startActivity(chooserIntent)
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "未找到可用的浏览器应用", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        //异常处理
        e.printStackTrace()
        Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
    }
}

private fun formatUrl(url: String): String {
    return when {
        url.startsWith("http://") || url.startsWith("https://") -> url
        url.startsWith("www.") -> "https://$url"
        else -> "https://$url"
    }
}

@Composable
fun SettingActivityScreen(
    onBackClick: () -> Unit,
    onShowSyncTestScreen: () -> Unit
) {
    val context = LocalContext.current
    var isReboot by remember { mutableStateOf(false) }

    var showSyncTestScreen by remember {
        mutableStateOf(SettingsManager.getBoolean("show_sync_test_screen", false))
    }

    val themeOptions = listOf(
        ThemeOption.FOLLOW_SYSTEM,
        ThemeOption.LIGHT,
        ThemeOption.DARK
    )

    val savedThemeMode = SettingsManager.getString("theme_mode", "follow_system")
    var selectedTheme by remember {
        mutableStateOf(
            when(savedThemeMode) {
                "light" -> ThemeOption.LIGHT
                "dark" -> ThemeOption.DARK
                else -> ThemeOption.FOLLOW_SYSTEM
            }
        )
    }

    var showThemeDialog by remember { mutableStateOf(false) }

    val githubLink = stringResource(R.string.V_creator)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = null
                    )
                }
                Text(
                    text = stringResource(R.string.setting),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "外观",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { showThemeDialog = true }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_dark_mode_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "深色模式",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = selectedTheme.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = selectedTheme.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "功能",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clip(MaterialTheme.shapes.medium)
                        .padding(vertical = 8.dp)
                ) {
                    var checked by remember { mutableStateOf(SettingsManager.getBoolean("fixed_menu", false)) }

                    SwitchListItem(
                        enable = false,  //TODO,此功能需要重构界面,暂时放弃
                        isReboot = isReboot,
                        leadingIcon = painterResource(R.drawable.outline_horizontal_split_24),
                        title = stringResource(R.string.setting_fixed_menu),
                        subtitle = if (isReboot) {
                            stringResource(R.string.is_reboot)
                        } else {
                            stringResource(R.string.setting_fixed_menu1)
                        },
                        checked = checked,
                        onCheckedChange = { newState ->
                            checked = newState
                            SettingsManager.putBoolean("fixed_menu", newState)
                            isReboot = true
                        }
                    )

                    // 添加同步测试屏幕开关
                    SwitchListItem(
                        enable = true,
                        isReboot = isReboot,
                        leadingIcon = painterResource(R.drawable.baseline_sync_24),
                        title = "显示同步测试屏幕",
                        subtitle = if (showSyncTestScreen) {
                            "同步测试功能已启用，将在下次打开设置时显示"
                        } else {
                            "启用后在设置中显示同步测试功能"
                        },
                        checked = showSyncTestScreen,
                        onCheckedChange = { newState ->
                            showSyncTestScreen = newState
                            SettingsManager.putBoolean("show_sync_test_screen", newState)
                        }
                    )

                    if (showSyncTestScreen) {
                        Button(
                            onClick = onShowSyncTestScreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("立即打开数据库同步测试")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Quick Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            openUrl(context, githubLink)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.github_invertocat_black),
                            contentDescription = "GitHub 仓库",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "版本 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.V_detailed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = selectedTheme,
                themeOptions = themeOptions,
                onThemeSelected = { newTheme ->
                    selectedTheme = newTheme
                    val modeKey = newTheme.key
                    // 保存设置
                    SettingsManager.putString("theme_mode", newTheme.key)
                    ThemeStateManager.updateThemeMode(context, modeKey)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    themeOptions: List<ThemeOption>,
    onThemeSelected: (ThemeOption) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择主题模式",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                themeOptions.forEach { themeOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(themeOption) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == themeOption,
                            onClick = { onThemeSelected(themeOption) }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = themeOption.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = themeOption.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = themeOption.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (themeOption != themeOptions.last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                Text("关闭")
            }
        }
    )
}

sealed class ThemeOption(
    val key: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object FOLLOW_SYSTEM : ThemeOption(
        key = "follow_system",
        title = "跟随系统",
        description = "自动根据系统设置切换深浅色模式",
        icon = Icons.Default.BrightnessAuto
    )

    object LIGHT : ThemeOption(
        key = "light",
        title = "浅色模式",
        description = "始终使用浅色主题",
        icon = Icons.Default.Brightness5
    )

    object DARK : ThemeOption(
        key = "dark",
        title = "深色模式",
        description = "始终使用深色主题",
        icon = Icons.Default.Brightness4
    )
}

@Preview(showBackground = true)
@Composable
fun SettingActivityScreenPreview() {
    MyNotesTheme {
        SettingActivityScreen(
            onBackClick = {},
            onShowSyncTestScreen = {}
        )
    }
}