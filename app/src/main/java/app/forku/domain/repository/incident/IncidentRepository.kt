package app.forku.domain.repository.incident

import app.forku.domain.model.incident.Incident

interface IncidentRepository {
    suspend fun reportIncident(incident: Incident): Result<Incident>
    suspend fun getIncidents(filter: String? = null, include: String? = null): Result<List<Incident>>
    suspend fun getIncidentById(id: String): Result<Incident>
    suspend fun getOperatorIncidents(): Result<List<Incident>>
    suspend fun getIncidentsByUserId(userId: String): Result<List<Incident>>
    suspend fun getIncidentCountForUser(userId: String? = null, businessId: String? = null, siteId: String? = null): Int
    
    /**
     * ✅ NEW: Get incident count specifically for Admin Dashboard
     * Counts ALL incidents in business/site context (not filtered by user)
     * @param businessId The business ID to filter by
     * @param siteId The site ID to filter by (null = all sites in business)
     * @return Total count of incidents
     */
    suspend fun getIncidentCountForAdmin(businessId: String?, siteId: String?): Int
    
    /**
     * ✅ NEW: Get incidents with explicit business and site filters (VIEW_FILTER mode)
     * Does NOT use user's personal context, uses provided filter parameters
     * Used for admin filtering across different sites
     */
    suspend fun getIncidentsWithFilters(businessId: String, siteId: String?): Result<List<Incident>>
} 