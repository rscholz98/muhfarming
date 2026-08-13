package com.mobile.sap.data.api.dto

/**
 * Wire DTOs mirroring the muhfarming backend exactly. Kept separate from the
 * UI models in `data/model` so the map/UI layer is insulated from the
 * normalized backend shape. Field names use the backend's JSON keys:
 * capitalized base keys (`ID`, `CreatedAt`, ...) and camelCase domain fields.
 */

// ---- Auth ----

data class AuthRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val token: String,
    val role: String
)

// ---- Farm ----

data class FarmDto(
    val ID: Long = 0,
    val name: String? = null,
    val userId: Long = 0
)

data class FarmRequest(
    val name: String
)

// ---- Field ----

data class FieldDto(
    val ID: Long = 0,
    val name: String? = null,
    val fieldNotes: String? = null,
    val farmId: Long = 0,
    val regionId: Long = 0,
    val region: RegionDto? = null
)

data class RegionDto(
    val ID: Long = 0,
    val name: String? = null,
    val geoCode: String? = null
)

data class FieldRequest(
    val name: String,
    val fieldNotes: String? = null,
    val farmId: Long,
    val regionId: Long
)

// ---- FieldCoordinate ----

data class FieldCoordinateDto(
    val ID: Long = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val sequenceOrder: Int = 0,
    val fieldId: Long = 0
)

data class FieldCoordinateRequest(
    val latitude: Double,
    val longitude: Double,
    val sequenceOrder: Int,
    val fieldId: Long
)
