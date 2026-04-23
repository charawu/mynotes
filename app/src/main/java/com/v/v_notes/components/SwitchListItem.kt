package com.v.v_notes.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.v.v_notes.R
import com.v.v_notes.ui.theme.MyNotesTheme

/**
 * 简单的开关列表项组件
 * @param modifier 修饰符
 * @param leadingIcon 左侧图标
 * @param title 标题文本
 * @param subtitle 副标题文本，可选
 * @param checked 开关的当前状态
 * @param onCheckedChange 开关状态改变时的回调
 * @param thumbIconChecked 开关打开时的拇指图标
 * @param thumbIconUnchecked 开关关闭时的拇指图标
 */
@Composable
fun SwitchListItem(
    isReboot: Boolean = false,
    modifier: Modifier = Modifier,
    leadingIcon: Painter,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    thumbIconChecked: Painter = painterResource(R.drawable.outline_done_24),
    thumbIconUnchecked: Painter = painterResource(R.drawable.outline_close_24),
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧内容区域
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧图标
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )

                // 文本区域
                Column {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = title
                    )

                    subtitle?.let {
                        Text(
                            style = MaterialTheme.typography.titleSmall,
                            text = it,
                            color = if (isReboot){
                                Color.Red
                            }else{
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // 开关
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = if (checked) {
                    {
                        Icon(
                            painter = thumbIconChecked,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    {
                        Icon(
                            painter = thumbIconUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwitchListItemPreview() {
    MyNotesTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SwitchListItem(
                leadingIcon = painterResource(R.drawable.outline_horizontal_split_24),
                title = "固定菜单栏",
                subtitle = "始终显示侧边菜单",
                checked = true,
                onCheckedChange = {}
            )

            SwitchListItem(
                leadingIcon = painterResource(R.drawable.baseline_arrow_back_24),
                title = "深色模式",
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}