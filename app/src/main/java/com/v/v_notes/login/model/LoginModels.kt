package com.v.v_notes.login.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String = "",
    val password: String = ""
)

@Serializable
data class LoginResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: LoginData? = null
)

@Serializable
data class LoginData(
    val token: String = "",
    val userId: Long = 0,
    val username: String = "",
    val email: String = ""
)