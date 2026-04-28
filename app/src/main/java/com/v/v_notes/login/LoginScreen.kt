package com.v.v_notes.login

import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.v.v_notes.login.auth.AuthManager
import com.v.v_notes.sync.manager.SyncManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authManager: AuthManager,
    syncManager: SyncManager,
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    var context = LocalContext.current

    Dialog(onDismissRequest = onBackClick) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "账户登录",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "登录以启用云同步功能",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(24.dp))

                //用户名输入
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = ""
                    },
                    label = { Text("用户名") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "用户名")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(12.dp))

                //密码输入
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text("密码") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "密码")
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                //错误
                if (errorMessage.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "错误",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            errorMessage = "请输入用户名和密码"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = ""

                        coroutineScope.launch {
                            val result = authManager.mockLogin(username, password)

                            isLoading = false

                            if (result.success) {
                                onLoginSuccess()
                                onBackClick()
                            } else {
                                errorMessage = result.message
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("登录中...")
                    } else {
                        Icon(Icons.Default.Login, contentDescription = "登录")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("登录")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        Toast.makeText(context,"需要对接数据库以使用注册",Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("注册")
                }

                //测试用户填充
                OutlinedButton(
                    onClick = {
                        username = "test_user_${System.currentTimeMillis() % 1000}"
                        password = "123456"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "测试账户")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("使用测试账户")
                }
            }
        }
    }
}