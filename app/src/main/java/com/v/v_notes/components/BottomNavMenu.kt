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
        //主界面
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

        //Archive
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

        //Trash
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
        //keep
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

        //archive
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

        //trash
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

        //设置选项
        if (showAlertAndSetting) {
            //alert
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

            //setting
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


@Preview(showBackground = true)
@Composable
fun BottomNavMenuPreview() {
    MyNotesTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray)
            )

            BottomNavMenu(
                selectedItem = 3,
                onItemSelected = { }
            )
        }
    }
}