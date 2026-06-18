package com.cleanguard.ai.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.cleanguard.ai.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CleanGuardNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val packageName = sbn.packageName
        if (packageName == "com.cleanguard.ai" || packageName == "com.cleanguard.ai.debug") return

        val appName = try {
            val pm = applicationContext.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }

        scope.launch {
            notificationRepository.recordNotification(packageName, appName)
        }
    }
}
