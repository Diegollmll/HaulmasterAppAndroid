package app.forku.data.api

import app.forku.data.api.dto.business.UserBusinessAssignmentDto
import retrofit2.Response
import retrofit2.http.*

interface UserBusinessApi {
    @GET("api/userbusiness/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getUserBusinessAssignments(): Response<List<UserBusinessAssignmentDto>>

    @GET("api/userbusiness/byid/{businessId}/{userId}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getUserBusinessAssignment(
        @Path("businessId") businessId: String,
        @Path("userId") userId: String
    ): Response<UserBusinessAssignmentDto>

    @POST("api/userbusiness")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun assignUserToBusiness(
        @Body assignment: UserBusinessAssignmentDto
    ): Response<UserBusinessAssignmentDto>

    @DELETE("api/userbusiness/{businessId}/{userId}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun removeUserFromBusiness(
        @Path("businessId") businessId: String,
        @Path("userId") userId: String
    ): Response<Unit>
}

data class UserBusinessAssignmentDto(
    val businessId: String,
    val userId: String,
    val role: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null
) 