package com.v.v_notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.v.v_notes.R
import com.v.v_notes.ui.theme.MyNotesTheme

/**
 * 底部导航菜单组件
 * 用于在ArchiveActivity和TrashActivity底部显示，支持快速切换
 */
@Composable
fun BottomNavMenu(
    selectedItem: Int = 1,
    onItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            //.height(65.dp)
            .background(MaterialTheme.colorScheme.surface),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Keep/Notes (主界面)
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.outline_lightbulb_24),
                    contentDescription = stringResource(R.string.keep),
                    tint = if (selectedItem == 1) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.keep),
                    color = if (selectedItem == 1) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Archive (归档)
        NavigationBarItem(
            selected = selectedItem == 3,
            onClick = { onItemSelected(3) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.outline_archive_24),
                    contentDescription = stringResource(R.string.archived),
                    tint = if (selectedItem == 3) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.archived),
                    color = if (selectedItem == 3) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Trash (回收站)
        NavigationBarItem(
            selected = selectedItem == 4,
            onClick = { onItemSelected(4) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.outline_delete_forever_24),
                    contentDescription = stringResource(R.string.trash),
                    tint = if (selectedItem == 4) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.trash),
                    color = if (selectedItem == 4) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

/**
 * 增强版底部导航菜单组件
 * 可以根据设置选项决定是否包含提醒和设置项
 *
 * @param showAlertAndSetting 是否显示提醒和设置项（从设置中获取的Boolean值）
 * @param selectedItem 当前选中的菜单项
 * @param onItemSelected 菜单项点击回调
 * @param modifier 修饰符
 */
@Composable
fun EnhancedBottomNavMenu(
    showAlertAndSetting: Boolean = false,
    selectedItem: Int = 1,
    onItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Keep/Notes (主界面)
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.outline_lightbulb_24),
                    contentDescription = stringResource(R.string.keep),
                    tint = if (selectedItem == 1) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.keep),
                    color = if (selectedItem == 1) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Archive (归档)
        NavigationBarItem(
            selected = selectedItem == 3,
            onClick = { onItemSelected(3) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.outline_archive_24),
                    contentDescription = stringResource(R.string.archived),
                    tint = if (selectedItem == 3) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.archived),
                    color = if (selectedItem == 3) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Trash (回收站)
        NavigationBarItem(
            selected = selectedItem == 4,
            onClick = { onItemSelected(4) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.outline_delete_forever_24),
                    contentDescription = stringResource(R.string.trash),
                    tint = if (selectedItem == 4) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.trash),
                    color = if (selectedItem == 4) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // 如果设置中开启了显示提醒和设置，则添加这些项
        if (showAlertAndSetting) {
            // Alert (提醒)
            NavigationBarItem(
                selected = selectedItem == 2,
                onClick = { onItemSelected(2) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_notifications_none_24),
                        contentDescription = stringResource(R.string.alert),
                        tint = if (selectedItem == 2) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.alert),
                        color = if (selectedItem == 2) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Setting (设置)
            NavigationBarItem(
                selected = selectedItem == 5,
                onClick = { onItemSelected(5) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_settings_24),
                        contentDescription = stringResource(R.string.setting),
                        tint = if (selectedItem == 5) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.setting),
                        color = if (selectedItem == 5) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

/**
 * 底部导航菜单的预览
 */
@Preview(showBackground = true)
@Composable
fun BottomNavMenuPreview() {
    MyNotesTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 模拟内容区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray)
            )

            // 底部导航菜单
            BottomNavMenu(
                selectedItem = 3, // 模拟当前在归档页面
                onItemSelected = { }
            )
        }
    }
}

/**
 * 增强版底部导航菜单的预览
 */
@Preview(showBackground = true)
@Composable
fun EnhancedBottomNavMenuPreview() {
    MyNotesTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 模拟内容区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray)
            )

            // 增强版底部导航菜单（显示所有项）
            EnhancedBottomNavMenu(
                showAlertAndSetting = true,
                selectedItem = 3, // 模拟当前在归档页面
                onItemSelected = { }
            )
        }
    }
}