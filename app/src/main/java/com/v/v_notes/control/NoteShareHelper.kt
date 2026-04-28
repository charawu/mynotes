package com.v.v_notes.control

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.util.Log
import androidx.core.content.FileProvider
import com.v.v_notes.data.model.Note
import java.io.File

object NoteShareHelper {
    private const val TAG = "NoteShareHelper"

    private const val FILE_PROVIDER_AUTHORITY = ".fileprovider"

    fun buildShareText(note: Note): String {
        val builder = StringBuilder()

        builder.append("${note.title}\n")

        val plainContent = htmlToPlainText(note.content)
        if (plainContent.isNotBlank()) {
            builder.append("\n")
            builder.append(plainContent)
        }

        if (note.todoItems.isNotEmpty()) {
            if (builder.isNotEmpty() && !builder.endsWith("\n")) {
                builder.append("\n")
            }

            if (builder.isNotEmpty() && plainContent.isNotBlank()) {
                builder.append("\n")
            }

            builder.append("待办事项：\n")

            note.todoItems.forEachIndexed { index, todo ->
                val statusChar = if (todo.isCompleted) "✓" else "✗"
                val number = index + 1
                builder.append("$number. $statusChar ${todo.text}\n")
            }

            //统计信息
            val completedCount = note.todoItems.count { it.isCompleted }
            val totalCount = note.todoItems.size
            val progress = if (totalCount > 0) {
                (completedCount.toFloat() / totalCount * 100).toInt()
            } else {
                0
            }

            builder.append("\n完成进度：$completedCount/$totalCount ($progress%)\n")
        }

        return builder.toString().trim()
    }

    private fun htmlToPlainText(html: String): String {
        if (html.isBlank()) return ""

        return try {
            var text = html

            //首先处理所有列表，确保每个列表项都有换行
            text = processAllListsWithLineBreaks(text)

            //处理其他HTML标签
            text = processBasicHtmlTags(text)

            //移除所有HTML标签
            text = text.replace(Regex("<[^>]*>"), "")

            //解码HTML实体
            val result = decodeHtmlEntities(text)

            //清理格式，但保留列表项之间的换行
            cleanFormattingForLists(result)
        } catch (e: Exception) {
            Log.e(TAG, "HTML转换失败", e)
            html
        }
    }

    private fun processAllListsWithLineBreaks(html: String): String {
        var result = html

        //先处理所有有序列表
        result = processListsByType(result, "ol", false)

        //再处理所有无序列表
        result = processListsByType(result, "ul", true)

        //处理所有独立的<li>标签
        result = processIndividualLiTags(result)

        return result
    }

    private fun processListsByType(html: String, tag: String, isUnordered: Boolean): String {
        var result = html
        val listRegex = Regex("<${tag}[^>]*>([\\s\\S]*?)</${tag}>")

        var match: MatchResult?
        var processedCount = 0
        var offset = 0

        //使用while循环处理匹配
        while (true) {
            match = listRegex.find(result, offset)
            if (match == null) break

            val listContent = match.groupValues[1]

            //处理列表内容
            val processedList = if (isUnordered) {
                processUnorderedListContent(listContent)
            } else {
                processOrderedListContent(listContent)
            }

            //在列表前后添加换行，确保列表项之间有明确分隔
            val listWithLineBreaks = "\n$processedList\n"

            //替换原始列表
            val start = match.range.first
            val end = match.range.last + 1
            result = result.replaceRange(start, end, listWithLineBreaks)

            //更新偏移量
            offset = start + listWithLineBreaks.length
            processedCount++

            //防止无限循环
            if (processedCount > 100) {
                Log.w(TAG, "处理列表超过100次，可能陷入循环")
                break
            }
        }

        return result
    }

    private fun processOrderedListContent(content: String): String {
        val result = StringBuilder()
        var itemNumber = 1

        //使用正则表达式查找所有<li>标签
        val liRegex = Regex("<li[^>]*>([\\s\\S]*?)</li>")
        var lastEnd = 0

        liRegex.findAll(content).forEach { match ->
            val itemContent = match.groupValues[1]

            //确保每个列表项后都有换行
            val processedItem = htmlToPlainTextInternal(itemContent)
            result.append("${itemNumber}. ${processedItem.trim()}\n")
            itemNumber++

            lastEnd = match.range.last
        }

        //处理<li>标签之间的文本
        if (lastEnd < content.length) {
            val remaining = content.substring(lastEnd + 1)
            if (remaining.isNotBlank()) {
                result.append(htmlToPlainTextInternal(remaining))
            }
        }

        return result.toString().trim()
    }

    private fun processUnorderedListContent(content: String): String {
        val result = StringBuilder()

        //使用正则表达式查找所有<li>标签
        val liRegex = Regex("<li[^>]*>([\\s\\S]*?)</li>")
        var lastEnd = 0

        liRegex.findAll(content).forEach { match ->
            val itemContent = match.groupValues[1]

            //确保每个列表项后都有换行
            val processedItem = htmlToPlainTextInternal(itemContent)
            result.append("• ${processedItem.trim()}\n")

            lastEnd = match.range.last
        }

        //处理<li>标签之间的文本
        if (lastEnd < content.length) {
            val remaining = content.substring(lastEnd + 1)
            if (remaining.isNotBlank()) {
                result.append(htmlToPlainTextInternal(remaining))
            }
        }

        return result.toString().trim()
    }

    private fun processIndividualLiTags(html: String): String {
        var result = html

        //查找所有<li>标签
        val liRegex = Regex("<li[^>]*>([\\s\\S]*?)</li>")
        var processedCount = 0
        var offset = 0

        while (true) {
            val match = liRegex.find(result, offset)
            if (match == null) break

            val itemContent = match.groupValues[1]

            //检查是否在列表内部
            val beforeContext = result.substring(
                maxOf(0, match.range.first - 10),
                match.range.first
            )
            val isInsideList = beforeContext.contains(Regex("</?[uo]l", RegexOption.IGNORE_CASE))

            if (!isInsideList) {
                //独立列表项，确保有换行分隔
                val processedItem = htmlToPlainTextInternal(itemContent)
                val replacement = "• ${processedItem.trim()}\n"

                val start = match.range.first
                val end = match.range.last + 1
                result = result.replaceRange(start, end, replacement)
                offset = start + replacement.length
            } else {
                offset = match.range.last + 1
            }

            processedCount++
            if (processedCount > 100) {
                Log.w(TAG, "处理独立li标签超过100次，可能陷入循环")
                break
            }
        }

        return result
    }
    private fun htmlToPlainTextInternal(html: String): String {
        if (html.isBlank()) return ""

        var text = html
        //处理HTML标签
        text = processBasicHtmlTags(text)
        //移除所有HTML标签
        text = text.replace(Regex("<[^>]*>"), "")
        //解码HTML实体
        val result = decodeHtmlEntities(text)

        return result
    }

    private fun processBasicHtmlTags(html: String): String {
        return html
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<div[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</div>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n")
    }

    private fun decodeHtmlEntities(text: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(text).toString()
        }
    }

    private fun cleanFormattingForLists(text: String): String {
        var result = text.trim()

        //处理有序列表项：数字. 开头的内容
        result = result.replace(Regex("(\\d+\\.\\s+)"), "\n$1")

        result = result.replace(Regex("(•\\s+)"), "\n$1")

        //清理多余的空白字符
        result = result
            .replace(Regex(" {2,}"), " ")  // 多个空格合并为一个
            .replace(Regex("^\\s+", RegexOption.MULTILINE), "")  // 去除行首空白
            .replace(Regex("\\s+$", RegexOption.MULTILINE), "")  // 去除行尾空白

        //清理换行
        result = result.replace(Regex("\\n{3,}"), "\n\n")

        // 清理列表项内部的换行
        val lines = result.lines()
        val processedLines = mutableListOf<String>()
        var inListItem = false

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) {
                inListItem = false
                processedLines.add("")
                continue
            }

            //检查是否是列表项
            val isListItem = trimmedLine.matches(Regex("^(\\d+\\.\\s+.*|•\\s+.*)"))

            if (isListItem) {
                //如果是列表项，确保前面有空行（除非是第一个列表项）
                if (processedLines.isNotEmpty() && processedLines.last().isNotBlank() && !inListItem) {
                    processedLines.add("")
                }
                processedLines.add(trimmedLine)
                inListItem = true
            } else {
                //如果不是列表项
                processedLines.add(trimmedLine)
                inListItem = false
            }
        }

        //重新组合字符串
        result = processedLines.joinToString("\n")

        //最后清理开头和结尾的空行
        result = result.trim()

        return result
    }

    fun buildShareMarkdown(note: Note): String {
        val builder = StringBuilder()

        //标题
        builder.append("# ${note.title}\n\n")

        //内容
        val plainContent = htmlToPlainText(note.content)
        if (plainContent.isNotBlank()) {
            builder.append("$plainContent\n\n")
        }

        //待办事项
        if (note.todoItems.isNotEmpty()) {
            if (plainContent.isNotBlank()) {
                builder.append("\n")
            }
            builder.append("## 待办事项\n")

            note.todoItems.forEach { todo ->
                val statusChar = if (todo.isCompleted) "[x]" else "[ ]"
                builder.append("- $statusChar ${todo.text}\n")
            }
        }

        return builder.toString().trim()
    }

    fun getShareableUri(context: Context, fileUriString: String): Uri? {
        return try {
            val filePath = if (fileUriString.startsWith("file://")) {
                fileUriString.removePrefix("file://")
            } else {
                fileUriString
            }

            val file = File(filePath)
            if (!file.exists()) {
                Log.w(TAG, "文件不存在: $filePath")
                return null
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}$FILE_PROVIDER_AUTHORITY",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "获取分享Uri失败: $fileUriString", e)
            null
        }
    }

    fun convertImageUris(context: Context, imageUris: List<String>): List<Uri> {
        return imageUris.mapNotNull { uriString ->
            getShareableUri(context, uriString)
        }
    }

    fun createShareIntent(
        context: Context,
        note: Note,
        imageUris: List<Uri> = emptyList(),
        shareType: ShareType = ShareType.ALL
    ): Intent {
        val shareText = buildShareText(note)

        return when (shareType) {
            ShareType.TEXT -> {
                // 仅分享文本
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, note.title)
                }
            }

            ShareType.IMAGE -> {
                //仅分享图片
                when {
                    imageUris.size == 1 -> {
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, imageUris[0])
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    imageUris.size > 1 -> {
                        Intent().apply {
                            action = Intent.ACTION_SEND_MULTIPLE
                            type = "image/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    else -> {
                        //分享文本
                        createShareIntent(context, note, emptyList(), ShareType.TEXT)
                    }
                }
            }

            ShareType.ALL -> {
                //分享文本和图片
                when {
                    imageUris.isEmpty() -> {
                        //只分享文本
                        createShareIntent(context, note, emptyList(), ShareType.TEXT)
                    }
                    imageUris.size == 1 -> {
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, imageUris[0])
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_SUBJECT, note.title)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    else -> {
                        Intent().apply {
                            action = Intent.ACTION_SEND_MULTIPLE
                            type = "image/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_SUBJECT, note.title)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                }
            }
        }
    }

    fun shareNote(
        context: Context,
        note: Note,
        imageUris: List<Uri> = emptyList(),
        shareType: ShareType = ShareType.ALL,
        chooserTitle: String = "分享笔记"
    ) {
        val shareIntent = createShareIntent(context, note, imageUris, shareType)
        context.startActivity(
            Intent.createChooser(shareIntent, chooserTitle)
        )
    }

    enum class ShareType {
        TEXT,
        IMAGE,
        ALL
    }
}