package com.cleanguard.ai.data.remote.api

import com.cleanguard.ai.data.remote.dto.VirusTotalResponse
import retrofit2.http.*

interface VirusTotalApiService {
    @GET("files/{hash}")
    suspend fun getFileReport(
        @Path("hash") hash: String,
        @Header("x-apikey") apiKey: String
    ): VirusTotalResponse
}
