package app.forku.data.api.dto.business

import app.forku.domain.model.business.BusinessStatus
import com.google.gson.annotations.SerializedName

data class BusinessStats(
    @SerializedName("TotalUsers")
    val totalUsers: Int,
    
    @SerializedName("TotalVehicles")
    val totalVehicles: Int,
    
    @SerializedName("ActiveUsers")
    val activeUsers: Int,
    
    @SerializedName("ActiveVehicles")
    val activeVehicles: Int,
    
    @SerializedName("AdminCount")
    val adminCount: Int,
    
    @SerializedName("OperatorCount")
    val operatorCount: Int,
    
    @SerializedName("BusinessStatus")
    val businessStatus: BusinessStatus,
    
    @SerializedName("LastUpdated")
    val lastUpdated: Long
) 