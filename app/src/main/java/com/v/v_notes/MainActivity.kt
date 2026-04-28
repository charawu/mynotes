package com.v.v_notes

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.v.v_notes.addlist.RichTextEditorActivity
import com.v.v_notes.archive.ArchiveActivity
import com.v.v_notes.control.NoteShareHelper
import com.v.v_notes.control.moveSelectedNotesToTrash
import com.v.v_notes.data.database.NoteDatabase
import com.v.v_notes.data.model.Note
import com.v.v_notes.factory.NoteViewModelFactory
import com.v.v_notes.setting.SettingActivity
import com.v.v_notes.trash.TrashActivity
import com.v.v_notes.ui.theme.MyNotesTheme
import com.v.v_notes.viewmodel.NoteViewModel
import com.v.v_notes.control.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import com.v.v_notes.login.auth.AuthManager
import com.v.v_notes.components.AddButton
import com.v.v_notes.components.AddButtonList
import com.v.v_notes.components.BottomNavMenu
import com.v.v_notes.components.Menu
import com.v.v_notes.components.NoteListItem
import com.v.v_notes.control.ThemeStateManager
import com.v.v_notes.login.LoginScreen
import com.v.v_notes.login.UserManageDialog
import com.v.v_notes.sync.manager.SyncManager
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        applyStoredThemeAtLaunch()

        super.onCreate(savedInstanceState)

        ThemeStateManager.initialize(this)

        ThemeStateManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    MyNotesApp()
                }
            }
        }
    }

    private fun applyStoredThemeAtLaunch() {
        val savedMode = SettingsManager.getString("theme_mode", "follow_system")
        when (savedMode) {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

}

@PreviewScreenSizes
@Composable
fun MyNotesApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    //数据库实例
    val noteDatabase = NoteDatabase.getInstance(context)
    val noteDao = noteDatabase.noteDao()

    //用户登录
    var showLoginDialog by remember { mutableStateOf(false) }
    var showUserManageDialog by remember { mutableStateOf(false) }

    val authManager = AuthManager(context)
    val syncManager = SyncManager(context,noteDao)

    val loginState = authManager.loginState.collectAsState().value

    val onAccountClick = {
        if (loginState.isLoggedIn) {
            //已登录
            showUserManageDialog = true
        } else {
            //未登录
            showLoginDialog = true
        }
    }

    //焦点管理
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    //状态管理
    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(1) }
    var isActive by remember { mutableStateOf(false) }

    //选中笔记ID列表
    val selectedNoteIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    //控制删除确认对话
    var showDeleteDialog by remember { mutableStateOf(false) }

    //控制分享选项对话
    var showShareOptionsDialog by remember { mutableStateOf(false) }
    var notesToShare by remember { mutableStateOf<List<Note>>(emptyList()) }
    var shareDialogTitle by remember { mutableStateOf("分享笔记") }

    //排序状态
    var sortOrder by remember {
        mutableStateOf(SettingsManager.getBoolean("note_sort_descending", true))
    }

    //搜索状态
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Note>>(emptyList()) }
    val searchFocusRequester = remember { FocusRequester() }

    //获取数据库实例
    val database = remember { NoteDatabase.getInstance(context) }

    val noteFlow = remember(sortOrder) {
        if (sortOrder) {
            //降序
            database.noteDao().getNotesByTimeDescending()
        } else {
            //升序
            database.noteDao().getNotesByTimeAscending()
        }
    }

    val allNotes by noteFlow.collectAsState(initial = emptyList())

    //搜索笔记
    LaunchedEffect(searchQuery, allNotes) {
        if (searchQuery.isNotBlank()) {
            val results = withContext(Dispatchers.IO) {
                try {
                    database.noteDao().searchNotes(searchQuery)
                } catch (e: Exception) {
                    Log.e("MainActivity", "搜索失败", e)
                    emptyList()
                }
            }
            searchResults = results
        } else {
            searchResults = emptyList()
        }
    }

    //确定要显示的笔记列表
    val notesToDisplay = if (searchQuery.isNotBlank()) {
        searchResults
    } else {
        allNotes
    }

    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            application = LocalContext.current.applicationContext as Application
        )
    )

    //获取是否使用底部菜单的设置
    val useBottomMenu by remember { mutableStateOf(SettingsManager.getBoolean("fixed_menu")) }

    LaunchedEffect(Unit) {
        Log.d("MainActivity", "应用启动，开始加载笔记")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Crossfade(
                targetState = isSelectionMode,
                animationSpec = tween(300)
            ) { isSelection ->
                if (isSelection) {

                    // 计算是否有选中的笔记是已置顶的
                    val hasPinnedNotes = selectedNoteIds.any { noteId ->
                        allNotes.find { it.id == noteId }?.isPinned == true
                    }

                    SelectionTopBar(
                        selectedCount = selectedNoteIds.size,
                        allNotes = allNotes,
                        onDeleteClick = { showDeleteDialog = true },
                        onArchiveClick = {
                            selectedNoteIds.forEach { noteId ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    database.noteDao().updateArchiveStatus(noteId, true)
                                }
                            }
                            Toast.makeText(
                                context,
                                "已归档 ${selectedNoteIds.size} 条笔记",
                                Toast.LENGTH_SHORT
                            ).show()

                            selectedNoteIds.clear()
                        },
                        onShareClick = {
                            //实现分享
                            val selectedNotes = allNotes.filter { it.id in selectedNoteIds }
                            if (selectedNotes.isEmpty()) {
                                Toast.makeText(context, "请先选择笔记", Toast.LENGTH_SHORT).show()
                                return@SelectionTopBar
                            }

                            notesToShare = selectedNotes
                            shareDialogTitle = "分享 ${selectedNotes.size} 条笔记"

                            val imageUris = selectedNotes.flatMap { note ->
                                NoteShareHelper.convertImageUris(context, note.imageUris)
                            }

                            //检查是否有图片
                            if (imageUris.isEmpty()) {
                                //没有图片,直接分享文本
                                if (selectedNotes.size == 1) {
                                    //单条笔记
                                    NoteShareHelper.shareNote(
                                        context = context,
                                        note = selectedNotes.first(),
                                        imageUris = emptyList(),
                                        shareType = NoteShareHelper.ShareType.TEXT,
                                        chooserTitle = "分享笔记: ${selectedNotes.first().title}"
                                    )
                                } else {
                                    //多条笔记，分享合并的文本
                                    shareMultipleNotes(context, selectedNotes)
                                }
                            } else {
                                //有图片,显示选项对话
                                showShareOptionsDialog = true
                            }
                        },
                        onCancelSelection = { selectedNoteIds.clear() },
                        onPinClick = {
                            //置顶/取消置顶
                            selectedNoteIds.forEach { noteId ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    // 如果当前有任何笔记是已置顶的，就全部取消置顶，否则全部置顶
                                    val newPinStatus = !hasPinnedNotes
                                    database.noteDao().updatePinStatus(noteId, newPinStatus)
                                }
                            }
                            val actionText = if (hasPinnedNotes) "已取消置顶" else "已置顶"
                            Toast.makeText(
                                context,
                                "$actionText ${selectedNoteIds.size} 条笔记",
                                Toast.LENGTH_SHORT
                            ).show()
                            selectedNoteIds.clear()
                        },
                        isAnyPinned = hasPinnedNotes
                    )
                } else {
                    NormalTopBar(
                        onMenuClick = { isMenuExpanded = true },
                        onAccountClick = onAccountClick,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchFocusChange = { isSearchActive = it },
                        searchFocusRequester = searchFocusRequester,
                        sortOrder = sortOrder,
                        onSortClick = {
                            //切换排序顺序
                            val newSortOrder = !sortOrder
                            sortOrder = newSortOrder
                            SettingsManager.putBoolean("note_sort_descending", newSortOrder)

                            Toast.makeText(
                                context,
                                if (newSortOrder) "已按时间降序排序" else "已按时间升序排序",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            Menu(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart),
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                onItemSelected = { itemId ->
                    selectedItem = itemId
                    when (itemId) {
                        1 -> {
                            selectedItem = 1
                        }

                        3 -> {
                            selectedItem = 1
                            val intent = Intent(context, ArchiveActivity::class.java)
                            context.startActivity(intent)
                        }

                        4 -> {
                            selectedItem = 1
                            val intent = Intent(context, TrashActivity::class.java)
                            context.startActivity(intent)
                        }

                        5 -> {
                            selectedItem = 1
                            val intent = Intent(context, SettingActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                },
                selectedItem = selectedItem,
                showOnlyAlertAndSetting = useBottomMenu
            )

            //笔记列表
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NotesListScreen(
                    allNotes = notesToDisplay,
                    selectedNoteIds = selectedNoteIds,
                    isSelectionMode = isSelectionMode,
                    onNoteClick = { noteId ->
                        if (isSelectionMode) {
                            //在选择时,切换选中状态
                            if (selectedNoteIds.contains(noteId)) {
                                selectedNoteIds.remove(noteId)
                            } else {
                                selectedNoteIds.add(noteId)
                            }
                        } else {
                            //打开详情
                            val intent = NoteDetailActivity.newIntent(context, noteId)
                            context.startActivity(intent)
                        }
                    },
                    onNoteLongPress = { noteId ->
                        //长按
                        if (!selectedNoteIds.contains(noteId)) {
                            selectedNoteIds.add(noteId)
                        }
                    }
                )

                //搜索焦点处理,
                if (isSearchActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                //点击空白区域时关闭键盘和搜索焦点
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                isSearchActive = false
                            }
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.01f))
                    )
                }
            }

            //底部菜单
            if (useBottomMenu) {
                BottomNavMenu(
                    selectedItem = selectedItem,
                    onItemSelected = { itemId ->
                        selectedItem = itemId
                        when (itemId) {
                            1 -> {
                                selectedItem = 1
                            }

                            3 -> {
                                selectedItem = 3
                                val intent = Intent(context, ArchiveActivity::class.java)
                                context.startActivity(intent)
                            }

                            4 -> {
                                selectedItem = 4
                                val intent = Intent(context, TrashActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    }
                )
            }

            if (showLoginDialog) {
                LoginScreen(
                    authManager = authManager,
                    syncManager = syncManager,
                    onLoginSuccess = {
                        showLoginDialog = false
                        //登录成功后的处理
                    },
                    onBackClick = { showLoginDialog = false }
                )
            }

            // 用户管理对话框（新增）
            if (showUserManageDialog) {
                UserManageDialog(
                    authManager = authManager,
                    syncManager = syncManager,
                    onDismiss = { showUserManageDialog = false },
                    onLogout = {
                        showUserManageDialog = false
                        Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                    }
                )
            }

        }

        //浮动按钮
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (useBottomMenu) 90.dp else 60.dp,
                    end = 60.dp
                ),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 65.dp)
            ) {
                AddButtonList(
                    expanded = isActive,
                    onPhotoClick = {
                        isActive = false
                        val intent = Intent(context, RichTextEditorActivity::class.java)
                        intent.putExtra("quickAction", "image")
                        context.startActivity(intent)
                    },

                    onDrawClick = {
                        isActive = false
                        val intent = Intent(context, RichTextEditorActivity::class.java)
                        intent.putExtra("quickAction", "draw")
                        context.startActivity(intent)
                    },

                    onCheckClick = {
                        isActive = false
                        val intent = Intent(context, RichTextEditorActivity::class.java)
                        intent.putExtra("quickAction", "todo")
                        context.startActivity(intent)
                    },
                    onTextClick = {
                        isActive = false
                        val intent = Intent(context, RichTextEditorActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

            AddButton(
                isActive = isActive,
                onToggle = { isActive = it }
            )
        }
    }

    //删除确认对话
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_title)) },
            text = {
                Text("${stringResource(R.string.delete_1)}${selectedNoteIds.size}${stringResource(R.string.delete_2)}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        //删除
                        moveSelectedNotesToTrash(
                            noteIds = selectedNoteIds.toList(),
                            coroutineScope = coroutineScope,
                            noteViewModel = noteViewModel,
                            allNotes = allNotes
                        )
                        //清空选择列表
                        selectedNoteIds.clear()
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    //分享选项对话
    if (showShareOptionsDialog && notesToShare.isNotEmpty()) {
        //图片uri
        val imageUris = notesToShare.flatMap { note ->
            NoteShareHelper.convertImageUris(context, note.imageUris)
        }

        AlertDialog(
            onDismissRequest = {
                showShareOptionsDialog = false
                notesToShare = emptyList()
            },
            title = { Text(shareDialogTitle) },
            text = {
                Column {
                    Text("请选择要分享的内容：")
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        //仅文本
                        item {
                            ShareOptionItem(
                                icon = Icons.Default.TextFields,
                                title = "仅分享文本",
                                description = "只分享笔记的文本内容",
                                onClick = {
                                    showShareOptionsDialog = false
                                    if (notesToShare.size == 1) {
                                        NoteShareHelper.shareNote(
                                            context = context,
                                            note = notesToShare.first(),
                                            imageUris = emptyList(),
                                            shareType = NoteShareHelper.ShareType.TEXT,
                                            chooserTitle = "分享笔记: ${notesToShare.first().title}"
                                        )
                                    } else {
                                        shareMultipleNotes(context, notesToShare)
                                    }
                                    notesToShare = emptyList()
                                }
                            )
                        }

                        // 仅图片
                        if (imageUris.isNotEmpty()) {
                            item {
                                ShareOptionItem(
                                    icon = Icons.Default.Image,
                                    title = "仅分享图片",
                                    description = "只分享笔记中的图片",
                                    onClick = {
                                        showShareOptionsDialog = false
                                        if (notesToShare.size == 1) {
                                            NoteShareHelper.shareNote(
                                                context = context,
                                                note = notesToShare.first(),
                                                imageUris = imageUris,
                                                shareType = NoteShareHelper.ShareType.IMAGE,
                                                chooserTitle = "分享图片: ${notesToShare.first().title}"
                                            )
                                        } else {
                                            shareMultipleNotesImages(
                                                context,
                                                notesToShare,
                                                imageUris
                                            )
                                        }
                                        notesToShare = emptyList()
                                    }
                                )
                            }
                        }

                        //全部
                        item {
                            ShareOptionItem(
                                icon = Icons.Default.Share,
                                title = "分享全部",
                                description = "分享文本和所有图片",
                                onClick = {
                                    showShareOptionsDialog = false
                                    if (notesToShare.size == 1) {
                                        NoteShareHelper.shareNote(
                                            context = context,
                                            note = notesToShare.first(),
                                            imageUris = imageUris,
                                            shareType = NoteShareHelper.ShareType.ALL,
                                            chooserTitle = "分享笔记: ${notesToShare.first().title}"
                                        )
                                    } else {
                                        shareMultipleNotesAll(context, notesToShare, imageUris)
                                    }
                                    notesToShare = emptyList()
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showShareOptionsDialog = false
                        notesToShare = emptyList()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun NormalTopBar(
    onMenuClick: () -> Unit,
    onAccountClick: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    searchFocusRequester: FocusRequester,
    sortOrder: Boolean,
    onSortClick: () -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                painter = painterResource(R.drawable.baseline_menu_24),
                contentDescription = "菜单"
            )
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp)
                        .focusRequester(searchFocusRequester)
                        .onFocusChanged { focusState ->
                            isSearching = focusState.isFocused
                            onSearchFocusChange(focusState.isFocused)
                        },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    singleLine = true,
                    cursorBrush = SolidColor(
                        if (isSearching) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onSearchFocusChange(false)
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "搜索 Nots",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = onSortClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_import_export_24),
                        contentDescription = "排序",
                        tint = if (sortOrder) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        IconButton(onClick = onAccountClick) {
            Icon(
                painter = painterResource(R.drawable.ic_account_box),
                contentDescription = "账户"
            )
        }
    }
}

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    allNotes: List<Note>,
    onDeleteClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onShareClick: () -> Unit,
    onCancelSelection: () -> Unit,
    onPinClick: () -> Unit,
    isAnyPinned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCancelSelection,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "取消选择",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Text(
            text = "已选中 $selectedCount 项",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        IconButton(
            onClick = onShareClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "分享",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        IconButton(
            onClick = onArchiveClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "归档",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        IconButton(
            onClick = onPinClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(if (isAnyPinned) R.drawable.baseline_push_pin_off_24 else R.drawable.baseline_push_pin_24),
                contentDescription = if (isAnyPinned) "取消置顶" else "置顶",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


//这个是notesList的预览
//组件包里面的命名重复了但是懒得改了
@Composable
fun NotesListScreen(
    allNotes: List<Note>,
    selectedNoteIds: List<String>,
    isSelectionMode: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongPress: (String) -> Unit
) {
    if (allNotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "暂无笔记",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "点击右下角按钮创建第一条笔记",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else {

        // 分离置顶笔记和普通笔记
        val pinnedNotes = allNotes.filter { it.isPinned }
        val unpinnedNotes = allNotes.filter { !it.isPinned }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // 显示置顶笔记部分
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_push_pin_24),
                            contentDescription = "置顶笔记",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "置顶笔记",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                items(pinnedNotes) { note ->
                    NoteListItem(
                        note = note,
                        isSelected = selectedNoteIds.contains(note.id),
                        isSelectionMode = isSelectionMode,
                        onClick = { onNoteClick(note.id) },
                        onLongPress = { onNoteLongPress(note.id) }
                    )
                }

                // 在置顶笔记和普通笔记之间添加分隔
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 显示普通笔记部分
            if (pinnedNotes.isNotEmpty() && unpinnedNotes.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "其他笔记",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            items(unpinnedNotes) { note ->
                NoteListItem(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    isSelectionMode = isSelectionMode,
                    onClick = { onNoteClick(note.id) },
                    onLongPress = { onNoteLongPress(note.id) }
                )
            }
        }

//            items(allNotes) { note ->
//                NoteListItem(
//                    note = note,
//                    isSelected = selectedNoteIds.contains(note.id),
//                    isSelectionMode = isSelectionMode,
//                    onClick = { onNoteClick(note.id) },
//                    onLongPress = { onNoteLongPress(note.id) }
//                )
//            }
    }
}


@Composable
fun ShareOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

//处理分享
private fun shareMultipleNotes(context: android.content.Context, notes: List<Note>) {
    if (notes.isEmpty()) {
        Toast.makeText(context, "没有可分享的笔记", Toast.LENGTH_SHORT).show()
        return
    }

    val builder = StringBuilder()
    if (notes.size == 1) {
        builder.append(NoteShareHelper.buildShareText(notes.first()))
    } else {
        builder.append("分享 ${notes.size} 条笔记\n\n")
        notes.forEachIndexed { index, note ->
            builder.append("${index + 1}. ${note.title}\n")
            val plainText = NoteShareHelper.buildShareText(note)
            builder.append(plainText)
            builder.append("\n\n")
        }
    }

    val shareText = builder.toString().trim()
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "分享笔记")
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享笔记"))
}


private fun shareMultipleNotesImages(
    context: android.content.Context,
    notes: List<Note>,
    imageUris: List<android.net.Uri>
) {
    if (imageUris.isEmpty()) {
        Toast.makeText(context, "没有可分享的图片", Toast.LENGTH_SHORT).show()
        return
    }

    val shareIntent = if (imageUris.size == 1) {
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUris[0])
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    context.startActivity(Intent.createChooser(shareIntent, "分享图片"))
}

private fun shareMultipleNotesAll(
    context: android.content.Context,
    notes: List<Note>,
    imageUris: List<android.net.Uri>
) {
    if (notes.isEmpty()) {
        Toast.makeText(context, "没有可分享的笔记", Toast.LENGTH_SHORT).show()
        return
    }

    val builder = StringBuilder()
    if (notes.size == 1) {
        builder.append(NoteShareHelper.buildShareText(notes.first()))
    } else {
        builder.append("分享 ${notes.size} 条笔记\n\n")
        notes.forEachIndexed { index, note ->
            builder.append("${index + 1}. ${note.title}\n")
            val plainText = NoteShareHelper.buildShareText(note)
            builder.append(plainText)
            builder.append("\n\n")
        }
    }

    val shareText = builder.toString().trim()

    val shareIntent = if (imageUris.isEmpty()) {
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "分享笔记")
        }
    } else if (imageUris.size == 1) {
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUris[0])
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "分享笔记")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "分享笔记")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    context.startActivity(Intent.createChooser(shareIntent, "分享笔记"))
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyNotesTheme {
        MyNotesApp()
    }
}