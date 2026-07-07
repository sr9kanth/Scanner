package com.cleanguard.ai.data.remote.api

import com.cleanguard.ai.data.remote.dto.DeepSeekRequest
import com.cleanguard.ai.data.remote.dto.DeepSeekResponse
import retrofit2.http.*

interface DeepSeekApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse
}
