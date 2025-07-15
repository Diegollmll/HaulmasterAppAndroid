package app.forku.domain.usecase.certification

import app.forku.domain.model.certification.CertificationVehicleType
import app.forku.domain.repository.certification.CertificationVehicleTypeRepository
import javax.inject.Inject
import java.util.UUID

class GetCertificationVehicleTypesUseCase @Inject constructor(
    private val repository: CertificationVehicleTypeRepository
) {
    suspend operator fun invoke(certificationId: String): List<CertificationVehicleType> =
        repository.getCertificationVehicleTypesByCertificationId(certificationId)
}

class SaveCertificationVehicleTypesUseCase @Inject constructor(
    private val repository: CertificationVehicleTypeRepository
) {
    suspend operator fun invoke(
        certificationId: String,
        vehicleTypeIds: List<String>
    ): Result<List<CertificationVehicleType>> {
        return try {
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ========== STARTING DIFFERENTIAL LOGIC ==========")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] CertificationId: $certificationId")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] Requested VehicleTypeIds: $vehicleTypeIds")
            
            // Get existing associations
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] Getting existing associations...")
            val existingAssociations = repository.getCertificationVehicleTypesByCertificationId(certificationId)
            val existingVehicleTypeIds = existingAssociations.map { it.vehicleTypeId }
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] Existing VehicleTypeIds: $existingVehicleTypeIds")
            
            // Calculate differences
            val toCreate = vehicleTypeIds - existingVehicleTypeIds.toSet()
            val toDelete = existingVehicleTypeIds - vehicleTypeIds.toSet()
            val toKeep = vehicleTypeIds.intersect(existingVehicleTypeIds.toSet())
            
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] DIFFERENTIAL ANALYSIS:")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE]   - To CREATE: $toCreate (${toCreate.size} items)")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE]   - To DELETE: $toDelete (${toDelete.size} items)")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE]   - To KEEP: $toKeep (${toKeep.size} items)")
            
            val results = mutableListOf<CertificationVehicleType>()
            
            // Delete removed associations (only if necessary)
            if (toDelete.isNotEmpty()) {
                android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] Deleting ${toDelete.size} removed associations...")
                for (vehicleTypeId in toDelete) {
                    val associationToDelete = existingAssociations.find { it.vehicleTypeId == vehicleTypeId }
                    if (associationToDelete != null) {
                        android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] Deleting association: ${associationToDelete.id} (vehicleTypeId=$vehicleTypeId)")
                        val deleteResult = repository.deleteCertificationVehicleType(associationToDelete.id)
                        if (deleteResult.isFailure) {
                            android.util.Log.w("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ⚠️ Failed to delete association ${associationToDelete.id}, continuing: ${deleteResult.exceptionOrNull()?.message}")
                        } else {
                            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ✅ Successfully deleted association: ${associationToDelete.id}")
                        }
                    }
                }
            } else {
                android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ✅ No associations to delete")
            }
            
            // Create new associations (only if necessary)
            if (toCreate.isNotEmpty()) {
                android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] Creating ${toCreate.size} new associations...")
                for (vehicleTypeId in toCreate) {
                    val certificationVehicleType = CertificationVehicleType(
                        id = UUID.randomUUID().toString(),
                        certificationId = certificationId,
                        vehicleTypeId = vehicleTypeId,
                        vehicleTypeName = null, // Will be populated by backend or mapping
                        businessId = null, // Will be set by repository
                        siteId = null, // Will be set by repository
                        timestamp = java.time.Instant.now().toString(), // ISO format timestamp
                        isMarkedForDeletion = false,
                        isDirty = true,
                        isNew = true,
                        internalObjectId = 0
                    )
                    
                    android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] Creating NEW association: certId=$certificationId, vehicleTypeId=$vehicleTypeId")
                    val result = repository.createCertificationVehicleType(certificationVehicleType)
                    if (result.isSuccess) {
                        val created = result.getOrThrow()
                        android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ✅ Successfully created association: ${created.id}")
                        results.add(created)
                    } else {
                        android.util.Log.e("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ❌ Failed to create association: ${result.exceptionOrNull()?.message}")
                        return Result.failure(result.exceptionOrNull() ?: Exception("Failed to create association"))
                    }
                }
            } else {
                android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ✅ No new associations to create")
            }
            
            // Add existing associations that we're keeping
            val keptAssociations = existingAssociations.filter { it.vehicleTypeId in toKeep }
            results.addAll(keptAssociations)
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ✅ Kept ${keptAssociations.size} existing associations")
            
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ✅ DIFFERENTIAL LOGIC COMPLETED:")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE]   - Total final associations: ${results.size}")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE]   - Created: ${toCreate.size}, Deleted: ${toDelete.size}, Kept: ${keptAssociations.size}")
            android.util.Log.d("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ========== COMPLETED ==========")
            Result.success(results)
        } catch (e: Exception) {
            android.util.Log.e("SaveCertificationVehicleTypesUseCase", "📋 [SAVE] ❌ Exception in SaveCertificationVehicleTypesUseCase", e)
            Result.failure(e)
        }
    }
}

class DeleteCertificationVehicleTypesUseCase @Inject constructor(
    private val repository: CertificationVehicleTypeRepository
) {
    suspend operator fun invoke(certificationId: String): Result<Boolean> =
        repository.deleteCertificationVehicleTypesByCertificationId(certificationId)
} 