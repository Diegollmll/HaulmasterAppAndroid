package app.forku.data.api

import app.forku.data.api.dto.checklist.UserChecklistDto
import retrofit2.Response
import retrofit2.http.*

interface UserChecklistApi {
    @GET("dataset/api/userchecklist/byid/{id}")
    suspend fun getByIdDataset(@Path("id") id: String): Response<UserChecklistDto>

    @GET("api/userchecklist/byid/{id}")
    suspend fun getById(@Path("id") id: String): Response<UserChecklistDto>

    @GET("dataset/api/userchecklist/list")
    suspend fun getListDataset(): Response<List<UserChecklistDto>>

    @GET("api/userchecklist/list")
    suspend fun getList(): Response<List<UserChecklistDto>>

    @GET("dataset/api/userchecklist/count")
    suspend fun getCount(): Response<Int>

    @POST("dataset/api/userchecklist")
    suspend fun save(@Body userChecklist: UserChecklistDto): Response<UserChecklistDto>

    @DELETE("dataset/api/userchecklist")
    suspend fun delete(@Body userChecklist: UserChecklistDto): Response<Unit>
} 