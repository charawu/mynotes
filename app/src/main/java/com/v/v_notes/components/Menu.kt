package com.v.v_notes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    onDismissRequest:() -> Unit = {},
    selectedItem:Int = 1,
    onItemSelected:(Int) -> Unit = {}
) {

    Box(
        modifier = modifier
    ) {
        DropdownMenu(
            modifier = Modifier,
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            DropdownMenuItem(
                text = {Text(stringResource(R.string.keep))},

                onClick = {
                    onItemSelected(1)
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_lightbulb_24),
                        contentDescription = null
                    )
                }
            )

            DropdownMenuItem(
                text = {Text(stringResource(R.string.alert))},
                onClick = {
                    onItemSelected(2)
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_notifications_none_24),
                        contentDescription = null
                    )
                }
            )

            DropdownMenuItem(
                text = {Text(stringResource(R.string.archive))},
                onClick = {
                    onItemSelected(3)
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_archive_24),
                        contentDescription = null
                    )
                }
            )

            DropdownMenuItem(
                text = {Text(stringResource(R.string.rash))},
                onClick = {
                    onItemSelected(4)
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_delete_forever_24),
                        contentDescription = null
                    )
                }
            )

            DropdownMenuItem(
                text = {Text(stringResource(R.string.setting))},
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
    MyNotesTheme{
        Column() {
            Menu(
                expanded = true,
                selectedItem = 2
            )
        }
    }
}