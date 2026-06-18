package com.cleanguard.ai.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cleanguard.ai.domain.usecase.ScanAppsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScheduledScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scanAppsUseCase: ScanAppsUseCase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "cleanguard_scheduled_scan"
    }

    override suspend fun doWork(): Result {
        return scanAppsUseCase().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
