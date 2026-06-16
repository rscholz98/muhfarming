package com.mobile.sap.data.repository

import com.mobile.sap.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object FieldRepository {

    // Mocked fields data for Cameroon cities - Focused on Yaoundé for visibility
    private val mockFields = listOf(
        // Yaoundé - Coffee Field 1 (North of city center)
        Field(
            id = "field-yaounde-1",
            region = "Centre",
            coordinates = listOf(
                Coordinate(3.870, 11.510),
                Coordinate(3.870, 11.520),
                Coordinate(3.860, 11.520),
                Coordinate(3.860, 11.510),
                Coordinate(3.870, 11.510)
            ),
            incidents = listOf(
                Incident(
                    id = "inc-1",
                    type = "Pest",
                    description = "Coffee berry borer detected",
                    reportedAt = "2026-06-15T08:30:00Z"
                )
            ),
            hazards = listOf(
                Hazard(
                    id = "haz-1",
                    name = "Heavy Rainfall",
                    severity = HazardSeverity.MEDIUM
                )
            ),
            fertilizers = listOf(
                Fertilizer(
                    id = "fert-1",
                    name = "Organic Compost",
                    quantity = 120.0,
                    unit = "kg"
                )
            ),
            cultivation = Cultivation(
                cropType = "Coffee",
                season = "Rainy Season",
                status = "Growing"
            ),
            cultivationGuideline = "Shade-grown coffee requires regular pruning and pest monitoring",
            cultivationRisk = CultivationRisk.MEDIUM,
            createdAt = "2026-01-10T10:00:00Z",
            updatedAt = "2026-06-15T09:30:00Z"
        ),

        // Yaoundé - Plantain Field 2 (East of city center)
        Field(
            id = "field-yaounde-2",
            region = "Centre",
            coordinates = listOf(
                Coordinate(3.855, 11.530),
                Coordinate(3.855, 11.540),
                Coordinate(3.845, 11.540),
                Coordinate(3.845, 11.530),
                Coordinate(3.855, 11.530)
            ),
            incidents = emptyList(),
            hazards = listOf(
                Hazard(
                    id = "haz-2",
                    name = "Soil Erosion",
                    severity = HazardSeverity.LOW
                )
            ),
            fertilizers = listOf(
                Fertilizer(
                    id = "fert-2",
                    name = "Potash",
                    quantity = 80.0,
                    unit = "kg"
                )
            ),
            cultivation = Cultivation(
                cropType = "Plantain",
                season = "Year-round",
                status = "Growing"
            ),
            cultivationGuideline = "Ensure adequate drainage and mulching",
            cultivationRisk = CultivationRisk.LOW,
            createdAt = "2026-02-20T10:00:00Z",
            updatedAt = "2026-06-14T11:30:00Z"
        ),

        // Yaoundé - Cassava Field 3 (South of city center)
        Field(
            id = "field-yaounde-3",
            region = "Centre",
            coordinates = listOf(
                Coordinate(3.830, 11.505),
                Coordinate(3.830, 11.515),
                Coordinate(3.820, 11.515),
                Coordinate(3.820, 11.505),
                Coordinate(3.830, 11.505)
            ),
            incidents = listOf(
                Incident(
                    id = "inc-3",
                    type = "Disease",
                    description = "Cassava mosaic disease spotted",
                    reportedAt = "2026-06-12T14:20:00Z"
                )
            ),
            hazards = listOf(
                Hazard(
                    id = "haz-3",
                    name = "Disease Outbreak",
                    severity = HazardSeverity.HIGH
                )
            ),
            fertilizers = listOf(
                Fertilizer(
                    id = "fert-3",
                    name = "NPK 15-15-15",
                    quantity = 100.0,
                    unit = "kg"
                )
            ),
            cultivation = Cultivation(
                cropType = "Cassava",
                season = "Main Season",
                status = "Mature"
            ),
            cultivationGuideline = "Remove infected plants immediately, apply disease-resistant varieties",
            cultivationRisk = CultivationRisk.HIGH,
            createdAt = "2025-12-05T09:00:00Z",
            updatedAt = "2026-06-12T15:00:00Z"
        ),

        // Yaoundé - Maize Field 4 (West of city center)
        Field(
            id = "field-yaounde-4",
            region = "Centre",
            coordinates = listOf(
                Coordinate(3.850, 11.480),
                Coordinate(3.850, 11.490),
                Coordinate(3.840, 11.490),
                Coordinate(3.840, 11.480),
                Coordinate(3.850, 11.480)
            ),
            incidents = emptyList(),
            hazards = emptyList(),
            fertilizers = listOf(
                Fertilizer(
                    id = "fert-4",
                    name = "Urea",
                    quantity = 150.0,
                    unit = "kg"
                )
            ),
            cultivation = Cultivation(
                cropType = "Maize",
                season = "First Season",
                status = "Flowering"
            ),
            cultivationGuideline = "Monitor for fall armyworm, apply top-dressing fertilizer",
            cultivationRisk = CultivationRisk.MEDIUM,
            createdAt = "2026-03-15T08:30:00Z",
            updatedAt = "2026-06-10T10:15:00Z"
        ),

        // Yaoundé - Vegetable Garden 5 (Northwest)
        Field(
            id = "field-yaounde-5",
            region = "Centre",
            coordinates = listOf(
                Coordinate(3.865, 11.495),
                Coordinate(3.865, 11.502),
                Coordinate(3.858, 11.502),
                Coordinate(3.858, 11.495),
                Coordinate(3.865, 11.495)
            ),
            incidents = emptyList(),
            hazards = emptyList(),
            fertilizers = listOf(
                Fertilizer(
                    id = "fert-5",
                    name = "Organic Compost",
                    quantity = 200.0,
                    unit = "kg"
                )
            ),
            cultivation = Cultivation(
                cropType = "Mixed Vegetables",
                season = "All Year",
                status = "Growing"
            ),
            cultivationGuideline = "Rotate crops every 8 weeks, maintain organic practices",
            cultivationRisk = CultivationRisk.LOW,
            createdAt = "2026-01-20T07:00:00Z",
            updatedAt = "2026-06-15T09:00:00Z"
        ),

        // Douala - Cocoa Field (larger polygon)
        Field(
            id = "field-douala-1",
            region = "Littoral",
            coordinates = listOf(
                Coordinate(4.060, 9.760),
                Coordinate(4.060, 9.775),
                Coordinate(4.045, 9.775),
                Coordinate(4.045, 9.760),
                Coordinate(4.060, 9.760)
            ),
            incidents = emptyList(),
            hazards = listOf(
                Hazard(
                    id = "haz-6",
                    name = "Black Pod Disease Risk",
                    severity = HazardSeverity.HIGH
                )
            ),
            fertilizers = listOf(
                Fertilizer(
                    id = "fert-6",
                    name = "NPK Fertilizer",
                    quantity = 200.0,
                    unit = "kg"
                ),
                Fertilizer(
                    id = "fert-7",
                    name = "Potassium",
                    quantity = 50.0,
                    unit = "kg"
                )
            ),
            cultivation = Cultivation(
                cropType = "Cocoa",
                season = "Year-round",
                status = "Harvesting"
            ),
            cultivationGuideline = "Monitor for fungal diseases, maintain shade cover at 40-50%",
            cultivationRisk = CultivationRisk.HIGH,
            createdAt = "2025-11-20T14:00:00Z",
            updatedAt = "2026-06-14T16:20:00Z"
        )
    )

    /**
     * Get all fields, optionally filtered by region
     */
    fun getFields(region: String? = null): Flow<List<Field>> = flow {
        delay(300) // Simulate network delay

        val filtered = if (region != null) {
            mockFields.filter { it.region.equals(region, ignoreCase = true) }
        } else {
            mockFields
        }

        emit(filtered)
    }

    /**
     * Get fields by city coordinates (within a radius)
     */
    fun getFieldsNearLocation(latitude: Double, longitude: Double, radiusKm: Double = 5.0): Flow<List<Field>> = flow {
        delay(300)

        val nearbyFields = mockFields.filter { field ->
            // Check if field center is within radius
            val fieldCenter = field.coordinates.let { coords ->
                val avgLat = coords.map { it.latitude }.average()
                val avgLng = coords.map { it.longitude }.average()
                Coordinate(avgLat, avgLng)
            }

            val distance = calculateDistance(latitude, longitude, fieldCenter.latitude, fieldCenter.longitude)
            distance <= radiusKm
        }

        emit(nearbyFields)
    }

    /**
     * Get a single field by ID
     */
    fun getFieldById(fieldId: String): Flow<Field?> = flow {
        delay(200)
        emit(mockFields.find { it.id == fieldId })
    }

    /**
     * Calculate distance between two coordinates (Haversine formula)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}
