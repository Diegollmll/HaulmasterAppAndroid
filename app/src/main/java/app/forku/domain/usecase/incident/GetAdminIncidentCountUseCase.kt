package app.forku.domain.usecase.incident

import app.forku.domain.repository.incident.IncidentRepository
import javax.inject.Inject
import android.util.Log

class GetAdminIncidentCountUseCase @Inject constructor(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(businessId: String? = null, siteId: String? = null): Int {
        Log.d("GetAdminIncidentCountUseCase", "[invoke] === 🚀 ADMIN INCIDENT COUNT USECASE ===")
        Log.d("GetAdminIncidentCountUseCase", "[invoke] CALLED with businessId=$businessId, siteId=$siteId")
        
        val result = repository.getIncidentCountForAdmin(businessId, siteId)
        
        Log.d("GetAdminIncidentCountUseCase", "[invoke] RESULT: $result incidents found for Admin")
        Log.d("GetAdminIncidentCountUseCase", "[invoke] === END ADMIN INCIDENT COUNT ====")
        
        return result
    }
} 