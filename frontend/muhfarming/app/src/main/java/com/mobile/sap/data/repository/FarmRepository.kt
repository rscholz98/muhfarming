package com.mobile.sap.data.repository

import com.mobile.sap.data.api.ApiService
import com.mobile.sap.data.api.RetrofitClient
import com.mobile.sap.data.api.dto.FarmDto
import com.mobile.sap.data.api.dto.FarmRequest
import com.mobile.sap.data.event.DataChange
import com.mobile.sap.data.event.DataEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Farms data backed by the muhfarming backend. Farms are owned by the caller
 * (the backend sets `userId` from the JWT), so a farmer fully manages their own
 * farms. The JWT is attached automatically by the auth interceptor in
 * [RetrofitClient].
 */
class FarmRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    suspend fun list(): Result<List<FarmDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getFarms()
            if (resp.isSuccessful) Result.success(resp.body().orEmpty())
            else Result.failure(Exception("Failed to load farms (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading farms."))
        }
    }

    suspend fun create(name: String): Result<FarmDto> = withContext(Dispatchers.IO) {
        try {
            val resp = api.createFarm(FarmRequest(name = name))
            val created = resp.body()
            if (resp.isSuccessful && created != null) {
                DataEvents.emit(DataChange.Farm)
                Result.success(created)
            } else Result.failure(Exception("Failed to create farm (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while creating farm."))
        }
    }

    suspend fun update(id: Long, name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = api.updateFarm(id, FarmRequest(name = name))
            if (resp.isSuccessful) {
                DataEvents.emit(DataChange.Farm)
                Result.success(Unit)
            } else Result.failure(Exception("Failed to update farm (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while updating farm."))
        }
    }

    suspend fun delete(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = api.deleteFarm(id)
            if (resp.isSuccessful) {
                DataEvents.emit(DataChange.Farm)
                Result.success(Unit)
            } else Result.failure(Exception("Failed to delete farm (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while deleting farm."))
        }
    }
}
