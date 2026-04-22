package com.v.v_notes.control
//
//import android.Manifest
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.provider.Settings
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.platform.LocalContext
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.v.v_notes.ui.image.PhotoEditorActivity
//
///**
// * 图片编辑工具类
// * 提供启动编辑器和处理权限的便捷方法
// */
//object PhotoEditorUtils {
//
//    /**
//     * 启动图片编辑器
//     * @param context Context
//     * @param imageUri 要编辑的图片Uri
//     * @param saveToOriginal 是否保存到原始文件
//     * @param onEditComplete 编辑完成回调，返回编辑后的图片Uri
//     */
//    fun launchEditor(
//        context: Context,
//        imageUri: Uri,
//        saveToOriginal: Boolean = false,
//        onEditComplete: (Uri?) -> Unit
//    ) {
//        val intent = PhotoEditorActivity.createIntent(context, imageUri, saveToOriginal)
//        if (context is Activity) {
//            context.startActivityForResult(intent, EDIT_IMAGE_REQUEST_CODE)
//        } else {
//            context.startActivity(intent)
//        }
//    }
//
//    /**
//     * 处理Activity结果
//     */
//    fun handleActivityResult(
//        requestCode: Int,
//        resultCode: Int,
//        data: Intent?,
//        onEditComplete: (Uri?) -> Unit
//    ) {
//        if (requestCode == EDIT_IMAGE_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                val editedUri = data?.getStringExtra(PhotoEditorActivity.RESULT_EDITED_URI)
//                    ?.let { Uri.parse(it) }
//                onEditComplete(editedUri)
//            } else {
//                onEditComplete(null)
//            }
//        }
//    }
//
//    /**
//     * 检查并请求存储权限
//     */
//    fun checkStoragePermission(context: Context): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    /**
//     * 请求存储权限
//     */
//    fun requestStoragePermission(activity: Activity, requestCode: Int) {
//        ActivityCompat.requestPermissions(
//            activity,
//            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//            requestCode
//        )
//    }
//
//    const val EDIT_IMAGE_REQUEST_CODE = 1001
//    const val STORAGE_PERMISSION_REQUEST_CODE = 1002
//}
//
///**
// * 在Compose中使用的图片编辑器启动器
// */
//@Composable
//fun rememberPhotoEditorLauncher(
//    onEditComplete: (Uri?) -> Unit
//): PhotoEditorLauncher {
//    val context = LocalContext.current
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val editedUri = result.data?.getStringExtra(PhotoEditorActivity.RESULT_EDITED_URI)
//                ?.let { Uri.parse(it) }
//            onEditComplete(editedUri)
//        } else {
//            onEditComplete(null)
//        }
//    }
//
//    return PhotoEditorLauncher(launcher, context)
//}
//
//class PhotoEditorLauncher(
//    private val launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
//    private val context: Context
//) {
//    fun launch(imageUri: Uri, saveToOriginal: Boolean = false) {
//        val intent = PhotoEditorActivity.createIntent(context, imageUri, saveToOriginal)
//        launcher.launch(intent)
//    }
//}
//
///**
// * 权限请求对话框
// */
//@Composable
//fun PermissionDialog(
//    permission: String,
//    rationale: String,
//    onDismiss: () -> Unit,
//    onConfirm: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("需要权限") },
//        text = { Text(rationale) },
//        confirmButton = {
//            Button(onClick = onConfirm) {
//                Text("去设置")
//            }
//        },
//        dismissButton = {
//            Button(onClick = onDismiss) {
//                Text("取消")
//            }
//        }
//    )
//}
//
///**
// * 图片编辑功能集成示例
// */
//@Composable
//fun ImageEditFeatureIntegration() {
//    val context = LocalContext.current
//    val showPermissionDialog = remember { mutableStateOf(false) }
//
//    // 图片编辑器启动器
//    val photoEditorLauncher = rememberPhotoEditorLauncher { editedUri ->
//        editedUri?.let {
//            // 处理编辑后的图片
//            // 例如：更新笔记中的图片
//        }
//    }
//
//    // 权限检查
//    fun checkAndLaunchEditor(imageUri: Uri) {
//        if (PhotoEditorUtils.checkStoragePermission(context)) {
//            // 有权限，启动编辑器
//            photoEditorLauncher.launch(imageUri, saveToOriginal = false)
//        } else {
//            // 无权限，显示对话框
//            showPermissionDialog.value = true
//        }
//    }
//
//    // 权限请求对话框
//    if (showPermissionDialog.value) {
//        PermissionDialog(
//            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            rationale = "需要存储权限来保存编辑后的图片。请前往设置开启权限。",
//            onDismiss = { showPermissionDialog.value = false },
//            onConfirm = {
//                // 跳转到应用设置
//                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                    data = Uri.fromParts("package", context.packageName, null)
//                }
//                context.startActivity(intent)
//                showPermissionDialog.value = false
//            }
//        )
//    }
//
//    // 在实际使用中，这里会有图片列表和编辑按钮
//    // 当用户点击图片时，调用 checkAndLaunchEditor(imageUri)
//}
