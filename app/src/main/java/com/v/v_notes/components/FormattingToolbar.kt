package com.v.v_notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import com.v.v_notes.R
import com.v.v_notes.addlist.resetToPlainText

@OptIn(ExperimentalRichTextApi::class)
@Composable
fun FormattingToolbar(
    modifier: Modifier = Modifier,
    editorState: RichTextState,
    onImageClick: () -> Unit,
    onAddTodo: () -> Unit
) {
    // 定义所有样式状态变量
    var boldState by remember { mutableStateOf(false) }
    var italicState by remember { mutableStateOf(false) }
    var underlineState by remember { mutableStateOf(false) }
    var unorderedListState by remember { mutableStateOf(false) }
    var orderedListState by remember { mutableStateOf(false) }
    var heading1State by remember { mutableStateOf(false) }
    var heading2State by remember { mutableStateOf(false) }
    var normalTextState by remember { mutableStateOf(false) }

    // 监听编辑器状态变化，检测当前选中文本的样式
    LaunchedEffect(editorState.selection) {
        val currentSpanStyle = editorState.currentSpanStyle

        // 检测粗体 - 修复：使用更精确的检测
        val fontWeightWeight = currentSpanStyle?.fontWeight?.weight ?: 400
        val isBold = fontWeightWeight >= FontWeight.Bold.weight && fontWeightWeight < FontWeight.ExtraBold.weight
        boldState = isBold

        // 检测斜体
        val isItalic = currentSpanStyle?.fontStyle == FontStyle.Italic
        italicState = isItalic

        // 检测下划线
        val isUnderline = currentSpanStyle?.textDecoration?.contains(TextDecoration.Underline) ?: false
        underlineState = isUnderline

        // 检测无序列表
        val isUnorderedList = editorState.isUnorderedList
        unorderedListState = isUnorderedList

        // 检测有序列表
        val isOrderedList = editorState.isOrderedList
        orderedListState = isOrderedList

        // 检测标题样式 - 基于字体大小判断
        val fontSize = currentSpanStyle?.fontSize
        val isHeading1 = fontSize == 24.sp
        val isHeading2 = fontSize == 20.sp
        val isNormalText = fontSize == 16.sp || fontSize == null || fontSize == 14.sp

        heading1State = isHeading1
        heading2State = isHeading2
        normalTextState = isNormalText
    }

    // 样式修改器函数 - 与粗体按钮相同的背景样式
    @Composable
    fun Stylemodifier(newState: Boolean): Modifier {
        return if (newState) {
            Modifier.background(
                MaterialTheme.colorScheme.inversePrimary,
                shape = MaterialTheme.shapes.small
            )
        } else {
            Modifier.background(Color.Transparent)
        }
    }

    // 记录当前的默认样式状态
    var currentDefaultStyle by remember { mutableStateOf(SpanStyle()) }

    // 监听样式变化，包括点击按钮时的样式切换
    LaunchedEffect(editorState.selection, boldState, italicState, underlineState) {
        // 当没有选中文本时，我们需要跟踪当前的默认样式
        // 我们可以通过获取光标位置的样式来实现
        val cursorStyle = editorState.currentSpanStyle ?: currentDefaultStyle

        // 更新当前默认样式
        currentDefaultStyle = cursorStyle
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.large
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 粗体按钮
            IconButton(
                onClick = {
                    val currentSpanStyle = editorState.currentSpanStyle
                    val currentFontWeight = currentSpanStyle?.fontWeight
                    val isCurrentlyBold =
                        (currentFontWeight?.weight ?: 400) >= FontWeight.Bold.weight

                    if (isCurrentlyBold) {
                        // 如果当前是粗体，切换到正常字重
                        editorState.toggleSpanStyle(
                            SpanStyle(fontWeight = FontWeight.Normal)
                        )
                        boldState = false
                    } else {
                        // 如果当前不是粗体，切换到粗体
                        editorState.toggleSpanStyle(
                            SpanStyle(fontWeight = FontWeight.Bold)
                        )
                        boldState = true
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(R.drawable.baseline_format_bold_24),
                    contentDescription = "粗体",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(boldState))
                )
            }

            // 斜体按钮
            IconButton(
                onClick = {
                    val currentSpanStyle = editorState.currentSpanStyle
                    val isCurrentlyItalic = currentSpanStyle?.fontStyle == FontStyle.Italic

                    if (isCurrentlyItalic) {
                        // 如果当前是斜体，切换到正常
                        editorState.toggleSpanStyle(
                            SpanStyle(fontStyle = FontStyle.Normal)
                        )
                        italicState = false
                    } else {
                        // 如果当前不是斜体，切换到斜体
                        editorState.toggleSpanStyle(
                            SpanStyle(fontStyle = FontStyle.Italic)
                        )
                        italicState = true
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatItalic,
                    contentDescription = "斜体",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(italicState))
                )
            }

            // 下划线按钮
            IconButton(
                onClick = {
                    val currentSpanStyle = editorState.currentSpanStyle
                    val hasUnderline = currentSpanStyle?.textDecoration?.contains(TextDecoration.Underline) ?: false

                    if (hasUnderline) {
                        // 移除下划线
                        editorState.removeSpanStyle(
                            SpanStyle(textDecoration = TextDecoration.Underline)
                        )
                        underlineState = false
                    } else {
                        // 添加下划线
                        editorState.addSpanStyle(
                            SpanStyle(textDecoration = TextDecoration.Underline)
                        )
                        underlineState = true
                    }

                    // 额外的保护：检查并移除可能的删除线
                    val updatedStyle = editorState.currentSpanStyle
                    val hasLineThrough = updatedStyle?.textDecoration?.contains(TextDecoration.LineThrough) ?: false

                    if (hasLineThrough) {
                        // 如果意外出现了删除线，立即移除
                        editorState.removeSpanStyle(
                            SpanStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatUnderlined,
                    contentDescription = "下划线",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(underlineState))
                )
            }

            // 项目符号列表按钮
            IconButton(
                onClick = {
                    editorState.toggleUnorderedList()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "项目符号列表",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(unorderedListState))
                )
            }

            // 编号列表按钮
            IconButton(
                onClick = {
                    editorState.toggleOrderedList()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = "编号列表",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(orderedListState))
                )
            }

            // 1级标题按钮
            IconButton(
                onClick = {
                    editorState.addParagraphStyle(
                        ParagraphStyle(lineHeight = 32.sp)
                    )
                    // 使用Normal字重，而不是Bold
                    editorState.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    // 设置标题状态
                    heading1State = true
                    heading2State = false
                    normalTextState = false
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_looks_one_24),
                    contentDescription = "1级标题",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(heading1State)),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 2级标题按钮
            IconButton(
                onClick = {
                    editorState.addParagraphStyle(
                        ParagraphStyle(lineHeight = 28.sp)
                    )
                    // 使用Normal字重，而不是Bold
                    editorState.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    // 设置标题状态
                    heading1State = false
                    heading2State = true
                    normalTextState = false
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_looks_two_24),
                    contentDescription = "2级标题",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(heading2State)),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 正文标题按钮
            IconButton(
                onClick = {
                    editorState.addParagraphStyle(ParagraphStyle())
                    // 使用Normal字重，而不是Medium
                    editorState.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    // 设置标题状态
                    heading1State = false
                    heading2State = false
                    normalTextState = true
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_text_format_24),
                    contentDescription = "正文标题",
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(normalTextState)),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 清除格式按钮
            IconButton(
                onClick = {
                    // 清除格式的完整实现
                    try {
                        // 使用我们改进的清除格式方法
                        resetToPlainText(editorState)

                        // 重置所有状态变量
                        boldState = false
                        italicState = false
                        underlineState = false
                        unorderedListState = false
                        orderedListState = false
                        heading1State = false
                        heading2State = false
                        normalTextState = false

                    } catch (e: Exception) {
                        // 备用方案
                        println("清除格式时出错: ${e.message}")
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatClear,
                    contentDescription = "清除格式",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 图片插入按钮
            IconButton(
                onClick = onImageClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.Image,
                    contentDescription = "插入图片",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 待办事项按钮
            IconButton(
                onClick = onAddTodo,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(R.drawable.outline_check_box_24),
                    contentDescription = "添加待办",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}