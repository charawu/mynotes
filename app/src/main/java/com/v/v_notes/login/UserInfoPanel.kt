package com.v.v_notes.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.v.v_notes.login.auth.AuthManager

@Composable
fun UserInfoPanel(
    authManager: AuthManager,
    onLoginClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    val loginState = authManager.loginState.collectAsState().value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (loginState.isLoggedIn) {
                //已登录状态
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "用户",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = loginState.username,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "用户ID: ${loginState.userId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    //同步
                    IconButton(
                        onClick = onSyncClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "同步")
                    }

                    //退出登录
                    IconButton(
                        onClick = {
                            authManager.logout()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "退出登录")
                    }
                }
            } else {
                //未登录
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "未登录",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "未登录",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "登录以启用云同步功能",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    //登录按钮
                    Button(
                        onClick = onLoginClick
                    ) {
                        Text("登录")
                    }
                }
            }
        }
    }
}