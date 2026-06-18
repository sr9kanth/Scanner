package com.cleanguard.ai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.cleanguard.ai.worker.ScheduledScanWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class CleanGuardApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicScan()
    }

    private fun schedulePeriodicScan() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val scanRequest = PeriodicWorkRequestBuilder<ScheduledScanWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ScheduledScanWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            scanRequest
        )
    }
}
