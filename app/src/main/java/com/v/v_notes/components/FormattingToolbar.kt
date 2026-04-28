package com.v.v_notes.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
    onAddTodo: () -> Unit,
    onDrawClick: () -> Unit
) {

    var boldState by remember { mutableStateOf(false) }
    var italicState by remember { mutableStateOf(false) }
    var underlineState by remember { mutableStateOf(false) }
    var unorderedListState by remember { mutableStateOf(false) }
    var orderedListState by remember { mutableStateOf(false) }
    var heading1State by remember { mutableStateOf(false) }
    var heading2State by remember { mutableStateOf(false) }
    var normalTextState by remember { mutableStateOf(false) }

    var scrollState = rememberScrollState()

    //检测选中文本
    LaunchedEffect(editorState.selection) {
        val currentSpanStyle = editorState.currentSpanStyle

        //粗体
        val fontWeightWeight = currentSpanStyle?.fontWeight?.weight ?: 400
        val isBold = fontWeightWeight >= FontWeight.Bold.weight && fontWeightWeight < FontWeight.ExtraBold.weight
        boldState = isBold

        //斜体
        val isItalic = currentSpanStyle?.fontStyle == FontStyle.Italic
        italicState = isItalic

        //下划线
        val isUnderline = currentSpanStyle?.textDecoration?.contains(TextDecoration.Underline) ?: false
        underlineState = isUnderline

        //无序列表
        val isUnorderedList = editorState.isUnorderedList
        unorderedListState = isUnorderedList

        //有序列表
        val isOrderedList = editorState.isOrderedList
        orderedListState = isOrderedList

        //标题样式
        val fontSize = currentSpanStyle?.fontSize
        val isHeading1 = fontSize == 24.sp
        val isHeading2 = fontSize == 20.sp
        val isNormalText = fontSize == 16.sp || fontSize == null || fontSize == 14.sp

        heading1State = isHeading1
        heading2State = isHeading2
        normalTextState = isNormalText
    }

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

    //var currentDefaultStyle by remember { mutableStateOf(SpanStyle()) }

    LaunchedEffect(editorState.selection, boldState, italicState, underlineState) {

        val cursorStyle = editorState.currentSpanStyle

        //currentDefaultStyle = cursorStyle
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .horizontalScroll(scrollState)
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
            //加粗
            IconButton(
                onClick = {
                    val currentSpanStyle = editorState.currentSpanStyle
                    val currentFontWeight = currentSpanStyle?.fontWeight
                    val isCurrentlyBold =
                        (currentFontWeight?.weight ?: 400) >= FontWeight.Bold.weight

                    if (isCurrentlyBold) {

                        editorState.toggleSpanStyle(
                            SpanStyle(fontWeight = FontWeight.Normal)
                        )
                        boldState = false
                    } else {

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
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(boldState))
                )
            }

            //斜体
            IconButton(
                onClick = {
                    val currentSpanStyle = editorState.currentSpanStyle
                    val isCurrentlyItalic = currentSpanStyle?.fontStyle == FontStyle.Italic

                    if (isCurrentlyItalic) {

                        editorState.toggleSpanStyle(
                            SpanStyle(fontStyle = FontStyle.Normal)
                        )
                        italicState = false
                    } else {

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
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(italicState))
                )
            }

            //下划线
            IconButton(
                onClick = {
                    val currentSpanStyle = editorState.currentSpanStyle
                    val hasUnderline = currentSpanStyle?.textDecoration?.contains(TextDecoration.Underline) ?: false

                    if (hasUnderline) {

                        editorState.removeSpanStyle(
                            SpanStyle(textDecoration = TextDecoration.Underline)
                        )
                        underlineState = false
                    } else {

                        editorState.addSpanStyle(
                            SpanStyle(textDecoration = TextDecoration.Underline)
                        )
                        underlineState = true
                    }

                    //难绷
                    val updatedStyle = editorState.currentSpanStyle
                    val hasLineThrough = updatedStyle?.textDecoration?.contains(TextDecoration.LineThrough) ?: false

                    if (hasLineThrough) {
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
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(underlineState))
                )
            }

            //项无序
            IconButton(
                onClick = {
                    editorState.toggleUnorderedList()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(unorderedListState))
                )
            }

            //有序
            IconButton(
                onClick = {
                    editorState.toggleOrderedList()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(orderedListState))
                )
            }

            //1
            IconButton(
                onClick = {
                    editorState.addParagraphStyle(
                        ParagraphStyle(lineHeight = 32.sp)
                    )
                    editorState.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    heading1State = true
                    heading2State = false
                    normalTextState = false
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_looks_one_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(heading1State)),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            //2
            IconButton(
                onClick = {
                    editorState.addParagraphStyle(
                        ParagraphStyle(lineHeight = 28.sp)
                    )

                    editorState.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )

                    heading1State = false
                    heading2State = true
                    normalTextState = false
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_looks_two_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(heading2State)),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            //正
            IconButton(
                onClick = {
                    editorState.addParagraphStyle(ParagraphStyle())

                    editorState.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )

                    heading1State = false
                    heading2State = false
                    normalTextState = true
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_text_format_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .then(Stylemodifier(normalTextState)),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            //清除
            IconButton(
                onClick = {

                    try {
                        resetToPlainText(editorState)

                        boldState = false
                        italicState = false
                        underlineState = false
                        unorderedListState = false
                        orderedListState = false
                        heading1State = false
                        heading2State = false
                        normalTextState = false

                    } catch (e: Exception) {
                        Log.d("清除格式错误", e.message.toString())
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.FormatClear,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            //图片插入
            IconButton(
                onClick = onImageClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            //待办
            IconButton(
                onClick = onAddTodo,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(R.drawable.outline_check_box_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}