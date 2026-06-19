package com.cleanguard.ai.presentation.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.domain.repository.AppScanRepository
import com.cleanguard.ai.domain.usecase.ScanAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

enum class AppFilter { ALL, HIGH_RISK, SUSPICIOUS, WARNING, SAFE, ACCESSIBILITY, OVERLAY }

data class AppListUiState(
    val apps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val filter: AppFilter = AppFilter.ALL,
    val isLoading: Boolean = true,
    val totalCount: Int = 0
)

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appScanRepository: AppScanRepository,
    private val scanAppsUseCase: ScanAppsUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(AppFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isScanning = MutableStateFlow(true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AppListUiState> = combine(
        _filter.flatMapLatest { filter ->
            when (filter) {
                AppFilter.ALL -> appScanRepository.getAllApps()
                AppFilter.HIGH_RISK -> appScanRepository.getHighRiskApps()
                AppFilter.SUSPICIOUS -> appScanRepository.getAllApps().map { list ->
                    list.filter { it.riskLevel == RiskLevel.SUSPICIOUS }
                }
                AppFilter.WARNING -> appScanRepository.getAllApps().map { list ->
                    list.filter { it.riskLevel == RiskLevel.REVIEW }
                }
                AppFilter.SAFE -> appScanRepository.getAllApps().map { list ->
                    list.filter { it.riskLevel == RiskLevel.SAFE }
                }
                AppFilter.ACCESSIBILITY -> appScanRepository.getAccessibilityApps()
                AppFilter.OVERLAY -> appScanRepository.getOverlayApps()
            }
        },
        appScanRepository.getAllApps(),
        _searchQuery,
        _isScanning
    ) { apps, allApps, query, scanning ->
        val filtered = if (query.isBlank()) apps
        else apps.filter {
            it.appName.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }
        AppListUiState(
            apps = filtered,
            searchQuery = query,
            filter = _filter.value,
            isLoading = scanning,
            totalCount = allApps.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppListUiState())

    init {
        // Scan only if DB is empty (Dashboard auto-scans on first open; this handles direct navigation)
        viewModelScope.launch {
            val hasApps = appScanRepository.getAllApps().first().isNotEmpty()
            if (!hasApps) {
                _isScanning.value = true
                scanAppsUseCase()
            }
            _isScanning.value = false
        }
    }

    fun setFilter(filter: AppFilter) { _filter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun rescan() {
        viewModelScope.launch {
            _isScanning.value = true
            scanAppsUseCase()
            _isScanning.value = false
        }
    }
}
