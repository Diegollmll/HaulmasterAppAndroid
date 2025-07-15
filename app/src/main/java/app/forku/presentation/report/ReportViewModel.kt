package app.forku.presentation.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.report.ReportRepository
import app.forku.domain.model.report.*
import app.forku.domain.repository.report.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "ReportViewModel"
    }

    init {
        loadFilterOptions()
    }

    fun selectReportType(reportType: ReportType) {
        Log.d(TAG, "Selecting report type: ${reportType.displayName}")
        
        // Clear any existing data and errors when changing report type
        val currentFilter = _uiState.value.currentFilter
        val newFilter = if (reportType == ReportType.CHECKLISTS) {
            // For checklists, reset includeDetails to null to force user selection
            currentFilter.copy(includeDetails = null)
        } else {
            // For other reports, clear includeDetails since it's not relevant
            currentFilter.copy(includeDetails = null)
        }
        
        _uiState.value = _uiState.value.copy(
            selectedReportType = reportType,
            currentFilter = newFilter,
            reportData = emptyList(),
            error = null,
            exportError = null,
            lastGeneratedAt = null,
            loadingProgress = LoadingProgress()
        )
        
        // Load filter options for the new report type
        loadFilterOptions()
    }

    fun updateFilter(filter: ReportFilter) {
        Log.d(TAG, "Updating filter: ${filter.getFilterSummary()}")
        _uiState.value = _uiState.value.copy(currentFilter = filter)
    }

    fun generateReport() {
        val currentState = _uiState.value
        val reportType = currentState.selectedReportType ?: return
        
        Log.d(TAG, "🎯 Generating report for: ${reportType.displayName}")
        
        // Validation for CHECKLISTS - require detail level selection
        if (reportType == ReportType.CHECKLISTS && currentState.currentFilter.includeDetails == null) {
            Log.w(TAG, "⚠️ Cannot generate checklist report without detail level selection")
            _uiState.value = currentState.copy(
                error = "Por favor selecciona un nivel de detalle (Con Detalles o Sin Detalles) antes de generar el reporte."
            )
            return
        }
        
        // Clear any previous errors
        _uiState.value = currentState.copy(
            isLoading = true, 
            error = null,
            exportError = null,
            loadingProgress = LoadingProgress(
                isActive = true,
                currentStep = "Iniciando generación del reporte...",
                completedSteps = 0,
                totalSteps = 3
            )
        )
        
        viewModelScope.launch {
            try {
                // Step 1: Fetch data
                updateLoadingProgress("Fetching data from server...", 1)
                
                val data = when (reportType) {
                    ReportType.VEHICLES -> reportRepository.getVehiclesReport(currentState.currentFilter)
                    ReportType.CHECKLISTS -> reportRepository.getChecklistsReport(currentState.currentFilter)
                    ReportType.INCIDENTS -> reportRepository.getIncidentsReport(currentState.currentFilter)
                    ReportType.CERTIFICATIONS -> reportRepository.getCertificationsReport(currentState.currentFilter)
                }
                
                // Step 2: Process data
                updateLoadingProgress("Processing report data...", 2)
                
                Log.d(TAG, "📊 Data received from repository: ${data.size} items")
                Log.d(TAG, "📊 Data type: ${data.javaClass.simpleName}")
                if (data.isNotEmpty()) {
                    Log.d(TAG, "📊 First item type: ${data.first().javaClass.simpleName}")
                }
                
                // Step 3: Finalize
                updateLoadingProgress("Finalizing report...", 3)
                
                // Small delay to show final step
                kotlinx.coroutines.delay(500)
                
                val newState = _uiState.value.copy(
                    reportData = data,
                    isLoading = false,
                    lastGeneratedAt = System.currentTimeMillis(),
                    loadingProgress = LoadingProgress() // Reset progress
                )
                
                _uiState.value = newState
                
                Log.d(TAG, "🎯 State updated - hasData: ${newState.hasData}, reportData.size: ${newState.reportData.size}")
                Log.d(TAG, "Report generated successfully with ${data.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating report", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to generate report: ${e.message}",
                    loadingProgress = LoadingProgress() // Reset progress
                )
            }
        }
    }

    private fun updateLoadingProgress(step: String, completedSteps: Int) {
        val currentProgress = _uiState.value.loadingProgress
        _uiState.value = _uiState.value.copy(
            loadingProgress = currentProgress.copy(
                currentStep = step,
                completedSteps = completedSteps
            )
        )
        Log.d(TAG, "📊 Progress: $step (${completedSteps}/${currentProgress.totalSteps})")
    }

    fun exportReport(format: ExportFormat) {
        val currentState = _uiState.value
        val reportType = currentState.selectedReportType ?: return
        
        Log.d(TAG, "Exporting report as ${format.displayName}")
        
        _uiState.value = currentState.copy(isExporting = true, exportError = null)
        
        viewModelScope.launch {
            try {
                val exportData = reportRepository.exportReport(reportType, currentState.currentFilter, format)
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportData = exportData,
                    exportFormat = format,
                    exportFileName = generateFileName(reportType, format)
                )
                
                Log.d(TAG, "Report exported successfully (${exportData.size} bytes)")
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting report", e)
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = "Failed to export report: ${e.message}"
                )
            }
        }
    }

    fun clearExportData() {
        _uiState.value = _uiState.value.copy(
            exportData = null,
            exportFormat = null,
            exportFileName = null,
            exportError = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, exportError = null)
    }

    private fun loadFilterOptions() {
        val reportType = _uiState.value.selectedReportType ?: return
        
        viewModelScope.launch {
            try {
                val options = reportRepository.getFilterOptions(reportType)
                _uiState.value = _uiState.value.copy(filterOptions = options)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading filter options", e)
            }
        }
    }

    private fun generateFileName(reportType: ReportType, format: ExportFormat): String {
        val timestamp = System.currentTimeMillis()
        val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(timestamp)
        val reportName = reportType.name.lowercase()
        
        // Add detail level for checklist reports
        val detailSuffix = if (reportType == ReportType.CHECKLISTS) {
            val includeDetails = _uiState.value.currentFilter.includeDetails
            when (includeDetails) {
                true -> "_con_detalles"
                false -> "_sin_detalles"
                null -> "_unknown_detail_level"
            }
        } else {
            ""
        }
        
        return "forku_${reportName}_report${detailSuffix}_${dateStr}.${format.fileExtension}"
    }
}

/**
 * UI State for the Report screen
 */
data class ReportUiState(
    val selectedReportType: ReportType? = null,
    val currentFilter: ReportFilter = ReportFilter(),
    val filterOptions: ReportFilterOptions = ReportFilterOptions(
        businesses = emptyList(),
        sites = emptyList(),
        users = emptyList(),
        vehicles = emptyList(),
        statuses = emptyList(),
        types = emptyList(),
        severities = emptyList(),
        dateRanges = emptyList(),
        detailOptions = emptyList()
    ),
    val reportData: List<Any> = emptyList(),
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null,
    val exportError: String? = null,
    val lastGeneratedAt: Long? = null,
    val exportData: ByteArray? = null,
    val exportFormat: ExportFormat? = null,
    val exportFileName: String? = null,
    val loadingProgress: LoadingProgress = LoadingProgress()
) {
    val hasData: Boolean get() = reportData.isNotEmpty()
    val hasFilters: Boolean get() = currentFilter.hasFilters()
}

/**
 * Loading progress information for detailed user feedback
 */
data class LoadingProgress(
    val currentStep: String = "",
    val totalSteps: Int = 0,
    val completedSteps: Int = 0,
    val startTime: Long = 0L,
    val isActive: Boolean = false
) {
    val progressPercentage: Float get() = if (totalSteps > 0) (completedSteps.toFloat() / totalSteps.toFloat()) else 0f
    val elapsedTime: Long get() = if (startTime > 0) System.currentTimeMillis() - startTime else 0L
    val estimatedTimeRemaining: Long get() {
        if (completedSteps == 0 || totalSteps == 0) return 0L
        val avgTimePerStep = elapsedTime / completedSteps
        return avgTimePerStep * (totalSteps - completedSteps)
    }
} 