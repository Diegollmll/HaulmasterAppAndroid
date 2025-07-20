package app.forku.presentation.report

import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.core.network.NetworkConnectivityManager
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.BaseScreen
import app.forku.domain.model.report.*
import app.forku.domain.repository.report.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.forku.R
import androidx.compose.ui.res.colorResource

@Composable
fun ReportScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Handle export data
    LaunchedEffect(uiState.exportData) {
        uiState.exportData?.let { data ->
            val fileName = uiState.exportFileName ?: "report.csv"
            Log.d("ReportScreen", "📁 Creating file: $fileName with ${data.size} bytes")
            
            try {
                // Create reports directory if it doesn't exist
                val reportsDir = File(context.getExternalFilesDir(null), "reports")
                if (!reportsDir.exists()) {
                    reportsDir.mkdirs()
                    Log.d("ReportScreen", "📁 Created reports directory: ${reportsDir.absolutePath}")
                }
                
                val file = File(reportsDir, fileName)
                file.writeBytes(data)
                Log.d("ReportScreen", "📁 File saved to: ${file.absolutePath}")
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                Log.d("ReportScreen", "📁 Generated URI: $uri")
                
                // Create direct sharing intent without chooser - more reliable
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = when (uiState.exportFormat) {
                        ExportFormat.CSV -> "text/csv"
                        ExportFormat.JSON -> "application/json"
                        else -> "text/plain"
                    }
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "ForkU Report: ${uiState.selectedReportType?.displayName}")
                    putExtra(Intent.EXTRA_TEXT, "Please find attached the requested report from ForkU.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                
                Log.d("ReportScreen", "📧 Sharing intent created with URI permissions")
                
                // Grant permissions to ALL possible activities that can handle this intent
                // This is more comprehensive than the chooser approach
                val allResolveInfos = context.packageManager.queryIntentActivities(
                    shareIntent, 
                    android.content.pm.PackageManager.MATCH_ALL
                )
                Log.d("ReportScreen", "📧 Found ${allResolveInfos.size} total apps that can handle sharing")
                
                // Grant permissions globally to all apps that can handle the intent
                for (resolveInfo in allResolveInfos) {
                    val packageName = resolveInfo.activityInfo.packageName
                    try {
                        context.grantUriPermission(
                            packageName,
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        Log.d("ReportScreen", "📧 Granted URI permission to: $packageName")
                    } catch (e: Exception) {
                        Log.w("ReportScreen", "📧 Failed to grant permission to: $packageName", e)
                    }
                }
                
                // Grant permission to system UID (this handles system processes)
                try {
                    context.grantUriPermission(
                        "android", // System package
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    Log.d("ReportScreen", "📧 Granted URI permission to Android system")
                } catch (e: Exception) {
                    Log.w("ReportScreen", "📧 Failed to grant permission to system", e)
                }
                
                // Create chooser AFTER granting permissions
                val chooserIntent = Intent.createChooser(shareIntent, "Share Report")
                
                // Grant permissions to chooser itself if possible
                try {
                    val chooserComponent = chooserIntent.resolveActivity(context.packageManager)
                    chooserComponent?.packageName?.let { packageName ->
                        context.grantUriPermission(
                            packageName,
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        Log.d("ReportScreen", "📧 Granted URI permission to chooser: $packageName")
                    }
                } catch (e: Exception) {
                    Log.w("ReportScreen", "📧 Failed to grant permission to chooser", e)
                }
                
                // Set flags on chooser as well
                chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                chooserIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                
                context.startActivity(chooserIntent)
                
            } catch (e: Exception) {
                Log.e("ReportScreen", "📁 Error creating/sharing file", e)
            }
            
            viewModel.clearExportData()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = false,
        topBarTitle = "Reports",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                ReportTypeSelector(
                    selectedType = uiState.selectedReportType,
                    onTypeSelected = { reportType ->
                        viewModel.selectReportType(reportType)
                        // No auto-generate - let user control when to generate report
                    }
                )
            }
            
            if (uiState.selectedReportType != null) {
                // Checklist Detail Level Selector - Only show for CHECKLISTS
                if (uiState.selectedReportType == ReportType.CHECKLISTS) {
                    item {
                        ChecklistDetailLevelSelector(
                            currentDetailLevel = uiState.currentFilter.includeDetails,
                            onDetailLevelChanged = { includeDetails ->
                                viewModel.updateFilter(
                                    uiState.currentFilter.copy(includeDetails = includeDetails)
                                )
                                // No auto-generate - let user control when to generate report
                            }
                        )
                    }
                }
                
                item {
                    ReportFilterSection(
                        filter = uiState.currentFilter,
                        filterOptions = uiState.filterOptions,
                        onFilterChanged = viewModel::updateFilter,
                        reportType = uiState.selectedReportType
                    )
                }
                
                item {
                    ReportActionButtons(
                        onGenerateReport = viewModel::generateReport,
                        onExportCsv = { viewModel.exportReport(ExportFormat.CSV) },
                        onExportJson = { viewModel.exportReport(ExportFormat.JSON) },
                        isLoading = uiState.isLoading,
                        isExporting = uiState.isExporting,
                        hasData = uiState.hasData,
                        loadingProgress = uiState.loadingProgress,
                        reportType = uiState.selectedReportType,
                        currentFilter = uiState.currentFilter
                    )
                }
                
                item {
                    ReportDataDisplaySection(
                        reportType = uiState.selectedReportType!!,
                        data = uiState.reportData,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        lastGeneratedAt = uiState.lastGeneratedAt,
                        onClearError = viewModel::clearError
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportTypeSelector(
    selectedType: ReportType?,
    onTypeSelected: (ReportType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Report Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ReportType.values().forEach { reportType ->
                ReportTypeCard(
                    reportType = reportType,
                    isSelected = selectedType == reportType,
                    onClick = { 
                        onTypeSelected(reportType)
                        // Only auto-generate for non-checklist reports
                        // Checklists will require detail level selection first
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReportTypeCard(
    reportType: ReportType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reportType.icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reportType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = reportType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReportFilterSection(
    filter: ReportFilter,
    filterOptions: ReportFilterOptions,
    onFilterChanged: (ReportFilter) -> Unit,
    reportType: ReportType? = null
) {
    var showFilters by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (filter.hasFilters()) {
                        Badge(
                            containerColor = colorResource(id = R.color.primary_blue)
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(
                        onClick = { showFilters = !showFilters }
                    ) {
                        Text(if (showFilters) "Hide" else "Show")
                        Icon(
                            if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
            }
            
            if (filter.hasFilters()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = filter.getFilterSummary(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (showFilters) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status Filter
                if (filterOptions.statuses.isNotEmpty()) {
                    FilterDropdown(
                        label = "Status",
                        options = filterOptions.statuses,
                        selectedValue = filter.status,
                        onValueChanged = { status ->
                            onFilterChanged(filter.copy(status = status))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Vehicle Filter
                if (filterOptions.vehicles.isNotEmpty()) {
                    FilterDropdown(
                        label = "Vehicle", 
                        options = filterOptions.vehicles,
                        selectedValue = filter.vehicleId,
                        onValueChanged = { vehicleId ->
                            onFilterChanged(filter.copy(vehicleId = vehicleId))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Site Filter
                if (filterOptions.sites.isNotEmpty()) {
                    FilterDropdown(
                        label = "Site",
                        options = filterOptions.sites,
                        selectedValue = filter.siteId,
                        onValueChanged = { siteId ->
                            onFilterChanged(filter.copy(siteId = siteId))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Vehicle Type Filter
                if (filterOptions.types.isNotEmpty()) {
                    FilterDropdown(
                        label = "Vehicle Type",
                        options = filterOptions.types,
                        selectedValue = filter.type,
                        onValueChanged = { type ->
                            onFilterChanged(filter.copy(type = type))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Clear Filters Button
                if (filter.hasFilters()) {
                    OutlinedButton(
                        onClick = { onFilterChanged(ReportFilter()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Filters")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    options: List<FilterOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.value == selectedValue }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedOption?.displayName ?: "All",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    onClick = {
                        onValueChanged(null)
                        expanded = false
                    }
                )
                
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        onClick = {
                            onValueChanged(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportActionButtons(
    onGenerateReport: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    isLoading: Boolean,
    isExporting: Boolean,
    hasData: Boolean,
    loadingProgress: LoadingProgress = LoadingProgress(),
    reportType: ReportType? = null,
    currentFilter: ReportFilter = ReportFilter()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Show detailed progress when loading
            if (isLoading && loadingProgress.isActive) {
                LoadingProgressIndicator(loadingProgress)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Check if generate button should be disabled for checklists without detail level
            val isChecklistWithoutDetailLevel = reportType == ReportType.CHECKLISTS && currentFilter.includeDetails == null
            val canGenerate = !isLoading && !isExporting && !isChecklistWithoutDetailLevel
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onGenerateReport,
                    enabled = canGenerate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.primary_blue),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (hasData) "Refresh" else "Generate",
                        fontWeight = FontWeight.Medium
                    )
                }
                
                OutlinedButton(
                    onClick = onExportCsv,
                    enabled = hasData && !isLoading && !isExporting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(id = R.color.primary_blue)
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (hasData && !isLoading && !isExporting) 
                            colorResource(id = R.color.primary_blue) 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colorResource(id = R.color.primary_blue),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Export CSV",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Show helper text for checklist detail level requirement
            if (isChecklistWithoutDetailLevel) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Selecciona un nivel de detalle arriba para generar el reporte",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ReportDataDisplaySection(
    reportType: ReportType,
    data: List<Any>,
    isLoading: Boolean,
    error: String?,
    lastGeneratedAt: Long?,
    onClearError: () -> Unit
) {
    when {
        isLoading -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generating report...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        error != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onClearError,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
        
        data.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (reportType) {
                                ReportType.CHECKLISTS, ReportType.INCIDENTS -> 
                                    "This report type is not yet implemented.\nTry Vehicles or Certifications reports."
                                else -> "No data available for the selected filters"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        else -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${reportType.displayName} Results",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (lastGeneratedAt != null) {
                            Text(
                                text = "Generated: ${formatTimestamp(lastGeneratedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = "${data.size} items found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Statistics Summary (instead of individual records)
                when (reportType) {
                    ReportType.VEHICLES -> VehicleReportSummary(data as List<VehicleReportItem>)
                    ReportType.CHECKLISTS -> ChecklistReportSummary(data as List<ChecklistReportItem>)
                    ReportType.INCIDENTS -> IncidentReportSummary(data as List<IncidentReportItem>)
                    ReportType.CERTIFICATIONS -> CertificationReportSummary(data as List<CertificationReportItem>)
                }
                
                // Export reminder
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "For detailed records, use the Export CSV button above",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingProgressIndicator(progress: LoadingProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Progress header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Generating Report",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${progress.completedSteps}/${progress.totalSteps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { progress.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Current step
            Text(
                text = progress.currentStep,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Time information
            if (progress.elapsedTime > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Elapsed: ${formatTime(progress.elapsedTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (progress.estimatedTimeRemaining > 0 && progress.completedSteps > 0) {
                        Text(
                            text = "Est. remaining: ${formatTime(progress.estimatedTimeRemaining)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun ChecklistReportSummary(data: List<ChecklistReportItem>) {
    val totalChecklists = data.size
    val passedChecklists = data.count { it.status == "PASSED" }
    val failedChecklists = data.count { it.status == "FAILED" }
    val inProgressChecklists = data.count { it.status == "IN_PROGRESS" }
    val totalPassedItems = data.sumOf { it.passedItems }
    val totalFailedItems = data.sumOf { it.failedItems }
    val totalItems = data.sumOf { it.totalItems }
    val uniqueVehicles = data.map { it.vehicleCodename }.distinct().size
    val uniqueUsers = data.map { it.userName }.distinct().size
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Checklist Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = "Passed",
                    value = passedChecklists.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "Failed",
                    value = failedChecklists.toString(),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "In Progress",
                    value = inProgressChecklists.toString(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Items Overview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "✅ Passed: $totalPassedItems",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "❌ Failed: $totalFailedItems",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📊 Total: $totalItems",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Coverage",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "🚗 Vehicles: $uniqueVehicles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "👤 Operators: $uniqueUsers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val successRate = if (totalChecklists > 0) (passedChecklists * 100 / totalChecklists) else 0
                    Text(
                        text = "📈 Success Rate: $successRate%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleReportSummary(data: List<VehicleReportItem>) {
    val totalVehicles = data.size
    val availableVehicles = data.count { it.status == "AVAILABLE" }
    val inUseVehicles = data.count { it.status == "IN_USE" }
    val outOfServiceVehicles = data.count { it.status == "OUT_OF_SERVICE" }
    val maintenanceVehicles = data.count { it.status == "MAINTENANCE" }
    val uniqueTypes = data.map { it.vehicleType }.distinct().size
    val uniqueSites = data.map { it.siteName }.distinct().size
    val totalSessions = data.sumOf { it.totalSessions }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Vehicle Fleet Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status breakdown - First row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = "Available",
                    value = availableVehicles.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "In Use",
                    value = inUseVehicles.toString(),
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status breakdown - Second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = "Out of Service",
                    value = outOfServiceVehicles.toString(),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "Maintenance",
                    value = maintenanceVehicles.toString(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fleet Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "🚗 Total Vehicles: $totalVehicles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🔧 Vehicle Types: $uniqueTypes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📍 Sites: $uniqueSites",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Usage Statistics",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "📊 Total Sessions: $totalSessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val avgSessions = if (totalVehicles > 0) totalSessions / totalVehicles else 0
                    Text(
                        text = "📈 Avg per Vehicle: $avgSessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val utilizationRate = if (totalVehicles > 0) (inUseVehicles * 100 / totalVehicles) else 0
                    Text(
                        text = "⚡ Utilization: $utilizationRate%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun IncidentReportSummary(data: List<IncidentReportItem>) {
    val totalIncidents = data.size
    val highSeverity = data.count { it.severity == "HIGH" || it.severity == "CRITICAL" }
    val mediumSeverity = data.count { it.severity == "MEDIUM" }
    val lowSeverity = data.count { it.severity == "LOW" }
    val uniqueTypes = data.map { it.type }.distinct().size
    val uniqueVehicles = data.mapNotNull { it.vehicleName }.distinct().size
    val uniqueReporters = data.map { it.reportedBy }.distinct().size
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Incident Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Severity breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = "High/Critical",
                    value = highSeverity.toString(),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "Medium",
                    value = mediumSeverity.toString(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "Low",
                    value = lowSeverity.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Incident Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "📊 Total Incidents: $totalIncidents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🏷️ Incident Types: $uniqueTypes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🚗 Vehicles Involved: $uniqueVehicles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Safety Metrics",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "👤 Reporters: $uniqueReporters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val criticalRate = if (totalIncidents > 0) (highSeverity * 100 / totalIncidents) else 0
                    Text(
                        text = "🚨 Critical Rate: $criticalRate%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📈 Reporting Rate: High",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CertificationReportSummary(data: List<CertificationReportItem>) {
    val totalCertifications = data.size
    val validCertifications = data.count { it.status == "VALID" }
    val expiredCertifications = data.count { it.status == "EXPIRED" }
    val expiringSoon = data.count { it.status == "EXPIRING_SOON" }
    val uniqueTypes = data.map { it.certificationType }.distinct().size
    val uniqueUsers = data.map { it.userName }.distinct().size
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Certification Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = "Valid",
                    value = validCertifications.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "Expiring Soon",
                    value = expiringSoon.toString(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatisticCard(
                    title = "Expired",
                    value = expiredCertifications.toString(),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Certification Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "📊 Total Certifications: $totalCertifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🏷️ Certification Types: $uniqueTypes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "👤 Certified Users: $uniqueUsers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Compliance Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    val complianceRate = if (totalCertifications > 0) (validCertifications * 100 / totalCertifications) else 0
                    Text(
                        text = "✅ Compliance Rate: $complianceRate%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val renewalNeeded = expiredCertifications + expiringSoon
                    Text(
                        text = "🔄 Renewal Needed: $renewalNeeded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📈 Tracking: Active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChecklistDetailLevelSelector(
    currentDetailLevel: Boolean?,
    onDetailLevelChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.report_config_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(id = R.string.report_detail_level),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sin Detalles Option
                DetailOptionCard(
                    title = stringResource(id = R.string.report_no_details),
                    description = stringResource(id = R.string.report_no_details_desc),
                    icon = "📊",
                    isSelected = currentDetailLevel == false,
                    onClick = { onDetailLevelChanged(false) },
                    modifier = Modifier.weight(1f)
                )
                
                // Con Detalles Option  
                DetailOptionCard(
                    title = stringResource(id = R.string.report_with_details),
                    description = stringResource(id = R.string.report_with_details_desc),
                    icon = "📋",
                    isSelected = currentDetailLevel == true,
                    onClick = { onDetailLevelChanged(true) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (currentDetailLevel == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.report_select_detail_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DetailOptionCard(
    title: String,
    description: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                },
                textAlign = TextAlign.Center
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
} 