package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistQuestionVehicleType

interface ChecklistQuestionVehicleTypeRepository {
    suspend fun getVehicleTypesByChecklistItemId(checklistItemId: String): List<ChecklistQuestionVehicleType>
    suspend fun getAllQuestionVehicleTypes(): List<ChecklistQuestionVehicleType>
    suspend fun getQuestionVehicleTypeById(id: String): ChecklistQuestionVehicleType?
    suspend fun saveQuestionVehicleTypeAssociation(questionVehicleType: ChecklistQuestionVehicleType): ChecklistQuestionVehicleType
    suspend fun deleteQuestionVehicleTypeAssociation(id: String): Boolean
} 