package app.forku.data.api.dto.site

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("address")
    val address: String,
    
    @SerialName("business_id")
    val businessId: String,
    
    @SerialName("latitude")
    val latitude: Double,
    
    @SerialName("longitude")
    val longitude: Double,
    
    @SerialName("is_active")
    val isActive: Boolean,
    
    @SerialName("created_at")
    val createdAt: String,
    
    @SerialName("updated_at")
    val updatedAt: String
) 