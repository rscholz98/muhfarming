package com.mobile.sap.data.api

import com.mobile.sap.data.api.dto.AlertDto
import com.mobile.sap.data.api.dto.AlertRequest
import com.mobile.sap.data.api.dto.AuthRequest
import com.mobile.sap.data.api.dto.CropDto
import com.mobile.sap.data.api.dto.CultivationDto
import com.mobile.sap.data.api.dto.CultivationGuidelineDto
import com.mobile.sap.data.api.dto.CultivationGuidelineRequest
import com.mobile.sap.data.api.dto.CultivationRequest
import com.mobile.sap.data.api.dto.CultivationRiskDto
import com.mobile.sap.data.api.dto.CultivationRiskRequest
import com.mobile.sap.data.api.dto.FarmDto
import com.mobile.sap.data.api.dto.FarmRequest
import com.mobile.sap.data.api.dto.FertilizerDto
import com.mobile.sap.data.api.dto.FieldCoordinateDto
import com.mobile.sap.data.api.dto.FieldCoordinateRequest
import com.mobile.sap.data.api.dto.FieldDto
import com.mobile.sap.data.api.dto.FieldRequest
import com.mobile.sap.data.api.dto.HazardDto
import com.mobile.sap.data.api.dto.HazardRequest
import com.mobile.sap.data.api.dto.IncidentDto
import com.mobile.sap.data.api.dto.IncidentRequest
import com.mobile.sap.data.api.dto.RegionDto
import com.mobile.sap.data.api.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * muhfarming backend API. Auth routes are public; all other routes require a
 * Bearer JWT, attached automatically by the auth interceptor in
 * [RetrofitClient].
 */
interface ApiService {

    // ---- Auth (public) ----

    @POST("auth/login")
    suspend fun login(@Body body: AuthRequest): Response<TokenResponse>

    @POST("auth/signup")
    suspend fun signup(@Body body: AuthRequest): Response<TokenResponse>

    // ---- Farms ----

    @GET("farms")
    suspend fun getFarms(): Response<List<FarmDto>>

    @POST("farms")
    suspend fun createFarm(@Body body: FarmRequest): Response<FarmDto>

    @PUT("farms/{id}")
    suspend fun updateFarm(@Path("id") id: Long, @Body body: FarmRequest): Response<FarmDto>

    @DELETE("farms/{id}")
    suspend fun deleteFarm(@Path("id") id: Long): Response<Unit>

    // ---- Regions (reference data) ----

    @GET("regions")
    suspend fun getRegions(): Response<List<RegionDto>>

    // ---- Fields ----

    @GET("fields")
    suspend fun getFields(): Response<List<FieldDto>>

    @POST("fields")
    suspend fun createField(@Body body: FieldRequest): Response<FieldDto>

    @PUT("fields/{id}")
    suspend fun updateField(@Path("id") id: Long, @Body body: FieldRequest): Response<FieldDto>

    @DELETE("fields/{id}")
    suspend fun deleteField(@Path("id") id: Long): Response<Unit>

    // ---- Field coordinates ----

    @GET("field-coordinates")
    suspend fun getFieldCoordinates(): Response<List<FieldCoordinateDto>>

    @POST("field-coordinates")
    suspend fun createFieldCoordinate(
        @Body body: FieldCoordinateRequest
    ): Response<FieldCoordinateDto>

    // ---- Cultivations (reference data; writes admin-only) ----

    @GET("cultivations")
    suspend fun getCultivations(): Response<List<CultivationDto>>

    @POST("cultivations")
    suspend fun createCultivation(@Body body: CultivationRequest): Response<CultivationDto>

    @PUT("cultivations/{id}")
    suspend fun updateCultivation(
        @Path("id") id: Long, @Body body: CultivationRequest
    ): Response<CultivationDto>

    @DELETE("cultivations/{id}")
    suspend fun deleteCultivation(@Path("id") id: Long): Response<Unit>

    // ---- Cultivation guidelines (writes admin-only) ----

    @GET("cultivation-guidelines")
    suspend fun getCultivationGuidelines(): Response<List<CultivationGuidelineDto>>

    @POST("cultivation-guidelines")
    suspend fun createCultivationGuideline(
        @Body body: CultivationGuidelineRequest
    ): Response<CultivationGuidelineDto>

    @PUT("cultivation-guidelines/{id}")
    suspend fun updateCultivationGuideline(
        @Path("id") id: Long, @Body body: CultivationGuidelineRequest
    ): Response<CultivationGuidelineDto>

    @DELETE("cultivation-guidelines/{id}")
    suspend fun deleteCultivationGuideline(@Path("id") id: Long): Response<Unit>

    // ---- Cultivation risks (writes admin-only) ----

    @GET("cultivation-risks")
    suspend fun getCultivationRisks(): Response<List<CultivationRiskDto>>

    @POST("cultivation-risks")
    suspend fun createCultivationRisk(
        @Body body: CultivationRiskRequest
    ): Response<CultivationRiskDto>

    @PUT("cultivation-risks/{id}")
    suspend fun updateCultivationRisk(
        @Path("id") id: Long, @Body body: CultivationRiskRequest
    ): Response<CultivationRiskDto>

    @DELETE("cultivation-risks/{id}")
    suspend fun deleteCultivationRisk(@Path("id") id: Long): Response<Unit>

    // ---- Hazards (writes admin-only) ----

    @GET("hazards")
    suspend fun getHazards(): Response<List<HazardDto>>

    @POST("hazards")
    suspend fun createHazard(@Body body: HazardRequest): Response<HazardDto>

    @PUT("hazards/{id}")
    suspend fun updateHazard(@Path("id") id: Long, @Body body: HazardRequest): Response<HazardDto>

    @DELETE("hazards/{id}")
    suspend fun deleteHazard(@Path("id") id: Long): Response<Unit>

    // ---- Incidents (writes admin-only) ----

    @GET("incidents")
    suspend fun getIncidents(): Response<List<IncidentDto>>

    @POST("incidents")
    suspend fun createIncident(@Body body: IncidentRequest): Response<IncidentDto>

    @PUT("incidents/{id}")
    suspend fun updateIncident(
        @Path("id") id: Long, @Body body: IncidentRequest
    ): Response<IncidentDto>

    @DELETE("incidents/{id}")
    suspend fun deleteIncident(@Path("id") id: Long): Response<Unit>

    // ---- Alerts (writes admin-only) ----

    @GET("alerts")
    suspend fun getAlerts(): Response<List<AlertDto>>

    @POST("alerts")
    suspend fun createAlert(@Body body: AlertRequest): Response<AlertDto>

    @PUT("alerts/{id}")
    suspend fun updateAlert(@Path("id") id: Long, @Body body: AlertRequest): Response<AlertDto>

    @DELETE("alerts/{id}")
    suspend fun deleteAlert(@Path("id") id: Long): Response<Unit>

    // ---- Fertilizers (read-only in UI) ----

    @GET("fertilizers")
    suspend fun getFertilizers(): Response<List<FertilizerDto>>

    // ---- Crops ----

    @GET("crops")
    suspend fun getCrops(): Response<List<CropDto>>
}
