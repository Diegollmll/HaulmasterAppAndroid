package app.forku.data.api.dto.goservices

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for GO Services settings
 */
data class GOServicesSettingsDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("value")
    val value: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("isSystem")
    val isSystem: Boolean = false,

    @SerializedName("isReadOnly")
    val isReadOnly: Boolean = false,

    @SerializedName("dataType")
    val dataType: String? = null,

    @SerializedName("validationRules")
    val validationRules: Map<String, Any>? = null,

    @SerializedName("lastModified")
    val lastModified: String? = null,

    @SerializedName("modifiedBy")
    val modifiedBy: String? = null
) 