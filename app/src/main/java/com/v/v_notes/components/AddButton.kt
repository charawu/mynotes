package com.v.v_notes.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.v.v_notes.R

@Composable
fun AddButton(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    onToggle: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {

    val rotationAngle by animateFloatAsState(
        targetValue = if (isActive) 45f else 0f,
        animationSpec = tween(durationMillis = 300), // 300毫秒动画
        label = "rotateAnimation"
    )

    Box(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.primary
            )
    ) {
        IconButton(
            onClick = {
                onClick()
                onToggle(!isActive)
            },
            modifier = Modifier
                .size(60.dp)
        ) {

            Icon(
                painter = painterResource(id = R.drawable.baseline_add_24),
                contentDescription = if (isActive) "close" else "add",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(35.dp)
                    .rotate(rotationAngle)
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddButtonPreview() {
    var isActive by remember { mutableStateOf(false) }

    Box(

    ) {
        AddButton(
            modifier = Modifier
                .size(48.dp),
            isActive = isActive,
            onToggle = { newState ->
                isActive = newState
            }
        )
    }
}