package app.forku.data.api.dto

import com.google.gson.annotations.SerializedName

data class EnergySourceDto(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean = true
) 