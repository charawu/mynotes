package com.v.v_notes.control

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ImageFileManager(private val context: Context) {

    companion object {
        private const val IMAGE_DIRECTORY = "notes_images"
        private const val TEMP_IMAGE_DIRECTORY = "temp_notes_images"
        private const val TAG = "ImageFileManager"
    }

    //获取临时目录
    fun getTempImageDirectory(): File {
        return File(context.filesDir, TEMP_IMAGE_DIRECTORY)
    }

    //保存到临时目录
    fun saveImageToTempStorage(imageUri: Uri): Uri? {
        return try {
            when (imageUri.scheme) {
                "content" -> saveContentUriToTemp(imageUri)
                "file" -> saveFileUriToTemp(imageUri)
                else -> {
                    saveToTempStorageFromStream(imageUri)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存图片到临时目录失败: ${e.message}", e)
            null
        }
    }

    private fun saveContentUriToTemp(contentUri: Uri): Uri? {
        val bitmap = try {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, contentUri)
            )
        } catch (e: Exception) {
            Log.e(TAG, "解码内容URI图片到临时目录失败: ${e.message}", e)
            return null
        }
        return saveBitmapToTemp(bitmap)
    }

    private fun saveFileUriToTemp(fileUri: Uri): Uri? {
        val filePath = fileUri.path ?: return null
        val file = File(filePath)
        if (!file.exists()) return null

        val bitmap = try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            Log.e(TAG, "解码文件图片到临时目录失败: ${e.message}", e)
            return null
        }

        if (bitmap == null) {
            Log.e(TAG, "无法解码文件图片到临时目录: $filePath")
            return null
        }
        return saveBitmapToTemp(bitmap)
    }

    private fun saveToTempStorageFromStream(uri: Uri): Uri? {
        val inputStream: InputStream? = try {
            when (uri.scheme) {
                "http", "https" -> {
                    val connection = java.net.URL(uri.toString()).openConnection()
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream
                }
                else -> {
                    context.contentResolver.openInputStream(uri)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "打开图片输入流到临时目录失败: ${e.message}", e)
            null
        }

        inputStream?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                return saveBitmapToTemp(bitmap)
            }
        }
        return null
    }

    private fun saveBitmapToTemp(bitmap: Bitmap): Uri? {
        val tempImageDir = getTempImageDirectory()
        if (!tempImageDir.exists()) {
            if (!tempImageDir.mkdirs()) {
                Log.e(TAG, "创建临时图片目录失败: ${tempImageDir.absolutePath}")
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        //临时图片添加前缀以区分
        val fileName = "TEMP_NOTE_IMG_${timeStamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
        val tempImageFile = File(tempImageDir, fileName)

        return try {
            FileOutputStream(tempImageFile).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                outStream.flush()
            }
            Uri.fromFile(tempImageFile).also {
                Log.d(TAG, "图片保存到临时目录成功: ${it.path}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存Bitmap到临时文件失败: ${e.message}", e)
            null
        }
    }

    fun isTempUri(uri: Uri): Boolean {
        val path = uri.path ?: return false
        return path.contains(TEMP_IMAGE_DIRECTORY)
    }

    //将临时目录移动到私有目录
    fun moveTempImageToPrivate(tempUri: Uri): String? {
        if (!isTempUri(tempUri)) {
            Log.w(TAG, "尝试移动非临时文件: $tempUri")
            return null
        }

        val tempFilePath = tempUri.path ?: return null
        val tempFile = File(tempFilePath)
        if (!tempFile.exists()) {
            Log.e(TAG, "临时文件不存在，无法移动: $tempFilePath")
            return null
        }

        val imageDir = getImageDirectory()
        if (!imageDir.exists()) {
            if (!imageDir.mkdirs()) {
                Log.e(TAG, "创建正式图片目录失败: ${imageDir.absolutePath}")
                return null
            }
        }

        //移除 TEMP_
        val tempFileName = tempFile.name
        val finalFileName = if (tempFileName.startsWith("TEMP_")) {
            tempFileName.substring(5)
        } else {
            //如果临时文件没有标准前缀，仍使用原名，但添加时间戳避免冲突
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            "NOTE_IMG_${timeStamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
        }

        val finalFile = File(imageDir, finalFileName)

        return try {
            if (tempFile.renameTo(finalFile)) {
                val finalUri = Uri.fromFile(finalFile).toString()
                Log.d(TAG, "临时文件移动成功: $tempFilePath -> ${finalFile.absolutePath}")
                finalUri
            } else {
                Log.w(TAG, "直接重命名失败，尝试复制: $tempFilePath")
                try {
                    tempFile.copyTo(target = finalFile, overwrite = true)
                    //复制成功后尝试删除临时文件
                    if (tempFile.delete()) {
                        val finalUri = Uri.fromFile(finalFile).toString()
                        Log.d(TAG, "临时文件通过复制移动成功: ${finalFile.absolutePath}")
                        finalUri
                    } else {
                        //失败处理
                        Log.e(TAG, "复制成功但无法删除临时文件: $tempFilePath")
                        Uri.fromFile(finalFile).toString()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "复制临时文件失败: $tempFilePath, 错误: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "移动临时文件异常: ${e.message}", e)
            null
        }
    }

    //清空临时目录
    fun clearTempStorage() {
        val tempDir = getTempImageDirectory()
        try {
            if (tempDir.exists()) {
                val deleted = tempDir.deleteRecursively()
                if (deleted) {
                    Log.d(TAG, "临时目录清空成功: ${tempDir.absolutePath}")
                } else {
                    Log.w(TAG, "临时目录清空失败: ${tempDir.absolutePath}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清空临时目录异常: ${e.message}", e)
        }
    }

    private fun saveContentUriToPrivate(contentUri: Uri): Uri? {
        val bitmap = try {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, contentUri)
            )
        } catch (e: Exception) {
            Log.e(TAG, "解码内容URI图片失败: ${e.message}", e)
            return null
        }

        return saveBitmapToPrivate(bitmap)
    }

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

    private fun saveToPrivateStorageFromStream(uri: Uri): Uri? {
        val inputStream: InputStream? = try {
            when (uri.scheme) {
                "http", "https" -> {
                    val connection = java.net.URL(uri.toString()).openConnection()
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream
                }
                else -> {
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

            Uri.fromFile(imageFile).also {
                Log.d(TAG, "图片保存成功: ${it.path}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存Bitmap到文件失败: ${e.message}", e)
            null
        }
    }

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

    fun deleteImagesFromPrivateStorage(imageUris: List<String>) {
        for (uri in imageUris) {
            deleteImageFromPrivateStorage(uri)
        }
    }

    fun getImageDirectory(): File {
        return File(context.filesDir, IMAGE_DIRECTORY)
    }

    fun getAccessibleImageUri(imageUri: String): Uri {
        return try {
            val uri = Uri.parse(imageUri)
            if (uri.scheme == "file") {
                uri
            } else {
                val filePath = uri.path ?: return uri
                Uri.fromFile(File(filePath))
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取可访问URI失败: ${e.message}", e)
            Uri.parse(imageUri)
        }
    }
}