package com.v.v_notes.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.v.v_notes.R
import com.v.v_notes.ui.theme.MyNotesTheme

@Composable
fun AddButtonList(
    onPhotoClick: () -> Unit = {},
    onDrawClick: () -> Unit = {},
    onCheckClick: () -> Unit = {},
    onTextClick: () -> Unit = {},
    expanded: Boolean = false
) {

    val animationDelay = 50

    Column() {
        val modifier: Modifier = Modifier
            .clip(shape = RoundedCornerShape(18.dp))
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(end = 5.dp)


        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = animationDelay * 3,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.5f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = animationDelay * 3,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            )
        ) {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPhotoClick,
                        modifier = Modifier
                    ) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onPrimary,
                            painter = painterResource(R.drawable.outline_insert_photo_24),
                            contentDescription = null
                        )
                    }
                    Text(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = modifier,
                        text = stringResource(R.string.add_menu_photo)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = animationDelay * 2,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.5f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = animationDelay * 2,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            )
        ) {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDrawClick,
                        modifier = Modifier
                    ) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onPrimary,
                            painter = painterResource(R.drawable.outline_draw_24),
                            contentDescription = null
                        )
                    }
                    Text(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = modifier,
                        text = stringResource(R.string.add_menu_draw)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = animationDelay * 1,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.5f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = animationDelay * 1,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            )
        ) {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCheckClick,
                        modifier = Modifier
                    ) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onPrimary,
                            painter = painterResource(R.drawable.outline_check_box_24),
                            contentDescription = null
                        )
                    }
                    Text(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = modifier,
                        text = stringResource(R.string.add_menu_todo)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 0,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.5f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 0,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            )
        ) {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onTextClick,
                        modifier = Modifier
                    ) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onPrimary,
                            painter = painterResource(R.drawable.outline_text_fields_24),
                            contentDescription = null
                        )
                    }
                    Text(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = modifier,
                        text = stringResource(R.string.add_menu_text)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddButtonListPreview(){
    MyNotesTheme() {
        AddButtonList(
            onPhotoClick = {},
            onDrawClick = {},
            onCheckClick = {},
            onTextClick = {},
            expanded = true
        )
    }
}