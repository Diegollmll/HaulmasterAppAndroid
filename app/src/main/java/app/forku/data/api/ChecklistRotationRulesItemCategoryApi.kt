package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistRotationRulesItemCategoryDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistRotationRulesItemCategoryApi {
    @GET("dataset/api/checklistrotationrulesitemcategory/byid/{id}")
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistRotationRulesItemCategoryDto>

    @GET("api/checklistrotationrulesitemcategory/byid/{id}")
    suspend fun getById(@Path("id") id: String): Response<ChecklistRotationRulesItemCategoryDto>

    @GET("dataset/api/checklistrotationrulesitemcategory/list")
    suspend fun getListDataset(): Response<List<ChecklistRotationRulesItemCategoryDto>>

    @GET("api/checklistrotationrulesitemcategory/list")
    suspend fun getList(): Response<List<ChecklistRotationRulesItemCategoryDto>>

    @GET("dataset/api/checklistrotationrulesitemcategory/count")
    suspend fun getCount(): Response<Int>

    @POST("dataset/api/checklistrotationrulesitemcategory")
    suspend fun save(@Body rules: ChecklistRotationRulesItemCategoryDto): Response<ChecklistRotationRulesItemCategoryDto>

    @DELETE("dataset/api/checklistrotationrulesitemcategory")
    suspend fun delete(@Body rules: ChecklistRotationRulesItemCategoryDto): Response<Unit>
} 