package com.mobile.sap.util

import com.mobile.sap.data.api.dto.FieldCoordinateDto
import com.mobile.sap.data.api.dto.FieldCoordinateRequest
import com.mobile.sap.data.api.dto.FieldDto
import com.mobile.sap.data.api.dto.FieldRequest
import com.mobile.sap.data.model.Coordinate
import com.mobile.sap.data.model.Cultivation
import com.mobile.sap.data.model.Field

/**
 * Translates between the normalized backend DTOs (Field + FieldCoordinate rows)
 * and the denormalized UI [Field] model that the map renders.
 *
 * The backend only persists a field's `name`, `fieldNotes`, `regionId`, and its
 * coordinates. Season / status / risk are not backend-backed yet, so they are
 * treated as display-only: they round-trip within a session but are not sent to
 * or restored from the server.
 */
object FieldMapper {

    /** Default region used when the UI region string is not a numeric id. */
    const val DEFAULT_REGION_ID: Long = 1L

    /**
     * Build a UI [Field] from a backend field plus all coordinate rows that
     * belong to it. Coordinates are ordered by `sequenceOrder`.
     */
    fun toUiField(dto: FieldDto, coordinates: List<FieldCoordinateDto>): Field {
        val ordered = coordinates
            .filter { it.fieldId == dto.ID }
            .sortedBy { it.sequenceOrder }
            .map { Coordinate(latitude = it.latitude, longitude = it.longitude) }

        // Prefer the nested region name; fall back to the numeric id as text so
        // the details card always shows something.
        val regionLabel = dto.region?.name?.takeIf { it.isNotBlank() }
            ?: dto.regionId.toString()

        val cropName = dto.name?.takeIf { it.isNotBlank() } ?: "Field"

        return Field(
            id = dto.ID.toString(),
            region = regionLabel,
            coordinates = ordered,
            cultivation = Cultivation(
                cropType = cropName,
                season = "",
                status = ""
            ),
            cultivationGuideline = dto.fieldNotes?.takeIf { it.isNotBlank() },
            cultivationRisk = null
        )
    }

    /**
     * Map a UI [Field] (from the add/edit form) to a backend field request.
     * `name` carries the crop type, `fieldNotes` carries the guideline, and the
     * region string is parsed to a numeric id when possible.
     */
    fun toFieldRequest(field: Field, farmId: Long): FieldRequest = FieldRequest(
        name = field.cultivation?.cropType?.takeIf { it.isNotBlank() } ?: "Field",
        fieldNotes = field.cultivationGuideline,
        farmId = farmId,
        regionId = field.region.trim().toLongOrNull() ?: DEFAULT_REGION_ID
    )

    /** Build ordered coordinate requests for a newly created field. */
    fun toCoordinateRequests(field: Field, fieldId: Long): List<FieldCoordinateRequest> =
        field.coordinates.mapIndexed { index, coord ->
            FieldCoordinateRequest(
                latitude = coord.latitude,
                longitude = coord.longitude,
                sequenceOrder = index,
                fieldId = fieldId
            )
        }
}
