package app.forku.data.api.dto.business

import com.google.gson.annotations.SerializedName

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