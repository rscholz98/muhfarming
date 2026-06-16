package com.mobile.sap.data.model

data class Coordinate(
    val latitude: Double,
    val longitude: Double
)

data class Incident(
    val id: String,
    val type: String,
    val description: String,
    val reportedAt: String
)

data class Hazard(
    val id: String,
    val name: String,
    val severity: HazardSeverity
)

enum class HazardSeverity {
    LOW, MEDIUM, HIGH
}

data class Fertilizer(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String
)

data class Cultivation(
    val cropType: String,
    val season: String,
    val status: String
)

enum class CultivationRisk {
    LOW, MEDIUM, HIGH
}

data class Field(
    val id: String,
    val region: String,
    val coordinates: List<Coordinate>,
    val incidents: List<Incident> = emptyList(),
    val hazards: List<Hazard> = emptyList(),
    val fertilizers: List<Fertilizer> = emptyList(),
    val cultivation: Cultivation? = null,
    val cultivationGuideline: String? = null,
    val cultivationRisk: CultivationRisk? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
