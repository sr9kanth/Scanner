package com.cleanguard.ai.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.NotificationStats
import com.cleanguard.ai.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    val allStats: StateFlow<List<NotificationStats>> = notificationRepository
        .getAllStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spammyApps: StateFlow<List<NotificationStats>> = notificationRepository
        .getSpammyApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
