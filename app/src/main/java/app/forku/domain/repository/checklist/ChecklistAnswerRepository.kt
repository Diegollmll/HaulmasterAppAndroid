package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistAnswer

interface ChecklistAnswerRepository {
    suspend fun getById(id: String): ChecklistAnswer?
    suspend fun getAll(): List<ChecklistAnswer>
    suspend fun getAllPaginated(page: Int, pageSize: Int): List<ChecklistAnswer>
    suspend fun save(item: ChecklistAnswer): ChecklistAnswer
    suspend fun delete(item: ChecklistAnswer)
    suspend fun getLastChecklistAnswerForVehicle(vehicleId: String): ChecklistAnswer?
} 