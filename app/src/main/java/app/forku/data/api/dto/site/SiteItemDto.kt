package app.forku.data.api.dto.site

import com.google.gson.annotations.SerializedName

data class SiteItemDto(
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Address")
    val address: String? = null,
    
    @SerializedName("BusinessId")
    val businessId: String,
    
    @SerializedName("CountryId")
    val countryId: String,
    
    @SerializedName("CountryStateId")
    val countryStateId: String,
    
    @SerializedName("TimezoneId")
    val timezoneId: String? = null,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("_business_NewObjectId")
    val businessNewObjectId: Int = 0,
    
    @SerializedName("_country_NewObjectId")
    val countryNewObjectId: Int = 0,
    
    @SerializedName("_stateProvince_NewObjectId")
    val stateProvinceNewObjectId: Int = 0,
    
    @SerializedName("_timezone_NewObjectId")
    val timezoneNewObjectId: Int = 0,
    
    @SerializedName("business")
    val business: String? = null,
    
    @SerializedName("country")
    val country: String? = null,
    
    @SerializedName("incidentItems")
    val incidentItems: List<String> = emptyList(),
    
    @SerializedName("isMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("isDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("isNew")
    val isNew: Boolean = false,
    
    @SerializedName("internalObjectId")
    val internalObjectId: Int = 0
) 