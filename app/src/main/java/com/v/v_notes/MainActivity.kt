package com.v.v_notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.v.v_notes.components.AddButton
import com.v.v_notes.components.AddButtonList

import com.v.v_notes.components.Menu
import com.v.v_notes.setting.SettingActivity
import com.v.v_notes.ui.theme.MyNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    MyNotesApp()
                }
            }
        }


    }
}

@PreviewScreenSizes
@Composable
fun MyNotesApp() {

    val context = LocalContext.current

    var isMenuExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(1) }


    var isActive by remember { mutableStateOf(false) }

    Column() {
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    isMenuExpanded = true
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_menu_24),
                    contentDescription = "menu"
                )
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(40.dp)
            ) {

                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = {

                            }
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .padding(1.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .weight(0.5f),
                            text = stringResource(R.string.search_box),
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                painter = painterResource(R.drawable.baseline_splitscreen_24),
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                painter = painterResource(R.drawable.baseline_import_export_24),
                                contentDescription = null
                            )
                        }
                    }

                }
            }


            IconButton(
                modifier = Modifier,
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_account_box),
                    contentDescription = null
                )
            }
        }

        Menu(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart),
            expanded = isMenuExpanded,
            onDismissRequest = {
                isMenuExpanded = false
            },
            onItemSelected = { itemId ->
                selectedItem = itemId

                when (itemId) {
                    1 -> {
                        selectedItem = 1
                    }

                    2 -> {
                        selectedItem = 2
                    }

                    3 -> {
                        selectedItem = 3
                    }

                    4 -> {
                        selectedItem = 4
                    }

                    5 -> {
                        selectedItem = 5
                        val intent = Intent(context, SettingActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            },
            selectedItem = selectedItem
        )


        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                // 放置在按钮上方
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 30.dp, bottom = 105.dp) // 调整位置
                ) {
                    AddButtonList(expanded = isActive)
                }

                // 添加按钮
                AddButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(40.dp),
                    isActive = isActive,
                    onToggle = { isActive = it }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyNotesTheme(
    ){
        MyNotesApp()
    }
}