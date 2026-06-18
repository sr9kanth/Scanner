package com.cleanguard.ai.presentation.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.domain.repository.AppScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class AppFilter { ALL, HIGH_RISK, SUSPICIOUS, ACCESSIBILITY, OVERLAY }

data class AppListUiState(
    val apps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val filter: AppFilter = AppFilter.ALL,
    val isLoading: Boolean = true
)

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appScanRepository: AppScanRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(AppFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AppListUiState> = combine(
        _filter.flatMapLatest { filter ->
            when (filter) {
                AppFilter.ALL -> appScanRepository.getAllApps()
                AppFilter.HIGH_RISK -> appScanRepository.getHighRiskApps()
                AppFilter.SUSPICIOUS -> appScanRepository.getAllApps().map { list ->
                    list.filter { it.riskLevel == RiskLevel.SUSPICIOUS }
                }
                AppFilter.ACCESSIBILITY -> appScanRepository.getAccessibilityApps()
                AppFilter.OVERLAY -> appScanRepository.getOverlayApps()
            }
        },
        _searchQuery
    ) { apps, query ->
        val filtered = if (query.isBlank()) apps
        else apps.filter {
            it.appName.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }
        AppListUiState(apps = filtered, searchQuery = query, filter = _filter.value, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppListUiState())

    fun setFilter(filter: AppFilter) { _filter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
}
