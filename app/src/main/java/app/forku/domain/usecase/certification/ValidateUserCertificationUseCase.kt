package app.forku.domain.usecase.certification

import app.forku.domain.model.certification.Certification
import app.forku.domain.model.certification.CertificationStatus
import app.forku.domain.repository.certification.CertificationRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.core.business.BusinessContextManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Use case to validate that a user has valid certifications for a specific vehicle type
 */
class ValidateUserCertificationUseCase @Inject constructor(
    private val certificationRepository: CertificationRepository,
    private val vehicleRepository: VehicleRepository,
    private val businessContextManager: BusinessContextManager
) {
    
    /**
     * Validates if the user can operate a specific vehicle based on their certifications
     * @param userId The user ID to validate
     * @param vehicleId The vehicle ID to check
     * @return ValidationResult with details about the validation
     */
    suspend operator fun invoke(userId: String, vehicleId: String): ValidationResult {
        return try {
            // Get the vehicle to determine its type
            val businessId = businessContextManager.getCurrentBusinessId()
                ?: return ValidationResult(
                    isValid = false,
                    message = "No business context available",
                    validCertifications = emptyList()
                )
            
            val vehicle = vehicleRepository.getVehicle(vehicleId, businessId)
            val vehicleTypeId = vehicle.type.Id
            val vehicleTypeName = vehicle.type.Name
            
            // Get user's certifications
            val userCertifications = certificationRepository.getCertifications(userId)
            
            // Filter certifications that include this vehicle type and are valid
            val validCertifications = userCertifications.filter { certification: Certification ->
                // Check if certification is active
                if (certification.status != CertificationStatus.ACTIVE) {
                    return@filter false
                }
                
                // Check if certification has expired (3 years validity)
                if (isCertificationExpired(certification)) {
                    return@filter false
                }
                
                // Check if certification includes this vehicle type
                certification.vehicleTypeIds.contains(vehicleTypeId)
            }
            
            if (validCertifications.isNotEmpty()) {
                ValidationResult(
                    isValid = true,
                    message = "User has valid certification for vehicle type: $vehicleTypeName",
                    validCertifications = validCertifications
                )
            } else {
                // Check why validation failed
                val hasAnyCertification = userCertifications.any { certification -> certification.vehicleTypeIds.contains(vehicleTypeId) }
                val message = when {
                    !hasAnyCertification -> "No certification found for vehicle type: $vehicleTypeName"
                    userCertifications.any { certification -> certification.vehicleTypeIds.contains(vehicleTypeId) && certification.status != CertificationStatus.ACTIVE } -> 
                        "Certification for $vehicleTypeName is inactive"
                    userCertifications.any { certification -> certification.vehicleTypeIds.contains(vehicleTypeId) && isCertificationExpired(certification) } -> 
                        "Certification for $vehicleTypeName has expired (3 year validity)"
                    else -> "Invalid certification for vehicle type: $vehicleTypeName"
                }
                
                ValidationResult(
                    isValid = false,
                    message = message,
                    validCertifications = emptyList()
                )
            }
            
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                message = "Error validating certification: ${e.message}",
                validCertifications = emptyList()
            )
        }
    }
    
    /**
     * Checks if a certification has expired (3 years from issued date)
     */
    private fun isCertificationExpired(certification: Certification): Boolean {
        return try {
            val issuedDate = LocalDate.parse(
                certification.issuedDate.substring(0, 10), // Take only YYYY-MM-DD part
                DateTimeFormatter.ISO_LOCAL_DATE
            )
            val expiryDate = issuedDate.plusYears(3)
            val currentDate = LocalDate.now()
            
            currentDate.isAfter(expiryDate)
        } catch (e: Exception) {
            // If we can't parse the date, consider it expired for safety
            true
        }
    }
}

/**
 * Result of certification validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String,
    val validCertifications: List<Certification>
) 