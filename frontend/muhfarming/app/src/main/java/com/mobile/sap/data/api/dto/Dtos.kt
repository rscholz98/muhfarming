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

// ---- Cultivation (reference data) ----

data class CultivationDto(
    val ID: Long = 0,
    val name: String? = null,
    val estTimeToHarvestWeeks: Int = 0
)

data class CultivationRequest(
    val name: String,
    val estTimeToHarvestWeeks: Int = 0
)

// ---- Hazard (reference data) ----

data class HazardDto(
    val ID: Long = 0,
    val name: String? = null,
    val description: String? = null
)

data class HazardRequest(
    val name: String,
    val description: String
)

// ---- Fertilizer (reference data, read-only in UI) ----

data class FertilizerDto(
    val ID: Long = 0,
    val name: String? = null
)

// ---- CultivationGuideline ----

data class CultivationGuidelineDto(
    val ID: Long = 0,
    val type: String? = null,
    val weekFrom: Int = 0,
    val weekTo: Int = 0,
    val instructions: String? = null,
    val cultivationId: Long = 0,
    val fertilizerId: Long? = null,
    val fertilizer: FertilizerDto? = null
)

data class CultivationGuidelineRequest(
    val type: String? = null,
    val weekFrom: Int = 0,
    val weekTo: Int = 0,
    val instructions: String? = null,
    val cultivationId: Long,
    val fertilizerId: Long? = null
)

// ---- CultivationRisk ----

data class CultivationRiskDto(
    val ID: Long = 0,
    val weekFrom: Int = 0,
    val weekTo: Int = 0,
    val solution: String? = null,
    val cultivationId: Long = 0,
    val hazardId: Long = 0,
    val hazard: HazardDto? = null
)

data class CultivationRiskRequest(
    val weekFrom: Int = 0,
    val weekTo: Int = 0,
    val solution: String? = null,
    val cultivationId: Long,
    val hazardId: Long
)

// ---- Incident ----

data class IncidentDto(
    val ID: Long = 0,
    val date: String? = null,
    val priority: String? = null,
    val description: String? = null,
    val cultivationRiskId: Long = 0,
    val regionId: Long = 0,
    val region: RegionDto? = null
)

data class IncidentRequest(
    val date: String? = null,
    val priority: String? = null,
    val description: String? = null,
    val cultivationRiskId: Long,
    val regionId: Long
)

// ---- Alert ----

data class AlertDto(
    val ID: Long = 0,
    val fieldId: Long = 0,
    val field: FieldDto? = null,
    val incidentId: Long = 0,
    val incident: IncidentDto? = null
)

data class AlertRequest(
    val fieldId: Long,
    val incidentId: Long
)

// ---- Crop ----

data class CropDto(
    val ID: Long = 0,
    val status: String? = null,
    val datePlanted: String? = null,
    val lastUpdated: String? = null,
    val fieldId: Long = 0,
    val cultivationId: Long = 0
)
