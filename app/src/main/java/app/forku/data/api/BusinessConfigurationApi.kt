package app.forku.data.api

import app.forku.data.api.dto.business.BusinessConfigurationDto
import retrofit2.Response
import retrofit2.http.*

interface BusinessConfigurationApi {
    @GET("dataset/api/businessconfiguration/byid/{id}")
    suspend fun getBusinessConfigurationByIdDataset(@Path("id") id: String): Response<BusinessConfigurationDto>

    @GET("api/businessconfiguration/byid/{id}")
    suspend fun getBusinessConfigurationById(@Path("id") id: String): Response<BusinessConfigurationDto>

    @GET("dataset/api/businessconfiguration/list")
    suspend fun getBusinessConfigurationsDataset(): Response<List<BusinessConfigurationDto>>

    @GET("api/businessconfiguration/list")
    suspend fun getBusinessConfigurations(): Response<List<BusinessConfigurationDto>>

    @GET("dataset/api/businessconfiguration/count")
    suspend fun getBusinessConfigurationCount(): Response<Int>

    @GET("api/businessconfiguration/file/{id}/BusinessLogo")
    suspend fun getBusinessLogo(@Path("id") id: String): Response<okhttp3.ResponseBody>

    @POST("dataset/api/businessconfiguration")
    suspend fun saveBusinessConfigurationDataset(@Body config: BusinessConfigurationDto): Response<BusinessConfigurationDto>

    @POST("api/businessconfiguration")
    suspend fun saveBusinessConfiguration(@Body config: BusinessConfigurationDto): Response<BusinessConfigurationDto>

    @DELETE("dataset/api/businessconfiguration")
    suspend fun deleteBusinessConfigurationDataset(@Body config: BusinessConfigurationDto): Response<Unit>

    @DELETE("dataset/api/businessconfiguration/{id}")
    suspend fun deleteBusinessConfigurationByIdDataset(@Path("id") id: String): Response<Unit>
} 