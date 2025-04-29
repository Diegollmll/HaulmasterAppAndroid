package app.forku.data.api.dto.business

import app.forku.data.api.dto.country.CountryDto
import app.forku.data.api.dto.site.SiteItemDto
import com.google.gson.annotations.SerializedName

data class BusinessItemDto(
    @SerializedName("Id")
    val id: String,

    @SerializedName("Name")
    val name: String,

    @SerializedName("Status")
    val status: Int,

    @SerializedName("BusinessConfigurationId")
    val businessConfigurationId: String? = null,

    @SerializedName("CountryId")
    val countryId: String? = null,

    @SerializedName("CountryStateId")
    val countryStateId: String? = null,

    @SerializedName("_businessConfiguration_NewObjectId")
    val businessConfigurationNewObjectId: Int = 0,

    @SerializedName("_country_NewObjectId")
    val countryNewObjectId: Int = 0,

    @SerializedName("_stateProvince_NewObjectId")
    val stateProvinceNewObjectId: Int = 0,

    @SerializedName("businessConfiguration")
    val businessConfiguration: BusinessConfigurationDto? = null,

    @SerializedName("country")
    val country: CountryDto? = null,

    @SerializedName("siteItems")
    val siteItems: List<SiteItemDto> = emptyList(),

    @SerializedName("isMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,

    @SerializedName("isDirty")
    val isDirty: Boolean = false,

    @SerializedName("isNew")
    val isNew: Boolean = false,

    @SerializedName("internalObjectId")
    val internalObjectId: Int = 0,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("createdBy")
    val createdBy: String? = null,

    @SerializedName("updatedBy")
    val updatedBy: String? = null,

    @SerializedName("settings")
    val settings: Map<String, String>? = null,

    @SerializedName("metadata")
    val metadata: Map<String, String>? = null,

    @SerializedName("superAdminId")
    val superAdminId: String? = null
) 