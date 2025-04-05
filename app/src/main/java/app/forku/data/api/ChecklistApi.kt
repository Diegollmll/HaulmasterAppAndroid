package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistResponseDto
import app.forku.data.api.dto.checklist.PreShiftCheckDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistApi {
    @GET("checklist_questionary")
    suspend fun getChecklistQuestionary(): Response<ChecklistResponseDto>

    @GET("checks")
    suspend fun getAllChecks(@Query("businessId") businessId: String): Response<List<PreShiftCheckDto>>

    @GET("checks/{checkId}")
    suspend fun getCheckById(
        @Path("checkId") checkId: String
    ): Response<PreShiftCheckDto>

    @POST("checks")
    suspend fun createGlobalCheck(
        @Body check: PreShiftCheckDto
    ): Response<PreShiftCheckDto>

    @PUT("checks/{checkId}")
    suspend fun updateGlobalCheck(
        @Path("checkId") checkId: String,
        @Body check: PreShiftCheckDto
    ): Response<PreShiftCheckDto>
} 