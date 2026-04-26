package com.v.v_notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.v.v_notes.R
import com.v.v_notes.ui.theme.MyNotesTheme

@Composable
fun Menu(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onDismissRequest: () -> Unit = {},
    selectedItem: Int = 1,
    onItemSelected: (Int) -> Unit = {},
    showOnlyAlertAndSetting: Boolean = false  // 新增：是否只显示提醒和设置
) {
    Box(
        modifier = modifier
    ) {
        DropdownMenu(
            modifier = Modifier,
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            // 如果不是只显示提醒和设置，则显示所有菜单项
            if (!showOnlyAlertAndSetting) {
                // 1. Keep
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 5.dp)
                        .background(
                            if (selectedItem == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )

                ) {
                    DropdownMenuItem(
                        text = { Text(
                            stringResource(R.string.keep),
                            color = if (selectedItem == 1) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onBackground
                        ) },
                        onClick = {
                            onItemSelected(1)
                            onDismissRequest()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_lightbulb_24),
                                tint = if (selectedItem == 1) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onBackground,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

//                // 2. Alert
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(all = 5.dp)
//                        .background(
//                            if (selectedItem == 2) MaterialTheme.colorScheme.primary else Color.Transparent,
//                            shape = RoundedCornerShape(10.dp)
//                        )
//
//                ) {
//                    DropdownMenuItem(
//                        text = { Text(
//                            stringResource(R.string.alert),
//                            color = if (selectedItem == 2) MaterialTheme.colorScheme.onPrimary
//                            else MaterialTheme.colorScheme.onBackground
//                        ) },
//                        onClick = {
//                            onItemSelected(2)
//                            onDismissRequest()
//                        },
//                        leadingIcon = {
//                            Icon(
//                                painter = painterResource(R.drawable.baseline_notifications_none_24),
//                                tint = if (selectedItem == 2) MaterialTheme.colorScheme.onPrimary
//                                else MaterialTheme.colorScheme.onBackground,
//                                contentDescription = null
//                            )
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }

                // 3. Archive
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 5.dp)
                        .background(
                            if (selectedItem == 3) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )

                ) {
                    DropdownMenuItem(
                        text = { Text(
                            stringResource(R.string.archive),
                            color = if (selectedItem == 3) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onBackground
                        ) },
                        onClick = {
                            onItemSelected(3)
                            onDismissRequest()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_archive_24),
                                tint = if (selectedItem == 3) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onBackground,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 4. Trash
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 5.dp)
                        .background(
                            if (selectedItem == 4) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )

                ) {
                    DropdownMenuItem(
                        text = { Text(
                            stringResource(R.string.trash),
                            color = if (selectedItem == 4) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onBackground
                        ) },
                        onClick = {
                            onItemSelected(4)
                            onDismissRequest()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_delete_forever_24),
                                tint = if (selectedItem == 4) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onBackground,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
//            else {
//                // 如果只显示提醒和设置，则只显示这两项
//                // 1. Alert
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(all = 5.dp)
//                        .background(
//                            if (selectedItem == 2) MaterialTheme.colorScheme.primary else Color.Transparent,
//                            shape = RoundedCornerShape(10.dp)
//                        )
//
//                ) {
//                    DropdownMenuItem(
//                        text = { Text(
//                            stringResource(R.string.alert),
//                            color = if (selectedItem == 2) MaterialTheme.colorScheme.onPrimary
//                            else MaterialTheme.colorScheme.onBackground
//                        ) },
//                        onClick = {
//                            onItemSelected(2)
//                            onDismissRequest()
//                        },
//                        leadingIcon = {
//                            Icon(
//                                painter = painterResource(R.drawable.baseline_notifications_none_24),
//                                tint = if (selectedItem == 2) MaterialTheme.colorScheme.onPrimary
//                                else MaterialTheme.colorScheme.onBackground,
//                                contentDescription = null
//                            )
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//            }

            // 5. Setting (总是显示，不添加选中框)
            DropdownMenuItem(
                modifier = Modifier
                    .padding(all = 5.dp),
                text = { Text(stringResource(R.string.setting)) },
                onClick = {
                    onItemSelected(5)
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_settings_24),
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuPreview() {
    MyNotesTheme {
        Column {
            var selectedItem by remember { mutableIntStateOf(2) }
            var expanded by remember { mutableStateOf(true) }

            Menu(
                expanded = expanded,
                selectedItem = selectedItem,
                onDismissRequest = { expanded = false },
                onItemSelected = { index ->
                    selectedItem = index
                }
            )
        }
    }
}