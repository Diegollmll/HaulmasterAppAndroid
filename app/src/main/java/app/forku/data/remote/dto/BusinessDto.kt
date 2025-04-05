package app.forku.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BusinessDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("total_users")
    val totalUsers: Int,
    
    @SerializedName("total_vehicles")
    val totalVehicles: Int,
    
    @SerializedName("status")
    val status: String
) 