package app.forku.domain.usecase.incident

import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.UserRepository
import javax.inject.Inject
import android.net.Uri
import app.forku.domain.model.incident.IncidentStatus
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.incident.IncidentSeverityLevel
import app.forku.domain.model.incident.IncidentTypeFields
import java.time.LocalTime
import app.forku.domain.model.incident.LoadWeight

class ReportIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        type: IncidentType,
        date: Long,
        location: String,
        locationDetails: String,
        weather: String,
        description: String,
        incidentTime: LocalTime?,
        severityLevel: IncidentSeverityLevel?,
        preshiftCheckStatus: String,
        typeSpecificFields: IncidentTypeFields?,
        sessionId: String?,
        operatorId: String?,
        othersInvolved: List<String>,
        injuries: String,
        injuryLocations: List<String>,
        vehicleId: String?,
        vehicleType: VehicleType?,
        vehicleName: String,
        isLoadCarried: Boolean = false,
        loadBeingCarried: String = "",
        loadWeight: LoadWeight? = null,
        photos: List<Uri>,
        locationCoordinates: String?
    ): Result<Incident> {
        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val incident = Incident(
                id = null,
                type = type,
                description = description,
                timestamp = java.time.Instant.now().toString(),
                userId = currentUser.id,
                vehicleId = vehicleId,
                sessionId = sessionId,
                status = IncidentStatus.REPORTED,
                photos = photos,
                date = date,
                location = location,
                locationDetails = locationDetails,
                weather = weather,
                incidentTime = incidentTime,
                severityLevel = severityLevel,
                preshiftCheckStatus = preshiftCheckStatus,
                typeSpecificFields = typeSpecificFields,
                operatorId = operatorId,
                othersInvolved = othersInvolved,
                injuries = injuries,
                injuryLocations = injuryLocations,
                vehicleType = vehicleType,
                vehicleName = vehicleName,
                isLoadCarried = isLoadCarried,
                loadBeingCarried = loadBeingCarried,
                loadWeight = loadWeight,
                locationCoordinates = locationCoordinates
            )

            incidentRepository.reportIncident(incident)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 