package com.v.v_notes.components

import android.net.Uri
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.key

@Composable
fun EnhancedImagePreviewGrid(
    modifier: Modifier = Modifier,
    key: Int = 0,
    images: List<Uri>,
    onRemoveImage: (Int) -> Unit,
    onAddImage: () -> Unit,
    onEditImage: (Int, Uri) -> Unit
) {
    key(key) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(images, key = { index, uri ->
                    "image_${uri.toString()}_${key}_${index}"
                }) { index, uri ->
                    Box(
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable {
                                    // 点击图片时打开编辑对话框
                                    onEditImage(index, uri)
                                },
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box {
                                key("image_preview_${uri.toString()}_${key}_${index}") {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "插入的图片",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        //删除按钮
                        IconButton(
                            onClick = { onRemoveImage(index) },
                            modifier = Modifier
                                .size(28.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "删除图片",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                //添加图按钮
                item(key = "add_image_button_$key") {
                    Card(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { onAddImage() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加图片",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "添加图片",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}