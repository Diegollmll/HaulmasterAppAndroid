package app.forku.data.api.dto.country

import app.forku.data.api.dto.business.BusinessItemDto
import app.forku.data.api.dto.site.SiteItemDto
import app.forku.domain.model.country.CountryState
import com.google.gson.annotations.SerializedName

data class CountryStateDto(
    @SerializedName("\$type")
    val type: String = "CountryStateDataObject",
    
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Code")
    val code: String,
    
    @SerializedName("CountryId")
    val countryId: String,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("_country_NewObjectId")
    val countryNewObjectId: Int = 0,
    
    @SerializedName("businessItems")
    val businessItems: List<BusinessItemDto> = emptyList(),
    
    @SerializedName("country")
    val country: CountryDto? = null,
    
    @SerializedName("siteItems")
    val siteItems: List<SiteItemDto> = emptyList(),
    
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("isDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("isNew")
    val isNew: Boolean = false,
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int = 0
) {
    fun toDomain(): CountryState = CountryState(
        id = id,
        countryId = countryId,
        name = name,
        code = code,
        isActive = isActive
    )
}

fun CountryState.toDto(): CountryStateDto = CountryStateDto(
    id = id,
    countryId = countryId,
    name = name,
    code = code,
    isActive = isActive,
    isDirty = true
) 