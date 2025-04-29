package app.forku.data.api

import app.forku.data.api.dto.gogroup.GOGroupRoleDto
import retrofit2.Response
import retrofit2.http.*

interface GOGroupRoleApi {
    /**
     * Get a GOGroupRole instance by primary key and return data in dataset format
     */
    @GET("dataset/api/gogrouprole/byid/{gOGroupName}/{gORoleName}")
    suspend fun getGroupRoleByIdDataset(
        @Path("gOGroupName") gOGroupName: String,
        @Path("gORoleName") gORoleName: String
    ): Response<GOGroupRoleDto>

    /**
     * Get a GOGroupRole instance by primary key and return data in json structured format
     */
    @GET("api/gogrouprole/byid/{gOGroupName}/{gORoleName}")
    suspend fun getGroupRoleById(
        @Path("gOGroupName") gOGroupName: String,
        @Path("gORoleName") gORoleName: String
    ): Response<GOGroupRoleDto>

    /**
     * Get a list of GOGroupRole instances and return data in dataset format
     */
    @GET("dataset/api/gogrouprole/list")
    suspend fun getGroupRolesDataset(): Response<List<GOGroupRoleDto>>

    /**
     * Get a list of GOGroupRole instances and return data in json structured format
     */
    @GET("api/gogrouprole/list")
    suspend fun getGroupRoles(): Response<List<GOGroupRoleDto>>

    /**
     * Count the number of GOGroupRole instances
     */
    @GET("dataset/api/gogrouprole/count")
    suspend fun getGroupRoleCount(): Response<Int>

    /**
     * Save (create or update if existing) a given GOGroupRole instance in dataset format
     */
    @POST("dataset/api/gogrouprole")
    suspend fun saveGroupRoleDataset(@Body groupRole: GOGroupRoleDto): Response<GOGroupRoleDto>

    /**
     * Delete a given GOGroupRole instance in dataset format
     */
    @DELETE("dataset/api/gogrouprole")
    suspend fun deleteGroupRoleDataset(@Body groupRole: GOGroupRoleDto): Response<Unit>

    /**
     * Save (create or update if existing) a given GOGroupRole instance in json format
     */
    @POST("api/gogrouprole")
    suspend fun saveGroupRole(@Body groupRole: GOGroupRoleDto): Response<GOGroupRoleDto>

    /**
     * Delete a given GOGroupRole instance by group name and role name
     */
    @DELETE("dataset/api/gogrouprole/{gOGroupName}/{gORoleName}")
    suspend fun deleteGroupRole(
        @Path("gOGroupName") gOGroupName: String,
        @Path("gORoleName") gORoleName: String
    ): Response<Unit>
} 