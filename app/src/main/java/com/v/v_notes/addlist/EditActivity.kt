package com.v.v_notes.addlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v.v_notes.R
import com.v.v_notes.ui.theme.MyNotesTheme

class EditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NoteEditScreen(
                        onBackClick = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    onBackClick:() -> Unit = {}
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf(TextFieldValue("")) }

    //待定
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = null
                    )
                }

                Text(
                    text = stringResource(R.string.setting)
                )
            }
            //标题
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text("标题") },
                textStyle = TextStyle(fontSize = 22.sp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            //正文
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                textStyle = TextStyle(fontSize = 18.sp, lineHeight = 28.sp),
                decorationBox = { innerTextField ->
                    if (content.text.isEmpty()) {
                        Text(
                            text = "开始记录...",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    innerTextField()
                }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            EditToolbar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                onAction = { action ->
                    println("工具栏动作: $action")
                }
            )
        }
    }
}

@Composable
fun EditToolbar(
    onAction: (String) -> Unit,
    modifier: Modifier
    )
{
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            //撤销组
            Row {
                IconButton(onClick = { onAction("UNDO") }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_undo_24),
                        contentDescription = null
                    )
                }
                IconButton(onClick = { onAction("REDO") }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_redo_24),
                        contentDescription = null
                    )
                }
            }

            //工具栏折叠组
            Row {
                FilterChip(
                    selected = false,
                    onClick = { onAction("SWITCH_TO_TEXT_STYLE") },
                    label = { Text("A") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.baseline_format_bold_24),
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))


                FilterChip(
                    selected = false,
                    onClick = { onAction("SWITCH_TO_LIST_STYLE") },
                    label = { Text("•") }
                )
            }

            //save
            Row {
                IconButton(onClick = { onAction("SAVE") }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_done_24),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditScreenPreview(){
    MyNotesTheme() {
        NoteEditScreen()
    }
}
