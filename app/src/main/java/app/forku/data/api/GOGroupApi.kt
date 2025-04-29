package app.forku.data.api

import app.forku.data.api.dto.gogroup.GOGroupDto
import retrofit2.Response
import retrofit2.http.*

interface GOGroupApi {
    /**
     * Get a GOGroup instance by primary key and return data in dataset format
     */
    @GET("dataset/api/gogroup/byid/{name}")
    suspend fun getGroupByIdDataset(@Path("name") name: String): Response<GOGroupDto>

    /**
     * Get a GOGroup instance by primary key and return data in json structured format
     */
    @GET("api/gogroup/byid/{name}")
    suspend fun getGroupById(@Path("name") name: String): Response<GOGroupDto>

    /**
     * Get a list of GOGroup instances and return data in dataset format
     */
    @GET("dataset/api/gogroup/list")
    suspend fun getGroupsDataset(): Response<List<GOGroupDto>>

    /**
     * Get a list of GOGroup instances and return data in json structured format
     */
    @GET("api/gogroup/list")
    suspend fun getGroups(): Response<List<GOGroupDto>>

    /**
     * Count the number of GOGroup instances
     */
    @GET("dataset/api/gogroup/count")
    suspend fun getGroupCount(): Response<Int>

    /**
     * Save (create or update if existing) a given GOGroup instance in dataset format
     */
    @POST("dataset/api/gogroup")
    suspend fun saveGroupDataset(@Body group: GOGroupDto): Response<GOGroupDto>

    /**
     * Delete a given GOGroup instance in dataset format
     */
    @DELETE("dataset/api/gogroup")
    suspend fun deleteGroupDataset(@Body group: GOGroupDto): Response<Unit>

    /**
     * Save (create or update if existing) a given GOGroup instance in json format
     */
    @POST("api/gogroup")
    suspend fun saveGroup(@Body group: GOGroupDto): Response<GOGroupDto>

    /**
     * Delete a given GOGroup instance by name
     */
    @DELETE("dataset/api/gogroup/{name}")
    suspend fun deleteGroup(@Path("name") name: String): Response<Unit>
} 