package com.cleanguard.ai.di

import com.cleanguard.ai.data.remote.api.DeepSeekApiService
import com.cleanguard.ai.data.remote.api.GeminiApiService
import com.cleanguard.ai.data.remote.api.VirusTotalApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()

    @Provides
    @Singleton
    @Named("deepseek")
    fun provideDeepSeekRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()

    @Provides
    @Singleton
    @Named("virustotal")
    fun provideVirusTotalRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.virustotal.com/api/v3/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGeminiApiService(@Named("gemini") retrofit: Retrofit): GeminiApiService =
        retrofit.create(GeminiApiService::class.java)

    @Provides
    @Singleton
    fun provideDeepSeekApiService(@Named("deepseek") retrofit: Retrofit): DeepSeekApiService =
        retrofit.create(DeepSeekApiService::class.java)

    @Provides
    @Singleton
    fun provideVirusTotalApiService(@Named("virustotal") retrofit: Retrofit): VirusTotalApiService =
        retrofit.create(VirusTotalApiService::class.java)
}
