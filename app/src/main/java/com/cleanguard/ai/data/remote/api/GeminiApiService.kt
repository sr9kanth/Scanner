package com.cleanguard.ai.data.remote.api

import com.cleanguard.ai.data.remote.dto.GeminiRequest
import com.cleanguard.ai.data.remote.dto.GeminiResponse
import retrofit2.http.*

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
