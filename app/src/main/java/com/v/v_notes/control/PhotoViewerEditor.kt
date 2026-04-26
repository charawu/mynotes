package com.v.v_notes.control

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.unit.sp
import ja.burhanrashid52.photoeditor.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerEditor(
    imageLocalPath: Any,
    initialMode: ViewMode = ViewMode.VIEW,
    onModeChange: ((ViewMode) -> Unit)? = null,
    onSaveSuccess: ((File) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 当前模式状态
    var currentMode by remember { mutableStateOf(initialMode) }

    // 预览模式手势状态
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 加载本地图片
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(imageLocalPath) {
        bitmapState.value = withContext(Dispatchers.IO) {
            loadBitmapFromLocal(context, imageLocalPath)
        }
    }

    // PhotoEditor 状态
    val photoEditorViewState = remember { mutableStateOf<PhotoEditorView?>(null) }
    val photoEditorState = remember { mutableStateOf<PhotoEditor?>(null) }

    // 画笔状态
    var brushColor by remember { mutableStateOf(ComposeColor.Red) }
    var brushSize by remember { mutableStateOf(5f) }
    var isDrawingEnabled by remember { mutableStateOf(true) }

    // 用于强制刷新 PhotoEditor 的键
    val refreshKey = remember { mutableIntStateOf(0) }

    // 初始化 PhotoEditor
    LaunchedEffect(bitmapState.value, refreshKey.intValue) {
        if (bitmapState.value != null && photoEditorState.value == null) {
            Log.d("PhotoViewerEditor", "初始化 PhotoEditor, refreshKey: ${refreshKey.intValue}")

            val view = PhotoEditorView(context)
            photoEditorViewState.value = view

            // 设置图片
            bitmapState.value?.let { bitmap ->
                view.source.setImageBitmap(bitmap)
            }

            // 构建 PhotoEditor
            val editor = PhotoEditor.Builder(context, view)
                .setPinchTextScalable(true)
                .setClipSourceImage(true)
                .build()

            // 启用画笔模式
            editor.setBrushDrawingMode(true)

            // 设置初始画笔参数
            val colorInt = composeColorToAndroidColor(brushColor)

            // 修复：正确设置画笔颜色和大小
            try {
                // 方法1：尝试通过属性设置
                editor.brushColor = colorInt
                editor.brushSize = brushSize
                Log.d("PhotoViewerEditor", "通过属性设置画笔: 颜色=$colorInt, 大小=$brushSize")
            } catch (e: Exception) {
                Log.e("PhotoViewerEditor", "通过属性设置失败，尝试其他方法", e)
                try {
                    // 方法2：尝试通过方法设置
                    editor.javaClass.getMethod("setBrushColor", Int::class.java).invoke(editor, colorInt)
                    editor.javaClass.getMethod("setBrushSize", Float::class.java).invoke(editor, brushSize)
                    Log.d("PhotoViewerEditor", "通过反射方法设置画笔")
                } catch (e2: Exception) {
                    Log.e("PhotoViewerEditor", "所有设置方法都失败", e2)
                }
            }

            photoEditorState.value = editor
            Log.d("PhotoViewerEditor", "PhotoEditor 初始化完成")
        }
    }

    // 监听画笔状态变化，实时更新
    LaunchedEffect(brushColor, brushSize) {
        val colorInt = composeColorToAndroidColor(brushColor)
        Log.d("PhotoViewerEditor", "画笔状态变化: 颜色=$colorInt, 大小=$brushSize")

        photoEditorState.value?.let { editor ->
            // 获取当前画笔模式状态
            val wasEnabled = isDrawingEnabled

            try {
                // 临时禁用画笔模式
                if (wasEnabled) {
                    editor.setBrushDrawingMode(false)
                }

                // 设置新画笔参数
                editor.brushColor = colorInt
                editor.brushSize = brushSize

                Log.d("PhotoViewerEditor", "画笔参数已更新")

                // 恢复画笔模式
                if (wasEnabled) {
                    editor.setBrushDrawingMode(true)
                }

                // 增加刷新键，确保设置生效
                refreshKey.intValue++
            } catch (e: Exception) {
                Log.e("PhotoViewerEditor", "更新画笔参数失败", e)
            }
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部工具栏
        TopAppBar(
            title = { Text("图片编辑") },
            actions = {
                IconButton(onClick = {
                    val newMode = if (currentMode == ViewMode.VIEW) ViewMode.EDIT else ViewMode.VIEW
                    currentMode = newMode
                    onModeChange?.invoke(newMode)
                }) {
                    Icon(
                        imageVector = if (currentMode == ViewMode.VIEW) Icons.Default.Edit
                        else Icons.Default.Visibility,
                        contentDescription = "切换模式"
                    )
                }
                if (currentMode == ViewMode.EDIT) {
                    // 保存按钮
                    IconButton(onClick = {
                        coroutineScope.launch {
                            try {
                                saveAndOverwriteImage(
                                    photoEditor = photoEditorState.value,
                                    originalPath = imageLocalPath,
                                    context = context,
                                    onSuccess = onSaveSuccess,
                                    onError = onError
                                )
                            } catch (e: Exception) {
                                onError?.invoke(e)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            }
        )

        // 主内容区
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            bitmapState.value?.let { bitmap ->
                when (currentMode) {
                    ViewMode.VIEW -> {
                        // 预览模式
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, rotationChange ->
                                        scale *= zoom
                                        offset += pan
                                        rotation += rotationChange
                                    }
                                }
                                .clipToBounds()
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "预览图片",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                        rotationZ = rotation
                                    },
                                contentScale = ContentScale.Fit
                            )
                        }

                        // 重置按钮
                        FloatingActionButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                                rotation = 0f
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "重置")
                        }
                    }

                    ViewMode.EDIT -> {
                        // 编辑模式 - 显示 PhotoEditorView
                        AndroidView(
                            factory = {
                                photoEditorViewState.value ?: PhotoEditorView(context)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 画笔设置工具栏
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            // 画笔颜色选择
                            Card(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "画笔颜色",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            ComposeColor.Red,
                                            ComposeColor.Blue,
                                            ComposeColor.Green,
                                            ComposeColor.Black,
                                            ComposeColor.White,
                                            ComposeColor.Yellow,
                                            ComposeColor.Magenta,
                                            ComposeColor.Cyan
                                        ).forEachIndexed { index, color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(color)
                                                    .clickable {
                                                        brushColor = color
                                                        val colorInt = composeColorToAndroidColor(color)
                                                        Log.d("PhotoViewerEditor", "点击颜色: ${index + 1}, 颜色值: $colorInt")

                                                        // 立即更新状态，LaunchedEffect 会监听到并更新 PhotoEditor
                                                    }
                                                    .then(
                                                        if (brushColor == color) {
                                                            Modifier.border(
                                                                width = 2.dp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                shape = RoundedCornerShape(4.dp)
                                                            )
                                                        } else {
                                                            Modifier
                                                        }
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // 画笔大小
                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "画笔大小: ${brushSize.toInt()}px",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Slider(
                                        value = brushSize,
                                        onValueChange = { newSize ->
                                            brushSize = newSize
                                            Log.d("PhotoViewerEditor", "滑块设置画笔大小: $newSize")

                                            // 立即更新状态，LaunchedEffect 会监听到并更新 PhotoEditor
                                        },
                                        onValueChangeFinished = {
                                            Log.d("PhotoViewerEditor", "滑块释放，最终画笔大小: $brushSize")
                                        },
                                        valueRange = 2f..30f
                                    )
                                }
                            }
                        }

                        // 底部控制工具栏
                        BottomControlToolbar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            onUndo = { photoEditorState.value?.undo() },
                            onRedo = { photoEditorState.value?.redo() },
                            onClear = { photoEditorState.value?.clearAllViews() }
                        )
                    }
                }
            } ?: run {
                // 加载中
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("加载图片中...")
                }
            }
        }
    }
}

/**
 * 底部控制工具栏组件
 */
@Composable
fun BottomControlToolbar(
    modifier: Modifier = Modifier,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 撤销按钮
            IconButton(
                onClick = onUndo,
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "撤销",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 重做按钮
            IconButton(
                onClick = onRedo,
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "重做",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 清空按钮
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "清空",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// 转换 Compose Color 为 Android Color
private fun composeColorToAndroidColor(composeColor: ComposeColor): Int {
    val red = (composeColor.red * 255).toInt()
    val green = (composeColor.green * 255).toInt()
    val blue = (composeColor.blue * 255).toInt()
    return android.graphics.Color.rgb(red, green, blue)
}

// 加载本地图片
private suspend fun loadBitmapFromLocal(context: Context, imageLocalPath: Any): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            when (imageLocalPath) {
                is String -> {
                    Log.d("PhotoViewerEditor", "加载图片路径: $imageLocalPath")
                    BitmapFactory.decodeFile(imageLocalPath)
                }
                is Uri -> {
                    Log.d("PhotoViewerEditor", "加载图片Uri: $imageLocalPath")
                    context.contentResolver.openInputStream(imageLocalPath)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                else -> {
                    Log.e("PhotoViewerEditor", "不支持的图片路径类型: $imageLocalPath")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PhotoViewerEditor", "加载图片失败", e)
            null
        }
    }
}

/**
 * 保存并覆盖原文件
 */
private suspend fun saveAndOverwriteImage(
    photoEditor: PhotoEditor?,
    originalPath: Any,
    context: Context,
    onSuccess: ((File) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    if (photoEditor == null) {
        onError?.invoke(IllegalStateException("PhotoEditor 未初始化"))
        return
    }

    withContext(Dispatchers.IO) {
        try {
            Log.d("PhotoViewerEditor", "开始保存并覆盖原图片")

            // 确定要覆盖的目标文件
            val targetFile = when (originalPath) {
                is String -> {
                    Log.d("PhotoViewerEditor", "覆盖文件路径: $originalPath")
                    File(originalPath)
                }
                is Uri -> {
                    Log.d("PhotoViewerEditor", "覆盖Uri文件: $originalPath")
                    val path = originalPath.path
                    if (path != null) {
                        File(path)
                    } else {
                        throw IllegalArgumentException("无法从Uri获取文件路径")
                    }
                }
                else -> throw IllegalArgumentException("不支持的路径类型: $originalPath")
            }

            // 确保目标文件存在
            if (!targetFile.exists()) {
                throw IllegalStateException("原文件不存在: ${targetFile.absolutePath}")
            }

            Log.d("PhotoViewerEditor", "目标文件: ${targetFile.absolutePath}，文件大小: ${targetFile.length()} bytes")

            // 使用 saveAsFile 方法保存到原文件，实现覆盖
            photoEditor.saveAsFile(
                targetFile.absolutePath,
                object : PhotoEditor.OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        Log.d("PhotoViewerEditor", "图片保存成功，已覆盖原文件: $imagePath")
                        Log.d("PhotoViewerEditor", "新文件大小: ${targetFile.length()} bytes")
                        onSuccess?.invoke(targetFile)
                    }

                    override fun onFailure(exception: Exception) {
                        Log.e("PhotoViewerEditor", "图片保存失败", exception)
                        onError?.invoke(exception)
                    }
                }
            )

        } catch (e: Exception) {
            Log.e("PhotoViewerEditor", "保存并覆盖图片异常", e)
            onError?.invoke(e)
        }
    }
}

// 模式枚举
enum class ViewMode {
    VIEW,  // 预览模式
    EDIT   // 编辑模式
}