package app.forku.data.api.dto.certification

import com.google.gson.annotations.SerializedName

data class CertificationDto(
    @SerializedName("Id")
    val id: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Description")
    val description: String?,
    @SerializedName("IssuedDate")
    val issuedDate: String,
    @SerializedName("ExpiryDate")
    val expiryDate: String?,
    @SerializedName("Status")
    val status: Int,
    @SerializedName("GOUserId")
    val goUserId: String?,
    @SerializedName("Issuer")
    val issuer: String,
    @SerializedName("CertificationCode")
    val certificationCode: String?,
    @SerializedName("Timestamp")
    val timestamp: String,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("IsDirty")
    val isDirty: Boolean = false,
    @SerializedName("IsNew")
    val isNew: Boolean = false,
    @SerializedName("InternalObjectId")
    val internalObjectId: Int = 0,
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("SiteId")
    val siteId: String? = null // ✅ Add siteId for multitenancy
)


//data class CertificationDto(
//    val vehicleTypeId: String,
//    val isValid: Boolean,
//    val expiresAt: String,
//    val issuedAt: String? = null,
//    val issuedBy: String? = null,
//    val certificationNumber: String? = null,
//    val notes: String? = null,
//    val metadata: Map<String, String>? = null
//)