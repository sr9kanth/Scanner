package com.cleanguard.ai.di

import com.cleanguard.ai.data.repository.AIRepositoryImpl
import com.cleanguard.ai.data.repository.AppScanRepositoryImpl
import com.cleanguard.ai.data.repository.NotificationRepositoryImpl
import com.cleanguard.ai.data.repository.VirusTotalRepositoryImpl
import com.cleanguard.ai.domain.repository.AIRepository
import com.cleanguard.ai.domain.repository.AppScanRepository
import com.cleanguard.ai.domain.repository.NotificationRepository
import com.cleanguard.ai.domain.repository.VirusTotalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAppScanRepository(impl: AppScanRepositoryImpl): AppScanRepository
    @Binds @Singleton abstract fun bindAIRepository(impl: AIRepositoryImpl): AIRepository
    @Binds @Singleton abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds @Singleton abstract fun bindVirusTotalRepository(impl: VirusTotalRepositoryImpl): VirusTotalRepository
}
