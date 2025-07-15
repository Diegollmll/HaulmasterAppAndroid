package app.forku.domain.usecase.incident

import app.forku.domain.repository.incident.IncidentRepository
import javax.inject.Inject
import android.util.Log

class GetUserIncidentCountUseCase @Inject constructor(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(userId: String? = null, businessId: String? = null, siteId: String? = null): Int {
        Log.d("GetUserIncidentCountUseCase", "[invoke] CALLED with userId=$userId, businessId=$businessId, siteId=$siteId")
        val result = repository.getIncidentCountForUser(userId, businessId, siteId)
        Log.d("GetUserIncidentCountUseCase", "[invoke] RESULT: $result incidents found")
        return result
    }
} 