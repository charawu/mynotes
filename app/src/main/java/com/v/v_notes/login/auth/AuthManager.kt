package com.v.v_notes.login.auth

import android.content.Context
import android.util.Log
import com.v.v_notes.login.model.LoginRequest
import com.v.v_notes.login.model.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class AuthManager(private val context: Context) {

    companion object {
        private const val TAG = "AuthManager"
        private const val BASE_URL = "http://10.0.2.2:8080"  //服务器地址,目前为测试地址
    }

    //添加登录状态流
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        //从SharedPreferences加载状态
        _loginState.value = getLoginStateFromPrefs()
    }

    //从SharedPreferences读取登录状态
    private fun getLoginStateFromPrefs(): LoginState {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return LoginState(
            isLoggedIn = prefs.getBoolean("is_logged_in", false),
            token = prefs.getString("auth_token", "") ?: "",
            userId = prefs.getLong("user_id", 0),
            username = prefs.getString("username", "") ?: ""
        )
    }

   //模拟登录,测试使用
    suspend fun mockLogin(username: String, password: String): LoginResult {
        return withContext(Dispatchers.IO) {
            try {
                //延迟
                delay(1000)

                //验证
                val isValid = username.isNotBlank() && password.isNotBlank()

                if (isValid) {
                    val mockToken = "mock_token_${System.currentTimeMillis()}_${username}"
                    val mockUserId = (1000..9999).random().toLong()

                    //保存登录状态
                    saveLoginState(
                        isLoggedIn = true,
                        token = mockToken,
                        userId = mockUserId,
                        username = username
                    )

                    Log.d(TAG, "模拟登录成功 $username, Token: $mockToken")

                    LoginResult(
                        success = true,
                        message = "模拟登录成功",
                        token = mockToken,
                        userId = mockUserId
                    )
                } else {
                    LoginResult(
                        success = false,
                        message = "用户或密码不能为空"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "登录异常", e)
                LoginResult(
                    success = false,
                    message = "登录失败: ${e.message}"
                )
            }
        }
    }

    //对接服务器后的登录逻辑
    suspend fun realLogin(username: String, password: String): LoginResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/api/users/login")
                val connection = url.openConnection() as HttpsURLConnection

                connection.apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")

                    // 构建请求体
                    val requestBody = Json.encodeToString(LoginRequest(username, password))
                    outputStream.use { os ->
                        os.write(requestBody.toByteArray(Charsets.UTF_8))
                    }

                    // 检查响应
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = inputStream.bufferedReader().use { it.readText() }
                        val loginResponse = Json.decodeFromString<LoginResponse>(response)

                        if (loginResponse.success) {
                            loginResponse.data?.let { data ->
                                saveLoginState(
                                    isLoggedIn = true,
                                    token = data.token,
                                    userId = data.userId,
                                    username = data.username
                                )

                                Log.d(TAG, "登录成功: ${data.username}")

                                return@withContext LoginResult(
                                    success = true,
                                    message = "登录成功",
                                    token = data.token,
                                    userId = data.userId
                                )
                            }
                        }
                    }
                }

                LoginResult(
                    success = false,
                    message = "登录失败 HTTP ${connection.responseCode}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "登录异常", e)
                LoginResult(
                    success = false,
                    message = "登录失败 ${e.message}"
                )
            }
        }
    }

   //状态保存
    private fun saveLoginState(
        isLoggedIn: Boolean,
        token: String = "",
        userId: Long = 0,
        username: String = ""
    ) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_logged_in", isLoggedIn)
            putString("auth_token", token)
            putLong("user_id", userId)
            putString("username", username)
            apply()
        }
        // 更新 StateFlow
        _loginState.value = LoginState(isLoggedIn, token, userId, username)
    }

    //获取状态
    fun getLoginState(): LoginState {
        return getLoginStateFromPrefs()
    }

   //退出登录
    fun logout() {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            putString("auth_token", "")
            putLong("user_id", 0)
            putString("username", "")
            apply()
        }
        // 更新 StateFlow
        _loginState.value = LoginState()
        Log.d(TAG, "用户登出")
    }


    fun getAuthToken(): String {
        return getLoginState().token
    }


    fun getUserId(): Long {
        return getLoginState().userId
    }
}

data class LoginResult(
    val success: Boolean,
    val message: String = "",
    val token: String = "",
    val userId: Long = 0
)

data class LoginState(
    val isLoggedIn: Boolean = false,
    val token: String = "",
    val userId: Long = 0,
    val username: String = ""
)