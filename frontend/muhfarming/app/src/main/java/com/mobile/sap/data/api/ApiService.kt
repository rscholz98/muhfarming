package com.mobile.sap.data.api

import com.mobile.sap.data.model.PestInfoResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("pest-info")
    suspend fun getPestInfo(): Response<PestInfoResponse>
}
