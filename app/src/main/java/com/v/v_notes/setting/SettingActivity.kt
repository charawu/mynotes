package com.v.v_notes.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.v.v_notes.control.SettingsManager
import com.v.v_notes.ui.theme.MyNotesTheme

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SettingActivityScreen(
                        onBackClick = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingActivityScreen(
    onBackClick: () -> Unit
) {

    var checked by remember { mutableStateOf(false) }

    checked = SettingsManager.getBoolean("fixed_menu", false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp)
            .statusBarsPadding()
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


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
        ) {

            Row() {
                Icon(
                    modifier = Modifier
                        .weight(0.2f)
                        .align(Alignment.CenterVertically),
                    painter = painterResource(R.drawable.outline_horizontal_split_24),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = stringResource(R.string.setting_fixed_menu)
                    )

                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        text = stringResource(R.string.setting_fixed_menu1)
                    )
                }



                Switch(
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(0.2f)
                        .align(Alignment.CenterVertically),
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        SettingsManager.putBoolean("fixed_menu", checked)
                    },
                    thumbContent = if (checked) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.outline_done_24),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        {
                            Icon(
                                painter = painterResource(R.drawable.outline_close_24),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )
            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun SettingActivityScreenPreview() {
    MyNotesTheme {
        SettingActivityScreen(
            onBackClick = {}
        )
    }
}