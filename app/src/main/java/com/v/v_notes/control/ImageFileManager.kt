package com.v.v_notes.control

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 图片文件管理器
 * 负责将图片保存到应用的私有目录，并管理图片的生命周期
 */
class ImageFileManager(private val context: Context) {

    companion object {
        private const val IMAGE_DIRECTORY = "notes_images"
        private const val TAG = "ImageFileManager"
    }

    /**
     * 将图片URI保存到应用的私有目录
     * @param imageUri 原始图片URI（可以是内容URI、文件URI或网络URI）
     * @return 保存后的内部文件URI，如果失败返回null
     */
    fun saveImageToPrivateStorage(imageUri: Uri): Uri? {
        return try {
            when (imageUri.scheme) {
                "content" -> saveContentUriToPrivate(imageUri)
                "file" -> saveFileUriToPrivate(imageUri)
                else -> {
                    // 对于其他类型的URI（如网络URI），先下载到临时文件
                    saveToPrivateStorageFromStream(imageUri)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存图片失败: ${e.message}", e)
            null
        }
    }

    /**
     * 保存内容提供者URI对应的图片
     */
    private fun saveContentUriToPrivate(contentUri: Uri): Uri? {
        val bitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, contentUri)
                )
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, contentUri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "解码内容URI图片失败: ${e.message}", e)
            return null
        }

        return saveBitmapToPrivate(bitmap)
    }

    /**
     * 保存文件URI对应的图片
     */
    private fun saveFileUriToPrivate(fileUri: Uri): Uri? {
        val filePath = fileUri.path ?: return null
        val file = File(filePath)
        if (!file.exists()) return null

        val bitmap = try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            Log.e(TAG, "解码文件图片失败: ${e.message}", e)
            return null
        }

        if (bitmap == null) {
            Log.e(TAG, "无法解码文件图片: $filePath")
            return null
        }

        return saveBitmapToPrivate(bitmap)
    }

    /**
     * 从输入流保存图片
     */
    private fun saveToPrivateStorageFromStream(uri: Uri): Uri? {
        val inputStream: InputStream? = try {
            when (uri.scheme) {
                "http", "https" -> {
                    // 网络图片下载（简化版，实际项目中建议使用Coil或Glide）
                    val connection = java.net.URL(uri.toString()).openConnection()
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream
                }
                else -> {
                    // 尝试从内容解析器获取
                    context.contentResolver.openInputStream(uri)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "打开图片输入流失败: ${e.message}", e)
            null
        }

        inputStream?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                return saveBitmapToPrivate(bitmap)
            }
        }

        return null
    }

    /**
     * 将Bitmap保存到私有存储
     */
    private fun saveBitmapToPrivate(bitmap: Bitmap): Uri? {
        val imageDir = getImageDirectory()
        if (!imageDir.exists()) {
            if (!imageDir.mkdirs()) {
                Log.e(TAG, "创建图片目录失败: ${imageDir.absolutePath}")
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "NOTE_IMG_${timeStamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
        val imageFile = File(imageDir, fileName)

        return try {
            FileOutputStream(imageFile).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                outStream.flush()
            }

            // 返回内部存储的文件URI
            Uri.fromFile(imageFile).also {
                Log.d(TAG, "图片保存成功: ${it.path}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存Bitmap到文件失败: ${e.message}", e)
            null
        }
    }

    /**
     * 批量保存图片到私有存储
     * @param imageUris 原始图片URI列表
     * @return 保存后的内部URI列表
     */
    fun saveImagesToPrivateStorage(imageUris: List<Uri>): List<String> {
        val savedUris = mutableListOf<String>()

        for (uri in imageUris) {
            val savedUri = saveImageToPrivateStorage(uri)
            savedUri?.let {
                savedUris.add(it.toString())
            }
        }

        return savedUris
    }

    /**
     * 删除私有存储中的图片文件
     * @param imageUri 要删除的图片URI字符串
     */
    fun deleteImageFromPrivateStorage(imageUri: String) {
        try {
            val uri = Uri.parse(imageUri)
            val filePath = uri.path ?: return

            val file = File(filePath)
            if (file.exists() && file.delete()) {
                Log.d(TAG, "删除图片成功: $filePath")
            } else {
                Log.w(TAG, "删除图片失败或文件不存在: $filePath")
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除图片异常: ${e.message}", e)
        }
    }

    /**
     * 批量删除图片
     * @param imageUris 要删除的图片URI字符串列表
     */
    fun deleteImagesFromPrivateStorage(imageUris: List<String>) {
        for (uri in imageUris) {
            deleteImageFromPrivateStorage(uri)
        }
    }

    /**
     * 清理未使用的图片
     * @param usedImageUris 当前使用的图片URI列表
     * @param allStoredImages 所有已存储的图片URI（可选，用于全量清理）
     */
    fun cleanupUnusedImages(usedImageUris: List<String>, allStoredImages: List<String>? = null) {
        val usedFiles = usedImageUris.mapNotNull { uri ->
            Uri.parse(uri).path?.let { File(it) }
        }

        val imageDir = getImageDirectory()
        if (!imageDir.exists() || !imageDir.isDirectory) return

        val allFiles = allStoredImages?.mapNotNull { uri ->
            Uri.parse(uri).path?.let { File(it) }
        } ?: imageDir.listFiles()?.toList() ?: emptyList()

        for (file in allFiles) {
            if (file.isFile && !usedFiles.any { it.absolutePath == file.absolutePath }) {
                if (file.delete()) {
                    Log.d(TAG, "清理未使用图片: ${file.name}")
                }
            }
        }
    }

    /**
     * 获取图片目录
     */
    fun getImageDirectory(): File {
        return File(context.filesDir, IMAGE_DIRECTORY)
    }

    /**
     * 获取目录中的所有图片文件
     */
    fun getAllStoredImages(): List<String> {
        val imageDir = getImageDirectory()
        if (!imageDir.exists() || !imageDir.isDirectory) return emptyList()

        return imageDir.listFiles { _, name ->
            name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".png") || name.endsWith(".webp")
        }?.map { Uri.fromFile(it).toString() } ?: emptyList()
    }

    /**
     * 获取图片文件
     * @param imageUri 图片URI字符串
     * @return 图片文件，如果不存在返回null
     */
    fun getImageFile(imageUri: String): File? {
        return try {
            val uri = Uri.parse(imageUri)
            val filePath = uri.path ?: return null
            val file = File(filePath)
            if (file.exists()) file else null
        } catch (e: Exception) {
            Log.e(TAG, "获取图片文件失败: ${e.message}", e)
            null
        }
    }

    /**
     * 获取图片的可访问URI（用于显示）
     * 对于内部文件，使用FileProvider获取可共享的URI
     */
    fun getAccessibleImageUri(imageUri: String): Uri {
        return try {
            val uri = Uri.parse(imageUri)
            // 如果是文件URI，直接返回
            if (uri.scheme == "file") {
                uri
            } else {
                // 否则尝试解析为文件URI
                val filePath = uri.path ?: return uri
                Uri.fromFile(File(filePath))
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取可访问URI失败: ${e.message}", e)
            Uri.parse(imageUri)
        }
    }
}
