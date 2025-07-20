package app.forku.data.repository.report

import android.util.Log
import app.forku.domain.repository.report.*
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.certification.CertificationRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.checklist.AnsweredChecklistItemRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import app.forku.domain.model.report.*
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentSeverityLevelEnum
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.NearMissType
import app.forku.domain.model.incident.LoadWeightEnum
import app.forku.core.business.BusinessContextManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val certificationRepository: CertificationRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val answeredChecklistItemRepository: AnsweredChecklistItemRepository,
    private val incidentRepository: IncidentRepository,
    private val businessContextManager: BusinessContextManager,
    private val businessRepository: BusinessRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val vehicleCategoryRepository: VehicleCategoryRepository
) : ReportRepository {

    companion object {
        private const val TAG = "ReportRepository"
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    override suspend fun getVehiclesReport(filter: ReportFilter): List<VehicleReportItem> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating vehicles report with filter: ${filter.getFilterSummary()}")
            
            // Get current business context for security
            val currentBusinessId = businessContextManager.getCurrentBusinessId()
            Log.d(TAG, "🔒 Current business context: $currentBusinessId")
            
            val vehicles = vehicleRepository.getAllVehicles()
            Log.d(TAG, "🚗 Retrieved ${vehicles.size} vehicles from repository")
            
            // Debug: Log all vehicles with their business IDs
            vehicles.forEachIndexed { index, vehicle ->
                Log.d(TAG, "🚗 Vehicle $index: ${vehicle.model} (businessId: ${vehicle.businessId}, status: ${vehicle.status})")
            }
            
            val filteredVehicles = vehicles.mapNotNull { vehicle ->
                Log.d(TAG, "🔍 Processing vehicle: ${vehicle.model}")
                Log.d(TAG, "  - Vehicle businessId: ${vehicle.businessId}")
                Log.d(TAG, "  - Current user businessId: $currentBusinessId")
                Log.d(TAG, "  - Vehicle siteId: ${vehicle.siteId}")
                Log.d(TAG, "  - Filter siteId: ${filter.siteId}")
                Log.d(TAG, "  - Vehicle status: ${vehicle.status}")
                Log.d(TAG, "  - Filter status: ${filter.status}")
                
                // SECURITY: Always apply business context filter first
                if (vehicle.businessId != currentBusinessId) {
                    Log.d(TAG, "🔒 SECURITY: Filtered out - different business context")
                    return@mapNotNull null
                }
                
                // Apply additional filters
                if (filter.businessId != null && vehicle.businessId != filter.businessId) return@mapNotNull null
                if (filter.siteId != null && vehicle.siteId != filter.siteId) return@mapNotNull null
                if (filter.status != null && vehicle.status.name != filter.status) return@mapNotNull null
                if (filter.vehicleId != null && vehicle.id != filter.vehicleId) return@mapNotNull null
                
                Log.d(TAG, "  ✅ Vehicle passed all filters")
                
                // Get vehicle name - use codename (friendly name) if available, otherwise model
                val vehicleName = vehicle.codename.takeIf { it.isNotBlank() } 
                    ?: vehicle.model.takeIf { it.isNotBlank() }
                    ?: "Vehicle ${vehicle.id.take(8)}"
                
                // Get vehicle type name
                val vehicleTypeName = try {
                    vehicle.type.Name
                } catch (e: Exception) {
                    Log.w(TAG, "Could not get vehicle type for ${vehicle.model}", e)
                    "Unknown Type"
                }
                
                // Get business name (real name, not just ID)
                val businessName = try {
                    val businessId = vehicle.businessId ?: currentBusinessId
                    if (businessId != null) {
                        val business = businessRepository.getBusinessById(businessId)
                        business.name
                    } else {
                        "No Business Assigned"
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting business name for vehicle ${vehicle.id}", e)
                    val businessId = vehicle.businessId ?: currentBusinessId
                    if (businessId != null) {
                        "Business (${businessId.take(8)})"
                    } else {
                        "Unknown Business"
                    }
                }
                
                // Get site name - try to get real name, fallback to formatted ID
                val siteName = try {
                    vehicle.siteId?.let { siteId ->
                        // Use a more descriptive format with business context
                        val businessId = vehicle.businessId ?: currentBusinessId
                        "Site (${siteId.take(8)}) - Business (${businessId?.take(8) ?: "Unknown"})"
                    } ?: "No Site Assigned"
                } catch (e: Exception) {
                    "Unknown Site"
                }
                
                // Get session information using VehicleSessionRepository
                val lastSessionDate = try {
                    val lastSession = vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicle.id)
                    lastSession?.endTime?.substring(0, 10) // Extract date part
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting last session for vehicle ${vehicle.id}", e)
                    null
                }
                
                val lastSessionUser = try {
                    val lastSession = vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicle.id)
                    lastSession?.userId?.let { userId ->
                        "User ${userId.take(8)}" // Format user ID for display
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting last session user for vehicle ${vehicle.id}", e)
                    null
                }
                
                val totalSessions = try {
                    // Get all sessions for this vehicle and count them
                    val allSessions = vehicleSessionRepository.getSessions()
                    allSessions.count { session -> 
                        session.vehicleId == vehicle.id && session.endTime != null 
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting session count for vehicle ${vehicle.id}", e)
                    0
                }
                
                // Get vehicle category name (real name, not just ID)
                val vehicleCategoryName = try {
                    if (vehicle.categoryId.isNotBlank()) {
                        val category = vehicleCategoryRepository.getVehicleCategory(vehicle.categoryId)
                        category.name
                    } else {
                        "No Category"
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting category name for vehicle ${vehicle.id}", e)
                    if (vehicle.categoryId.isNotBlank()) {
                        "Category (${vehicle.categoryId.take(8)})"
                    } else {
                        "Unknown Category"
                    }
                }
                
                VehicleReportItem(
                    vehicleId = vehicle.id,
                    vehicleName = vehicleName,
                    vehicleType = vehicleTypeName,
                    status = vehicle.status.name,
                    lastSessionDate = lastSessionDate,
                    lastSessionUser = lastSessionUser,
                    totalSessions = totalSessions,
                    businessName = businessName,
                    siteName = siteName,
                    
                    // Additional vehicle details
                    model = vehicle.model.takeIf { it.isNotBlank() } ?: "Unknown Model",
                    serialNumber = vehicle.serialNumber.takeIf { it.isNotBlank() } ?: "No Serial Number",
                    description = vehicle.description.takeIf { it.isNotBlank() } 
                        ?: "${vehicleTypeName} - ${vehicle.energyType.takeIf { it.isNotBlank() } ?: "Unknown Energy"}",
                    energySource = vehicle.energyType.takeIf { it.isNotBlank() } ?: when (vehicle.energySource) {
                        0 -> "Electric"
                        1 -> "LPG"
                        2 -> "Diesel"
                        else -> "Unknown Energy Source"
                    },
                    categoryName = vehicleCategoryName,
                    currentHourMeter = vehicle.currentHourMeter ?: "Not Available",
                    hasIssues = vehicle.hasIssues
                )
            }
            
            Log.d(TAG, "🎯 Final result: ${filteredVehicles.size} vehicles after filtering")
            Log.d(TAG, "🔒 SECURITY: All vehicles belong to business: $currentBusinessId")
            filteredVehicles
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating vehicles report", e)
            emptyList()
        }
    }

    override suspend fun getChecklistsReport(filter: ReportFilter): List<ChecklistReportItem> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating checklists report with filter: ${filter.getFilterSummary()}")
            
            // Get current business context for security
            val currentBusinessId = businessContextManager.getCurrentBusinessId()
            Log.d(TAG, "🔒 Current business context: $currentBusinessId")
            
            // Get all checklist answers (completed checklists)
            val allChecks = checklistAnswerRepository.getAll()
            Log.d(TAG, "📋 Retrieved ${allChecks.size} checklist answers from repository")
            
            // Get all answered checklist items for detailed reports
            val allAnsweredItems = answeredChecklistItemRepository.getAll()
            Log.d(TAG, "📝 Retrieved ${allAnsweredItems.size} answered checklist items from repository")
            
            // Group answered items by checklistAnswerId for efficient lookup
            val answeredItemsByChecklistId = allAnsweredItems.groupBy { it.checklistAnswerId }
            
            val filteredChecklists = allChecks.mapNotNull { checklistAnswer ->
                Log.d(TAG, "🔍 Processing checklist answer: ${checklistAnswer.id}")
                
                // SECURITY: Apply business context filter
                if (checklistAnswer.businessId != currentBusinessId) {
                    Log.d(TAG, "🔒 SECURITY: Filtered out checklist - different business context")
                    return@mapNotNull null
                }
                
                // Apply additional filters
                if (filter.vehicleId != null && checklistAnswer.vehicleId != filter.vehicleId) return@mapNotNull null
                if (filter.userId != null && checklistAnswer.goUserId != filter.userId) return@mapNotNull null
                
                try {
                    // Get vehicle information
                    val vehicle = vehicleRepository.getAllVehicles().find { it.id == checklistAnswer.vehicleId }
                    
                    // Get business name (real name, not just ID)
                    val businessName = try {
                        val businessId = checklistAnswer.businessId ?: currentBusinessId
                        if (businessId != null) {
                            businessRepository.getBusinessById(businessId).name
                        } else {
                            "Unknown Business"
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error getting business name", e)
                        "Unknown Business"
                    }
                    
                    // Get site name - simplified approach
                    val siteName = vehicle?.siteId?.let { siteId ->
                        "Site ${siteId.take(8)}" // For now, use ID format
                    } ?: "Unknown Site"
                    
                    // Get vehicle details usando las propiedades que realmente existen
                    val vehicleName = vehicle?.codename?.takeIf { it.isNotBlank() } 
                        ?: vehicle?.model 
                        ?: checklistAnswer.vehicleName.takeIf { it != "Unknown" } 
                        ?: "Unknown Vehicle"
                    
                    val vehicleType = vehicle?.type?.Name ?: "Unknown Type"
                    val vehicleCategory = vehicle?.categoryId ?: "Unknown Category" // Usar categoryId en lugar de make
                    val vehicleModel = vehicle?.model ?: "Unknown Model"
                    val vehicleSerialNumber = vehicle?.serialNumber ?: "Unknown Serial"
                    
                    // Get user information
                    val userName = checklistAnswer.operatorName.takeIf { it.isNotBlank() && it != "Unknown" } 
                        ?: "User ${checklistAnswer.goUserId.take(8)}"
                    
                    // Parse dates and times
                    val startDateTime = checklistAnswer.startDateTime
                    val endDateTime = checklistAnswer.endDateTime.takeIf { it.isNotEmpty() } ?: startDateTime
                    val completedDate = endDateTime
                    
                    // Extract date and time components
                    val reportDate = try {
                        completedDate.substring(0, 10) // YYYY-MM-DD
                    } catch (e: Exception) {
                        completedDate
                    }
                    
                    val reportTime = try {
                        completedDate.substring(11, 19) // HH:MM:SS
                    } catch (e: Exception) {
                        "00:00:00"
                    }
                    
                    // Calculate duration
                    val duration = try {
                        checklistAnswer.duration?.toString() ?: "0"
                    } catch (e: Exception) {
                        "0"
                    }
                    
                    // Get answered items for this checklist
                    val answeredItems = answeredItemsByChecklistId[checklistAnswer.id] ?: emptyList()
                    Log.d(TAG, "📝 Found ${answeredItems.size} answered items for checklist ${checklistAnswer.id}")
                    
                    // Calculate real statistics from answered items
                    val passedItems = answeredItems.count { it.answer.equals("PASS", ignoreCase = true) || it.answer.equals("YES", ignoreCase = true) || it.answer.equals("OK", ignoreCase = true) }
                    val failedItems = answeredItems.count { it.answer.equals("FAIL", ignoreCase = true) || it.answer.equals("NO", ignoreCase = true) || it.answer.equals("NOT OK", ignoreCase = true) }
                    val totalItems = answeredItems.size
                    
                    // Determine status based on actual results
                    val status = when {
                        totalItems == 0 -> "INCOMPLETE"
                        failedItems > 0 -> "FAILED"
                        passedItems == totalItems -> "PASSED"
                        else -> "PARTIAL"
                    }
                    
                    // Determine issue flag
                    val issueFlag = if (failedItems > 0) "Y" else "N"
                    
                    // Get checklist name from ChecklistRepository
                    val checklistName = try {
                        val checklist = checklistRepository.getChecklistById(checklistAnswer.checklistId)
                        checklist?.title ?: "Checklist v${checklistAnswer.checklistVersion}"
                    } catch (e: Exception) {
                        Log.w(TAG, "Error getting checklist name for ${checklistAnswer.checklistId}", e)
                        "Checklist v${checklistAnswer.checklistVersion}"
                    }
                    
                    // Convert answered items to ChecklistItemResult for the report
                    val checklistItems = answeredItems.map { item ->
                        ChecklistItemResult(
                            question = item.question,
                            result = item.answer,
                            comment = item.userComment ?: ""
                        )
                    }
                    
                    // Create summary for "Sin Detalles" report
                    val summary = "Summary: ${passedItems} passed, ${failedItems} failed, ${totalItems} total"
                    
                    ChecklistReportItem(
                        checklistId = checklistAnswer.id,
                        checklistName = checklistName,
                        vehicleCodename = vehicleName,
                        userName = userName,
                        completedDate = completedDate,
                        status = status,
                        passedItems = passedItems,
                        failedItems = failedItems,
                        totalItems = totalItems,
                        businessName = businessName,
                        siteName = siteName,
                        locationCoordinates = checklistAnswer.locationCoordinates,
                        reportDate = reportDate,
                        reportTime = reportTime,
                        vehicleType = vehicleType,
                        vehicleCategory = vehicleCategory,
                        vehicleModel = vehicleModel,
                        vehicleSerialNumber = vehicleSerialNumber,
                        checklistVersion = checklistAnswer.checklistVersion,
                        appVersion = "1.0",
                        startDateTime = startDateTime,
                        endDateTime = endDateTime,
                        duration = duration,
                        checklistItems = checklistItems,
                        userComments = "", // TODO: Add when available
                        photoVideoUrl = "", // TODO: Add photo URL if available
                        issueFlag = issueFlag,
                        vehicleRemovedFromService = "N", // TODO: Determine based on vehicle status
                        summary = summary
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing checklist answer ${checklistAnswer.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "🎯 Final result: ${filteredChecklists.size} checklists after filtering")
            Log.d(TAG, "🔒 SECURITY: All checklists belong to business: $currentBusinessId")
            filteredChecklists
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating checklists report", e)
            emptyList()
        }
    }

    override suspend fun getIncidentsReport(filter: ReportFilter): List<IncidentReportItem> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating incidents report with filter: ${filter.getFilterSummary()}")
            
            // Get current business context for security
            val currentBusinessId = businessContextManager.getCurrentBusinessId()
            Log.d(TAG, "🔒 Current business context: $currentBusinessId")
            
            // Get all incidents with user information
            val incidentsResult = incidentRepository.getIncidents(include = "GOUser")
            val allIncidents = incidentsResult.getOrElse { 
                Log.w(TAG, "Failed to get incidents: ${incidentsResult.exceptionOrNull()}")
                emptyList()
            }
            Log.d(TAG, "🚨 Retrieved ${allIncidents.size} incidents from repository")
            
            val filteredIncidents = allIncidents.mapNotNull { incident ->
                Log.d(TAG, "🔍 Processing incident: ${incident.id}")
                
                // DEBUG: Log all incident data to see what's missing
                Log.d(TAG, "📊 INCIDENT DEBUG DATA:")
                Log.d(TAG, "  - ID: ${incident.id}")
                Log.d(TAG, "  - Type: ${incident.type}")
                Log.d(TAG, "  - Description: ${incident.description}")
                Log.d(TAG, "  - Timestamp: '${incident.timestamp}' (length: ${incident.timestamp.length})")
                Log.d(TAG, "  - LocationDetails: '${incident.locationDetails}' (length: ${incident.locationDetails.length})")
                Log.d(TAG, "  - Location: '${incident.location}' (length: ${incident.location.length})")
                Log.d(TAG, "  - Weather: '${incident.weather}' (length: ${incident.weather.length})")
                Log.d(TAG, "  - IsLoadCarried: ${incident.isLoadCarried}")
                Log.d(TAG, "  - LoadWeight: ${incident.loadWeight}")
                Log.d(TAG, "  - LoadBeingCarried: '${incident.loadBeingCarried}' (length: ${incident.loadBeingCarried.length})")
                Log.d(TAG, "  - OthersInvolved: '${incident.othersInvolved}' (length: ${incident.othersInvolved?.length ?: 0})")
                Log.d(TAG, "  - VehicleId: ${incident.vehicleId}")
                Log.d(TAG, "  - VehicleName: '${incident.vehicleName}' (length: ${incident.vehicleName.length})")
                Log.d(TAG, "  - SeverityLevel: ${incident.severityLevel}")
                Log.d(TAG, "  - Status: ${incident.status}")
                Log.d(TAG, "  - BusinessId: ${incident.businessId}")
                Log.d(TAG, "  - SiteId: ${incident.siteId}")
                Log.d(TAG, "  - Date (epoch): ${incident.date}")
                Log.d(TAG, "  - LocationCoordinates: '${incident.locationCoordinates}' (length: ${incident.locationCoordinates?.length ?: 0})")
                Log.d(TAG, "  - Injuries: '${incident.injuries}' (length: ${incident.injuries.length})")
                Log.d(TAG, "  - InjuryLocations: ${incident.injuryLocations.size} items")
                
                // SECURITY: Apply business context filter
                if (incident.businessId != currentBusinessId) {
                    Log.d(TAG, "🔒 SECURITY: Filtered out incident - different business context")
                    return@mapNotNull null
                }
                
                // Apply additional filters
                if (filter.vehicleId != null && incident.vehicleId != filter.vehicleId) return@mapNotNull null
                if (filter.userId != null && incident.userId != filter.userId) return@mapNotNull null
                if (filter.severity != null && incident.severityLevel?.name != filter.severity) return@mapNotNull null
                
                // Get vehicle information if available
                val vehicle = try {
                    incident.vehicleId?.let { vehicleId ->
                        vehicleRepository.getAllVehicles().find { it.id == vehicleId }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting vehicle for incident ${incident.id}", e)
                    null
                }
                
                // Get site name from vehicle or incident
                val siteName = try {
                    vehicle?.siteId?.let { "Site ${it.take(8)}" } 
                        ?: incident.siteId?.let { "Site ${it.take(8)}" } 
                        ?: "Unknown Site"
                } catch (e: Exception) {
                    "Unknown Site"
                }
                
                // Get business name
                val businessName = try {
                    "Business ${incident.businessId?.take(8) ?: "Unknown"}"
                } catch (e: Exception) {
                    "Unknown Business"
                }
                
                // Get reporter name (for now use creator name or user ID)
                val reportedBy = try {
                    if (incident.creatorName != "Unknown") {
                        incident.creatorName
                    } else {
                        "User ${incident.userId.take(8)}"
                    }
                } catch (e: Exception) {
                    "Unknown User"
                }
                
                // Determine incident type
                val incidentType = try {
                    incident.type.name
                } catch (e: Exception) {
                    "UNKNOWN"
                }
                
                // Get incident status
                val status = try {
                    incident.status.name
                } catch (e: Exception) {
                    "UNKNOWN"
                }
                
                // Get severity
                val severity = try {
                    incident.severityLevel?.name ?: "MEDIUM"
                } catch (e: Exception) {
                    "MEDIUM"
                }
                
                // Get type-specific fields
                val typeSpecificFields = extractIncidentTypeSpecificFields(incident)
                
                // Format reported date better - use multiple sources
                val reportedDate = try {
                    when {
                        incident.timestamp.isNotBlank() && incident.timestamp.contains("T") -> {
                            // ISO format: "2024-12-30T17:16:48"
                            incident.timestamp.substring(0, 19).replace("T", " ")
                        }
                        incident.timestamp.isNotBlank() -> incident.timestamp
                        incident.date > 0 -> {
                            // Convert epoch milliseconds to readable date
                            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                .format(java.util.Date(incident.date))
                        }
                        else -> "Date not available"
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error formatting date for incident ${incident.id}", e)
                    incident.timestamp.takeIf { it.isNotBlank() } ?: "Date not available"
                }
                
                // Improve location details using multiple sources
                val locationDetails = when {
                    incident.locationDetails.isNotBlank() -> incident.locationDetails
                    incident.location.isNotBlank() -> incident.location
                    incident.locationCoordinates?.isNotBlank() == true -> "Coordinates: ${incident.locationCoordinates}"
                    else -> "Location not specified"
                }
                
                // Improve weather information with better fallbacks
                val weather = when {
                    incident.weather.isNotBlank() -> incident.weather
                    else -> null // Keep null instead of placeholder for CSV clarity
                }
                
                // Improve load information with better logic
                val loadWeight = when {
                    incident.loadWeight != null -> incident.loadWeight.name
                    incident.isLoadCarried -> "Load carried (weight not specified)"
                    else -> null
                }
                
                val loadBeingCarried = when {
                    incident.loadBeingCarried.isNotBlank() -> incident.loadBeingCarried
                    incident.isLoadCarried -> "Load type not specified"
                    else -> null
                }
                
                // Improve others involved field
                val othersInvolved = when {
                    incident.othersInvolved?.isNotBlank() == true -> incident.othersInvolved
                    incident.injuries.isNotBlank() -> "Injuries reported: ${incident.injuries}"
                    incident.injuryLocations.isNotEmpty() -> "Injury locations: ${incident.injuryLocations.joinToString(", ")}"
                    else -> null
                }
                
                IncidentReportItem(
                    // Common fields
                    incidentId = incident.id ?: "Unknown",
                    type = incidentType,
                    severity = severity,
                    reportedDate = reportedDate,
                    reportedBy = reportedBy,
                    vehicleId = incident.vehicleId,
                    vehicleName = vehicle?.codename?.takeIf { it.isNotBlank() } 
                        ?: vehicle?.model 
                        ?: incident.vehicleName,
                    description = incident.description,
                    status = status,
                    businessName = businessName,
                    siteName = siteName,
                    locationDetails = locationDetails,
                    weather = weather,
                    
                    // Common load information
                    isLoadCarried = if (incident.isLoadCarried) "Yes" else "No",
                    loadWeight = loadWeight,
                    loadBeingCarried = loadBeingCarried,
                    othersInvolved = othersInvolved,
                    
                    // Type-specific fields
                    collisionType = typeSpecificFields["collisionType"],
                    injurySeverity = typeSpecificFields["injurySeverity"],
                    injuryLocation = typeSpecificFields["injuryLocation"],
                    damageOccurrence = typeSpecificFields["damageOccurrence"],
                    nearMissType = typeSpecificFields["nearMissType"],
                    potentialImpact = typeSpecificFields["potentialImpact"],
                    hazardType = typeSpecificFields["hazardType"],
                    potentialConsequences = typeSpecificFields["potentialConsequences"],
                    preventiveMeasures = typeSpecificFields["preventiveMeasures"],
                    failureType = typeSpecificFields["failureType"],
                    vehicleFailDamage = typeSpecificFields["vehicleFailDamage"],
                    environmentalImpact = typeSpecificFields["environmentalImpact"],
                    immediateActions = typeSpecificFields["immediateActions"],
                    longTermSolutions = typeSpecificFields["longTermSolutions"],
                    contributingFactors = typeSpecificFields["contributingFactors"]
                )
            }
            
            Log.d(TAG, "🎯 Final result: ${filteredIncidents.size} incidents after filtering")
            Log.d(TAG, "🔒 SECURITY: All incidents belong to business: $currentBusinessId")
            filteredIncidents
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating incidents report", e)
            emptyList()
        }
    }

    override suspend fun getCertificationsReport(filter: ReportFilter): List<CertificationReportItem> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating certifications report with filter: ${filter.getFilterSummary()}")
            
            // Get current business context for security
            val currentBusinessId = businessContextManager.getCurrentBusinessId()
            Log.d(TAG, "🔒 Current business context: $currentBusinessId")
            
            // Use the correct method from CertificationRepository
            val certifications = certificationRepository.getCertifications()
            
            certifications.mapNotNull { certification ->
                // SECURITY: Always apply business context filter first
                if (certification.businessId != currentBusinessId) {
                    Log.d(TAG, "🔒 SECURITY: Filtered out certification - different business context")
                    return@mapNotNull null
                }
                
                // Apply additional filters
                if (filter.businessId != null && certification.businessId != filter.businessId) return@mapNotNull null
                if (filter.userId != null && certification.userId != filter.userId) return@mapNotNull null
                if (filter.status != null && certification.status.name != filter.status) return@mapNotNull null
                
                val daysUntilExpiry = certification.expiryDate?.let { expiryDate ->
                    try {
                        val expiry = LocalDate.parse(expiryDate, dateFormatter)
                        val today = LocalDate.now()
                        expiry.toEpochDay() - today.toEpochDay()
                    } catch (e: Exception) {
                        null
                    }
                }?.toInt()
                
                CertificationReportItem(
                    certificationId = certification.id,
                    userName = "User", // TODO: Get from user repository
                    certificationType = certification.name,
                    issueDate = certification.issuedDate ?: "",
                    expiryDate = certification.expiryDate ?: "",
                    status = certification.status.name,
                    daysUntilExpiry = daysUntilExpiry,
                    vehicleTypes = certification.vehicleTypes.map { it.Name },
                    businessName = "Business" // TODO: Get from business repository
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating certifications report", e)
            emptyList()
        }
    }

    override suspend fun exportReport(
        reportType: ReportType,
        filter: ReportFilter,
        format: ExportFormat
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Exporting ${reportType.displayName} report in ${format.displayName} format")
            
            val data = when (reportType) {
                ReportType.VEHICLES -> getVehiclesReport(filter)
                ReportType.CHECKLISTS -> getChecklistsReport(filter)
                ReportType.INCIDENTS -> getIncidentsReport(filter)
                ReportType.CERTIFICATIONS -> getCertificationsReport(filter)
            }
            
            when (format) {
                ExportFormat.CSV -> exportToCsv(data, reportType, filter)
                ExportFormat.JSON -> exportToJson(data, reportType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting report", e)
            byteArrayOf()
        }
    }

    override suspend fun getFilterOptions(reportType: ReportType): ReportFilterOptions = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting filter options for: ${reportType.displayName}")
            
            // Get current business context for security
            val currentBusinessId = businessContextManager.getCurrentBusinessId()
            
            // Get vehicles filtered by business context
            val vehicles = vehicleRepository.getAllVehicles().filter { 
                it.businessId == currentBusinessId 
            }
            
            val vehicleOptions = vehicles.map { 
                FilterOption(it.id, "${it.model} (${it.status.name})") 
            }
            
            // Get unique sites from vehicles
            val siteOptions = vehicles.mapNotNull { vehicle ->
                vehicle.siteId?.let { siteId ->
                    FilterOption(siteId, "Site ${siteId.take(8)}")
                }
            }.distinctBy { it.value }
            
            // Get unique vehicle types
            val typeOptions = vehicles.mapNotNull { vehicle ->
                vehicle.type?.let { type ->
                    FilterOption(type.Id, type.Name)
                }
            }.distinctBy { it.value }
            
            val statuses = when (reportType) {
                ReportType.VEHICLES -> listOf(
                    FilterOption("AVAILABLE", "Available"),
                    FilterOption("IN_USE", "In Use"),
                    FilterOption("OUT_OF_SERVICE", "Out of Service"),
                    FilterOption("MAINTENANCE", "Maintenance")
                )
                ReportType.CERTIFICATIONS -> listOf(
                    FilterOption("ACTIVE", "Active"),
                    FilterOption("EXPIRED", "Expired"),
                    FilterOption("EXPIRING_SOON", "Expiring Soon")
                )
                ReportType.INCIDENTS -> listOf(
                    FilterOption("LOW", "Low Severity"),
                    FilterOption("MEDIUM", "Medium Severity"),
                    FilterOption("HIGH", "High Severity"),
                    FilterOption("CRITICAL", "Critical Severity")
                )
                else -> emptyList()
            }
            
            // Get date range options
            val dateRangeOptions = listOf(
                FilterOption("TODAY", "Today"),
                FilterOption("LAST_7_DAYS", "Last 7 Days"),
                FilterOption("LAST_30_DAYS", "Last 30 Days"),
                FilterOption("LAST_90_DAYS", "Last 90 Days"),
                FilterOption("CUSTOM", "Custom Range")
            )
            
            // Get detail options for checklist reports
            val detailOptions = when (reportType) {
                ReportType.CHECKLISTS -> listOf(
                    FilterOption("true", "With Details"),
                    FilterOption("false", "No Details")
                )
                else -> emptyList()
            }
            
            Log.d(TAG, "Filter options generated: vehicles=${vehicleOptions.size}, sites=${siteOptions.size}, types=${typeOptions.size}, statuses=${statuses.size}, details=${detailOptions.size}")
            
            ReportFilterOptions(
                businesses = listOf(FilterOption(currentBusinessId ?: "", "Current Business")), // Current business only for security
                sites = siteOptions,
                users = emptyList(), // TODO: Add when user repository is available
                vehicles = vehicleOptions,
                statuses = statuses,
                types = typeOptions,
                severities = if (reportType == ReportType.INCIDENTS) statuses else emptyList(),
                dateRanges = dateRangeOptions,
                detailOptions = detailOptions
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting filter options", e)
            ReportFilterOptions(
                businesses = emptyList(),
                sites = emptyList(),
                users = emptyList(),
                vehicles = emptyList(),
                statuses = emptyList(),
                types = emptyList(),
                severities = emptyList(),
                dateRanges = emptyList(),
                detailOptions = emptyList()
            )
        }
    }
    
    private fun exportToCsv(data: List<Any>, reportType: ReportType, filter: ReportFilter = ReportFilter()): ByteArray {
        val csv = StringBuilder()
        
        when (reportType) {
            ReportType.VEHICLES -> {
                csv.append("Vehicle Name,Type,Status,Model,Serial Number,Description,Energy Source,Category,Current Hour Meter,Has Issues,Last Session Date,Last Session User,Total Sessions,Business,Site\n")
                (data as List<VehicleReportItem>).forEach { item ->
                    csv.append("\"${item.vehicleName}\",\"${item.vehicleType}\",\"${item.status}\",\"${item.model}\",\"${item.serialNumber}\",\"${item.description}\",\"${item.energySource}\",\"${item.categoryName}\",\"${item.currentHourMeter}\",\"${if (item.hasIssues) "Yes" else "No"}\",\"${item.lastSessionDate ?: ""}\",\"${item.lastSessionUser ?: ""}\",${item.totalSessions},\"${item.businessName}\",\"${item.siteName}\"\n")
                }
            }
            ReportType.CERTIFICATIONS -> {
                csv.append("Certification ID,User Name,Type,Issue Date,Expiry Date,Status,Days Until Expiry,Vehicle Types,Business\n")
                (data as List<CertificationReportItem>).forEach { item ->
                    csv.append("\"${item.certificationId}\",\"${item.userName}\",\"${item.certificationType}\",\"${item.issueDate}\",\"${item.expiryDate}\",\"${item.status}\",${item.daysUntilExpiry ?: ""},\"${item.vehicleTypes.joinToString(";")}\",\"${item.businessName}\"\n")
                }
            }
            ReportType.CHECKLISTS -> {
                // Control the level of detail based on filter.includeDetails
                val includeDetails = filter.includeDetails ?: true // Default to true if not specified
                
                if (includeDetails) {
                    // CON DETALLES: Include all common fields PLUS detailed fields
                    csv.append("Report ID,Checklist Name,Date,Time,Operator Name,Vehicle Codename,Vehicle Type,Category,Model,Serial Number,Business,Site,Start DateTime,End DateTime,Last Check DateTime,Duration,Checklist Version,App Version,Checklist Item,Result,Comment,Photo/Video URL,Issue Flag (Y/N),Vehicle Removed from Service (Y/N)\n")
                    (data as List<ChecklistReportItem>).forEach { item ->
                        if (item.checklistItems.isNotEmpty()) {
                            // Export each checklist item as a separate row
                            item.checklistItems.forEach { checklistItem ->
                                csv.append("\"${item.checklistId}\",\"${item.checklistName}\",\"${item.reportDate}\",\"${item.reportTime}\",\"${item.userName}\",\"${item.vehicleCodename}\",\"${item.vehicleType}\",\"${item.vehicleCategory}\",\"${item.vehicleModel}\",\"${item.vehicleSerialNumber}\",\"${item.businessName}\",\"${item.siteName}\",\"${item.startDateTime}\",\"${item.endDateTime}\",\"${item.completedDate}\",\"${item.duration}\",\"${item.checklistVersion}\",\"${item.appVersion}\",\"${checklistItem.question}\",\"${checklistItem.result}\",\"${checklistItem.comment}\",\"${item.photoVideoUrl}\",\"${item.issueFlag}\",\"${item.vehicleRemovedFromService}\"\n")
                            }
                        } else {
                            // If no individual items, export summary row with details
                            csv.append("\"${item.checklistId}\",\"${item.checklistName}\",\"${item.reportDate}\",\"${item.reportTime}\",\"${item.userName}\",\"${item.vehicleCodename}\",\"${item.vehicleType}\",\"${item.vehicleCategory}\",\"${item.vehicleModel}\",\"${item.vehicleSerialNumber}\",\"${item.businessName}\",\"${item.siteName}\",\"${item.startDateTime}\",\"${item.endDateTime}\",\"${item.completedDate}\",\"${item.duration}\",\"${item.checklistVersion}\",\"${item.appVersion}\",\"${item.summary}\",\"${item.status}\",\"${item.userComments}\",\"${item.photoVideoUrl}\",\"${item.issueFlag}\",\"${item.vehicleRemovedFromService}\"\n")
                        }
                    }
                } else {
                    // SIN DETALLES: Common fields only, no detailed item-specific fields, but include Summary
                    csv.append("Report ID,Checklist Name,Date,Time,Operator Name,Vehicle Codename,Vehicle Type,Category,Model,Serial Number,Business,Site,Start DateTime,End DateTime,Last Check DateTime,Duration,Checklist Version,App Version,Status,Passed Items,Failed Items,Total Items,Summary\n")
                    (data as List<ChecklistReportItem>).forEach { item ->
                        csv.append("\"${item.checklistId}\",\"${item.checklistName}\",\"${item.reportDate}\",\"${item.reportTime}\",\"${item.userName}\",\"${item.vehicleCodename}\",\"${item.vehicleType}\",\"${item.vehicleCategory}\",\"${item.vehicleModel}\",\"${item.vehicleSerialNumber}\",\"${item.businessName}\",\"${item.siteName}\",\"${item.startDateTime}\",\"${item.endDateTime}\",\"${item.completedDate}\",\"${item.duration}\",\"${item.checklistVersion}\",\"${item.appVersion}\",\"${item.status}\",${item.passedItems},${item.failedItems},${item.totalItems},\"${item.summary}\"\n")
                    }
                }
            }
            ReportType.INCIDENTS -> {
                csv.append("Incident ID,Type,Severity,Reported Date,Reported By,Vehicle ID,Vehicle Name,Description,Status,Business,Site,Location Details,Weather,Load Carried,Load Weight,Load Being Carried,Others Involved,Collision Type,Injury Severity,Injury Location,Damage Occurrence,Near Miss Type,Potential Impact,Hazard Type,Potential Consequences,Preventive Measures,Failure Type,Vehicle Fail Damage,Environmental Impact,Immediate Actions,Long Term Solutions,Contributing Factors\n")
                (data as List<IncidentReportItem>).forEach { item ->
                    csv.append("\"${item.incidentId}\",\"${item.type}\",\"${item.severity}\",\"${item.reportedDate}\",\"${item.reportedBy}\",\"${item.vehicleId ?: ""}\",\"${item.vehicleName ?: ""}\",\"${item.description}\",\"${item.status}\",\"${item.businessName}\",\"${item.siteName}\",\"${item.locationDetails}\",\"${item.weather ?: ""}\",\"${item.isLoadCarried ?: ""}\",\"${item.loadWeight ?: ""}\",\"${item.loadBeingCarried ?: ""}\",\"${item.othersInvolved ?: ""}\",\"${item.collisionType ?: ""}\",\"${item.injurySeverity ?: ""}\",\"${item.injuryLocation ?: ""}\",\"${item.damageOccurrence ?: ""}\",\"${item.nearMissType ?: ""}\",\"${item.potentialImpact ?: ""}\",\"${item.hazardType ?: ""}\",\"${item.potentialConsequences ?: ""}\",\"${item.preventiveMeasures ?: ""}\",\"${item.failureType ?: ""}\",\"${item.vehicleFailDamage ?: ""}\",\"${item.environmentalImpact ?: ""}\",\"${item.immediateActions ?: ""}\",\"${item.longTermSolutions ?: ""}\",\"${item.contributingFactors ?: ""}\"\n")
                }
            }
        }
        
        return csv.toString().toByteArray()
    }
    
    private fun exportToJson(data: List<Any>, reportType: ReportType): ByteArray {
        // Simple JSON export implementation
        val jsonBuilder = StringBuilder()
        jsonBuilder.append("[\n")
        
        data.forEachIndexed { index, item ->
            if (index > 0) jsonBuilder.append(",\n")
            jsonBuilder.append("  \"${item.toString()}\"")
        }
        
        jsonBuilder.append("\n]")
        return jsonBuilder.toString().toByteArray()
    }

    /**
     * Helper function to extract type-specific fields from incident based on its type
     */
    private fun extractIncidentTypeSpecificFields(incident: Incident): Map<String, String?> {
        return try {
            when (incident.type) {
                IncidentTypeEnum.COLLISION -> extractCollisionFields(incident)
                IncidentTypeEnum.NEAR_MISS -> extractNearMissFields(incident)
                IncidentTypeEnum.HAZARD -> extractHazardFields(incident)
                IncidentTypeEnum.VEHICLE_FAIL -> extractVehicleFailFields(incident)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting type-specific fields for incident ${incident.id}", e)
            emptyMap()
        }
    }
    
    private fun extractCollisionFields(incident: Incident): Map<String, String?> {
        val collisionFields = incident.typeSpecificFields as? IncidentTypeFields.CollisionFields
        
        return mapOf(
            "collisionType" to collisionFields?.collisionType?.name,
            "injurySeverity" to collisionFields?.injurySeverity?.name,
            "injuryLocation" to collisionFields?.injuryLocations?.joinToString(", "),
            "damageOccurrence" to collisionFields?.damageOccurrence?.joinToString(", ") { it.name },
            "immediateActions" to collisionFields?.immediateActions?.joinToString(", ") { it.name },
            "longTermSolutions" to collisionFields?.longTermSolutions?.joinToString(", ") { it.name },
            "contributingFactors" to collisionFields?.contributingFactors?.joinToString(", ") { it.name },
            "environmentalImpact" to collisionFields?.environmentalImpact?.joinToString(", ") { it.name }
        )
    }
    
    private fun extractNearMissFields(incident: Incident): Map<String, String?> {
        val nearMissFields = incident.typeSpecificFields as? IncidentTypeFields.NearMissFields
        
        return mapOf(
            "nearMissType" to nearMissFields?.nearMissType?.name,
            "potentialImpact" to calculatePotentialImpact(incident.severityLevel, nearMissFields?.nearMissType),
            "immediateActions" to nearMissFields?.immediateActions?.joinToString(", ") { it.name },
            "longTermSolutions" to nearMissFields?.longTermSolutions?.joinToString(", ") { it.name },
            "contributingFactors" to nearMissFields?.contributingFactors?.joinToString(", ") { it.name }
        )
    }
    
    private fun extractHazardFields(incident: Incident): Map<String, String?> {
        val hazardFields = incident.typeSpecificFields as? IncidentTypeFields.HazardFields
        
        return mapOf(
            "hazardType" to hazardFields?.hazardType?.name,
            "potentialConsequences" to hazardFields?.potentialConsequences?.joinToString(", ") { it.name },
            "preventiveMeasures" to hazardFields?.preventiveMeasures?.joinToString(", ") { it.name },
            "correctiveActions" to hazardFields?.correctiveActions?.joinToString(", ") { it.name }
        )
    }
    
    private fun extractVehicleFailFields(incident: Incident): Map<String, String?> {
        val vehicleFailFields = incident.typeSpecificFields as? IncidentTypeFields.VehicleFailFields
        
        return mapOf(
            "failureType" to vehicleFailFields?.failureType?.name,
            "vehicleFailDamage" to vehicleFailFields?.damageOccurrence?.joinToString(", ") { it.name },
            "environmentalImpact" to vehicleFailFields?.environmentalImpact?.joinToString(", ") { it.name },
            "immediateActions" to vehicleFailFields?.immediateActions?.joinToString(", ") { it.name },
            "longTermSolutions" to vehicleFailFields?.longTermSolutions?.joinToString(", ") { it.name },
            "contributingFactors" to vehicleFailFields?.contributingFactors?.joinToString(", ") { it.name }
        )
    }
    
    private fun calculatePotentialImpact(severity: IncidentSeverityLevelEnum?, nearMissType: NearMissType?): String? {
        return when {
            severity == IncidentSeverityLevelEnum.HIGH && nearMissType == NearMissType.PEDESTRIAN_AVOIDANCE -> "High - Potential serious injury"
            severity == IncidentSeverityLevelEnum.CRITICAL -> "Critical - Potential fatality or major damage"
            nearMissType == NearMissType.LOAD_INSTABILITY -> "Medium - Load damage or injury risk"
            nearMissType == NearMissType.BRAKING_ISSUE -> "High - Collision risk"
            else -> severity?.name?.let { "${it.lowercase().replaceFirstChar { c -> c.uppercase() }} - Potential incident" }
        }
    }

    /**
     * Helper function to provide meaningful fallbacks for empty incident fields
     */
    private fun getIncidentFieldWithFallback(
        incident: Incident, 
        field: String, 
        primaryValue: String?, 
        fallbackValue: String? = null
    ): String {
        return when {
            !primaryValue.isNullOrBlank() -> primaryValue
            !fallbackValue.isNullOrBlank() -> fallbackValue
            else -> {
                Log.w(TAG, "⚠️ Missing data for incident ${incident.id}: $field is empty")
                when (field) {
                    "timestamp" -> "Date not recorded"
                    "weather" -> "Weather not recorded"
                    "locationDetails" -> "Location not specified"
                    "loadBeingCarried" -> "Load details not specified"
                    else -> "Not specified"
                }
            }
        }
    }
} 