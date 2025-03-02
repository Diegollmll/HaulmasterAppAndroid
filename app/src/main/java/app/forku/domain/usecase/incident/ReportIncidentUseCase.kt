package app.forku.domain.usecase.incident

import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.AuthRepository
import javax.inject.Inject
import android.net.Uri

class ReportIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        type: IncidentType,
        description: String,
        photos: List<Uri>
    ): Result<Incident> {
        val currentUser = authRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not authenticated"))

        val currentSession = sessionRepository.getCurrentSession()
        
        val incident = Incident(
            type = type,
            description = description,
            timestamp = java.time.Instant.now().toString(),
            userId = currentUser.id,
            vehicleId = currentSession?.vehicleId,
            sessionId = currentSession?.id,
            photos = photos
        )

        return incidentRepository.reportIncident(incident)
    }
} 