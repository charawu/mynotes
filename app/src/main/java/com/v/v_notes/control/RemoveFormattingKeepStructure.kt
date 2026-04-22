package com.v.v_notes.control

fun removeFormattingKeepStructure(html: String): String {
    if (html.isBlank()) return ""

    var result = html

    // 1. 移除内联样式属性（style="..."） - 清除字体大小、颜色、背景等样式
    result = result.replace(Regex("""style\s*=\s*["'][^"']*["']"""), "")

    // 2. 移除class属性
    result = result.replace(Regex("""class\s*=\s*["'][^"']*["']"""), "")

    // 3. 替换标题标签为普通段落标签，保留文本内容
    // 处理<h1>到<h6>所有标题标签
    result = result.replace(Regex("""<h([1-6])\b[^>]*>"""), "<p>")
    result = result.replace(Regex("""</h([1-6])>"""), "</p>")

    // 4. 移除粗体标签，保留文本内容
    result = result.replace(Regex("""</?b\b[^>]*>"""), "")
    result = result.replace(Regex("""</?strong\b[^>]*>"""), "")

    // 5. 移除斜体标签，保留文本内容
    result = result.replace(Regex("""</?i\b[^>]*>"""), "")
    result = result.replace(Regex("""</?em\b[^>]*>"""), "")

    // 6. 移除下划线标签，保留文本内容
    result = result.replace(Regex("""</?u\b[^>]*>"""), "")

    // 7. 处理列表：移除列表标签，但保留列表项文本和换行
    // 移除<ul>和<ol>标签
    result = result.replace(Regex("""</?ul\b[^>]*>"""), "")
    result = result.replace(Regex("""</?ol\b[^>]*>"""), "")
    // 将<li>标签替换为换行，保留文本内容
    result = result.replace(Regex("""<li\b[^>]*>"""), "<br>")
    result = result.replace(Regex("""</li>"""), "")

    // 8. 移除其他可能的格式标签（根据您的工具栏功能）
    // 移除删除线标签（如果支持）
    result = result.replace(Regex("""</?s\b[^>]*>"""), "")
    result = result.replace(Regex("""</?strike\b[^>]*>"""), "")

    // 9. 保留段落和换行标签
    // 确保<p>标签没有属性
    result = result.replace(Regex("""<p\b[^>]*>"""), "<p>")
    // 保留<br>标签（包括自闭合和带属性的）
    result = result.replace(Regex("""<br\s*/?>"""), "<br>")

    // 10. 清理多余的空白和空标签
    // 移除完全空的段落标签
    result = result.replace(Regex("""<p>\s*</p>"""), "")
    // 合并连续的<br>标签（最多保留2个）
    result = result.replace(Regex("""(<br>\s*){3,}"""), "<br><br>")

    // 11. 确保文本有基本的段落包裹
    val hasParagraph = result.contains("<p>") || result.contains("<br>")
    val hasContent = result.replace(Regex("""<[^>]+>|\s"""), "").isNotBlank()

    if (hasContent && !hasParagraph) {
        // 如果没有段落标签但有内容，用段落包裹
        result = "<p>$result</p>"
    } else if (!hasContent) {
        // 如果完全没有内容，返回空字符串
        result = ""
    }

    // 12. 处理HTML实体（保留必要的）
    result = result.replace("&nbsp;", " ") // 空格实体转为普通空格
    // 其他HTML实体保持不变（如&lt; &gt; &amp;等）

    return result.trim()
}
