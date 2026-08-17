package com.mobile.sap.data.repository

import com.mobile.sap.data.api.ApiService
import com.mobile.sap.data.api.RetrofitClient
import com.mobile.sap.data.api.dto.FarmRequest
import com.mobile.sap.data.model.Coordinate
import com.mobile.sap.data.model.Field
import com.mobile.sap.util.FieldMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fields data backed by the muhfarming backend. Reads combine `GET /fields`
 * with `GET /field-coordinates` (grouped by fieldId); writes go through
 * `POST/PUT/DELETE /fields` and `POST /field-coordinates`. The JWT is attached
 * automatically by the auth interceptor in [RetrofitClient].
 */
class FieldRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    /**
     * Fetch all of the caller's fields, hydrated with their coordinates.
     * Optionally filter by region name (matched against the UI region label).
     */
    suspend fun getFields(region: String? = null): Result<List<Field>> =
        withContext(Dispatchers.IO) {
            try {
                val fieldsResp = api.getFields()
                if (!fieldsResp.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Failed to load fields (${fieldsResp.code()})")
                    )
                }
                val coordsResp = api.getFieldCoordinates()
                val coordinates = if (coordsResp.isSuccessful) {
                    coordsResp.body().orEmpty()
                } else {
                    emptyList()
                }

                val fields = fieldsResp.body().orEmpty().map { dto ->
                    FieldMapper.toUiField(dto, coordinates)
                }

                val filtered = if (region != null) {
                    fields.filter { it.region.equals(region, ignoreCase = true) }
                } else {
                    fields
                }
                Result.success(filtered)
            } catch (e: Exception) {
                Result.failure(Exception("Network error while loading fields."))
            }
        }

    /** Fields whose polygon centroid is within [radiusKm] of the given point. */
    suspend fun getFieldsNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Result<List<Field>> = getFields().map { fields ->
        fields.filter { field ->
            if (field.coordinates.isEmpty()) return@filter false
            val avgLat = field.coordinates.map { it.latitude }.average()
            val avgLng = field.coordinates.map { it.longitude }.average()
            calculateDistance(latitude, longitude, avgLat, avgLng) <= radiusKm
        }
    }

    /**
     * Create a field on the given farm: `POST /fields` then
     * `POST /field-coordinates` for each selected corner. Falls back to
     * [ensureFarm] only when no farm was chosen (e.g. legacy callers).
     */
    suspend fun createField(field: Field, farmId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resolvedFarmId = farmId.takeIf { it > 0 } ?: ensureFarm()
            ?: return@withContext Result.failure(
                Exception("Could not find or create a farm for this account.")
            )

            val fieldResp = api.createField(FieldMapper.toFieldRequest(field, resolvedFarmId))
            val created = fieldResp.body()
            if (!fieldResp.isSuccessful || created == null) {
                return@withContext Result.failure(
                    Exception("Failed to create field (${fieldResp.code()})")
                )
            }

            FieldMapper.toCoordinateRequests(field, created.ID).forEach { req ->
                api.createFieldCoordinate(req)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Network error while creating field."))
        }
    }

    /**
     * Update a field's name/notes/region, keeping it on its current farm.
     * Coordinate editing is not exposed by the UI, so coordinates are left
     * untouched.
     */
    suspend fun updateField(field: Field): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = field.id.toLongOrNull()
                ?: return@withContext Result.failure(Exception("Invalid field id."))
            // Preserve the field's existing farm; fall back to the caller's farm.
            val currentFarmId = api.getFields().body()
                ?.firstOrNull { it.ID == id }?.farmId?.takeIf { it > 0 }
                ?: ensureFarm()
                ?: return@withContext Result.failure(
                    Exception("Could not resolve the farm for this field.")
                )
            val resp = api.updateField(id, FieldMapper.toFieldRequest(field, currentFarmId))
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to update field (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while updating field."))
        }
    }

    /** Delete a field by its (numeric) id. */
    suspend fun deleteField(fieldId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = fieldId.toLongOrNull()
                ?: return@withContext Result.failure(Exception("Invalid field id."))
            val resp = api.deleteField(id)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to delete field (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while deleting field."))
        }
    }

    /**
     * Return the caller's first farm id, creating a default farm if none exist.
     * Returns null only when both the lookup and the creation fail.
     */
    private suspend fun ensureFarm(): Long? {
        val farmsResp = api.getFarms()
        if (farmsResp.isSuccessful) {
            farmsResp.body()?.firstOrNull()?.let { return it.ID }
        }
        val createResp = api.createFarm(FarmRequest(name = "My Farm"))
        return if (createResp.isSuccessful) createResp.body()?.ID else null
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
