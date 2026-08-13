package com.mobile.sap.data.api

import com.mobile.sap.data.api.dto.AuthRequest
import com.mobile.sap.data.api.dto.FarmDto
import com.mobile.sap.data.api.dto.FarmRequest
import com.mobile.sap.data.api.dto.FieldCoordinateDto
import com.mobile.sap.data.api.dto.FieldCoordinateRequest
import com.mobile.sap.data.api.dto.FieldDto
import com.mobile.sap.data.api.dto.FieldRequest
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
}
