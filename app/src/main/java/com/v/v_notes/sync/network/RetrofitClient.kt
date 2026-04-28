package com.v.v_notes.sync.network

import com.v.v_notes.sync.data.SyncNote
import com.v.v_notes.sync.data.SyncRequest
import com.v.v_notes.sync.data.SyncResponse
import com.v.v_notes.sync.data.SyncTodoItem
import kotlinx.coroutines.delay
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SyncApiService {
    @POST("api/notes/sync")
    suspend fun syncNotes(
        @Header("Authorization") token: String = "",
        @Body request: SyncRequest
    ): Response<SyncResponse>
}

object RetrofitClient {
    //使用模拟模式
    private const val USE_MOCK_MODE = true

    //模拟API服务
    private val mockApiService = object : SyncApiService {
        override suspend fun syncNotes(token: String, request: SyncRequest): Response<SyncResponse> {
            //模拟延迟
            delay(1000)

            println("[模拟服务器] 收到同步请求")
            println("  用户ID: ${request.userId}")
            println("  设备ID: ${request.deviceId}")
            println("  最后同步时间: ${request.lastSyncTime}")
            println("  上传数量: ${request.notes.size}")

            //模拟服务器处理
            val mockResponse = SyncResponse(
                success = true,
                message = "同步成功（模拟服务器）",
                newLastSyncTime = System.currentTimeMillis(),
                notes = getMockServerNotes()
            )

            println("[模拟服务器] 返回响应")
            println("  成功: ${mockResponse.success}")
            println("  消息: ${mockResponse.message}")
            println("  返回数量: ${mockResponse.notes.size}")

            return Response.success(mockResponse)
        }
    }

    val syncApiService: SyncApiService
        get() = mockApiService

    private fun getMockServerNotes(): List<SyncNote> {
        return listOf(
            SyncNote(
                id = "server_note_001",
                title = "来自服务器同步到本地的笔记示例,用于测试同步服务",
                content = "同步实力",
                imageUris = emptyList(),
                todoItems = listOf(
                    SyncTodoItem("todo1", "待办1", false),
                    SyncTodoItem("todo2", "待办2", true)
                ),
                isArchived = false,
                isDeleted = false,
                isPinned = true,
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis() - 43200000
            )
        )
    }
}