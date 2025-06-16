package app.forku.data.api.dto.site

import com.google.gson.annotations.SerializedName

data class SiteDto(
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Address")
    val address: String? = null,
    
    @SerializedName("BusinessId")
    val businessId: String,
    
    @SerializedName("Latitude")
    val latitude: Double? = null,
    
    @SerializedName("Longitude")
    val longitude: Double? = null,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("CreatedAt")
    val createdAt: String? = null,
    
    @SerializedName("UpdatedAt")
    val updatedAt: String? = null,
    
    @SerializedName("CountryId")
    val countryId: String? = null,
    
    @SerializedName("CountryStateId")
    val countryStateId: String? = null,
    
    @SerializedName("TimezoneId")
    val timezoneId: String? = null
) 