package com.v.v_notes

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.v.v_notes.components.Menu
import com.v.v_notes.ui.theme.MyNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
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
    var menuItems = mapOf(
        1 to "记事",
        2 to "提醒",
        3 to "已归档",
        4 to "设置"
    )

    Column(

    ) {
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
                    contentDescription = null
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
                            text = stringResource(R.string.search_box)
                        )

                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_splitscreen_24),
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
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

                    }

                    2 -> {

                    }

                    3 -> {

                    }

                    4 -> {

                    }
                    5 ->{
                        Toast.makeText(context,"text1", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyNotesApp()
}