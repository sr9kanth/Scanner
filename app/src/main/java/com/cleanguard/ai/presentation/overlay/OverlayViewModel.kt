package com.cleanguard.ai.presentation.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.repository.AppScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val appScanRepository: AppScanRepository
) : ViewModel() {
    val overlayApps: StateFlow<List<AppInfo>> = appScanRepository
        .getOverlayApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
