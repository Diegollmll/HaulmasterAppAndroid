package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistMetadataVehicleTypeDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistMetadataVehicleTypeApi {
    @GET("dataset/api/checklistmetadatavehicletype/byid/{id}")
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistMetadataVehicleTypeDto>

    @GET("api/checklistmetadatavehicletype/byid/{id}")
    suspend fun getById(@Path("id") id: String): Response<ChecklistMetadataVehicleTypeDto>

    @GET("dataset/api/checklistmetadatavehicletype/list")
    suspend fun getListDataset(): Response<List<ChecklistMetadataVehicleTypeDto>>

    @GET("api/checklistmetadatavehicletype/list")
    suspend fun getList(): Response<List<ChecklistMetadataVehicleTypeDto>>

    @GET("dataset/api/checklistmetadatavehicletype/count")
    suspend fun getCount(): Response<Int>

    @POST("dataset/api/checklistmetadatavehicletype")
    suspend fun save(@Body metadata: ChecklistMetadataVehicleTypeDto): Response<ChecklistMetadataVehicleTypeDto>

    @DELETE("dataset/api/checklistmetadatavehicletype")
    suspend fun delete(@Body metadata: ChecklistMetadataVehicleTypeDto): Response<Unit>
} 