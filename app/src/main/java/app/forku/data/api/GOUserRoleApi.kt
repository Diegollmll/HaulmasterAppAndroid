package app.forku.data.api

import app.forku.data.api.dto.gouserrole.GOUserRoleDto
import retrofit2.Response
import retrofit2.http.*

interface GOUserRoleApi {
    /**
     * Get a GOUserRole instance by primary key and return data in dataset format
     */
    @GET("dataset/api/gouserrole/byid/{gORoleName}/{gOUserId}")
    suspend fun getUserRoleByIdDataset(
        @Path("gORoleName") gORoleName: String,
        @Path("gOUserId") gOUserId: String
    ): Response<GOUserRoleDto>

    /**
     * Get a GOUserRole instance by primary key and return data in json structured format
     */
    @GET("api/gouserrole/byid/{gORoleName}/{gOUserId}")
    suspend fun getUserRoleById(
        @Path("gORoleName") gORoleName: String,
        @Path("gOUserId") gOUserId: String
    ): Response<GOUserRoleDto>

    /**
     * Get a list of GOUserRole instances and return data in dataset format
     */
    @GET("dataset/api/gouserrole/list")
    suspend fun getUserRolesDataset(): Response<List<GOUserRoleDto>>

    /**
     * Get a list of GOUserRole instances and return data in json structured format
     */
    @GET("api/gouserrole/list")
    suspend fun getUserRoles(): Response<List<GOUserRoleDto>>

    /**
     * Count the number of GOUserRole instances
     */
    @GET("dataset/api/gouserrole/count")
    suspend fun getUserRoleCount(): Response<Int>

    /**
     * Save (create or update if existing) a given GOUserRole instance in dataset format
     */
    @POST("dataset/api/gouserrole")
    suspend fun saveUserRoleDataset(@Body userRole: GOUserRoleDto): Response<GOUserRoleDto>

    /**
     * Delete a given GOUserRole instance in dataset format
     */
    @DELETE("dataset/api/gouserrole")
    suspend fun deleteUserRoleDataset(@Body userRole: GOUserRoleDto): Response<Unit>

    /**
     * Save (create or update if existing) a given GOUserRole instance in json format
     */
    @POST("api/gouserrole")
    suspend fun saveUserRole(@Body userRole: GOUserRoleDto): Response<GOUserRoleDto>

    /**
     * Delete a given GOUserRole instance by role name and user ID
     */
    @DELETE("dataset/api/gouserrole/{gORoleName}/{gOUserId}")
    suspend fun deleteUserRole(
        @Path("gORoleName") gORoleName: String,
        @Path("gOUserId") gOUserId: String
    ): Response<Unit>
} 