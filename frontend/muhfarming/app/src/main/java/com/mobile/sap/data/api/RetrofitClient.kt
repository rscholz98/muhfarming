package com.mobile.sap.data.api

import com.mobile.sap.data.auth.SessionManager
import com.mobile.sap.data.event.AuthEvents
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client for the muhfarming backend.
 *
 * [init] must be called once (from [com.mobile.sap.MainActivity]) with a
 * [SessionManager] so the auth interceptor can attach the stored JWT to every
 * request. All entity routes require `Authorization: Bearer <token>`.
 */
object RetrofitClient {
    private const val BASE_URL = "http://18.195.50.142:8080/"

    @Volatile
    private var session: SessionManager? = null

    fun init(sessionManager: SessionManager) {
        session = sessionManager
    }

    // Attaches the current JWT (if any) to outgoing requests.
    private val authInterceptor = Interceptor { chain ->
        val token = session?.token
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Detects an expired/invalid session: any 401 on a non-auth route means the
    // stored token is no longer accepted by the backend. Clear it and signal the
    // UI to force a logout. The login/signup routes are exempt — a 401 there is
    // just "bad credentials", not a dead session.
    private val unauthorizedInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (response.code == 401 && !chain.request().isAuthRoute()) {
            session?.clear()
            AuthEvents.emitUnauthorized()
        }
        response
    }

    private fun okhttp3.Request.isAuthRoute(): Boolean {
        val path = url.encodedPath
        return path.endsWith("/auth/login") || path.endsWith("/auth/signup")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
