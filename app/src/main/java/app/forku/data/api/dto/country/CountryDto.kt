package app.forku.data.api.dto.country

import app.forku.domain.model.country.Country
import app.forku.data.api.dto.site.SiteItemDto
import com.google.gson.annotations.SerializedName

data class CountryDto(
    @SerializedName("\$type")
    val type: String = "CountryDataObject",
    
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Code")
    val code: String,
    
    @SerializedName("PhoneCode")
    val phoneCode: String,
    
    @SerializedName("Currency")
    val currency: String,
    
    @SerializedName("CurrencySymbol")
    val currencySymbol: String,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("businessItems")
    val businessItems: List<BusinessItemDto> = emptyList(),
    
    @SerializedName("countryStateItems")
    val countryStateItems: List<CountryStateDto> = emptyList(),
    
    @SerializedName("siteItems")
    val siteItems: List<String> = emptyList(),
    
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("isDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("isNew")
    val isNew: Boolean = false,
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int = 0
) {
    fun toDomain(): Country = Country(
        id = id,
        name = name,
        code = code,
        phoneCode = phoneCode,
        currency = currency,
        currencySymbol = currencySymbol,
        isActive = isActive
    )
}

data class BusinessItemDto(
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Status")
    val status: Int = 0,
    
    @SerializedName("BusinessConfigurationId")
    val businessConfigurationId: String,
    
    @SerializedName("CountryId")
    val countryId: String,
    
    @SerializedName("CountryStateId")
    val countryStateId: String,
    
    @SerializedName("_businessConfiguration_NewObjectId")
    val businessConfigurationNewObjectId: Int = 0,
    
    @SerializedName("_country_NewObjectId")
    val countryNewObjectId: Int = 0,
    
    @SerializedName("_stateProvince_NewObjectId")
    val stateProvinceNewObjectId: Int = 0,
    
    @SerializedName("businessConfiguration")
    val businessConfiguration: BusinessConfigurationDto? = null,
    
    @SerializedName("country")
    val country: String? = null,
    
    @SerializedName("siteItems")
    val siteItems: List<SiteItemDto> = emptyList(),
    
    @SerializedName("isMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("isDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("isNew")
    val isNew: Boolean = false,
    
    @SerializedName("internalObjectId")
    val internalObjectId: Int = 0
)

data class BusinessConfigurationDto(
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("BusinessLogo")
    val businessLogo: String? = null,
    
    @SerializedName("BusinessLogoFileSize")
    val businessLogoFileSize: Int = 0,
    
    @SerializedName("BusinessLogoInternalName")
    val businessLogoInternalName: String? = null,
    
    @SerializedName("EmailNotificationsEnabled")
    val emailNotificationsEnabled: Boolean = true,
    
    @SerializedName("PushNotificationsEnabled")
    val pushNotificationsEnabled: Boolean = true,
    
    @SerializedName("OperatingHours")
    val operatingHours: String? = null,
    
    @SerializedName("PrimaryColor")
    val primaryColor: String? = null,
    
    @SerializedName("SecondaryColor")
    val secondaryColor: String? = null,
    
    @SerializedName("TimezoneId")
    val timezoneId: String? = null,
    
    @SerializedName("WorkWeekDefinition")
    val workWeekDefinition: String? = null,
    
    @SerializedName("_timezone_NewObjectId")
    val timezoneNewObjectId: Int = 0,
    
    @SerializedName("business")
    val business: String? = null,
    
    @SerializedName("timezone")
    val timezone: TimezoneDto? = null,
    
    @SerializedName("isMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("isDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("isNew")
    val isNew: Boolean = false,
    
    @SerializedName("internalObjectId")
    val internalObjectId: Int = 0
)

data class TimezoneDto(
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Offset")
    val offset: String,
    
    @SerializedName("businessConfiguration")
    val businessConfiguration: String? = null,
    
    @SerializedName("site")
    val site: String? = null,
    
    @SerializedName("isMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("isDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("isNew")
    val isNew: Boolean = false,
    
    @SerializedName("internalObjectId")
    val internalObjectId: Int = 0
)

fun Country.toDto(): CountryDto = CountryDto(
    id = id,
    name = name,
    code = code,
    phoneCode = phoneCode,
    currency = currency,
    currencySymbol = currencySymbol,
    isActive = isActive,
    isDirty = true
) 