package com.mobile.sap.data.repository

import com.mobile.sap.data.api.ApiService
import com.mobile.sap.data.api.RetrofitClient
import com.mobile.sap.data.api.dto.AuthRequest
import com.mobile.sap.data.auth.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Authenticates against the backend and persists the resulting JWT + role in
 * [SessionManager]. Returns the role on success so callers can route by it.
 */
class AuthRepository(
    private val session: SessionManager,
    private val api: ApiService = RetrofitClient.apiService
) {

    suspend fun login(username: String, password: String): Result<String> =
        authenticate(username, password, signUp = false)

    suspend fun signup(username: String, password: String): Result<String> =
        authenticate(username, password, signUp = true)

    private suspend fun authenticate(
        username: String,
        password: String,
        signUp: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = AuthRequest(username.trim(), password)
            val response = if (signUp) api.signup(body) else api.login(body)
            val token = response.body()
            if (response.isSuccessful && token != null) {
                session.save(token.token, token.role)
                Result.success(token.role)
            } else {
                Result.failure(Exception(errorMessage(response.code(), signUp)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error. Please try again."))
        }
    }

    private fun errorMessage(code: Int, signUp: Boolean): String = when {
        signUp && code == 409 -> "Username already taken"
        signUp && code == 400 -> "Password must be at least 6 characters"
        !signUp && code == 401 -> "Invalid username or password"
        else -> "Request failed ($code)"
    }
}
