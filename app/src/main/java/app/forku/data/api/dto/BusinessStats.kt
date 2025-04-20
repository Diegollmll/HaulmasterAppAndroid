package app.forku.data.api.dto

import com.google.gson.annotations.SerializedName

data class BusinessStats(
    @SerializedName("total_businesses")
    val totalBusinesses: Int = 0,

    @SerializedName("active_businesses")
    val activeBusinesses: Int = 0,

    @SerializedName("pending_businesses")
    val pendingBusinesses: Int = 0,

    @SerializedName("suspended_businesses")
    val suspendedBusinesses: Int = 0,

    @SerializedName("total_users")
    val totalUsers: Int = 0,

    @SerializedName("total_vehicles")
    val totalVehicles: Int = 0,

    @SerializedName("average_users_per_business")
    val averageUsersPerBusiness: Double = 0.0,

    @SerializedName("average_vehicles_per_business")
    val averageVehiclesPerBusiness: Double = 0.0,

    @SerializedName("last_business_created")
    val lastBusinessCreated: String? = null,

    @SerializedName("last_business_updated")
    val lastBusinessUpdated: String? = null,

    @SerializedName("business_growth_rate")
    val businessGrowthRate: Double = 0.0,

    @SerializedName("user_growth_rate")
    val userGrowthRate: Double = 0.0,

    @SerializedName("vehicle_growth_rate")
    val vehicleGrowthRate: Double = 0.0,

    @SerializedName("most_active_business")
    val mostActiveBusiness: String? = null,

    @SerializedName("most_vehicles_business")
    val mostVehiclesBusiness: String? = null
) 